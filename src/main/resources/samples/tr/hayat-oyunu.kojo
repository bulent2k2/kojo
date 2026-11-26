// Hayat Oyunu (Conway) -- önce fareyle deseni çiz, sonra çalıştır.
//
//   Fare  : boş hücreye bas (ve sürükle) = çiz,  dolu hücreye bas (ve sürükle) = sil
//   Boşluk: başlat / durdur      N: tek adım (durmuşken)
//   C     : temizle              R: başa dön (en son düzenlediğin desene)
//   1..9  : hız (0 = en hızlı)   Fare tekerleği: yakınlaş / uzaklaş
//
// Durdurunca yeniden düzenleyebilirsin. Tuşlar çalışmazsa önce tuvale bir kez tıkla.
silVeSakla()
den canlanmaHızı = 6 // 1-10 arası; rakam tuşlarıyla da değişir
// ES: 'dünyanın' kenar boyu
dez ES = 128; dez AS = ES * ES
// tüm hücreleri ölü başlat
tanım boşDünya = Aralık(0, AS).soldanKatla(Yöney[Sayı]())((x, y) => x :+ 0)
den v = boşDünya
// başlangıç deseniyle başlıyoruz. Sonda tanımlı bilinen desenlerden birini seç:
// fpent, diehard, acorn, planör, blok, pervane, oval6, oval6artı1 -- boş başlamak için: Dizin()
v = başlat(v, oval6artı1)
tanım iki = Dizin((0, 1), (0, 0))
tanım üç = (1, 0) :: iki
tanım pervane = (0, 2) :: iki
tanım oval6artı1 = (0,-1)::oval6
tanım oval6 = Dizin((0, 0), (2, 0), (0, 1), (2, 1), (1, 2), (1, -1))

den sonDüzenlenen = v  // R tuşu bu desene geri döner
den nesil = 0          // kaçıncı kuşaktayız
den çalışıyor = yanlış // başta DÜZENLEME kipindeyiz: simülasyon durmuş
den t = 0              // canlandırma sayacı
// 1 ile 10 arası hızdan kaç karede bir yeni kuşak hesaplanacağı (hız 10 = her kare)
den taban: Sayı = 1
tanım hızıKur(hız: Sayı) = {
  canlanmaHızı = enİrisi(1, enUfağı(10, hız))
  taban = kuvveti(11 - canlanmaHızı, 2).toInt
}
hızıKur(canlanmaHızı)

// ---- hücre <-> sahne dönüşümü ----
// Hücre i, sahnede ((i / ES) * 10 - ES * 5, (i % ES) * 10 - ES * 5) merkezli 10x10'luk bir kare.
tanım hücreX(i: Sayı) = (i / ES) * 10 - ES * 5
tanım hücreY(i: Sayı) = (i % ES) * 10 - ES * 5
// Sahnedeki (x, y) noktasının üstündeki hücre. Dünyanın dışındaysa -1.
tanım hücreİndisi(x: Kesir, y: Kesir): Sayı = {
  dez a = yuvarla((x + ES * 5) / 10).toInt
  dez b = yuvarla((y + ES * 5) / 10).toInt
  eğer (a < 0 || a >= ES || b < 0 || b >= ES) -1 yoksa a * ES + b
}
tanım hücreResmi(i: Sayı) = boyaRengi(mavi) * götür(hücreX(i), hücreY(i)) -> Resim.daire(5)

// ---- bir kez çizilenler: zemin, ızgara, yazılar, kaplama ----
dez köşe = -ES * 5 - 5 // dünyanın sol alt köşesi (x de y de)
dez kenar = ES * 10    // dünyanın kenar uzunluğu
// zemin: dünyanın nerede bittiği görünsün (dışındaki hücreler hep ölü sayılıyor)
dez zemin = boyaRengi(Renk(255, 255, 235)) * kalemRengi(gri) * götür(köşe, köşe) -> Resim.dikdörtgen(kenar, kenar)
// ızgara: bütün çizgiler TEK bir resimde, bir kez çiziliyor
dez ızgara = kalemRengi(açıkGri) * kalemBoyu(1) -> Resim.yoldan { yol =>
  için (k <- Aralık(0, ES + 1)) {
    yol.kondur(köşe + k * 10, köşe); yol.doğruÇiz(köşe + k * 10, köşe + kenar)
    yol.kondur(köşe, köşe + k * 10); yol.doğruÇiz(köşe + kenar, köşe + k * 10)
  }
}
dez yardım1 = Resim.yazıRenkli("Fare: boş hücreye bas-sürükle = çiz, dolu hücreye bas-sürükle = sil", 14, koyuGri)
dez yardım2 = Resim.yazıRenkli("Boşluk: başlat/durdur   N: tek adım   C: temizle   R: başa dön   1-9, 0: hız", 14, koyuGri)
dez durumYazısı = Resim.yazıRenkli("", 16, siyah)
// kaplama: dünyanın üstünü örten SAYDAM kare. Fare olaylarını o yakalıyor.
// (Masaüstü Koco'da olayı en üstteki resim alıyor; o yüzden her çizimden sonra öne alıyoruz.)
dez kaplama = boyaRengi(renksiz) * kalemRengi(renksiz) * götür(köşe, köşe) -> Resim.dikdörtgen(kenar, kenar)

çiz(zemin, ızgara, yardım1, yardım2, durumYazısı, kaplama)
// Yazılar tuvalin sol üst köşesinde dursun. Tuval açılışta henüz ortalanmamış olabilir,
// sonra da kaydırılıp yakınlaştırılabilir; o yüzden tuval alanı değişince yeniden yerleştiriyoruz.
den yazılarınYeri = (0.0, 0.0)
tanım yazılarıYerleştir() = {
  dez yer = (tuvalAlanı.x + 10, tuvalAlanı.y + tuvalAlanı.boy - 10)
  eğer (yer != yazılarınYeri) {
    yazılarınYeri = yer
    yardım1.konumuKur(yer._1, yer._2)
    yardım2.konumuKur(yer._1, yer._2 - 20)
    durumYazısı.konumuKur(yer._1, yer._2 - 45)
  }
}
yazılarıYerleştir()

// ---- canlı hücreleri çiz (yalnız bir şey değişince) ----
den ekrandakiler: Dizin[Resim] = Dizin() // ekrandaki hücre resmi (hiç canlı yoksa boş)
tanım ekranıTazele() = {
  ekrandakiler.herbiriİçin(r => r.sil())
  dez canlılar = Aralık(0, AS).dizine.ele(i => v(i) == 1)
  // bütün canlı hücreler tek bir resim kümesinde: silmesi de çizmesi de tek iş
  ekrandakiler = eğer (canlılar.boşMu) Dizin() yoksa Dizin(Resim.dizi(canlılar.işle(i => hücreResmi(i))))
  ekrandakiler.herbiriİçin(r => r.çiz())
  dez kip = eğer (çalışıyor) "ÇALIŞIYOR" yoksa "DÜZENLEME"
  durumYazısı.güncelle(s"$kip    nesil: $nesil    canlı: ${canlılar.boyu}    hız: $canlanmaHızı")
  durumYazısı.öneAl()
  kaplama.öneAl()
}

// ---- düzenleme: fareyle çiz / sil ----
den çizerken = doğru // fareye basılan hücre ölüyse doğru (çiz), canlıysa yanlış (sil)
tanım hücreyiKur(i: Sayı, değer: Sayı) = {
  v = v.değiştir(i, değer)
  sonDüzenlenen = v
  nesil = 0
  ekranıTazele()
}
kaplama.fareyeBasınca { (x, y) =>
  dez i = hücreİndisi(x, y)
  eğer (!çalışıyor && i >= 0) {
    çizerken = v(i) == 0
    hücreyiKur(i, eğer (çizerken) 1 yoksa 0)
  }
}
kaplama.fareyiSürükleyince { (x, y) =>
  dez i = hücreİndisi(x, y)
  dez istenen = eğer (çizerken) 1 yoksa 0
  eğer (!çalışıyor && i >= 0 && v(i) != istenen) hücreyiKur(i, istenen)
}

// ---- kumanda: tuşlar ----
tanım birAdım() = {
  v = Aralık(0, AS).soldanKatla(Yöney[Sayı]())((x, y) => x :+ sonrakiDurum(v, y))
  nesil += 1
  eğer (v.say(_ == 1) == 0) çalışıyor = yanlış // hepsi öldü: dur
  ekranıTazele()
}
tanım başlatDurdur() = {
  eğer (çalışıyor) çalışıyor = yanlış
  yoksa eğer (v.say(_ == 1) > 0) { çalışıyor = doğru; t = 0 }
  ekranıTazele()
}
tanım temizle() = {
  çalışıyor = yanlış
  v = boşDünya; sonDüzenlenen = v; nesil = 0
  ekranıTazele()
}
tanım başaDön() = {
  çalışıyor = yanlış
  v = sonDüzenlenen; nesil = 0
  ekranıTazele()
}
tuşaBasınca { tuş =>
  eğer (tuş == tuşlar.boşluk) başlatDurdur()
  yoksa eğer (tuş == tuşlar.n) { eğer (!çalışıyor) birAdım() }
  yoksa eğer (tuş == tuşlar.c) temizle()
  yoksa eğer (tuş == tuşlar.r) başaDön()
  yoksa eğer (tuş == tuşlar.n0) { hızıKur(10); ekranıTazele() }
  yoksa eğer (tuş >= tuşlar.n1 && tuş <= tuşlar.n9) { hızıKur(tuş - tuşlar.n0); ekranıTazele() }
}
tuvaliEtkinleştir()

satıryaz("Hayat Oyunu: fareyle hücre çiz/sil, Boşluk ile başlat/durdur.")
satıryaz("N: tek adım   C: temizle   R: başa dön   1-9, 0: hız   (tuşlar için önce tuvale tıkla)")
ekranıTazele()

canlandır {
  yazılarıYerleştir()
  eğer (çalışıyor) {
    eğer (t % taban == 0) birAdım()
    t += 1
  }
}

// ---- kural: sonrakiDurum hiç değişmedi (B3/S23; kural(t)'de t = komşular + kendisi) ----
tanım sonrakiDurum(v: Yöney[Sayı], ix: Sayı) = {
  dez kural = Yöney(0, 0, 0, 1, 1, 0, 0, 0, 0, 0)
  dez x = ix / ES; dez y = ix % ES
  dez t = Aralık(0, 3).soldanKatla(0)((st, i) => {
    st + Aralık(0, 3).soldanKatla(0)((s, j) => {
      dez xt = x + i - 1; dez yt = y + j - 1
      s + (eğer ((xt < 0) || (xt >= ES) || (yt < 0) || (yt >= ES)) 0 yoksa v(xt * ES + yt))
    })
  })
  eğer (v(ix) == 1) kural(t) yoksa { eğer (t == 3) 1 yoksa 0 }
}
tanım başlat(v: Yöney[Sayı], p: Dizin[(Sayı, Sayı)]) =
  p.soldanKatla(v)((x, y) => x.değiştir((y._1 + ES / 2) * ES + y._2 + ES / 2, 1))
tanım fpent = Dizin((0, 1), (1, 0), (1, 1), (1, 2), (2, 2))
tanım diehard = Dizin((0, 1), (1, 0), (1, 1), (5, 0), (6, 0), (7, 0), (6, 2))
tanım acorn = Dizin((0, 0), (1, 0), (1, 2), (3, 1), (4, 0), (5, 0), (6, 0))
tanım planör = Dizin((1, 2), (2, 1), (0, 0), (1, 0), (2, 0)) // sağ alta doğru kayar
tanım blok = Dizin((0, 0), (0, 1), (1, 0), (1, 1))          // hiç değişmez
