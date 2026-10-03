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
 * Çevirmen bir KİTAPLIK: `cevirmen.scala` (Çevirmen) + `cevirisozlugu.scala`
 * (ÇeviriSözlüğü) + `dict.scala` (anahtar sözcük tabloları; düz veri) + iki
 * TSV, yalnız scala-library ve yamalı scalariform'a dayanıyor. `dict.scala`'yı
 * bu sınamanın ilk koşusu buldu: Çevirmen anahtar sözcük çiftlerini oradan
 * kuruyor (ölçüldü: onsuz 4 hata, "not found: value dict"). iKojo'nun "Çevir" komutu (kojojs-dev#183) bu iki dosyayı
 * sunucusuna kopyalayıp orada koşturacak; Kojo'nun geri kalanına (Utils,
 * derleyici sondası, masaüstü kaynakları) sızan bir bağımlılık orada derlemeyi
 * kırar. Bu sınama o sızıntıyı burada, kaynağında yakalar: iki dosyayı başka
 * HİÇBİR şey olmadan derler.
 *
 * Üreteç (sozlukureteci.scala), komut satırı (cevirmenmain.scala) ve
 * doğrulama (ceviridogrulama.scala) kitaplığın DIŞINDA: masaüstü kaynaklarını
 * okuyor ya da derleyiciyi kullanıyorlar.
 */
@RunWith(classOf[JUnitRunner])
class CevirmenKitaplikTest extends FunSuite with Matchers {
  val dizin = new File("src/main/scala/net/kogics/kojo/lite/i18n/tr")
  val kitaplık = Seq("cevirmen.scala", "cevirisozlugu.scala", "dict.scala")

  private def jar(c: Class[_]): String = new File(c.getProtectionDomain.getCodeSource.getLocation.toURI).getPath

  /** Verilen dosyaları yalnız verilen sınıf yoluyla tür denetiminden geçirir; hataları döndürür. */
  def derle(dosyalar: Seq[File], sınıfYolu: Seq[String]): Seq[String] = {
    val s = new Settings
    s.usejavacp.value = false
    s.classpath.value = sınıfYolu.mkString(File.pathSeparator)
    s.stopAfter.value = List("refchecks")
    s.nowarn.value = true
    s.encoding.value = "UTF-8"
    val r = new StoreReporter(s)
    val g = new Global(s, r)
    val kaynaklar = dosyalar.map { f =>
      new BatchSourceFile(f.getName, new String(Files.readAllBytes(f.toPath), StandardCharsets.UTF_8))
    }
    new g.Run().compileSources(kaynaklar.toList)
    r.infos.toSeq.filter(_.severity == r.ERROR).map(h => s"${h.pos.source.file.name}:${h.pos.line}: ${h.msg}")
  }

  val yalnızKitaplıkYolu = Seq(jar(classOf[scala.Option[_]]), jar(classOf[scalariform.lexer.Token]))

  test("çevirmen kitaplığı yalnız scala-library ve scalariform'la derleniyor (kojojs-dev#183)") {
    assume(dizin.isDirectory, "depo kökünden koşmalı")
    derle(kitaplık.map(new File(dizin, _)), yalnızKitaplıkYolu) shouldBe empty
  }

  test("sınama bağımlılık sızıntısını gerçekten yakalıyor") {
    // Savın kendisi de sınansın: aynı dosyaların Kojo'ya dokunan bir kopyası kırmızı olmalı,
    // yoksa yukarıdaki sav (ör. sınıf yolu yanlışlıkla tam Kojo'yu içeriyorsa) boşa yeşil yanar.
    assume(dizin.isDirectory, "depo kökünden koşmalı")
    val geçici = Files.createTempDirectory("cevirmen-kitaplik").toFile
    try {
      val sızdıran = new File(geçici, "sizdiran.scala")
      Files.write(sızdıran.toPath,
        "package net.kogics.kojo.lite.i18n.tr\nobject Sızıntı { val y = net.kogics.kojo.util.Utils.loadResource(\"/x\") }\n"
          .getBytes(StandardCharsets.UTF_8))
      val hatalar = derle(kitaplık.map(new File(dizin, _)) :+ sızdıran, yalnızKitaplıkYolu)
      hatalar should not be empty
      hatalar.mkString should include("sizdiran.scala")
    }
    finally {
      geçici.listFiles().foreach(_.delete()); geçici.delete()
    }
  }

  test("kitaplık sözlüğü kendi kaynaklarından yüklüyor") {
    val s = net.kogics.kojo.lite.i18n.tr.ÇeviriSözlüğü.yükle()
    s.satırlar.size should be > 1000
    s.kurallar.size should be > 50
  }
}
