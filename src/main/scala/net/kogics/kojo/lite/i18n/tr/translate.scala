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
  private def common(str: String) = { str
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
    .replace("java.math.BigInteger", "JİriSayı")
    .replace("scala.math.BigInt", "İriSayı")
    .replace("math.BigInt", "İriSayı")
    .replace("BigInt", "İriSayı")
    .replace("Int", "Sayı")
    .replace("Long", "Uzun")
    .replace("Float", "UfakKesir")
    .replace("Double", "Kesir")
    .replace("scala.math.BigDecimal", "İriKesir")
    .replace("math.BigDecimal", "İriKesir")
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
  }

  def typeInfo(str: String) = { common(str
    .replace("net.kogics.kojo.lite.i18n.TurkishAPI.", "")
    // Code Completion on Matematik e.g. has this as type:
    .replace("net.kogics.kojo.lite.i18n.TurkishAPI", "")
  )
  }

  /*
   java.lang.IllegalArgumentException: More than Int.MaxValue elements.
   at scala.collection.immutable.NumericRange$.check$1(NumericRange.scala:431)
   at scala.collection.immutable.NumericRange$.count(NumericRange.scala:441)
   */
  def beforeCommon(str: String) = { str
    .replace("More than Int.MaxValue elements.", "Sayı.Enİrisi değerinden çok ögesi var.")
    .replace("java.lang.NullPointerException", "BoşGöstergeHatası")
    // https://stackoverflow.com/questions/68608157/how-can-i-fix-this-warning-the-fonts-times-and-times-are-not-available-fo
    .replace("""Warning: the fonts "Times" and "Times" are not available for the Java logical font "Serif", which may have unexpected appearance or behavior. Re-enable the "Times" font to remove this warning.""", """Uyarı: "Times" arayüzü "Serif" özelliğini desteklemiyor. Onun için bazı görünüşler bozuk olabilir. Bilgisayarınızda "Times" arayüzünü tekrar kurarak bu uyarıyı temizleyebilirsiniz. Daha detaylı bilgi için, bakınız: https://stackoverflow.com/questions/68608157/how-can-i-fix-this-warning-the-fonts-times-and-times-are-not-available-fo""")
    // macos'te: /Users/ben/.kojo/lite/libk
    /*
     Scanning libk...
     Additional jars available (within libk) - 25

     Scanning extensions...
     Additional jars available (within /Users/ben/.kojo/extension/kojo-music) - 4
     */
    // todo: bbx to localization level 4?
    // or simply do it in ../../../xscala/ScalaCodeRunner2.scala using level 1:
    //   ~/kojo-repo/src/main/resources/net/kogics/kojo/lite/Bundle_tr.properties
    .replace("Scanning libk...", s"Kütüp taraması yapılıyor (sabit diskte ${Yardımcı.kütüpDizini} dizininde)...")
    .replace("Scanning extensions...", s"Eklenti taraması yapılıyor (sabit diskte ${Yardımcı.eklentiDizini} dizininde)...")
    .replace("Additional jars available ", "Ek jar dosyaları bulundu") // clears a trailing space
    .replace("(within libk) - ", ". Sayısı: ") // adds a leading dot
    .replace("(within ", " (dizin: ") // adds a leading space. todo: only if match, concat to the end: " tane."
  }

  def regexpChanges(str: String) = {
    // bbx todo
    str
  }

  def result(str: String) = { common(beforeCommon(regexpChanges(str)))
    .replace("expected class or object definition", "gereken sınıf ya da nesne tanımı bulunamadı")
    .replace("val res", "dez sonuç")
    .replace("<not computed>", "<hesaplanmadı>")
    .replace("scala.collection.mutable.", "")
    .replace("scala.collection.immutable.", "")
    .replace("TurkishAPI.", "")
    .replace("Unable to stop script.", "Programın çalışmasını güzelce durduramadık.")
    .replace("Doing a forced-stop. It's best to just restart Kojo!", "Zorlayarak durduruyoruz. Kojo'yu kapatıp açmak iyi olur!")
    .replace("val ", "dez ")
    /*  .replace("var ", "den ")  var is a very common word in turkish */
    .replace("def ", "tanım ") // bbx todo: use regexp! hedef !=> hetanım
    .replace("hetanım", "hedef").replace("setanım", "sedef") // (:-)
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
    /* Error: object creation impossible.
     Missing implementation for member of trait Seçenek:
     def yazıya: UserCode.this.TurkishAPI.Yazı = ???
     */
    .replace("Error", "Hata")
    .replace("object creation impossible", "nesne oluşturulamadı")
    .replace("Missing implementation for member of trait",
      "Temel tür olan özellik yöntemlerinden birinin tanımı eksik. Özelliğin adı")
    //
    .replace("Warning", "Uyarı")
    /*
     Hata[7,91]: overloaded method apply with alternatives:
     (x: java.math.BigSayıeger)scala.math.BigSayı <and>
     (x: Yazı)scala.math.BigSayı <and>
     (x: Dizik[Lokma])scala.math.BigSayı <and>
     (l: Uzun)scala.math.BigSayı <and>
     (i: Sayı)scala.math.BigSayı
     cannot be applied to (scala.math.BigSayı)
     def foo(ds: List[BigInt]): S = ds.reverse.zip(kuvvetler(ds.size)).map{ case (d, p) => S(d*p) }.reduce(_ + _)
     ^
     */
    .replace("overloaded method apply with alternatives", "birkaç tane işlev uygulama () seçeneği var")
    .replace("cannot be applied to", "hiçbiri şu türe uygulanamıyor:")
    /*
     Error[x,y]: unclosed string literal
     Error[Z,W]: Missing closing brace <> assumed here
     */
    .replace("unclosed string literal", "çift tırnak işaretinin teki eksik")
    .replace("Missing closing brace", "kapatan kıvrık parantez")
    .replace("assumed here", "burada olduğu varsayıldı")
    // Hata oldu - ÇalışmaSırasıKuralDışı: You can't redraw a picture (kütük dosyasında daha çok bilgi var)
    .replace("You can't redraw a picture", "Resim bir kere çizildikten sonra yine çizilemez.")
    // Problem: java.lang.IllegalStateException: Cannot access a Picture's geometry before it is drawn.
    .replace("Problem: java.lang.IllegalStateException: Cannot access a Picture's geometry before it is drawn.",
      "Hata: KuralDışıDurumHatası: Resim çizilmeden önce kullanıldı.")
    /* Hata[28,13]: not enough arguments for method apply: (v1: Resim): İkil in trait Function1.
     Unspecified value parameter v1.
     top.çarptıMı()
     */
    .replace("not enough arguments for method apply", "İşlev uygulama yöntemi için yeterince girdi yok")
    .replace("in trait Function", "şu özellik için: İşlev")
    /*  Hata[3,6]: not enough arguments for method fonk1: (g1: Sayı): Sayı.
     *     Unspecified value parameter g1.
     *     fonk1()
     *          ^
     */
    .replace("not enough arguments for method ", "yeterince girdi yok. Yöntem ")
    .replace("Unspecified value parameter ", "eksik değer girdisi ")

    /* Hata[28,21]: too many arguments (found 2, expected 1) for method apply: (v1: Resim): İkil in trait Function1
     top.çarptıMı(engel, foo)
     *                   ^
     Hata[3,12]: too many arguments (found 2, expected 1) for method fonk1: (g1: Sayı): Sayı
     fonk1(100, 200)
     *          ^
     */
    .replace("too many arguments (found", "fazla girdi verildi (bulunan")
    //
      .replace(", expected ", ", beklenen ")
      .replace(" for method apply: ", " işlev uygulama yöntemi için: ")
      .replace(" for method ", " şu yöntem için: ")

    // Hata[31,5]: not found: value doğrudanÇağır
    .replace("not found: value", "değer bulunamadı:")
  }

}
