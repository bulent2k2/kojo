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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import net.kogics.kojo.lite.i18n.tr.Translator
import net.kogics.kojo.lite.i18n.tr.TranslationValidator
import net.kogics.kojo.lite.i18n.tr.DictionaryGenerator

/**
 * The claim that the translation COMPILES, over the repo's sample scripts.
 *
 * The criterion is "scripts the translation breaks": a script whose original passes type checking
 * with the prelude of its own language, but whose translation does not pass with the prelude of
 * the other language. Scripts whose original does not compile anyway (run-time features,
 * `#include`) do not count.
 *
 * The known breakages are listed below BY NAME and WITH REASON. The test demands that the set be
 * EXACTLY this: if a new breakage appears it fails (regression), and if a breakage gets fixed it
 * also fails (remove it from the list, so the progress is recorded). That way the answer to
 * "how many scripts compile" is always written in the tree.
 *
 * Duration: four batched type checks (Kojo's Builtins in every unit), ~30-60 s.
 */
@RunWith(classOf[JUnitRunner])
@annotation.nowarn class TranslatorCompileTest extends FunSuite with Matchers {

  def kojoFiles(dir: File): Seq[File] = {
    val fs = Option(dir.listFiles()).getOrElse(Array.empty[File]).toSeq
    fs.filter(_.isDirectory).flatMap(kojoFiles) ++ fs.filter(f => f.isFile && f.getName.endsWith(".kojo"))
  }
  val samplesRoot = new File("src/main/resources/samples")
  lazy val samples = kojoFiles(samplesRoot).sortBy(_.getPath)
  def relative(f: File) = f.getPath.stripPrefix(samplesRoot.getPath).stripPrefix("/")
  val languageDirPattern = "^[a-z]{2}/".r
  lazy val turkishSamples = samples.filter(f => relative(f).startsWith("tr/"))
  lazy val englishSamples = samples.filterNot(f => languageDirPattern.findFirstIn(relative(f)).isDefined)

  /** (number of originals that compile, scripts the translation breaks: name -> first error) */
  def breakages(files: Seq[File], direction: Translator.Direction): (Int, Map[String, String]) = {
    val sourceIsTurkish = direction == Translator.TurkishToEnglish
    val originals = files.map(f => relative(f) -> DictionaryGenerator.readFile(f))
    val translations = originals.map { case (name, code) => name -> Translator.translateScript(code, direction)._1 }
    val originalDiagnostics = TranslationValidator.typeCheck(originals, turkish = sourceIsTurkish)
    val translationDiagnostics = TranslationValidator.typeCheck(translations, turkish = !sourceIsTurkish)
    val originalsPassing = originals.map(_._1).filter(name => originalDiagnostics(name).isEmpty)
    val translationText = translations.toMap
    // The error + the TRANSLATED text of that line: the diagnosis should look at the text, not at the line number.
    val broken = originalsPassing.flatMap(name => translationDiagnostics(name).headOption.map { h =>
      val lineText = translationText(name).linesIterator.drop(h.line - 1).toSeq.headOption.getOrElse("").trim.take(100)
      name -> s"${h.toString}  ||  $lineText"
    }).toMap
    (originalsPassing.size, broken)
  }

  // Turkish -> English: known breakages and their reasons. Classes:
  //   RECEIVER -- the member name depends on the receiver's type (the same Turkish name goes to two
  //             English names); cannot be told apart at word level, the dictionary picks the most
  //             frequent one and the other breaks
  //   USER -- the user's own name collides with a script-command name; the definition site and the
  //             use site are translated in different contexts
  //   WRAPPER -- the Turkish wrapper adds/drops arguments, or has no bare English counterpart
  //   PACKAGE -- the `ay` package alias; its members are Turkish classes
  val knownTrEnBreakages: Map[String, String] = Map(
    "tr/addition-game.kojo" -> "PACKAGE: `ay.Yazıgirdisi`",
    "tr/angle-experiment.kojo" -> "RECEIVER: `textExtent(..).boyu` -> length (Rectangle.height is needed)",
    "tr/animated-square-creation.kojo" -> "RECEIVER: `yol.kondur(x, y)` GeneralPath.moveTo; the rule says setPosition (Resim/Kaplumbağa)",
    "tr/collidium.kojo" -> "USER: the user definition `doğruÇiz` and GeoYol.doğruÇiz both become lineTo, taken for recursion",
    "tr/eye-effects.kojo" -> "WRAPPER: the `sahneIşığı(...)` transformer has no bare English counterpart (spotLight is not exposed in Builtins)",
    "tr/genart-joy-division.kojo" -> "WRAPPER: `Aralık.kapalı(kesir, kesir, kesir)` Range.inclusive needs Int",
    "tr/genart-mondrian.kojo" -> "WRAPPER: `rastgeleKarıştır(EsnekDizim)` -> shuffle(ArrayBuffer) type mismatch",
    "tr/genart-tiled-lines.kojo" -> "USER: `sınıf Köşegen(eni, boyu)`: boyu is a script-command name in both contexts, definition/use diverge",
    "tr/genart-tri-mesh.kojo" -> "RECEIVER: `yol.kondur(x, y)` GeneralPath.moveTo (Dizim.boş[T](n, m) -> Array.ofDim was fixed by a rule, this is the next error)",
    "tr/hunted.kojo" -> "WRAPPER: bare `varMı(belki)`; in English it is belki.isDefined",
    "tr/image-collage.kojo" -> "RECEIVER: `kaplumbağa.yazı(..)` write; the rule binds the member yazı to Picture.text",
    "tr/l-systems.kojo" -> "RECEIVER: `StringBuilder.sayıya` -> toInt; Turkish implicit conversion",
    "tr/lamp-animation.kojo" -> "RECEIVER: `yol.kondur` GeneralPath.moveTo",
    "tr/mandelbrot.kojo" -> "WRAPPER: `Resim.imge(en, boy)` has no English Picture.image(w, h)",
    "tr/memory-cards.kojo" -> "RECEIVER: `yazıÇerçevesi.boyu` Rectangle.height",
    "tr/multiplication-game.kojo" -> "PACKAGE: `ay.Yazıgirdisi`",
    "tr/physics-uvats.kojo" -> "RECEIVER: `kaplumbağa.sil()` clear; the rule binds the member sil to erase (Resim)",
    "tr/pong.kojo" -> "RECEIVER: `belki.varMı` isDefined; the dictionary picks exists (Dizi.varMı(p))",
    "tr/sprite-boundary-polygon.kojo" -> "WRAPPER: `.yenidenÇiz`/`repaint` is specific to Resim",
    "tr/subtraction-game.kojo" -> "PACKAGE: `ay.Yazıgirdisi`",
    "tr/tangram-skier.kojo" -> "WRAPPER: `götür(nokta)` -> trans(x, y); the Turkish wrapper takes a Nokta",
    "tr/two-turtle-interaction.kojo" -> "RECEIVER: `kaplumbağa.çevir(k)` towards; the dictionary picks spin (Renk.çevir)",
    "tr/two-turtle-interaction2.kojo" -> "RECEIVER: `kaplumbağa.çevir(k)`",
    "tr/widgets-canvas.kojo" -> "PACKAGE: `ay.Yazıgirdisi`"
  )
  // English -> Turkish: known breakages and their reasons. Additional class:
  //   LAYER -- the Turkish layer has no counterpart (a gap of the layer, not of the translator)
  val knownEnTrBreakages: Map[String, String] = Map(
    "addition-game.kojo" -> "LAYER: `TextField.girdiOdağıOl`",
    "angle-experiment.kojo" -> "LAYER: `Resim.yazıBoyunuKur`",
    "animated-square-creation.kojo" -> "RECEIVER: `path.moveTo` -> ilerle (turtle); kondur on GeoYol",
    "car-ride.kojo" -> "LAYER: `moveToBackAboveStage`",
    "collidium.kojo" -> "LAYER: `Yöney2B.limit`",
    "eskiz-gezginler.kojo" -> "RECEIVER: `cd.fill(r,g,b,a)` -> doldur; boya on TuvalÇizim (same as fireworks-canvas.kojo)",
    "fireworks-canvas.kojo" -> "RECEIVER: `canvas.fill(r,g,b,a)` -> doldur; boya on TuvalÇizim",
    "genart-delaunay.kojo" -> "WRAPPER: `üçgenDöşeme(Dizi[Nokta])` does not match Seq[Point]",
    "genart-joy-division.kojo" -> "RECEIVER: `path.moveTo`",
    "genart-tri-mesh.kojo" -> "RECEIVER: `path.moveTo`",
    "instruction-palette.kojo" -> "RECEIVER: `LinkedHashMap.keys` -> anahtarlar; no Turkish implicit conversion",
    "lamp-animation.kojo" -> "RECEIVER: `path.moveTo`",
    "mandelbrot.kojo" -> "WRAPPER: `zamanTut(başlık)(işlev)(bitiş)` three parameter lists",
    "multiplication-game.kojo" -> "LAYER: `TextField.girdiOdağıOl`",
    "pong.kojo" -> "LAYER: `Resim.yazı(içerik, boy: Sayı, renk)` is missing (yazı(içerik, yazıyüzü, renk) exists)",
    "primes.kojo" -> "RECEIVER: `Stream.filter` -> ele; the Turkish implicit conversion turns it into Dizi, #:: disappears",
    "sprite-boundary-polygon.kojo" -> "LAYER: `Resim.withPenColor/withFillColor`",
    "subtraction-game.kojo" -> "LAYER: `TextField.girdiOdağıOl`",
    "tree2.kojo" -> "USER: the user variable `penWidth` and the command `penThickness` both become kalemBoyu",
    "two-turtle-interaction.kojo" -> "LAYER: no `noktayaDön(kaplumbağa)` overload",
    "two-turtle-interaction2.kojo" -> "LAYER: `noktayaDön(kaplumbağa)`",
    "unit-circle.kojo" -> "WRAPPER: `Picture.textu(içerik, yazıyüzü, renk)`: the rule yazıRenkli says (boy: Sayı); 5 samples use a size, 1 uses a typeface",
    "widgets-canvas.kojo" -> "RECEIVER: `DropDown.value` -> değeri; no Turkish wrapper"
  )

  test("when Turkish samples are translated to English, only the known scripts break") {
    assume(turkishSamples.nonEmpty, "must run from the repo root")
    val (originalsPassing, broken) = breakages(turkishSamples, Translator.TurkishToEnglish)
    info(s"Turkish samples: ${turkishSamples.size}, originals that compile: $originalsPassing, translations that also compile: ${originalsPassing - broken.size}")
    broken.foreach { case (name, error) => info(s"  broken: $name -- $error") }
    withClue("NEW breakage (regression): ") { (broken.keySet -- knownTrEnBreakages.keySet) shouldBe empty }
    withClue("FIXED breakage (remove it from the list): ") { (knownTrEnBreakages.keySet -- broken.keySet) shouldBe empty }
    originalsPassing should be >= 85
  }

  test("when English samples are translated to Turkish, only the known scripts break") {
    assume(englishSamples.nonEmpty, "must run from the repo root")
    val (originalsPassing, broken) = breakages(englishSamples, Translator.EnglishToTurkish)
    info(s"English samples: ${englishSamples.size}, originals that compile: $originalsPassing, translations that also compile: ${originalsPassing - broken.size}")
    broken.foreach { case (name, error) => info(s"  broken: $name -- $error") }
    withClue("NEW breakage (regression): ") { (broken.keySet -- knownEnTrBreakages.keySet) shouldBe empty }
    withClue("FIXED breakage (remove it from the list): ") { (knownEnTrBreakages.keySet -- broken.keySet) shouldBe empty }
    originalsPassing should be >= 75
  }

  // WHY A SEPARATE TEST: transformer names (eksenler, yansıtX, yansıtY, döndür...) are never used
  // BARE with `->` in any sample script, so the two batch tests above never translate them.
  // Measured: with 226 tests green, `eksenler` was translated to code that does not compile
  // (`not found: value AxesOn`), and `yansıtX`/`yansıtY` to a member Picture does not have.
  // The root cause was that the composable class table missed the `case object` form; when the
  // landing fails it falls back to the raw inner class name, and since that name EXISTS IN THE
  // SOURCE the phantom counter stays silent too. This test closes that silent path.
  //
  // The lights are deliberately OUT: `noktaIşık`/`sahneIşığı` find the right name
  // (picture.pointLight) but Builtins does not expose it bare -- a known wrapper gap, listed
  // above as tr/eye-effects.kojo.
  test("transformer names resolve to the composable wrapper (eksenler, yansıtX, yansıtY)") {
    val script =
      """|dez r = Resim.daire(50)
         |dez a = eksenler -> r
         |dez b = yansıtX -> r
         |dez c = (yansıtX * yansıtY) -> r
         |r.yansıtX()
         |r.yansıtY()
         |""".stripMargin
    val (output, _) = Translator.translateTurkishToEnglish(script)
    output should include("axesOn -> r")
    output should include("flipX -> r")
    output should include("(flipX * flipY) -> r")
    output should include("r.flipX()")
    // The real assertion: not just the name, it must COMPILE. The raw inner class name (AxesOn,
    // FlipX) exists in the source but cannot be composed; only the typer can tell.
    TranslationValidator.typeCheck("dönüştürücüler.kojo", output, turkish = false) shouldBe empty
  }

  test("single script: the English translation of angles.kojo compiles with the Kojo prelude (karesi included)") {
    val f = new File(samplesRoot, "tr/angles.kojo")
    assume(f.exists())
    val (output, report) = Translator.translateTurkishToEnglish(DictionaryGenerator.readFile(f))
    Translator.leftoverKeywords(output, Translator.TurkishToEnglish) shouldBe empty
    // `karesi(x)` adds an ARGUMENT (math.pow(x, 2)). It used to be impossible to carry over and
    // was deliberately left Turkish; now the rules file can write a constant argument: math.pow(1,'2).
    report.remainingTurkishNames.keySet should not contain "karesi"
    output should include("math.pow(")
    output should not include "karesi"
    TranslationValidator.typeCheck("angles.kojo", output, turkish = false) shouldBe empty
  }

  test("rule with a constant argument: karesi(x) -> math.pow(x, 2)") {
    val (output, _) = Translator.translateTurkishToEnglish("tanım u(x: Kesir) = karesi(x) + karesi(x + 1)\n")
    output shouldBe "def u(x: Double) = math.pow(x, 2) + math.pow(x + 1, 2)\n"
    // In a definition context the parentheses hold a PARAMETER list; no constant must be written there
    val (userVersion, _) = Translator.translateTurkishToEnglish("tanım karesi(x: Kesir) = x * x\ndez y = karesi(2.0)\n")
    userVersion should include("def pow(x: Double) = x * x")
  }
}
