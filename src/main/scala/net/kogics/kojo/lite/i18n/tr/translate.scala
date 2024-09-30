/*
 * Copyright (C) 2024
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

object translate {
  def typeInfo(str: String) = str
    .replace("net.kogics.kojo.lite.i18n.tr.", "")
    .replace("net.kogics.kojo.lite.i18n.TurkishAPI.", "")
    .replace("UserCode.this.TurkishAPI.", "")
    .replace("UserCode", "KullanıcınınYazılımı")
    .replace("net.kogics.kojo.core.Point", "Nokta")
    .replace("Wrapper", "Sarıcı")
    .replace("type", "tür")
    .replace("this", "bu")
    .replace("Null", "Yok")
    .replace("Array", "Dizik")
    .replace("List", "Dizin")
    .replace("IndexedSeq", "DiziSıralı")
    .replace("Seq", "Dizi")
    .replace("Char", "Harf")
    .replace("String", "Yazı")
    .replace("Int", "Sayı")
    .replace("Double", "Kesir")
    .replace("Boolean", "İkil")
    .replace("Unit", "Birim")
    .replace("Vector", "Yöney")
    .replace("java.awt.Color", "Renk")
    .replace("true", "doğru")
    .replace("false", "yanlış")
    .replace("implicit ", "örtük ")
/* todo: add more basic types */

  def result(str: String) = str
    .replace("expected class or object definition", "gereken sınıf ya da nesne tanımı bulunamadı")
    .replace("val res", "dez sonuç")
    .replace("net.kogics.kojo.lite.i18n.tr.", "")
    .replace("<not computed>", "<hesaplanmadı>")
    .replace("scala.collection.mutable.", "")
    .replace("scala.collection.immutable.", "")
    .replace("TurkishAPI.", "")
    .replace("net.kogics.kojo.core.Point", "Nokta")
    .replace("val ", "dez ")
/*  .replace("var ", "den ")  var is too common in turkish */
    .replace("def ", "tanım ")
    .replace("class ", "sınıf ")
    .replace("case ", "durum ")
    .replace("ArrayBuffer", "EsnekDizim")
    .replace("Array", "Dizik")
    .replace("IndexedSeq", "DiziSıralı")
    .replace("Seq", "Dizi")
    .replace("Map", "Eşlek")
    .replace("Vector", "Yöney")
    .replace("LazyList", "MiskinDizin")
    .replace("List", "Dizin")
    .replace("Char", "Harf")
    .replace("String", "Yazı")
    .replace("Int", "Sayı")
    .replace("Double", "Kesir")
    .replace("Boolean", "İkil")
    .replace("Unit", "Birim")
    .replace("Point", "Nokta")
    .replace("true", "doğru")
    .replace("false", "yanlış")
    .replace("mutated ", "değişti ")
    .replace("null", "yok")
    .replace("java.lang.RuntimeException", "ÇalışmaSırasıKuralDışı")
    .replace("java.lang.Exception", "KuralDışı")
    .replace("java.lang.AssertionError", "BelirtimHatası")
    .replace("java.lang.IllegalArgumentException", "KuraldışıGirdiHatası")
    .replace("java.lang.IndexOutOfBoundsException", "SınırDışınaTaşmaHatası")
    .replace("scala.NotImplementedError", "EksikTanımHatası")
    .replace("java.lang.NullPointerException", "BoşGöstergeHatası")
    .replace("java.lang.ArithmeticException", "MatematikselHata")
    .replace("is out of bounds", "sınırlar dışında")
    .replace("assertion failed", "belirtilen koşul sağlanmadı")
    .replace("requirement failed", "gerek koşul sağlanmadı")
    .replace("an implementation is missing", "bir tanım eksik")
    .replace("/ by zero", "Sıfıra bölüm")
    .replace("Unable to format", "Kod düzenlenemedi")
    .replace("max", "enİri")
    .replace("min", "enUfak")
}
