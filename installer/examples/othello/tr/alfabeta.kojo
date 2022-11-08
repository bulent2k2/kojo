//#yükle tr/durum

object ABa { // alfa-beta arama
    var sayaç = 0
    val debug = doğru
    def hamleYap(drm: Durum): Belki[Oda] = {
        var çıktı: Belki[Oda] = Hiçbiri
        sayaç = 0
        zamanTut(s"Alfa-beta ${düzeydenUstalığa} arama") {
            çıktı = alfaBetaHamle(drm)
        }(s"sürdü.")
        if (debug) {
            val oyuncu = drm.sıra match {
                case Beyaz => "Beyaz"
                case Siyah => "Siyah"
            }
            val hamle: Yazı = çıktı.işle(oda => s"Oda(${oda.str},${oda.stn})").alYoksa("yok")
            satıryaz(s"sayaç=$sayaç $oyuncu -> $hamle,")
        }
        çıktı
    }
    def yeter(derinlik: Sayı): İkil = { // yeterince derin ve çok "düşündük" mü?
        //satıryaz((derinlik, sayaç))
        derinlik <= 0 && sayaç > hamleSayısıÜstSınırı
    }
    def alfaBetaHamle(drm: Durum): Belki[Oda] =
        if (drm.seçenekler.boşMu) Hiçbiri
        else if (drm.seçenekler.boyu == 1) {
            if (debug) {
                var tekHamle = drm.seçenekler.başı
                satıryaz(s"Tek hamle var: ($tekHamle,${drm.oyna(tekHamle).skor})")
            }
            Biri(drm.seçenekler.başı)
        }
        else // todo: karşı oyuncunun skorunu azaltan birden çok hamle varsa rastgele seç
        if (debug) {
            val hepsi = for (hamle <- drm.seçenekler) yield hamle -> abHamle(drm.oyna(hamle), aramaDerinliğiSınırı)
            satıryaz(hepsi)
            if (yanlış) Biri(drm.sıra match {
                case Siyah => hepsi.enUfağı(_._2)._1
                case Beyaz => hepsi.enİrisi(_._2)._1
            })
            else Biri(hepsi.enUfağı(_._2)._1)
        }
        else Biri({
            for (hamle <- drm.seçenekler)
                yield hamle -> abHamle(drm.oyna(hamle), aramaDerinliğiSınırı)
        }.enUfağı(_._2)._1)

    def abHamle(drm: Durum, derinlik: Sayı): Sayı =
        if (drm.bitti || yeter(derinlik)) drm.skor
        else if (drm.seçenekler.boşMu) azalt2(new Durum(drm.tahta, drm.karşıTaş), derinlik - 1, Sayı.EnUfağı, Sayı.Enİrisi)
        else azalt(drm, derinlik, Sayı.EnUfağı, Sayı.Enİrisi)

    def azalt(drm: Durum, derinlik: Sayı, alfa: Sayı, beta: Sayı): Sayı =
        if (drm.bitti || yeter(derinlik)) drm.skor
        else if (drm.seçenekler.boşMu) -artır2(new Durum(drm.tahta, drm.karşıTaş), derinlik - 1, Sayı.EnUfağı, Sayı.Enİrisi)
        else {
            var yeniBeta = beta
            sayaç += drm.seçenekler.boyu
            drm.seçenekler.herbiriİçin { hamle => // onun hamleleri
                yeniBeta = enUfağı(yeniBeta, artır(drm.oyna(hamle), derinlik - 1, alfa, yeniBeta))
                if (alfa >= yeniBeta) return alfa
            }
            yeniBeta
        }
    def artır(drm: Durum, derinlik: Sayı, alfa: Sayı, beta: Sayı): Sayı =
        if (drm.bitti || yeter(derinlik)) drm.skor
        else if (drm.seçenekler.boşMu) -azalt2(new Durum(drm.tahta, drm.karşıTaş), derinlik - 1, Sayı.EnUfağı, Sayı.Enİrisi)
        else {
            var yeniAlfa = alfa
            sayaç += drm.seçenekler.boyu
            drm.seçenekler.herbiriİçin { hamle =>
                yeniAlfa = enİrisi(yeniAlfa, azalt(drm.oyna(hamle), derinlik - 1, yeniAlfa, beta))
                if (yeniAlfa >= beta) return beta
            }
            yeniAlfa
        }
    def azalt2(drm: Durum, derinlik: Sayı, alfa: Sayı, beta: Sayı): Sayı = {
        belirt(drm.seçenekler.doluMu, "azalt2 hata")
        var yeniAlfa = alfa
        sayaç += drm.seçenekler.boyu
        drm.seçenekler.herbiriİçin { hamle =>
            yeniAlfa = enİrisi(yeniAlfa, azalt(drm.oyna(hamle), derinlik - 1, yeniAlfa, beta))
            if (yeniAlfa >= beta) return beta
        }
        yeniAlfa
    }
    def artır2(drm: Durum, derinlik: Sayı, alfa: Sayı, beta: Sayı): Sayı = {
        belirt(drm.seçenekler.doluMu, "artır2 hata")
        var yeniBeta = beta
        sayaç += drm.seçenekler.boyu
        drm.seçenekler.herbiriİçin { hamle =>
            yeniBeta = enUfağı(yeniBeta, artır(drm.oyna(hamle), derinlik - 1, alfa, yeniBeta))
            if (alfa >= yeniBeta) return alfa
        }
        yeniBeta
    }

    def ustalık(derece: Ustalık): Birim = {
        val ikili = derece match {
            case Er       => (3, 25000)
            case Çırak    => (4, 50000)
            case Kalfa    => (5, 100000)
            case Usta     => (6, 200000)
            case Doktor   => (7, 500000)
            case Aheste   => (7, 500000)
            case Deha     => (8, 1000000)
            case ÇokSabır => (8, 1000000)
            case _        => (5, 100000)
        }
        aramaDerinliğiSınırı = ikili._1
        hamleSayısıÜstSınırı = ikili._2
    }

    def düzeydenUstalığa: Ustalık =
        if (aramaDerinliğiSınırı < 3) ErdenAz
        else if (aramaDerinliğiSınırı > 8) DehadanÇok
        else {
            aramaDerinliğiSınırı match {
                case 3 => Er
                case 4 => Çırak
                case 5 => Kalfa
                case 6 => Usta
                case 7 => Doktor
                case 8 => Deha
            }
        }
    var aramaDerinliğiSınırı = 3
    var hamleSayısıÜstSınırı = 25000

}

var i = 0
class Oyun(tane: Sayı) {
    val t = yeniTahta(tane)
    val drm = new Durum(t, Siyah)

    var enİriSayaç = 0
    def oyna: Durum = {
        val çıktı = döngü(drm)
        satıryaz(s"En İri Sayaç=$enİriSayaç")
        çıktı
    }

    import scala.annotation.tailrec
    @tailrec
    private def döngü(drm: Durum): Durum = {
        drm.tahta.yaz("")
        i += 1
        if (drm.oyunBittiMi) return drm
        if (i > tane * tane) { satıryaz("çok uzadı!"); return drm }
        val (eskiDurum, hamle) = ABa.hamleYap(drm) match {
            case Biri(oda) => {
                if (ABa.sayaç > enİriSayaç) enİriSayaç = ABa.sayaç
                (drm, oda)
            }
            case _ => {
                val drm2 = new Durum(drm.tahta, drm.karşıTaş)
                satıryaz(s"Sıra yine ${drm2.sıra}'de")
                ABa.hamleYap(drm2) match {
                    case Biri(oda) => {
                        if (ABa.sayaç > enİriSayaç) enİriSayaç = ABa.sayaç
                        (drm2, oda)
                    }
                    case _ => throw new KuralDışı("Burada olmamalı")
                }
            }
        }
        val yeniDurum = eskiDurum.oyna(hamle)
        satıryaz(s"$i. hamle ${eskiDurum.sıra} $hamle:")
        döngü(yeniDurum)
    }
}

def dene1 = {
    val tane = 4
    var t = new Tahta(tane, Yöney.doldur(tane * tane)(0))
    satıryaz("t"); t.yaz()
    val foo = t.koy(Oda(1, 1), Beyaz)
    t = t.koy(Dizi(Oda(2, 2), Oda(3, 3)), Beyaz)
    t = t.koy(Dizi(Oda(2, 3), Oda(3, 2)), Siyah)
    val t2 = t.oyna(Siyah, Oda(1, 2))
    satıryaz("t2"); t2.yaz()
    val t3 = t2.oyna(Beyaz, Oda(1, 3))
    satıryaz("t3"); t3.yaz()
    satıryaz("t"); t.yaz()
    foo.yaz()
}

// Birim denemeler (unit tests)

def dene1b = { // hamle olmadığında sıra geçmesini dene
    val tane = 4
    var t = new Tahta(tane, Yöney.doldur(tane * tane)(0))
    satıryaz("t"); t.yaz()
    t = t.koy(Dizi(Oda(0, 0), Oda(1, 0), Oda(2, 0),
        Oda(0, 1), Oda(1, 1), Oda(0, 2)), Beyaz)
    t = t.koy(Dizi(Oda(2, 1), Oda(2, 2), Oda(2, 3),
        Oda(1, 2)), Siyah)
    t.yaz()
    val d = new Durum(t, Siyah)
    belirt(d.seçenekler.boşMu, "Sıra yine beyazın")
    belirt(ABa.hamleYap(d) == Hiçbiri, "Hamle yok")
    val d2 = new Durum(t, Beyaz)
    belirt(d2.seçenekler.boyu == 4, "Dört seçenek")
    // satıryaz(ABa.hamleYap(d2))
    belirt(ABa.hamleYap(d2) == Biri(Oda(3, 2)), "Doğru hamle")
    t = t.oyna(Beyaz, Oda(3, 2))
    t.yaz()
}

def dene2 = {
    çıktıyıSil
    val o = new Oyun(4) // 6
    //ABa.ustalık(Çırak)
    //ABa.ustalık(Çırak)
    ABa.aramaDerinliğiSınırı = 13
    ABa.hamleSayısıÜstSınırı = 1000000
    o.oyna
}

def dene2a = { // 4x4 tahta için dene2 ile aynı
    ABa.aramaDerinliğiSınırı = 13
    ABa.hamleSayısıÜstSınırı = 1000000
    var t = yeniTahta(4)
    var sıra: Taş = Siyah
    çıktıyıSil()
    for (hamleSayısı <- 1 |-| 14) {
        ABa.hamleYap(new Durum(t, sıra)) işle { oda =>
            satıryaz(s"Hamle $hamleSayısı: $sıra $oda oynadı:")
            t = t.oyna(sıra, oda)
            t.yaz()
        } alYoksa satıryaz(s"$sıra'ın hamlesi yok!")
        sıra = if (sıra == Siyah) Beyaz else Siyah
    }
}

def dene2b = { // alfa beta arama doğru skoru kullanıyor mu?
    ABa.aramaDerinliğiSınırı = 13
    ABa.hamleSayısıÜstSınırı = 1000000
    var t = yeniTahta(4)
    çıktıyıSil()
    var say = 1
    for (
        (sıra, hamle) <- Dizi(
            Siyah -> Oda(0, 1),
            Beyaz -> Oda(0, 0),
            Siyah -> Oda(1, 0),
            Beyaz -> Oda(0, 2),
            Siyah -> Oda(0, 3),
            Beyaz -> Oda(2, 0),
            Siyah -> Oda(3, 0),
            Beyaz -> Oda(1, 3),
            Siyah -> Oda(2, 3),
            Beyaz -> Oda(3, 1),
            Siyah -> Oda(3, 2),
            Beyaz -> Oda(3, 3),
            Siyah -> Oda(-1, -1) // Bu hamle geçersiz. Hiç teşebbüs edilmeyecek.
        )
    ) ABa.hamleYap(new Durum(t, sıra)) işle { oda =>
        satıryaz(s"$say: $sıra için alfa-beta önerisi: $oda oynanan hamle: $hamle")
        t = t.oyna(sıra, hamle)
        t.yaz()
        say += 1
    } alYoksa satıryaz(s"$sıra'ın hamlesi yok!")
}

/* Elimizdeki alfabeta motoru ile en iyi hamleleri oynarsak sonuç şöyle oluyor:
  S S B S
  S S B S
  S S S S
  B B B S
  Siyah 6 taşla kazanır (11-5)
  Bu çok yanlış, çünkü iyi oynanırsa 4x4 tahtada hep ikinci oynayan Beyaz kazanmalı!
    https://en.wikipedia.org/wiki/Computer_Othello#Othello_4_%C3%97_4
*/
def dene2c = { // alfa beta arama doğru skoru kullanıyor mu?
    ABa.aramaDerinliğiSınırı = 13
    ABa.hamleSayısıÜstSınırı = 1000000
    var t = yeniTahta(4)
    çıktıyıSil()
    var say = 1
    for (
        (sıra, hamle) <- Dizi(
            Siyah -> Oda(0, 1),
            Beyaz -> Oda(2, 0),
            Siyah -> Oda(3, 3),
            Beyaz -> Oda(0, 2),
            Siyah -> Oda(3, 1),
            Beyaz -> Oda(2, 3),
            Siyah -> Oda(1, 3),
            Beyaz -> Oda(0, 0),
            Siyah -> Oda(3, 0),
            Beyaz -> Oda(3, 2),
            Siyah -> Oda(1, 0),
            Siyah -> Oda(0, 3),
            Siyah -> Oda(-1, -1) // Bu hamle geçersiz. Hiç teşebbüs edilmeyecek.
        )
    ) ABa.hamleYap(new Durum(t, sıra)) işle { oda =>
        satıryaz(s"$say: $sıra için alfa-beta önerisi: $oda oynanan hamle: $hamle")
        t = t.oyna(sıra, hamle)
        t.yaz()
        say += 1
    } alYoksa satıryaz(s"$sıra'ın hamlesi yok!")
}

def birDiziHamleYap(hamleler: Dizi[(Taş, Oda)]) = {
    ABa.aramaDerinliğiSınırı = 13
    ABa.hamleSayısıÜstSınırı = 1000000
    var t = yeniTahta(4)
    çıktıyıSil()
    var say = 1
    for ((sıra, hamle) <- hamleler)
        ABa.hamleYap(new Durum(t, sıra)) işle { oda =>
            satıryaz(s"$say: $sıra için alfa-beta önerisi: $oda oynanan hamle: $hamle")
            t = t.oyna(sıra, hamle)
            t.yaz()
            say += 1
        } alYoksa satıryaz(s"$sıra'ın hamlesi yok!")
}

def dene2d = { // after fixing how we use artır2 and azalt2
    /*
     *  S S S B
     *  S B B B
     *  S B B B
     *  B B B B
     */
    birDiziHamleYap(Dizi(
        Siyah -> Oda(0, 1),
        Beyaz -> Oda(2, 0),
        Siyah -> Oda(3, 1),
        Beyaz -> Oda(0, 0),
        Siyah -> Oda(3, 2),
        Beyaz -> Oda(0, 2),
        Siyah -> Oda(1, 0),
        Beyaz -> Oda(2, 3),
        Siyah -> Oda(1, 3),
        Beyaz -> Oda(0, 3),
        Siyah -> Oda(3, 0),
        Beyaz -> Oda(3, 3))
    )
}

def dene2e = { // BUG5.kojo
    /*
    S S S S
    S S S S
    S B S S
    B B B S
    Oyun bitti.
    Beyazlar: 4
    Siyahlar: 12
 */
    birDiziHamleYap(Dizi(
        Siyah -> Oda(0, 1),
        Beyaz -> Oda(2, 0),
        Siyah -> Oda(3, 0),
        Beyaz -> Oda(0, 0),
        Siyah -> Oda(1, 0),
        Beyaz -> Oda(0, 2),
        Siyah -> Oda(0, 3),
        Beyaz -> Oda(2, 3),
        Siyah -> Oda(1, 3),
        Beyaz -> Oda(3, 1),
        Siyah -> Oda(3, 1),
        Siyah -> Oda(3, 3),
        Siyah -> Oda(3, 2)
    ))
}

def dene2f = { // BUG5b.kojo
    birDiziHamleYap(Dizi(
        Siyah -> Oda(0, 1),
        Beyaz -> Oda(0, 2),
        Siyah -> Oda(0, 3),
        Beyaz -> Oda(2, 0),
        Siyah -> Oda(3, 0),
        Beyaz -> Oda(0, 0),
        Siyah -> Oda(1, 0),
        Siyah -> Oda(3, 3)
    ))
}

/* Bu yazılımcığı otello.kojo ve menu.kojo kullanıyor.
   Onun için denemeleri artık çalıştırmıyoruz */
// dene1c
dene2f
// satıryaz("arama motoru hazır")
