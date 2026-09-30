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
 * Komut satırı:
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.CevirmenMain --tr2en betik.kojo [-o cikti.kojo] [--dogrula]"
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.CevirmenMain --en2tr script.kojo [-o cikti.kojo] [--dogrula]"
 *
 * Çıktı dosyaya (-o) ya da standart çıktıya; rapor her zaman standart hataya.
 * --dogrula: çıktıyı hedef dilin Kojo prelude'üyle sarıp tür denetiminden geçirir
 * (ÇeviriDoğrulama); hata varsa asıl betiğin satır numarasıyla basar, çıkış kodu 1.
 * Bu, "çevrildi" ile "çalışır" arasındaki farkı kapatan adımdır; sözlüğün
 * taşıyamadığı her şey (bkz. Çevirmen SINIRLAR) burada görünür.
 *
 * Sınıf adı ve bayraklar bilerek ASCII: sbt'nin runMain'ine kabuktan Türkçe
 * harf geçirmek platforma göre sorun çıkarıyor.
 */
object CevirmenMain {
  private val kullanım =
    """kullanım: CevirmenMain (--tr2en | --en2tr) <girdi.kojo> [-o <çıktı.kojo>] [--dogrula]
      |  --tr2en    Türkçe (Koco) yazılımcığı İngilizce Kojo'ya çevir
      |  --en2tr    İngilizce Kojo yazılımcığını Türkçe Koco'ya çevir
      |  --dogrula  çıktıyı hedef dilin derleyicisiyle tür denetiminden geçir
      |""".stripMargin

  def main(args: Array[String]): Unit = {
    val doğrula = args.contains("--dogrula")
    val (yön, kalan) = args.toList.filterNot(_ == "--dogrula") match {
      case "--tr2en" :: k => (Çevirmen.TürkçedenİngilizceyeYön, k)
      case "--en2tr" :: k => (Çevirmen.İngilizcedenTürkçeyeYön, k)
      case _              => System.err.println(kullanım); sys.exit(2)
    }
    val (girdi, çıktıDosyası) = kalan match {
      case g :: "-o" :: ç :: Nil => (g, Some(ç))
      case g :: Nil              => (g, None)
      case _                     => System.err.println(kullanım); sys.exit(2)
    }
    val kod = SözlükÜreteci.oku(new File(girdi))
    val (çıktı, rapor) = Çevirmen.çevir(kod, yön)
    // Rapor: çeviri stdout'a gidiyorsa stderr'e (boru hattı temiz kalsın), dosyaya gidiyorsa
    // stdout'a -- sbt altında stderr her satırı `[error]` diye gösteriyor, başarı iletisi
    // hata gibi okunuyordu (inceleme #62).
    val raporla: String => Unit = if (çıktıDosyası.isDefined) println(_) else System.err.println(_)
    çıktıDosyası match {
      case Some(ç) => java.nio.file.Files.write(new File(ç).toPath, çıktı.getBytes("UTF-8")); raporla(s"yazıldı: $ç")
      case None    => print(çıktı)
    }
    rapor.özet(yön).linesIterator.foreach(raporla)
    var kusurlu = false
    val kalanlar = Çevirmen.kalanAnahtarSözcükler(çıktı, yön)
    if (kalanlar.nonEmpty) { raporla(s"UYARI: kaynak dilin anahtar sözcüğü kaldı: ${kalanlar.mkString(", ")}"); kusurlu = true }
    if (doğrula) {
      val türkçeHedef = yön == Çevirmen.İngilizcedenTürkçeyeYön
      val hatalar = ÇeviriDoğrulama.türDenetimi(new File(girdi).getName, çıktı, türkçeHedef)
      if (hatalar.isEmpty) raporla("doğrulama: çeviri " + (if (türkçeHedef) "Türkçe" else "İngilizce") + " Kojo prelude'üyle tür denetiminden geçti")
      else { raporla(s"doğrulama: ${hatalar.size} hata"); hatalar.foreach(h => raporla("  " + h)); kusurlu = true }
    }
    if (kusurlu) sys.exit(1)
  }
}
