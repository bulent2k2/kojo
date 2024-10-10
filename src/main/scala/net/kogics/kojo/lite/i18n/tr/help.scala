/*
 * Copyright (C) 2021 June
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

object help {
  // ../../../xscala/CodeTemplates.scala
  val templates = Map(
    "rastgeleİkil" -> "rastgeleİkil",
    "englishTurtle" -> "englishTurtle",
    "yeniKaplumbağa" -> "yeniKaplumbağa(${x},${y},${kılık})",
    "a_kalıp" -> "a_kalıp()",
    "ileri" -> "ileri(${adım})",
    "geri" -> "geri(${adım})",
    "sağ" -> "sağ(${açı},${yarıçap})",
    "sol" -> "sol(${açı})",
    "sol" -> "sol(${açı},${yarıçap})",
    "atla" -> "atla(${x},${y})",
    "ilerle" -> "ilerle(${x},${y})",
    "zıpla" -> "zıpla(${adım})",
    "ev" -> "ev()",
    "noktayaDön" -> "noktayaDön(${x},${y})",
    "açıyaDön" -> "açıyaDön(${açı})",
    "doğrultu" -> "doğrultu()",
    "doğu" -> "doğu()",
    "batı" -> "batı()",
    "kuzey" -> "kuzey()",
    "güney" -> "güney()",
    "canlandırmaHızınıKur" -> "canlandırmaHızınıKur(${milisaniye})",
    "yazı" -> "yazı(${yazı})",
    "yazıBoyunuKur" -> "yazıBoyunuKur(${boyutKur})",
    "yay" -> "yay(${yarıçap},${açı})",
    "daire" -> "daire(${yarıçap})",
    "görünür" -> "görünür()",
    "görünmez" -> "görünmez()",
    "kalemiİndir" -> "kalemiİndir()",
    "kalemiKaldır" -> "kalemiKaldır()",
    "kalemİnikMi" -> "kalemİnikMi",
    "kalemRenginiKur" -> "kalemRenginiKur(${renk})",
    "boyamaRenginiKur" -> "boyamaRenginiKur(${renk})",
    "kalemKalınlığınıKur" -> "kalemKalınlığınıKur(${en})",
    "biçimleriBelleğeYaz" -> "biçimleriBelleğeYaz()",
    "biçimleriGeriYükle" -> "biçimleriGeriYükle()",
    "konumVeYönüBelleğeYaz" -> "konumVeYönüBelleğeYaz()",
    "konumVeYönüGeriYükle" -> "konumVeYönüGeriYükle()",
    "ışınlarıAç" -> "ışınlarıAç()",
    "ışınlarıKapat" -> "ışınlarıKapat()",
    "sil" -> "sil()",
    "çıktıyıSil" -> "çıktıyıSil()",
    "silVeSakla" -> "silVeSakla()",
    "silipSakla" -> "silipSakla()",
    "çizimiSil" -> "çizimiSil()",
    "artalanıKur" -> "artalanıKur(${renk})",
    "artalanıKurDik" -> "artalanıKurDik(${renk1},${renk2})",
    "artalanıKurYatay" -> "artalanıKurYatay(${renk1},${renk2})",
    "konum" -> "konum",
    "yinele" -> "yinele(${sayı}) {\n    ${cursor}\n}",
    "yineleDizinli" -> "yineleDizinli(${sayı}) { i =>\n    ${cursor}\n}",
    "yineleDoğruysa" -> "yineleDoğruysa(${koşul}) {\n    ${cursor}\n}",
    "yineleOlanaKadar" -> "yineleOlanaKadar(${koşul}) {\n    ${cursor}\n}",
    "yineleKere" -> "yineleKere(${dizi}) { ${e} =>\n    ${cursor}\n}",
    "yineleİçin" -> "yineleİçin(${dizi}) { ${e} =>\n    ${cursor}\n}",
    "yineleİlktenSona" -> "yineleİlktenSona(${ilk},${son}) { s => \n    ${cursor}\n}",
    "satıryaz" -> "satıryaz(${yazı})",
    "satıroku" -> "satıroku(${istem})",
    "yuvarla" -> "yuvarla(${sayı},${basamaklar})",
    "rastgele" -> "rastgele(${üstSınır})",
    "rastgeleKesir" -> "rastgeleKesir(${üstSınır})",
    "giysiKur" -> "giysiKur(${dostaAdı})",
    "giysileriKur" -> "giysileriKur(${dostaAdı1},${dostaAdı2})",
    "birsonrakiGiysi" -> "birsonrakiGiysi()",
    "buAn" -> "buAn()",
    "buSaniye" -> "buSaniye()",
    "hızıKur" -> "hızıKur(${hız})",
    "eksenler" -> "eksenler",
    "götür" -> "götür(${x}, ${y})",
    "döndür" -> "döndür(${açı})",
    "döndürMerkezli" -> "döndürMerkezli(${açı}, ${x}, ${y})",
    "büyüt" -> "büyüt(${oran})",
    "büyütXY" -> "büyütXY(${xOranı}, ${yOranı})",
    "boyaRengi" -> "döndür(${boya})",
    "Nokta" -> "Nokta(${x}, ${y})",
    "Aralık" -> "Aralık(${ilki}, ${sonuncu}, ${adım})",
    "yazıTamamlamaSeçenekleriniYazdırmayıAçKapa" -> "yazıTamamlamaSeçenekleriniYazdırmayıAçKapa()",
    "RenkKYM" -> "RenkKYM(${kırmızı}, ${yeşil}, ${mavi}, ${saydamlıkDerecesi})",
    "RenkADA" -> "RenkADA(${arıRenk}, ${doygunluk}, ${aydınlıkAçıklık})", 
    "RenkDD" -> "RenkDD(${x1}, ${y1}, ${renk1}, ${x2}, ${y2}, ${renk2}, ${dönüşlü})", 
    "Yazıyüzü" -> "Yazıyüzü(${adı}, ${boyu})",

    // todo more
  )

  // NOTE: We can't use less than operator! < is meaningful to the xml/html stuff! Instead, use &lt;
  // https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
  val content = Map(
    "a_kalıp" -> <div>
      <strong>komut</strong>(g1, g2) - Açıklama ... <br/>
      Daha çok açıklama ... <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>

      dez x = komut
      x.yöntem

      </pre> Bu örnekten sonra açıklama ...
      </div>.toString,

    "yazıTamamlamaSeçenekleriniYazdırmayıAçKapa" -> <div>
      Yazı tamamlama seçeneklerini çıktı penceresine yazdırmak için bu komudu kullanabilirsin. Yazdırma açıksa, tekrar çağırırarak yazdırmayı kapatabilirsin. <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>

      dez piSayısınınBaşı = Dizi(3, 1, 4, 1, 5)
      yazıTamamlamaSeçenekleriniYazdırmayıAçKapa()
      // şimdi Ctrl-return'le kodu çalıştır
      // Sonra, aşağıya piS yaz ve Ctrl-boşluk'la yazı tamamlama özelliğini kullan

      </pre> Tekrar tekrar Ctrl-return ile bu komudu çalıştırarak yazı tamamlamalarının çıktı penceresine yazdırılmalarını kapatıp açabilirsin.
      </div>.toString,

    "dez" -> <div>
      <strong>dez</strong> - Değişmez bir değere ad takar. Bu sayede yazılım daha anlaşılır olur. Ayrıca sonradan değiştirmek kolaylaşır. <br/> <br/>
      <em>Örnek:</em> <br/> <br/> <pre>
      sil()
      dez adım = 20 // Bunu 10 ya da 30 yapıp tekrar çalıştırabilirsin
      daire(adım)
      yinele(4) {{
        ileri(adım)
        sol()
        ileri(adım)
      }}
      </pre>
      </div>.toString,

    "den" -> <div>
      <strong>den</strong> - Bir değişken tanımlar ve ona bir değer takar. Değişkenin değerini daha sonra değiştirebilirsin. <br/> <br/>
      <em>Örnek:</em> <br/> <br/> <pre>
      çıktıyıSil
      den x = 3
      x = x + 4
      satıryaz(x)
      x = x * 6
      satıryaz(x)
      </pre>
      </div>.toString,

    "tanım" -> <div>
      <strong>tanım</strong> - Yeni bir komut ya da yeni bir işlev tanımlar. <br/> <br/>
      <em>Örnek 1:</em> <br/> <br/> <pre>
      // Yeni bir komut yazalım. Adı kare olsun.
      // Bir tane de girdisi olsun. Girdinin adı da
      // kenar olsun ve kenarın uzunluğunu versin
      tanım kare(kenar: Sayı) {{
        yinele(4) {{
          ileri(kenar)
          sağ()
        }}
      }}
      sil()
      hızıKur(orta)

      // komudumuzu iki kere çağıralım
      kare(100)
      kare(200)
      // Komutların bir "yan etkisi" olur, ama çıktısı olmaz.
      // Bu komutun yan etkisi de tuvalimize bir kare çizmek.
      // Çıktısı olmadığını şöyle de açık açık yazabilirdik:
      tanım kare2(kenar: Sayı): Birim = {{
        yinele(4) {{
          ileri(kenar)
          sağ()
        }}
      }}
      // Birim türünün amacı çıktı olmadığını belirtmek.
      kalemRenginiKur(mavi)
      kare2(80)
      kare2(120)
      </pre><br/>
      <em>Örnek 2:</em> <br/> <br/> <pre>
      // topla adında bir işlev yazalım (fonksiyon da denir)
      // İki girdisi bir de çıktısı var
      tanım topla(s1: Sayı, s2: Sayı) =
        s1 + s2

      çıktıyıSil()
      // işlevimizi satıryaz komudu içinden çağıralım
      satıryaz(topla(3, 5))
      // başka bir toplam bulmak istersek yine çağırabiliriz
      satıryaz(topla(20, 7))
      // yine çağıralım ama bu sefer çıktısına değişmez
      // bir ad takalım
      dez çıktı = topla(23, 19)
      // Kaplumbağaya yazdıralım
      kalemRenginiKur(mor)
      zıpla(-50)
      yazı(çıktı)
      gizle

      // Scala derleyicisi, işlevlerin çıktısının türünü genellikle belirler.
      // Ama yine de açık açık yazmakta da fayda var:
      tanım topla2(s1: Sayı, s2: Sayı): Sayı = s1 + s2
      belirt(topla(-5, 15) == topla2(-5, 15), "iki tanım eşit olmalı")

      </pre><br/>
      <em>Örnek 3:</em> <br/> <br/> <pre>
      // köşegen adında bir işlev tanımlayalım
      // eni ve boyu verilen bir dikdörtenin (ya da karenin) köşegen uzunluğunu bulsun
      tanım köşegen(en: Kesir, boy: Kesir): Kesir = karekökü(karesi(en) + karesi(boy))
      // Çıktının türünü de açıkca yazdık. Gerek yoktu ama yine de iyi bir alışkanlık bu
      satıryaz(köşegen(1, 1)) // birim karenin köşegeni bize 2'nin karekökünü verir
      satıryaz(köşegen(3, 4)) // çok meşhur bir dik üçgen
      satıryaz(köşegen(5, 12)) // bu da kenarları tam sayı olan başka bir dik üçgen

      </pre><br/>
      <em>Örnek 4:</em> <br/> <br/> <pre>
      // açı adında bir komut tanımlayalım
      // kaplumbağaya renkli bir açı çizdirsin
      tanım açı(açı: Kesir = 90, renk: Renk = kırmızı, boy: Sayı = 150): Birim = {{
        kalemRenginiKur(renk)
        ileri(boy)
        geri(boy)
        sol(açı)
        kalemRenginiKur(renk)
        ileri(boy)
        yazı(açı)
        geri(boy)
        sağ(açı)
      }}

      // Ufak birkaç ayarlama
      zıpla(50)
      sol; zıpla; sağ
      hızıKur(orta)
      kalemKalınlığınıKur(1)

      açı() // varsayılan açı 90 derece yani dik açı
      açı(60, yeşil, 200)
      açı(45, mavi, 175)
      açı(30) // varsayılan renk kırmızı

      </pre>
      </div>.toString,

    "için" -> <div> Çok faydalı bir anahtar sözcük. İki değişik kullanımı vardır: <br/>
      <br/>

      1- [komutlarla]:<br/>
    <br/>

      <strong>için</strong> (sayı &lt;- 1 |-| n) {{ komutlar }} - komutları n kere yineler. Bu arada 
       sayı da 1'den n'ye kadar değişir ve komutlar tarafından kullanılabilir <br/>
      <br/>

    <pre>
      silipSakla
      hızıKur(hızlı)
      kalemKalınlığınıKur(5)
      <strong>için</strong> (sayı &lt;- 1 |-| 10) {{
          dez yarıçap = 19 + sayı * 10
          kalemRenginiKur(rastgeleRenk)
          kalemiKaldır
          ileri(sayı * 2.2); sol; ileri(10); sağ
          kalemiİndir
          daire(yarıçap)
      }}
    </pre>

      2- [deyişlerle]:<br/>
    <br/>

      <strong>için</strong> (sayı &lt;- 1 |-| n) ver {{ deyiş }} - Her yinelemede verilen deyişi ve sayının o andaki değerini kullanarak 
        bir öge oluşturur ve bunların hepsini bir arada çıktı olarak verir. <br/>
      <br/>

    <pre>
      çıktıyıSil
      dez dizi = <strong>için</strong> (sayı &lt;- 0 |- 21) ver (sayı * sayı)
      satıryaz(dizi)
      yoksa satıryaz("ilk 20 kare:" ++ dizi.yazıYap("{{", " ", "}}"))      
    </pre>

    </div>.toString,

    "eğer" -> <div>
      <strong>eğer</strong>(koşul) - Programın işleyişinde bir karar aşaması tanımlar.
      <br/> Arkasından <strong>yoksa</strong> da gelebilir.
      <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>
      çıktıyıSil
      dez koşulSağlandıMı = rastgeleİkil
      eğer (koşulSağlandıMı) satıryaz("Koşul sağlandı")
      yoksa satıryaz("Hayır, koşul sağlanmadı")
      satıryaz("\nKontrolla beraber return tuşuna basarak tekrar tekrar çalıştır")
      </pre> <strong>rastgeleİkil</strong> komudu yazı tura atmaya benzer. Yüzde elli ihtimalle doğru yüzde elli ihtimalle de yanlış çıkar.
      <strong>yoksa</strong> anahtar sözcüğüne de bakın.

    <br/>
    <br/> <em>Büyük örnek:</em> <br/>    <br/>
      Bak yazılım nasıl ciddi bir iştir ve dikkatsizliğe gelmez, onu da burada görelim. 
      Her türlü olasılığı değerlendirmek epey emek işi. Sevmeden olmaz.

    <pre>

      çıktıyıSil
      dez yazı = satıroku("10 ile 20 arasında bir sayı girer misin?")
      // hemen alttaki kodu şimdilik atla ve
      // eğer'le başlayan satırdan itibaren okumaya devam et.
      // Girdinin yazı olmaması durumu idare etmemiz için gerekli. Ama
      // henüz bilmediğin birkaç anahtar sözcük ve Belki türünü kullanıyor.

      dez girdi = yazı.sayıyaBelki eşle {{
        durum Biri(sayı) => sayı
        durum Hiçbiri    => 0
      }}

      // Buradan itibaren okumaya devam!
      eğer (girdi == 0)
          eğer (yazı.boşMu)
            satıryaz("Oyun bozancılık yaptın. Hiçbir şey girmedin.")
          yoksa eğer (yazı == "0") satıryaz("Oyun bozancılık yaptın ve 0 girdin!")
          yoksa satıryaz("Oyun bozancılık yaptın. Sayı girmedin.")
      yoksa {{
          eğer (girdi > 15) satıryaz("15'ten büyük bir sayı seçtin")
          eğer (girdi &lt; 15) satıryaz("15'ten küçük bir sayı seçtin")
          eğer (girdi == 15) satıryaz("15'i seçtin. Bak şu tesadüfe!")
          yoksa eğer (girdi > 20 || girdi &lt; 10)
            satıryaz("Neden 10 ile 20 arasında bir sayı girmedin?")
      }} // birden çok komut olunca burada gerekli oldu kıvırcık parantezler.

      satıryaz("\nKontrolla return tuşuna basıp tekrar oynayabilirsin")
      yazılımcıkDüzenleyicisiniEtkinleştir()

      </pre>
      </div>.toString,


    "rastgeleİkil" -> <div>
      <strong>rastgeleİkil</strong> - Girdi almayan bir komut. Çıktısının türü İkil, değeri de ya yanlış ya da doğru olur. İki seçeneğin de olasılığı yüzde ellidir.
      <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>
      çıktıyıSil
      dez yirmiTane = (1 |-| 10).işle(s => (s, rastgeleİkil))
      yirmiTane.herbiriİçin(satıryaz)

      // Bir örnek daha. doğru tura, yanlış da yazı olsun.
      // Yüz kere para atsak, kaç tanesi tura gelir acaba?
      silipSakla
      dez say = (1 |-| 100).ele(s => rastgeleİkil == doğru).boyu
      yazı(say)

      </pre>
      </div>.toString,


    "yoksa" -> <div>
      <strong>yoksa</strong> - Sadece <strong>eğer</strong> anahtar sözcüğünden sonra kullanılır. Koşul sağlanmazsa etkinleşir.
      <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>
      çıktıyıSil
      silipSakla
      dez para = rastgeleİkil
      dez iddia = eğer (para) "Tura" yoksa "Yazı"
      yazı(iddia ++ " geldi!")
      satıryaz("Ctrl-return ile tekrar tekrar çalıştır")
      </pre>
      </div>.toString,


    "englishTurtle" ->
      <div>
      <strong>englishTurtle</strong><br/><br/>
      Bu komut sadece İngilizce komutları bilen bir kaplumbağacık verir. Bu kaplumbağanın türü
      <tt>Turtle</tt>'dır. Türkçe komutları anlamaz. Türkçe bilen kaplumbağanın türü ise <tt>Kaplumbağa</tt>'dır.
      <br/>Bu 'Turtle', ki kaplumbağa demek, İngilizce komutları belirlemek ve kullanmak istenirse faydalı olabilir.
      <br/><em>Örnek:</em> <br/>
      <pre>
      val ingilizceAnlayanKaplumbağa = englishTurtle
      repeat(4){{
        ingilizceAnlayanKaplumbağa.forward(100)
        ingilizceAnlayanKaplumbağa.right()
      }}
      </pre>
      Bu örnek İngilizce komutlarla bir kare çizer.
      <br/><br/>
      yeniKaplumbağa komutuna da bir bakıver.
      </div>.toString,


    "yeniKaplumbağa" ->
      <div>
      <strong>yeniKaplumbağa</strong>(x, y, kılık) - Bu komut x,y noktasında yeni bir kaplumbağa oluşturur ve verilmişse ona bır kılık giydirir. Kılık verilmemişse kaplumbağa olarak çizer. Onun için de adı yeniKaplumbağa!
      <pre>
      val araba = yeniKaplumbağa(100, 200, Görünüş.araba)
      araba.canlandırmaHızınıKur(100)
      araba.kalemRenginiKur(mavi)
      araba.ilerle(0,0)
      araba.kuzey()
      </pre>Bu örnek bir araba oluşturur, (100, 200) noktasından (0, 0) noktasına hızlıca giderek mavi bir doğru parçası, yani bir çizgi çizer.
      </div>.toString,

    "ileri" -> <div><strong>ileri</strong>(adımSayısı) - Bu komut kaplumbağaya verilen sayı kadar adım atırarak baktığı doğrultuda ilerletir. Adım sayısı verilmemişse 25 adım atar.</div>.toString,

    "geri" -> <div><strong>geri</strong>(adımSayısı) - ileri komutunun tersi. Kaplumbağayı verilen sayı kadar geri götürür. Adım sayısı verilmemişse 25 adım atar.</div>.toString,

    "sol" -> <div>
      <strong>sol</strong>() - Bu komut kaplumbağayı olduğu yerde sola doğru (saat yönünün tersine doğru) 90 derece döndürür. <br/>
      <strong>sol</strong>(derece) - Bu komut kaplumbağayı olduğu yerde sola doğru (saat yönünün tersine) verilen derece kadar döndürür. <br/>
      <strong>sol</strong>(derece, yarıçap) - Bu komut kaplumbağayı verilen yarıçaplı bir yay üzerinde sola doğru (saat yönünün tersine doğru) verilen derece kadar döndürerek ilerletir. <br/> </div>.toString,

    "sağ" -> <div> <strong>sağ</strong>() - Bu komut kaplumbağayı sağa doğru (saat yönününde) 90 derece döndürür. <br/> <strong>sağ</strong>(derece) - Bu komut kaplumbağayı sağa doğru (saat yönünde) verilen derece kadar döndürür. <br/> <strong>sağ</strong>(derece, yarıçap) - Bu komut kaplumbağayı verilen yarıçaplı bir yay üzerinde sağa doğru (saat yönünde) verilen derece kadar döndürerek ilerletir. <br/> </div>.toString,
    "atla" -> <div> <strong>atla</strong>(x, y) - Bu komut kaplumbağayı çizgi çizmeden (x, y) noktasına götürür. Kaplumbağanın yönü değişmez. <br/> </div>.toString,

    "ilerle" -> <div><strong>ilerle</strong>(x, y) - Bu komut kaplumbağanın yönünü (x, y) noktasına çevirir ve o noktaya kadar götürür. </div>.toString,

    "zıpla" -> <div> <strong>zıpla</strong>(adımSayısı) - Bu komut <em>kalemi kaldırıp</em> kaplumbağayı verilen adım kadar ilerletir, böylece çizgi çizilmemiş olur. Sonra da kalemi indirir ki arkadan gelen komutlar çizmeye devam etsin. <br/> </div>.toString,

    "ev" -> <div><strong>ev</strong>() - Bu komut kaplumbağayı başlangıç noktasına götürür ve yönünü kuzeye çevirir. </div>.toString,

    "noktayaDön" -> <div>
      <strong>noktayaDön</strong>(x, y) - Bu komut kaplumbağayı (x, y) noktasına çevirir. <br/>
      <strong>noktayaDön</strong>(nokta) - Bu komut kaplumbağayı verilen noktaya çevirir. <br/>
      <br/><em>Örnek:</em> <br/>      <pre>
      dez n = Nokta(100, 100)
      noktayaDön(n)
      </pre>
      </div>.toString,

    "açıyaDön" -> <div><strong>açıyaDön</strong>(angle) - Bu komut kaplumbağayı verilen açıya çevirir. (0 derece ekranın sağına bakar (<em>doğu</em>), 90 yukarı (<em>kuzey</em>)).</div>.toString,

    "doğrultu" -> <div><strong>doğrultu</strong> - Bu komut kaplumbağanın yönünü bildirir. (0 derece ekranın sağına bakar (<em>doğu</em>), 90 yukarı (<em>kuzey</em>)).</div>.toString,

    "Nokta" -> <div><strong>Nokta(x, y)</strong>() - Yeni bir nokta tanımlar. </div>.toString,

    "Aralık" -> <div><strong>Aralık(ilki, sonuncu, adım)</strong> - Yeni bir aralık tanımlar. <br/>
      <br/><em>Örnek:</em> <br/>      <pre>
    Aralık(1, 100, 7).herbiriİçin {{ x =>
        dez kare = x*x
        satıryaz(x, kare, kare*x, kare*kare)
    }} </pre>
 </div>.toString,

    "doğu" -> <div><strong>doğu</strong>() - Bu komut kaplumbağayı doğuya çevirir. </div>.toString,
    "batı" -> <div><strong>batı</strong>() - Bu komut kaplumbağayı batıya çevirir. </div>.toString,
    "kuzey" -> <div><strong>kuzey</strong>() - Bu komut kaplumbağayı kuzeye çevirir. </div>.toString,
    "güney" -> <div><strong>güney</strong>() - Bu komut kaplumbağayı güneye çevirir. </div>.toString,

    "canlandırmaHızınıKur" -> <div> <strong>canlandırmaHızınıKur</strong>(süre) - Bu komut kaplumbağanın hızını belirler. Verilen süre milisaniye olarak kaplumbağanın yüz adım atması için gereken süreyi belirler.<br/> Başlangıç değeri 1000 milisaniye yani 1 saniyedir.<br/> </div>.toString,

    "yazı" -> <div><strong>yazı</strong>(nesne) - Bu komut kaplumbağanın durduğu yere verilen nesnenin yazı olarak karşılığını yazar. <br/>
      <tt>tuvaleYaz</tt> komutuyla eştir.</div>.toString,

    "yazıBoyunuKur" -> <div><strong>yazıBoyunuKur</strong>(sayı) - Bu komut kaplumbağanın yazı tipinin boyunu belirler. </div>.toString,

    "yay" -> <div> <strong>yay</strong>(yarıçap, açı) - Bu komut kaplumbağaya verilen yarıçaplı dairenin verilen açı büyüklüğündeki yayını çizdirir. <br/> Artı açılar sola doğru (saat yönünün tersine), eksi açılar da sağa doğru (saat yönünde) çizilir. <br/> </div>.toString,

    "daire" -> <div> <strong>daire</strong>(yarıçap) - Bu komut kaplumbağaya yarıçapı verilen daireyi çizdirir. <br/> <tt>daire(50)</tt> komutu <tt>yay(50, 360)</tt> komutuyla aynı işleve sahiptir (yani aynı işi yapar!).<br/> </div>.toString,

    "görünür" -> <div><strong>görünür</strong>() - Bu komut <tt>görünmez()</tt> komutuyla saklanan kaplumbağayı tekrar ortaya çıkarır. </div>.toString,

    "görünmez" -> <div><strong>görünmez</strong>() - Bu komut kaplumbağayı görünmez kılar. Kaplumbağamızı <tt>görünür()</tt> komutuyla tekrar ortaya çıkarabilirsiniz.</div>.toString,

    "kalemiİndir" -> <div> <strong>kalemiİndir</strong>() - Bu komut kaplumbağanın kalemini indirerek sonraki komutlarla ilerlediğinde çizgi çizmesini sağlar.<br/> Başlangıçta kalem inik durumdadır. br/> </div>.toString,

    "kalemiKaldır" -> <div><strong>kalemiKaldır</strong>() - Bu komut kaplumbağanın kalemini kaldır. Kaplumbağa bundan sonra ilerlerken çizgi çizmez. <br/></div>.toString,

    "kalemİnikMi" -> <div><strong>kalemİnikMi</strong> - Bu komut kalemin inik olup olmadığını bildirir. </div>.toString,

    "kalemRenginiKur" -> <div><strong>kalemRenginiKur</strong>(renk) - Bu komut kaplumbağanın çizim yapmakta kullandığı kalemin rengini belirler. <br/></div>.toString,

    "boyamaRenginiKur" -> <div><strong>boyamaRenginiKur</strong>(renk) - Bu komut kaplumbağanın çizdiği şekillerin içini boyamak için kullandığı kalemin rengini belirler. <br/></div>.toString,

    "kalemKalınlığınıKur" -> <div><strong>kalemKalınlığınıKur</strong>(thickness) - Bu komut kaplumbağanın çizim yapmakta kullandığı kalemin kalınlığını belirler.<br/></div>.toString,

    "biçimleriBelleğeYaz" -> <div> <strong>biçimleriBelleğeYaz</strong>() - Bu komut kaplumbağanın o anda kullandığı biçimleri belleğe yazarak daha sonra <tt>biçimleriGeriYükle()</tt> komutuyla kolaylıkla eski duruma dönülmesine yarar. Kaplumbağamızın biçimlerini kısa bir süre için değiştirip sonra eski hale kolayca dönmek için bu komutu kullanırız. Bu yolla iki farklı çizim biçimi arasında gidip gelmek kolaylaşır. <br/> <p> Kaplumbağanın belleğe yazılan biçimleri şunlardır: <ul> <li>Kalem Rengi</li> <li>Kalem Kalınlığı</li> <li>Boyama Rengi</li> <li>Kalem Yazısı</li> <li>Kalem İnik mi Kalkık mı</li> </ul> </p> </div>.toString,

    "biçimleriGeriYükle" -> <div> <strong>biçimleriGeriYükle</strong>() - Bu komut daha önce kullanılan <tt>biçimleriBelleğeYaz()</tt> komutuyla kaydedilen kaplumbağa biçimlerini geri yükler. <br/> <p> Kaplumbağanın bellekte yazılı olan biçimleri şunlardır: <ul> <li>Kalem Rengi</li> <li>Kalem Kalınlığı</li> <li>Boyama Rengi</li> <li>Kalem Yazısı</li> <li>Kalem İnik mi Kalkık mı</li> </ul> </p> </div>.toString,

    "konumVeYönüBelleğeYaz" -> <div> <strong>konumVeYönüBelleğeYaz</strong>() - Bu komut kaplumbağanın o anki konum ve yönünü belleğe kaydeder ki yerini ve yönünü değiştiren komutlarla gittiği yeni konumdan ve yönden <tt>konumVeYönüGeriYükle()</tt> komutuyla kolaylıkla geri dönebilelim. <br/> </div>.toString,

    "konumVeYönüGeriYükle" -> <div> <strong>konumVeYönüGeriYükle</strong>() - Bu komut kaplumbağayı daha önce kullanılan <tt>konumVeYönüBelleğeYaz()</tt> komutuyla kaydedilen konum ve doğrultuya geri götürür. <br/> </div>.toString,

    "ışınlarıAç" -> <div><strong>ışınlarıAç</strong>() - Bu komut kaplumbağanın önünü, arkasını, sağını ve solunu bir artı çizerek daha kolay seçmemizi sağlar.</div>.toString,

    "ışınlarıKapat" -> <div><strong>ışınlarıKapat</strong>() - Bu komut <tt>ışınlarıAç()</tt> komutuyla kaplumbağanın üstüne çizilen artıyı siler.</div>.toString,

    "sil" -> <div><strong>sil</strong>() - Bu komut kaplumbağanın tuvalini temizler, kaplumbağayı başlangıç konumuna geri getirir ve kuzey doğrultusuna çevirir.</div>.toString,

    "çıktıyıSil" -> <div><strong>çıktıyıSil</strong>() - Bu komut çıktı penceresindeki bütün çıktıları silerek temizler. </div>.toString,
    "silVeSakla" -> <div><strong>silVeSakla</strong>() - Bu komut tuvaldeki çizimleri siler ve kaplumbağayı görünmez kılar. </div>.toString,
    "silipSakla" -> <div><strong>silipSakla</strong>() - Bu komut tuvaldeki çizimleri siler ve kaplumbağayı görünmez kılar. </div>.toString,
    "çizimiSil" -> <div><strong>çizimiSil</strong>() - Bu komut tuvaldeki çizimleri siler. </div>.toString,

    // todo: Renk* üçlüsü için help yazılmıyor
    "RenkKYM" -> <div><strong>RenkKYM</strong>(kırmızı, yeşil, mavi, saydamlık) - Verilen kırmızı, yeşil, mavi ve saydamlık değerlerini kullanarak bir renk oluşturur. Saydamlık verilmezse tam saydam olur. Her eğer 0-255 arasında bir sayı olmalı. </div>.toString,

    "RenkADA" -> <div><strong>RenkADA</strong>(arıRenk, doygunluk, aydınlıkAçıklık) - Verilen arı renk (0-360), doygunluk (0-100), ve aydınlık/açıklık derecesi (0-100) değerlerini kullanarak bir renk oluşturur. </div>.toString,

    "RenkDD" -> <div><strong>RenkDD</strong>(x1, y1, renk1, x2, y2, renk2, dönüşlü) - Verilen iki renk ve iki nokta arasında doğrusal değişimle boyayan bir renk yelpazesi oluşturur. Son girdi olmasa da olur ve dönüşümsüz olur. </div>.toString,

    "Yazıyüzü" -> <div><strong>Yazıyüzü</strong>(adı, boyu) - Verilen yazı yüzü adı ve boyunu kullanarak yeni bir yazı yüzü oluşturur. </div>.toString,

    "artalanıKur" -> <div><strong>artalanıKur</strong>(renk) - Bu komutla tuval verilen renge boyanır. Kojonun tanıdığı sarı, mavi ve siyah gibi renkleri kullanabilirsiniz ya da <tt>RenkKYM</tt>, <tt>RenkADA</tt> ve <tt>RenkDD</tt> komutlarını kullanarak kendi renklerinizi yaratabilirsiniz. </div>.toString,

    "artalanıKurDik" -> <div><strong>artalanıKurDik</strong>(renk1, renk2) - Bu komutla tuval aşağıdan yukarı doğru birinci renkten ikinci renge derece derece değişerek boyanır. </div>.toString,

    "artalanıKurYatay" -> <div><strong>artalanıKurYatay</strong>(renk1, renk2) - Bu komutla tuval soldan sağa doğru birinci renkten ikinci renge derece derece değişerek boyanır. </div>.toString,

    "konum" -> <div><strong>konum</strong> - Bu komut kaplumbağacığın bulunduğu konumu nokta (Point) olarak bildirir. <tt>konum.x</tt> ve <tt>konum.y</tt> ile de x ve y koordinatları okunabilir. </div>.toString,

    "yinele" -> <div><strong>yinele</strong>(sayı){{ }} - Bu komut küme içine alınan komutları verilen sayı kadar tekrar tekrar çağırır. <br/></div>.toString,

    "yineleDizinli" -> <div><strong>yineleDizinli</strong>(sayı) {{i => }} - Bu komut, küme içine alılan komutları verilen sayı kadar tekrar tekrar çağırır. Kaçıncı yineleme olduğunu <tt>i</tt> değişkenini küme içinde kullanarak görebiliriz. </div>.toString,

    "yineleDoğruysa" -> <div><strong>yineleDoğruysa</strong>(koşul) {{ }} - Bu komut küme içine alılan komutları verilen koşul doğru oldukça tekrar çağırır. <br/></div>.toString,

    "yineleOlanaKadar" -> <div><strong>yineleOlanaKadar</strong>(koşul) {{ }} - Bu komut küme içine alılan komutları verilen koşul sağlanana kadar tekrar çağırır. <br/></div>.toString,

    "yineleKere" -> <div><strong>yineleKere</strong>(dizi){{ }} - Bu komut küme içine alılan komutları verilen dizideki her eleman için birer kere çağırır. <br/></div>.toString,

    "yineleİçin" -> <div><strong>yineleİçin</strong>(dizi){{ }} - Bu komut küme içine alılan komutları verilen dizideki her eleman için birer kere çağırır. yineleKere ile aynı işlevi görür.<br/></div>.toString,

    "yineleİlktenSona" -> <div><strong>yineleİlktenSona</strong>(ilk, son){{ }} - Bu komut küme içine alınan komutları ilk sayıdan son sayıya kadar tekrar çağırır.</div>.toString,

    "satıryaz" -> <div><strong>satıryaz</strong>(obj) - Bu komut verilen nesneyi yazı olarak çıktı penceresine yazar ve yeni satıra geçer. </div>.toString,

    "satıroku" -> <div><strong>satıroku</strong>(yazı) - Bu komut verilen yazıyı istem olarak çıktı penceresine yazar ve arkasından sizin yazdığınız bir satırı okur ve çıktı olarak verir. </div>.toString,

    "yuvarla" -> <div><strong>yuvarla</strong>(sayı, basamak) - Bu komut verilen sayıyı noktadan sonra verilen basamak sayısına kadar yuvarlar. </div>.toString,

    "rastgele" -> <div><strong>rastgele</strong>(üstsınır) - Bu komut verilen üst sınırdan küçük rastgele bir doğal sayı verir. Sıfırdan küçük sayılar vermez. </div>.toString,

    "rastgeleKesir" -> <div><strong>rastgeleÇift</strong>() - Bu komut verilen üst sınırdan küçük rastgele bir kesirli sayı (çift çözünürlüklü) verir. Sıfırdan küçük sayılar vermez. </div>.toString,

    "giysiKur" -> <div><strong>giysiKur</strong>(giysiDosyası) - Kaplumbağanın görünüşünü verilen dosyadaki resimle değiştirir. </div>.toString,

    "giysileriKur" -> <div><strong>giysilerKur</strong>(giysiDosyası1, giysiDosyası2, ...) - Kaplumbağa için bir dizi giysi belirler ve giysiDosyası1 resmini giydirir. <tt>birSonrakiGiysi()</tt> komutuyla dizideki bir sonraki giysiyi giydirebiliriz. </div>.toString,

    "birsonrakiGiysi" -> <div><strong>birSonrakiGiysi</strong>() - Kaplumbağaya <tt>giysilerKur()</tt> komutuyla girilen giysi dizisindeki bir sonraki resmi giydirir. </div>.toString,

    "buAn" -> <div><strong>buAn</strong>() - Bu komut evrensel zamana (UTC) göre 1 Ocak 1970 tam geceyarısından bu ana kadar geçen zamanı kesirsiz milisaniye olarak verir.</div>.toString,

    "buSaniye" -> <div><strong>buSaniye</strong>() - Bu komut evrensel zamana (UTC) göre 1 Ocak 1970 tam geceyarısından bu ana kadar geçen zamanı kesirli saniye olarak verir.</div>.toString,

    "hızıKur" -> <div><strong>hızıKur</strong>(hız) - Kaplumbağacığın hızını belirler. yavaş, orta, hızlı ve çokHızlı değerlerinden birini dene.</div>.toString,

    "eksenler" ->
      <div>
      <strong>eksenler -> resim</strong> - Verilen resmin yerel eksenlerini çizerek yeni bir resim oluşturur.
      <br/>
      Bu ve benzeri yöntemler bir resimde değişiklik yapmak için kullanılır. Ayrıca benzer yöntemlerle birleştirilebilirler. Onun için türün adını BirleşebilenDönüştürücü koyduk. BD diye de kısalttık.
      <br/><em>Örnek:</em> <br/>
      <pre>
      tanım resim = Resim {{
        yinele(2) {{
          ileri(50); sağ()
          ileri(100); sağ()
        }}
      }}
      silVeSakla
      // dört tane BD * ile birleşiyorlar, ve -> ile resmi dönüştürüyorlar
      çiz(
        götür(-100, -50) * döndür(45) *
          boyaRengi(mavi) * eksenler -> resim
      )
      eksenleriGöster()
      </pre>
      Bu örnekte bir dikdörtgen çiziyor, onu (-100, -50) noktasına taşıyor, 45 derece döndürüyor ve içini maviye boyuyoruz. Bir de bu çizimin kendi eksenlerini yani yerel eksenlerini çiziyoruz. En sondaki eksenleriGöster komutuyla da genel ya da mutlak eksenleri gösteriyoruz.
      </div>.toString,

    "eksenleriGöster" ->
      <div>
      <strong>eksenleriGöster</strong> - Çizim tuvalinde yatay (X) ve dikey (Y) eksenlerini çizer.
      </div>.toString,

    "eksenleriGizle" ->
      <div>
      <strong>eksenleriGizle</strong> - Çizim tuvalindeki yatay (X) ve dikey (Y) eksenlerini siler.
      </div>.toString,

    "götür" -> <div>

      <strong>götür</strong>(x, y) <br/>
      <strong>götür</strong>(nokta) <br/>
      <strong>götür</strong>(yöney2b) <br/> <br/>

      Bir resmi çizmeden önce verilen koordinatlara götürür. <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>

      tanım kare = Resim {{
          yinele(4) {{
              ileri(50)
              sağ()
          }}
      }}

      silipSakla()
      eksenleriGöster
      dez r1 = götür(50, 100) -> kare
      r1.çiz

      dez r2 = götür(100, 50) * boyaRengi(mavi) -> kare
      dez r3 = götür(-50, 50) * boyaRengi(yeşil) -> kare
      dez r4 = götür(50, -50) * boyaRengi(kırmızı) -> kare
      çiz(r2, r3, r4)

    </pre> 'boyaRengi' ve 'götür' yöntemlerine birleşebilen bir dönüştürücü (BD) deriz, çünkü * ile birleştirebilir ve resmi dönüştürürler. Bir de 'döndür'e bak. <br/>

      </div>.toString,

    "döndür" -> <div>
      <strong>döndür</strong>(açı) - Bir resmi çizmeden önce verilen açı kadar saat yönünün tersine döndürür. <br/>
      360 derece döndürmek tam dönüş olur ve etkisi olmaz. <br/>
      <br/><em>Örnek:</em> <br/>
      <pre>

        silipSakla
        dez d1 = döndür(30) -> Resim.dikdörtgen(100, 20)
        d1.çiz

      </pre> Bu örnekten sonra açıklama ...
      </div>.toString,

    "sürüm" -> "sürüm - Çıktıya kullanılan Scala sürümünü yazar.",

    // todo: much more
  )
}
