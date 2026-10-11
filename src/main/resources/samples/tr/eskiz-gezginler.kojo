// Eskiz örneği: iz bırakan gezginler.
// Her gezgin her karede rastgele küçük bir adım atar ve arkasında renkli bir iz bırakır.
// İzler zamanla sönüyor: her karede tuvalin üstüne çok saydam siyah bir örtü çekiliyor.
//
// Neden Eskiz? Her adım için yeni bir Resim (örneğin Resim.daire) çizseydik, Kojo birkaç
// dakika sonra "Too many pics" sınırına (60000) takılıp çizmeyi bırakırdı (#87).
// Eskiz tek bir Resim: çizdikleri piksel olduğundan sınır yok, istediğiniz kadar çalışır.
silVeSakla()
başlangıçNoktasıAltSolKöşeOlsun() // eskiz resmi tuvalin sol alt köşesinden başlar

dez en = tuvalAlanı.eni
dez boy = tuvalAlanı.boyu
dez gezginSayısı = 12
dez adımBoyu = 7.0

sınıf Gezgin(arıRenk: Kesir) {
    den x = en / 2
    den y = boy / 2

    tanım adım(tuval: TuvalÇizim) {
        dez eskiX = x
        dez eskiY = y
        x = enUfağı(en, enİrisi(0.0, x + rastgeleKesir(-adımBoyu, adımBoyu))) // kenarda durur
        y = enUfağı(boy, enİrisi(0.0, y + rastgeleKesir(-adımBoyu, adımBoyu)))
        tuval.fırça(Renk.adas(arıRenk, 1, 0.5, 0.9))
        tuval.fırçaAğırlığı(2)
        tuval.çizgi(eskiX, eskiY, x, y)
    }
}

sınıf Gezginler yayar Eskiz {
    dez gezginler = (1 |-| gezginSayısı).işle(i => yeni Gezgin(i * 360.0 / gezginSayısı))

    tanım kurulum(tuval: TuvalÇizim) {
        tuval.artalan(siyah)
    }

    tanım çizimDöngüsü(tuval: TuvalÇizim) {
        tuval.boya(0, 0, 0, 20) // soldurucu örtü
        tuval.fırçasız()
        tuval.dikdörtgen(0, 0, en, boy)
        gezginler.herbiriİçin(_.adım(tuval))
    }
}

çiz(Resim.eskizden(yeni Gezginler))
