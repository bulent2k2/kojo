//#yükle tr/tahta

class Durum(val tahta: Tahta, val sıra: Taş) {
    val karşıTaş = if (sıra == Beyaz) Siyah else Beyaz
    def skor = tahta.drm(sıra) - tahta.drm(karşıTaş)
    def bitti = oyunBittiMi
    def oyunBittiMi = {
        if (yasallar.boyu > 0) yanlış else {
            val yeniDurum = new Durum(tahta, karşıTaş)
            yeniDurum.yasallar.boyu == 0
        }
    }
    def seçenekler = yasallar
    def yasallar = tahta.yasallar(sıra)
    def oyna(oda: Oda) = {
        val yTahta = tahta.oyna(sıra, oda)
        new Durum(yTahta, karşıTaş)
    }
}
