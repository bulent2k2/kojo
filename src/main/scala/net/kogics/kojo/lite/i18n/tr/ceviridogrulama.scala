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
 * Çeviri çıktısının DERLENDİĞİNİ kanıtlar: yazılımcık Kojo'nun kendi sarma
 * biçimiyle sarılıp (CodeExecutionSupport.compilerPrefix + Türkçe için initk
 * yapıştırıcısı) derleyici `typer`da durdurularak tür denetiminden geçirilir.
 *
 * NEDEN: çevirmen ad-ada çalışır ve sözlükte olmayan, alıcının türüne bağlı ya
 * da argüman ekleyen sarmalayıcıları (karesi = pow(x, 2)) taşıyamaz. "Çevrildi"
 * ile "çalışır" arasındaki boşluğu ancak hedef dilin derleyicisi kapatır. Bu
 * nesne o adımı araca gömer; CevirmenMain --doğrula ve CevirmenDerlemeTest
 * bunu kullanır.
 *
 * SINIRLAR:
 *   - Kullanılan derleyici sınıf yolundaki derleyicidir: sbt altında yamalı
 *     (Türkçe anahtar sözcüklü) derleyici. İngilizce çıktıda Türkçe anahtar
 *     sözcük kalsa bu derleyici sesini çıkarmaz; o denetim ayrıca yapılır
 *     (Çevirmen.kalanAnahtarSözcükler). Türkçe AD kalırsa yakalanır: İngilizce
 *     prelude Türkçe adları bilmez.
 *   - `Builtins.instance` yerine `val builtins: Builtins = ???` -- typer için
 *     kararlı bir ad yeter, değer gerekmez. Yani bu bir TÜR denetimi, koşum değil.
 *   - Kojo'nun betik yürütme yolu ayrıca `import TSCanvas._; import Tw._` yapıyor
 *     (Builtins.scala'daki interpret çağrısı); onlar da eklendi, yoksa cleari gibi
 *     adlar yanlış yere "yok" çıkıyordu (ölçüldü).
 *   - Bir birimde AYRIŞTIRMA hatası varsa scalac hiçbir birimi typer'a sokmaz;
 *     bu yüzden önce parser geçişiyle bozuk birimler ayıklanır, typer kalanlara
 *     uygulanır. İki Global kurulur; ilki ucuz.
 */
object ÇeviriDoğrulama {
  final case class Hata(evre: String, satır: Int, ileti: String) {
    override def toString = s"$evre satır $satır: $ileti"
  }

  // CodeExecutionSupport.compilerPrefix ile aynı yapı; Builtins.instance yerine tür imzası.
  private val önek =
    """object %s {
      |  val builtins: net.kogics.kojo.lite.Builtins = ???
      |  import builtins._
      |  import builtins.TSCanvas._
      |  import builtins.Tw._
      |  import net.kogics.kojo.util.ScalatestHelper.{test, ignore}
      |  import org.scalatest.Matchers
      |  class UserCode {
      |""".stripMargin
  // src/main/resources/i18n/initk/tr.tw.kojo'nun kod satırları.
  private val türkçeYapıştırıcı =
    """    val TurkishAPI = net.kogics.kojo.lite.i18n.TurkishAPI
      |    import TurkishAPI.{bi => _, _ }
      |    import kaplumbağa.{sil => _, _}
      |""".stripMargin
  private val kapanış = "\n  }\n}\n"

  private def nesneAdı(ad: String) = "Çeviri_" + ad.map(c => if (c.isLetterOrDigit) c else '_')

  /** Yazılımcığı sarar; satır kayması (önek satır sayısı) ile birlikte döner. */
  def sar(ad: String, kod: String, türkçe: Boolean): (String, Int) = {
    val baş = önek.format(nesneAdı(ad)) + (if (türkçe) türkçeYapıştırıcı else "")
    (baş + kod + kapanış, baş.count(_ == '\n'))
  }

  private def ayarlar(evre: String): Settings = {
    val s = new Settings
    s.usejavacp.value = true          // sbt (fork) ve runMain altında Kojo'nun tam sınıf yolu
    s.stopAfter.value = List(evre)
    s.nowarn.value = true
    s.encoding.value = "UTF-8"
    // Sonda tek birimde yüzlerce "not found" üretir; varsayılan 100 hata tavanı sonrakileri
    // yutuyor ve adlar yanlışlıkla "çözüldü" sayılıyordu (ölçüldü: 16 işaret, beklenen yüzlerce).
    s.maxerrs.value = 1000000
    s
  }

  private def geçir(evre: String, birimler: Seq[(String, String, Int)]): Map[String, Seq[Hata]] = {
    val settings = ayarlar(evre)
    val reporter = new StoreReporter(settings)
    val global = new Global(settings, reporter)
    val kaynaklar = birimler.map { case (ad, metin, _) => new BatchSourceFile(ad, metin) }.toList
    new global.Run().compileSources(kaynaklar)
    val kayma = birimler.map { case (ad, _, k) => ad -> k }.toMap
    reporter.infos.toSeq.filter(_.severity == reporter.ERROR).groupBy(_.pos.source.file.name).map { case (ad, hs) =>
      ad -> hs.toSeq.sortBy(_.pos.line).map(h => Hata(evre, h.pos.line - kayma.getOrElse(ad, 0), h.msg.linesIterator.next()))
    }
  }

  /**
   * Betikleri (ad, kod) hedef dilin prelude'üyle tür denetiminden geçirir.
   * Sonuç: ad -> hatalar (boş liste = derlendi). `türkçe`: Türkçe prelude mü.
   */
  def türDenetimi(betikler: Seq[(String, String)], türkçe: Boolean): Map[String, Seq[Hata]] = {
    val birimler = betikler.map { case (ad, kod) => val (metin, kayma) = sar(ad, kod, türkçe); (ad, metin, kayma) }
    val ayrıştırma = geçir("parser", birimler)
    val kalan = birimler.filterNot { case (ad, _, _) => ayrıştırma.contains(ad) }
    val türHataları = if (kalan.isEmpty) Map.empty[String, Seq[Hata]] else geçir("typer", kalan)
    betikler.map { case (ad, _) => ad -> ayrıştırma.getOrElse(ad, türHataları.getOrElse(ad, Nil)) }.toMap
  }

  def türDenetimi(ad: String, kod: String, türkçe: Boolean): Seq[Hata] = türDenetimi(Seq(ad -> kod), türkçe)(ad)

  /**
   * Hangi adlar Kojo prelude'ünde YALIN adıyla çözülüyor? Değer/yöntem/nesne için
   * `locally(ad)`, tür için `type T = ad`; hepsi tek birimde, ad başına bir satır.
   * "not found" DIŞINDAKİ hatalar (eksik argüman listesi, aşırı yükleme belirsizliği)
   * "çözüldü" sayılır: ad var, yalnız burada çağrılmadı.
   *
   * NEDEN: sözlüğün hiçbir kaynağı tek başına güvenilir değil -- package.scala `çokHızlı =
   * SuperFast` (iç enum) derken data.scala `superFast` (betik düzeyi) diyor; sarmalayıcı
   * `kosinüs = math.cos` derken tablo `cos` diyor. Hakem derleyici: çözülmeyen hedef yalnız
   * üye bağlamında (`x.length`) kullanılabilir, yalın bağlamda sözlükten düşer.
   */
  def yalınÇözülenler(adlar: Seq[(String, Boolean)], türkçe: Boolean): Set[String] = {
    if (adlar.isEmpty) return Set.empty
    val satırlar = adlar.zipWithIndex.map { case ((ad, türMü), n) => if (türMü) s"    type Sonda_$n = $ad" else s"    def sonda_$n = locally($ad)" }
    val (metin, kayma) = sar("sonda", satırlar.mkString("\n"), türkçe)
    val hatalar = geçir("typer", Seq(("sonda", metin, kayma))).getOrElse("sonda", Nil)
    val bulunamayan = hatalar.filter(h => h.ileti.startsWith("not found") || h.ileti.contains("is not a member of")).map(_.satır).toSet
    adlar.zipWithIndex.collect { case ((ad, _), n) if !bulunamayan(n + 1) => ad }.toSet
  }
}
