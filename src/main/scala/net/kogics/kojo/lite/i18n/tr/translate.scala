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
  private def common(str: String) = str
    .replace("net.kogics.kojo.lite.i18n.tr.", "")
    .replace("UserCode.this.TurkishAPI.", "")
    .replace("UserCode", "KullanıcınınYazılımı")
    .replace("net.kogics.kojo.core.Point", "Nokta")
    .replace("net.kogics.kojo.doodle.Color", "Renk")
    .replace("java.awt.Color", "Renk")
    .replace("ArrayCharSequence", "HarfDizisindenDizik")
    .replace("SeqCharSequence", "HarfDizisindenDizi")
    .replace("Wrapper", "Sarıcı")
    .replace("type", "tür")
    .replace("this", "bu")
    .replace("Object", "Nesne")
    .replace("Unit", "Birim")
    .replace("Nothing", "Hiç")
    .replace("Null", "Yok")
    .replace("AnyRef", "HerGönder")
    .replace("AnyVal", "HerDeğer")
    .replace("Any", "Her")
    .replace("ArraySeq", "DizikDizisi")
    .replace("ArrayBuffer", "EsnekDizim")
    .replace("Array", "Dizik")
    .replace("LazyList", "MiskinDizin")
    .replace("List", "Dizin")
    .replace("IndexedSeq", "SıralıDizi")
    .replace("Seq", "Dizi")
    .replace("Character", "JHarf")
    .replace("Char", "Harf")
    .replace("toString", "yazıya")
    .replace("String", "Yazı")
    .replace("Boolean", "İkil")
    .replace("Byte", "Lokma")
    .replace("Short", "Kısa")
    .replace("Int", "Sayı")
    .replace("Long", "Uzun")
    .replace("BigInt", "İriSayı")
    .replace("Float", "UfakKesir")
    .replace("Double", "Kesir")
    .replace("BigDecimal", "İriKesir")
    .replace("Integer", "JSayı")
    .replace("Number", "JTemelSayı")
    .replace("Map", "Eşlek")
    .replace("Vector", "Yöney")
    .replace("Point", "Nokta")
    .replace("true", "doğru")
    .replace("false", "yanlış")
    .replace("implicit ", "örtük ")
  /* todo: add more basic types */

  def typeInfo(str: String) = common(str
    .replace("net.kogics.kojo.lite.i18n.TurkishAPI.", "")
    // Code Completion on Matematik e.g. has this as type:
    .replace("net.kogics.kojo.lite.i18n.TurkishAPI", "")
  )

  def result(str: String) = common(str)
    .replace("expected class or object definition", "gereken sınıf ya da nesne tanımı bulunamadı")
    .replace("val res", "dez sonuç")
    .replace("<not computed>", "<hesaplanmadı>")
    .replace("scala.collection.mutable.", "")
    .replace("scala.collection.immutable.", "")
    .replace("TurkishAPI.", "")
    .replace("Unable to stop script.", "Programın çalışmasını güzelce durduramadık.")
    .replace("Doing a forced-stop. It's best to just restart Kojo!", "Zorlayarak durduruyoruz. Kojo'yu kapatıp açmak iyi olur!")
    .replace("val ", "dez ")
/*  .replace("var ", "den ")  var is too common in turkish */
    .replace("def ", "tanım ")
    .replace("class ", "sınıf ")
    .replace("case ", "durum ")
    .replace("true", "doğru")
    .replace("false", "yanlış")
    .replace("mutated ", "değişti ")
    .replace("null", "yok")
    .replace("java.lang.ThreadDeath", "İşParçacığıÖlümü")
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
    .replace("head of empty", "başı istenen boş bir")
  /* Error[13,13]: object creation impossible.
   Missing implementation for member of trait Seçenek:
   def yazıya: UserCode.this.TurkishAPI.Yazı = ???
   */
    .replace("Error", "Hata")
    .replace("object creation impossible", "nesne oluşturulamadı")
    .replace("Missing implementation for member of trait",
      "Temel tür olan özellik yöntemlerinden birinin tanımı eksik. Özelliğin adı")
}
