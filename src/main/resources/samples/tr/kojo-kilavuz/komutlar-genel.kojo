nesne komutlarGenel {

  dez kod10 = """dez girdi = satıroku("Adınız nedir? ")
satıryaz("Merhaba " + girdi + "! Ne var ne yok?")"""

  dez sayfa = Page(
    name = "daha çok komut var",
    body = tPage("Genel yazılım komutları",
      "Bazı basit komutlar çok faydalıdır. Sık kullanılanları burada görelim.".p,
      table(
        row("yaz".c, """yaz("Selam!")""".c, "yaz('a')".c, "yaz(kaplumbağa0)".c ),
        row("satıryaz".c, """satıryaz("Merhaba!")""".c),
        row("satıroku".c, kod10.c),
        row("".c),
        row("".c),
      ),
      "İngilizce karşılıkları da burada:".p,
      table(
        row("yaz(nesne)".c, "print(object)".c),
        row("satıryaz(nesne)".c, "println(object)".c),
        row("satıroku(istem)".c, "readln(prompt)".c),
        row(""),
        row("belirt(belit, mesaj)".c, "assert(assumption, message)".c),
        row("gerekli(koşul, mesaj)".c, "require(condition, message)".c),
      ),
      "Bu da bitiriş paragrafından bir öncesi.".p,
      "Artık bitti bu küçük örnek. Umarız faydalı oldu...".p
    )
  )

}
