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
// These tests are written in Turkish keywords (dez/den/eğer/yoksa/tanım/eşle/durum,
// durum sınıf, yayar, baskın tanım), so they only compile with the Turkish-keyword
// patched compiler in scala-tr/. They stay in this fork; the rest of the Turkish API
// coverage lives in TurkishAPITest, which is kept identical to the upstream copy.
@annotation.nowarn class TurkishKeywordTest extends FunSuite with Matchers {

  import TurkishAPI._

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

  test("Translation of toString overriding to work. V2 in Turkish") {
    durum sınıf Falan(a: Sayı, b: Kesir) yayar BaskınYazıyaYöntemiyle {
      baskın tanım yazıya = s"Filan($a,$b)"
    }
    dez f = Falan(1, 2.3)
    f.toString should be("Filan(1,2.3)")
    f.yazıya should be("Filan(1,2.3)")
  }
}
