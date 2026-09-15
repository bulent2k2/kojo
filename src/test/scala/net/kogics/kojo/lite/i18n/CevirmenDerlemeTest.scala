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
import net.kogics.kojo.lite.i18n.tr.ÇeviriDoğrulama
import net.kogics.kojo.lite.i18n.tr.SözlükÜreteci

/**
 * Çevirinin DERLENDİĞİ savı, deponun örnek betikleri üzerinde.
 *
 * Ölçüt "çevirinin kırdığı" betik: aslı hedef... yani kendi dilinin prelude'üyle
 * tür denetiminden geçen, çevirisi öteki dilin prelude'üyle geçmeyen betik.
 * Aslı zaten derlenmeyenler (koşum zamanı özellikleri, `#include`) sayılmaz.
 *
 * Bilinen kırıklar aşağıda ADIYLA ve NEDENİYLE listeli. Sınama kümenin AYNEN
 * bu olmasını ister: yeni bir kırık çıkarsa düşer (gerileme), bir kırık
 * düzelirse de düşer (listeden çıkarılsın; ilerleme kayıt altına alınsın).
 * Böylece "kaç betik derleniyor" sorusunun cevabı hep ağaçta yazılı.
 *
 * Süre: dört toplu tür denetimi (Kojo'nun Builtins'i her birimde), ~30-60 sn.
 */
@RunWith(classOf[JUnitRunner])
@annotation.nowarn class CevirmenDerlemeTest extends FunSuite with Matchers {

  def kojoDosyaları(d: File): Seq[File] = {
    val fs = Option(d.listFiles()).getOrElse(Array.empty[File]).toSeq
    fs.filter(_.isDirectory).flatMap(kojoDosyaları) ++ fs.filter(f => f.isFile && f.getName.endsWith(".kojo"))
  }
  val örneklerKökü = new File("src/main/resources/samples")
  lazy val örnekler = kojoDosyaları(örneklerKökü).sortBy(_.getPath)
  def göreli(f: File) = f.getPath.stripPrefix(örneklerKökü.getPath).stripPrefix("/")
  val dilDizini = "^[a-z]{2}/".r
  lazy val türkçeÖrnekler = örnekler.filter(f => göreli(f).startsWith("tr/"))
  lazy val ingilizceÖrnekler = örnekler.filterNot(f => dilDizini.findFirstIn(göreli(f)).isDefined)

  /** (aslı derlenen sayısı, çevirinin kırdığı betikler: ad -> ilk hata) */
  def kırıkları(dosyalar: Seq[File], yön: Çevirmen.Yön): (Int, Map[String, String]) = {
    val kaynakTürkçe = yön == Çevirmen.TürkçedenİngilizceyeYön
    val asıllar = dosyalar.map(f => göreli(f) -> SözlükÜreteci.oku(f))
    val çeviriler = asıllar.map { case (ad, kod) => ad -> Çevirmen.çevir(kod, yön)._1 }
    val aslıHatalar = ÇeviriDoğrulama.türDenetimi(asıllar, türkçe = kaynakTürkçe)
    val çeviriHatalar = ÇeviriDoğrulama.türDenetimi(çeviriler, türkçe = !kaynakTürkçe)
    val aslıGeçen = asıllar.map(_._1).filter(ad => aslıHatalar(ad).isEmpty)
    val çeviriMetni = çeviriler.toMap
    // Hata + o satırın ÇEVRİLİ metni: teşhis satır numarasına değil metne baksın.
    val kırık = aslıGeçen.flatMap(ad => çeviriHatalar(ad).headOption.map { h =>
      val satır = çeviriMetni(ad).linesIterator.drop(h.satır - 1).toSeq.headOption.getOrElse("").trim.take(100)
      ad -> s"${h.toString}  ||  $satır"
    }).toMap
    (aslıGeçen.size, kırık)
  }

  // Türkçe -> İngilizce: bilinen kırıklar ve nedenleri. Sınıflar:
  //   ALICI  -- üye adı alıcının türüne bağlı (aynı Türkçe ad iki İngilizce ada gider); sözcük
  //             düzeyinde ayırt edilemez, sözlük en sık kullanılanı seçer, öteki kırılır
  //   KULLANICI -- kullanıcının kendi adı bir yazılımcık adıyla çakışıyor; tanım yeri ile
  //             kullanım yeri ayrı bağlamda çevriliyor
  //   SARMALAYICI -- Türkçe sarmalayıcı argüman ekliyor/çıkarıyor ya da İngilizce'de yalın karşılığı yok
  //   PAKET  -- `ay` paket takma adı; üyeleri Türkçe sınıflar
  val bilinenTrEn: Map[String, String] = Map(
    "tr/addition-game.kojo" -> "PAKET: `ay.Yazıgirdisi`",
    "tr/angle-experiment.kojo" -> "ALICI: `textExtent(..).boyu` -> length (Rectangle.height gerekli)",
    "tr/angles.kojo" -> "SARMALAYICI: `karesi(x)` = math.pow(x, 2); bilerek çevrilmiyor, görünür kalıyor",
    "tr/animated-square-creation.kojo" -> "ALICI: `yol.kondur(x, y)` GeneralPath.moveTo; kural setPosition (Resim/Kaplumbağa) diyor",
    "tr/car-ride.kojo" -> "ALICI: `mp3.durdur()` KMp3.stop; sözlük stopAnimation seçiyor",
    "tr/collidium.kojo" -> "KULLANICI: kullanıcı tanımı `doğruÇiz` ile GeoYol.doğruÇiz ikisi de lineTo oluyor, özyineleme sanılıyor",
    "tr/estimating-pi-mc.kojo" -> "SARMALAYICI: `karesi`",
    "tr/eye-effects.kojo" -> "SARMALAYICI: `sahneIşığı(...)` dönüştürücüsünün İngilizce yalın karşılığı yok (spotLight Builtins'te kapalı)",
    "tr/fireworks-canvas.kojo" -> "KULLANICI: sınıf içi `göster(tuval)` tanımı yalın (draw), çağrısı üye (setVisible)",
    "tr/fireworks.kojo" -> "KULLANICI: sınıf içi `göster()` tanımı",
    "tr/genart-joy-division.kojo" -> "SARMALAYICI: `Aralık.kapalı(kesir, kesir, kesir)` Range.inclusive Int ister",
    "tr/genart-mondrian.kojo" -> "SARMALAYICI: `rastgeleKarıştır(EsnekDizim)` -> shuffle(ArrayBuffer) tür uyuşmazlığı",
    "tr/genart-tiled-lines.kojo" -> "KULLANICI: `sınıf Köşegen(eni, boyu)`: boyu iki bağlamda da yazılımcık adı, tanım/kullanım ayrışıyor",
    "tr/genart-tri-mesh.kojo" -> "SARMALAYICI: `Dizim.boş[T](n, m)` = Array.ofDim; argümansız boş = Array.empty, tek adla ikisi olmaz",
    "tr/hunted.kojo" -> "SARMALAYICI: yalın `varMı(belki)`; İngilizce'de belki.isDefined",
    "tr/image-collage.kojo" -> "ALICI: `kaplumbağa.yazı(..)` write; kural üye yazı'yı Picture.text'e bağlıyor",
    "tr/l-systems.kojo" -> "ALICI: `StringBuilder.sayıya` -> toInt; Türkçe örtük dönüşüm",
    "tr/lamp-animation.kojo" -> "ALICI: `yol.kondur` GeneralPath.moveTo",
    "tr/lunar-lander.kojo" -> "KULLANICI: sınıf içi `göster()` tanımı",
    "tr/mandelbrot.kojo" -> "SARMALAYICI: `Resim.imge(en, boy)` İngilizce Picture.image(w, h) yok",
    "tr/memory-cards.kojo" -> "ALICI: `yazıÇerçevesi.boyu` Rectangle.height",
    "tr/multiplication-game.kojo" -> "PAKET: `ay.Yazıgirdisi`",
    "tr/physics-uvats.kojo" -> "ALICI: `kaplumbağa.sil()` clear; kural üye sil'i erase'e (Resim) bağlıyor",
    "tr/pong.kojo" -> "ALICI: `belki.varMı` isDefined; sözlük exists (Dizi.varMı(p)) seçiyor",
    "tr/sprite-boundary-polygon.kojo" -> "SARMALAYICI: `.yenidenÇiz`/`repaint` Resim'e özgü",
    "tr/subtraction-game.kojo" -> "PAKET: `ay.Yazıgirdisi`",
    "tr/tangram-skier.kojo" -> "SARMALAYICI: `götür(nokta)` -> trans(x, y); Türkçe sarmalayıcı Nokta alıyor",
    "tr/two-turtle-interaction.kojo" -> "ALICI: `kaplumbağa.çevir(k)` towards; sözlük spin (Renk.çevir) seçiyor",
    "tr/two-turtle-interaction2.kojo" -> "ALICI: `kaplumbağa.çevir(k)`",
    "tr/widgets-canvas.kojo" -> "PAKET: `ay.Yazıgirdisi`"
  )
  // İngilizce -> Türkçe: bilinen kırıklar ve nedenleri. Ek sınıf:
  //   KATMAN -- Türkçe katmanda karşılığı yok (katmanın boşluğu, çevirmenin değil)
  val bilinenEnTr: Map[String, String] = Map(
    "addition-game.kojo" -> "KATMAN: `TextField.girdiOdağıOl`",
    "angle-experiment.kojo" -> "KATMAN: `Resim.yazıBoyunuKur`",
    "animated-square-creation.kojo" -> "ALICI: `path.moveTo` -> ilerle (kaplumbağa); GeoYol'da kondur",
    "car-ride.kojo" -> "KATMAN: `moveToBackAboveStage`",
    "collidium.kojo" -> "KATMAN: `Yöney2B.limit`",
    "fireworks-canvas.kojo" -> "ALICI: `canvas.fill(r,g,b,a)` -> doldur; TuvalÇizim'de boya",
    "genart-delaunay.kojo" -> "SARMALAYICI: `üçgenDöşeme(Dizi[Nokta])` ile Seq[Point] uyuşmuyor",
    "genart-joy-division.kojo" -> "ALICI: `path.moveTo`",
    "genart-tri-mesh.kojo" -> "ALICI: `path.moveTo`",
    "instruction-palette.kojo" -> "ALICI: `LinkedHashMap.keys` -> anahtarlar; Türkçe örtük dönüşüm yok",
    "lamp-animation.kojo" -> "ALICI: `path.moveTo`",
    "mandelbrot.kojo" -> "SARMALAYICI: `zamanTut(başlık)(işlev)(bitiş)` üç parametre listesi",
    "multiplication-game.kojo" -> "KATMAN: `TextField.girdiOdağıOl`",
    "pong.kojo" -> "KATMAN: `Resim.yazı(içerik, boy: Sayı, renk)` yok (yazı(içerik, yazıyüzü, renk) var)",
    "primes.kojo" -> "ALICI: `Stream.filter` -> ele; Türkçe örtük dönüşüm Dizi'ye çeviriyor, #:: kayboluyor",
    "sprite-boundary-polygon.kojo" -> "KATMAN: `Resim.withPenColor/withFillColor`",
    "subtraction-game.kojo" -> "KATMAN: `TextField.girdiOdağıOl`",
    "tree2.kojo" -> "KULLANICI: kullanıcı değişkeni `penWidth` ile komut `penThickness` ikisi de kalemBoyu",
    "two-turtle-interaction.kojo" -> "KATMAN: `noktayaDön(kaplumbağa)` aşırı yüklemesi yok",
    "two-turtle-interaction2.kojo" -> "KATMAN: `noktayaDön(kaplumbağa)`",
    "unit-circle.kojo" -> "SARMALAYICI: `Picture.textu(içerik, yazıyüzü, renk)` kural yazıRenkli (boy: Sayı) diyor; 5 örnekte boy, 1'inde yazıyüzü",
    "widgets-canvas.kojo" -> "ALICI: `DropDown.value` -> değeri; Türkçe sarmalayıcı yok"
  )

  test("Türkçe örnekler İngilizceye çevrilince yalnız bilinen betikler kırılıyor") {
    assume(türkçeÖrnekler.nonEmpty, "depo kökünden koşmalı")
    val (aslıGeçen, kırık) = kırıkları(türkçeÖrnekler, Çevirmen.TürkçedenİngilizceyeYön)
    info(s"Türkçe örnek: ${türkçeÖrnekler.size}, aslı derlenen: $aslıGeçen, çevirisi de derlenen: ${aslıGeçen - kırık.size}")
    kırık.foreach { case (ad, hata) => info(s"  kırık: $ad -- $hata") }
    withClue("YENİ kırık (gerileme): ") { (kırık.keySet -- bilinenTrEn.keySet) shouldBe empty }
    withClue("DÜZELEN kırık (listeden çıkarın): ") { (bilinenTrEn.keySet -- kırık.keySet) shouldBe empty }
    aslıGeçen should be >= 85
  }

  test("İngilizce örnekler Türkçeye çevrilince yalnız bilinen betikler kırılıyor") {
    assume(ingilizceÖrnekler.nonEmpty, "depo kökünden koşmalı")
    val (aslıGeçen, kırık) = kırıkları(ingilizceÖrnekler, Çevirmen.İngilizcedenTürkçeyeYön)
    info(s"İngilizce örnek: ${ingilizceÖrnekler.size}, aslı derlenen: $aslıGeçen, çevirisi de derlenen: ${aslıGeçen - kırık.size}")
    kırık.foreach { case (ad, hata) => info(s"  kırık: $ad -- $hata") }
    withClue("YENİ kırık (gerileme): ") { (kırık.keySet -- bilinenEnTr.keySet) shouldBe empty }
    withClue("DÜZELEN kırık (listeden çıkarın): ") { (bilinenEnTr.keySet -- kırık.keySet) shouldBe empty }
    aslıGeçen should be >= 75
  }

  // NEDEN AYRI SINAMA: dönüştürücü adları (eksenler, yansıtX, yansıtY, döndür...) örnek
  // betiklerin hiçbirinde `->` ile YALIN kullanılmıyor, o yüzden yukarıdaki iki toplu sınama
  // onları hiç çevirmiyor. Ölçüldü: 226 sınama yeşilken `eksenler` derlenmeyen koda
  // (`not found: value AxesOn`), `yansıtX`/`yansıtY` Picture'da olmayan üyeye çevriliyordu.
  // Kök, birleştirilebilir sınıf tablosunun `case object` biçimini kaçırmasıydı; iniş
  // kopunca ham iç sınıf adına düşülüyor ve o ad KAYNAKTA VAR olduğu için hayalet sayacı
  // da susuyor. Bu sınama o sessiz yolu kapatıyor.
  //
  // Işıklar bilerek DIŞARIDA: `noktaIşık`/`sahneIşığı` doğru adı (picture.pointLight)
  // buluyor ama Builtins onu yalın açmıyor -- bilinen sarmalayıcı boşluğu, yukarıda
  // tr/eye-effects.kojo olarak listeli.
  test("dönüştürücü adları birleştirilebilir sarmalayıcıya çözülüyor mu (eksenler, yansıtX, yansıtY)") {
    val betik =
      """|dez r = Resim.daire(50)
         |dez a = eksenler -> r
         |dez b = yansıtX -> r
         |dez c = (yansıtX * yansıtY) -> r
         |r.yansıtX()
         |r.yansıtY()
         |""".stripMargin
    val (çıktı, _) = Çevirmen.türkçedenİngilizceye(betik)
    çıktı should include("axesOn -> r")
    çıktı should include("flipX -> r")
    çıktı should include("(flipX * flipY) -> r")
    çıktı should include("r.flipX()")
    // Asıl sav: yalnız ad değil, DERLENİYOR olması. Ham iç sınıf adı (AxesOn, FlipX)
    // kaynakta var ama birleştirilemez; ancak typer bunu söyleyebilir.
    ÇeviriDoğrulama.türDenetimi("dönüştürücüler.kojo", çıktı, türkçe = false) shouldBe empty
  }

  test("tek betik: angles.kojo'nun İngilizce çevirisi Kojo prelude'üyle derleniyor mu (sınır: karesi)") {
    val f = new File(örneklerKökü, "tr/angles.kojo")
    assume(f.exists())
    val (çıktı, rapor) = Çevirmen.türkçedenİngilizceye(SözlükÜreteci.oku(f))
    Çevirmen.kalanAnahtarSözcükler(çıktı, Çevirmen.TürkçedenİngilizceyeYön) shouldBe empty
    // karesi bilerek çevrilmiyor; rapor onu Türkçe kalan diye göstermeli, derleyici de yakalamalı
    rapor.türkçeKalanlar.keySet should contain("karesi")
    val hatalar = ÇeviriDoğrulama.türDenetimi("angles.kojo", çıktı, türkçe = false)
    hatalar.map(_.ileti).exists(_.contains("karesi")) shouldBe true
    hatalar.filterNot(_.ileti.contains("karesi")) shouldBe empty
  }
}
