// Aceleci (açgözlü) arama: beş dörtgeni, birbirinin üstüne bindirmeden, küçük bir kutuya sığdırmak.
// Her karede her dörtgen rastgele biraz kaydırılıyor; enerji ARTARSA kaydırma geri alınıyor.
// Yani yalnız iyileştiren hamleler kabul ediliyor: hızlı, ama çoğu zaman ilk düştüğü çukurda
// takılıp kalıyor. Aynı sorunu benzetimli tavlamayla çözen örnekle karşılaştırın.
// (Bu beş dörtgen 150 x 150'lik bir kareyi tam dolduruyor: en küçük alan 22500.)
durum sınıf Dörtgen(w: Sayı, h: Sayı) {
  dez o = boyaRengi(rastgeleRenk) -> Resim.dikdörtgen(w, h)
  çiz(o)
  tanım kesişim(öbürü: Dörtgen): Kesir = yatayAçıklık.kesişim(öbürü.yatayAçıklık) * dikeyAçıklık.kesişim(öbürü.dikeyAçıklık)
  tanım yatayAçıklık = SayıAralığı(o.konum.x, o.konum.x + w)
  tanım dikeyAçıklık = SayıAralığı(o.konum.y, o.konum.y + h)
}
durum sınıf SayıAralığı(a: Kesir, b: Kesir) { require(a <= b)
  tanım kesişim(o: SayıAralığı) =
    eğer (o.a >= b || o.b <= a) 0.0
    yoksa enUfağı(o.b, b) - enİrisi(o.a, a)
}
dez nesneler = Diz(Dörtgen(100, 50), Dörtgen(50, 100), Dörtgen(100, 50), Dörtgen(50, 100), Dörtgen(50, 50))
dez (enK, boyK) = (tuvalAlanı.en.toInt, tuvalAlanı.boy.toInt) // tuvalin eni ve boyu
// Enerji: kutunun alanı + örtüşmenin çok ağır cezası
tanım enerji = 1e5 * kesişim(nesneler) + alan
tanım kesişim(os: Diz[Dörtgen]): Kesir = eğer (os.boşMu) 0.0 yoksa (os.kuyruğu.işle { o => os.başı.kesişim(o) }).topla + kesişim(os.kuyruğu)
tanım alan = enToplam * boyToplam
tanım enToplam = sağKenar - enSol.x
tanım boyToplam = üstKenar - enAlt.y
tanım enSol = nesneler.işle { o => o.o.konum }.indirge { (p1, p2) => eğer (p1.x < p2.x) p1 yoksa p2 }
tanım sağKenar = nesneler.işle { o => o.o.konum.x + o.w }.indirge { (x1, x2) => eğer (x1 > x2) x1 yoksa x2 }
tanım enAlt = nesneler.işle { o => o.o.konum }.indirge { (p1, p2) => eğer (p1.y < p2.y) p1 yoksa p2 }
tanım üstKenar = nesneler.işle { o => o.o.konum.y + o.h }.indirge { (y1, y2) => eğer (y1 > y2) y1 yoksa y2 }
dez tmax = 200
den t = tmax
den enerjiŞimdi = enerji
canlandır {
  eğer (t == 0) {
    durdur()
    satıryaz(f"alan = $alan%.0f ($enToplam%.0f x $boyToplam%.0f). En iyisi 22500 (150 x 150).")
    çiz(öteleme(enSol.x, enAlt.y) * kalemRengi(mavi) -> Resim.dikdörtgen(enToplam, boyToplam))
  } yoksa {
    t -= 1
    // kaydırma sınırı zamanla küçülüyor
    dez (x, y) = (0.3 * (enK - 100) * t / tmax, 0.3 * (boyK - 100) * t / tmax)
    için (o <- nesneler) {
      // Konumu SAYI olarak sakla: geri almak için eski yer gerekiyor.
      dez (px, py) = (o.o.konum.x, o.o.konum.y)
      dez taşınım = Nokta(x / 2 - rastgele(x.toInt + 1), y / 2 - rastgele(y.toInt + 1))
      o.o.konumuKur(px + taşınım.x, py + taşınım.y)
      dez yeniEnerji = enerji
      eğer (yeniEnerji > enerjiŞimdi) o.o.konumuKur(px, py) // kötüleşti: geri al
      yoksa enerjiŞimdi = yeniEnerji
    }
  }
}
tuvaliYakınlaştır(0.9, 0.9, 0, 0); gizle()
