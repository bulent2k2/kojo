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
import net.kogics.kojo.lite.i18n.tr.Translator.EnglishToTurkish
import net.kogics.kojo.lite.i18n.tr.Translator.TurkishToEnglish
import net.kogics.kojo.lite.i18n.tr.TranslationValidator
import net.kogics.kojo.lite.i18n.tr.TranslationDictionary
import net.kogics.kojo.lite.i18n.tr.DictionaryGenerator
import net.kogics.kojo.lite.i18n.tr.dict

/**
 * Tests for the Translator (Turkish <-> English script translation).
 *
 * Three layers: (1) unit tests -- what gets translated and what is left alone; (2) dictionary
 * freshness -- is translation-dictionary.tsv identical when regenerated from the sources;
 * (3) the sample corpus -- when every sample script in the repo is translated in both directions,
 * does any keyword of the source language remain. (3) is NOT compilation: the "it compiles"
 * claim needs the target language's compiler, which is a separate run (TranslatorMain --validate).
 */
@RunWith(classOf[JUnitRunner])
@annotation.nowarn class TranslatorTest extends FunSuite with Matchers {

  def tr2en(code: String) = Translator.translateTurkishToEnglish(code)._1
  def en2tr(code: String) = Translator.translateEnglishToTurkish(code)._1

  // ---- keywords ------------------------------------------------------------

  test("the keyword table is built from the lexer: 39 pairs, given/verilen left out") {
    // scalariform does not know the Scala 3 words (given, enum, then, export) in either language;
    // of the 41 pairs in dict.keywordTranslation, 39 match a token type. true/false/null are
    // CONSTANTS in the lexer, but they must be in the table: doğru/yanlış/yok get translated too.
    Translator.keywordTable.size shouldBe 39
    tr2en("doğru yanlış yok") shouldBe "true false null"
    Translator.keywordTable.values.map(_._1).toSet should not contain "given"
    Translator.keywordTable.values.map(_._2).toSet should not contain "verilen"
  }

  test("dict.turkishKeywords and dict.keywordTranslation describe the same words") {
    // The same file has two lists; one feeds the lexer, the other the translator.
    // If they drift apart, the translator mistakes a keyword for an identifier.
    val translatedKeywords = dict.keywordTranslation.values.toSet
    dict.turkishKeywords.filterNot(translatedKeywords) shouldBe empty
  }

  test("dez/den/tanım/eğer/yoksa/eşle/durum/doğru/yanlış -> val/var/def/if/else/match/case/true/false") {
    tr2en("dez a = doğru\nden b = yanlış\ntanım f(x: Sayı) = eğer (x > 0) x yoksa -x\na eşle { durum doğru => 1 }") shouldBe
      "val a = true\nvar b = false\ndef f(x: Int) = if (x > 0) x else -x\na match { case true => 1 }"
  }

  test("reverse direction: val/var/def/if/else -> dez/den/tanım/eğer/yoksa") {
    en2tr("val a = 1\nvar b = a\ndef f(n: Int) = if (n > 0) n else 0") shouldBe
      "dez a = 1\nden b = a\ntanım f(n: Sayı) = eğer (n > 0) n yoksa 0"
  }

  test("given/verilen is not a keyword to the lexer; it is translated from the rules file") {
    tr2en("verilen x: Sayı = 1") shouldBe "given x: Int = 1"
    en2tr("given x: Int = 1") shouldBe "verilen x: Sayı = 1"
  }

  // ---- what is left untouched ----------------------------------------------

  test("string literals and comments are left alone, but $name inside a string is code") {
    val code = "dez ileri = 3 // ileri sağ dez yorumu\n/* tanım */ satıryaz(s\"ileri $ileri ${ileri + 1} dez\")"
    tr2en(code) shouldBe "val forward = 3 // ileri sağ dez yorumu\n/* tanım */ println(s\"ileri $forward ${forward + 1} dez\")"
  }

  test("backquoted names stay as they are") {
    tr2en("dez `ileri` = 1\nileri(`ileri`)") shouldBe "val `ileri` = 1\nforward(`ileri`)"
  }

  test("line count and whitespace are preserved: the output is aligned with the input line by line") {
    val code = "yinele(4) {\n    ileri(100)\n\n    sağ()\n}\n"
    val output = tr2en(code)
    output.count(_ == '\n') shouldBe code.count(_ == '\n')
    output shouldBe "repeat(4) {\n    forward(100)\n\n    right()\n}\n"
  }

  test("names that are not in the dictionary are left untouched and reported") {
    val (output, report) = Translator.translateTurkishToEnglish("dez kenarUzunluğu = 10\nileri(kenarUzunluğu)")
    output shouldBe "val kenarUzunluğu = 10\nforward(kenarUzunluğu)"
    report.remainingTurkishNames shouldBe Map("kenarUzunluğu" -> 2)
    report.translatedCount shouldBe 1
  }

  test("an English name that collides with a Turkish keyword is backquoted (den = denominator)") {
    en2tr("val den = 3\nval x = num / den") shouldBe "dez `den` = 3\ndez x = num / `den`"
  }

  // ---- context rules -------------------------------------------------------

  test("sil: bare -> clear, member -> erase (translation-rules.tsv)") {
    // lowercase `resim` is the user's own name, not in the dictionary: it stays as is.
    tr2en("sil()\nresim.sil()") shouldBe "clear()\nresim.erase()"
  }

  test("disablePanAndZoom: EN->TR gives the wrapper yaklaşmayaİzinVerme, not its alias tuvaliSabitle; both go back to disablePanAndZoom") {
    en2tr("disablePanAndZoom()") shouldBe "yaklaşmayaİzinVerme()"
    tr2en("yaklaşmayaİzinVerme()") shouldBe "disablePanAndZoom()"
    tr2en("tuvaliSabitle()") shouldBe "disablePanAndZoom()"
  }

  test("CanvasDraw receiver (EN->TR): fill, rotate, beginShape and endShape go to the canvas names -- declaration or lambda") {
    // declared parameter
    val declared = en2tr("def f(cd: CanvasDraw) { cd.fill(1, 2, 3); cd.rotate(0.5); cd.beginShape(); cd.endShape() }")
    declared should include("cd.boya(1, 2, 3)")
    declared should include("cd.döndürRadyan(0.5)")
    declared should include("cd.şekilBaşla()")
    declared should include("cd.şekilBitir()")
    // single-block lambda
    en2tr("animateWithCanvasDraw { c => c.fill(0, 0, 0, 30) }") should include("c.boya(0, 0, 0, 30)")
    // setup variant: both blocks
    val two = en2tr("animateWithSetupCanvasDraw { a => a.fill(1, 1, 1) } { b => b.rotate(1) }")
    two should include("a.boya(1, 1, 1)")
    two should include("b.döndürRadyan(1)")
  }

  test("CanvasDraw receiver (EN->TR): a picture's fill/rotate, and a lambda nested in the canvas lambda, are not touched") {
    // not a canvas: the old translation
    val pic = en2tr("def g(p: Picture) { p.rotate(30) }")
    pic should include("p.döndür(30)")
    pic should not include "döndürRadyan"
    // a nested lambda's parameter is not the canvas
    val nested = en2tr("animateWithCanvasDraw { c => items.foreach { p => p.rotate(1) } }")
    nested should include("p.döndür(1)")
    nested should not include "p.döndürRadyan"
  }

  test("CanvasDraw receiver round trip: TuvalÇizim fill/rotate survive TR->EN->TR") {
    val tr = "tanım f(tuval: TuvalÇizim) { tuval.boya(1, 2, 3); tuval.döndürRadyan(0.5); tuval.şekilBaşla(); tuval.şekilBitir() }"
    en2tr(tr2en(tr)) shouldBe tr
  }

  test("Eskiz: TR->EN drops `extends Eskiz` and renames kurulum/çizimDöngüsü to setup/drawLoop inside the class") {
    val tr =
      """sınıf Gezginler yayar Eskiz {
        |  tanım kurulum(tuval: TuvalÇizim) { tuval.artalan(siyah) }
        |  tanım çizimDöngüsü(tuval: TuvalÇizim) { kurulum(tuval) }
        |}
        |çiz(Resim.eskizden(yeni Gezginler))
        |""".stripMargin
    val en = tr2en(tr)
    en should include("class Gezginler {")
    en should not include "Eskiz"
    en should include("def setup(tuval: CanvasDraw)")
    en should include("def drawLoop(tuval: CanvasDraw)")
    en should include("setup(tuval)") // the call inside the class is renamed too
    en should not include "kurulum"
    en should not include "çizimDöngüsü"
    en should include("draw(Picture.fromSketch(new Gezginler))")
  }

  test("Eskiz: EN->TR adds `yayar Eskiz` to a class with setup + drawLoop taking a CanvasDraw, and renames them back") {
    val en =
      """class Sketch {
        |  def setup(cd: CanvasDraw) { cd.background(black) }
        |  def drawLoop(cd: CanvasDraw) { cd.point(1, 2) }
        |}
        |draw(Picture.fromSketch(new Sketch))
        |""".stripMargin
    val tr = en2tr(en)
    tr should include("sınıf Sketch yayar Eskiz {")
    tr should include("tanım kurulum(cd: TuvalÇizim)")
    tr should include("tanım çizimDöngüsü(cd: TuvalÇizim)")
    tr should include("çiz(Resim.eskizden(yeni Sketch))")
  }

  test("Eskiz: round trip TR->EN->TR keeps the sketch shape") {
    val tr = "sınıf İzler yayar Eskiz {\n  tanım kurulum(t: TuvalÇizim) { t.artalan(siyah) }\n  tanım çizimDöngüsü(t: TuvalÇizim) { t.nokta(1, 2) }\n}\n"
    en2tr(tr2en(tr)) shouldBe tr
  }

  test("Eskiz: only the exact shape is rewritten (other parents, only one method, member access, other classes)") {
    // another parent clause: left as it is
    tr2en("sınıf X yayar Eskiz ile Y { tanım kurulum(t: TuvalÇizim) {} }") should include("Eskiz")
    // EN->TR: setup alone, or a class that already has a parent, is not a sketch
    en2tr("class A { def setup(cd: CanvasDraw) {} }") should not include "Eskiz"
    en2tr("class B extends C { def setup(cd: CanvasDraw) {}; def drawLoop(cd: CanvasDraw) {} }") should not include "Eskiz"
    // a method of the same name in an ordinary class, and a use after a dot, are not renamed
    val other = tr2en("sınıf Y { tanım kurulum() {} }\nsınıf Z yayar Eskiz { tanım kurulum(t: TuvalÇizim) { y.kurulum() } }")
    other should include("class Y { def kurulum() {} }")
    other should include("y.kurulum()")
  }

  test("nokta: point if the receiver is a TuvalÇizim, vertex otherwise -- type declaration or the lambda of canlandırTuvalÇizimle (fireworks-canvas)") {
    // declared parameter
    tr2en("tanım göster(tuval: TuvalÇizim) { tuval.nokta(1, 2) }") should include("tuval.point(1, 2)")
    // single-block lambda
    tr2en("canlandırTuvalÇizimle { t => t.nokta(1, 2) }") should include("t.point(1, 2)")
    // form with setup: two blocks, the parameter of both is a canvas
    val twoBlocks = tr2en("canlandırKurulumluTuvalÇizimle { a => a.nokta(0, 0) } { b => b.nokta(3, 4) }")
    twoBlocks should include("a.point(0, 0)")
    twoBlocks should include("b.point(3, 4)")
    // a receiver that is not a TuvalÇizim: the old behavior (GeoNokta.nokta = vertex)
    tr2en("tanım f(n: GeoNokta) { n.nokta(1, 2) }") should include("n.vertex(1, 2)")
    // a parameter of ANOTHER lambda INSIDE the lambda does not count as a canvas
    tr2en("canlandırTuvalÇizimle { t => liste.herbiriİçin { p => p.nokta(1, 2) } }") should include("p.vertex(1, 2)")
  }

  test("saydamlık: told apart by arity -- member( opacityMod, member opacity, bare opac (#75)") {
    // All three candidates had n=1, so the decision was left to alphabetical order, and `opac`,
    // which is NOT a member of Picture, won (a row generated for the bare script command). Since
    // two English methods differ only by arity, a single name-level rule is not enough; measured:
    //   member opacityMod -> `r.saydamlık` is broken ("missing argument list")
    //   member opacity    -> `r.saydamlık(0.5)` is broken ("Double does not take parameters")
    // lowercase `r` is the user's own name, not in the dictionary: it stays as is.
    tr2en("r.saydamlık(0.5)\nr.saydamlık\nsaydamlık(0.5)") shouldBe
      "r.opacityMod(0.5)\nr.opacity\nopac(0.5)"
  }

  test("the member( context is looked up on the SAME LINE: a next line starting with ( is not a call (#75)") {
    // nextSignificant skips the line end, so a member name at the end of a line would have taken
    // the `(` that starts the next line for its own argument list. In Scala 2 these are two
    // separate statements; the arity distinction therefore looks at the line number too.
    tr2en("r.saydamlık\n(0.5)") shouldBe "r.opacity\n(0.5)"
  }

  test("the member( context keeps using the MEMBER table (#75 trap)") {
    // If `member(` is not added to isMemberContext(), every member call WITH arguments falls into
    // the bare table, and rows marked `member` -- most of the dictionary -- stop being candidates.
    // A silent and wide regression: two corpus assertions broke at once in the breakage test.
    // This test pins it with a single name, quickly:
    // varsayılanDeğerle comes only from a row marked `member` and takes an argument.
    tr2en("m.varsayılanDeğerle(0)") shouldBe "m.withDefaultValue(0)"
  }

  test("götür/döndür: bare transformer trans/rot, member translate/rotate") {
    tr2en("götür(10, 0) -> r\nr.götür(10, 0)\ndöndür(30) -> r\nr.döndür(30)") shouldBe
      "trans(10, 0) -> r\nr.translate(10, 0)\nrot(30) -> r\nr.rotate(30)"
  }

  test("konumuOku/yönüOku -> readPosition/readHeading (iKojo), in both directions; turtle member too") {
    tr2en("konumuOku { n => satıryaz(n.x) }\nk1.yönüOku { a => satıryaz(a) }") shouldBe
      "readPosition { n => println(n.x) }\nk1.readHeading { a => println(a) }"
    en2tr("readPosition { p => println(p.y) }\nreadHeading(println)") shouldBe
      "konumuOku { p => satıryaz(p.y) }\nyönüOku(satıryaz)"
  }

  test("yinele has a block body; the rule binds it to repeat (frequency was pushing it to iterate)") {
    tr2en("yinele(3) { ileri(10) }") shouldBe "repeat(3) { forward(10) }"
    en2tr("repeat(3) { forward(10) }") shouldBe "yinele(3) { ileri(10) }"
  }

  test("göster: bare and member are both visible; the user's own göster() is consistent between definition and call") {
    tr2en("göster()\nateş.göster()") shouldBe "visible()\nateş.visible()"
    // the definition was bare (visible), the call was member (setVisible): the two diverged in lunar-lander
    tr2en("tanım göster() = ateş.göster()\ngöster()") shouldBe
      "def visible() = ateş.visible()\nvisible()"
  }

  test("durdur: the member of Mp3Çalar is stopMp3, bare durdur stays stopAnimation") {
    tr2en("frenSesiÇalar.durdur()\ndurdur()") shouldBe "frenSesiÇalar.stopMp3()\nstopAnimation()"
  }

  test("iKojo-specific Resim names: kalemKalınlığı/döndürme/renkliYazı/soluk (unit circle script)") {
    tr2en("çiz(kalemKalınlığı(4) * döndürme(30) -> Resim.daire(5))") shouldBe
      "draw(penThickness(4) * rot(30) -> Picture.circle(5))"
    // there is NO rule for büyütme: lamp-animation2 writes its own `tanım büyütme`, and the rule made it collide with büyüt on the same scale
    tr2en("büyütme(2)") shouldBe "büyütme(2)"
    tr2en("çiz(öteleme(0, 1) -> Resim.renkliYazı(\"a\", 40, mavi))") shouldBe
      "draw(trans(0, 1) -> Picture.textu(\"a\", 40, blue))"
    // the member soluk of Renk = fadeOut; the bare Resim transformer soluk(n) = fade
    tr2en("dez r = yeşil.soluk(0.8)\nçiz(soluk(100) -> Resim.daire(5))") shouldBe
      "val r = green.fadeOut(0.8)\ndraw(fade(100) -> Picture.circle(5))"
    tr2en("canlandırmayıDurdur()") shouldBe "stopAnimation()"
  }

  test("a user variable that collides with a library name that is only a MEMBER is left alone (merkez, zaman)") {
    // in the dictionary `merkez` is only the member `ay.değişmez.merkez`, and `zaman` only `Buan.zaman`:
    // a bare use that never comes after a dot is the user's own name (not CENTER/all)
    tr2en("dez merkez = 3\nçiz(merkez)\nden zaman = 0\nzaman += 1") shouldBe
      "val merkez = 3\ndraw(merkez)\nvar zaman = 0\nzaman += 1"
  }

  test("Dizim/Dizik.boş with sizes: Array.ofDim; EsnekDizim.boş stays ArrayBuffer.empty") {
    tr2en("Dizim.boş[Nokta](3, 4)") shouldBe "Array.ofDim[Point](3, 4)"
    tr2en("Dizik.boş[Sayı](2)") shouldBe "Array.ofDim[Int](2)"
    tr2en("EsnekDizim.boş[Nokta]") should startWith("ArrayBuffer.empty")
  }

  test("an ambiguous choice is reported, not swallowed silently; a clear winner is not reported") {
    // Artificial dictionary: `al` is take in 3 definitions, get in 2 (serious alternative: 2*2 >= 3);
    // `ileri` is forward in 5, advance in 1 (1*2 < 5: not reported).
    import TranslationDictionary.Row
    val s = new TranslationDictionary.Dictionary(
      Seq(Row("def", "al", "take", "a.scala", 3), Row("def", "al", "get", "b.scala", 2),
          Row("def", "ileri", "forward", "a.scala", 5), Row("def", "ileri", "advance", "b.scala", 1)),
      Nil)
    val (output, report) = Translator.translateScript("al(2)\nileri(3)", TurkishToEnglish, s)
    output shouldBe "take(2)\nforward(3)"
    report.ambiguities.map(b => (b.name, b.chosen, b.alternatives)) shouldBe Seq(("al", "take", Seq("get")))
  }

  test("the count column stands in for row multiplicity: both forms give the same frequency") {
    // When the source column dropped to file level, identical pairs in the same file collapsed
    // into one row; the weight moved to `count`. The old form, written as separate rows,
    // must give the same result.
    import TranslationDictionary.Row
    def translateWith(rows: Seq[Row]) = Translator.translateScript("al(2)", TurkishToEnglish, new TranslationDictionary.Dictionary(rows, Nil))
    val separateRows = Seq(Row("def", "al", "take", "a.scala"), Row("def", "al", "take", "b.scala"),
                           Row("def", "al", "take", "c.scala"), Row("def", "al", "get", "d.scala"))
    val merged = Seq(Row("def", "al", "take", "a.scala", 3), Row("def", "al", "get", "d.scala", 1))
    val (out1, report1) = translateWith(separateRows)
    val (out2, report2) = translateWith(merged)
    out1 shouldBe "take(2)"
    out2 shouldBe out1
    report2.ambiguities.map(b => (b.name, b.chosen, b.alternatives)) shouldBe report1.ambiguities.map(b => (b.name, b.chosen, b.alternatives))
  }

  test("on a tie, a wrapper row beats the data.scala table") {
    import TranslationDictionary.Row
    val s = new TranslationDictionary.Dictionary(
      Seq(Row("def", "kosinüs", "cos", "data.scala"), Row("def", "kosinüs", "math.cos", "matematik.scala")), Nil)
    Translator.translateScript("kosinüs(1)", TurkishToEnglish, s)._1 shouldBe "math.cos(1)"
  }

  // ---- deprecated names (#63) ----------------------------------------------
  //
  // In EN->TR a deprecated Turkish name must NOT be PRODUCED: the translated script compiles but
  // immediately raises a deprecation warning and teaches the student exactly the spelling we are
  // moving away from. The problem only shows up in TIES -- among key names a tie is the rule,
  // because each name is a single `val`. The remedy is a sort criterion, NOT a filter: the
  // deprecated name must stay available in TR->EN.

  test("on a tie, a non-deprecated name beats a deprecated one (#63)") {
    import TranslationDictionary.{Row, DeprecatedNote}
    val s = new TranslationDictionary.Dictionary(
      Seq(
        Row("val", "back_space", "backSpace", "klavye.scala", 1, DeprecatedNote),
        Row("val", "silGeri", "backSpace", "klavye.scala")
      ), Nil)
    // The names are the RECORD's own state: alphabetical order puts `back_space` ('b') before
    // `silGeri` ('s'), so without the criterion the DEPRECATED name would win.
    // CAUTION: when I first wrote this I picked `sil_geri`/`silGeri`, and the test stayed GREEN
    // under mutation -- there alphabetical order already picks the right one ('G' 71 < '_' 95),
    // so the test claimed the right thing but could not fail.
    Translator.translateScript("keys.backSpace", EnglishToTurkish, s)._1 shouldBe "keys.silGeri"
  }

  test("a deprecated name is STILL translated in TR->EN: the criterion is not a filter (#63)") {
    import TranslationDictionary.{Row, DeprecatedNote}
    val s = new TranslationDictionary.Dictionary(
      Seq(
        Row("val", "sil_geri", "backSpace", "klavye.scala", 1, DeprecatedNote),
        Row("val", "silGeri", "backSpace", "klavye.scala")
      ), Nil)
    // An old Koco script may have written `sil_geri`; it must still be translatable.
    Translator.translateScript("keys.sil_geri", TurkishToEnglish, s)._1 shouldBe "keys.backSpace"
  }

  test("frequency beats deprecation: the criterion is consulted AFTER -n (#63)") {
    import TranslationDictionary.{Row, DeprecatedNote}
    val s = new TranslationDictionary.Dictionary(
      Seq(
        Row("val", "çokKaynaklı", "x", "a.scala", 3, DeprecatedNote),
        Row("val", "azKaynaklı", "x", "b.scala", 1)
      ), Nil)
    // If deprecation took precedence over frequency, `azKaynaklı` would win. Frequency must stay
    // the dominant criterion, or a well-documented name loses to a single-source sibling.
    Translator.translateScript("x", EnglishToTurkish, s)._1 shouldBe "çokKaynaklı"
  }

  test("with the real dictionary: backSpace/pageUp are translated to camel case (#63 repro)") {
    en2tr("val a = keys.backSpace\nval b = keys.pageUp") shouldBe "dez a = keys.silGeri\ndez b = keys.sayfaYukarı"
  }

  test("with the real dictionary: deprecated snake-case spellings are still translated TR->EN (#63)") {
    tr2en("dez a = keys.sil_geri\ndez b = keys.sayfa_yukarı") shouldBe "val a = keys.backSpace\nval b = keys.pageUp"
  }

  test("receiver context: a color name comes from Renkler, a constructor call from Renk") {
    en2tr("val a = ColorMaker.khaki\nval b = ColorMaker.hsla(1, 2, 3, 4)") shouldBe "dez a = Renkler.haki\ndez b = Renk.adas(1, 2, 3, 4)"
  }

  // ---- round trip ----------------------------------------------------------

  test("the core turtle/picture script returns to itself in a round trip") {
    val code =
      """silVeSakla()
        |dez r = kalemRengi(kırmızı) * boyaRengi(mavi) -> Resim.daire(50)
        |çiz(r)
        |yinele(4) {
        |    ileri(100)
        |    sağ(90)
        |}
        |satıryaz(s"bitti ${r.boyu}")
        |""".stripMargin
    en2tr(tr2en(code)) shouldBe code
  }

  // ---- dictionary freshness ------------------------------------------------

  test("translation-dictionary.tsv is identical when regenerated from the sources (the commit after the generator was not forgotten)") {
    val root = new File(".")
    assume(new File(root, "src/main/scala/net/kogics/kojo/lite/i18n/trInit.scala").exists(), "must run from the repo root")
    val generated = DictionaryGenerator.extract(root)
    val committed = Translator.dictionary.rows
    withClue("run DictionaryGenerator and commit translation-dictionary.tsv: ") {
      generated.toSet -- committed.toSet shouldBe empty
      committed.toSet -- generated.toSet shouldBe empty
    }
  }

  test("translation-dictionary.tsv: source is a file name (no line number), count is positive, no duplicate rows") {
    val rows = Translator.dictionary.rows
    rows.size should be > 2000
    // A source carrying a line number turned the freshness test red even for an UNRELATED shift
    // in the wrapper files (measured: #60, then when #58/#61 reached master).
    // The number exists during generation and is dropped when writing the file (DictionaryGenerator.merge).
    withClue("line number in source: ") {
      rows.map(_.source).filter(_.matches(""".*:\d+$""")).distinct shouldBe empty
    }
    rows.filter(_.count < 1) shouldBe empty
    // Multiplicity lives in the count column; the same (kind, tr, en, source, note) is not written twice.
    val rowKeys = rows.map(s => (s.kind, s.tr, s.en, s.source, s.note))
    rowKeys.diff(rowKeys.distinct) shouldBe empty
    // Merging really adds something up: at least one pair comes from more than one definition.
    rows.map(_.count).sum should be > rows.size
  }

  test("translation-dictionary.tsv parser: a missing or non-numeric count column is an error") {
    // count carries the frequency weight; silently defaulting a missing column to 1 would
    // quietly mis-weight the row (review #65).
    def parse(line: String) = TranslationDictionary.parseRows(line)
    an[RuntimeException] should be thrownBy parse("def\tal\ttake\ta.scala")
    an[RuntimeException] should be thrownBy parse("def\tal\ttake\ta.scala\tmany")
    an[RuntimeException] should be thrownBy parse("def\tal\ttake\ta.scala\t0")
    parse("def\tal\ttake\ta.scala\t3\tmember-only") shouldBe Seq(TranslationDictionary.Row("def", "al", "take", "a.scala", 3, "member-only"))
  }

  test("translation-rules.tsv: valid direction/context, no duplicate keys") {
    val rules = Translator.dictionary.rules
    rules.map(_.direction).toSet should contain only ("tr>en", "en>tr")
    // Read from the constants, not from hand-written strings: when `member(` was added (#75) this
    // list went stale and the test turned red -- correctly caught, but keeping the list in two
    // places is needless.
    val validContexts = Set(TranslationDictionary.BareContext, TranslationDictionary.MemberContext,
      TranslationDictionary.MemberCallContext, TranslationDictionary.ReceiverContext, TranslationDictionary.ReceiverCallContext,
      TranslationDictionary.AnyContext)
    rules.map(_.context).filterNot(b => validContexts(b) || b.endsWith(".")) shouldBe empty
    val ruleKeys = rules.map(k => (k.direction, k.name, k.context))
    ruleKeys.diff(ruleKeys.distinct) shouldBe empty
  }

  test("en>tr rule targets resolve in English Kojo when translated back (no one-way rules)") {
    // Review #62 (A): there was `stageBorder -> Resim.tuval` but no `Resim.tuval -> ?`; the back
    // translation fell to another `tuval` of the generated dictionary (canvas, member) and
    // produced `Picture.canvas`. The qualified target of every en>tr rule (Resim.x, ^Resim.x,
    // ^renkler.x) is passed through tr>en and compiled to see whether it resolves in the English
    // prelude. Not a context-free name-to-name comparison:
    // GPics -> Resim.dizi -> picStack is a valid round trip.
    val targets = Translator.dictionary.rules.collect {
      case k if k.direction == "en>tr" && k.target != TranslationDictionary.LeaveUntouched && k.target.contains('.') =>
        k.target.stripPrefix(TranslationDictionary.SwallowReceiver).replaceAll("\\(\\d+(,\\d+)*\\)$", "")
    }.distinct
    targets should not be empty
    val backTranslated = targets.map(h => h -> Translator.translateTurkishToEnglish(h)._1.trim)
    val resolvedNames = TranslationValidator.resolvedBareNames(backTranslated.map { case (_, en) => (en, false) }, turkish = false)
    val broken = backTranslated.filterNot { case (_, en) => resolvedNames(en) }
    withClue(s"rule targets whose back translation does not resolve in English: ${broken.map { case (tr, en) => s"$tr -> $en" }.mkString(", ")}: ") { broken shouldBe empty }
  }

  // ---- sample corpus -------------------------------------------------------

  def kojoFiles(dir: File): Seq[File] = {
    val fs = Option(dir.listFiles()).getOrElse(Array.empty[File]).toSeq
    fs.filter(_.isDirectory).flatMap(kojoFiles) ++ fs.filter(f => f.isFile && f.getName.endsWith(".kojo"))
  }
  lazy val samples = kojoFiles(new File("src/main/resources/samples")).sortBy(_.getPath)
  // samples/<lang>/ subdirectories: tr is Turkish; de, es, nl are other languages -- they are not part of the English corpus.
  val languageDirPattern = "samples/[a-z]{2}/".r
  lazy val turkishSamples = samples.filter(_.getPath.contains("/tr/"))
  lazy val englishSamples = samples.filterNot(f => languageDirPattern.findFirstIn(f.getPath).isDefined)

  test("when all Turkish samples are translated to English, no Turkish keyword remains") {
    assume(turkishSamples.nonEmpty, "must run from the repo root")
    val leftovers = turkishSamples.flatMap { f =>
      val (output, _) = Translator.translateTurkishToEnglish(DictionaryGenerator.readFile(f))
      Translator.leftoverKeywords(output, TurkishToEnglish).map(k => s"${f.getName}: $k")
    }
    leftovers shouldBe empty
    turkishSamples.size should be >= 90
  }

  test("when all English samples are translated to Turkish, no English keyword remains") {
    assume(englishSamples.nonEmpty, "must run from the repo root")
    val leftovers = englishSamples.flatMap { f =>
      val (output, _) = Translator.translateEnglishToTurkish(DictionaryGenerator.readFile(f))
      Translator.leftoverKeywords(output, EnglishToTurkish).map(k => s"${f.getName}: $k")
    }
    leftovers shouldBe empty
    englishSamples.size should be >= 80
  }
}
