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

import java.io.File

import scala.io.Source

import net.kogics.kojo.util.Utils
import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.Tokens

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
  final case class Satır(cins: String, tr: String, en: String, kaynak: String, not: String = "")
  val YalnızÜye = "üye"
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

  private def alanlar(tsv: String): Iterator[Array[String]] =
    tsv.linesIterator.filterNot(l => l.trim.isEmpty || l.startsWith("#")).map(_.split('\t'))

  def satırlarıAyrıştır(tsv: String): Seq[Satır] = alanlar(tsv).map {
    case Array(cins, tr, en, kaynak, not, _*) => Satır(cins, tr, en, kaynak, not)
    case Array(cins, tr, en, kaynak)          => Satır(cins, tr, en, kaynak)
    case Array(cins, tr, en)                  => Satır(cins, tr, en, "")
    case a                              => sys.error(s"ceviri-sozlugu.tsv: bozuk satır: ${a.mkString("|")}")
  }.toVector

  def kurallarıAyrıştır(tsv: String): Seq[Kural] = alanlar(tsv).map {
    case Array(yön, ad, bağlam, hedef, not, _*) => Kural(yön, ad, bağlam, hedef, not)
    case Array(yön, ad, bağlam, hedef)          => Kural(yön, ad, bağlam, hedef, "")
    case a                                      => sys.error(s"ceviri-kurallar.tsv: bozuk satır: ${a.mkString("|")}")
  }.toVector

  def yükle(): Sözlük =
    new Sözlük(satırlarıAyrıştır(Utils.loadResource(sözlükYolu)), kurallarıAyrıştır(Utils.loadResource(kurallarYolu)))

  class Sözlük(val satırlar: Seq[Satır], val kurallar: Seq[Kural]) {
    // Adaylar sıklığa göre sıralı: bir Türkçe ad 22 yerde `min`e, 1 yerde
    // `MaxValue`a gidiyorsa `min` başa gelir. Eşitlikte SARMALAYICI satırı data.scala tablosunu
    // yener -- tablo elle ve yer yer eskimiş (çokHızlı=SuperFast, kosinüs=cos yazıyor; sarmalayıcı
    // superFast ve math.cos diyor; ölçüldü). En sonda ad sırası (kararlılık).
    private def adaylar(çiftler: Seq[(String, String, Boolean)]): Map[String, Seq[(String, Int)]] =
      çiftler.groupBy(_._1).map { case (ad, ss) =>
        val sayım = ss.groupBy(_._2).map { case (hedef, hs) => (hedef, hs.size, hs.count(_._3)) }
        ad -> sayım.toSeq.sortBy { case (hedef, n, sarmalayıcı) => (-n, -sarmalayıcı, hedef) }.map { case (hedef, n, _) => (hedef, n) }
      }
    private def sarmalayıcıdan(s: Satır) = !s.kaynak.startsWith("data.scala")

    // "Belirsiz" sayılmak için ikinci adayın kazananın en az YARISI kadar kaynağı olmalı.
    // Ölçüldü: eşik yokken `sağ -> right(3) / RIGHT(1)` her kullanımda raporlanıyor,
    // 90 betikte 1011 satırlık işe yaramaz bir rapor çıkıyordu.
    private def ciddiAlternatifler(as: Seq[(String, Int)]): Seq[String] = as match {
      case (_, kazanan) +: kalan => kalan.collect { case (hedef, n) if n * 2 >= kazanan => hedef }
      case _                     => Nil
    }

    private val trAdaylar = adaylar(satırlar.map(s => (s.tr, s.en, sarmalayıcıdan(s))))
    private val enAdaylar = adaylar(satırlar.map(s => (s.en, s.tr, sarmalayıcıdan(s))))
    // Yalın bağlam için: derleyicinin yalın çözemediği İngilizce hedefler dışarıda.
    private val yalınSatırlar = satırlar.filterNot(_.not == YalnızÜye)
    private val trAdaylarYalın = adaylar(yalınSatırlar.map(s => (s.tr, s.en, sarmalayıcıdan(s))))
    private val enAdaylarYalın = adaylar(yalınSatırlar.map(s => (s.en, s.tr, sarmalayıcıdan(s))))
    // `kuvveti -> math.pow`: ters yönde tek jeton `pow` gelir, alıcısı `math`. Nitelenmiş
    // hedefler alıcı bağlamıyla (`math.`) ayrıca dizinlenir; `math` kendisi `Matematik`e
    // çevrilir, üye buradan `kuvveti` olur. Ölçüldü: yoksa `Matematik.pow` kalıyordu.
    private val enAlıcılıAdaylar = adaylar(satırlar.collect {
      case s if s.en.contains('.') && s.not != TakmaAd => (s.en.substring(0, s.en.lastIndexOf('.') + 1) + "\u0000" + s.en.substring(s.en.lastIndexOf('.') + 1), s.tr, sarmalayıcıdan(s))
    })
    // `Resim.dizi -> picStack`: Türkçe nesnenin üyesi İngilizce'de YALIN. Üreteç böyle satırların
    // tr'sini nitelenmiş yazar; buradan TR->EN alıcılı arama `^picStack` (alıcı yutulur),
    // EN->TR ise enAdaylar üstünden zaten `Resim.dizi` verir.
    private val trAlıcılıAdaylar = adaylar(satırlar.collect {
      case s if s.tr.contains('.') => (s.tr.substring(0, s.tr.lastIndexOf('.') + 1) + "\u0000" + s.tr.substring(s.tr.lastIndexOf('.') + 1), AlıcıylaBirlikte + s.en, sarmalayıcıdan(s))
    })
    private def parantezsizMi(k: Kural) = k.not.contains(Parantezsiz)

    // Önce bağlama tam uyan kural (yalın / üye / alıcı / `Alıcı.`), sonra "*". Alıcıya
    // özel arama (`Resim.`) "*" kuralına DÜŞMEZ: o kural yalnız o alıcı için anlamlı;
    // düşerse sıradan üye araması zaten "*"ı dener.
    private def kural(yön: String, ad: String, bağlam: String): Option[Kural] =
      kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == bağlam)
        .orElse(if (bağlam == BağlamAlıcıÇağrı) kurallar.find(k => k.yön == yön && k.ad == ad && k.bağlam == BağlamAlıcı) else None)
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

    private def üyeBağlamı(bağlam: String) = bağlam == BağlamÜye || bağlam.endsWith(".")
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

/**
 * ceviri-sozlugu.tsv'yi Türkçe sarmalayıcı kaynaklarından üretir.
 *
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.SözlükÜreteci"      # depo kökünden
 *
 * (Modern JDK'da sbt.sh yerine CLAUDE.md'deki launcher-jar reçetesi.)
 *
 * NE ÇIKARIR: yalnız RHS'si düz bir ad zinciri olan bildirimler --
 *   def ileri(adım: Kesir): Birim = englishTurtle.forward(adım)   -> ileri  forward
 *   val siyah = Renkler.siyah  ... Renkler.siyah = r.black         -> siyah  black   (zincir çözülür)
 *   type Belki[B] = Option[B]                                       -> Belki  Option
 *   val (a, b) = (x.foo, y.bar)                                     -> a foo, b bar
 * Gövdesi blok, if, match vb. olan tanımlar bir "İngilizce karşılık" taşımadığı
 * için atlanır; bu bir sınır, hata değil. Hedefi Türkçe kalan (zinciri
 * çözülemeyen) satırlar da atlanır.
 *
 * NEDEN JETONLA: düzenli deyişle yapılmış bir ilk sürüm `apply`, `Col`, `C2`
 * gibi tür parametrelerini ve yorum/dizgi içindeki adları "çift" sanıyordu
 * (ölçüldü: 58 sahte belirsizlik). Yamalı scalariform hem Türkçe anahtar
 * sözcükleri hem dizgi/yorum sınırlarını doğru biliyor.
 */
object SözlükÜreteci {
  import ÇeviriSözlüğü.{Satır, YalnızÜye, TakmaAd, Gövdeli}

  /** Sarmalayıcı olmayan dosyalar: sözlük tabloları, çıktı çevirisi, yardım metni... */
  val atlananDosyalar = Set("dict.scala", "translate.scala", "help.scala", "templates.scala",
    "cevirmen.scala", "cevirisozlugu.scala", "ceviridogrulama.scala") // çevirmenin kendisi sarmalayıcı değil

  private val ascii = "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?$".r
  /** Yazılımcıkta niteleyicisiyle yazılan İngilizce nesneler: `math.sqrt`, `math.Pi`. */
  private val nitelenmişKalır = Set("math", "Picture")
  /** `new`suz kurulan Türkçe sarmalayıcı sınıflar: `Yöney2B(v.rotate(a))` içteki zinciri sarar. */
  private val sarmalayıcıYapıcıları = Set("Yöney2B")
  /** Her nesnede olan, çevrilmesi anlamsız adlar; sözlüğe girerlerse `apply -> Resim`
    * gibi 28 kaynaklı sahte çiftler çıkıyor (ölçüldü). */
  private val evrenselAdlar = Set("apply", "unapply", "toString", "equals", "hashCode", "copy", "main", "compare")
  /** `class BüyütBD(...) extends ComposableTransformer`: parantezsiz üst sınıf bir API adı
    * değil, taban sınıftır; hedef sanılınca `büyüt -> ComposableTransformer` çıkıyordu
    * (ölçüldü). Parantezli zincir (extends Dönüşüm(richBuiltins.rot(a))) yine alınır. */
  private val tabanSınıflar = Set("ComposableTransformer", "Transformer", "AnyRef", "Any", "Serializable", "Product",
    "Exception", "RuntimeException", "Enumeration", "Ordered", "Ordering", "Iterable", "Seq", "IndexedSeq", "Iterator")
  /** 1-2 harflik adlar sarmalayıcı değil, tür parametresi ya da iç yardımcıdır (Col, C2, S, f2, ts). */
  private def sarmalayıcıAdı(ad: String) = ad.length > 2 && !evrenselAdlar(ad)
  /** `def nokta(p: Nokta) = p`: RHS bir parametre, İngilizce API değil. 1-2 harflik
    * İngilizce API adı yok denecek kadar az; ölçüldü, süzmeyince p/x/n en sık
    * "belirsiz" çıkıyordu (294/250/205 kez). */
  /** toString/equals hedef OLABİLİR (yazıya -> toString; ölçüldü: yoksa `x.yazıya` çevrilmiyor);
    * apply/unapply/copy/main olamaz. */
  private val hedefOlamaz = Set("apply", "unapply", "copy", "main")
  private def hedefAdı(en: String) = { val sonParça = en.substring(en.lastIndexOf('.') + 1); sonParça.length > 2 && !hedefOlamaz(sonParça) }
  private val bildirimTürleri = Set(Tokens.DEF, Tokens.VAL, Tokens.VAR, Tokens.TYPE)
  private val açanlar = Set(Tokens.LPAREN, Tokens.LBRACKET, Tokens.LBRACE)
  private val kapayanlar = Set(Tokens.RPAREN, Tokens.RBRACKET, Tokens.RBRACE)

  def kaynakDosyaları(kök: File): Seq[File] = {
    val i18n = new File(kök, "src/main/scala/net/kogics/kojo/lite/i18n")
    val tr = new File(i18n, "tr")
    val trDosyaları = Option(tr.listFiles()).getOrElse(Array.empty[File])
      .filter(f => f.getName.endsWith(".scala") && !atlananDosyalar(f.getName)).sortBy(_.getName)
    new File(i18n, "trInit.scala") +: trDosyaları.toSeq
  }

  def oku(f: File): String = {
    val s = Source.fromFile(f, "UTF-8")
    try s.mkString finally s.close()
  }

  /** Bir dosyadan ham çiftler (zincir henüz çözülmemiş; `en` Türkçe kalmış olabilir). */
  def hamÇıkar(dosyaAdı: String, kod: String): Seq[Satır] = {
    val hepsi = ScalaLexer.rawTokenise(kod, forgiveErrors = true, scalaVersion = "2.13.0")
    val ts = hepsi.filterNot(t => t.tokenType == Tokens.WS || t.tokenType.isComment || t.tokenType.isNewline).toVector
    val satırNo: Int => Int = { val satırBaşları = 0 +: kod.zipWithIndex.collect { case ('\n', i) => i + 1 }
      ofs => satırBaşları.count(_ <= ofs) }
    def id(t: Token) = t.tokenType.isId && !t.text.startsWith("`") && t.text.exists(_.isLetter)
    val sonuç = Vector.newBuilder[Satır]

    // i konumundan başlayarak `id (DOT id)*` zincirini okur; sondaki adı ve bir sonraki konumu verir.
    def zincir(i0: Int, params: Set[String] = Set.empty): Option[(String, Int)] = {
      val yeniyle = i0 < ts.length && ts(i0).tokenType == Tokens.NEW
      var i = if (yeniyle) i0 + 1 else i0
      // `= { RepeatCommands.repeat(n) { ... } }`: gövdeli ama ilk deyimi bir çağrı
      // zinciri olan tanımlar (yinele, yineleİçin, zıpla...). İlk deyim zincir
      // değilse (val, if, ...) None döner ve tanım atlanır -- sınır, hata değil.
      if (i < ts.length && ts(i).tokenType == Tokens.LBRACE) return zincir(i + 1, params)
      if (i >= ts.length || !id(ts(i))) return None
      val parçalar = Vector.newBuilder[String]
      parçalar += ts(i).text
      var sonAd = ts(i).text; i += 1
      while (i + 1 < ts.length && ts(i).tokenType == Tokens.DOT && id(ts(i + 1))) { sonAd = ts(i + 1).text; parçalar += sonAd; i += 2 }
      // `def karekökü(x) = math.sqrt(x)`, `richBuiltins.Picture.circle(r)`: İngilizce yazılımcıkta
      // sqrt ve circle YALIN değil -- `math.sqrt`, `Picture.circle`. Niteleyiciyi düşürünce
      // çeviri derlenmiyordu (ölçüldü: angles.kojo, "not found: value sqrt"). Yalnız bilinen
      // yazılımcık nesneleri için: öteki niteleyiciler (englishTurtle, richBuiltins)
      // sarmalayıcının iç adları, yazılımcığa çıkmaz.
      val p = parçalar.result()
      val kök = p.lastIndexWhere(nitelenmişKalır)
      if (kök >= 0 && kök < p.length - 1) sonAd = p.drop(kök).mkString(".")
      // `new Resim(richBuiltins.Picture.circle(r))`: dıştaki sarmalayıcı sınıf hedef
      // değil, hedef parantezin İÇİNDEKİ zincir. Ölçüt "Türkçe harf var mı" DEĞİL --
      // Resim, Renk, Nokta ASCII -- ölçüt `new` ile kurulmuş olması. İçeride zincir
      // yoksa (new Kaplumbağa(t)) sınıfın kendisi hedef kalır.
      // İçte zincir yoksa (new AColor(120, 0, 200), new Kaplumbağa(x, y)) hedef YOK: `val mor =
      // new AColor(...)` bir değer, takma ad değil -- sınıf adını hedef sayınca `mor -> AColor`
      // çıkıyordu (ölçüldü: circles.kojo). new Kaplumbağa için kural var (yeniKaplumbağa).
      // `Yöney2B(rb.bouncePicVectorOffStage(p, v))`: `new`suz sarmalayıcı yapıcısı da aynı --
      // hedef içteki zincir (ölçüldü: lunar-lander.kojo, sahneKenarındanYansıtma -> Vector2D).
      val sarmalayıcıYapıcısı = p.length == 1 && sarmalayıcıYapıcıları(p.head)
      if ((yeniyle || sarmalayıcıYapıcısı) && i < ts.length && ts(i).tokenType == Tokens.LPAREN) return zincir(i + 1, params) match {
        case Some(iç) if !params(iç._1) => Some(iç)
        case _                          => None
      }
      Some((sonAd, i))
    }

    // Bildirimin başından `=`e kadar (parametreler, cins) atlar; derinlik 0'da `=` yoksa None.
    def eşittireKadar(i0: Int): Option[Int] = {
      var i = i0; var derinlik = 0
      while (i < ts.length) {
        val t = ts(i)
        if (açanlar(t.tokenType)) derinlik += 1
        else if (kapayanlar(t.tokenType)) { derinlik -= 1; if (derinlik < 0) return None }
        else if (derinlik == 0 && t.tokenType == Tokens.EQUALS) return Some(i)
        else if (derinlik == 0 && (bildirimTürleri(t.tokenType) || t.tokenType.isKeyword && t.tokenType != Tokens.IMPLICIT && t.tokenType != Tokens.OVERRIDE && t.tokenType != Tokens.FINAL && t.tokenType != Tokens.LAZY && t.tokenType != Tokens.PRIVATE && t.tokenType != Tokens.PROTECTED)) return None
        i += 1
      }
      None
    }

    // İmzadaki parametre adları: `ad:` biçiminde geçen her ad. `def çiz(rler: Resim*) = rler.foreach(...)`
    // gibi tanımlarda RHS parametreye başlıyor; onu İngilizce API sanmak sözlüğü
    // kirletiyordu (ölçüldü: p/x/n/rler en sık "belirsiz" çıkıyordu).
    def parametreler(baş: Int, bitiş: Int): Set[String] =
      (baş until bitiş).collect { case j if id(ts(j)) && j + 1 < ts.length && ts(j + 1).tokenType == Tokens.COLON => ts(j).text }.toSet

    // package.scala: sarmalayıcı yok ama çekirdek tür takma adları (type Nokta = Point) ve
    // sabitler (val çokHızlı = superFast) var; def'ler ve anahtar sözcük listeleri (_trKeywords,
    // trKeywordSet) alınmaz. Yalnız type alınca hız sabitleri kayboluyordu (ölçüldü: eye.kojo).
    val paketDosyası = dosyaAdı == "package.scala"
    def paketteAlınır(cins: String, ad: String) = !paketDosyası || ((cins == "type" || cins == "val") && !ad.startsWith("_") && !ad.toLowerCase.contains("keyword"))
    val yalnızTür = false
    // data.scala: elle kurulmuş `nDef("silVeSakla", "cleari")` tablosu (Name(tr, en, ne)). Sarmalayıcı
    // değil, doğrudan çift; boş en = "İngilizce karşılığı yok". Aynı jeton akışından okunur.
    if (dosyaAdı == "data.scala") {
      val cinsler = Map("nDef" -> "def", "nVal" -> "val", "nType" -> "type", "nMet" -> "def")
      var j = 0
      while (j + 5 < ts.length) {
        if (id(ts(j)) && cinsler.contains(ts(j).text) && ts(j + 1).tokenType == Tokens.LPAREN &&
            ts(j + 2).tokenType == Tokens.STRING_LITERAL && ts(j + 3).tokenType == Tokens.COMMA && ts(j + 4).tokenType == Tokens.STRING_LITERAL) {
          val tr = ts(j + 2).text.stripPrefix("\"").stripSuffix("\""); val en = ts(j + 4).text.stripPrefix("\"").stripSuffix("\"")
          if (en.nonEmpty && tr != en && sarmalayıcıAdı(tr) && hedefAdı(en)) sonuç += Satır(cinsler(ts(j).text), tr, en, s"$dosyaAdı:${satırNo(ts(j).offset)}")
        }
        j += 1
      }
      return sonuç.result()
    }
    // Kapsayıcı nesne: `object Resim { ... }` içindeki üyeler. İngilizce karşılığı YALIN olan
    // üyeler (picStack, stageArea) tr'de `Resim.x` diye yazılır; Sözlük bundan iki yönü kurar.
    // Derinlik ayrıca YEREL val'leri eler: `def f = { val faktor = math.pow(...) }` bir
    // sarmalayıcı değil (ölçüldü: faktor -> pow sahte çifti).
    val nesneYığını = scala.collection.mutable.Stack.empty[(String, Int)]
    var derinlik = 0
    var bekleyenNesne: Option[String] = None
    def kapsayıcıNesne: Option[String] = nesneYığını.headOption.map(_._1).filter(_.nonEmpty)
    def üyeDüzeyinde: Boolean = derinlik <= 1 || nesneYığını.headOption.exists(_._2 == derinlik)
    var i = 0
    while (i < ts.length) {
      val t = ts(i)
      // object/class/trait: gövdesi üye düzeyi. Yalnız object alınınca trait gövdesindeki
      // `val çokHızlı = ...` yerel sanılıp eleniyordu (ölçüldü: eye.kojo, fireworks-canvas.kojo).
      if ((t.tokenType == Tokens.OBJECT || t.tokenType == Tokens.CLASS || t.tokenType == Tokens.TRAIT) && i + 1 < ts.length && id(ts(i + 1)))
        bekleyenNesne = Some(if (t.tokenType == Tokens.OBJECT) ts(i + 1).text else "")
      if (t.tokenType == Tokens.LBRACE) { derinlik += 1; bekleyenNesne.foreach(n => nesneYığını.push((n, derinlik))); bekleyenNesne = None }
      if (t.tokenType == Tokens.RBRACE) { while (nesneYığını.headOption.exists(_._2 >= derinlik)) nesneYığını.pop(); derinlik -= 1 }
      val cinsi = t.text match { case "dez" => "val"; case "den" => "var"; case "tanım" => "def"; case "tür" => "type"; case x => x }
      if (bildirimTürleri(t.tokenType) && i + 1 < ts.length && paketteAlınır(cinsi, ts(i + 1).text)) {
        val cins = cinsi
        val kaynak = s"$dosyaAdı:${satırNo(t.offset)}"
        val sonraki = ts(i + 1)
        if (id(sonraki)) {
          eşittireKadar(i + 2).foreach { e =>
            val params = parametreler(i + 2, e)
            val yerelDeğer = (cins == "val" || cins == "var") && !üyeDüzeyinde
            val gövdeli = e + 1 < ts.length && ts(e + 1).tokenType == Tokens.LBRACE
            zincir(e + 1, params).foreach { case (en, _) =>
              if (en != sonraki.text && !params(en) && !yerelDeğer && sarmalayıcıAdı(sonraki.text) && hedefAdı(en)) {
                val trAdı = if (kapsayıcıNesne.contains("Resim") && !en.contains('.') && cins == "def") "Resim." + sonraki.text else sonraki.text
                sonuç += Satır(cins, trAdı, en, kaynak, if (gövdeli) Gövdeli else "")
              }
            }
          }
        }
        else if (sonraki.tokenType == Tokens.LPAREN) {
          // val (a, b, c) = (x, y, z)
          var j = i + 2; val adlar = Vector.newBuilder[String]
          while (j < ts.length && ts(j).tokenType != Tokens.RPAREN) { if (id(ts(j))) adlar += ts(j).text; j += 1 }
          if (j + 2 < ts.length && ts(j + 1).tokenType == Tokens.EQUALS && ts(j + 2).tokenType == Tokens.LPAREN) {
            var k = j + 3; val hedefler = Vector.newBuilder[Option[String]]
            var derinlik = 0; var devam = true
            while (devam && k < ts.length) {
              zincir(k) match {
                case Some((en, k2)) if derinlik == 0 => hedefler += Some(en); k = k2
                case _ =>
                  ts(k).tokenType match {
                    case tt if açanlar(tt)             => derinlik += 1; hedefler += None; k += 1
                    case Tokens.RPAREN if derinlik == 0 => devam = false
                    case tt if kapayanlar(tt)          => derinlik -= 1; k += 1
                    case Tokens.COMMA                   => k += 1
                    case _                              => hedefler += None; k += 1
                  }
              }
              // bir öğeyi okuduktan sonra virgüle ya da kapanışa kadar atla
              if (devam) {
                var d2 = 0
                while (k < ts.length && !(d2 == 0 && (ts(k).tokenType == Tokens.COMMA || ts(k).tokenType == Tokens.RPAREN))) {
                  if (açanlar(ts(k).tokenType)) d2 += 1 else if (kapayanlar(ts(k).tokenType)) d2 -= 1
                  k += 1
                }
                if (k < ts.length && ts(k).tokenType == Tokens.COMMA) k += 1
                else devam = false
              }
            }
            val a = adlar.result(); val h = hedefler.result()
            if (a.size == h.size) a.zip(h).foreach { case (tr, Some(en)) if en != tr && sarmalayıcıAdı(tr) && hedefAdı(en) => sonuç += Satır(cins, tr, en, kaynak); case _ => }
          }
        }
      }
      // `class DöndürBD(açı: Kesir) extends Dönüşüm(richBuiltins.rot(açı))`: dönüştürücüler
      // ve Türkçe sınıflar böyle kurulu; sınıf adı -> extends'in parantezindeki zincir
      // (yoksa üst sınıfın adı). Sonra `döndür -> DöndürBD -> rot` geçişli çözülür.
      else if ((t.tokenType == Tokens.CLASS || t.tokenType == Tokens.OBJECT) && i + 1 < ts.length && id(ts(i + 1)) && !yalnızTür) {
        val ad = ts(i + 1).text
        var j = i + 2; var derinlik = 0
        def durak(tt: scalariform.lexer.TokenType) = tt == Tokens.EXTENDS || tt == Tokens.LBRACE || bildirimTürleri(tt) || tt == Tokens.CLASS || tt == Tokens.OBJECT
        while (j < ts.length && !(derinlik == 0 && durak(ts(j).tokenType))) {
          if (açanlar(ts(j).tokenType)) derinlik += 1 else if (kapayanlar(ts(j).tokenType)) derinlik -= 1
          j += 1
        }
        if (j < ts.length && ts(j).tokenType == Tokens.EXTENDS) {
          val params = parametreler(i + 2, j)
          zincir(j + 1, params).foreach { case (üstSınıf, k) =>
            // Yalnız parantezli zincir: `extends Dönüşüm(richBuiltins.rot(a))` -> rot. Parantezsiz üst
            // sınıf adı (extends TurkishTurtle, extends ComposableImageEffect) bir API değil, iç
            // sınıftır; hedef sayınca Kaplumbağa -> TurkishTurtle çıkıyordu (ölçüldü). Yöney2B ->
            // Vector2D gibi gerçek olanlar kural dosyasında.
            val içZincir = if (k < ts.length && ts(k).tokenType == Tokens.LPAREN) zincir(k + 1, params).map(_._1).filterNot(params) else None
            içZincir.foreach { hedef =>
              if (hedef != ad && sarmalayıcıAdı(ad) && hedefAdı(hedef))
                sonuç += Satır(if (t.tokenType == Tokens.CLASS) "class" else "object", ad, hedef, s"$dosyaAdı:${satırNo(t.offset)}")
            }
            locally(üstSınıf)
          }
        }
      }
      i += 1
    }
    sonuç.result()
  }

  /** Türkçe hedefleri geçişli çözer: siyah -> Renkler.siyah -> black. En çok 4 atlama.
    * "Türkçe hedef" ölçütü harf değil, ADIN BURADA TANIMLI OLMASI: silipSakla -> silVeSakla
    * ASCII yazılıyor ama silVeSakla bir sarmalayıcı; onu İngilizce sanınca çeviri
    * silVeSakla() basıyordu (ölçüldü: animated-square-creation.kojo). */
  def zincirleriÇöz(ham: Seq[Satır], kuralHedefi: Map[String, String] = Map.empty): Seq[Satır] = {
    // Çözüm sırası: sarmalayıcı satırları önce, data.scala tablosu sonra. Tersi olunca
    // `çokHızlı -> tr.çokHızlı` tablonun SuperFast'ine çözülüp yanlış hedefe ikinci bir oy
    // veriyordu (ölçüldü: eye.kojo, "not found: value SuperFast").
    val hedefi: Map[String, Seq[String]] = ham.groupBy(_.tr).map { case (tr, ss) =>
      tr -> ss.sortBy(s => s.kaynak.startsWith("data.scala")).map(_.en).distinct }
    def çöz(en: String, derinlik: Int, görülen: Set[String]): Option[String] =
      if (derinlik == 0 || görülen(en)) None
      else if (hedefi.contains(en)) hedefi(en).iterator.flatMap(e => çöz(e, derinlik - 1, görülen + en)).nextOption()
      // `canlan -> tepkiVer`: hedef ASCII ama Türkçe bir ad; gövdeli olduğundan üretilmiş satırı
      // yok, kural dosyası biliyor (tepkiVer -> react). Kural da bir halka (ölçüldü: hunted.kojo).
      else if (kuralHedefi.contains(en)) Some(kuralHedefi(en))
      else if (ascii.matches(en)) Some(en)
      else None
    // Gövdeli tanım (`def üçgen(en) = { yinele(3) { ... } }`): ilk deyim bir başka Türkçe
    // sarmalayıcıysa bu bir GERÇEKLEŞTİRME, takma ad değil -- zincirlenince `üçgen -> repeat`
    // çıkıyor ve kullanıcının `tanım üçgen` tanımı `def repeat` oluyordu (ölçüldü:
    // sierpinski-tri.kojo). Gövdeli satır yalnız doğrudan İngilizce hedefe bağlanır.
    ham.flatMap { s =>
      if (s.not == Gövdeli) { if (hedefi.contains(s.en)) None else çöz(s.en, 1, Set(s.tr)).map(en => s.copy(en = en, not = "")) }
      else çöz(s.en, 4, Set(s.tr)).map(en => s.copy(en = en))
    }
  }

  /** Sabit tabloları: `object Çalgı { val AkustikBas = 32 }` (muzik.scala) ile
    * `object Instrument { val ACOUSTIC_BASS = 32 }` (music/Instrument.scala), `object Görünüş
    * { val araba = "/media/costumes/car.png" }` ile `class Costume { val car = ... }` arasında
    * sarmalayıcı yok, eşleşme DEĞERLE kurulur (sayı ya da dizgi sabiti). Aynı değeri taşıyan
    * adlar sırayla eşlenir (Piyano/PIANO, AkustikKuyruklu/ACOUSTIC_GRAND); sırası aşan ad ilkine
    * bağlanır. Satırlar iki yanı da nitelenmiş yazılır (`Çalgı.AkustikBas` ->
    * `Instrument.ACOUSTIC_BASS`): TR->EN alıcıyı yutup tam adı yazar, EN->TR alıcıyı kuraldan
    * (Instrument -> Çalgı), üyeyi buradan alır. Takma ad alıcıları (Çizim = Görünüş) yalnız
    * TR->EN için, `takma` notuyla. */
  private val sabitTanımı = "val\\s+([A-Za-zİıŞşĞğÖöÜüÇç_][A-Za-z0-9İıŞşĞğÖöÜüÇç_]*)\\s*(?::\\s*[A-Za-zİıŞşĞğÖöÜüÇç_]+)?\\s*=\\s*(\\d+|\"[^\"]*\")".r
  /** (Türkçe dosya, Türkçe nesne, takma adları, İngilizce dosya, İngilizce nesne). */
  val sabitNesneÇiftleri: Seq[(String, String, Seq[String], String, String)] = Seq(
    ("lite/i18n/tr/muzik.scala", "Çalgı", Nil, "music/Instrument.scala", "Instrument"),
    ("lite/i18n/tr/cizim.scala", "Görünüş", Seq("Çizim"), "turtle/TurtleWorldAPI.scala", "Costume"),
    ("lite/i18n/tr/cizim.scala", "Görünüş", Seq("Çizim"), "turtle/TurtleWorldAPI.scala", "Background"),
    ("lite/i18n/tr/cizim.scala", "Artalan", Nil, "turtle/TurtleWorldAPI.scala", "Background"),
    ("lite/i18n/tr/ses.scala", "Ses", Nil, "turtle/TurtleWorldAPI.scala", "Sound"))
  private def nesneGövdesi(kod: String, nesneAdı: String): Option[(String, Int)] = {
    val başlangıç = ("(object|class)\\s+" + java.util.regex.Pattern.quote(nesneAdı) + "\\s*\\{").r.findFirstMatchIn(kod).map(_.start).getOrElse(-1)
    if (başlangıç < 0) return None
    val gövde = kod.substring(başlangıç)
    var d = 0; var i = gövde.indexOf('{'); var sonKonum = -1
    while (sonKonum < 0 && i < gövde.length) { gövde(i) match { case '{' => d += 1; case '}' => d -= 1; if (d == 0) sonKonum = i; case _ => }; i += 1 }
    Some((gövde.substring(0, if (sonKonum < 0) gövde.length else sonKonum), kod.substring(0, başlangıç).count(_ == '\n') + 1))
  }
  private def sabitler(dosya: File, nesneAdı: String): Seq[(String, String, Int)] =
    if (!dosya.exists()) Nil
    else nesneGövdesi(oku(dosya), nesneAdı).toSeq.flatMap { case (gövde, satırBaşı) =>
      sabitTanımı.findAllMatchIn(gövde).toSeq.map(m => (m.group(1), m.group(2), satırBaşı + gövde.substring(0, m.start).count(_ == '\n')))
    }
  def sabitNesneler(kök: File): Seq[Satır] = sabitNesneÇiftleri.flatMap { case (trDosya, trNesne, takmalar, enDosya, enNesne) =>
    val kaynakKökü = new File(kök, "src/main/scala/net/kogics/kojo")
    val tr = sabitler(new File(kaynakKökü, trDosya), trNesne)
    val en = sabitler(new File(kaynakKökü, enDosya), enNesne)
    val trDeğere = tr.groupBy(_._2); val enDeğere = en.groupBy(_._2)
    def sıralı(değer: String, i: Int, karşı: Map[String, Seq[(String, String, Int)]]) =
      karşı.get(değer).map(ks => ks(math.min(i, ks.size - 1))._1)
    val trDosyaAdı = trDosya.substring(trDosya.lastIndexOf('/') + 1); val enDosyaAdı = enDosya.substring(enDosya.lastIndexOf('/') + 1)
    // İngilizce karşılığı olmayan Türkçe sabit (Görünüş.top1: collidium imgeleri Costume'da yok):
    // hedef değerin kendisi -- `Görünüş.top1` -> `"/media/collidium/ball1.png"`. Dizgi sabiti
    // yalın olarak yazılımcıkta geçerli; Resim.imge(Görünüş.top1) İngilizce'de zaten yolla yazılır.
    val trTarafı = trDeğere.toSeq.flatMap { case (v, adlar) => adlar.zipWithIndex.map { case ((ad, _, satır), i) =>
      Satır("val", trNesne + "." + ad, sıralı(v, i, enDeğere).map(enNesne + "." + _).getOrElse(v), s"$trDosyaAdı:$satır") } }
    val enTarafı = enDeğere.toSeq.flatMap { case (v, adlar) => adlar.zipWithIndex.flatMap { case ((ad, _, satır), i) =>
      sıralı(v, i, trDeğere).map(t => Satır("val", trNesne + "." + t, enNesne + "." + ad, s"$enDosyaAdı:$satır")) } }
    val asıl = (trTarafı ++ enTarafı).distinct
    asıl ++ takmalar.flatMap(takma => asıl.map(s => s.copy(tr = takma + s.tr.substring(s.tr.indexOf('.')), not = TakmaAd)))
  }

  /** Yardımcı zincir halkaları: sözlüğe girmez, yalnız zincir çözümüne hizmet eder.
    * `def kalemBoyu(b) = KalemBoyuBD(b)`; `case class KalemBoyuBD(...) { def apply(r) = new
    * Resim(picture.StrokeWidth(w)(r.p)) }`; picture/package.scala: `def strokeWidth(w) =
    * StrokeWidthc(w)`. Buradan `KalemBoyuBD -> StrokeWidthc` (Türkçe sınıf, İngilizce sınıf
    * adının `c`siz yazımı üstünden) ve `StrokeWidthc -> strokeWidth` (İngilizce sarmalayıcı)
    * halkaları çıkar; zincir `kalemBoyu -> strokeWidth`e ulaşır. İngilizce yazılımcıkta yalın
    * ad başka olabilir (penThickness): derleyici sondası ve kurallar karar verir. */
  private val dönüştürücüSınıfı = "case (?:class|object)\\s+([A-Za-z0-9İıŞşĞğÖöÜüÇç_]+)[^{}\\n]*(?:\\n[^{}\\n]*)?\\{\\s*def apply\\(r: Resim\\) = new Resim\\(picture\\.([A-Za-z]+)\\(".r
  private val ingilizceSarmalayıcı = "\\n  (?:def|val)\\s+([a-zA-Z]+)(?:\\([^\\n=]*\\))?\\s*=\\s*([A-Z][A-Za-z]*c)\\b".r
  def yardımcıZincirler(kök: File): Seq[Satır] = {
    val resim = new File(kök, "src/main/scala/net/kogics/kojo/lite/i18n/tr/resim.scala")
    val paket = new File(kök, "src/main/scala/net/kogics/kojo/picture/package.scala")
    val sınıflar = if (resim.exists()) dönüştürücüSınıfı.findAllMatchIn(oku(resim)).toSeq.map(m => Satır("class", m.group(1), m.group(2) + "c", "resim.scala")) else Nil
    val sarmalayıcılar = if (paket.exists()) ingilizceSarmalayıcı.findAllMatchIn(oku(paket)).toSeq.map(m => Satır("def", m.group(2), m.group(1), "picture/package.scala")) else Nil
    sınıflar ++ sarmalayıcılar
  }

  def çıkar(kök: File): Seq[Satır] = {
    val ham = kaynakDosyaları(kök).flatMap { f =>
      val göreli = f.getPath.stripPrefix(kök.getPath).stripPrefix("/")
      hamÇıkar(göreli.substring(göreli.lastIndexOf('/') + 1), oku(f))
    }
    val yardımcı = yardımcıZincirler(kök)
    val yardımcıAdlar = yardımcı.map(_.tr).toSet
    val kurallar = { val f = new File(kök, "src/main/resources" + ÇeviriSözlüğü.kurallarYolu); if (f.exists()) ÇeviriSözlüğü.kurallarıAyrıştır(oku(f)) else Nil }
    val kuralHedefi: Map[String, String] = kurallar.collect {
      case k if k.yön == "tr>en" && (k.bağlam == ÇeviriSözlüğü.BağlamHepsi || k.bağlam == ÇeviriSözlüğü.BağlamYalın) &&
        k.hedef != ÇeviriSözlüğü.Çevirme && !k.hedef.startsWith(ÇeviriSözlüğü.AlıcıylaBirlikte) => k.ad -> k.hedef
    }.groupBy(_._1).map { case (ad, hs) => ad -> hs.head._2 }
    val çözülmüş = (zincirleriÇöz(ham ++ yardımcı, kuralHedefi).filterNot(s => yardımcıAdlar(s.tr)) ++ sabitNesneler(kök)).distinct
    // Derleyici hakem: İngilizce hedef Kojo prelude'ünde yalın adıyla çözülüyor mu? Çözülmeyen
    // (x.length gibi üye, SuperFast gibi iç ad) satır "üye" işaretlenir ve yalın bağlamda
    // sözlükten düşer. Bu tek adım, kaynakların birbiriyle çelişen tercihlerini eler.
    val sondalar = çözülmüş.map(s => (s.en, s.cins == "type")).distinct
    val çözülenler = ÇeviriDoğrulama.yalınÇözülenler(sondalar, türkçe = false)
    çözülmüş.map(s => if (çözülenler(s.en)) s else s.copy(not = YalnızÜye)).sortBy(s => (s.tr, s.en, s.cins, s.kaynak))
  }

  def tsv(satırlar: Seq[Satır]): String = {
    val başlık =
      """|# ÜRETİLMİŞ DOSYA -- elle düzenlemeyin. Üretim:
         |#   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.SözlükÜreteci"
         |# Kaynak: lite/i18n/trInit.scala + lite/i18n/tr/*.scala sarmalayıcıları.
         |# Elle kararlar için: ceviri-kurallar.tsv
         |#
         |# cins	tr	en	kaynak	[not: üye = İngilizce ad yalın çözülmüyor, yalnız x.ad biçiminde]
         |""".stripMargin
    başlık + satırlar.map(s => s"${s.cins}\t${s.tr}\t${s.en}\t${s.kaynak}" + (if (s.not.nonEmpty) "\t" + s.not else "")).mkString("", "\n", "\n")
  }

  def main(args: Array[String]): Unit = {
    val kök = new File(args.headOption.getOrElse("."))
    val satırlar = çıkar(kök)
    println(s"derleyici sondası: ${satırlar.count(_.not == ÇeviriSözlüğü.YalnızÜye)} satır yalnız üye bağlamında")
    val çıktı = new File(kök, "src/main/resources" + ÇeviriSözlüğü.sözlükYolu)
    val eski = if (çıktı.exists()) oku(çıktı) else ""
    val yeniMetin = tsv(satırlar)
    java.nio.file.Files.write(çıktı.toPath, yeniMetin.getBytes("UTF-8"))
    val trSayısı = satırlar.map(_.tr).distinct.size
    val belirsiz = satırlar.groupBy(_.tr).count(_._2.map(_.en).distinct.size > 1)
    println(s"${satırlar.size} çift, $trSayısı ayrı Türkçe ad, $belirsiz çok hedefli -> ${çıktı.getPath}" +
      (if (eski == yeniMetin) " (değişmedi)" else " (GÜNCELLENDİ)"))
  }
}
