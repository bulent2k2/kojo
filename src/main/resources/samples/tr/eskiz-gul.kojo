// Eskiz örneği: gül eğrisi (rhodonea). r = kosinüs(k * açı), k = n / d.
// Eğri kare kare, her karede biraz daha uzayarak çiziliyor; renk açıyla değişiyor.
// Eğri kapanınca birkaç saniye bekleyip yeni bir (n, d) seçilir.
//
// Dönüşüm kaydet/geri yükle örneği de var: merkeze götür ile çizip dışarıyı bozmuyoruz.
// Eskiz tek bir Resim olduğundan, binlerce küçük çizgi "Too many pics" sınırına takılmaz (#87).
silVeSakla()
başlangıçNoktasıAltSolKöşeOlsun() // eskiz resmi tuvalin sol alt köşesinden başlar

dez en = tuvalAlanı.eni
dez boy = tuvalAlanı.boyu
dez yarıçap = 0.45 * enUfağı(en, boy)
dez seçenekler = Diz((7, 4), (5, 3), (9, 4), (5, 2), (8, 5), (3, 1))
dez karedeAdım = 40       // her karede kaç küçük çizgi
dez açıAdımı = 0.01

sınıf Gül yayar Eskiz {
    den (n, d) = seçenekler(0)
    den açı = 0.0
    den bekleme = 0

    tanım yarıçapı(a: Kesir) = yarıçap * kosinüs(n.toDouble / d * a)

    tanım yeniSeç(tuval: TuvalÇizim) {
        dez (n2, d2) = seçenekler(rastgele(seçenekler.boyu))
        n = n2; d = d2
        açı = 0.0
        tuval.artalan(siyah)
    }

    tanım kurulum(tuval: TuvalÇizim) {
        tuval.artalan(siyah)
    }

    tanım çizimDöngüsü(tuval: TuvalÇizim) {
        eğer (açı > 2 * piSayısı * d) {   // eğri kapandı
            bekleme += 1
            eğer (bekleme > 180) { bekleme = 0; yeniSeç(tuval) }
        }
        yoksa {
            tuval.dönüşümüKaydet()
            tuval.götür(en / 2, boy / 2)
            tuval.fırçaAğırlığı(2)
            yinele(karedeAdım) {
                dez a2 = açı + açıAdımı
                dez (x1, y1) = (yarıçapı(açı) * kosinüs(açı), yarıçapı(açı) * sinüs(açı))
                dez (x2, y2) = (yarıçapı(a2) * kosinüs(a2), yarıçapı(a2) * sinüs(a2))
                tuval.fırça(Renk.adas((açı * 180 / piSayısı) % 360, 1, 0.55, 1))
                tuval.çizgi(x1, y1, x2, y2)
                açı = a2
            }
            tuval.dönüşümüGeriYükle()
        }
    }
}

çiz(Resim.eskizden(yeni Gül))
