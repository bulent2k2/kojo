tanım duvar(en: Kesir, boy: Kesir) = {
    dez renk = renkler.darkOliveGreen
    boyaRengi(renk) * kalemRengi(renk) -> Resim.dikdörtgen(en, boy)
}
dez duvarBatı = duvar(50, 500)
dez duvarKuzey = duvar(800, 50)
dez duvarDoğu = duvar(50, 500)
dez duvarGüney = duvar(800, 50)
dez duvar1 = duvar(50, 150)
dez duvar1a = duvar(50, 150)
dez duvar2 = duvar(200, 100)
dez duvar3 = duvar(100, 200)
dez duvar4 = duvar(200, 50)
dez duvar4a = duvar(200, 50)
dez duvar5 = duvar(50, 400)
dez duvar6 = duvar(100, 150)
dez duvar6a = duvar(100, 150)
dez duvar7 = duvar(250, 100)
dez duvar8 = duvar(25, 50)

duvarBatı.konumuKur(-450, -250)
duvarKuzey.konumuKur(-400, 250)
duvarDoğu.konumuKur(400, -250)
duvarGüney.konumuKur(-400, -300)
duvar1.konumuKur(-350, -250)
duvar1a.konumuKur(-350, 100)
duvar2.konumuKur(-400, -50)
duvar3.konumuKur(-150, -100)
duvar4.konumuKur(-250, -200)
duvar4a.konumuKur(-250, 150)
duvar5.konumuKur(50, -200)
duvar6.konumuKur(150, 100)
duvar6a.konumuKur(150, -250)
duvar7.konumuKur(100, -50)
duvar8.konumuKur(-75, -250)

dez duvarlar = Resim.dizi(duvarBatı, duvarKuzey, duvarDoğu, duvarGüney, duvar1, duvar2,
    duvar3, duvar4, duvar1a, duvar4a, duvar5, duvar6, duvar6a, duvar7, duvar8)
