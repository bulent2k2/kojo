// Benzetimli tavlama (simulated annealing)
// Beş dörtgeni, birbirinin üstüne bindirmeden, olabildiğince küçük bir kutuya yerleştiriyoruz.
// Bilmece: bu beşi 150 x 150'lik bir kareyi TAM dolduruyor (fırıldak dizilişi),
// yani bulunabilecek en küçük alan 22500.
//
// Tavlamanın özü: arada bir daha KÖTÜ bir yeri de kabul etmek. Kötülük ne kadar büyük,
// sıcaklık ne kadar düşükse o kadar seyrek: olasılık = e üssü (-fark / sıcaklık).
// Sıcakken dörtgenler serbestçe dolaşır, soğudukça yerlerine oturur. Yalnız iyileştiren
// hamleleri kabul eden aceleci arama örneğiyle karşılaştırın: o çoğu zaman takılıp kalır.

sınıf Dörtgen(dez en: Sayı, dez boy: Sayı) {
  dez resim = boyaRengi(rastgeleRenk) -> Resim.dikdörtgen(en, boy)
  çiz(resim)
  den x, y = 0.0 // sol alt köşe
  den öncekiX, öncekiY = 0.0
  den enİyiX, enİyiY = 0.0
  tanım sağı = x + en
  tanım üstü = y + boy
  tanım yerleştir(yeniX: Kesir, yeniY: Kesir) = { x = yeniX; y = yeniY }
  tanım yeriniAnımsa() = { öncekiX = x; öncekiY = y }
  tanım geriAl() = yerleştir(öncekiX, öncekiY)
  tanım enİyiyiAnımsa() = { enİyiX = x; enİyiY = y }
  tanım enİyiyeDön() = yerleştir(enİyiX, enİyiY)
  tanım göster() = resim.konumuKur(x, y)
}

dez dörtgenler = Diz(yeni Dörtgen(100, 50), yeni Dörtgen(50, 100), yeni Dörtgen(100, 50),
  yeni Dörtgen(50, 100), yeni Dörtgen(50, 50))
dez n = 5

// --- enerji: kutunun alanı + örtüşme cezası ---
tanım örtüşme(a: Dörtgen, b: Dörtgen): Kesir = {
  dez en = enUfağı(a.sağı, b.sağı) - enİrisi(a.x, b.x)
  dez boy = enUfağı(a.üstü, b.üstü) - enİrisi(a.y, b.y)
  eğer (en > 0 && boy > 0) en * boy yoksa 0.0
}
tanım örtüşmeler(ds: Diz[Dörtgen]): Kesir =
  eğer (ds.boşMu) 0.0 yoksa ds.kuyruğu.işle(d => örtüşme(ds.başı, d)).topla + örtüşmeler(ds.kuyruğu)

tanım kutuSol = dörtgenler.işle(_.x).indirge((a, b) => enUfağı(a, b))
tanım kutuAlt = dörtgenler.işle(_.y).indirge((a, b) => enUfağı(a, b))
tanım kutuSağ = dörtgenler.işle(_.sağı).indirge((a, b) => enİrisi(a, b))
tanım kutuÜst = dörtgenler.işle(_.üstü).indirge((a, b) => enİrisi(a, b))
tanım kutuEni = kutuSağ - kutuSol
tanım kutuBoyu = kutuÜst - kutuAlt
tanım alan = kutuEni * kutuBoyu

// --- ayarlar ---
dez başSıcaklık = 5000.0
dez sonSıcaklık = 10.0
dez turBoyu = 4000 // bir soğuma turunda denenen hamle sayısı
dez turSayısı = 20 // kaç kez yeniden ısıtılacak
dez kareBaşınaHamle = 200 // her çizim karesinde denenen hamle
dez soğuma = kuvveti(sonSıcaklık / başSıcaklık, 1.0 / turBoyu) // her hamlede sıcaklık bununla çarpılır
// Birim örtüşme alanının bedeli. Sıcakken düşük, dörtgenler birbirinin içinden geçebilsin;
// soğudukça artıyor, sonunda örtüşme kutuyu küçültmeye değmiyor.
dez başCeza = 2.0
dez sonCeza = 30.0
dez yapıştırmaOlasılığı = 0.3
dez enBüyükKaydırma = 50

den sıcaklık = başSıcaklık
tanım ceza = sonCeza + (başCeza - sonCeza) * sıcaklık / başSıcaklık
tanım enerji = alan + ceza * örtüşmeler(dörtgenler)

// --- hamleler ---
// Rastgele kaydırma: sıcakken büyük adımlar, soğudukça küçük.
tanım kaydır(d: Dörtgen) = {
  dez k = 1 + (enBüyükKaydırma * sıcaklık / başSıcaklık).toInt
  d.yerleştir(d.x + rastgele(-k, k + 1), d.y + rastgele(-k, k + 1))
}
// a'yı b'nin bir kenarına, bir köşesiyle hizalı olarak yapıştır.
tanım yapıştır(a: Dörtgen, b: Dörtgen) = {
  dez hizaX = eğer (rastgele(2) == 0) b.x yoksa b.sağı - a.en
  dez hizaY = eğer (rastgele(2) == 0) b.y yoksa b.üstü - a.boy
  rastgele(4) eşle {
    durum 0 => a.yerleştir(b.x - a.en, hizaY) // b'nin soluna
    durum 1 => a.yerleştir(b.sağı, hizaY) // sağına
    durum 2 => a.yerleştir(hizaX, b.y - a.boy) // altına
    durum _ => a.yerleştir(hizaX, b.üstü) // üstüne
  }
}

den enİyiAlan = 1e12
tanım birHamle() = {
  dez öncekiEnerji = enerji
  dez i = rastgele(n)
  dez j = (i + 1 + rastgele(n - 1)) % n // i'den başka bir dörtgen
  dez d = dörtgenler(i)
  d.yeriniAnımsa()
  eğer (rastgeleKesir(1) < yapıştırmaOlasılığı) yapıştır(d, dörtgenler(j)) yoksa kaydır(d)
  dez fark = enerji - öncekiEnerji
  eğer (fark <= 0 || rastgeleKesir(1) < eüssü(-fark / sıcaklık)) {
    // kabul; örtüşmesiz ve şimdiye kadarkinden küçükse en iyi diye sakla
    eğer (örtüşmeler(dörtgenler) == 0 && alan < enİyiAlan) {
      enİyiAlan = alan
      için (o <- dörtgenler) o.enİyiyiAnımsa()
    }
  } yoksa d.geriAl() // ret: eski yerine dön
  sıcaklık *= soğuma
}

// Kutuyu tuvalin ortasına taşı (enerjiyi değiştirmez, yalnız görüntü için).
tanım ortala() = {
  dez dx = ((kutuSol + kutuSağ) / 2).toInt
  dez dy = ((kutuAlt + kutuÜst) / 2).toInt
  için (o <- dörtgenler) { o.yerleştir(o.x - dx, o.y - dy); o.göster() }
}

tanım bitir() = {
  durdur()
  için (o <- dörtgenler) o.enİyiyeDön()
  ortala()
  çiz(öteleme(kutuSol, kutuAlt) * kalemRengi(mavi) -> Resim.dikdörtgen(kutuEni, kutuBoyu))
  satıryaz(f"Sonuç: alan = $alan%.0f ($kutuEni%.0f x $kutuBoyu%.0f). En iyisi 22500 (150 x 150).")
}

den tur = 1
den hamle = 0
canlandır {
  yinele(kareBaşınaHamle) { birHamle() }
  hamle += kareBaşınaHamle
  ortala()
  eğer (hamle >= turBoyu) {
    satıryaz(f"$tur%2d. tur bitti, en iyi alan: $enİyiAlan%.0f")
    eğer (tur == turSayısı) bitir()
    yoksa { tur += 1; hamle = 0; sıcaklık = başSıcaklık } // yeniden ısıt
  }
}
tuvaliYakınlaştır(0.9, 0.9, 0, 0); gizle()
