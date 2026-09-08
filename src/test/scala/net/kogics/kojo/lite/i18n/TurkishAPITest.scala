/*
 * Copyright (C) 2020-24
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *   Lalit Pant <pant.lalit@gmail.com>
 *   Christoph Knabe  http://public.beuth-hochschule.de/~knabe/
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

import org.scalatest.{Matchers, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// ../../picture/PictureTest.scala
import net.kogics.kojo.lite.NoOpKojoCtx
import net.kogics.kojo.lite.canvas.SpriteCanvas
import net.kogics.kojo.util.Utils.doublesEqual
import net.kogics.kojo.staging

/**
  * Tests for TurkishAPI.
  *
  * @author Bulent Basaran   https://github.com/bulent2k2

  * Thanks to the original author (for GermanAPI):
  * Christoph Knabe  http://public.beuth-hochschule.de/~knabe/
  */
@RunWith(classOf[JUnitRunner])
@annotation.nowarn class TurkishAPITest extends FunSuite with Matchers {
  
  import TurkishAPI._

  test("yinele(n){block} should repeat the block n times") {
    val sb = new StringBuilder(10)
    yinele(5){
      sb append "+-"
    }
    sb.toString should be("+-+-+-+-+-")
  }
  
  test("yineleDoğruysa(condition){block} should execute block while condition holds"){
    val sb = new StringBuilder(10)
    var i = 10
    yineleDoğruysa(i > 5){
      sb.append(i).append(' ')
      i -= 1
    }
    sb.toString should be("10 9 8 7 6 ")
  }
  
  test("yineleOlanaKadar(condition){block} should execute block while condition does not hold"){
    val sb = new StringBuilder(10)
    var i = 1
    yineleOlanaKadar(i > 5){
      sb.append(i).append(' ')
      i += 1
    }
    sb.toString should be("1 2 3 4 5 ")
  }
  
  test("yineleDizinli(n){i => fn(i)} should call fn n times with indices 1 to n"){
    val sb = new StringBuilder(10)
    yineleDizinli(5){i =>
      sb.append(i).append(' ')
    }
    sb.toString should be("1 2 3 4 5 ")
  }

  test("yineleİlktenSona(start, end){i => fn(i)} should call fn with the Int values from start to end"){
    val sb = new StringBuilder(10)
    yineleİlktenSona(1, 5){i =>
      sb.append(i).append(' ')
    }
    sb.toString should be("1 2 3 4 5 ")
  }
  
  test("yineleKere[T](iterable){e => fn(e)} should process all elements of iterable"){
    val sb = new StringBuilder(10)
    yineleKere(1 to 10 by 2){i =>
      sb.append(i).append(' ')
    }
    sb.toString should be("1 3 5 7 9 ")
  }

  test("yineleİçin[T](iterable){e => fn(e)} should process all elements of iterable"){
    val sb = new StringBuilder(10)
    yineleİçin(1 to 10 by 3){i =>
      sb.append(i).append(' ')
    }
    sb.toString should be("1 4 7 10 ")
  }

  test("İkil (Boolean in Turkish) should work") {
    val test0: İkil = yanlış
    val test1: İkil = doğru

    test0 || false should be(yanlış)
    test1 && true  should be(doğru)
  }

  test("Translation of Option should work") {
    val o1: Belki[Sayı] = Biri(3)
    varMı(o1) should be(true)
    yokMu(o1) should be(false)
    varMı(Hiçbiri) should be(false)
    yokMu(Hiçbiri) should be(true)
    val o2 = Hiçbiri
    val test = for (o <- List(o1, o2)) yield(o match {
      case Biri(n) => n
      case Hiçbiri => -1
    })
    test should be(List(3, -1))
    o1.al should be(3)
    o2.alYoksa(1) should be(1)
    o1.işle(_.yazıya) should be(Biri("3"))
    o2.işle((x: Int) => x * 3) should be(Hiçbiri)
    o1.düzİşle{ x => Option(s"x=${x}") } should be(Biri("x=3"))
  }
  
  test("Translations of math API should work -- abs == mutlakDeğer") {
    val abs0: Kesir = mutlakDeğer(0)
    val abs1: Kesir = mutlakDeğer(-72.001)
    val abs2: Kesir = mutlakDeğer(42)
    abs0 should be(0)
    abs1 should be(72.001)
    abs2 should be(42)
  }

  test("Translations of math API should work -- max == enİrisi") {
    val m1: Sayı = enİrisi(-3, 5)
    val m2: Kesir = enİrisi(-30.05, 50.03)
    m1 should be(5)
    m2 should be(50.03)
  }

  test("Translations of math API should work -- pi, e and sqrt2") {
    val pi: Kesir = piSayısı
    val e: Kesir = eSayısı
    val s2: Kesir = gücü(2, 0.5)
    pi shouldBe 3.1415 +- 0.0001
    e shouldBe 2.7182 +- 0.0001
    s2 shouldBe 1.4142 +- 0.0001
  }

  test("Translations of math API should work -- misc") {
    val x = 1.234
    yuvarla(x) should be(1)
    yuvarla(x, 1) should be(1.2)
    val y = 1.5005
    yuvarla(y) should be(2)
    yuvarla(y,3) should be(1.501)
  }

  test("Translation of require should work") {
    val pass = try {
      gerekli(true && doğru, "Bu doğru")
      true
    } catch {
      case _: Throwable => false
    }
    pass should be(true)
    val pass2 = try {
      gerekli(false || yanlış, "Bu da yanlış")
      false
    } catch {
      case _: Throwable => true
    }
    pass2 should be(true)
  }

  test("Translation of util.Random.shuffle should work") {
    val beşKartlıEl = Dizi(1, 2, 3, 4, 5)
    var count = 0
    while (rastgeleKarıştır(beşKartlıEl) == Dizi(1, 2, 3, 4, 5)) count += 1
    count < 10 should be(true)
    val d2 = Dizin(1, 2, 3, 4, 5)
    while (rastgeleKarıştır(beşKartlıEl) == Dizi(1, 2, 3, 4, 5)) count += 1
    count < 20 should be(true)
  }

  test("Translation of Range should work") {
    val a = new Aralık(1, 10, 3)
    a.ilki shouldBe 1
    a.sonuncu shouldBe 10
    a.adım shouldBe 3
    a.dizine shouldBe List(1, 4, 7)
    a.yazı() shouldBe "Aralık(1, 4, 7)"
    a.map(_ * 2) shouldBe Vector(2, 8, 14)
    a.flatMap(s => List(s, s*s)) shouldBe Vector(1, 1, 4, 16, 7, 49)

    val a2 = new Aralık(1, 200, 7)
    a2.dizine.size shouldBe 29
    a2.başı shouldBe 1
    a2.sonu shouldBe 197
    a2.uzunluğu shouldBe 29

    val a3 = Aralık.kapalı(1, 10, 3)
    a3.dizine shouldBe List(1, 4, 7, 10)
    (for (i <- a3 if i % 2 != 0) yield i) shouldBe Vector(1, 7)

    val a4 = Aralık.kapalı(5, 1, -1)
    a4.dizine shouldBe List(5, 4, 3, 2, 1)
    var tane = 0
    for(i <- a4; j <- a4) { tane += 1 }
    tane shouldBe (25)
    val a5 = Aralık(5, 1, -1)
    a5.dizine shouldBe List(5, 4, 3, 2)
    var toplam = 0
    a5.foreach( x => toplam += x )
    toplam shouldBe (2 + 3 + 4 + 5)
    a5.herÖgeİçin( x => toplam -= x )
    toplam shouldBe (0)
    val a6 = Aralık.kapalı(1, 10, 3)
    val op = (x: Int, y: Int) => x - y
    a6.indirge(op) should be(-20)
    a6.soldanKatla(100)(op) should be(78)
    a6.sağdanKatla(100)(op) should be(94)
  }

  test("Translations of mutable.Stack should work") {
    val y1 = Yığın.boş[Sayı]
    y1.tane should be(0)
    y1.koy(1)
    y1.tane should be(1)
    val y2 = Yığın(1,2,3)
    y2.tane should be(3)
    val y3 = Yığın.doldur(y2)
    y3.tane should be(3)
    // todo more!
  }

  test("Translations of mutable.Map should work") {
    val e1 = Eşlem.boş[Yazı, Sayı]
    e1 eşli ("anahtar") should be(yanlış)
    e1 eşEkle ("anahtar" -> 99)
    e1 eşli ("anahtar") should be(doğru)
    e1("anahtar") should be(99)
    e1 eşEkle ("b" -> 88)
    e1 eşli ("b") should be(doğru)
    e1("b") should be(88)
    val l = e1.m.toSeq
    l.size should be(2)
    l.head match {
      case ("b", 88) => l.tail.head should be("anahtar", 99)
      case _ => l.tail.head should be("b", 88)
    }
    val e2 = Eşlem(
      "mavi" -> 1,
      "yeşil" -> 2,
      "sarı" -> 3
    )
    e2.sayı should be(3)
    e2("mavi") should be(1)
    var e3 = Eşlem( 1 -> 1, 2 -> 4, 4 -> 16, 16 -> 256 )
    e3 += (10 -> 100)
    e3(10) should be(100)
    e3.sayı should be(5)
    e3.eşli(2) should be(doğru)
    e3 -= 2
    e3.eşli(2) should be(yanlış)
    e3.sayı should be(4)

    var toplam1 = 0
    var toplam2 = 0
    e3.herÖgeİçin { ikili => val (a, b) = ikili; toplam1 += a; toplam2 += b }
    toplam1 should be(1 + 4 + 16 + 10)
    toplam2 should be(1 + 16 + 256 + 100)

    e3.al(10) shouldBe(Biri(100))
    e3.al(3) shouldBe(Hiçbiri)
    e3.alYoksa(3, -1) shouldBe(-1)
  }

  test("Translation of Array should work") {
    val s0 = Dizim.boş[Harf](10)
    s0.boyut should be(1)
    val s1 = Dizim.boş[Sayı](3, 3)
    s1.boyut should be(2)
    s1(0) should be(Array(0, 0, 0))
    s1(0)(0) should be(0)
    s1(0)(1) = 1
    s1(0) should be(Array(0, 1, 0))
    val s2 = Dizim.doldur[Sayı](2, 2)(5)
    s2.boyut should be(2)
    s2(0) should be(Array(5, 5))
    s2(0)(0) should be(5)
  }

  test("Translation of mutable.ArrayBuffer should work") {
    val ed1 = EsnekDizim.boş[Sayı]
    ed1.sayı should be(0)
    ed1 += 42
    ed1.sayı should be(1)
    ed1 += 2002
    ed1 += 2006
    ed1 += 2011
    ed1(0) should be(42)
    ed1.çıkar(0)
    ed1(0) should be(2002)
    ed1(1) should be(2006)
    ed1.sayı should be(3)
    ed1.sil()
    ed1.sayı should be(0)
    import net.kogics.kojo.core.Point
    val noktalar = EsnekDizim(Point(-100, -50), Point(100, -50), Point(-100, 50))
    noktalar.sayı should be(3)
    noktalar.ekle(Point(100, 100))
    noktalar.sayı should be(4)
    def deneme(nler: Seq[Point]) = nler.toList.tail
    deneme(noktalar.dizi).size should be(3)
  }

  test("Translations of Vector and its methods should work") {
    val y1 = Yöney(3, 4)
    y1(0) should be(3)
    y1.boyu should be(2)
    val y1b = y1 :+ 5
    y1b(2) should be(5)
    val y2 = Yöney.boş[Yazı]
    y2.boyu should be(0)
    val y2b = y2 :+ "Merhaba"
    (y2b :+ "Dünya!").size should be(2)
    y2b(0)(2) should be('r')
    y1.değiştir(0, 5) should be(Dizi(5, 4))
    val y3 = Yöney.doldur(10)(3)
    y3.boyu should be(10)
    for (i <- 0 |- 10) { y3(i) should be(3) }
  }

  test("Translation for Set should work") {
    var k1 = Küme.boş[Sayı]
    k1.size should be(0)
    k1 += 3
    k1.size should be(1)
    k1(3) should be(true)
    k1(5) should be(false)
    k1 += 5
    k1(5) should be(true)
    k1.foreach { e =>
      (e<=5) should be(true)
      (e>=3) should be(true)
    }
    k1 -= 3
    k1(3) should be(false)
    k1(5) should be(true)
    var k2 = Küme(51, 18, 14, 10, 6)
    k2.size should be(5)
    for (s <- List(6, 10, 14, 18, 51)) { k2(s) should be(true) }
    k2(2) should be(false)
    k2 += 2
    k2(2) should be(true)
  }

  test("Translations of Character should work") {
    for (c <- '0' to '9') Harf.sayıMı(c) should be(true)
    Harf.sayıMı(' ') should be(false)
    Harf.sayıMı('a') should be(false)
    Harf.harfMi('a') should be(true)

    /*
     Harf.enUfağı should be('\u0000')
     Harf.enİrisi should be('\uffff')
     */
  }
  test("Translations needed for mandelbrot sample should work") {
    case class Dörtgen(x1: Kesir, x2: Kesir, y1: Kesir, y2: Kesir) {
      def alanı() = (x2 - x1) * (y2 - y1)
      def ortaNoktası = (x, y)
      val (x, y) = ((x2 + x1) / 2, (y2 + y1) / 2)
      def yazı = {
        val a = alanı()
        if (a > 0.0001) s"${yuvarla(a, 5)}" else f"${a}%2.3e"
      }
      def dörtlü = (x1, x2, y1, y2)
      def büyüt(oran: Kesir): Dörtgen = {
        if (oran <= 0 || oran >= 10.0) this else {
          val o2 = 0.5 * oran
          val en2 = o2 * (x2 - x1)
          val boy2 = o2 * (y2 - y1)
          Dörtgen(x - en2, x + en2, y - boy2, y + boy2)
        }
      }
    }
    class Pencere {
      def koy(d: Dörtgen) = bakışlar.koy(d)
      def al(): Dörtgen = bakışlar.al()
      def boşMu() = bakışlar.tane == 0
      def boşalt() = while (!boşMu()) al()
      private val bakışlar = Yığın.boş[Dörtgen]
    }
    val p1 = new Pencere
    p1.boşMu() should be(doğru)
    val d1 = Dörtgen(1, 2, 3, 4)
    val d2 = Dörtgen(0, 1, 2, 3)
    p1.koy(d1)
    p1.boşMu() should be(yanlış)
    p1.koy(d2)
    p1.al() should be (d2)
    p1.al() should be (d1)
    p1.boşMu() should be(doğru)
  }

  test("Translations of String methods to work") {
    val y1 = "Hadi canım sen de"
    val a1 = y1.böl(" ")
    a1 should be(Dizin("Hadi", "canım", "sen", "de"))
    val a2 = y1.böl(" ", 2)
    a2 should be(Dizin("Hadi", "canım sen de"))
    val a3 = y1.böl("a")
    a3 should be(Dizin("H", "di c", "nım sen de"))
    val a4 = y1.böl(new Dizim(Array('a',' ')))
    a4 should be(Dizin("H", "di", "c", "nım", "sen", "de"))
    val a5 = y1.böl(' ')
    a5 should be(Dizin("Hadi", "canım", "sen", "de"))
    // false should be(true)
    val y2 = "Merhaba Dünya!"
    y2.boyu should be(14)
    y2.başı should be('M')
    y2.kuyruğu should be("erhaba Dünya!")
    y2.doluMu should be(doğru)
    y2.boşMu should be(yanlış)
    y2.ele(_ == 'a') should be("aaa")
    y2.eleDeğilse(_ == 'a') should be("Merhb Düny!")
    y2.işle(x => x.büyükHarfe) should be("MERHABA DÜNYA!")
    y2.düzİşle(x => x.yazıya * 3) should be("MMMeeerrrhhhaaabbbaaa   DDDüüünnnyyyaaa!!!")
    val y3 = y2.büyükHarfe
    y3 should be("MERHABA DÜNYA!")
    y3.küçükHarfe should be("merhaba dünya!")
    val y0 = "abc"
    y0.dizine should be(List('a', 'b', 'c'))
    val ys = Dizin("aa", "bb", "cc")
    ys.işle(_.dizine) should be(List(List('a', 'a'), List('b', 'b'), List('c', 'c')))
    ys.düzİşle(_.dizine) should be(List('a', 'a', 'b', 'b', 'c', 'c'))

    Yazı.olarak(3) should be("3")
    Yazı.olarak(3.14) should be("3.14")
    Yazı.olarak(yanlış) should be("yanlış")
    val a = Array('a', 'b', 'c', 'd', 'e')
    Yazı.olarak(a) should be("abcde")
    Yazı.olarak(a, 1, 3) should be("bcd")
  }

  test("Translations of List[T] methods to work") {
    val d0 = 0 :: Boş; d0.başı should be(0); d0.kuyruğu should be(Boş)
    // todo: duplicates below
    val d1 = Dizin(1, 3, 2)
    d1.başı should be(1)
    d1.kuyruğu should be(Dizin(3, 2))
    d1.boyu should be(3)
    d1.boşMu should be(yanlış)
    d1.doluMu should be(doğru)
    d1.ele(_ % 2 == 0) should be(Dizin(2))
    d1.eleDeğilse(_ % 2 == 0) should be(Dizin(1, 3))
    d1.işle(_ * 10) should be(Dizin(10, 30, 20))
    d1.düzİşle(x => Dizin(x, x*x)) should be(Dizin(1, 1, 3, 9, 2, 4))
    d1.sıralı should be(Dizin(1, 2, 3))
    d1.sırala(1.0 / _) should be(Dizin(3, 2, 1))
    d1.sırayaSok((x, y) => -x < -y) should be(Dizin(3, 2, 1))
    d1.indirge((x, y) => x * 10 + y) should be(132)
    d1.soldanKatla(10)(_ + _) should be(16)
    d1.sağdanKatla(2)(_ * _) should be(12)
    val d2 = Dizin(2, 3, 4)
    d2.topla should be(9)
    d2.çarp should be(24)
    Dizin(2, 2, 1, 1).yinelemesiz should be(Dizin(2, 1))
    Dizin(2, 4, 6, 1, 3, 5).yinelemesizİşlevle(_ % 2 == 0) should be(Dizin(2, 1))
    Dizin(1, 2, 3).yazıYap should be("123")
    Dizin(1, 2, 3).yazıYap(" ") should be("1 2 3")
    Dizin(1, 2, 3).yazıYap("{", " ", "}") should be("{1 2 3}")
  }

  test("Translations of collection.Seq[T] methods to work") {
    // todo: duplicates above
    val d1 = Diz(1, 3, 2)
    d1.başı should be(1)
    d1.kuyruğu should be(Diz(3, 2))
    d1.boyu should be(3)
    d1.boşMu should be(yanlış)
    d1.doluMu should be(doğru)
    d1.ele(_ % 2 == 0) should be(Diz(2))
    d1.eleDeğilse(_ % 2 == 0) should be(Diz(1, 3))
    d1.işle(_ * 10) should be(Diz(10, 30, 20))
    d1.düzİşle(x => Diz(x, x*x)) should be(Diz(1, 1, 3, 9, 2, 4))
    d1.sıralı should be(Diz(1, 2, 3))
    d1.sırala(1.0 / _) should be(Diz(3, 2, 1))
    d1.sırayaSok((x, y) => -x < -y) should be(Diz(3, 2, 1))
    d1.indirge((x, y) => x * 10 + y) should be(132)
    d1.soldanKatla(10)(_ + _) should be(16)
    d1.sağdanKatla(2)(_ * _) should be(12)
    val d2 = Diz(2, 3, 4)
    d2.topla should be(9)
    d2.çarp should be(24)
    Diz(2, 2, 1, 1).yinelemesiz should be(Diz(2, 1))
    Diz(2, 4, 6, 1, 3, 5).yinelemesizİşlevle(_ % 2 == 0) should be(Diz(2, 1))
    Diz(1, 2, 3).yazıYap should be("123")
    Diz(1, 2, 3).yazıYap(" ") should be("1 2 3")
    Diz(1, 2, 3).yazıYap("{", " ", "}") should be("{1 2 3}")
    Diz(2, 4).değiştir(0, 5) should be(Diz(5, 4))
  }

  test("Translations of Seq[T] methods to work") {
    // todo: duplicates above and below
    val d1 = Dizi(1, 3, 2)
    d1.başı should be(1)
    d1.kuyruğu should be(Dizi(3, 2))
    d1.boyu should be(3)
    d1.boşMu should be(yanlış)
    d1.doluMu should be(doğru)
    d1.ele(_ % 2 == 0) should be(Dizi(2))
    d1.eleDeğilse(_ % 2 == 0) should be(Dizi(1, 3))
    d1.işle(_ * 10) should be(Dizi(10, 30, 20))
    d1.düzİşle(x => Dizi(x, x*x)) should be(Dizi(1, 1, 3, 9, 2, 4))
    d1.sıralı should be(Dizi(1, 2, 3))
    d1.sırala(1.0 / _) should be(Dizi(3, 2, 1))
    d1.sırayaSok((x, y) => -x < -y) should be(Dizi(3, 2, 1))
    d1.indirge((x, y) => x * 10 + y) should be(132)
    d1.soldanKatla(10)(_ + _) should be(16)
    d1.sağdanKatla(2)(_ * _) should be(12)
    val d2 = Dizi(2, 3, 4)
    d2.topla should be(9)
    d2.çarp should be(24)
    Dizi(2, 2, 1, 1).yinelemesiz should be(Dizi(2, 1))
    Dizi(2, 4, 6, 1, 3, 5).yinelemesizİşlevle(_ % 2 == 0) should be(Dizi(2, 1))
    Dizi(1, 2, 3).yazıYap should be("123")
    Dizi(1, 2, 3).yazıYap(" ") should be("1 2 3")
    Dizi(1, 2, 3).yazıYap("{", " ", "}") should be("{1 2 3}")
    Dizi(2, 4).değiştir(0, 5) should be(Dizi(5, 4))
  }

  test("Translations of Array[T] methods to work") {
    val d = Dizik(10, 3, 9); d.boyu should be(3)
    val golden = Dizik(5, 1, 4)
    d.işle(_/2) should be(golden)
    d.işleYerinde(_/2) should be(golden); d should be(golden)
    val gold2 = Dizik(100, 1, 4)
    d.değiştir(0, 100) should be(gold2)
    d.değiştirYerinde(0, 100); d should be(gold2)
    d.değiştirYerinde(2, 100); d should be(Dizik(100, 1, 100))
    d(1) = 100; d should be(Dizik(100, 100, 100))
    val d0 = Dizik[Sayı](); d0.boyu should be(0)
    // copied from above
    val d1 = Dizik(1, 3, 2)
    d1.başı should be(1)
    d1.kuyruğu should be(Dizik(3, 2))
    d1.boyu should be(3)
    d1.boşMu should be(yanlış)
    d1.doluMu should be(doğru)
    d1.ele(_ % 2 == 0) should be(Dizik(2))
    d1.eleDeğilse(_ % 2 == 0) should be(Dizik(1, 3))
    d1.işle(_ * 10) should be(Dizik(10, 30, 20))
    d1.düzİşle(x => Dizik(x, x*x)) should be(Dizik(1, 1, 3, 9, 2, 4))
    d1.sıralı should be(Dizik(1, 2, 3))
    d1.sırala(1.0 / _) should be(Dizik(3, 2, 1))
    d1.sırayaSok((x, y) => -x < -y) should be(Dizik(3, 2, 1))
    d1.indirge((x, y) => x * 10 + y) should be(132)
    d1.soldanKatla(10)(_ + _) should be(16)
    d1.sağdanKatla(2)(_ * _) should be(12)
    val d2 = Dizik(2, 3, 4)
    d2.topla should be(9)
    d2.çarp should be(24)
    Dizik(2, 2, 1, 1).yinelemesiz should be(Dizik(2, 1))
    Dizik(2, 4, 6, 1, 3, 5).yinelemesizİşlevle(_ % 2 == 0) should be(Dizik(2, 1))
    Dizik(1, 2, 3).yazıYap should be("123")
    Dizik(1, 2, 3).yazıYap(" ") should be("1 2 3")
    Dizik(1, 2, 3).yazıYap("{", " ", "}") should be("{1 2 3}")
    Dizik(2, 4).değiştir(0, 5) should be(Dizik(5, 4))
  }

  test("Translations of Char methods to work") {
    val h1: Harf = 'a'
    val h2: Harf = h1.büyükHarfe
    h2 should be('A')
    h2.küçükHarfe should be(h1)
    h1.sayıya should be(97)
    h1.kesire should be(97.0)
    h1.yazıya should be("a")
    h1.sayıMı should be(yanlış)
    '3'.sayıMı should be(doğru)
    h1.boşlukMu should be(yanlış)
    ' '.boşlukMu should be(doğru)
    '\t'.boşlukMu should be(doğru)
    h1.küçükHarfMi should be(doğru)
    h2.küçükHarfMi should be(yanlış)
    h1.büyükHarfMi should be(yanlış)
    h2.büyükHarfMi should be(doğru)
  }

  test("Translations of to, until and by to work") {
    val d1 = for (i <- 1 |- 4) yield i
    d1 should be(Dizi(1, 2, 3))
    val d2 = for (i <- 1 |-| 4) yield i
    d2 should be(Dizi(1, 2, 3, 4))
    val d3 = for (i <- 1 |- 10 adım 3) yield i
    d3 should be(Dizi(1, 4, 7))
    val d4 = for (i <- 1 |-| 10 adım 3) yield i
    d4 should be(Dizi(1, 4, 7, 10))
    val op = (x: Int, y: Int) => x - y
    d4.indirge(op) should be(-20)
    d4.soldanKatla(100)(op) should be(78)
    d4.sağdanKatla(100)(op) should be(94)
  }

  test("Translations of Range methods to work") {
    val r = 1 until 30
    r.içindeMi(30) should be(false)
    r.boyu should be(29)
    val r2 = r by 10
    r2.dizine should be(Dizin(1, 11, 21))
    r2.diziye should be(Dizi(1, 11, 21))
  }

  test("Translations of Any, AnyRef and Object methods to work") {
    val o = new Object
    o.yazıya.boyu > 0 should be(true)
    val d = Dizi(1, 2)
    d.yazıya should be("ArraySeq(1, 2)")
    val d2 = Dizin(1, 2)
    d2.yazıya should be("List(1, 2)")
    val x = 5
    x.yazıya should be("5")
    val y = 5.0
    y.yazıya should be("5.0")
    val h = 'a'
    h.yazıya should be("a")
    case class Foo(x: Int)
    val f1 = Foo(1)
    val f2 = Foo(1)
    val f3 = Foo(2)
    f1 eşitMi f2 should be(true)
    f1 eşitMi f3 should be(false)
    f1 aynıMı f1 should be(true)
    f1 aynıMı f2 should be(false)
    f2 aynıMı f3 should be(false)
    def check1(f: => Foo): İkil = f1 aynıMı f
    def check2(f: Foo): İkil = f1 aynıMı f
    check1(f1) should be(true)
    check2(f1) should be(true)
  }

  test("Translations of Int and Double methods to work") {
    1.5.sayıya should be(1)
    1.kesire should be(1.0)
    0.toDegrees should be(0)
    piSayısı.toDegrees should be(180)
    val s1 = Sayılar(); s1.diziye.boyu should be(0)
    val s2 = s1 :+ 0; s2(0) should be(0)
    val slar = Sayılar(1, 2, 3); slar.diziye.boyu should be(3)
    slar(0) should be(1)
    slar(1) should be(2)
    slar(2) should be(3)
    val epeyİri = "12345678909876543210"
    val is = İriSayı(epeyİri); is.kesire > 1e19 should be(true)
    is.yazıya should be(epeyİri)
    val i2 = İriSayı("12345678909876543209")
    is > i2 should be(true)
    i2 enUfağı is should be(i2)
    is enİrisi i2 should be(is)
    val iky = "0.00000000000000000005"
    val ik1 = İriKesir(iky)
    val ik2 = İriKesir("0.00000000000000000004")
    ik2 enUfağı ik1 should be(ik2)
    val ik3 = İriKesir(1.0) / İriKesir(0.9)
    ik3 should be(İriKesir("1.111111111111111111111111111111111"))
    ik3.ölçek should be(33)
  }

  ignore("Translation of java.util.Calendar and System.nanoTime etc to work") {
    yinele (4) {
      val b = BuAn()
      val (saniye, dakika, saat) = (b.saniye, b.dakika, b.saat)
      saniye >= 0 && saniye <= 59 should be(true)
      dakika >= 0 && dakika <= 59 should be(true)
      saat >= 0 && saat <= 24 should be(true)
    }
    buSaniye > 1.0E9 should be(true)
    // used to get > 80k, but now I get only > 44k. Let's be very safe (Lalit reported failure even for 4k):
    // println(s"buSaniye2=$buSaniye2")
    buSaniye2 > 1.0 should be(true) // Returns the current value of the running Java Virtual Machine's high-resolution time source, in nanoseconds.
    buAn > 1_659_458_389_799L should be(true)
    buAn2 > İriSayı("1659457918531") should be(true)
    sayıyaKadarSay(5000, doğru) < 0.01 should be(true) // in my runs, we get: 0.003 (3 millisec)
    sayıyaKadarSay(1000000, doğru) < 0.1 should be(true) // we get: 0.022 (22 msec)
  }

  test("Companion objects for translations to work") {
    val e = Eşlek("a" -> 10, "b" -> 3); e.sayı should be(2)
    var d = Diz(1, 2, 4); d.boyu should be(3)
    d = d :+ 3; d.boyu should be(4)
    val k = Küme(100, 10, 1); k.boyu should be(3)
    val k2 = Küme.boş[Yazı]; k2.boyu should be(0)
    val k3 = k2 + "Merhaba"; k3.boyu should be(1)
    (k3 + "Dünya").boyu should be(2)
    (k3 - "Merhaba").boyu should be(0)
    val m = MiskinDizin.sayalım(3, 2)
    m al 2 should be(Dizi(3, 5))
    m al 100 alSağdan 2 should be(Dizi(199, 201))
  }

  test("Dizi ailesi: ortak çekirdek -- Dizi") {
    val d = Dizi(3, 1, 2)
    d.başıBelki should be(Biri(3)); Dizi[Sayı]().sonuBelki should be(Hiçbiri)
    d.bul(_ > 1) should be(Biri(3)); d.bulSondan(_ > 1) should be(Biri(2))
    d.nerede(_ == 1) should be(1); d.nerede(_ > 1, 1) should be(2); d.neredeSondan(_ > 1) should be(2)
    d.dilimSırası(Dizi(1, 2)) should be(1); d.dilimSırasıSondan(Dizi(3)) should be(0)
    d.sıralar.toList should be(Dizin(0, 1, 2))
    d.başındaMı(Dizi(3, 1)) should be(doğru); d.sonundaMı(Dizi(2)) should be(doğru)
    d.karşılıklıMı(Dizi(6, 2, 4))(_ * 2 == _) should be(doğru)
    // bölme, öbekleme
    d.böl(_ > 1) should be((Dizi(3, 2), Dizi(1)))
    d.bölİşle(x => if (x > 1) Left(x) else Right(x.yazıya)) should be((Dizi(3, 2), Dizi("1")))
    d.bölDoğruKaldıkça(_ > 2) should be((Dizi(3), Dizi(1, 2)))
    d.bölYerinden(1) should be((Dizi(3), Dizi(1, 2)))
    d.öbekli(2).toList should be(Dizin(Dizi(3, 1), Dizi(2)))
    d.kayarÖbekli(2).toList should be(Dizin(Dizi(3, 1), Dizi(1, 2)))
    d.kayarÖbekli(2, 2).toList should be(Dizin(Dizi(3, 1), Dizi(2)))
    d.öbekleİşle(_ % 2)(_ * 10) should be(Eşlek(1 -> Dizi(30, 10), 0 -> Dizi(20)))
    d.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    d.kombinasyonlar(2).toList should be(Dizin(Dizi(3, 1), Dizi(3, 2), Dizi(1, 2)))
    d.permütasyonlar.size should be(6)
    d.kuyruklar.toList should be(Dizin(Dizi(3, 1, 2), Dizi(1, 2), Dizi(2), Dizi()))
    d.önler.toList should be(Dizin(Dizi(3, 1, 2), Dizi(3, 1), Dizi(3), Dizi()))
    // katlama, indirgeme, tarama
    d.katla(0)(_ + _) should be(6)
    d.indirgeSoldan(_ - _) should be(0); d.indirgeSağdan(_ - _) should be(4)
    d.indirgeBelki(_ + _) should be(Biri(6)); Dizi[Sayı]().indirgeBelki(_ + _) should be(Hiçbiri)
    d.indirgeSoldanBelki(_ + _) should be(Biri(6)); d.indirgeSağdanBelki(_ + _) should be(Biri(6))
    d.tara(0)(_ + _) should be(Dizi(0, 3, 4, 6))
    d.taraSoldan("")(_ + _) should be(Dizi("", "3", "31", "312"))
    d.taraSağdan(0)(_ + _) should be(Dizi(6, 3, 2, 0))
    d.enUfağıBelki should be(Biri(1)); d.enİrisiBelki should be(Biri(3))
    Dizi[Sayı]().enİrisiBelki should be(Hiçbiri)
    d.enUfağıBelki(x => -x) should be(Biri(3)); d.enİrisiBelki(x => -x) should be(Biri(1))
    // ekleme, çıkarma
    d.sonunaEkle(9) should be(Dizi(3, 1, 2, 9)); d.önüneEkle(9) should be(Dizi(9, 3, 1, 2))
    d.sonunaEkleHepsini(Dizi(7, 8)) should be(Dizi(3, 1, 2, 7, 8))
    d.önüneEkleHepsini(Dizi(7, 8)) should be(Dizi(7, 8, 3, 1, 2))
    d.uzat(5, 0) should be(Dizi(3, 1, 2, 0, 0))
    d.yama(1, Dizi(8, 9), 1) should be(Dizi(3, 8, 9, 2))
    d.fark(Dizi(1)) should be(Dizi(3, 2)); d.kesişim(Dizi(2, 3)) should be(Dizi(3, 2))
    d.bileşim(Dizi(4)) should be(Dizi(3, 1, 2, 4))
    // seçme, düzleştirme, ikili işlemler
    d.seçİşle { case x if x > 1 => x * 10 } should be(Dizi(30, 20))
    d.seçİşleİlk { case x if x < 3 => x } should be(Biri(1))
    Dizi(Dizi(1, 2), Dizi(3)).düzleştir should be(Dizi(1, 2, 3))
    Dizi(Dizi(1, 2), Dizi(3, 4)).devrik should be(Dizi(Dizi(1, 3), Dizi(2, 4)))
    Dizi((1, "a"), (2, "b")).ikiliyiAç should be((Dizi(1, 2), Dizi("a", "b")))
    d.ikileHepsini(Dizi("x"), -1, "-") should be(Dizi((3, "x"), (1, "-"), (2, "-")))
    d.tersİşle(_ * 2) should be(Dizi(4, 2, 6))
  }

  test("Dizi ailesi: ortak çekirdek -- Diz, SıralıDizi, Dizin, Yöney") {
    val z = Diz(3, 1, 2)
    z.bul(_ > 2) should be(Biri(3)); z.böl(_ > 1)._1 should be(Diz(3, 2))
    z.tara(0)(_ + _) should be(Diz(0, 3, 4, 6)); z.sonunaEkle(9).sonu should be(9)
    z.öbekli(2).toList.boyu should be(2); z.enİrisiBelki should be(Biri(3))
    val s: SıralıDizi[Sayı] = Yöney(3, 1, 2)
    s.başıBelki should be(Biri(3)); s.neredeSondan(_ > 1) should be(2)
    s.bölYerinden(1)._2 should be(Dizi(1, 2)); s.uzat(4, 0).sonu should be(0)
    s.seçİşle { case x if x > 1 => x } should be(Dizi(3, 2))
    val n = Dizin(3, 1, 2)
    n.bul(_ > 1) should be(Biri(3)); n.bulSondan(_ > 1) should be(Biri(2))
    n.böl(_ > 1) should be((Dizin(3, 2), Dizin(1)))
    n.bölDoğruKaldıkça(_ > 2) should be((Dizin(3), Dizin(1, 2)))
    n.kayarÖbekli(2).toList should be(Dizin(Dizin(3, 1), Dizin(1, 2)))
    n.taraSoldan(0)(_ + _) should be(Dizin(0, 3, 4, 6))
    n.sonunaEkle(9) should be(Dizin(3, 1, 2, 9)); n.önüneEkle(9) should be(Dizin(9, 3, 1, 2))
    n.fark(Dizin(1)) should be(Dizin(3, 2)); n.kuyruklar.toList.boyu should be(4)
    n.seçİşleİlk { case x if x < 3 => x } should be(Biri(1))
    n.ikileHepsini(Dizin("x"), -1, "-") should be(Dizin((3, "x"), (1, "-"), (2, "-")))
    val y = Yöney(3, 1, 2)
    y.başıBelki should be(Biri(3)); y.bul(_ > 1) should be(Biri(3))
    y.böl(_ > 1) should be((Yöney(3, 2), Yöney(1)))
    y.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    y.tara(0)(_ + _) should be(Yöney(0, 3, 4, 6))
    y.sonunaEkleHepsini(Yöney(7)) should be(Yöney(3, 1, 2, 7))
    y.uzat(5, 0) should be(Yöney(3, 1, 2, 0, 0)); y.yama(1, Yöney(8), 1) should be(Yöney(3, 8, 2))
    y.tersİşle(_ * 2) should be(Yöney(4, 2, 6)); y.permütasyonlar.size should be(6)
  }

  test("Dizi ailesi: ortak çekirdek -- Dizik ve EsnekDizik") {
    val d = Dizik(3, 1, 2)
    d.başıBelki should be(Biri(3)); d.sonuBelki should be(Biri(2))
    d.bul(_ > 1) should be(Biri(3)); d.nerede(_ == 1) should be(1); d.neredeSondan(_ > 1) should be(2)
    d.sıralar.toList should be(Dizin(0, 1, 2))
    d.başındaMı(Dizik(3, 1)) should be(doğru); d.sonundaMı(Dizik(2)) should be(doğru)
    d.böl(_ > 1)._1 should be(Dizik(3, 2))
    d.bölİşle(x => if (x > 1) Left(x) else Right(x.yazıya))._2 should be(Dizik("1"))
    d.bölDoğruKaldıkça(_ > 2)._1 should be(Dizik(3)); d.bölYerinden(1)._2 should be(Dizik(1, 2))
    d.öbekli(2).toList.boyu should be(2); d.kayarÖbekli(2).toList.boyu should be(2)
    d.öbekleİşle(_ % 2)(_ * 10).al(1).al should be(Dizik(30, 10))
    d.kombinasyonlar(2).toList.boyu should be(3); d.permütasyonlar.size should be(6)
    d.kuyruklar.toList.boyu should be(4); d.önler.toList.boyu should be(4)
    d.katla(0)(_ + _) should be(6)
    d.tara(0)(_ + _) should be(Dizik(0, 3, 4, 6))
    d.taraSoldan(0)(_ + _) should be(Dizik(0, 3, 4, 6)); d.taraSağdan(0)(_ + _) should be(Dizik(6, 3, 2, 0))
    d.sonunaEkle(9) should be(Dizik(3, 1, 2, 9)); d.önüneEkle(9) should be(Dizik(9, 3, 1, 2))
    d.sonunaEkleHepsini(Dizik(7)) should be(Dizik(3, 1, 2, 7))
    d.önüneEkleHepsini(Dizik(7)) should be(Dizik(7, 3, 1, 2))
    d.uzat(5, 0) should be(Dizik(3, 1, 2, 0, 0)); d.yama(1, Dizik(8, 9), 1) should be(Dizik(3, 8, 9, 2))
    d.fark(Dizi(1)) should be(Dizik(3, 2)); d.kesişim(Dizi(2, 3)) should be(Dizik(3, 2))
    d.seçİşle { case x if x > 1 => x * 10 } should be(Dizik(30, 20))
    d.seçİşleİlk { case x if x < 3 => x } should be(Biri(1))
    Dizik(Dizik(1, 2), Dizik(3)).düzleştir should be(Dizik(1, 2, 3))
    Dizik(Dizik(1, 2), Dizik(3, 4)).devrik should be(Dizik(Dizik(1, 3), Dizik(2, 4)))
    val (dSayılar, dHarfler) = Dizik((1, "a"), (2, "b")).ikiliyiAç // ikili demet: parçaları ayrı ayrı
    dSayılar should be(Dizik(1, 2)); dHarfler should be(Dizik("a", "b"))
    d.ikileHepsini(Dizi("x"), -1, "-") should be(Dizik((3, "x"), (1, "-"), (2, "-")))
    d.tersİşle(_ * 2) should be(Dizik(4, 2, 6))

    val e = EsnekDizik(3, 1, 2)
    e.başıBelki should be(Biri(3)); e.bul(_ > 1) should be(Biri(3)); e.bulSondan(_ > 1) should be(Biri(2))
    e.nerede(_ == 1) should be(1); e.sıralar.toList should be(Dizin(0, 1, 2))
    e.karşılıklıMı(Dizi(6, 2, 4))(_ * 2 == _) should be(doğru)
    e.böl(_ > 1)._1 should be(EsnekDizik(3, 2))
    e.bölDoğruKaldıkça(_ > 2)._1 should be(EsnekDizik(3))
    e.öbekli(2).toList.boyu should be(2)
    e.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    e.katla(0)(_ + _) should be(6); e.indirgeBelki(_ + _) should be(Biri(6))
    e.tara(0)(_ + _) should be(EsnekDizik(0, 3, 4, 6))
    e.enUfağıBelki should be(Biri(1)); e.enİrisiBelki should be(Biri(3))
    e.sonunaEkle(9) should be(EsnekDizik(3, 1, 2, 9))
    e.önüneEkleHepsini(EsnekDizik(7)) should be(EsnekDizik(7, 3, 1, 2))
    e.uzat(5, 0) should be(EsnekDizik(3, 1, 2, 0, 0))
    e.fark(Dizi(1)) should be(EsnekDizik(3, 2))
    e.seçİşle { case x if x > 1 => x * 10 } should be(EsnekDizik(30, 20))
    e.tersİşle(_ * 2) should be(EsnekDizik(4, 2, 6))
    e should be(EsnekDizik(3, 1, 2)) // hiçbiri e'yi DEĞİŞTİRMEDİ
  }

  test("Küme: ortak çekirdek") {
    val k = Küme(3, 1, 2)
    k.önü.boyu should be(2); k.sonu should be(2)
    k.başıBelki should be(Biri(3)); Küme[Sayı]().sonuBelki should be(Hiçbiri)
    k.bul(_ > 2) should be(Biri(3)); k.bul(_ > 9) should be(Hiçbiri)
    k.bölİşle(x => if (x > 1) Left(x) else Right(x.yazıya)) should be((Küme(3, 2), Küme("1")))
    k.bölDoğruKaldıkça(_ > 2)._1 should be(Küme(3))
    k.bölYerinden(1) should be((Küme(3), Küme(1, 2)))
    k.kayarÖbekli(2).toList should be(Dizin(Küme(3, 1), Küme(1, 2)))
    k.öbekleİşle(_ % 2)(_ * 10) should be(Eşlek(1 -> Küme(30, 10), 0 -> Küme(20)))
    k.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    k.kuyruklar.toList.boyu should be(4); k.önler.toList.boyu should be(4)
    k.katla(0)(_ + _) should be(6)
    k.indirgeSoldan(_ + _) should be(6); k.indirgeSağdan(_ + _) should be(6)
    k.indirgeBelki(_ + _) should be(Biri(6)); Küme[Sayı]().indirgeBelki(_ + _) should be(Hiçbiri)
    k.indirgeSoldanBelki(_ + _) should be(Biri(6)); k.indirgeSağdanBelki(_ + _) should be(Biri(6))
    k.tara(0)(_ + _) should be(Küme(0, 3, 4, 6))
    k.taraSoldan(0)(_ + _) should be(Küme(0, 3, 4, 6))
    k.taraSağdan(0)(_ + _) should be(Küme(6, 3, 2, 0))
    k.enUfağıBelki should be(Biri(1)); k.enİrisiBelki should be(Biri(3))
    Küme[Sayı]().enİrisiBelki should be(Hiçbiri)
    k.enUfağıBelki(x => -x) should be(Biri(3)); k.enİrisiBelki(x => -x) should be(Biri(1))
    k.fark(Küme(1)) should be(Küme(3, 2))
    k.seçİşle { case x if x > 1 => x * 10 } should be(Küme(30, 20))
    k.seçİşleİlk { case x if x < 3 => x } should be(Biri(1))
    Küme(Dizi(1, 2), Dizi(3)).düzleştir should be(Küme(1, 2, 3))
    Küme((1, "a"), (2, "b")).ikiliyiAç should be((Küme(1, 2), Küme("a", "b")))
    k.ikileHepsini(Dizi("x"), -1, "-").boyu should be(3)
  }

  test("Eşlek ve Eşlem: ortak çekirdek") {
    // Eşlek: değişmez eşlem; ögesi bir İKİLİ (anahtar -> değer)
    val e = Eşlek("a" -> 1, "b" -> 2, "c" -> 3)
    e.başıBelki should be(Biri(("a", 1)))
    Eşlek[Yazı, Sayı]().sonuBelki should be(Hiçbiri)
    e.bul(_._2 > 2) should be(Biri(("c", 3)))
    e.böl(_._2 > 1) should be((Eşlek("b" -> 2, "c" -> 3), Eşlek("a" -> 1)))
    e.bölDoğruKaldıkça(_._2 < 3)._1 should be(Eşlek("a" -> 1, "b" -> 2))
    e.bölYerinden(1)._2 should be(Eşlek("b" -> 2, "c" -> 3))
    e.öbekli(2).toList.boyu should be(2)
    e.kayarÖbekli(2).toList.boyu should be(2)
    e.öbekleİşleİndirge(_._2 % 2)(_._2)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    e.kuyruklar.toList.boyu should be(4); e.önler.toList.boyu should be(4)
    e.indirgeBelki((x, y) => (x._1 + y._1, x._2 + y._2)) should be(Biri(("abc", 6)))
    e.taraSoldan(0)((s, ikili) => s + ikili._2).dizine should be(Dizin(0, 1, 3, 6))
    e.enUfağıBelki(_._2) should be(Biri(("a", 1)))
    e.enİrisiBelki(_._2) should be(Biri(("c", 3)))
    e.seçİşle { case (a, d) if d > 1 => a }.kümeye should be(Küme("b", "c"))
    e.seçİşleİlk { case (a, d) if d > 2 => a } should be(Biri("c"))
    e.dilim(0, 2).boyu should be(2)
    val (anahtarlar, değerler) = e.ikiliyiAç
    anahtarlar.kümeye should be(Küme("a", "b", "c")); değerler.kümeye should be(Küme(1, 2, 3))
    e.ikileHepsini(Dizi("x"), ("-", 0), "-").size should be(3)

    // Eşlem: değişir eşlem, aynı yöntemler
    val m = Eşlem("a" -> 1, "b" -> 2, "c" -> 3)
    m.başıBelki should be(Biri(("a", 1)))
    m.bul(_._2 > 2) should be(Biri(("c", 3)))
    m.böl(_._2 > 1)._1.size should be(2)
    m.bölYerinden(1)._2.size should be(2)
    m.öbekli(2).toList.boyu should be(2)
    m.öbekleİşleİndirge(_._2 % 2)(_._2)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    m.indirgeBelki((x, y) => (x._1 + y._1, x._2 + y._2)) should be(Biri(("abc", 6)))
    m.taraSoldan(0)((s, ikili) => s + ikili._2).dizine should be(Dizin(0, 1, 3, 6))
    m.enİrisiBelki(_._2) should be(Biri(("c", 3)))
    m.seçİşle { case (a, d) if d > 1 => a }.kümeye should be(Küme("b", "c"))
    m.dilim(0, 2).size should be(2)
    m.boyu should be(3) // hiçbiri m'i DEĞİŞTİRMEDİ
  }

  test("Kuyruk, Yazı ve Belki: ortak çekirdek") {
    val k = Kuyruk(3, 1, 2)
    k.başıBelki should be(Biri(3)); k.bul(_ > 1) should be(Biri(3)); k.bulSondan(_ > 1) should be(Biri(2))
    k.nerede(_ == 1) should be(1); k.neredeSondan(_ > 1) should be(2)
    k.sıralar.toList should be(Dizin(0, 1, 2))
    k.böl(_ > 1)._1 should be(Kuyruk(3, 2))
    k.bölDoğruKaldıkça(_ > 2)._1 should be(Kuyruk(3)); k.bölYerinden(1)._2 should be(Kuyruk(1, 2))
    k.öbekli(2).toList.boyu should be(2); k.kayarÖbekli(2).toList.boyu should be(2)
    k.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    k.katla(0)(_ + _) should be(6); k.indirgeBelki(_ + _) should be(Biri(6))
    k.tara(0)(_ + _) should be(Kuyruk(0, 3, 4, 6))
    k.enUfağıBelki should be(Biri(1)); k.enİrisiBelki should be(Biri(3))
    k.sonunaEkle(9) should be(Kuyruk(3, 1, 2, 9)); k.önüneEkle(9) should be(Kuyruk(9, 3, 1, 2))
    k.uzat(5, 0) should be(Kuyruk(3, 1, 2, 0, 0)); k.yama(1, Dizi(8), 1) should be(Kuyruk(3, 8, 2))
    k.fark(Dizi(1)) should be(Kuyruk(3, 2)); k.kesişim(Dizi(2, 3)) should be(Kuyruk(3, 2))
    k.seçİşle { case x if x > 1 => x * 10 } should be(Kuyruk(30, 20))
    k.tersİşle(_ * 2) should be(Kuyruk(4, 2, 6))
    k.boyu should be(3) // hiçbiri kuyruğu DEĞİŞTİRMEDİ

    val y = "merhaba"
    y.başıBelki should be(Biri('m')); "".sonuBelki should be(Hiçbiri)
    y.bul(_ == 'h') should be(Biri('h')); y.nerede(_ == 'h') should be(3)
    y.neredeSondan(_ == 'a') should be(6); y.sıralar.boyu should be(7)
    y.bölDoğruKaldıkça(_ != 'h') should be(("mer", "haba"))
    y.bölYerinden(3) should be(("mer", "haba"))
    y.öbekli(3).toList should be(Dizin("mer", "hab", "a"))
    y.kayarÖbekli(3).toList.başı should be("mer")
    y.kuyruklar.toList.boyu should be(8); y.önler.toList.boyu should be(8)
    "abc".kombinasyonlar(2).toList should be(Dizin("ab", "ac", "bc"))
    "abc".permütasyonlar.toList.boyu should be(6)
    y.dilim(0, 3) should be("mer")
    "ab".uzat(4, '-') should be("ab--")
    "merhaba".yama(0, "M", 1) should be("Merhaba")
    "merhaba".fark("aeh") should be("mrba")
    "merhaba".kesişim("ae") should be("ea")
    "merhab".sonunaEkle('a') should be("merhaba")
    "erhaba".önüneEkle('m') should be("merhaba")
    "mer".sonunaEkleHepsini("haba") should be("merhaba")
    "haba".önüneEkleHepsini("mer") should be("merhaba")

    val b: Belki[Sayı] = Biri(5)
    b.seçİşle { case x if x > 1 => x * 10 } should be(Biri(50))
    b.seçİşle { case x if x > 9 => x } should be(Hiçbiri)
    b.içeriyorMu(5) should be(doğru); b.içeriyorMu(6) should be(yanlış)
    b.varMı(_ > 1) should be(doğru); b.hepsiDoğruMu(_ > 9) should be(yanlış)
    Hiçbiri.hepsiDoğruMu((_: Sayı) > 9) should be(doğru) // boş her koşulu sağlar
    var toplam = 0; b.herbiriİçin(toplam += _); toplam should be(5)
    b.katla(0)(_ * 2) should be(10); (Hiçbiri: Belki[Sayı]).katla(-1)(_ * 2) should be(-1)
    Biri(Biri(7)).düzleştir should be(Biri(7))
    b.ikile(Biri("a")) should be(Biri((5, "a"))); b.ikile(Hiçbiri) should be(Hiçbiri)
    Biri((1, "a")).ikiliyiAç should be((Biri(1), Biri("a")))
    b.diziye should be(Dizi(5))
  }

  test("Türe özgü adlar: Yazı, Küme, Belki") {
    "merhaba\ndünya".satırlar.toList should be(Dizin("merhaba", "dünya"))
    "merhaba".başındanAt("mer") should be("haba"); "merhaba".sonundanAt("aba") should be("merh")
    "satır\n".satırSonunuAt should be("satır")
    "42".uzuna should be(42L); "abc".uzunaBelki should be(Hiçbiri); "7".uzunaBelki should be(Biri(7L))
    "merhaba".ikiyeAyır(_ == 'a') should be(("aa", "merhb"))
    "abc".ikiyeAyırİşle(h => eğer (h == 'b') Left(h) yoksa Right(h.büyükHarfe)) should be((Dizi('b'), Dizi('A', 'C')))
    "abc".seçİşle { durum h eğer h != 'b' => h.büyükHarfe } should be(Dizi('A', 'C'))

    val k = Küme(1, 2)
    k.ekli(3) should be(Küme(1, 2, 3)); k.çıkarılmış(1) should be(Küme(2))
    k.hepsiÇıkarılmış(Dizi(1, 2)) should be(Küme[Sayı]())
    Küme(1).altKümesiMi(k) should be(doğru); k.altKümesiMi(Küme(1)) should be(yanlış)

    val b: Belki[Sayı] = Biri(5)
    b.boşsaÖbürü(Biri(9)) should be(Biri(5))       // yoksa ANAHTAR KELİME olduğu için bu ad
    (Hiçbiri: Belki[Sayı]).boşsaÖbürü(Biri(9)) should be(Biri(9))
    b.sola("sağdaki") should be(Left(5)); (Hiçbiri: Belki[Sayı]).sola("sağdaki") should be(Right("sağdaki"))
    b.sağa("soldaki") should be(Right(5)); (Hiçbiri: Belki[Sayı]).sağa("soldaki") should be(Left("soldaki"))
    Belki.iseVer(doğru)(3) should be(Biri(3)); Belki.iseVer(yanlış)(3) should be(Hiçbiri)
    Belki.değilseVer(yanlış)(3) should be(Biri(3)); Belki.değilseVer(doğru)(3) should be(Hiçbiri)
  }

  test("Yerinde değiştirenler: EsnekDizik, Kuyruk, Eşlem") {
    val e = EsnekDizik(3, 1, 2)
    e.eleYerinde(_ > 1) should be(EsnekDizik(3, 2)); e should be(EsnekDizik(3, 2))
    e.işleYerinde(_ * 10) should be(EsnekDizik(30, 20)); e should be(EsnekDizik(30, 20))
    e.sıralıYerinde should be(EsnekDizik(20, 30)); e should be(EsnekDizik(20, 30))
    e.başaEkle(5); e should be(EsnekDizik(5, 20, 30))
    e.araEkle(1, 7); e should be(EsnekDizik(5, 7, 20, 30))
    e.hepsiniEkle(Dizi(40, 50)); e should be(EsnekDizik(5, 7, 20, 30, 40, 50))
    e.çıkar(0, 2); e should be(EsnekDizik(20, 30, 40, 50))
    e.baştanKırp(1); e should be(EsnekDizik(30, 40, 50))
    e.sondanKırp(1); e should be(EsnekDizik(30, 40))
    e.alYerinde(1) should be(EsnekDizik(30))
    val kopya = e.kopyası; kopya.boşalt(); kopya.boşMu should be(doğru); e.boşMu should be(yanlış)

    val ku = Kuyruk(1, 2)
    ku.kuyruğaEkle(3) should be(Kuyruk(1, 2, 3))
    ku.ilki should be(1); ku.baştanÇıkar() should be(1); ku should be(Kuyruk(2, 3))
    ku.baştanÇıkarBelki should be(Biri(2)); ku should be(Kuyruk(3))
    ku.kuyruğaEkleHepsini(Dizi(4, 5)); ku should be(Kuyruk(3, 4, 5))
    ku.sondanÇıkar() should be(5); ku should be(Kuyruk(3, 4))
    ku.baştanÇıkarDoğruKaldıkça(_ < 4) should be(Dizi(3)); ku should be(Kuyruk(4))
    ku.eleYerinde(_ > 9); ku.boşMu should be(doğru)
    ku.baştanÇıkarBelki should be(Hiçbiri)

    val m = Eşlem("a" -> 1)
    m.koy("b", 2) should be(Hiçbiri); m.boyu should be(2)
    m.güncelle("a", 10); m.al("a") should be(Biri(10))
    m.alYoksaEkle("c", 3) should be(3); m.alYoksaEkle("c", 9) should be(3)
    m.çıkar("c") should be(Biri(3)); m.al("c") should be(Hiçbiri)
    m.değerleriİşleYerinde((_, d) => d * 2); m.al("a") should be(Biri(20))
    m.eleYerinde(_._1 == "a"); m.boyu should be(1)
    m.değerleriİşle(_ + 1) should be(Eşlek("a" -> 21))
    m.anahtarlarıEle(_ == "z").boyu should be(0)
    var toplam = 0; m.herİkiliİçin((_, d) => toplam += d); toplam should be(20)
    m.anahtarYineleyici.toList should be(Dizin("a")); m.değerYineleyici.toList should be(Dizin(20))
    m.boşalt(); m.boşMu should be(doğru)

    // Eşlek (değişmez): yeni eşlek verir, olanı değiştirmez
    val ek = Eşlek("a" -> 1, "b" -> 2)
    ek.çıkarılmış("a") should be(Eşlek("b" -> 2)); ek.boyu should be(2)
    ek.hepsiÇıkarılmış(Dizi("a", "b")) should be(Eşlek[Yazı, Sayı]())
    ek.değiştirİşlevle("a")(_ => Biri(9)) should be(Eşlek("a" -> 9, "b" -> 2))
    ek.değerleriİşle(_ * 10) should be(Eşlek("a" -> 10, "b" -> 20))
    ek.dönüştür((_, d) => d + 1) should be(Eşlek("a" -> 2, "b" -> 3))
    ek.varsayılanlı(_ => 0).getOrElse("z", -1) should be(-1)
  }

  test("ÖncelikSırası: ortak çekirdek (sonuç Dizi)") {
    val ö = ÖncelikSırası(3, 1, 2)
    ö.başıBelki should be(Biri(3))                 // en büyük başta
    ö.bul(_ < 2) should be(Biri(1))
    ö.böl(_ > 1)._1.sıralı should be(Dizi(2, 3))
    ö.bölYerinden(1)._1 should be(Dizi(3))
    ö.öbekli(2).toList.boyu should be(2)
    ö.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    ö.katla(0)(_ + _) should be(6); ö.indirgeBelki(_ + _) should be(Biri(6))
    ö.taraSoldan(0)(_ + _) should be(Dizi(0, 3, 4, 6))
    ö.enUfağıBelki should be(Biri(1)); ö.enİrisiBelki should be(Biri(3))
    ö.seçİşle { durum x eğer x > 1 => x * 10 }.sıralı should be(Dizi(20, 30))
    ö.kuyruğa.boyu should be(3)
    ö.işleYerinde(_ * 10); ö.başı should be(30)
    ö.hepsiniEkle(Dizi(100)); ö.başı should be(100)
  }

  test("MiskinDizin: tamamlanan yöntemler") {
    val m = MiskinDizin(3, 1, 2)
    m.önü.dizine should be(Dizin(3, 1)); m.sonu should be(2)
    m.başıBelki should be(Biri(3)); MiskinDizin.boş[Sayı].sonuBelki should be(Hiçbiri)
    m.bul(_ > 1) should be(Biri(3)); m.bulSondan(_ > 1) should be(Biri(2))
    m.nerede(_ == 1) should be(1); m.nerede(_ > 1, 1) should be(2); m.neredeSondan(_ > 1) should be(2)
    m.başındaMı(Dizi(3, 1)) should be(doğru); m.sonundaMı(Dizi(2)) should be(doğru)
    m.karşılıklıMı(Dizi(6, 2, 4))(_ * 2 == _) should be(doğru)
    m.sıralar.toList should be(Dizin(0, 1, 2))
    m.hepsiİçinDoğruMu(_ > 0) should be(doğru)
    // bölme ve öbekleme
    m.böl(_ > 1)._1.dizine should be(Dizin(3, 2))
    val (solda, sağda) = m.bölİşle(x => if (x > 1) Left(x) else Right(x.yazıya))
    solda.dizine should be(Dizin(3, 2)); sağda.dizine should be(Dizin("1"))
    m.bölDoğruKaldıkça(_ > 2)._1.dizine should be(Dizin(3))
    m.bölYerinden(1)._2.dizine should be(Dizin(1, 2))
    m.öbekli(2).toList.map(_.dizine) should be(Dizin(Dizin(3, 1), Dizin(2)))
    m.kayarÖbekli(2).toList.map(_.dizine) should be(Dizin(Dizin(3, 1), Dizin(1, 2)))
    m.kayarÖbekli(2, 2).toList.map(_.dizine) should be(Dizin(Dizin(3, 1), Dizin(2)))
    m.öbekleİşle(_ % 2)(_ * 10) should be(Eşlek(1 -> MiskinDizin(30, 10), 0 -> MiskinDizin(20)))
    m.öbekleİşleİndirge(_ % 2)(x => x)(_ + _) should be(Eşlek(1 -> 4, 0 -> 2))
    m.kombinasyonlar(2).toList.map(_.dizine) should be(Dizin(Dizin(3, 1), Dizin(3, 2), Dizin(1, 2)))
    m.permütasyonlar.size should be(6); m.kuyruklar.size should be(4); m.önler.size should be(4)
    // katlama ve tarama
    m.katla(0)(_ + _) should be(6)
    m.indirgeSoldan(_ - _) should be(0); m.indirgeSağdan(_ - _) should be(4)
    m.indirgeBelki(_ + _) should be(Biri(6)); MiskinDizin.boş[Sayı].indirgeBelki(_ + _) should be(Hiçbiri)
    m.indirgeSoldanBelki(_ + _) should be(Biri(6)); m.indirgeSağdanBelki(_ + _) should be(Biri(6))
    m.tara(0)(_ + _).dizine should be(Dizin(0, 3, 4, 6))
    m.taraSoldan("")(_ + _).dizine should be(Dizin("", "3", "31", "312"))
    m.taraSağdan(0)(_ + _).dizine should be(Dizin(6, 3, 2, 0))
    m.enUfağıBelki should be(Biri(1)); m.enİrisiBelki should be(Biri(3))
    MiskinDizin.boş[Sayı].enİrisiBelki should be(Hiçbiri)
    m.enUfağıBelki(x => -x) should be(Biri(3)); m.enİrisiBelki(x => -x) should be(Biri(1))
    // ekleme -- önüneEkle ve sonunaEkleHepsini TEMBEL: hesaplanmayan parça patlamaz
    m.sonunaEkle(9).dizine should be(Dizin(3, 1, 2, 9))
    m.önüneEkle(0).başı should be(0)
    var sayaç = 0
    val e = m.önüneEkle { sayaç += 1; 0 }
    sayaç should be(0) // öge ancak ilk erişimde hesaplanır
    e.başı should be(0); sayaç should be(1)
    m.önüneEkleHepsini(Dizi(7, 8)).dizine should be(Dizin(7, 8, 3, 1, 2))
    m.sonunaEkleHepsini(Dizi(7, 8)).dizine should be(Dizin(3, 1, 2, 7, 8))
    MiskinDizin.sayalım(1).sonunaEkleHepsini(throw new Exception("hesaplanmamalı")).al(3).dizine should be(Dizin(1, 2, 3))
    m.hepsiniHesapla.dizine should be(Dizin(3, 1, 2))
    m.uzat(5, 0).dizine should be(Dizin(3, 1, 2, 0, 0))
    m.yama(1, Dizi(8, 9), 1).dizine should be(Dizin(3, 8, 9, 2))
    m.fark(Dizi(1)).dizine should be(Dizin(3, 2)); m.kesişim(Dizi(2, 3)).dizine should be(Dizin(3, 2))
    m.bileşim(Dizi(4)).dizine should be(Dizin(3, 1, 2, 4))
    // seçme, düzleştirme, ikili işlemler
    m.seçİşle { case x if x > 1 => x * 10 }.dizine should be(Dizin(30, 20))
    m.seçİşleİlk { case x if x < 3 => x } should be(Biri(1))
    MiskinDizin(Dizi(1, 2), Dizi(3)).düzleştir.dizine should be(Dizin(1, 2, 3))
    MiskinDizin(Dizi(1, 2), Dizi(3, 4)).devrik.işle(_.dizine).dizine should be(Dizin(Dizin(1, 3), Dizin(2, 4)))
    val (sayılar, harfler) = MiskinDizin((1, "a"), (2, "b")).ikiliyiAç
    sayılar.dizine should be(Dizin(1, 2)); harfler.dizine should be(Dizin("a", "b"))
    m.ikileHepsini(Dizi("x"), -1, "-").dizine should be(Dizin((3, "x"), (1, "-"), (2, "-")))
    m.tersİşle(_ * 2).dizine should be(Dizin(4, 2, 6))
    // eşlik nesnesi
    MiskinDizin.sıraylaDoldur(3)(_ * 2).dizine should be(Dizin(0, 2, 4))
    MiskinDizin.aralık(1, 4).dizine should be(Dizin(1, 2, 3)); MiskinDizin.aralık(1, 10, 4).dizine should be(Dizin(1, 5, 9))
    MiskinDizin.türet(1)(n => if (n > 8) Hiçbiri else Biri((n, n * 2))).dizine should be(Dizin(1, 2, 4, 8))
    MiskinDizin.diziden(Dizin(1, 2)).dizine should be(Dizin(1, 2))
    MiskinDizin.ekle(Dizi(1), Dizin(2)).dizine should be(Dizin(1, 2))
    // Eşlem.katla (eski adı kalta yazım hatasıydı)
    Eşlem("a" -> 1, "b" -> 2).katla(("", 0))((x, y) => ("", x._2 + y._2))._2 should be(3)
  }

  test("Queue and PriorityQueue translations to work") {
    val s = ÖncelikSırası(3, 5, 1, 2, 9)
    val s2 = s.ikizle()
    s.baştanAl should be(9)
    s.baştanAl should be(5)
    s.baştanAlHepsini should be(Dizi(3, 2, 1))
    s.boyu should be(0)
    s2.boyu should be(5)
    s2.ekle(10); s2.başı should be(10); s2.boyu should be(6)
    s2.ekle(20, -10, 30); s2.başı should be(30); s2.boyu should be(9)
    s2.sil(); s2.boşMu should be(doğru)

    val k = Kuyruk(1, 5, 3, 4, 7)
    val k2 = k.ikizle()
    k should be(k2)
    k.baştanAl should be(1)
    k.baştanAlHepsini(x => x > 4) should be(Dizi(5, 7))
    k.baştanAl should be(3)
    k.boyu should be(1)
    k.sil(); k.boşMu should be(doğru)
    k.ekle(11); k.başı should be(11); k.boyu should be(1)
    k.ekleHepsini(Dizi(10, 20, 30)); k.başı should be(11); k.boyu should be(4)
  }

  /* 
  // See: ~/src/kojo/git/kojo/src/test/scala/net/kogics/kojo/turtle/TurtleTest2.scala
  // ~/src/kojo/git/kojo/src/test/scala/net/kogics/kojo/lite/TestEnv.scala
  import net.kogics.kojo.lite.TestEnv
  import scala.language.reflectiveCalls
  val kojoCtx = new NoOpKojoCtx
  val foo = TestEnv(kojoCtx)

  // ~/src/kojo/git/kojo/src/main/scala/net/kogics/kojo/lite/CodeExecutionSupport.scala
  import net.kogics.kojo.lite.AppMode
  val codeRunner = AppMode.currentMode.scalaCodeRunner(kojoCtx)
  import net.kogics.kojo.lite.{CoreBuiltins, Builtins}
  val builtins = new Builtins(
    foo.TSCanvas,
    foo.Tw,
    foo.Staging,
    foo.storyTeller,
    foo.mp3player,
    foo.fuguePlayer,
    kojoCtx,
    codeRunner
  )
  
  TurkishInit.init(builtins)

  test("Translation of Turtle commands should work") {
    val k1 = yeniKaplumbağa(30, 40)
    k1.konum.x should be (30)
    k1.konum.y should be (40)
    k1.ileri()
    k1.konum.x should be (30)
    k1.konum.y should be (140)
    yinele(4) {
      k1.ileri()
      k1.sağ()
    }
    k1.konum.x should be (30)
    k1.konum.y should be (140)
    k1.doğrultu should be (90)
  }

  test("Picture from a Turtle drawing in Turkish") {
    val r = Resim { k: Kaplumbağa0 =>
      import k._
      yinele(4) {
        ileri()
        sağ()
      }
    }
    r.çiz()
    r.alan should be (10000)
  }

  test("Translations of Vector2D should work") {
    val y1 = Yöney2B(3, 4)
    y1.boyu should be(5)
  }

  test("Translations of Picture should work") {
    val r = götür(30, 40) * kalemRengi(mavi) -> Resim.dikdörtgen(100, 200)
    r.çizili should be(yanlış)
    r.alan should be(20000.0)
    r.konum.x should be(30)
    r.konum.y should be(40)
  }

   */

  test("Translations of keywords should work") {
    dez x = 1
    x should be(1)
    den y = 2
    y *= 2
    y should be(4)
    dez deneme: İkil = eğer (y < x) { yanlış } yoksa { doğru }
    deneme should be(doğru)
    tanım t1(söz: Yazı): Sayı = söz eşle {
      durum "merhaba" => 1
      durum "dünya" => 2
      durum _ => 3
    }
    t1("merhaba") should be(1)
    t1("dünya") should be(2)
    t1("foo bar") should be(3)
  }

  test("Translation of hashCode overriding to work. V1 in English") {
    case class Foo(a: Int)
    val x = Foo(1)
    val y = Foo(1)
    x == y should be(true)
    x != y should be(false)
    var counter = 1
    case class Bar(a: Int) extends Eşsizlik {
      val no = counter
      counter += 1
      def kıymaKodu = no.kıymaKodu
      println(kıymaKodu)
    }
    val p = Bar(1)
    val q = Bar(1)
    p == q should be(false)
    p != q should be(true)
  }

  test("Translation of hashCode overriding to work. V2 in Turkish") {
    durum sınıf Foo(a: Sayı)
    dez x = Foo(1); dez y = Foo(1)
    (x == y && !(x != y)) should be(true)

    den sayaç = 1
    durum sınıf Bar(a: Sayı) yayar Eşsizlik {
      dez no = sayaç; sayaç += 1
      tanım kıymaKodu = no.kıymaKodu
      satıryaz(kıymaKodu)
    }
    dez p = Bar(1); dez q = Bar(1)
    (p != q && !(p == q)) should be(true)
  }

  test("Translation of toString overriding to work. V1 in English") {
    case class Foo(a: Int, b: Double) extends BaskınYazıyaYöntemiyle {
      override def yazıya = s"Bar($a,$b)"
    }
    val f = Foo(1, 2.3)
    f.toString should be("Bar(1,2.3)")
  }

  test("Translation of toString overriding to work. V2 in Turkish") {
    durum sınıf Falan(a: Sayı, b: Kesir) yayar BaskınYazıyaYöntemiyle {
      baskın tanım yazıya = s"Filan($a,$b)"
    }
    dez f = Falan(1, 2.3)
    f.toString should be("Filan(1,2.3)")
    f.yazıya should be("Filan(1,2.3)")
  }

  test("Translation of URL to work") {
    dez yazı = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Flower_poster_2.jpg/330px-Flower_poster_2.jpg"
    dez bkk = BKK(yazı)
    bkk.toExternalForm should be(yazı)
  }
}
