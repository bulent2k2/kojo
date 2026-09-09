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

import scala.tools.nsc.Settings

import org.junit.Test
import org.junit.Assert._

import net.kogics.kojo.xscala.CompilerListener
import net.kogics.kojo.lite.NoOpRunContext
import net.kogics.kojo.xscala.CompilerAndRunner

/**
 * tr/help.scala'daki koleksiyon örneklerinin GERÇEKTEN yazdığı şeyi yazdığını
 * doğrular. Belgedeki her örnek burada çalıştırılıp yanındaki sonuçla
 * karşılaştırılıyor; bir yöntemin davranışı değişirse yardım metni sessizce
 * yanlış kalmıyor, test kırılıyor.
 *
 * Hepsi TEK bir programda derlenip koşuluyor: 78 örnek için 78 derleme değil,
 * bir derleme.
 */
/**
 * Örneklerin sonuçlarını toplar. Neden println değil: derlenen kod başka bir
 * iş parçacığında koşuyor ve Predef.println Console.out'a yazıyor -- Console.out
 * ilklendiğinde yakalanan System.out'u tutuyor, sonradan System.setOut yapmak
 * ona işlemiyor. Statik bir toplayıcı bu sorunu tümden atlıyor.
 */
object KoleksiyonYardımıToplayıcı {
  val sonuçlar = new java.util.concurrent.ConcurrentHashMap[String, String]
  // Çıktı penceresinde görünen biçimi saklıyoruz: Kojo REPL çıktısını
  // translate.result'tan geçiriyor (List -> Dizin, Set -> Küme, ...), belgedeki
  // sonuç da öğrencinin GÖRDÜĞÜ metin olmalı.
  def koy(ad: String, değer: Any): Unit =
    sonuçlar.put(ad, tr.translate.result(String.valueOf(değer)))
}

class KoleksiyonYardımıTest {
  private val toplayıcı = "net.kogics.kojo.lite.i18n.KoleksiyonYardımıToplayıcı"

  private def örnekleriKoştur(): Map[String, String] = {
    val kod = tr.help.koleksiyonYöntemleri
      .map {
        case (ad, _, _, örnek, _) =>
          s"""try { $toplayıcı.koy("$ad", $örnek) }
             |catch { case t: Throwable => $toplayıcı.koy("$ad", "KURALDIŞI: " + t) }""".stripMargin
      }
      .mkString("\n")

    val derlemeHataları = scala.collection.mutable.ListBuffer.empty[String]
    val listener = new CompilerListener {
      // ilk hatada durmuyoruz: bir koşuda hepsini görelim
      def error(msg: String, line: Int, column: Int, offset: Int, lineContent: String): Unit =
        derlemeHataları += s"satır $line: $msg -- ${lineContent.trim}"
      def warning(msg: String, line: Int, column: Int): Unit = {}
      def info(msg: String, line: Int, column: Int): Unit = {}
      def message(msg: String): Unit = {}
    }
    val runner = new CompilerAndRunner(
      { () =>
        val s = new Settings(); s.usejavacp.value = true; s
      },
      Some("import net.kogics.kojo.lite.i18n.TurkishAPI._"),
      listener,
      new NoOpRunContext
    )

    KoleksiyonYardımıToplayıcı.sonuçlar.clear()
    runner.compileAndRun(kod)

    // Bu kontrol olmazsa tek bir derleme hatası bütün örnekleri düşürür ve
    // test "78 örnek hiç çalışmadı" diye yanıltıcı biçimde patlar.
    if (derlemeHataları.nonEmpty)
      fail(s"${derlemeHataları.size} örnek derlenmedi:\n" + derlemeHataları.mkString("\n"))

    import scala.jdk.CollectionConverters._
    KoleksiyonYardımıToplayıcı.sonuçlar.asScala.toMap
  }

  @Test
  def örneklerYazdıklarınıYazıyor(): Unit = {
    val gerçek = örnekleriKoştur()
    val yanlışlar = tr.help.koleksiyonYöntemleri.flatMap {
      case (ad, _, _, örnek, beklenen) =>
        gerçek.get(ad) match {
          case None       => Some(s"$ad: örnek hiç çalışmadı -- $örnek")
          case Some(`beklenen`) => None
          case Some(v)    => Some(s"$ad: `$örnek`\n     belgede: $beklenen\n     gerçek : $v")
        }
    }
    assertTrue(
      s"${yanlışlar.size} örnek belgedeki sonucu vermiyor:\n" + yanlışlar.mkString("\n"),
      yanlışlar.isEmpty
    )
  }

  @Test
  def türkçeSarmalayıcılarBilinenSahipSayılıyor(): Unit = {
    // Bu adlar uydurma değil: tamamlama koşturulup ölçüldü (bkz. commit iletisi).
    // Biri değişirse yardım metinleri sessizce görünmez olur, o yüzden çivili.
    val ölçülenSahipler = List(
      "net.kogics.kojo.lite.i18n.tr.SeqMethodsInTurkish.SeqYöntemleri",
      "net.kogics.kojo.lite.i18n.tr.SeqMethodsInTurkish.ImmutableIterableMethods",
      "net.kogics.kojo.lite.i18n.tr.StackMethodsInTurkish.YığınYöntemleri",
      "net.kogics.kojo.lite.i18n.tr.StringMethodsInTurkish.YazıYöntemleri",
      "net.kogics.kojo.lite.i18n.tr.Eşlem"
    )
    ölçülenSahipler.foreach { sahip =>
      assertTrue(s"$sahip tanınmıyor", tr.trDestesiMi(sahip))
    }
    assertFalse(tr.trDestesiMi("scala.collection.immutable.List"))
    assertFalse(tr.trDestesiMi(null))
  }

  @Test
  def elleYazılanYardımlarKaybolmadı(): Unit = {
    // `elleYazılan ++ koleksiyonYardımı` deseydik aynı adlı girdiler sessizce
    // ezilirdi. Nitekim bir süre ezildi: sil (tuvali sil) ve yazı (tuvale yaz)
    // -- ikisi de öğrencinin ilk öğrendiği komutlardan. Artık birleşiyorlar.
    val çakışanlar = List("sil", "yazı")
    çakışanlar.foreach { ad =>
      val metin = tr.help.content.getOrElse(ad, fail(s"$ad için yardım yok").asInstanceOf[String])
      assertTrue(
        s"$ad girdisinde iki anlam da yok -- koleksiyon girdisi elle yazılanı ezmiş olabilir",
        metin.contains("Bu ad başka bir türde de kullanılıyor")
      )
    }
    // sil girdisi hâlâ tuvalden söz ediyor mu (elle yazılan kısım duruyor mu)
    assertTrue("sil girdisinde tuval/çizim anlatımı kaybolmuş", tr.help.content("sil").contains("tuval"))
  }

  @Test
  def herYöntemİçinYardımVar(): Unit = {
    val içerik = tr.help.content
    tr.help.koleksiyonYöntemleri.foreach {
      case (ad, imza, _, _, _) =>
        val metin = içerik.getOrElse(ad, fail(s"$ad için yardım metni yok").asInstanceOf[String])
        assertTrue(s"$ad metninde imza geçmiyor", metin.contains(imza))
    }
  }
}
