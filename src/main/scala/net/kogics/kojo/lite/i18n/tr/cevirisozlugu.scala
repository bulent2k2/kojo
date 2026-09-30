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

/**
 * Çeviri sözlüğü: Türkçe (Koco) ad <-> İngilizce (Kojo) ad.
 *
 * İKİ DOSYADAN OLUŞUR, ikisi de kaynak dizininde (src/main/resources/i18n/tr/):
 *
 *   ceviri-sozlugu.tsv   ÜRETİLMİŞ. Elle düzenlenmez. SözlükÜreteci, Türkçe
 *                        sarmalayıcı kaynaklarını (trInit.scala + tr dizinindeki .scala dosyaları)
 *                        okuyup `def ileri(adım) = englishTurtle.forward(adım)`
 *                        gibi tek satırlık yönlendirmelerden çiftleri çıkarır.
 *                        Sarmalayıcıların KENDİSİ sözlüktür; ikinci bir kopya
 *                        tutmuyoruz. Tazeliğini CevirmenTest sınıyor.
 *
 *   ceviri-kurallar.tsv  ELLE. Üretilmiş sözlüğün tek başına çözemediği yerler:
 *                        bir Türkçe adın alıcıya göre başka İngilizce ada gitmesi
 *                        (`sil()` clear, `resim.sil()` erase), ters yönde hangi
 *                        Türkçe yazımın kanonik olduğu, sarmalayıcı zinciri
 *                        olmayan nesne adları (Resim <-> Picture), ve
 *                        "çevirme" istisnaları.
 *
 * NEDEN AYRI: üretilmiş dosya mekanik ve büyük; kural dosyası küçük ve her
 * satırı bilinçli bir karar. İkisini karıştırınca hangi satırın neden orada
 * olduğu kaybolur (bkz. kojojs-dev/araclar/adlar.py'nin aynı gerekçesi).
 *
 * TSV biçimi (sekmeyle ayrılmış, `#` ile başlayan satırlar yorum):
 *   sözlük:   cins  tr  en  kaynak          cins: def|val|var|type|class|object
 *   kurallar: yön  ad  bağlam  hedef  not   yön: tr>en|en>tr, bağlam: yalın|üye|*,
 *                                          hedef "-" ise "bu adı çevirme"
 */
object ÇeviriSözlüğü {
  /** `not` = "üye": İngilizce ad Kojo prelude'ünde yalın çözülmüyor (x.length gibi bir üye ya da
    * iç ad); yalnız üye bağlamında kullanılır. Üreteç derleyiciye sorarak işaretler. */
  /** Sözlüğün bir satırı. `kaynak` DOSYA adıdır, satır numarası taşımaz: numara taşıyınca
    * sarmalayıcı dosyalarında ilgisiz bir satır kayması tazelik sınamasını kırmızıya düşürüyordu
    * (ölçüldü: #60, sonra #58/#61 master'a girince; iki kez de tek değişiklik satır numarasıydı).
    * `sayı` o dosyada aynı çifti veren tanım sayısıdır: sıklık tablosu KAÇ tanımın bu hedefe
    * gittiğine bakar, dosya düzeyine inerken o bilgi kaybolmasın diye sütuna yazılır. */
  final case class Satır(cins: String, tr: String, en: String, kaynak: String, sayı: Int = 1, not: String = "") {
    /** `not` bir İM KÜMESİ: virgülle ayrılmış sıfır ya da daha çok im ("üye", "eskitilmiş",
      * "üye,eskitilmiş"). Tek değerli olduğu varsayımı #63'te kırıldı: eskitilmiş bir ad
      * aynı zamanda yalnız-üye olabiliyor. Sütun eklemek yerine imi kümeye çevirmek
      * TSV'nin şemasını (`cins tr en kaynak sayı [not]`) olduğu gibi bırakıyor. */
    def notlar: Set[String] = if (not.isEmpty) Set.empty else not.split(',').iterator.map(_.trim).filter(_.nonEmpty).toSet
    def notluMu(im: String): Boolean = notlar(im)
  }
  val YalnızÜye = "üye"
  /** Tanımın başında `@deprecated` var. EN->TR'de eskitilmiş bir Türkçe ad, eşit oylu
    * eskitilmemiş kardeşine YENİLİR (bkz. adaylar). TR->EN'de hiçbir şey değişmez:
    * `sil_geri` yazan eski bir Koco betiği yine çevrilebilmeli -- yani bu bir SIRALAMA
    * ölçütü, süzgeç değil (#63). */
  val Eskitilmiş = "eskitilmiş"
  /** Üreteç içi geçici not: tanımın gövdesi `{ ... }` bloğu, hedef ilk deyimin zinciri.
    * Zincir çözümünde Türkçe sarmalayıcı adları üstünden ilerlemez (bkz. zincirleriÇöz). */
  val Gövdeli = "gövdeli"
  /** Kuralın `not` sütununda geçerse: İngilizce ad betik düzeyinde parantez ALMAZ (TurtleWorldAPI'nin
    * `def saveStyle = ...` ileticisi) ama Türkçe sarmalayıcı `()` ile çağrılır; TR->EN'de `()`
    * yutulur. Üreteç bunu SAPTAYAMAZ -- sarmalayıcı `englishTurtle.saveStyle()` diye parantezli
    * çağırıyor (Turtle'da parantezli); betik düzeyindeki ad ayrı bir tanım. Kural işi. */
  val Parantezsiz = "parantezsiz"
  final case class Kural(yön: String, ad: String, bağlam: String, hedef: String, not: String)

  /** Bir ad için seçilen hedef. `alternatifler` boş değilse seçim belirsizdi: en sık
    * görülen alındı, ötekiler raporlanır. */
  final case class Seçim(hedef: String, alternatifler: Seq[String], parantezsiz: Boolean = false) {
    def kesin: Boolean = alternatifler.isEmpty
  }

  val sözlükYolu = "/i18n/tr/ceviri-sozlugu.tsv"
  val kurallarYolu = "/i18n/tr/ceviri-kurallar.tsv"

  val BağlamYalın = "yalın" // önünde `.` yok:  sil()
  val BağlamÜye = "üye"     // önünde `.` var:  resim.sil()
  val BağlamHepsi = "*"
  val Çevirme = "-"
  /** Not "takma": satır bir TAKMA AD alıcısı için (`Çizim.x`, Çizim = Görünüş). Yalnız TR->EN
    * alıcılı aramaya girer; EN->TR adayı olmaz, yoksa asıl adla eşit oyla "belirsiz" çıkıyor. */
  val TakmaAd = "takma"
  /** Hedef bu imle başlıyorsa alıcı ve nokta da yutulur: `Resim.dizi` -> `picStack`.
    * Böyle bir kuralın bağlamı alıcının adıdır, sonunda nokta: `Resim.` */
  val AlıcıylaBirlikte = "^"
  /** Bağlam "alıcı": ad bir üyenin alıcısı (`Color.khaki` içindeki Color). Renkler
    * Türkçe'de `Renkler` nesnesinde, tür adı ise `Renk`: aynı İngilizce ad iki yere gider. */
  val BağlamAlıcı = "alıcı"
  /** Bağlam "alıcı(": çağrılan bir üyenin alıcısı (`ColorMaker.hsla(...)` içindeki ColorMaker).
    * Kural yoksa "alıcı"ya, o da yoksa "*"a düşer. */
  val BağlamAlıcıÇağrı = "alıcı("
  /**
   * Bağlam "üye(": ARGÜMAN LİSTESİYLE çağrılan üye (`r.saydamlık(0.5)`), argümansız
   * kullanımdan (`r.saydamlık`) ayrı. Kural yoksa "üye"ye, o da yoksa "*"a düşer.
   *
   * NEDEN VAR: bir Türkçe ad, arity'si farklı iki İngilizce yönteme gidebiliyor ve
   * ad düzeyinde kural bunu ayırt edemiyordu. Ölçülen örnek `saydamlık`
   * (resim.scala:261 argümanlı -> opacityMod, :308 argümansız -> opacity): üç aday da
   * n=1, karar alfabetiğe kalıyor ve `opac` kazanıyordu -- Resim'in üyesi olmayan,
   * yalın yazılımcık komutu için üretilmiş satır. Tek bir `üye` kuralı yazmak yarısını
   * bozuyordu, ölçüldü (sorun #75):
   *   üye opacityMod -> `r.saydamlık` kırık ("missing argument list")
   *   üye opacity    -> `r.saydamlık(0.5)` kırık ("Double does not take parameters")
   */
  val BağlamÜyeÇağrı = "üye("

  private def alanlar(tsv: String): Iterator[Array[String]] =
    tsv.linesIterator.filterNot(l => l.trim.isEmpty || l.startsWith("#")).map(_.split('\t'))

  private def sayıyaÇevir(alan: String, satır: Array[String]): Int =
    alan.toIntOption.filter(_ > 0).getOrElse(sys.error(s"ceviri-sozlugu.tsv: 5. sütun (sayı) pozitif tamsayı olmalı: ${satır.mkString("|")}"))

  def satırlarıAyrıştır(tsv: String): Seq[Satır] = alanlar(tsv).map { a => a match {
    case Array(cins, tr, en, kaynak, sayı, not, _*) => Satır(cins, tr, en, kaynak, sayıyaÇevir(sayı, a), not)
    case Array(cins, tr, en, kaynak, sayı)          => Satır(cins, tr, en, kaynak, sayıyaÇevir(sayı, a))
    // Eksik sütun HATA: `sayı` sıklık tartısını taşıyor, sessizce 1 demek satırı gürültüsüzce
    // yanlış ağırlıklandırırdı (inceleme #65). Savunmacı varsayılan, yük taşıyan alanda yanlış.
    case _ => sys.error(s"ceviri-sozlugu.tsv: bozuk satır (beklenen: cins tr en kaynak sayı [not]): ${a.mkString("|")}")
  }}.toVector

  def kurallarıAyrıştır(tsv: String): Seq[Kural] = alanlar(tsv).map {
    case Array(yön, ad, bağlam, hedef, not, _*) => Kural(yön, ad, bağlam, hedef, not)
    case Array(yön, ad, bağlam, hedef)          => Kural(yön, ad, bağlam, hedef, "")
    case a                                      => sys.error(s"ceviri-kurallar.tsv: bozuk satır: ${a.mkString("|")}")
  }.toVector

  def yükle(): Sözlük =
    new Sözlük(satırlarıAyrıştır(kaynağıOku(sözlükYolu)), kurallarıAyrıştır(kaynağıOku(kurallarYolu)))

  // Utils.loadResource'un aynısı (UTF-8), ama Kojo'nun geri kalanına bağlanmadan: çevirmen
  // (Çevirmen + ÇeviriSözlüğü + dict) yalnız scalariform'a dayanan bir kitaplık, iKojo'nun
  // sunucusunda da koşacak (kojojs-dev#183). Bağımlılık gelirse CevirmenKitaplikTest kırılır.
  private def kaynağıOku(yol: String): String = {
    val akış = getClass.getResourceAsStream(yol)
    require(akış != null, s"kaynak yok: $yol")
    // Source, InputStream.readAllBytes DEĞİL: o Java 9'da geldi, Kojo Java 8'i de hedefliyor
    try scala.io.Source.fromInputStream(akış, "UTF-8").mkString
    finally akış.close()
  }

  class Sözlük(val satırlar: Seq[Satır], val kurallar: Seq[Kural]) {
    // Adaylar sıklığa göre sıralı: bir Türkçe ad 22 yerde `min`e, 1 yerde
    // `MaxValue`a gidiyorsa `min` başa gelir. Eşitlikte SARMALAYICI satırı data.scala tablosunu
    // yener -- tablo elle ve yer yer eskimiş (çokHızlı=SuperFast, kosinüs=cos yazıyor; sarmalayıcı
    // superFast ve math.cos diyor; ölçüldü). En sonda ad sırası (kararlılık).
    // Dördüncü alan satırın `sayı`sı: dosya düzeyine inen kaynak sütununda çokluk orada durur,
    // `ileri -> forward` tek satırda 3 tanım demek olabilir. Satır SAYMAK yerine sayı TOPLANIR.
    // Beşinci alan: HEDEF eskitilmiş mi (#63). Sıklıktan SONRA, sarmalayıcıdan ÖNCE bakılıyor:
    // eşit oyda eskitilmemiş kardeş kazansın, ama daha çok kaynağı olan bir hedefi
    // eskitilmişlik yenmesin -- sıklık hâlâ baskın ölçüt.
    // Yalnız EN->TR'de dolu: orada hedef Türkçe addır ve `back_space` gibi bir adı ÜRETMEK
    // istemiyoruz. TR->EN'de hedef İngilizce ve eskitilmişliğini bilmiyoruz; ayrıca eskitilmiş
    // Türkçe adı ÇEVİREBİLMEK gerekiyor, o yön kaynağa bakar, hedefe değil.
    private def adaylar(çiftler: Seq[(String, String, Boolean, Int, Boolean)]): Map[String, Seq[(String, Int)]] =
      çiftler.groupBy(_._1).map { case (ad, ss) =>
        val sayım = ss.groupBy(_._2).map { case (hedef, hs) =>
          (hedef, hs.map(_._4).sum, hs.filter(_._3).map(_._4).sum, hs.exists(_._5)) }
        ad -> sayım.toSeq
          .sortBy { case (hedef, n, sarmalayıcı, eskitilmiş) => (-n, eskitilmiş, -sarmalayıcı, hedef) }
          .map { case (hedef, n, _, _) => (hedef, n) }
      }
    private def sarmalayıcıdan(s: Satır) = !s.kaynak.startsWith("data.scala")

    // "Belirsiz" sayılmak için ikinci adayın kazananın en az YARISI kadar kaynağı olmalı.
    // Ölçüldü: eşik yokken `sağ -> right(3) / RIGHT(1)` her kullanımda raporlanıyor,
    // 90 betikte 1011 satırlık işe yaramaz bir rapor çıkıyordu.
    private def ciddiAlternatifler(as: Seq[(String, Int)]): Seq[String] = as match {
      case (_, kazanan) +: kalan => kalan.collect { case (hedef, n) if n * 2 >= kazanan => hedef }
      case _                     => Nil
    }

    private val trAdaylar = adaylar(satırlar.map(s => (s.tr, s.en, sarmalayıcıdan(s), s.sayı, false)))
    private val enAdaylar = adaylar(satırlar.map(s => (s.en, s.tr, sarmalayıcıdan(s), s.sayı, s.notluMu(Eskitilmiş))))
    // Yalın bağlam için: derleyicinin yalın çözemediği İngilizce hedefler dışarıda.
    private val yalınSatırlar = satırlar.filterNot(_.notluMu(YalnızÜye))
    private val trAdaylarYalın = adaylar(yalınSatırlar.map(s => (s.tr, s.en, sarmalayıcıdan(s), s.sayı, false)))
    private val enAdaylarYalın = adaylar(yalınSatırlar.map(s => (s.en, s.tr, sarmalayıcıdan(s), s.sayı, s.notluMu(Eskitilmiş))))
    // `kuvveti -> math.pow`: ters yönde tek jeton `pow` gelir, alıcısı `math`. Nitelenmiş
    // hedefler alıcı bağlamıyla (`math.`) ayrıca dizinlenir; `math` kendisi `Matematik`e
    // çevrilir, üye buradan `kuvveti` olur. Ölçüldü: yoksa `Matematik.pow` kalıyordu.
    private val enAlıcılıAdaylar = adaylar(satırlar.collect {
      case s if s.en.contains('.') && !s.notluMu(TakmaAd) => (s.en.substring(0, s.en.lastIndexOf('.') + 1) + "\u0000" + s.en.substring(s.en.lastIndexOf('.') + 1), s.tr, sarmalayıcıdan(s), s.sayı, s.notluMu(Eskitilmiş))
    })
    // `Resim.dizi -> picStack`: Türkçe nesnenin üyesi İngilizce'de YALIN. Üreteç böyle satırların
    // tr'sini nitelenmiş yazar; buradan TR->EN alıcılı arama `^picStack` (alıcı yutulur),
    // EN->TR ise enAdaylar üstünden zaten `Resim.dizi` verir.
    private val trAlıcılıAdaylar = adaylar(satırlar.collect {
      case s if s.tr.contains('.') => (s.tr.substring(0, s.tr.lastIndexOf('.') + 1) + "\u0000" + s.tr.substring(s.tr.lastIndexOf('.') + 1), AlıcıylaBirlikte + s.en, sarmalayıcıdan(s), s.sayı, false)
    })
    private def parantezsizMi(k: Kural) = k.not.contains(Parantezsiz)

    // Önce bağlama tam uyan kural (yalın / üye / alıcı / `Alıcı.`), sonra "*". Alıcıya
    // özel arama (`Resim.`) "*" kuralına DÜŞMEZ: o kural yalnız o alıcı için anlamlı;
    // düşerse sıradan üye araması zaten "*"ı dener.
    private def kural(yön: String, ad: String, bağlam: String): Option[Kural] =
      kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == bağlam)
        .orElse(if (bağlam == BağlamAlıcıÇağrı) kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == BağlamAlıcı) else None)
        .orElse(if (bağlam == BağlamÜyeÇağrı) kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == BağlamÜye) else None)
        .orElse(if (bağlam.endsWith(".")) None else kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == BağlamHepsi))

    // Alıcıya özel arama (`resim.`) yalnız KURAL bilir; kural yoksa None döner ki çağıran
    // sıradan üye/yalın aramasına geçebilsin. Tabloya düşerse `resim.sil()` için `üye ->
    // erase` kuralına hiç sıra gelmiyordu (ölçüldü: sil, boyu, Color, hsla hepsi bundan).
    private def seç(yön: String, ad: String, bağlam: String, tablo: Map[String, Seq[(String, Int)]]): Option[Seçim] =
      kural(yön, ad, bağlam) match {
        case Some(k) if k.hedef == Çevirme  => None
        case Some(k)                        => Some(Seçim(k.hedef, Nil, parantezsizMi(k)))
        case None if bağlam.endsWith(".")   =>
          val alıcılı = if (yön == "en>tr") enAlıcılıAdaylar else trAlıcılıAdaylar
          alıcılı.get(bağlam + "\u0000" + ad).map(as => Seçim(as.head._1, ciddiAlternatifler(as)))
        case None                           => tablo.get(ad).map(as => Seçim(as.head._1, ciddiAlternatifler(as)))
      }

    // DİKKAT: "üye(" de üye bağlamıdır. Burayı atlamak sessiz ve geniş bir gerileme olurdu:
    // ARGÜMANLI her üye çağrısı yalın tabloya (trAdaylarYalın) düşer ve `üye` işaretli
    // satırlar -- sözlüğün büyük bölümü -- aday olmaktan çıkardı.
    private def üyeBağlamı(bağlam: String) =
      bağlam == BağlamÜye || bağlam == BağlamÜyeÇağrı || bağlam.endsWith(".")
    /** Bu ad için bu bağlamda açık bir "çevirme" (-) kuralı var mı? Yön: "tr>en" | "en>tr". */
    def çevrilmez(yön: String, ad: String, bağlam: String): Boolean = kural(yön, ad, bağlam).exists(_.hedef == Çevirme)
    def türkçedenİngilizceye(ad: String, bağlam: String): Option[Seçim] =
      seç("tr>en", ad, bağlam, if (üyeBağlamı(bağlam)) trAdaylar else trAdaylarYalın)
    def ingilizcedenTürkçeye(ad: String, bağlam: String): Option[Seçim] =
      seç("en>tr", ad, bağlam, if (üyeBağlamı(bağlam)) enAdaylar else enAdaylarYalın)

    /** Sözlüğün bildiği bütün Türkçe adlar (kurallardakiler dahil). */
    def türkçeAdlar: Set[String] = trAdaylar.keySet ++ kurallar.filter(_.yön == "tr>en").map(_.ad)
    def ingilizceAdlar: Set[String] = enAdaylar.keySet ++ kurallar.filter(_.yön == "en>tr").map(_.ad)
  }
}
