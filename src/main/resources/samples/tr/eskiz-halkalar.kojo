// Eskiz örneği: su yüzeyinde halkalar.
// Rastgele yerlere damlalar düşer; her damla genişleyen ve sönen halkalar bırakır.
// Her karede tuval baştan boyanıyor (artalan) ve canlı halkaların hepsi yeniden çiziliyor:
// iz kalmıyor, ama yüzlerce elips bile tek bir Resim, yani tek bir eskiz.
//
// Burada neden Resim.daire değil de eskiz? Her halka her karede yeni bir Resim olsaydı
// Kojo kısa sürede "Too many pics" sınırına (60000) takılırdı (#87).
silVeSakla()
başlangıçNoktasıAltSolKöşeOlsun() // eskiz resmi tuvalin sol alt köşesinden başlar

dez en = tuvalAlanı.eni
dez boy = tuvalAlanı.boyu
dez enBüyükYarıçap = 0.18 * enUfağı(en, boy)

sınıf Halka(x: Kesir, y: Kesir, arıRenk: Kesir) {
    den yaş = 0.0
    tanım ölüMü = yaş >= 1.0
    tanım adım() { yaş += 0.012 }
    tanım göster(tuval: TuvalÇizim) {
        dez saydamlık = 1.0 - yaş            // yaşlandıkça sönüyor
        dez r = enBüyükYarıçap * yaş
        tuval.boyasız()
        tuval.fırçaAğırlığı(3 * saydamlık + 0.5)
        tuval.fırça(Renk.adas(arıRenk, 0.8, 0.6, saydamlık))
        tuval.elips(x, y, 2 * r, 2 * r)
        tuval.elips(x, y, 1.3 * r, 1.3 * r)  // iç halka
    }
}

sınıf Su yayar Eskiz {
    dez halkalar = EsnekDizim.boş[Halka]

    tanım kurulum(tuval: TuvalÇizim) {
        tuval.artalan(0, 20, 50)
    }

    tanım çizimDöngüsü(tuval: TuvalÇizim) {
        eğer (rastgeleKesir(1) < 0.06) {
            halkalar.ekle(yeni Halka(rastgeleKesir(en), rastgeleKesir(boy), rastgeleKesir(160, 220)))
        }
        halkalar.eleYerinde(h => !h.ölüMü)
        tuval.artalan(0, 20, 50)
        halkalar.herbiriİçin { h =>
            h.adım()
            h.göster(tuval)
        }
    }
}

çiz(Resim.eskizden(yeni Su))
