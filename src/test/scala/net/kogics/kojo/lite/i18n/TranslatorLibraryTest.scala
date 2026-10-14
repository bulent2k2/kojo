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
package net.kogics.kojo.lite.i18n

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.Global
import scala.tools.nsc.Settings

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.Matchers

/**
 * The Translator is a LIBRARY: `translator.scala` (Translator) + `translationdictionary.scala`
 * (TranslationDictionary) + `dict.scala` (keyword tables; plain data) + the two TSVs depend only
 * on scala-library and the patched scalariform. This test's first run found `dict.scala`: the
 * Translator builds its keyword pairs from it (measured: without it 4 errors, "not found: value
 * dict"). iKojo's "Çevir" command (kojojs-dev#183) will copy these files to its server and run
 * them there; a dependency leaking into the rest of Kojo (Utils, the compiler probe, desktop
 * sources) would break compilation over there. This test catches that leak here, at its source:
 * it compiles the files with NOTHING else.
 *
 * The generator (dictionarygenerator.scala), the command line (translatormain.scala) and the
 * validator (translationvalidator.scala) are OUTSIDE the library: they read desktop sources or
 * use the compiler.
 */
@RunWith(classOf[JUnitRunner])
class TranslatorLibraryTest extends FunSuite with Matchers {
  val dir = new File("src/main/scala/net/kogics/kojo/lite/i18n/tr")
  val library = Seq("translator.scala", "translationdictionary.scala", "dict.scala")

  private def jar(c: Class[_]): String = new File(c.getProtectionDomain.getCodeSource.getLocation.toURI).getPath

  /** Type-checks the given files with only the given classpath; returns the errors. */
  def compileFiles(files: Seq[File], classPath: Seq[String]): Seq[String] = {
    val s = new Settings
    s.usejavacp.value = false
    s.classpath.value = classPath.mkString(File.pathSeparator)
    s.stopAfter.value = List("refchecks")
    s.nowarn.value = true
    s.encoding.value = "UTF-8"
    val r = new StoreReporter(s)
    val g = new Global(s, r)
    val sources = files.map { f =>
      new BatchSourceFile(f.getName, new String(Files.readAllBytes(f.toPath), StandardCharsets.UTF_8))
    }
    new g.Run().compileSources(sources.toList)
    r.infos.toSeq.filter(_.severity == r.ERROR).map(h => s"${h.pos.source.file.name}:${h.pos.line}: ${h.msg}")
  }

  val libraryOnlyClassPath = Seq(jar(classOf[scala.Option[_]]), jar(classOf[scalariform.lexer.Token]))

  test("the translator library compiles with only scala-library and scalariform (kojojs-dev#183)") {
    assume(dir.isDirectory, "must run from the repo root")
    compileFiles(library.map(new File(dir, _)), libraryOnlyClassPath) shouldBe empty
  }

  test("the test really catches a dependency leak") {
    // The assertion itself must be tested too: a copy of the same files that touches Kojo must be red,
    // otherwise the assertion above (e.g. if the class path accidentally contained all of Kojo) would be green for nothing.
    assume(dir.isDirectory, "must run from the repo root")
    val tempDir = Files.createTempDirectory("translator-library").toFile
    try {
      val leaker = new File(tempDir, "leaker.scala")
      Files.write(leaker.toPath,
        "package net.kogics.kojo.lite.i18n.tr\nobject Leak { val y = net.kogics.kojo.util.Utils.loadResource(\"/x\") }\n"
          .getBytes(StandardCharsets.UTF_8))
      val errors = compileFiles(library.map(new File(dir, _)) :+ leaker, libraryOnlyClassPath)
      errors should not be empty
      errors.mkString should include("leaker.scala")
    }
    finally {
      tempDir.listFiles().foreach(_.delete()); tempDir.delete()
    }
  }

  test("the library's dictionary loads from its own resources") {
    val s = net.kogics.kojo.lite.i18n.tr.TranslationDictionary.load()
    s.rows.size should be > 1000
    s.rules.size should be > 50
  }
}
