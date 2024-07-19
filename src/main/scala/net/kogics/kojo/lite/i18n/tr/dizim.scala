/*
 * Copyright (C) 2021
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *   Lalit Pant <pant.lalit@gmail.com>
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

import scala.reflect.ClassTag

import collection.mutable.ArrayBuffer

// todo: this has only the bare essentials for Array and ArrayBuffer. Add more to the interface..
object EsnekDizim {
  def apply[T](elemanlar: T*) = new EsnekDizim[T](ArrayBuffer.from(elemanlar))
  def boş[T] = new EsnekDizim[T](ArrayBuffer.empty[T])
}
class EsnekDizim[T](val a: ArrayBuffer[T]) {
  type Col = EsnekDizim[T]
  type C2[A] = EsnekDizim[A]
  def apply(yer: Sayı) = a(yer)
  def sayı = a.size
  def ekle(eleman: T) = { a.append(eleman); this }
  def +=(eleman: T) = ekle(eleman)
  def çıkar(yer: Sayı) = a.remove(yer)
  def sil() = a.clear()
  def dizi = a.toSeq
  def diziye = a.toSeq
  def boşMu: İkil = a.isEmpty
  def doluMu: İkil = a.nonEmpty

  def ele(deneme: T => İkil): Col = new EsnekDizim(a.filter(deneme))
  def eleYerinde(deneme: T => İkil): this.type = { a.filterInPlace(deneme); this }

  def işle[B](işlev: T => B): C2[B] = new EsnekDizim(a.map(işlev))
  def herbiriİçin[B](işlev: T => B): Birim = a.foreach(işlev)
  
  // todo: more to come
}

object Dizim {
  def boş[T: ClassTag](b1: Sayı) = new Dizim(Array.ofDim[T](b1))
  def boş[T: ClassTag](b1: Sayı, b2: Sayı) = new Dizim(Array.ofDim[T](b1, b2))
  def boş[T: ClassTag](b1: Sayı, b2: Sayı, b3: Sayı) = new Dizim(Array.ofDim[T](b1, b2, b3))

  def doldur[T: ClassTag](b1: Sayı)(e: => T) = new Dizim(Array.fill[T](b1)(e))
  def doldur[T: ClassTag](b1: Sayı, b2: Sayı)(e: => T) = new Dizim(Array.fill[T](b1, b2)(e))
  def doldur[T: ClassTag](b1: Sayı, b2: Sayı, b3: Sayı)(e: => T) = new Dizim(Array.fill[T](b1, b2, b3)(e))
}
class Dizim[T](val a: Array[T]) {
  def diziye = a.toSeq
  def yazıya = toString
  override def toString = String.valueOf(a)
  def apply(b1: Sayı) = a(b1)
  @annotation.nowarn
  def boyut: Sayı = { // just an exercise -- not really needed
    var b = 1
    var p = a // scala style pointer
    var recurse = true
    while (recurse) p(0) match {
      case x: Array[T] => b += 1; p = x
      case _           => recurse = false
    }
    b
  }
}

trait ArrayMethodsInTurkish {
  type Dizik[T] = Array[T]
  object Dizik {
    def apply[T](elems: T*)(implicit arg: ClassTag[T]) = Array(elems: _*)
    def boş[T](implicit arg: ClassTag[T]) = Array.empty
  }
  // todo: copied from dizi.scala
  implicit class ArrayMethods[T](d: Dizik[T]) {
    type Col = Dizik[T]
    type Eşlek[A, D] = collection.immutable.Map[A, D]
    def başı: T = d.head
    def kuyruğu: Col = d.tail
    def önü: Col = d.init
    def sonu: T = d.last
    def boyu: Sayı = d.length
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    // https://www.scala-lang.org/api/2.13.x/scala/Array.html
    // map[B](f: (T) => B)(implicit ct: ClassTag[B]): Dizik[B]
    // Builds a new array by applying a function to all elements of this array.
    def işle[A](işlev: T => A)(implicit ct: ClassTag[A]): Dizik[A] = d.map(işlev)(ct)
    def işleYerinde(işlev: (T) => T): Dizik[T] = d.mapInPlace(işlev)
    def düzİşle[A: ClassTag](işlev: T => Dizik[A]): Dizik[A] = d.flatMap(işlev)
    def sıralı(implicit ord: Ordering[T]): Col = d.sorted(ord)
    def sırala[A](i: T => A)(implicit ord: Ordering[A]): Col = d.sortBy(i)
    def sırayaSok(önce: (T, T) => İkil): Col = d.sortWith(önce)
    def indirge[B >: T](işlem: (B, B) => B): B = d.reduce(işlem)
    def soldanKatla[T2](z: T2)(işlev: (T2, T) => T2): T2 = d.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (T, T2) => T2): T2 = d.foldRight(z)(işlev)
    // https://github.com/scala/scala/blob/v2.12.7/src/library/scala/collection/TraversableOnce.scala#L1
    def topla[T2 >: T](implicit num: scala.math.Numeric[T2]) = d.sum(num) // foldLeft(num.zero)(num.plus)
    def çarp[T2 >: T](implicit num: scala.math.Numeric[T2]) = d.product(num) // foldLeft(num.one)(num.times)
    def yinelemesiz = d.distinct
    def yinelemesizİşlevle[T2](işlev: T => T2): Col = d.distinctBy(işlev)
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
    def tersi = d.reverse
    def değiştir[S >: T: ClassTag](yeri: Sayı, değeri: S): Dizik[S] = d.updated(yeri, değeri)
    def değiştirYerinde(yeri: Sayı, değeri: T): Birim = d.update(yeri, değeri)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    // def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def içeriyorMu(öge: T): İkil = d.contains(öge)
    def içeriyorMuDilim(dilim: Col): İkil = d.containsSlice(dilim)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    // def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    // def sırası[S >: T](öge: S, başlamaNoktası: Sayı): Sayı = d.indexOf(öge, başlamaNoktası)
    // def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)
    // def sırasıSondan[S >: T](öge: S, sonNokta: Sayı): Sayı = d.lastIndexOf(öge, sonNokta)
    def sırası(öge: T): Sayı = d.indexOf(öge)
    def sırası(öge: T, başlamaNoktası: Sayı): Sayı = d.indexOf(öge, başlamaNoktası)
    def sırasıSondan(öge: T): Sayı = d.lastIndexOf(öge)
    def sırasıSondan(öge: T, sonNokta: Sayı): Sayı = d.lastIndexOf(öge, sonNokta)

    def dizine = d.toList
    def diziye = d.toSeq
    def kümeye = d.toSet
    def yöneye = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def eşleme[A, D](implicit delil: T <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(d.toMap)
    def say(işlev: T => İkil): Sayı = d.count(işlev)

    def dilim(nereden: Sayı, nereye: Sayı) = d.slice(nereden, nereye)
    def ikile[S](öbürü: scala.collection.IterableOnce[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: (T) => A): Eşlek[A, Col] = d.groupBy(iş)

    def enUfağı[B >: T](implicit sıralama: math.Ordering[B]): T = d.min(sıralama)
    def enUfağı[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): T = d.minBy(iş)(karşılaştırma)
    def enİrisi[B >: T](implicit sıralama: math.Ordering[B]): T = d.max(sıralama)
    def enİrisi[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): T = d.maxBy(iş)(karşılaştırma)
    // todo: more to come
  }

}
