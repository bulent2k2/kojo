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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.Matchers

import net.kogics.kojo.lite.{Builtins, DrawingCanvasAPI, NoOpKojoCtx}
import net.kogics.kojo.lite.canvas.SpriteCanvas
import net.kogics.kojo.music.{FuguePlayer, KMp3}
import net.kogics.kojo.staging
import net.kogics.kojo.story.StoryTeller
import net.kogics.kojo.turtle.TurtleWorldAPI
import net.kogics.kojo.picture.PicCache

/**
 * Türkçe eskiz katmanı (tr/eskiz.scala, Resim.eskizden, tuval yöntemleri) ve #87'nin nedeni:
 * Resim nesneleriyle çizilen her iz noktası PicCache'e kayıt ekler, eskiz ise tek bir resimdir.
 */
@RunWith(classOf[JUnitRunner])
class TurkishEskizTest extends FunSuite with Matchers {
  import scala.language.reflectiveCalls
  // TestEnv KULLANMIYORUZ: o bir CodeExecutionSupport kuruyor, o da System.out/err'i çıktı penceresine
  // yönlendiriyor ve aynı JVM'deki InterpOutputHandlerTest'i StackOverflowError ile düşürüyor (ölçüldü).
  val kojoCtx = new NoOpKojoCtx
  val spriteCanvas = new SpriteCanvas(kojoCtx)
  val Tw = new TurtleWorldAPI(spriteCanvas.turtle0)
  val TSCanvas = new DrawingCanvasAPI(spriteCanvas)
  val Staging = new staging.API(spriteCanvas)
  val codeRunner: net.kogics.kojo.core.CodeRunner = null // Builtins kurulurken kullanılmıyor
  val builtins = new Builtins(TSCanvas, Tw, Staging, new StoryTeller(kojoCtx), new KMp3(kojoCtx), new FuguePlayer(kojoCtx), kojoCtx, codeRunner)
  TurkishInit.init(builtins)
  import TurkishAPI._

  /** Çağrıları sayan eskiz: ne zaman ve kaç kez çağrıldığını gösterir. */
  class Sayaç extends Eskiz {
    var kurulumSayısı = 0
    var döngüSayısı = 0
    def kurulum(tuval: TuvalÇizim): Birim = { kurulumSayısı += 1; tuval.artalan(siyah) }
    def çizimDöngüsü(tuval: TuvalÇizim): Birim = {
      döngüSayısı += 1
      tuval.boya(kırmızı); tuval.elips(10, 10, 5, 5)
    }
  }

  test("Resim.eskizden bir Resim kurar; kurulmak çizmek değil") {
    PicCache.clear()
    val e = new Sayaç
    val r = Resim.eskizden(e)
    r shouldBe a[Resim]
    e.kurulumSayısı should be(0)
  }

  test("Eskiz, İngilizce yapısal türe uyarlanıyor: setup kurulum'u, drawLoop çizimDöngüsü'nü çağırıyor") {
    val e = new Sayaç
    val u = new tr.EskizUyarlayıcı(e)
    // TuvalÇizim'i gerçek bir Java2D grafiğiyle kuruyoruz
    val img = new java.awt.image.BufferedImage(50, 50, java.awt.image.BufferedImage.TYPE_INT_ARGB)
    val cd = new net.kogics.kojo.lite.CanvasDraw(img.createGraphics, 50, 50, builtins)
    u.setup(cd); u.drawLoop(cd); u.drawLoop(cd)
    e.kurulumSayısı should be(1)
    e.döngüSayısı should be(2)
  }

  test("tuval yöntemleri İngilizce karşılıklarını çağırıyor") {
    val img = new java.awt.image.BufferedImage(50, 50, java.awt.image.BufferedImage.TYPE_INT_ARGB)
    val cd = new net.kogics.kojo.lite.CanvasDraw(img.createGraphics, 50, 50, builtins)
    cd.döngüdeMi should be(true)
    cd.döngüyüDurdur()
    cd.döngüdeMi should be(false)
    cd.fırçaUcu(cd.yuvarlakUç); cd.fırçaBirleşimi(cd.sivriBirleşim)
    // dönüşümü kaydet/geri yükle: arada yapılan götür/büyüt/döndür dışarı sızmamalı
    cd.dönüşümüKaydet(); cd.götür(10, 20); cd.büyüt(2); cd.büyüt(2, 3); cd.döndürRadyan(0.5); cd.döndürDerece(30)
    cd.dönüşümüGeriYükle()
    cd.şekilBaşla(); cd.köşe(0, 0); cd.köşe(10, 0); cd.köşe(10, 10); cd.şekilBitir()
    cd.rastgeleTohum(42L)
  }

  test("#87: 'Too many pics' iletisi bütün cümle olarak Türkçe; 'this' -> 'bu' bozulması yok") {
    val ileti =
      """There are too many pics in your drawing, and trying to draw them might freeze Kojo.
        |If you still want to go ahead with this, use the pic.draw() method.
        |Or use Picture.fromSketch(...).
        |Problem: BelirtimHatası: belirtilen koşul sağlanmadı: Too many pics to draw - Kojo might freeze.""".stripMargin
    // Kojo her satırı ayrı bir çıktı olarak yazıyor (println ve hata iletisi); burada da satır satır
    val çıktı = ileti.linesIterator.map(tr.translate.result).mkString("\n")
    çıktı should include("Çiziminizde çok fazla resim var")
    çıktı should include("r.çiz() yöntemini kullanın")
    çıktı should include("Resim.eskizden(...)")
    çıktı should include("Çizilecek resim sayısı çok fazla - Kojo donabilir.")
    çıktı should not include "go ahead"
    çıktı should not include "with bu"
    çıktı should not include "Too many pics"
  }

  test("#87: iz noktası Resim olarak kurulunca PicCache büyür, eskizde büyümez") {
    PicCache.clear()
    val n0 = PicCache.size
    (1 to 100).foreach { _ =>
      Resim.daire(3).kalemRenkli(kırmızı).boyalı(kırmızı).taşınmış(1, 2)
    }
    val resimKayıtları = PicCache.size - n0
    resimKayıtları should be(300) // nokta başına 3 (daire sarmalayıcıları); çiz(r) bir tane daha ekler

    PicCache.clear()
    val n1 = PicCache.size
    val e = new Sayaç
    (1 to 100).foreach { _ => Resim.eskizden(e) } // kurmak bile tek resim; çizilen noktalar piksel
    (PicCache.size - n1) should be < resimKayıtları
  }
}
