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
