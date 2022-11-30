// Mekanik simülasyon == kinematik bir canlandırma, hepsi fizik

tümEkran()
silVeSakla()
eksenleriGöster()
gridiGöster()
yaklaş(0.75, 500, 300)

dez a = 20.0 // başlangıç ivmesi
dez b = 10.0 // başlangıç hızı. Bunu -100 ya da -200 yapmayı dene!

dez araba = yeniKaplumbağa(0, 0, Costume.car)

dez konumEğrisi = yeniKaplumbağa(0, 0, Costume.pencil)
konumEğrisi.kalemRenginiKur(mavi)

dez hızEğrisi = yeniKaplumbağa(0, b, Costume.pencil)
hızEğrisi.kalemRenginiKur(yeşil)

dez t0 = buSaniye // göreceli olarak bu anın zamanı
dez geçenZaman = yeniKaplumbağa(100, -50)

tanım zamanıGöster(t: Yazı) {
    geçenZaman.sil()
    geçenZaman.gizle()
    geçenZaman.kalemRenginiKur(mavi)
    geçenZaman.yazı(t)
}

canlandır {
    dez t = buSaniye - t0 // program başlayalı beri kaç saniye geçmiş
    // kinematik denklemlerle arabanın hızını ve ivmesini hesaplayalım
    dez h = b + a * t 
    dez x = b * t + 0.5 * a * t * t 
    araba.konumuKur(x, 10)
    konumEğrisi.ilerle(t * 50, x)
    hızEğrisi.ilerle(t * 50, h)
    zamanıGöster(f"Zaman: $t%.1f saniye")
}

tuşaBasınca { t =>
    t eşle {
      durum tuşlar.VK_ESCAPE => durdur()  // sol üst tuş
      durum _ =>
    }
}
