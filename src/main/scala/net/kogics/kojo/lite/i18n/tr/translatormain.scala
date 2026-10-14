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

import java.io.File

/**
 * Command line:
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.TranslatorMain --tr2en script.kojo [-o output.kojo] [--validate]"
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.TranslatorMain --en2tr script.kojo [-o output.kojo] [--validate]"
 *
 * The output goes to the file (-o) or to standard output; the report always goes to standard error.
 * --validate: wraps the output in the target language's Kojo prelude and type-checks it
 * (TranslationValidator); if there are errors, prints them with the line numbers of the
 * output script and exits with code 1.
 * This is the step that closes the gap between "translated" and "works"; everything the
 * dictionary cannot handle (see the LIMITS in Translator) shows up here.
 *
 * The class name and the flags are deliberately ASCII: passing Turkish letters from the
 * shell to sbt's runMain causes trouble on some platforms.
 */
object TranslatorMain {
  private val usage =
    """usage: TranslatorMain (--tr2en | --en2tr) <input.kojo> [-o <output.kojo>] [--validate]
      |  --tr2en    translate a Turkish (Koco) script into English Kojo
      |  --en2tr    translate an English Kojo script into Turkish Koco
      |  --validate type-check the output with the target language's compiler
      |""".stripMargin

  def main(args: Array[String]): Unit = {
    val validate = args.contains("--validate")
    val (direction, restArgs) = args.toList.filterNot(_ == "--validate") match {
      case "--tr2en" :: k => (Translator.TurkishToEnglish, k)
      case "--en2tr" :: k => (Translator.EnglishToTurkish, k)
      case _              => System.err.println(usage); sys.exit(2)
    }
    val (inputPath, outputPath) = restArgs match {
      case inputArg :: "-o" :: outputArg :: Nil => (inputArg, Some(outputArg))
      case inputArg :: Nil                      => (inputArg, None)
      case _                                    => System.err.println(usage); sys.exit(2)
    }
    val code = DictionaryGenerator.readFile(new File(inputPath))
    val (output, report) = Translator.translateScript(code, direction)
    // Report: if the translation goes to stdout, the report goes to stderr (so the pipeline stays clean);
    // if it goes to a file, the report goes to stdout -- under sbt, stderr shows every line as `[error]`,
    // so a success message used to read like an error (review #62).
    val printReport: String => Unit = if (outputPath.isDefined) println(_) else System.err.println(_)
    outputPath match {
      case Some(outputArg) => java.nio.file.Files.write(new File(outputArg).toPath, output.getBytes("UTF-8")); printReport(s"written: $outputArg")
      case None            => print(output)
    }
    report.summary(direction).linesIterator.foreach(printReport)
    var failed = false
    val leftovers = Translator.leftoverKeywords(output, direction)
    if (leftovers.nonEmpty) { printReport(s"WARNING: keywords of the source language are left over: ${leftovers.mkString(", ")}"); failed = true }
    if (validate) {
      val targetIsTurkish = direction == Translator.EnglishToTurkish
      val diagnostics = TranslationValidator.typeCheck(new File(inputPath).getName, output, targetIsTurkish)
      if (diagnostics.isEmpty) printReport("validation: the translation passed type checking against the " + (if (targetIsTurkish) "Turkish" else "English") + " Kojo prelude")
      else { printReport(s"validation: ${diagnostics.size} errors"); diagnostics.foreach(h => printReport("  " + h)); failed = true }
    }
    if (failed) sys.exit(1)
  }
}
