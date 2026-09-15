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

import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.TokenType
import scalariform.lexer.Tokens

/**
 * Çevirmen: Türkçe (Koco) yazılımcık <-> İngilizce (Kojo) yazılımcık.
 *
 * NASIL: yazılımcık yamalı scalariform ile jetonlanır; yalnız ANAHTAR SÖZCÜK ve
 * TANIMLAYICI jetonları değiştirilir, gerisi (boşluk, yorum, dizgi, sayı)
 * olduğu gibi kalır. Yani çıktı girdiyle satır satır hizalıdır; yorumlar
 * çevrilmez (kod çevrilir, anlatım değil).
 *
 * NEDEN AST DEĞİL: çeviri ad-ada; yapı bilgisi gerekmiyor. Yapının katacağı
 * tek şey alıcıya bağlı belirsizliği çözmek (`sil()` clear, `resim.sil()`
 * erase); onu "bir önceki jeton `.` mi" kuralı + ceviri-kurallar.tsv
 * karşılıyor. Ölçüldü: 861 Türkçe adın 803'ü tek hedefli.
 *
 * NEDEN DÜZENLİ DEYİŞ DEĞİL: `s"$x ve ${x+1} dez"` içinde `x` KODdur
 * (çevrilmeli), `dez` DİZGİdir (dokunulmamalı). Yamalı sözcükleyici bu ayrımı
 * biliyor, düzenli deyiş bilmiyor; ayrıca `dez`, `tanım`, `eğer` gibi Türkçe
 * anahtar sözcükleri anahtar sözcük JETONU olarak veriyor, o yüzden metin
 * aramıyoruz, jeton türünden gidiyoruz.
 *
 * SINIRLAR (dürüstçe):
 *   - Alıcının TÜRÜNE bağlı adlar (yazı.boyu -> length, resim.boyu -> height)
 *     sözcük düzeyinde ayırt edilemez; en sık görülen alınır ve Rapor.belirsiz'e
 *     yazılır. Kesin kanıt hedef dilin derleyicisidir: çıktıyı derleyin.
 *   - Aynı sınıftan bir ad ELLE KURALLA tek hedefe bağlanmışsa (en>tr take -> al)
 *     seçim tek adaylı olur, yani Rapor.belirsiz'e de DÜŞMEZ. `take` çoğunluk
 *     derlemlerde `al` (13 tanım), Eşlem/Eşlek'te `alSırayla` (orada `al` = get).
 *     Yazı anahtarlı eşlemde yanlış gürültülüdür (tür uyuşmazlığı, derlenmez);
 *     SAYI anahtarlıda sessizdir: `Eşlek(1->"bir",...).al(2)` derlenir ama `take`
 *     değil `get` demektir. Eşlemde `take` çeviriyorsanız çıktıyı gözle doğrulayın.
 *   - Kullanıcının kendi adları sözlükteki bir adla çakışırsa (dez renk = 3)
 *     tutarlı biçimde çevrilir (tanım ve kullanım birlikte); çalışan program
 *     çalışan program olarak kalır.
 *   - Argüman değerine gömülü anlam (göster() = setVisible(true)) taşınamaz.
 *   - Sözlükte olmayan adlar olduğu gibi bırakılır ve Rapor.dokunulmayan'da
 *     sayılır: kullanıcının adları da, sözlüğün eksikleri de oradadır.
 */
object Çevirmen {
  sealed trait Yön
  case object TürkçedenİngilizceyeYön extends Yön
  case object İngilizcedenTürkçeyeYön extends Yön

  final case class Belirsiz(ad: String, seçilen: String, alternatifler: Seq[String], satır: Int)

  final case class Rapor(çevrilen: Int, belirsiz: Seq[Belirsiz], dokunulmayan: Map[String, Int],
                         sözlükteBilinen: Set[String] = Set.empty) {
    /** Kaynak dilde kalmış olması olası adlar: Türkçe harf taşıyanlar VE sözlüğün Türkçe
      * diye tanıyıp bilerek çevirmedikleri (karesi gibi ASCII yazılışlılar da böyle yakalanır). */
    def türkçeKalanlar: Map[String, Int] = dokunulmayan.filter { case (ad, _) => ad.exists(türkçeHarf) || sözlükteBilinen(ad) }
    def özet(yön: Yön): String = {
      val sb = new StringBuilder
      sb.append(s"çevrilen ad: $çevrilen\n")
      if (belirsiz.nonEmpty) {
        sb.append(s"belirsiz seçim (${belirsiz.size}) -- en sık görülen alındı, derleyerek doğrulayın:\n")
        belirsiz.foreach(b => sb.append(f"  satır ${b.satır}%4d  ${b.ad} -> ${b.seçilen}   (öteki: ${b.alternatifler.mkString(", ")})\n"))
      }
      val şüpheli = yön match { case TürkçedenİngilizceyeYön => türkçeKalanlar; case İngilizcedenTürkçeyeYön => Map.empty[String, Int] }
      if (şüpheli.nonEmpty) {
        sb.append(s"Türkçe kalan ad (${şüpheli.size}) -- sözlükte yok; kullanıcının adı ya da sözlüğün eksiği:\n")
        şüpheli.toSeq.sortBy { case (ad, n) => (-n, ad) }.foreach { case (ad, n) => sb.append(f"  $ad ($n)\n") }
      }
      val kalan = dokunulmayan.size - şüpheli.size
      if (kalan > 0) sb.append(s"dokunulmayan öteki ad: $kalan\n")
      sb.toString
    }
  }

  private val türkçeHarfler = "çğıöşüÇĞİÖŞÜ".toSet
  private def türkçeHarf(c: Char) = türkçeHarfler(c)

  lazy val sözlük: ÇeviriSözlüğü.Sözlük = ÇeviriSözlüğü.yükle()

  /**
   * Anahtar sözcük tablosu: jeton türü -> (İngilizce, Türkçe). dict.keywordTranslation'dan
   * kurulur, ama her çiftin İKİ yazımı da sözcükleyiciden geçirilip aynı jeton
   * türünü verdiği doğrulanır; vermeyenler (scalariform'un tanımadığı Scala 3
   * sözcükleri: given/verilen) tabloya girmez, ceviri-kurallar.tsv'den çevrilir.
   *
   * Ölçüt `isKeyword` DEĞİL: scalariform true/false/null'ı sabit sayıyor
   * (isKeyword=false, isLiteral=true); onunla süzünce doğru/yanlış/yok çevrilmeden
   * kalıyordu (ölçüldü: 39 yerine 36 çift). Ölçüt: iki yazım da tanımlayıcı OLMAYAN
   * aynı jeton türüne gidiyor.
   */
  lazy val anahtarSözcükler: Map[TokenType, (String, String)] = {
    def jetonTürü(w: String): Option[TokenType] =
      ScalaLexer.rawTokenise(w + " ", forgiveErrors = true, scalaVersion = "2.13.0").headOption
        .map(_.tokenType).filterNot(tt => tt.isId || tt == Tokens.EOF)
    dict.keywordTranslation.toSeq.flatMap { case (en, tr) =>
      (jetonTürü(en), jetonTürü(tr)) match {
        case (Some(te), Some(tt)) if te == tt => Some(te -> (en, tr))
        case _                                => None
      }
    }.toMap
  }

  private val yalnızTanımlayıcı = "^[\\p{L}_][\\p{L}\\p{N}_]*$".r
  private lazy val türkçeAnahtarSözcükler: Set[String] = dict.turkishKeywords.toSet
  private val tanımlayanlar: Set[TokenType] = Set(Tokens.DEF, Tokens.VAL, Tokens.VAR, Tokens.CLASS, Tokens.OBJECT, Tokens.TYPE, Tokens.TRAIT)

  def çevir(kod: String, yön: Yön, sözlük: ÇeviriSözlüğü.Sözlük = sözlük): (String, Rapor) = {
    val ts = ScalaLexer.rawTokenise(kod, forgiveErrors = true, scalaVersion = "2.13.0")
    val satırBaşları = 0 +: kod.zipWithIndex.collect { case ('\n', i) => i + 1 }
    def satırNo(t: Token) = satırBaşları.count(_ <= t.offset)

    val çıktı = new StringBuilder(kod.length + kod.length / 8)
    var çevrilen = 0
    val belirsiz = Vector.newBuilder[Belirsiz]
    val dokunulmayan = scala.collection.mutable.Map.empty[String, Int].withDefaultValue(0)
    val jetonlar = ts.toVector
    def anlamlı(t: Token) = t.tokenType != Tokens.WS && !t.tokenType.isComment && !t.tokenType.isNewline && t.tokenType != Tokens.EOF
    def sonrakiAnlamlı(i: Int): Option[Int] = (i + 1 until jetonlar.length).find(j => anlamlı(jetonlar(j)))
    def tanımlayıcı(t: Token) = t.tokenType.isId && yalnızTanımlayıcı.matches(t.text)
    val yönAdı = yön match { case TürkçedenİngilizceyeYön => "tr>en"; case İngilizcedenTürkçeyeYön => "en>tr" }
    def seç(ad: String, bağlam: String) = yön match {
      case TürkçedenİngilizceyeYön => sözlük.türkçedenİngilizceye(ad, bağlam)
      case İngilizcedenTürkçeyeYön => sözlük.ingilizcedenTürkçeye(ad, bağlam)
    }
    // Yazılımcığın KENDİ tanımladığı adlar: def/val/var/class/object/type ile bildirilenler,
    // `ad:` parametreleri ve `x =>` / `(x, y) =>` lambda parametreleri. Bunlar tutarlı biçimde
    // ama hep YALIN çevrilir: tanım yerinde `Picture.image` kısaltılıyordu ama kullanım yeri
    // `Picture.image(Picture.image, ...)` oluyordu (ölçüldü: car-ride.kojo, genart-tri-mesh.kojo).
    val kullanıcıAdları: Set[String] = {
      val anlamlılar = jetonlar.indices.filter(j => anlamlı(jetonlar(j)))
      val küme = Set.newBuilder[String]
      for (k <- anlamlılar.indices) {
        val j = anlamlılar(k); val t = jetonlar(j)
        def sonrakiTür = if (k + 1 < anlamlılar.length) Some(jetonlar(anlamlılar(k + 1)).tokenType) else None
        def öncekiTür = if (k > 0) Some(jetonlar(anlamlılar(k - 1)).tokenType) else None
        if (tanımlayıcı(t) && (öncekiTür.exists(tanımlayanlar) || sonrakiTür.contains(Tokens.COLON) || sonrakiTür.contains(Tokens.ARROW))) küme += t.text
        // `(a, b) =>`: parantez içindeki adlar
        if (t.tokenType == Tokens.LPAREN) {
          var m = k + 1; var adlar = List.empty[String]; var düzgün = true
          while (düzgün && m < anlamlılar.length && jetonlar(anlamlılar(m)).tokenType != Tokens.RPAREN) {
            val u = jetonlar(anlamlılar(m))
            if (tanımlayıcı(u)) adlar ::= u.text else if (u.tokenType != Tokens.COMMA) düzgün = false
            m += 1
          }
          if (düzgün && m + 1 < anlamlılar.length && jetonlar(anlamlılar(m + 1)).tokenType == Tokens.ARROW) adlar.foreach(küme += _)
        }
      }
      küme.result()
    }
    var önceki: Option[Token] = None // boşluk/yorum dışı bir önceki jeton
    var öncekininÖncekisi: Option[Token] = None
    var atla = Set.empty[Int]        // alıcı+nokta+ad birden değiştirilince yutulan jetonlar
    // `collection.mutable.Map`, `java.awt.Color`: paket yolunun üyeleri çevrilmez --
    // `collection.mutable.Eşlek` diye bir şey yok (ölçüldü: car-ride.kojo). Zincir kökü
    // bilinen bir paketse zincir bitene kadar dokunma. `math.` istisna: o çevrilir.
    val paketKökleri = Set("scala", "java", "javax", "collection", "util", "net", "org", "com", "sun")
    // Java/Scala sınıf nesneleri: üyeleri hiçbir dilde çevrilmez (`System.currentTimeMillis`,
    // `Math.floor`, `SwingConstants.CENTER`, `Stream.from`, `util.Random.shuffle`). Sözlük üye
    // adına bakınca `Math.taban`, `SwingConstants.merkez` çıkıyordu (ölçüldü: genart-mondrian,
    // subtraction-game, prime-factors). Character dışarıda: Harf nesnesine kuralla gidiyor.
    val sabitAlıcılar = Set("System", "Math", "SwingConstants", "BorderFactory", "Random", "Stream", "LazyList",
      "Integer", "Double", "Long", "Thread", "Runtime", "Files", "Paths")
    var paketYolunda = false
    // `^Resim.dikdörtgen(2,1)`: alıcılı hedefin ardındaki argüman listesi yeniden sıralanır --
    // Picture.rect(boy, en) = Resim.dikdörtgen(en, boy). Parantez arası çıktı parça parça
    // (üst düzey virgüllerde bölünerek) yakalanır, kapanışta verilen sırayla yazılır.
    final class Yakalama(val kapanış: Int, val virgüller: Set[Int], val sıra: Seq[String]) {
      val parçalar = scala.collection.mutable.ArrayBuffer(new StringBuilder)
    }
    var yakalama: Option[Yakalama] = None
    var yakalamaBaşlat: Option[Yakalama] = None // hedef yazıldıktan SONRA başlar; yoksa hedef ilk parçaya giriyordu
    // Hedefte argüman listesi: `Resim.dikdörtgen(2,1)` argümanları yeniden sıralar,
    // `math.pow(1,'2)` ise ikinciye SABİT yazar. Girdi rakamsa kaçıncı argüman (1'den),
    // `'` ile başlıyorsa gerisi olduğu gibi basılır. Sarmalayıcı argüman ekliyorsa
    // (karesi(x) = math.pow(x, 2)) tek yol budur.
    val sıralıHedef = "^(.*)\\(((?:\\d+|'[^,()]*)(?:,(?:\\d+|'[^,()]*))*)\\)$".r
    def argümanMı(girdi: String) = girdi.nonEmpty && girdi.forall(_.isDigit)
    /** `(` jetonundan eşleşen `)`ye: (kapanış dizini, üst düzey virgül dizinleri). */
    def argümanListesi(açılış: Int): Option[(Int, Set[Int])] = {
      var d = 0; var k = açılış; val virgüller = Set.newBuilder[Int]
      while (k < jetonlar.length) {
        jetonlar(k).tokenType match {
          case Tokens.LPAREN | Tokens.LBRACKET | Tokens.LBRACE => d += 1
          case Tokens.RPAREN | Tokens.RBRACKET | Tokens.RBRACE => d -= 1; if (d == 0) return Some((k, virgüller.result()))
          case Tokens.COMMA if d == 1                          => virgüller += k
          case _                                               =>
        }
        k += 1
      }
      None
    }
    /** Hedefteki argüman listesini uygular: ardındaki `(` ... `)` yakalanır, kapanışta
      * girdilere göre yeniden yazılır. Argüman sayısı uymuyorsa hiçbir şey yapılmaz
      * (ad yine çevrilir, liste olduğu gibi kalır). Tanım bağlamında ÇAĞRILMAZ: orada
      * parantez içi parametre listesidir, argüman değil. */
    def yakalamayıKur(sıra: String, sonJeton: Int): Unit = {
      val sıralama = sıra.split(',').toSeq
      val açılış = sonrakiAnlamlı(sonJeton).filter(p => jetonlar(p).tokenType == Tokens.LPAREN)
      açılış.flatMap(argümanListesi) match {
        case Some((kapanış, virgüller)) if virgüller.size + 1 == sıralama.count(argümanMı) =>
          atla = atla + açılış.get; yakalamaBaşlat = Some(new Yakalama(kapanış, virgüller, sıralama))
        case _ => // argüman sayısı uymuyor: adı çevir, listeye dokunma
      }
    }
    /** Hedefi (ad, argüman listesi) diye ayırır. */
    def hedefiAyır(hedef: String): (String, Option[String]) = hedef match {
      case sıralıHedef(ad, sıra) => (ad, Some(sıra))
      case _                     => (hedef, None)
    }
    // Kullanıcının kendi adı (`case class Rectangle(width: Double)` / `r.width`): tanım yeri
    // yalın, kullanım yeri üye bağlamında bakılınca ikisi ayrı yere düşüyordu (width tanımda
    // kalıyor, kullanımda eni oluyordu -- ölçüldü: genart-mondrian, genart-tiled-lines).
    // Sözlükte YALNIZ BİR bağlamda karşılığı olan kullanıcı adı her yerde o karşılığa gider.
    // İki bağlamda da karşılığı olan ad (yazı, boyu) bağlamına göre çevrilir: kullanıcı adı
    // bir yazılımcık adıyla çakışıyordur, tek karşılığa zorlamak yazılımcık çağrısını bozuyor
    // (ölçüldü: tic-tac-toe Resim.yazı -> Picture.write, tiled-lines ta.boyu -> ta.size).
    lazy val kullanıcıÇevirisi: Map[String, ÇeviriSözlüğü.Seçim] = kullanıcıAdları.iterator.flatMap { ad =>
      (seç(ad, ÇeviriSözlüğü.BağlamYalın), seç(ad, ÇeviriSözlüğü.BağlamÜye)) match {
        case (Some(y), None) => Some(ad -> y)
        case (None, Some(ü)) => Some(ad -> ü)
        case _               => None
      }
    }.toMap

    for (i <- jetonlar.indices) {
      val t = jetonlar(i)
      val tt = t.tokenType
      val metin: String =
        if (atla(i)) ""
        else if (anahtarSözcükler.contains(tt)) {
          val (en, tr) = anahtarSözcükler(tt)
          yön match {
            case TürkçedenİngilizceyeYön => en
            // `val den = 3` (payda): İngilizce yazar için bir AD, ama yamalı sözcükleyici için
            // `var` anahtar sözcüğü. İngilizce betikte Türkçe yazımlı bir anahtar sözcük
            // jetonu ancak yazarın tanımlayıcısı olabilir; ters tırnak Scala'nın çözümü.
            // Olduğu gibi bırakınca çeviri ayrıştırılmıyordu (ölçüldü: solving-linear-equations).
            case İngilizcedenTürkçeyeYön => if (t.text == tr) s"`$tr`" else tr
          }
        }
        else if (tt.isId && !yalnızTanımlayıcı.matches(t.text) && seç(t.text, ÇeviriSözlüğü.BağlamHepsi).isDefined) {
          // `1 |-| 600` -> `1 to 600`: işleç adları yalnız kural dosyasından çevrilir; sözlük
          // tabloları harfli adlarla kurulu, `+` gibi işleçler oraya hiç uğramaz.
          çevrilen += 1; seç(t.text, ÇeviriSözlüğü.BağlamHepsi).get.hedef
        }
        else if (tanımlayıcı(t)) {
          // `Resim.dizi(...)`: Türkçe nesnenin üyesi İngilizce'de yalın bir işlev (picStack).
          // Alıcıya özel kural (`Resim.` bağlamı) hedefi `^picStack` verirse alıcı ve
          // nokta yutulur, üç jeton tek ada iner. Bakış ileriye: alıcıyı yazmadan önce
          // üyeye bakıyoruz, geri almak yok.
          val alıcılıHedef = for {
            n <- sonrakiAnlamlı(i) if jetonlar(n).tokenType == Tokens.DOT
            m <- sonrakiAnlamlı(n) if tanımlayıcı(jetonlar(m))
            s <- seç(jetonlar(m).text, t.text + ".") if s.hedef.startsWith(ÇeviriSözlüğü.AlıcıylaBirlikte)
          } yield (n, m, s.hedef.drop(1))
          val üye = önceki.exists(_.tokenType == Tokens.DOT)
          val üyeKonumu = sonrakiAnlamlı(i).filter(n => jetonlar(n).tokenType == Tokens.DOT).flatMap(sonrakiAnlamlı).filter(m => tanımlayıcı(jetonlar(m)))
          val alıcıOlarak = !üye && üyeKonumu.isDefined
          // `ColorMaker.khaki` (renk adı) ile `ColorMaker.hsla(...)` (yapıcı çağrısı) aynı alıcıyı
          // Türkçe'de iki ayrı nesneye götürür (Renkler / Renk). Ayırt edici: üye parantezle
          // çağrılıyor mu. Bağlam `alıcı(` = çağrılan üyenin alıcısı.
          val çağrılanınAlıcısı = alıcıOlarak && üyeKonumu.flatMap(sonrakiAnlamlı).exists(p => jetonlar(p).tokenType == Tokens.LPAREN)
          if (!üye) paketYolunda = (paketKökleri(t.text) || sabitAlıcılar(t.text)) && alıcıOlarak
          // EN->TR: `ColorMaker.blueViolet` -- alıcı çevrilir (Renkler), üyenin Türkçesi yoksa
          // `Renkler.blueViolet` diye bir şey yok. Koco İngilizce adı olduğu gibi kabul eder;
          // üyesi çevrilemeyen alıcıya dokunma (ölçüldü: flappy-ball, pong, subtraction-game).
          // TR->EN'de anlamsız: alıcı da üye de İngilizce'de olmalı, bırakmak kurtarmaz.
          // Kullanıcının kendi adı (`val square = ...; square.x`) alıcı olsa da tanım yerindeki
          // çevirisini korumalı; yoksa `kare` tanımlanıp `square.x` kalıyordu (ölçüldü: genart-mondrian).
          val üyesizAlıcı = yön == İngilizcedenTürkçeyeYön && alıcıOlarak && !kullanıcıAdları(t.text) && üyeKonumu.exists { m =>
            val üyeAdı = jetonlar(m).text
            sözlük.çevrilmez(yönAdı, üyeAdı, t.text + ".") ||
              (seç(üyeAdı, t.text + ".").isEmpty && seç(üyeAdı, ÇeviriSözlüğü.BağlamÜye).isEmpty)
          }
          if (paketYolunda || üyesizAlıcı) { dokunulmayan(t.text) += 1; t.rawText }
          else alıcılıHedef match {
            case Some((n, m, hedef)) =>
              atla = atla + n + m; çevrilen += 1
              val (ad, sıra) = hedefiAyır(hedef)
              sıra.foreach(yakalamayıKur(_, m))
              ad
            case None =>
              // Sıra: alıcıya özel düz kural (`tuvalAlanı.` -> height), sonra alıcı/üye/yalın bağlamı.
              val alıcıAdı = if (üye) öncekininÖncekisi.filter(tanımlayıcı).map(_.text + ".") else None
              val bağlam = if (üye) ÇeviriSözlüğü.BağlamÜye else if (çağrılanınAlıcısı) ÇeviriSözlüğü.BağlamAlıcıÇağrı else if (alıcıOlarak) ÇeviriSözlüğü.BağlamAlıcı else ÇeviriSözlüğü.BağlamYalın
              // `tanım dereceye(k) = ...`, `tanım araba(imge: Yazı)`: tanımlanan ad nitelenmiş olamaz
              // (`def math.toDegrees`, `araba(Picture.image: String)` ayrıştırılmaz -- ölçüldü:
              // angles.kojo, car-ride.kojo). Önünde def/val/... ya da ardında `:` varsa tanım bağlamı;
              // hedefin son parçası alınır.
              val tanımBağlamı = kullanıcıAdları(t.text) || önceki.exists(p => tanımlayanlar(p.tokenType)) ||
                sonrakiAnlamlı(i).exists(n => jetonlar(n).tokenType == Tokens.COLON)
              // `SwingConstants.CENTER` için "-" kuralı: alıcıya özel "çevirme" üye bağlamına
              // düşmez (düşünce `SwingConstants.merkez` oluyordu -- ölçüldü: subtraction-game).
              val alıcıyaÖzelÇevirme = alıcıAdı.exists(a => sözlük.çevrilmez(yönAdı, t.text, a))
              val kullanıcıTanımlı = kullanıcıÇevirisi.contains(t.text)
              // `engeller herbiriİçin { ... }`, `xs foreach f`: sonek çağrı, önünde nokta yok ama üye.
              // Yalın karşılığı olmayan ad, önündeki jeton bir değerse (ad, sabit, `)`, `]`) üye
              // bağlamında da aranır (ölçüldü: flappy-ball.kojo).
              val sonekKonumu = !üye && !alıcıOlarak && önceki.exists(p => p.tokenType.isId || p.tokenType.isLiteral ||
                p.tokenType == Tokens.RPAREN || p.tokenType == Tokens.RBRACKET)
              val seçim =
                if (kullanıcıTanımlı) Some(kullanıcıÇevirisi(t.text))
                // Açık "-" kuralı (vertex yalın, setColumns yalın) sonek düşüşünü de keser (ölçüldü: tree2).
                else if (alıcıyaÖzelÇevirme || sözlük.çevrilmez(yönAdı, t.text, bağlam)) None
                else alıcıAdı.flatMap(a => seç(t.text, a)).orElse(seç(t.text, bağlam))
                  .orElse(if (sonekKonumu) seç(t.text, ÇeviriSözlüğü.BağlamÜye) else None)
              seçim match {
                case Some(s) =>
                  çevrilen += 1
                  if (!s.kesin) belirsiz += Belirsiz(t.text, s.hedef, s.alternatifler, satırNo(t))
                  // `biçimleriBelleğeYaz()` -> `saveStyle`: İngilizce ad parantezsiz (def saveStyle: Unit),
                  // TR->EN'de ardındaki boş `()` yutulur. Ters yönde `()` EKLENMEZ: Scala 2'de
                  // `yazı()` yerine `yazı` yazmak geçerli, ama `toString` gibi Java yöntemleri de
                  // "parantezsiz" işaretlendiğinden ekleme parametre listelerini bozuyordu (ölçüldü:
                  // genart-tri-mesh.kojo, "not a legal formal parameter").
                  val boşParantez = for { n <- sonrakiAnlamlı(i) if jetonlar(n).tokenType == Tokens.LPAREN
                                          m <- sonrakiAnlamlı(n) if jetonlar(m).tokenType == Tokens.RPAREN } yield (n, m)
                  if (s.parantezsiz && !tanımBağlamı && yön == TürkçedenİngilizceyeYön) boşParantez.foreach { case (n, m) => atla = atla + n + m }
                  val parantezEki = ""
                  // Ad ile argüman listesini önce ayır: sabitte nokta olabilir ('0.5), son
                  // parçayı almadan ayırmazsak substring listenin içine düşer.
                  val (hedefAdı, argSırası) = hedefiAyır(s.hedef)
                  // Tanım bağlamında parantez içi PARAMETRE listesidir; yakalama kurulursa
                  // `tanım karesi(x: Kesir)` -> `def pow(x: Kesir, 2)` olurdu.
                  if (!tanımBağlamı) argSırası.foreach(yakalamayıKur(_, i))
                  // Üye bağlamında da: `Matematik.karekökü` -> alıcı zaten `math`, üye `sqrt`.
                  (if (tanımBağlamı || üye) hedefAdı.substring(hedefAdı.lastIndexOf('.') + 1) else hedefAdı) + parantezEki
                case None =>
                  dokunulmayan(t.text) += 1
                  // Sözcükleyicinin anahtar sözcük saymadığı Türkçe sözcük (verilen) de ad olarak
                  // kullanılmış olabilir; o da ters tırnağa.
                  if (yön == İngilizcedenTürkçeyeYön && türkçeAnahtarSözcükler(t.text)) s"`${t.text}`" else t.rawText
              }
          }
        }
        else t.rawText
      yakalama match {
        case Some(y) if i == y.kapanış =>
          val parçalar = y.parçalar.map(_.toString.trim)
          val yazılan = y.sıra.map(g => if (argümanMı(g)) parçalar(g.toInt - 1) else g.substring(1))
          çıktı.append("(").append(yazılan.mkString(", ")).append(")"); yakalama = None
        case Some(y) if y.virgüller(i) => y.parçalar += new StringBuilder
        case Some(y)                   => y.parçalar.last.append(metin)
        case None                      => çıktı.append(metin)
      }
      if (yakalamaBaşlat.isDefined) { yakalama = yakalamaBaşlat; yakalamaBaşlat = None }
      if (anlamlı(t)) { öncekininÖncekisi = önceki; önceki = Some(t) }
    }
    val bilinen = yön match {
      case TürkçedenİngilizceyeYön => dokunulmayan.keySet.filter(sözlük.türkçeAdlar).toSet
      case İngilizcedenTürkçeyeYön => Set.empty[String]
    }
    (çıktı.toString, Rapor(çevrilen, belirsiz.result(), dokunulmayan.toMap, bilinen))
  }

  def türkçedenİngilizceye(kod: String): (String, Rapor) = çevir(kod, TürkçedenİngilizceyeYön)
  def ingilizcedenTürkçeye(kod: String): (String, Rapor) = çevir(kod, İngilizcedenTürkçeyeYön)

  /** Yazılımcıkta kaynak dilin anahtar sözcüğü kalmış mı? Çevirinin en ucuz sağlaması. */
  def kalanAnahtarSözcükler(kod: String, yön: Yön): Seq[String] = {
    val yasak: Set[String] = yön match {
      case TürkçedenİngilizceyeYön => anahtarSözcükler.values.map(_._2).toSet
      case İngilizcedenTürkçeyeYön => anahtarSözcükler.values.map(_._1).toSet
    }
    ScalaLexer.rawTokenise(kod, forgiveErrors = true, scalaVersion = "2.13.0")
      .filter(t => anahtarSözcükler.contains(t.tokenType) && yasak(t.text)).map(_.text).distinct
  }
}

/**
 * Komut satırı:
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.CevirmenMain --tr2en betik.kojo [-o cikti.kojo] [--dogrula]"
 *   ./sbt.sh "runMain net.kogics.kojo.lite.i18n.tr.CevirmenMain --en2tr script.kojo [-o cikti.kojo] [--dogrula]"
 *
 * Çıktı dosyaya (-o) ya da standart çıktıya; rapor her zaman standart hataya.
 * --dogrula: çıktıyı hedef dilin Kojo prelude'üyle sarıp tür denetiminden geçirir
 * (ÇeviriDoğrulama); hata varsa asıl betiğin satır numarasıyla basar, çıkış kodu 1.
 * Bu, "çevrildi" ile "çalışır" arasındaki farkı kapatan adımdır; sözlüğün
 * taşıyamadığı her şey (bkz. Çevirmen SINIRLAR) burada görünür.
 *
 * Sınıf adı ve bayraklar bilerek ASCII: sbt'nin runMain'ine kabuktan Türkçe
 * harf geçirmek platforma göre sorun çıkarıyor.
 */
object CevirmenMain {
  private val kullanım =
    """kullanım: CevirmenMain (--tr2en | --en2tr) <girdi.kojo> [-o <çıktı.kojo>] [--dogrula]
      |  --tr2en    Türkçe (Koco) yazılımcığı İngilizce Kojo'ya çevir
      |  --en2tr    İngilizce Kojo yazılımcığını Türkçe Koco'ya çevir
      |  --dogrula  çıktıyı hedef dilin derleyicisiyle tür denetiminden geçir
      |""".stripMargin

  def main(args: Array[String]): Unit = {
    val doğrula = args.contains("--dogrula")
    val (yön, kalan) = args.toList.filterNot(_ == "--dogrula") match {
      case "--tr2en" :: k => (Çevirmen.TürkçedenİngilizceyeYön, k)
      case "--en2tr" :: k => (Çevirmen.İngilizcedenTürkçeyeYön, k)
      case _              => System.err.println(kullanım); sys.exit(2)
    }
    val (girdi, çıktıDosyası) = kalan match {
      case g :: "-o" :: ç :: Nil => (g, Some(ç))
      case g :: Nil              => (g, None)
      case _                     => System.err.println(kullanım); sys.exit(2)
    }
    val kod = SözlükÜreteci.oku(new File(girdi))
    val (çıktı, rapor) = Çevirmen.çevir(kod, yön)
    // Rapor: çeviri stdout'a gidiyorsa stderr'e (boru hattı temiz kalsın), dosyaya gidiyorsa
    // stdout'a -- sbt altında stderr her satırı `[error]` diye gösteriyor, başarı iletisi
    // hata gibi okunuyordu (inceleme #62).
    val raporla: String => Unit = if (çıktıDosyası.isDefined) println(_) else System.err.println(_)
    çıktıDosyası match {
      case Some(ç) => java.nio.file.Files.write(new File(ç).toPath, çıktı.getBytes("UTF-8")); raporla(s"yazıldı: $ç")
      case None    => print(çıktı)
    }
    rapor.özet(yön).linesIterator.foreach(raporla)
    var kusurlu = false
    val kalanlar = Çevirmen.kalanAnahtarSözcükler(çıktı, yön)
    if (kalanlar.nonEmpty) { raporla(s"UYARI: kaynak dilin anahtar sözcüğü kaldı: ${kalanlar.mkString(", ")}"); kusurlu = true }
    if (doğrula) {
      val türkçeHedef = yön == Çevirmen.İngilizcedenTürkçeyeYön
      val hatalar = ÇeviriDoğrulama.türDenetimi(new File(girdi).getName, çıktı, türkçeHedef)
      if (hatalar.isEmpty) raporla("doğrulama: çeviri " + (if (türkçeHedef) "Türkçe" else "İngilizce") + " Kojo prelude'üyle tür denetiminden geçti")
      else { raporla(s"doğrulama: ${hatalar.size} hata"); hatalar.foreach(h => raporla("  " + h)); kusurlu = true }
    }
    if (kusurlu) sys.exit(1)
  }
}
