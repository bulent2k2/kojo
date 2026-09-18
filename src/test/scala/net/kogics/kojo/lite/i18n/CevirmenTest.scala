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

import net.kogics.kojo.lite.i18n.tr.Çevirmen
import net.kogics.kojo.lite.i18n.tr.Çevirmen.İngilizcedenTürkçeyeYön
import net.kogics.kojo.lite.i18n.tr.Çevirmen.TürkçedenİngilizceyeYön
import net.kogics.kojo.lite.i18n.tr.ÇeviriDoğrulama
import net.kogics.kojo.lite.i18n.tr.ÇeviriSözlüğü
import net.kogics.kojo.lite.i18n.tr.SözlükÜreteci
import net.kogics.kojo.lite.i18n.tr.dict

/**
 * Çevirmen (Türkçe <-> İngilizce yazılımcık) sınamaları.
 *
 * Üç katman: (1) parça sınamaları -- ne çevrilir, ne dokunulmaz; (2) sözlük
 * tazeliği -- ceviri-sozlugu.tsv kaynaktan yeniden üretilince aynı mı;
 * (3) örnek gövdesi -- deponun bütün örnek betikleri iki yönde çevrilince
 * kaynak dilin anahtar sözcüğü kalmıyor mu. (3) derleme DEĞİL: "derleniyor"
 * savı hedef dilin derleyicisini ister, o ayrı bir tur (CevirmenMain --doğrula).
 */
@RunWith(classOf[JUnitRunner])
@annotation.nowarn class CevirmenTest extends FunSuite with Matchers {

  def tr2en(kod: String) = Çevirmen.türkçedenİngilizceye(kod)._1
  def en2tr(kod: String) = Çevirmen.ingilizcedenTürkçeye(kod)._1

  // ---- anahtar sözcükler ---------------------------------------------------

  test("anahtar sözcük tablosu sözcükleyiciden kurulur: 39 çift, given/verilen dışarıda") {
    // scalariform Scala 3 sözcüklerini (given, enum, then, export) iki dilde de tanımıyor;
    // dict.keywordTranslation'daki 41 çiftten 39'u jeton türüyle eşleşir. true/false/null
    // sözcükleyicide SABİT, ama tabloda olmalı: doğru/yanlış/yok da çevrilecek.
    Çevirmen.anahtarSözcükler.size shouldBe 39
    tr2en("doğru yanlış yok") shouldBe "true false null"
    Çevirmen.anahtarSözcükler.values.map(_._1).toSet should not contain "given"
    Çevirmen.anahtarSözcükler.values.map(_._2).toSet should not contain "verilen"
  }

  test("dict.turkishKeywords ile dict.keywordTranslation aynı sözcükleri anlatıyor") {
    // Aynı dosyada iki liste var; biri sözcükleyiciye, öteki çevirmene giriyor.
    // Ayrışırlarsa çevirmen bir anahtar sözcüğü tanımlayıcı sanır.
    val çeviridekiler = dict.keywordTranslation.values.toSet
    dict.turkishKeywords.filterNot(çeviridekiler) shouldBe empty
  }

  test("dez/den/tanım/eğer/yoksa/eşle/durum/doğru/yanlış -> val/var/def/if/else/match/case/true/false") {
    tr2en("dez a = doğru\nden b = yanlış\ntanım f(x: Sayı) = eğer (x > 0) x yoksa -x\na eşle { durum doğru => 1 }") shouldBe
      "val a = true\nvar b = false\ndef f(x: Int) = if (x > 0) x else -x\na match { case true => 1 }"
  }

  test("ters yön: val/var/def/if/else -> dez/den/tanım/eğer/yoksa") {
    en2tr("val a = 1\nvar b = a\ndef f(n: Int) = if (n > 0) n else 0") shouldBe
      "dez a = 1\nden b = a\ntanım f(n: Sayı) = eğer (n > 0) n yoksa 0"
  }

  test("given/verilen sözcükleyicide anahtar sözcük değil, kural dosyasından çevrilir") {
    tr2en("verilen x: Sayı = 1") shouldBe "given x: Int = 1"
    en2tr("given x: Int = 1") shouldBe "verilen x: Sayı = 1"
  }

  // ---- neye dokunulmaz -----------------------------------------------------

  test("dizgi sabitlerine ve yorumlara dokunulmaz, ama dizgi içindeki $ad koddur") {
    val kod = "dez ileri = 3 // ileri sağ dez yorumu\n/* tanım */ satıryaz(s\"ileri $ileri ${ileri + 1} dez\")"
    tr2en(kod) shouldBe "val forward = 3 // ileri sağ dez yorumu\n/* tanım */ println(s\"ileri $forward ${forward + 1} dez\")"
  }

  test("ters tırnaklı adlar olduğu gibi kalır") {
    tr2en("dez `ileri` = 1\nileri(`ileri`)") shouldBe "val `ileri` = 1\nforward(`ileri`)"
  }

  test("satır sayısı ve boşluk korunur: çıktı girdiyle satır satır hizalı") {
    val kod = "yinele(4) {\n    ileri(100)\n\n    sağ()\n}\n"
    val çıktı = tr2en(kod)
    çıktı.count(_ == '\n') shouldBe kod.count(_ == '\n')
    çıktı shouldBe "repeat(4) {\n    forward(100)\n\n    right()\n}\n"
  }

  test("sözlükte olmayan adlar dokunulmadan kalır ve raporlanır") {
    val (çıktı, rapor) = Çevirmen.türkçedenİngilizceye("dez kenarUzunluğu = 10\nileri(kenarUzunluğu)")
    çıktı shouldBe "val kenarUzunluğu = 10\nforward(kenarUzunluğu)"
    rapor.türkçeKalanlar shouldBe Map("kenarUzunluğu" -> 2)
    rapor.çevrilen shouldBe 1
  }

  test("İngilizce ad Türkçe anahtar sözcükle çakışıyorsa ters tırnağa alınır (den = payda)") {
    en2tr("val den = 3\nval x = num / den") shouldBe "dez `den` = 3\ndez x = num / `den`"
  }

  // ---- bağlam kuralları ----------------------------------------------------

  test("sil: yalın -> clear, üye -> erase (ceviri-kurallar.tsv)") {
    // `resim` küçük harfle kullanıcının adı, sözlükte yok: olduğu gibi kalır.
    tr2en("sil()\nresim.sil()") shouldBe "clear()\nresim.erase()"
  }

  test("götür/döndür: yalın dönüştürücü trans/rot, üye translate/rotate") {
    tr2en("götür(10, 0) -> r\nr.götür(10, 0)\ndöndür(30) -> r\nr.döndür(30)") shouldBe
      "trans(10, 0) -> r\nr.translate(10, 0)\nrot(30) -> r\nr.rotate(30)"
  }

  test("yinele gövdeli bir tanım; kural repeat'e bağlar (sıklık iterate'e düşürüyordu)") {
    tr2en("yinele(3) { ileri(10) }") shouldBe "repeat(3) { forward(10) }"
    en2tr("repeat(3) { forward(10) }") shouldBe "yinele(3) { ileri(10) }"
  }

  test("belirsiz seçim raporlanır, sessizce yutulmaz; açık üstünlük raporlanmaz") {
    // Yapay sözlük: `al` 3 tanımda take, 2'de get (ciddi alternatif: 2*2 >= 3);
    // `ileri` 5'te forward, 1'de advance (1*2 < 5: raporlanmaz).
    import ÇeviriSözlüğü.Satır
    val s = new ÇeviriSözlüğü.Sözlük(
      Seq(Satır("def", "al", "take", "a.scala", 3), Satır("def", "al", "get", "b.scala", 2),
          Satır("def", "ileri", "forward", "a.scala", 5), Satır("def", "ileri", "advance", "b.scala", 1)),
      Nil)
    val (çıktı, rapor) = Çevirmen.çevir("al(2)\nileri(3)", TürkçedenİngilizceyeYön, s)
    çıktı shouldBe "take(2)\nforward(3)"
    rapor.belirsiz.map(b => (b.ad, b.seçilen, b.alternatifler)) shouldBe Seq(("al", "take", Seq("get")))
  }

  test("sayı sütunu satır çokluğunun yerine geçer: iki biçim aynı sıklığı verir") {
    // Kaynak sütunu dosya düzeyine inince aynı dosyadaki özdeş çiftler tek satıra indi;
    // tartı `sayı`ya taşındı. Ayrı satırlarla yazılmış eski biçim aynı sonucu vermeli.
    import ÇeviriSözlüğü.Satır
    def çevir(ss: Seq[Satır]) = Çevirmen.çevir("al(2)", TürkçedenİngilizceyeYön, new ÇeviriSözlüğü.Sözlük(ss, Nil))
    val ayrıSatırlar = Seq(Satır("def", "al", "take", "a.scala"), Satır("def", "al", "take", "b.scala"),
                           Satır("def", "al", "take", "c.scala"), Satır("def", "al", "get", "d.scala"))
    val birleşik = Seq(Satır("def", "al", "take", "a.scala", 3), Satır("def", "al", "get", "d.scala", 1))
    val (ç1, r1) = çevir(ayrıSatırlar)
    val (ç2, r2) = çevir(birleşik)
    ç1 shouldBe "take(2)"
    ç2 shouldBe ç1
    r2.belirsiz.map(b => (b.ad, b.seçilen, b.alternatifler)) shouldBe r1.belirsiz.map(b => (b.ad, b.seçilen, b.alternatifler))
  }

  test("eşitlikte sarmalayıcı satırı data.scala tablosunu yener") {
    import ÇeviriSözlüğü.Satır
    val s = new ÇeviriSözlüğü.Sözlük(
      Seq(Satır("def", "kosinüs", "cos", "data.scala"), Satır("def", "kosinüs", "math.cos", "matematik.scala")), Nil)
    Çevirmen.çevir("kosinüs(1)", TürkçedenİngilizceyeYön, s)._1 shouldBe "math.cos(1)"
  }

  // ---- eskitilmiş adlar (#63) ----------------------------------------------
  //
  // EN->TR'de eskitilmiş bir Türkçe ad ÜRETİLMEMELİ: çevrilen betik derleniyor ama
  // anında eskitme uyarısı veriyor ve öğrenciye tam da bıraktığımız yazımı öğretiyor.
  // Sorun yalnız EŞİTLİK hallerinde çıkıyor -- tuş adlarında eşitlik kural, çünkü her ad
  // tek bir `val`. Çare süzgeç DEĞİL sıralama ölçütü: eskitilmiş ad TR->EN'de kalmalı.

  test("eşitlikte eskitilmemiş ad eskitilmişi yener (#63)") {
    import ÇeviriSözlüğü.{Satır, Eskitilmiş}
    val s = new ÇeviriSözlüğü.Sözlük(
      Seq(
        Satır("val", "back_space", "backSpace", "klavye.scala", 1, Eskitilmiş),
        Satır("val", "silGeri", "backSpace", "klavye.scala")
      ), Nil)
    // Adlar KAYDIN kendi durumu: alfabetik sıra `back_space`i ('b') `silGeri`den ('s')
    // önce koyuyor, yani ölçüt olmasa ESKİTİLMİŞ ad kazanır.
    // DİKKAT: ilk yazdığımda `sil_geri`/`silGeri` seçmiştim ve sav mutasyonda YEŞİL kaldı --
    // orada alfabetik sıra zaten doğruyu seçiyor ('G' 71 < '_' 95), yani sav doğru şeyi
    // iddia ediyor ama kırılamıyordu.
    Çevirmen.çevir("keys.backSpace", İngilizcedenTürkçeyeYön, s)._1 shouldBe "keys.silGeri"
  }

  test("eskitilmiş ad TR->EN'de HÂLÂ çevriliyor: ölçüt süzgeç değil (#63)") {
    import ÇeviriSözlüğü.{Satır, Eskitilmiş}
    val s = new ÇeviriSözlüğü.Sözlük(
      Seq(
        Satır("val", "sil_geri", "backSpace", "klavye.scala", 1, Eskitilmiş),
        Satır("val", "silGeri", "backSpace", "klavye.scala")
      ), Nil)
    // Eski bir Koco betiği `sil_geri` yazmış olabilir; çevrilebilmeli.
    Çevirmen.çevir("keys.sil_geri", TürkçedenİngilizceyeYön, s)._1 shouldBe "keys.backSpace"
  }

  test("sıklık eskitilmişliği yener: ölçüt -n'den SONRA bakıyor (#63)") {
    import ÇeviriSözlüğü.{Satır, Eskitilmiş}
    val s = new ÇeviriSözlüğü.Sözlük(
      Seq(
        Satır("val", "çokKaynaklı", "x", "a.scala", 3, Eskitilmiş),
        Satır("val", "azKaynaklı", "x", "b.scala", 1)
      ), Nil)
    // Eskitilmişlik sıklığın ÖNÜNE geçseydi `azKaynaklı` kazanırdı. Sıklık baskın
    // ölçüt kalmalı, yoksa iyi belgelenmiş bir ad tek kaynaklı bir kardeşe yenilir.
    Çevirmen.çevir("x", İngilizcedenTürkçeyeYön, s)._1 shouldBe "çokKaynaklı"
  }

  test("gerçek sözlükle: backSpace/pageUp deve yazıma çevriliyor (#63 reprosu)") {
    en2tr("val a = keys.backSpace\nval b = keys.pageUp") shouldBe "dez a = keys.silGeri\ndez b = keys.sayfaYukarı"
  }

  test("gerçek sözlükle: eskitilmiş yılan yazım TR->EN'de çevriliyor (#63)") {
    tr2en("dez a = keys.sil_geri\ndez b = keys.sayfa_yukarı") shouldBe "val a = keys.backSpace\nval b = keys.pageUp"
  }

  test("alıcı bağlamı: renk adı Renkler'den, yapıcı çağrısı Renk'ten") {
    en2tr("val a = ColorMaker.khaki\nval b = ColorMaker.hsla(1, 2, 3, 4)") shouldBe "dez a = Renkler.haki\ndez b = Renk.adas(1, 2, 3, 4)"
  }

  // ---- gidiş-dönüş ---------------------------------------------------------

  test("çekirdek kaplumbağa/resim yazılımcığı gidiş-dönüşte kendine döner") {
    val kod =
      """silVeSakla()
        |dez r = kalemRengi(kırmızı) * boyaRengi(mavi) -> Resim.daire(50)
        |çiz(r)
        |yinele(4) {
        |    ileri(100)
        |    sağ(90)
        |}
        |satıryaz(s"bitti ${r.boyu}")
        |""".stripMargin
    en2tr(tr2en(kod)) shouldBe kod
  }

  // ---- sözlük tazeliği -----------------------------------------------------

  test("ceviri-sozlugu.tsv kaynaktan yeniden üretilince aynı (üreteç sonrası commit unutulmamış)") {
    val kök = new File(".")
    assume(new File(kök, "src/main/scala/net/kogics/kojo/lite/i18n/trInit.scala").exists(), "depo kökünden koşmalı")
    val üretilen = SözlükÜreteci.çıkar(kök)
    val ağaçtaki = Çevirmen.sözlük.satırlar
    withClue("SözlükÜreteci'yi koşturup ceviri-sozlugu.tsv'yi commit'leyin: ") {
      üretilen.toSet -- ağaçtaki.toSet shouldBe empty
      ağaçtaki.toSet -- üretilen.toSet shouldBe empty
    }
  }

  test("ceviri-sozlugu.tsv: kaynak dosya adı (satır numarası yok), sayı pozitif, yinelenen satır yok") {
    val satırlar = Çevirmen.sözlük.satırlar
    satırlar.size should be > 2000
    // Satır numarası taşıyan kaynak, sarmalayıcı dosyalarındaki İLGİSİZ bir kaymada bile
    // tazelik sınamasını kırmızıya düşürüyordu (ölçüldü: #60, sonra #58/#61 master'a girince).
    // Numara üretim sırasında var, dosyaya yazılırken atılıyor (SözlükÜreteci.birleştir).
    withClue("kaynakta satır numarası: ") {
      satırlar.map(_.kaynak).filter(_.matches(""".*:\d+$""")).distinct shouldBe empty
    }
    satırlar.filter(_.sayı < 1) shouldBe empty
    // Çokluk sayı sütununda durur; aynı (cins, tr, en, kaynak, not) iki kez yazılmaz.
    val anahtarlar = satırlar.map(s => (s.cins, s.tr, s.en, s.kaynak, s.not))
    anahtarlar.diff(anahtarlar.distinct) shouldBe empty
    // Birleştirme gerçekten bir şey topluyor: en az bir çift birden çok tanımdan geliyor.
    satırlar.map(_.sayı).sum should be > satırlar.size
  }

  test("ceviri-sozlugu.tsv ayrıştırıcısı: sayı sütunu eksikse ya da sayı değilse hata verir") {
    // sayı sıklık tartısını taşıyor; eksik sütuna sessizce 1 demek satırı gürültüsüzce yanlış
    // ağırlıklandırırdı (inceleme #65).
    def ayrıştır(satır: String) = ÇeviriSözlüğü.satırlarıAyrıştır(satır)
    an[RuntimeException] should be thrownBy ayrıştır("def\tal\ttake\ta.scala")
    an[RuntimeException] should be thrownBy ayrıştır("def\tal\ttake\ta.scala\tçok")
    an[RuntimeException] should be thrownBy ayrıştır("def\tal\ttake\ta.scala\t0")
    ayrıştır("def\tal\ttake\ta.scala\t3\tüye") shouldBe Seq(ÇeviriSözlüğü.Satır("def", "al", "take", "a.scala", 3, "üye"))
  }

  test("ceviri-kurallar.tsv: geçerli yön/bağlam, yinelenen anahtar yok") {
    val kurallar = Çevirmen.sözlük.kurallar
    kurallar.map(_.yön).toSet should contain only ("tr>en", "en>tr")
    kurallar.map(_.bağlam).filterNot(b => b == "yalın" || b == "üye" || b == "alıcı" || b == "alıcı(" || b == "*" || b.endsWith(".")) shouldBe empty
    val anahtarlar = kurallar.map(k => (k.yön, k.ad, k.bağlam))
    anahtarlar.diff(anahtarlar.distinct) shouldBe empty
  }

  test("en>tr kural hedefleri geri çevrilince İngilizce Kojo'da çözülüyor (tek yönlü kural yok)") {
    // İnceleme #62 (A): `stageBorder -> Resim.tuval` vardı, `Resim.tuval -> ?` yoktu; geri çeviri
    // üretilmiş sözlüğün başka bir `tuval`ine (canvas, üye) düşüp `Picture.canvas` üretiyordu.
    // Her en>tr kuralının nitelenmiş hedefi (Resim.x, ^Resim.x, ^renkler.x) tr>en'den geçirilip
    // İngilizce başlangıçta çözülüyor mu diye derlenir. Bağlamsız ad-ad karşılaştırma değil:
    // GPics -> Resim.dizi -> picStack geçerli bir dönüş.
    val hedefler = Çevirmen.sözlük.kurallar.collect {
      case k if k.yön == "en>tr" && k.hedef != ÇeviriSözlüğü.Çevirme && k.hedef.contains('.') =>
        k.hedef.stripPrefix(ÇeviriSözlüğü.AlıcıylaBirlikte).replaceAll("\\(\\d+(,\\d+)*\\)$", "")
    }.distinct
    hedefler should not be empty
    val geri = hedefler.map(h => h -> Çevirmen.türkçedenİngilizceye(h)._1.trim)
    val çözülenler = ÇeviriDoğrulama.yalınÇözülenler(geri.map { case (_, en) => (en, false) }, türkçe = false)
    val kırık = geri.filterNot { case (_, en) => çözülenler(en) }
    withClue(s"geri çevirisi İngilizce'de çözülmeyen kural hedefleri: ${kırık.map { case (tr, en) => s"$tr -> $en" }.mkString(", ")}: ") { kırık shouldBe empty }
  }

  // ---- örnek gövdesi -------------------------------------------------------

  def kojoDosyaları(d: File): Seq[File] = {
    val fs = Option(d.listFiles()).getOrElse(Array.empty[File]).toSeq
    fs.filter(_.isDirectory).flatMap(kojoDosyaları) ++ fs.filter(f => f.isFile && f.getName.endsWith(".kojo"))
  }
  lazy val örnekler = kojoDosyaları(new File("src/main/resources/samples")).sortBy(_.getPath)
  // samples/<dil>/ alt dizinleri: tr Türkçe; de, es, nl başka diller -- İngilizce gövdeye girmez.
  val dilDizini = "samples/[a-z]{2}/".r
  lazy val türkçeÖrnekler = örnekler.filter(_.getPath.contains("/tr/"))
  lazy val ingilizceÖrnekler = örnekler.filterNot(f => dilDizini.findFirstIn(f.getPath).isDefined)

  test("bütün Türkçe örnekler İngilizceye çevrilince Türkçe anahtar sözcük kalmıyor") {
    assume(türkçeÖrnekler.nonEmpty, "depo kökünden koşmalı")
    val kalanlar = türkçeÖrnekler.flatMap { f =>
      val (çıktı, _) = Çevirmen.türkçedenİngilizceye(SözlükÜreteci.oku(f))
      Çevirmen.kalanAnahtarSözcükler(çıktı, TürkçedenİngilizceyeYön).map(k => s"${f.getName}: $k")
    }
    kalanlar shouldBe empty
    türkçeÖrnekler.size should be >= 90
  }

  test("bütün İngilizce örnekler Türkçeye çevrilince İngilizce anahtar sözcük kalmıyor") {
    assume(ingilizceÖrnekler.nonEmpty, "depo kökünden koşmalı")
    val kalanlar = ingilizceÖrnekler.flatMap { f =>
      val (çıktı, _) = Çevirmen.ingilizcedenTürkçeye(SözlükÜreteci.oku(f))
      Çevirmen.kalanAnahtarSözcükler(çıktı, İngilizcedenTürkçeyeYön).map(k => s"${f.getName}: $k")
    }
    kalanlar shouldBe empty
    ingilizceÖrnekler.size should be >= 80
  }
}
