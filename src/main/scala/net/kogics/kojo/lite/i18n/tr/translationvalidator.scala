/*
 * Copyright (C) 2026
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */
package net.kogics.kojo.lite.i18n.tr

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.StoreReporter

/**
 * Proves that translated output COMPILES: the script is wrapped the way Kojo
 * itself wraps user code (CodeExecutionSupport.compilerPrefix + the initk glue
 * for Turkish) and type-checked by a compiler that is stopped at `typer`.
 *
 * WHY: the translator works name by name and cannot handle wrappers that are
 * missing from the dictionary, that depend on the receiver's type, or that add
 * arguments (karesi = pow(x, 2)). Only the target language's compiler can close
 * the gap between "translated" and "works". This object builds that step into
 * the tool; `TranslatorMain --validate` and `TranslatorCompileTest` use it.
 *
 * LIMITS:
 *   - The compiler used is the one on the class path: under sbt, the patched
 *     compiler (the one with Turkish keywords). If a Turkish keyword is left in
 *     English output, this compiler stays silent; that check is done separately
 *     (Translator.leftoverKeywords). A leftover Turkish NAME is caught, because
 *     the English prelude does not know Turkish names.
 *   - Instead of `Builtins.instance` we use `val builtins: Builtins = ???` -- a
 *     stable name is enough for the typer, no value is needed. So this is a TYPE
 *     check, not a run.
 *   - Kojo's script execution path additionally does `import TSCanvas._; import Tw._`
 *     (the interpret call in Builtins.scala); those are included too, otherwise
 *     names like cleari wrongly came out as "not found" (measured).
 *   - If one unit has a PARSE error, scalac does not send any unit to the typer;
 *     so a parser pass first weeds out the broken units, and the typer is
 *     applied to the rest. Two Globals are created; the first is cheap.
 */
object TranslationValidator {
  final case class Diagnostic(phase: String, line: Int, message: String) {
    override def toString = s"$phase line $line: $message"
  }

  // Same structure as CodeExecutionSupport.compilerPrefix; a type signature instead of Builtins.instance.
  private val prefix =
    """object %s {
      |  val builtins: net.kogics.kojo.lite.Builtins = ???
      |  import builtins._
      |  import builtins.TSCanvas._
      |  import builtins.Tw._
      |  import net.kogics.kojo.util.ScalatestHelper.{test, ignore}
      |  import org.scalatest.Matchers
      |  class UserCode {
      |""".stripMargin
  // The code lines of src/main/resources/i18n/initk/tr.tw.kojo.
  private val turkishGlue =
    """    val TurkishAPI = net.kogics.kojo.lite.i18n.TurkishAPI
      |    import TurkishAPI.{bi => _, _ }
      |    import kaplumbağa.{sil => _, _}
      |""".stripMargin
  private val closingText = "\n  }\n}\n"

  private def objectName(name: String) = "Translation_" + name.map(c => if (c.isLetterOrDigit) c else '_')

  /** Wraps the script; returns it together with the line shift (the number of lines in the prefix). */
  def wrap(name: String, code: String, turkish: Boolean): (String, Int) = {
    val header = prefix.format(objectName(name)) + (if (turkish) turkishGlue else "")
    (header + code + closingText, header.count(_ == '\n'))
  }

  private def settingsFor(phase: String): Settings = {
    val s = new Settings
    s.usejavacp.value = true          // under sbt (fork) and runMain: Kojo's full class path
    s.stopAfter.value = List(phase)
    s.nowarn.value = true
    s.encoding.value = "UTF-8"
    // The probe produces hundreds of "not found" errors in a single unit; the default cap of 100 errors
    // swallowed the later ones and names were wrongly counted as "resolved" (measured: 16 marked, hundreds expected).
    s.maxerrs.value = 1000000
    s
  }

  private def runPhase(phase: String, units: Seq[(String, String, Int)]): Map[String, Seq[Diagnostic]] = {
    val settings = settingsFor(phase)
    val reporter = new StoreReporter(settings)
    val global = new Global(settings, reporter)
    val sources = units.map { case (name, text, _) => new BatchSourceFile(name, text) }.toList
    new global.Run().compileSources(sources)
    val lineShift = units.map { case (name, _, k) => name -> k }.toMap
    reporter.infos.toSeq.filter(_.severity == reporter.ERROR).groupBy(_.pos.source.file.name).map { case (name, hs) =>
      name -> hs.toSeq.sortBy(_.pos.line).map(h => Diagnostic(phase, h.pos.line - lineShift.getOrElse(name, 0), h.msg.linesIterator.next()))
    }
  }

  /**
   * Type-checks the scripts (name, code) against the target language's prelude.
   * Result: name -> diagnostics (empty list = compiled). `turkish`: whether the prelude is the Turkish one.
   */
  def typeCheck(scripts: Seq[(String, String)], turkish: Boolean): Map[String, Seq[Diagnostic]] = {
    val units = scripts.map { case (name, code) => val (text, lineShift) = wrap(name, code, turkish); (name, text, lineShift) }
    val parseErrors = runPhase("parser", units)
    val remainingUnits = units.filterNot { case (name, _, _) => parseErrors.contains(name) }
    val typeErrors = if (remainingUnits.isEmpty) Map.empty[String, Seq[Diagnostic]] else runPhase("typer", remainingUnits)
    scripts.map { case (name, _) => name -> parseErrors.getOrElse(name, typeErrors.getOrElse(name, Nil)) }.toMap
  }

  def typeCheck(name: String, code: String, turkish: Boolean): Seq[Diagnostic] = typeCheck(Seq(name -> code), turkish)(name)

  /**
   * Which names resolve in the Kojo prelude by their BARE name? For a value/method/object
   * `locally(name)`, for a type `type T = name`; all in a single unit, one line per name.
   * Errors OTHER than "not found" (a missing argument list, an overload ambiguity)
   * count as "resolved": the name exists, it just was not called properly here.
   *
   * WHY: no single source in the dictionary is reliable on its own -- package.scala says `çokHızlı =
   * SuperFast` (an inner enum) while data.scala says `superFast` (script level); the wrapper says
   * `kosinüs = math.cos` while the table says `cos`. The compiler is the referee: a target that does not
   * resolve can only be used in a member context (`x.length`) and is dropped from the dictionary in a bare context.
   */
  def resolvedBareNames(names: Seq[(String, Boolean)], turkish: Boolean): Set[String] = {
    if (names.isEmpty) return Set.empty
    val probeLines = names.zipWithIndex.map { case ((name, isType), n) => if (isType) s"    type Probe_$n = $name" else s"    def probe_$n = locally($name)" }
    val (text, lineShift) = wrap("probe", probeLines.mkString("\n"), turkish)
    val diagnostics = runPhase("typer", Seq(("probe", text, lineShift))).getOrElse("probe", Nil)
    val notFoundLines = diagnostics.filter(h => h.message.startsWith("not found") || h.message.contains("is not a member of")).map(_.line).toSet
    names.zipWithIndex.collect { case ((name, _), n) if !notFoundLines(n + 1) => name }.toSet
  }
}
