/*
 * Copyright (C) 2009 Anthony Bagwell
 * Copyright (C) 2009 Phil Bagwell <pbagwell@bluewin.ch>
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
 * Copyright (C) 2021 Bülent Başaran <ben@scala.org>
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */

// Click the Run button in the tool-bar above to start the story
//
// =================================================================
//
// This Tutorial originally created by Anthony Bagwell for simplyscala.com,
// then extended and adapted for Kojo by Phil Bagwell.
// Some of the Kojo examples provided by Lalit Pant.
// Turkish translation is lovingly provided by Bülent Başaran.
// Please note that simplyscala.com is no more. It can be revisited at the 
// wonderful web archive:
//    http://web.archive.org/web/20130305041026/http://www.simplyscala.com

// Set Up Styles for tutorial pages
val pageStyle = "color:black;background-color:#99CCFF; margin:15px;font-size:small;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;color:maroon;"
val codeStyle = "font-size:90%;"
val smallNoteStyle = "color:gray;font-size:95%;"
val sublistStyle = "margin-left:60px;"

import language.implicitConversions
import language.postfixOps

showVerboseOutput()
retainSingleLineCode()
def pgHeader(hdr: String) =
    <p style={headerStyle}>
        {new xml.Unparsed(hdr)}
        {nav}
        <hr/>
        
    </p>
 
def tCode(cd:String)=Para(
    <div style="background-color:CCFFFF;"> <pre style={codeStyle}> {cd} </pre> </div>,
    code = {clearOutput;stRunCode(cd);stSetScript(cd)}
) 

var pages = new collection.mutable.ListBuffer[StoryPage]
var pg: StoryPage = _
var header: xml.Node = _

def link(page: String) = "http://localpage/%s" format(page)
val homeLink = <div style={smallNoteStyle+centerStyle}><a href={link("home#7")}>Start Page</a></div>

def nav={<div style={smallNoteStyle}>
        <a href={link(("Menu").toString)}>Menü</a>
         </div>}

// Mark up support

implicit def toSHtm(s:String):SHtm={new SHtm(escTrx(s))}
def escTrx(s:String) = s.replace("&" , "&amp;").replace(">" , "&gt;").replace("<" , "&lt;")
def escRem(s:String) = s.replace("&amp;","&"  ).replace("&gt;",">").replace("&lt;","<")
def row(c:SHtm *)={
    val r=c.map(x=>{new SHtm("<td>" + x.s + "</td>")}).reduce(_ + _) 
    new SHtm("<tr>" + r.s + "</tr>")
}
def table(c:SHtm *)={
    val r=c.reduce(_ + _) 
    new SHtm("""<table border="1">""" + r.s + "</table>")
}
//def toHtm(h:SHtm *)={h.reduce(_ + _)}

def tPage(title:String,h:SHtm *)={
    <body style={pageStyle}>
    <p style={headerStyle}>
        {new xml.Unparsed(title)}
        {nav}
        <hr/>
        {new xml.Unparsed(h.reduce(_ + _).s)}
    
    </p>
    </body>    
    
}
val codeExamples = new Array[String](1000)
var codeID = 0 
// Mark up DSL definitions class

class SHtm (var s:String){
    def h2=new SHtm("<h2>" + s + "</h2>")    
    def h3=new SHtm("<h3>" + s + "</h3>")
    def h4=new SHtm("<h4>" + s + "</h4>")
    def i=new SHtm("<i>" + s + "</i>")    
    def h1=new SHtm("<h1>" + s + "</h1>")
    def p=new SHtm("<p>" + s + "</p>")
    def b=new SHtm("<b>" + s + "</b>")
    def link(url:String)=new SHtm("<a href=\"http://" + url + " \">" + s + "</a></br>")
    def c={
    codeID+=1
    codeExamples(codeID)=escRem(s)
    new SHtm("""<hr/><div style=background-color:CCFFFF;"> <pre><code><a href="http://runhandler/example/""" + 
                 codeID.toString +
                 """ " style="text-decoration: none;font-size:x-small;">""" + s + """</a></code></pre><hr/></div>""")
    }
    def + (a:SHtm)={new SHtm(s + "\n" + a.s) }
}


// **********  Start of Tutorial  *************
pages += Page(
  name = "Menu",
  body = <body style={pageStyle}>
    <div style={pageStyle+centerStyle}> <h1>Scala'ya Hızlı Giriş</h1> </div>
    <div style={pageStyle}>
    <p>Bu kılavuzcuk simplyscala.com'dan Kojo'ya uyarlanarak yazılmıştır. Kılavuzda ileri veya geri gitmek için en aşağıdaki üçgenlere tıkla. Ya da istediğin bölüme atlamak için aşağıdaki başlıklardan birine tıkla.</p> <br/>
    <a href={link("GS")}>Başlayalım</a> <br/>
    <a href={link("Flow")}>Program Akışı If, Else ve While Komutları</a> <br/>
    <a href={link("Literals")}>Değişmezler, Sayılar, Kesirler ve Diziler</a> <br/>
    <a href={link("Functions")}>İşlevler</a> <br/>
    <a href={link("OandC")}>Nesne ve Sınıflar</a> <br/>
    <a href={link("PMS")}>Örüntü Eşleme ve Switch ve Case Komutları</a> <br/>
    <a href={link("BTree")}>İleri Eşleme Yöntemleri - İkili Ağaç</a> <br/>
    <a href={link("STI")}>Dingin Türleme ve Tür Çıkarımı</a> <br/>
    <a href={link("FAO")}>İşlevler Birer Nesnedir</a> <br/>
    <a href={link("Tup")}>Sıralamalar (Tuple)</a> <br/>
    <a href={link("MF")}>Matematiksel İşlevler</a> <br/>
    <a href={link("OPA")}>İşlem Önceliği ve Birleşmeliği</a> <br/>
    <a href={link("US")}>Yazıların (String) Kullanılışı</a> <br/>
    <a href={link("UL")}>Dizinlerin (List) Kullanılışı</a> <br/>
    <a href={link("UT")}>Kamplumbacığın Kullanılışı</a> <br/>
    <a href={link("GAG")}>Çizim ve Oyun</a> <br/>
    <a href={link("LM")}>Daha Çok Öğrenelim</a> <br/>
    <br/>
    <p>Not: Anthony Bagwell'in simplyscala.com sitesi artık yaşamıyor. Arşivini şurada bulabilirsin: <a href="http://web.archive.org/web/20130305041026/http://www.simplyscala.com">simplyscala.com from web.archive.org</a>.</p>
    </div>
    </body>
)

pages += Page(
  name = "GS",
  body = tPage("Başlayalım",
    "Başlayalım".h2,
    "Kılavuzcuğumuzda pek çok yazılımcık örneği bulacaksın. Onları buradan kolaylıkla çalıştırabilirsin. Herhangi birinin üstüne tıkladığında o örnek, yazılımcık düzenleyicisine taşınır ve çalışır. Yani hepsini yazmana gerek yok. Hatta yazılımcığı çalıştırma düğmesine bile tıklamaya gerek kalmıyor. Ayrıca, işin bir de güzel yanı şu: istediğin değişiklileri orada yapıp tekrar çalıştırabilirsin. Bunu için düzenleyicinin hemen üzerindeki yeşil üçgene tıklaman yeter. Haydi şimdi bunu deneyelim. Aşağıdaki mesajı Bülent yerine kendi adını yazıp tekrar çalıştırıver. Bu arada benim adım Bülent. Bu satırları siz Türkçe severler için severek çevirdim.".p,
    """satıryaz("merhaba dünya! kaplumbacık ve Bülent'ten hepinize selamlar, sevgiler")""".c,
    "Yaptığın değişikliğin sonucunu Çıktı Gözünde ya da Çizim Tuvalinde hemen göreceksin.".p,
    "Bu kılavuzcuk ilk sayfada gördüğün bölümlerden oluşuyor. Bir sonraki bölüme geçmek için, ya da bir önceki bölüme dönmek için bu pencerenin altındaki sağa ve sola bakan mavi daire içindeki beyaz üçgenlere tıkla. İlgini çeken bölüme de kolaylıkla atlayabilirsin istersen. Bunun için ilk sayfaya geri dönüp ordaki mavi ve altı çizili başlıklardan herhangi birine tıklayabilirsin. İlk sayfaya dönmek için en üstte solda menü yazıyor ya, ona tıklayıver. En alttaki mavi daire içindeki kareye basarsan bu kılavuzcuktan çıkıp Kojo'nun normal düzenine dönebilirsin. Çıkınca kılavuzcukta son seçtiğin yazılımcık kapanmaz. Onun üzerinde değişikler yapıp çalıştırmaya odaklanabilirsin.".p,
    "Daha önce çalışan bir yazılıma dönmek de kolay: yazılım düzenleyicisinin üst kısmındaki menünün ortasında sağa ve sola bakan mavi oklara tıkla. Seçtiğin yazılımı düzenlemeye kaldığın yerden devam edebilirsin. Her örneği deneyebilir, istediğine geri dönüp değişiklikler yapıp tekrar çalıştırabilirsin. Değişik fikirler dener, programlama dilini daha iyi tanıyıp yazım, gramer kurallarını daha çabuk öğrenebilirsin. Yazılımcık düzenleyicisindeki programları diske kaydedebilir ve sonra oradan geri yükleyebilirsin. Bunun için Dosya menüsüdeki komutları kullan.".p,
    "Şu anda kullandığın Kojo öğrenim ortamı, çok gelişmiş ve uzman bilgisayar mühendislerinin en sevdikleri dillerin önde gelenlerinden Scala programlama dilini öğrenmene yardımcı olmak için hem çok faydalı hem de eğlendirici özellikler ve beceriler içeriyor. Müzik çalan programlar yazabilir, ileri matematik kavramlarını resimler ve grafikler çizerek inceleyip daha iyi öğrenir, değişik tür oyunları hem oynayabilir hem de nasıl yazıldıklarını kolayca öğrenebilir, hatta fizik deneyleri bile yapabilirsin! En güzeli Kojo'nun kaplumbağaları var! Onlara yol göstererek çizimler yaptırabilirsin. Küçük hemen anlayıp seveceğin tek kaplumbağalı bir örnekle başlayalım mı? Kaplumbağa yürüsün ve bir taraftan da bir üçgen çizsin istersen, bu yazılımcığa tıklaman yeter:".p,
    """
  sil
  ileri(100)
  sağ(120)
  ileri(100)
  sağ(120)
  ileri(100)
  sağ(120)
""".c,
    "Bu kılavuzcuğun kaplumbağacığın kullanılışı başlıklı (sondan üçüncü) bölümünde daha pek çok örnek yazılımcık ve kaplumbağanın anladığı komutların bir listesini bulabilirsin.".p,
    "Scala dili tam anlamıyla genel ve güçlü bir bilgisayar programla dilidir. Özelliklerinin çoğunluğu uzman programcılara ve bilgisayar mühendislerine tanıdık gelecektir. Bu kılavuzcuktan faydalanmak için programlama dili bilmesen de olur, ama elbette daha önce başka bir programlama diliyle deneyimin olduysa daha hızlı ilerleyebilirsin. Onun için anladığın yerleri ve bölümleri hızlı geçmen hatta atlaman bile doğal olur.".p,
    "Öyleyse, başlayalım mı artık?".p,
    "Deyişler".h2,
    "Basit matematiksel deyişler pek şaşırtıcı gelmeyecektir. Tanıdık matematik işlemleri ve öncelikleri Scala'da da geçerlidir. İşlemleri daha açık açık sıralamak için parantez kullanılır.".p,
    "1+2".c,
    "3+4*(2-3)".c,
    """23%5  // Tabanlı aritmetik işlemi yani tabana bölünce ne kalıyorsa onu verir""".c,
    "6/4".c,
    "Bu son işlemde önemli bir nokta var. Tam sayılar bölününce sonuç yine bir tam sayı olur ve kalan dikkate alınmaz. Ufak bir değişiklikle kesirli ve daha doğru bir işlem yapabiliriz:".p,
    "6/4.0".c,
    "Biraz daha uzun bir işlem yapalım:".p,
    "3.5*9.4+7/5".c,
    """Scala tam ve kesirli gibi değişik bir kaç sayı tipini tanır. Hepsinin bir tür adı var. Örneğin Sayı, Uzun, İriSayı, Kesir, vb. Bunları ileride detaylı olarak inceleyeceğiz. Yukarıdaki iki örnekten 4.0 ve 3.5 kesir (İngilizcesi Double), 4 ve 6 ise birer tam sayıdır (İngilizcede Integer, kısaca Int). Eğer bir matematiksel deyiş bir kaç tür sayı içiriyorsa Scala derleyicisi mümkünse sayının türünü biraz zorlayarak değiştirebilir.""".p,
    """Bir deyişin sonucunu bir değişken kullanarak kaydedip daha sonra yine kullanabiliriz. Değişken isimleri (ve sonra göreceğimiz başka tür isimler) harf, sayı ve * / + - : = ! < > & ^ | gibi semboller kullanarak yazılır. Örneğin, "FutbolTopu", "BilardoTopuBeyaz1", "yardımHattı", "*+" and "Res4" (Result yani sonuç)...""".p,
    """Bunun için iki yöntem vardır: "var" ve "val" komut sözcükleri. "val" (İngilizce value sözcüğünün kısaltması) ile sabit ve hiç değişmeyecek değerleri ve sonuçları saklayabiliriz. Ya neden bir de "var" (ingilizce variable sözcüğünün kısaltması) komutu var (var var ama varı kullanma! 8-)? Aşağıda bir örnekle ikisinin farkını hemen anlayacağız. "val" ile tanımlanan değerlerin sabit olması (ingilizcede 'immutable value') aslında çok önemli bir işlevsel programlama (functional programming) kavramıdır, ama bunu daha sonra yeri gelince daha iyi anlayacağız. Şimdilik mümkün oldukça 'var' yerine 'val' komutunu kullanmaya dikkat edelim. Bu sayede programın başka bir yerindeki değişkenleri yanlışlıkla bozamayız.""".p,
    "val noktaSayısı=34+5".c,
    """Bir ya da daha fazla sayıda işlemin sonucunu çıktı gözüne 'satıryaz(deyiş1, deyiş2, deyişn)' komutunu kullanarak yazabiliriz. Deyişler arasına virgül koymayı unutmayalım. Gerekmez ama istersek virgülden sonra boşluk bırakarak yazılımın okunuşu biraz daha kolaylaştırabiliriz. Ama çıktı da durum başka. Orada boşluk bırakmak nasıl olur yakında göreceğiz.""".p,
    """
satıryaz(noktaSayısı,3+2,noktaSayısı/2, 3.9/2.3)
var boy=noktaSayısı+4
satıryaz(boy)""".c,
    "Farkettiniz mi, çıktı gözü her değerin adını ve değerini yazmakla kalmıyor ikisinin arasında o değerin türünü de yazıyor, Int (Sayı), Double (Kesir) vb. Şimdi deneyelim,".p,
    """noktaSayısı=10 // bu hata verir. val ile tanımlanan değişkenlerin değerleri değiştirilemez""".c,
    """'error' hata demek. Scala derleyicisi (compiler), 'reassignment to val' yani sabit bir değeri değiştirmek hata olur diyor ve izin vermiyor.""".p,
    """boy+=4 // yani boyunu 4 nokta uzatalım
satıryaz(boy)""".c,
    """Okuyanları bilgilendirmek ve kendimize anımsatmak için satır sonlarına // yani iki taksim ya da bölüm işaretinden sonra bir açıklama yazabiliriz. Bir satıra sığmıyorsa /* ile başlayıp */ ile biten daha uzun açıklamalar ekleyebiliriz. Scala derleyicisi bunları göz ardı eder ve bu sayesede bilgisayarın kafası karışmaz 8-).""".p,
    """
/*
  Çok satırlı bir açıklama örneği
  Fahrenayttan santigrata çevirelim
*/
val dereceF = 98.4
// Tek satırda açıklama: 0S = 32F. 9F artış 5C artışa denk
satıryaz(dereceF, "derece Fahrenayt", (dereceF-32)*5/9, "derece Santigrat")  // satır sonu açıklama
""".c,
    "'satıryaz' komutuyla yazı da yazdık yukarıda gördüğün gibi. Böyle çift tırnaklar içine alınan yazıların türüne Dizi (İngilizcesi String) diyoruz. Bu tür, sadece yazı yazmak için değil, yazılarla işlemler yapmak için de kullanılabilir:".p,
    """val adım = "Mustafa Kemal"
val mesaj = "Merhaba " + adım
satıryaz(mesaj)
""".c,
    "Toplama işareti sanki toplama yaparmış gibi yazıları birbirine ekleyiveriyor. Mantıklı değil mi? Dizi türüyle daha neler yazılabilir neler! İki bölüm sonra başka örnekler de göreceğiz.".p,
    "Duymuşsundur eminim, bilgisayar devreleri aslında 2, 3, 4 gibi sayıları bile bilmez. Onun yerine sadece 0 ve 1 sayılarını tanır. Hatta tanımak dedik de aslında sadece voltaj değerlerini ve elektrik akımlarını tanır onlar. Bu uzun, ilginç ve çok keyifli bir öyküdür. Benim gibi elektrik mühendisi olmak istersen, bana emaille selam ve sorularını yollayabilirsin. Neyse, konumuza dönelim. Daha büyük sayılarla işlemler yapmak için bilgisayar onları bir 0 ve 1 dizisi olarak ele alır ve içindeki sayısız mantık devreleri sayesinde toplama, çıkarma, çarpma, bölme ve hatta türev ve integral alma gibi daha ileri matematik işlemlerini kolayca ve hiç üşenmeden halleder. Bunların detayı bilgisayar uzmanlarının işi. Biz 0 ve 1 dizilerine dönelim, çünkü herşey onlarla başlıyor! İngilizcede 'binary arithmetic' denir. Biz çift tabanlı sayma diyelim. Her sayı, 0 ve 1'lerden oluşan bir dizi olduğu için, direk onun parçacıkları üzerinde de işlemler yapabiliriz. Bu işlemlere 'bitwise' yani parçacık işlemi denir. Bu 0 ve 1 dizilerinin her bir elemanına İngilizcede 'bit' denir. Saçlarımızda yaşayan ve zararsız küçük böcekcikler değil elbet! İngilizcede azıcık, küçücük anlamlarına geliyor. Biz parçacık diyelim istersen. Çok uzattık. Kusura kalma. Şimdi parçacıkları teker teker nasıl işleme sokarız bir kaç örnek görelim:".p,
    """3&2 // mantıksal 've' işlemi de denir (ingilizcesi 'logic and'). Sadece 1 ve 1 sonuç olarak 1 verir. Girdilerden biri 0 olursa sonucu da 0 olur.""".c,
    """1|2 // mantıksal veya (or) işlemi. Sadece 0 veya 0 sonuç olarak 0 verir. Biri 1 olursa sonuç da 1 olur. """.c,
    """1^2 // mantıksal dışlayan veya işlemi (xor yani exclusive or). Sadece biri 1 öbürü 0 olunca 1 verir. """.c,
    """1<<2 // parçacıkları iki kere sola kaydır """.c,
    """-24>>2 // sağa kaydır ama eksiyse eksi kalsın """.c,
    """-14>>>2 // sağa kaydırıyor ama sonuca bakın! Ben anlamadım vallahi. Ya sen?""".c,
    "Bakın bu çok ilginç. Parçacıkları sola kaydırmak ikiyle çarpmaya denk! Sola kaydırmaksa ikiye bölmeye benzemiyor mu? Bu daha önce de dediğim gibi uzmanlık konusu. Üzerinde yazılmış pekçok bilimsel makale ve ders kitapları var. Hatta bazıları çok azımızın anlayabileceği yüksek ihtisas kitapları! Bugünlük bu kadarı fazla bile. Ama sen istersen bu yazılımcığı kurcala. Dene. Bakarsın uzman olmak istersin. Neden olmasın?".p,
    "Farkında mısınız? Bu kadarcık bilgiyle bile artık çok güçlü bir hesap makinemiz oldu. Ama dahası var! Bir sonraki bölümde program akışı nasıl düzenlenir öğrenecek ve yazılımcıklarımızı çok daha becerikli hale getireceğiz.".p
  )
)

pages += Page(
  name = "Flow",
  body = tPage("Program Akışı If, Else ve While Komutları",
    "Şu ana kadar yazdığımız yazılımcıkların komutları, baştan sona kadar, sırayla, satır satır ve teker teker çalıştılar. Ama pek çok durumda program akışını değiştirmek isteriz. Yani komutların çalışma sırasını duruma göre değiştiririz. Bu sayede, bazı komutları yineler, bazılarını atlarız.".p,
    "İlk önce bir komut dizisi oluşturuz. Bunun için komutları kıvrık parantezler, yani {} içine alıyoruz. Bu komut dizisi istediğimiz kadar satır ve hatta başka komut dizileri de içerebilir. Dizinin içindeki son komut dizinin değerini belirler.".p,
    "Program akışını değiştirmek için kullandığımız bir kaç değişik yöntem var. Gelin en önemlilerinden biri olan, eğer/yoksa anlamına gelen, koşullar oluşturmaya yarayan 'if' ve 'else' yapısıyla başlayalım.".p,"if".h3,
    """Genel olarak şöyle yazarız: "if (koşul) dizi1/deyiş1 else dizi2/deyiş2". Eğer koşul doğruysa ilk kısım yani dizi1/deyiş1 çalışır. Yoksa 'else' sözcüğünden sonra gelenler çalışır.""".p,
    """if(doğru) satıryaz("Doğru") else satıryaz("Doğru değil")""".c,
    """Bunu daha kısa da yazabiliriz. 'if/else' deyişi hep bir değer verir. Bakın bunu da satıryaz komutuyla yazıyoruz.""".p,
    """satıryaz(if(doğru) "Doğru" else "Doğru değil")""".c,
    """Kulladığımız koşulun geçerli olması için 'doğru' ya da 'yanlış' (İngilizcede 'true' veya 'false') değerlerinden birini vermesi gerekir. Bu değerlerin türüne biz İkil deriz (İngilizcesi Boolean). Bu koşulu sağlayan işlemlere karşılaştırma işlemi deriz. Bunlar matematiksel işlemler ya da benzerleri olabilir. İlk önce matematiksel olanları, yani sayıları karşılaştıran işlemleri görelim. Başka tür değerleri (örneğin sözcükleri) karşılaştırmayı sonraya bırakalım.""".p,
    """1>2 // büyüktür""".c,
    """1<2 // küçüktür""".c,
    """1==2 // eşittir""".c,
    """1>=2 // büyük ya da eşittir""".c,
    """1!=2 // eşit değil""".c,
    """1<=2 // küçük ya da eşittir""".c,
    "if/else yapısının nasıl çalıştığını anlamak çok kolay. Koşul doğruysa 'else' sözcüğünden önceki komutlar çalıştırılırlar. Yok, eğer koşul doğru değilse, o halde 'else' sözcüğünden sonraki komutlar çalıştırılır. Scala ve diğer işlevsel dillere benzemeyen programlama dilleri (örneğin, C, Java, Python), if/else yapısını sadece akışı belirlemek için kullanırlar. Ama Scala, Haskell ve diğer işlevsel diller gibi, if/else yapısından bir değer beklerler. Onun için 'if' genelde tek başına kullanılamaz. Arkasından hemen 'else' gelir ve iki durumda da bir değer geri bildirilir. Bunun bir istisnası da vardır, ama onu da sonraya bırakalım. Bakın bu örneklerde koşul olarak hep yalın sayıları karşılaştırıyoruz. Ama elbette başka 'val' değerler ve komutlar da kullanabiliriz. Yeter ki bir İkil, yani doğru ya da yanlış değeri olsun.".p,
    """if(1>2) 4 else 5 // büyüktür""".c,
    """if (1<2) 6 else 7 // küçüktür""".c,
    """val deneme1=if (1==2) 8 else 9 // eşittir""".c,
    """val kitapsa = 6>=3
val değer=16
val sayı=10
val satış=if (kitapsa)değer*sayı else {
  val birim = değer/sayı // a block of expressions enclosed in {}
  birim*3  // this, the blocks last expression gives the blocks return value
}""".c,
    "Gerçek hayatta da olduğu gibi bazen bir kaç koşul bir araya gelir. Bu durumda iki tane mantıksal işlem kullanırız. Bunlardan birincisi '&&' mantıksal 'Ve' anlamına gelir. İkincisi de '||'  mantıksal 'Veya' anlamına gelir. Bunları sakın parçacıkları işleyen '&' ve '|' işlemleriyle karıştırmayalım. Aslında ilişkili ve benzer kavramlar. Ama dikkat edelim. Çok uzatmadan birkaç örnek görelim, göreceksin çok doğal gelecek. Ne de olsa mantık hepimizde var. ".p,
    """val kitapsa = 6>=3
val değer=16
val sayı=10
val satış=if (((kitapsa)&&(değer>5))||(sayı>30))değer*sayı else değer/sayı""".c,
    "while".h2,
    """İngilizce'de pek çok farklı anlama gelen 'while' sözcüğünü Türkçemize 'o halde' ya da 'o sırada' diye çevirebiliriz. Bu yapı programlama dillerinde çok kullanılır. Genel olarak, "while (koşul) dizi/deyiş" yapısı kullanarak komut dizisini tekrar tekrar çalıştırabiliriz. Koşul sağlandığı sürece yineleme devam eder. Koşul değişince, yani artık doğru olmadığında yineleme son bulur. Bir örnekle anlamak çok daha kolay olacak. Ama ilk önce bir değişkenle başlayalım:""".p,
    """var toplam=18
while(toplam < 15) toplam += 5
yaz(toplam)
""".c,
    """Kolay değil mi? Sizce kaç kere yinelenecek komut? Sonunda toplam kaç olacak? Bu biraz hileli bir soru oldu. Aslında yinelenecek mi, toplam değişecek mi diye mi sormalıydık? Toplamın ilk değerini ya da koşulu değiştirip tekrar çalıştırabiliriz elbet. Bu 'while' yapısını tersine de çevirebiliriz: "do dizi/deyiş while (koşul)" 'do' İngilizce'de de doremi gibi nota adıdır, ama daha yaygın anlamı 'Yap' ya da 'Yapmak' demek. Hatta emir kipi. Yani lütfen yap yani çalıştır diyoruz. Bakın burada ilk önce komut dizisi çalıştırılır sonra koşula bakılır. Doğruysa komut dizisi yinelenir:""".p,
    """var toplam=18
do toplam += 5
while (toplam < 15)
yaz(toplam)
""".c,
    "Gördük ki bu sefer toplam 23 oldu. Bir önceki örnekteki halbuki 18 olmuştu. Bakın bu yapıyı iki sayının ortak paydalarının en büyüğünü hesaplamak için kullanalım:".p,
    """// en büyük ortak paydayı bulalım
var x = 36
var y = 99
while (x != 0) {
    val yardımcı = x
    x = y % x
    y = yardımcı
}
satıryaz("ortak paydaların en büyüğü: " + y)
""".c,
    "for".h2,
    """Komut dizilerini kolayca yinelemek için kullanabileceğimiz bir yöntem daha var ki belki de en faydalısı. "for (aralık) dizi/deyiş" sayesinde verilen aralıktaki her bir değer için dizi/deyiş yinelenir. 'for' elbet yine ingilizce bir sözcük. Anlamı 'için' demek ("for you" senin için demek). Aralık da nedir mi? Hemen bir örnek görelim:""".p,
    """for(i <- 1 to 4) yaz("merhaba!")""".c,
    "'to' sözcüğü de bizim yapım/çekim ekimiz gibi, birden dörde kadar derken dörde sözcüğündeki '-e' anlamında. İngilizceyle Türkçe ne kadar çok farklı sanki, değil mi? Bir de bana sorun. 22 yaşında ilk defa yaşamak için Amerika'ya gittiğimde o kadar zorluk çektim ki! Güya iyi biliyordum hem de İngilizceyi! İşimize dönelim: Aralık burada birden dörde kadar olan sayılar elbet. 'i' değişkeni 1 değeriyle başlıyor ve her tekrarda bir artıyor. Son sayı burada 4. Ama sonuncu sayıya gelmeden hemen önce durmak istersek 'to' yerine 'until' sözcüğünü kullanıyoruz, yani kadar anlamına gelen İngilizce sözcük:".p,
    """for (i <- 1 until 12) {
  val kare = i*i
  satıryaz(i, kare)
}""".c,
    "Biliyor musun, bu yineleme işlemlerini birden çok boyutta yapmak bilgisayarla çok kolay. Birden fazla aralık vereceğiz ve her aralık için de bir değişken. Tek dikkat etmemiz gereken ikisi arasına bir noktalı virgül koymak. Bakın ne kolay!".p,
    """for(i <- 1 until 5 ; j <- "abc") satıryaz(i, j)""".c,
    """Bakın şu işe! Sayı yerine harfler kullandık! "for" yapısı içinde kullandığımız kümeler illa da sayılardan oluşmak zorunda değil yani. Genel olarak biz bunlara küme tekerleme diyebiliriz (İngilizcesi: iterating through a set or collection) yani teker teker her küme elemanını ele alıyoruz. "abc" yazısı da aslında bir harf kümesi ya da kolleksiyonu. Bakın hep küme ya da kolleksiyon dedim. Bu kavramlar yakın ama ufak farklılıkları var. Daha sonra bunlara verilen anlamı daha iyi anlayacağız. Şu anda çok da önemli değil gerçekten. Neyse. Harflerle tekerlemeye bir örnek daha verelim ve devam edelim:""".p,
    """for(c<-"merhaba!") satıryaz(c)""".c,
    "Şimdi de matematik, bilhassa kartezyen geometrisi sevenlere bir süprizimiz var. Kaplumbağacığı kullanarak bir eğri çizelim. Neyin eğrisi? İki boyutlu bir poligon. Genel olarak a*x^2 + b*x + c diye yazabiliriz. Yine bu çok faydalı olan 'for' yapısıyla:".p,
    """sil
def eğri(x:Double) = 0.001 * x * x + 0.5 * x + 10   // 'def' define yani tanımla demek. Bunu daha sonra daha iyi anlatacağız. 
gridOn();axesOn() // kare çizgileri ve x ve y eksenlerini çizelim
val aralık = 200
atla(-aralık,eğri(-aralık))
for(x <- -aralık+10 to aralık; if (x % 10 == 0)) lineTo(x, eğri(x))
""".c,
    "Eksenleri silelim. Ve bir sonraki bölüme devam edelim!".p,
    "axesOff(); gridOff()".c
  )
)

pages += Page(
    name = "Literals",
    body = tPage("Değişmezler, Sayılar, Kesirler ve Diziler",
                 "Literals allow you to define the value of one of the basic types in your code. They are pretty much the same as those you find in Java and similar to other languages".p,

                 "Base Types".h2,
                 "Scala has a set of base types that are predefined. They are summarized as follows.".p,
                 table(
            row("Byte","8-bit signed 2's complement integer (-128 to 127 inclusive)"),
            row("Short","16-bit signed 2's complement integer (-32,768 to 32,767 inclusive)"),
            row("Int","32-bit signed 2's complement integer (-2,147,483,648 to 2,147,483,647 inclusive)"),
            row("Long","64-bit signed 2's complement integer (-2^63 to 2^63-1, inclusive)"),
            row("Float","32-bit IEEE 754 single-precision float"),
            row("Double","64-bit IEEE 754 double-precision float"),
            row("Char","16-bit unsigned Unicode character"),
            row("String","a sequence of Unicode characters"),
            row("Boolean","true/false")),
                 "Integers".h2,
                 "There are four types of integer namely Int, Long, Short, and Byte. You can use literals expressed in different bases - decimal and hexadecimal. You signal which form you are using by the first characters.".p,
                 "Decimal(base 10): Any number starting with a non-zero digit.".p,
                 "17".c,
                 "298".c,
                 "Hexadecimal(base 16): starts with a 0x or 0X and is followed by the hex digits 0 to 9, a to f or A to F".p, 
                 """0x23  //hex = 35 dec""".c,
                 """0x01FF  //hex = 511 dec""".c,
                 """0xcb17 //hex = 51991 dec""".c,
                 """By default these will be created as type Int. You can force them to type Long by adding the letter "l" or "L".""".p,
                 """0XFAF1L // hex long = 64241""".c,
                 "You can assign literals to Short or Byte variables. However, the value must be in the appropriate aralık for that type.".p,
                 "val abyte: Byte = 27".c,
                 "val ashort: Short = 1024".c,
                 """val errbyte: Byte = 128 // Error - not in range -128 to 127""".c,
                 "Floating point".h3,
                 "Floating point literals are numbers containing a decimal point. They must start with a non-zero digit and can be followed by E or e that prefixes an exponent indicating the power of 10 to use. Some examples are:-".p,

                 "9.876".c,
                 "val tiny= 1.2345e-5".c,
                 "val large = 9.87E45".c,
                 """By default floating literals are created as type Double but you can force them to type Float by adding the letter "f" or "F". Optionally "d" or "D" can be appended to a floating literal.""".p, 
                 "val sma = 1.5324F".c,
                 "val ams = 3e5f".c,
                 "Character".h3,
                 "Character literals are specified by any Unicode character in single quotes.".p,
                 "val chr = 'A'".c,
                 "You may also specify its value in this way:".p,
                 "Unicode: A hexidecimal number between '\\u0000' and '\\uFFFF'".p,
                 "val chra = \'\\u0041\' // is an A ".c,
                 "val chre = \'\\u0045\' // is an E ".c,
                 "Finally, there are also a few character literals represented by special escape sequences. These all start with a back slash. See reference for complete list.".p,
                 "Strings".h3,
                 "A string literal is a sequence of characters enclosed in double quotes:".p,
                 """val helloW = "hello world" """.c,
                 "To include some special characters in your string it is convenient to use 'escape' sequences. These start with a \\ and are followed by a character designating the required character.".p,
                 table(
            row("""\n""", "line feed","""\b""", "backspace","""\t""",      "tab","""\f""", "form feed"),
            row("""\r""", "carriage return","""\" """, "double quote", """\'""", "single quote","""\\""", "backslash")
        ),
                 """val someEsc = "\\\"\'" """.c,
                 "Scala includes a special syntax to avoid these multiple escape characters. if you start and end a string with triple quotes (\"\"\") then all the characters such as newlines, quotation marks, and special characters are treated just like others.".p,
                 "satıryaz(\"\"\"Welcome to Kojo.\n\"Turtle\" graphics \\Turtle section\\ for more information.\"\"\")".c,
                 "Boolean".h3,
                 "The Boolean type has two possible values and the literals are true or false:".p,
                 "val isBig = true".c,
                 "val isFool = false".c
    ))

pages += Page(
    name = "Functions",
    body = tPage("İşlevler",
                 """Functions give you the capability to define a set of commands or calculations that you wish to reuse or repeat - and refer to them by a name. A function is defined using the "def" key word followed by its name. The example that follows creates a function that returns an integer, the max value for the two integer arguments.""".p,
                 """def max(x: Int, y: Int): Int = {
    if (x > y) x
    else y
}
""".c,
                 """The name of the function,"max" in this case,  follows the "def", and then come the parameters with their associated types within parentheses. A type annotation is added after each parameter name and preceded by a colon. This function has two parameters of type Int. Then the return type is defined following a colon, and it's again Int in this case. Finally there is an equal sign and the function body enclosed in curly brackets.""".p,
                 """Once you have defined a function you can use it by calling it with the appropriate parameters.""".p,
                 "max(6,7)".c,
                 "Typically a function will return a value of some type. However, for some functions no return value is expected; they are only used to cause a side effect, printing or writing to a file for example. In this case the return will often be of type 'Unit' meaning no value or void. This type of function is called a procedure and here are some examples you may have already seen.".p,
                 """sil
ileri(100)
satıryaz("a procedure")
""".c,
"Functions that have no side-effects are called pure functions, and closely resemble mathematical functions. Pure functions provide the basis for a style of programming called functional programming.".p,
"Recursive Functions".h3, 
                 "Functions can make recursive calls to themselves. Recursive functions form an alternative way of controlling iterations. In the following function that computes the Greatest Common Divisor no variables are required for intermediate values.".p,

                 """def gcd(x: Long, y: Long): Long =
    if (y == 0) x else gcd(y, x % y)
""".c,
                 """Compare this to the earlier version written with a "while" loop.""".p,
                 "gcd(96,128)".c,
                 "Now here is another recursive function, it calls itself twice to create the branches of a rather pretty binary tree using the Turtle. The recursions stop when the distance is küçüktür or equal to 4.".p,
                 """def tree(distance: Double) {
    if (distance > 4) {
        setPenThickness(distance/7)
        setPenColor(Color(distance.toInt, Math.abs(255-distance*3).toInt, 125))
        ileri(distance)
        sağ(25)
        tree(distance*0.8-2)
        left(45)
        tree(distance-10)
        sağ(20)
        back(distance)
    }
}

sil()
invisible()
setAnimationDelay(60)
penUp()
back(200)
penDown()
tree(90)
""".c
    )
)

pages += Page(
    name = "OandC",
    body = tPage("Nesne ve Sınıflar",
                 "Everything is an Object".h2,
                 "Scala is an Object Oriented language. The underlying premis, like other Object Oriented languages, is that there are objects that contain state and this state is manipulated or accessed by means of Methods. Kojo is in fact a collection of objects that interpret the commands or procedures that you use in the Script Editor to move the Turtle, draw lines on the Turtle Canvas or print in the Output Pane.".p, 
                 "New types of object can be defined by describing their class. The class specification details what fields an object will contain, and the Methods that it will implement. Methods are functions that run within the context of an object, and have access to all the fields of the object. You create methods by defining them within a class with 'def', just as you have done for regular functions. Methods can be commands or pure functions.".p,  
                 "When you define a class you are also defining a new type of object. This new type and those already defined such as Int or Double are treated in a uniform way. The benefits of this uniformity, everything is an object, will soon become apparent.".p, 
                 "You can start by defining an object that represents a point with two fields. It is conventional to start class names with an capital letter while variable names and method names start with a small one".p,

                 """class Point {
var x=0
var y=0
}
""".c,

                 """This is the definition for the object. An instance of of the object can be created by using the "new" keyword.""".p,
                 "val p=new Point".c,
                 """The variables within an Object can be accessed by using "." """.p,
                 """p.x=3
p.y=4
""".c,
                 "You can retrieve the state in the same way.".p,
                 "satıryaz(p.x,p.y)".c,
                 "Setting the variables individually each time an instance of a new point is created is time consuming. Scala simplifies this by letting you specify the initial field values as parameters to the class definition. When you construct a new point you just provide the field values you want for that instance.".p,
                 "class Point( var x:Int,var y:Int)".c,

                 "And then create a point. Test it with satıryaz as before".p,
                 "val p=new Point(3,4)".c,
                 "Now suppose you would like to add two points together to create a new point. The equivalent of vector addition. Then you may add an appropriate method to do so. This is just a function like any other except that the function can access the class instance field values directly.".p,
                 """class Point(var x:Int,var y:Int){
    def vectorAdd(newpt:Point):Point={
          new Point(x+newpt.x,y+newpt.y)
          }
   }
""".c,
                 "Given this definition two points can be created and their vector addition made.".p,
                 """val p1=new Point(3,4)
val p2=new Point(7,2)
val p3=p1.vectorAdd(p2)
satıryaz(p3.x,p3.y)
""".c,

                 """So far this looks pretty much as a Java programmer would expect. However,it would be more natural to write "p1+p2". In Scala you can do so. Method names are identifiers and can be composed using almost all of the non-alphanumeric symbols as described before for variable names. A few combinations are reserved and you will get an error if you try to use them. So the class can be rewritten to use "+" and a method for "-" created too.""".p,
                 """class Point(var x:Int,var y:Int){
def +(newpt:Point):Point={
  new Point(x+newpt.x,y+newpt.y)
  }
def -(newpt:Point):Point={
  new Point(x-newpt.x,y-newpt.y)
  }
override def toString="Point("+x+","+y+")"
}
val p1=new Point(3,4)
val p2=new Point(7,2)
val p3=new Point(-2,2)
""".c,
                 """val p4=p1+p2-p3
satıryaz(p4.x,p4.y)
""".c,
                 "With this arrangement you can create a very natural looking vector calculus and a whole lot more readable than the traditional equivalent.".p,
                 "In Scala all classes are created with a default 'toString' method which produces a string representation of the object, by default its reference. You can override that method to give a more user friendly representation as has been done with Point above.".p,  

                 """In Scala there is a further simplification of the class creation syntax with the introduction of "case classes". Taking the Point class above it can be expressed as a case class.""".p,
                 """case class Point(x:Int,y:Int){
def +(newpt:Point)=Point(x+newpt.x,y+newpt.y)
def -(newpt:Point)=Point(x-newpt.x,y-newpt.y)
override def toString="Point("+x+","+y+")"
}
val p1=Point(3,4)
val p2=Point(7,2)
val p3=Point(-2,2)
""".c,
                 "p1+p2-p3".c,

                 """You see that the "new" is not required to create a new instance. The Scala compiler recognises that the new instance is required and creates it for you. Also note that in this case the curly brackets have been dropped in the "def". They are not required as the right hand side of the "def" is a simple expression and not a sequence of statements. This is a general property of "def" and not just limited to case classes. Lastly see that the return type has been dropped in the method definitions. Scala can infer this from the method body.""".p,
                "Everything is an object. As such, you may have wondered why the example above was not written more in Java style.".p,
                "p1.+(p2.-(p3)) // p1.+(p2).-(p3) ?".c,
                "This is one of the nice syntactic features of Scala that helps to give clarity and uniformity to your code. You may leave out the parentheses and dots as Scala can infer where they belong. It is this carefully thought out syntax that allows you to implement Domain Specific Languages (DSLs). So all objects, including numbers are just objects with methods. For example you can perform the + method on the number 1 with the extended syntax too. In Scala the mathematical operators like '+' , '-' , '*' and '/' are just methods too.".p,
                "(1).+(2)".c,
                "All the base types are in fact objects too that can be used and extended just like any other object.".p, 

                """Note the first set of parentheses around the "1" are required here to remove an ambiguity. "1." is a type Double and the result would be type Double rather than Int. Try making the change.""".p
    )
)

pages += Page(
    name = "PMS",
    body =tPage("Örüntü Eşleme ve Switch ve Case Komutları",
                "Örüntü Eşleme".h2,
                "You may already be familiar with the 'switch' with 'case' form used in many languages to allow multi-way branching based on a value. In Scala this concept is extended to provide full algebraic pattern matching using 'match'. However, the simple switch on value can also be represented easily with match.".p,

                """def  decode(n:Int){
  n match {
    case 1 => satıryaz("One")
    case 2 => satıryaz("Two")
    case 5 => satıryaz("Five")
    case _ => satıryaz("Error")
  }
}
""".c, 
                "decode(2)".c,
                "The '=>' symbol is used to separate the match pattern from the expression or block to be evaluated. The '_' symbol is used in Scala to mean wild-card or in this case match anything. The last case statement behaves like default in the classical switch. 'match', like most other functions returns a value so the above function could be written more concisely.".p,

                """def  decode(n:Int){
  satıryaz(n match {
    case 1 => "One"
    case 2 => "Two"
    case 5 => "Five"
    case _ => "Error"
    }
  )
}
""".c,

                "decode(3)".c,
                "Unlike the traditional Java 'switch' the above mapping can easily be reversed.".p,
                """def  encode(s:String){
  satıryaz(s match {
    case "One" => 1
    case "Two" => 2 
    case "Five" => 5
    case _ => 0
    }
  )
}
""".c, 
                """encode("Five")""".c,
                "Pattern matching provide a safe way to take actions based on the type of an object. This ability is extremely useful for working with case classes and will be illustrated in the next section.".p,
                """def whatIs(a:Any):String = {a match{
    case x:Int => "An Int"
    case x:String => "A String"
    case x:Double => "A Double"
    case _ => "Unknown Type"
    }
 }

satıryaz(whatIs("text"),whatIs(2),whatIs(2F)) // the F makes the 2 a type Float""".c

    )
)

pages += Page(
    name = "BTree",
    body =tPage("İleri Eşleme Yöntemleri - İkili Ağaç",
                "The next example, a binary tree, shows you how to construct a tree for storing and looking up integer values for string keys. The design has internal nodes for the upper tree and leaf nodes for the the key/value pairs. A function will be created to find the value in the tree associated with the given key. In this design the internal nodes contain pointers to two children and a key. While leaf nodes contain no pointers but just the key and its related value.".p,
"""
/*       [b]
         / \
       [a] c,3
       / \
     a,1 b,2 
*/     
""".c,                
"The first task is to create classes that describe the node objects. Immediately we see a problem with the internal node at the top of the tree. The pointer to the sub-tree may be either an internal node type or a leaf node type. We want to say that the pointer is one of these two types but not any other. It could not be an Int for example. In Scala you can do this by creating a class hierarchy. First a general tree node class is specified and then each of the possible nodes types is derived from it using the 'extends' keyword. The sub-class is said to inherit the properties of the parent class.".p,
                """class TreeN
case class InterN(key:String,left:TreeN,right:TreeN) extends TreeN
case class LeafN(key:String,value:Int) extends TreeN
""".c,
"The 'extend' means that the newly defined class inherits all the fields and methods from the parent class, also called super class, as well as defining it's own. It also becomes a sub-type of that class, meaning that a sub-type object can be saved in a super-type variable as the following example illustrates.".p,
"""val tn:TreeN=LeafN("abc",13)""".c,
"Now we can see how the internal node can be defined using the type 'TreeN' to mean either a 'InterN' or 'LeafN' type.".p,
"Next we need to create the search function for such a tree. This is where pattern matching provides a type safe solution for finding what type of node we are dealing with and therefore take the appropriate action.".p,                  
                "In the 'find' function that follows, see how pattern matching with the case classes defined above is used to determine the node type and bind names to the parameters, the lower case letters k,l,r and v. These are called pattern variables.".p,
"""
def find(t:TreeN,key:String):Int={
     t match {
         case InterN(k,l,r) => find((if(k>=key)l else r),key)
         case LeafN(k,v) => if(k==key) v else 0
    }
}
// create a binary tree
val t=InterN("b",InterN("a",LeafN("a",1),LeafN("b",2)),LeafN("c",3)) 
/*       [b]
         / \
       [a] c,3
       / \
     a,1 b,2 
*/     
""".c,
                "Note the use of the case class constructor to efficiently create a test binary tree. Now you can try the find.".p,
                """find(t,"a")""".c,
                """find(t,"c")""".c,
                "You may like to try wrapping this up into a Binary tree class, including member methods for adding, finding and deleting entries.".p, 
                "Patterns may also be constants, indicated by a starting uppercase letter or a literal. Constant patterns match values that are equal to them.".p,
                "Suppose, for some reason, you would like to hide the key 'c' during the find. A simple modification to the find function does this nicely, and also illustrates the use of a constant pattern.".p,    
                """def find(t:TreeN,key:String):Int={
     t match {
         case InterN(k,l,r) => find((if(k>=key)l else r),key)
         case LeafN("c",_) => 0
         case LeafN(k,v) => if(k==key) v else 0
    }
}
""".c,

                "Notice the use of '_' as a wild card to match any value and remember that the case statements are evaluated in order.".p,
"Inheritance and class heirarchy is one of the fundemental concepts that underpins Object Oriented programming. Pattern matching gives you a type safe way of dealing with objects created in that way.".p) 
)
pages += Page(
    name = "STI",
    body = tPage("Dingin Türleme ve Tür Çıkarımı",
                 "Scala is a statically typed language, all the variables and functions have types that are fully defined at compile time. Using a variable or a function in a way inappropriate for the type will give compiler type errors. This means that the compiler can find many unintended programming errors for you before execution. However, you will have noticed that in the examples there are few type definitions. This is because Scala can usually infer what type a variable must be from the way you have used it.".p, 

                 "For example, if you write 'val x=3' then the compiler infers that 'x' must be type Int as '3' is a integer literal. In a few situations the compiler will not be able to decide what you intended and will generate a type ambiguity error. In these cases you simply add the intended type annotation.".p,

                 "In general you must define function parameter types; however the compiler can usually infer the return type - so it can usually be omitted. The exception to this rule is if you define recursive functions, ones that call themselves. For these, you must define the return type.".p,

                 "Type inferencing dramatically reduces the amount of typing you must do and gives a great deal more clarity to the code. It is this type inferencing that gives Scala the feel of being dynamically typed.".p

    )
)

pages += Page(
    name = "FAO",
    body = tPage("İşlevler Birer Nesnedir",
                 "İşlevler de Birer Nesnedir".h2,
                 "In Scala everything is an object and so are functions. They may be passed as arguments, returned from other functions or stored in variables. This feature of Scala enables some very concise and elegant solutions to common programming problems as well as allowing extremely flexible program flow control structures.  The Scala Actors make heavy use of this capability for supporting concurrent programming. However, list manipulation provides a good starting point for an introduction. ".p,
                 "Lists are a very natural and common way people think about working with things. Scala provides a type of object called a List that allows you to represent lists of objects very easily. Lists keep things in a sequential order and provide a large number of methods or commands to enable you to create and manipulate lists. One way to create a list is to use the class constructor as we did for Point earlier. The constructor accepts any number of arguments and creates a List object containing those items. Here is an example that creates an integer list. The type will be designated by List[Int] - the [] encloses the element type information. Because each of the elements is an Int, Scala infers that the type of 'lst' is List[Int] - and you do not have to specify this explicitly.".p,  
                 "val lst=List(1,7,2,8,5,6,3,9,14,12,4,10)".c,
                 "Let's get started by using just three List methods - 'head', 'tail' and '::'. You will find more in the section 'Using Lists'.".p,
                 "'head' returns the first or leftmost item, '1' in our list above.".p,
                "satıryaz(lst.head)".c,
"'tail' returns the list with the first item, the '1', removed.".p,
                "satıryaz(lst.tail)".c,
"'::' returns a new list with an item added.".p,
                "satıryaz(23::lst)".c,
"Notice that the methods do not change or mutate the original list but return a new one. For this reason Lists are called immutable data structures.".p,
"With these three basic methods, you can create other ones to do almost anything you need with lists.  For example, here is how you find all the odd integers in a list:".p,

                 """def odd(inLst:List[Int]):List[Int]={
  if(inLst==Nil) Nil 
  else if(inLst.head%2==1) inLst.head::odd(inLst.tail) 
  else odd(inLst.tail)
}""".c,
"Notice how recursion and 'tail' are used to walk down the list to examine each item. 'Nil' represents the empty list and can also be written as List[Int]() in this case. Once the end of the list is reached the recursion is stopped and the list of odd items constructed and passed back to the caller.".p,
"Now try it".p,
                 "odd(lst)".c,  
                 "That's a simple solution, and to change this to return a list of the even integers is also simple.".p,
                 """def even(inLst:List[Int]):List[Int]={
  if(inLst==Nil) Nil 
  else if(inLst.head%2==0) inLst.head::even(inLst.tail) 
  else even(inLst.tail)
}""".c,
                 "even(lst)".c,     
                 "However, there is duplication here (do you see it?). A more general solution appears by passing a function that encapsulates the filtering condition as an argument.".p,
                 "First the filter condition function is defined.".p,
                 "def isodd(v:Int)= v%2==1".c,
                 "And then the modified filter function itself is defined, with an extra parameter to pass the filter condition function, just like any other object. Notice the form of the type declaration. The type is a function that takes one Int parameter and returns a Boolean. Only functions of this type can be passed as arguments. In the function body 'cond' is used just like any other function.".p,
                 """def filter(inLst:List[Int],cond:(Int)=>Boolean):List[Int]={
  if(inLst==Nil) Nil 
  else if(cond(inLst.head)) inLst.head::filter(inLst.tail,cond) 
  else filter(inLst.tail,cond)
}""".c,
                 "filter(lst,isodd)".c,
                 "Although the even case can be added in the same way, a more concise version can be created using Anonymous functions. The def and name are dropped, creating an anonymous function definition that can be used directly as an argument. Notice the use of the => to separate the function parameter list from the body of the function.".p,
                 "filter(lst,(v:Int)=> v%2==0)".c,
                 "This filter function now does just what was required.".p, 
                 "A Taste of Generic Programming".h3,
                 "Suppose you now want to create a filter for type Double. You could go through the code and simply replace all the type definitions of Int with Double. You would perhaps then call the new function filterD. For each new type you would go through the same exercise and end up with many versions of the same thing.".p,

                 "In Scala you can avoid this duplicated effort nicely by using a generic type in place of the actual ones. It is like using a variable to represent the type instead of an actual type. The actual types get filled in later by the compiler where you make a call to the function.".p,

                 "Here 'T' is used, though any letters will do, in place of Int in filter and the function name is annotated with [T] to indicate that this is a generic function. Then the example becomes".p,

                 """def filter[T](inLst:List[T],cond:(T)=>Boolean):List[T]={
  if(inLst==Nil) Nil 
  else if(cond(inLst.head)) inLst.head::filter(inLst.tail,cond) 
  else filter(inLst.tail,cond)
}""".c,
                 "filter(lst,(v:Int)=> v%2==0)".c,
                 "Then you can try using this generic version of filter with a list of Doubles to find all the elements büyüktür 5.".p,
                 """val lstd=List(1.5,7.4,2.3,8.1,5.6,6.2,3.5,9.2,14.6,12.91,4.23,10.04)
filter(lstd,(v:Double)=> v>5)""".c,
                 "Or with a list of strings to find those with a length büyüktür 3".p, 
                 """val lsts=List("It's","a","far","far","better","thing","I","do","now")
filter(lsts,(v:String)=> v.length>3)""".c,
                 "In the reference you will find that lists have a filter function so you could equally write.".p, 
                 "lsts.filter((v:String)=> v.length>3)".c,
                 "Generics will be explored more later but now, more about functions as objects.".p,
                 "More on 'Functions are objects'".h3,
                 "The ability to carry out comprehensions, namely applying a function(s) for all members of a collection, is very powerful and leads to some very compact coding.".p, 

                 "Many times you will want to pass quite simple functions as arguments and Scala has some nice shorthand that you will find useful. You have already seen that inferencing can help. The lst.filter example can be simplified as the compiler knows the argument must be a function with a String type. This allows you to drop the parentheses and type annotation.".p,   
                 "lsts.filter(v=>v.length>3)".c,   
                 "Since binary operators are frequently used, Scala provides you with an even nicer shorthand. The anonymous function '(x,y)=>x+y' can be replaced by '_+_' . Similarly 'v=>v.Method' can be replaced by '_.Method'. The _ acts as a place holder for the arguments and you are saved the chore of inventing repetitive boiler plate names. So once more lst.filter can be simplified to.".p,
                 "lsts.filter(_.length>3)".c, 
                 "Reading other peoples Scala code you will come across these short forms quite often. Sometimes you must use the longer forms.".p,
 
                 "Here are some more examples of list manipulations with functions as arguments taken from the Reference section.".p,
                 "flatMap".h4,
                 "lsts.flatMap(_.toList)".c,
                 "You see that flatMap takes a list and uses your given function to create a new list. In this case it 'flattens' your list of words into characters and concatenates these sublists to produce the result.".p,
                 "sort".h4,
                 "The words could be sorted in ascending order using the sort method.".p,
                 "lsts.sortWith(_<_)".c, 
                 "Or descending order".p,
                 "lsts.sortWith(_>_)".c,
                 "Or ignoring the case of the characters.".p,
                 "lsts.sortWith(_.toUpperCase<_.toUpperCase)".c,
                 "By passing the appropriate function you can create a whole family of sorts without having to re-code the sort itself. This is a really neat way to vary behavior without rewriting the whole method.".p,
                 "fold".h4,
                 "foldLeft and foldRight allow you to combine adjacent list elements using an arbitrary function that you pass in. The process either starts from the left of the list or the right, and you provide a starting value. The option to start left or right allows you to choose which order you want to pass through the list.".p,
                 "Fold takes two arguments but the Scala syntax allows these to be passed individually for clarity. See the section below on 'Creating your own flow control' for more details on the syntax.".p,
                 "Now starting with the list.".p,
                 """val lst=List(1,7,2,8,5,6,3,9,14,12,4,10)
lst.foldLeft(0)(_+_)""".c,
                 "With the passed '_+_' function the foldLeft function starts with 0 adds 1 to it. Next 7 is then added to this result and so on through the rest of the list.".p,
                 "removeDuplicates".h4,
                 "Suppose you wish to know the unique letters that are in a list of words such as:-".p,
                 """val lstw=List("Once","more","unto","the","breach")
lstw.flatMap(_.toList).map(_.toUpper).distinct.sortWith(_<_)""".c,
                 "'flatMap' flattens all the words into a list of letters. 'map' converts them all to uppercase. All the duplicates are then removed and the result sorted into ascending order.".p,
                 
"Creating your own flow control".h4,
"The ability to pass functions or blocks of code as arguments to functions gives you the ability to create your own flow control structures. For example, to draw a box it would be nice to be able repeat a set of Turtle commands. This could be done using 'for' ".p,
"""sil
for(i<- 1 to 4){ileri(100);sağ()}
""".c,
"However it would be much nicer to write something like,".p,
"repeat(4){ileri(115);sağ()}".c,
"'repeat' is not a built in Scala flow control structure like 'if' or 'while' but just a function provided for you by Kojo. You can define a function to do just that too. The first step is as follows.".p,
"""
def myrepeat(rc:Int,rb: =>Any){
  for(i<-1 to rc)rb      
}
""".c,
"Up until now all the functions we have defined have been 'pass by value'. This means that arguments to the functions are evaluated before the function is called. The => symbol in the above function definition specifies a 'call by name'. This means that the argument to the function is not evaluated until it is actually needed within the function body. In our repeat function, the for loop will cause the expression passed as an argument to be evaluated the repeat count number of times".p,  
"myrepeat(4,{ileri(130);sağ()})".c,
"This is close to what we wanted, but there are still two extra brackets and a comma that we can get rid of. Scala provides a way of defining functions where the arguments can be passed in one at a time. This is called a curried function and allows the definition to be changed as follows.".p,
"""def myrep(rc:Int)(rb: =>Any){
  for(i<-1 to rc)rb      
}
""".c,
"Now you can write the concise style you wanted:".p,
"myrep(4) {ileri(160);sağ()}".c,
"Earlier you used the fold function, which employs this same technique to provide a more readable syntax.".p,  
"You will find that this ability to treat functions as objects is very useful in all sorts of programming tasks - for example - passing callback functions in event driven IO, passing tasks to Actors in concurrent processing environments, or in scheduling work loads. This often results in far more concise code, as you just saw.".p, 
"Repeated Arguments in a Function".h4,
"Sometimes it is useful to define a function that can accept any number of arguments. 'satıryaz' is one that has already been used extensively in this kılavuzcuk. Here is a function to sum any number of integer arguments.".p,
"""def sumInt(n:Int*)=n.reduce(_ + _)
satıryaz(sumInt(1,2,3),sumInt(4,5,6,7,8))
""".c,
"The * after the parameter definition indicates that the function expects any number of this type of argument. The parameter n is defined as a sequence of the parameter type. This means that you can use list style methods to transform the sequence. In this case 'reduce' is used to sum the items.".p,
"Other single parameters can preceed the repeated parameter. The repeated one must come last".p                 
    )
)

pages += Page(
    name = "Tup",
    body = tPage("Sıralamalar (Tuple)",
                 "Sıralamalar (Tuple)".h3,
                 "It is often useful to return more than one value from a function or make up collections of things with more than one value for each item. You can of course always create a class to do this but the typing overhead of making the definition becomes onerous. Scala allows you to create what are in effect objects with anonymous fields - inline, using Tuples. A tuple is simply a set of values enclosed in paretheses. A tuple can contain a mixture of types.".p,
                 "(3,'c')".c,  
                 "The Tuple is an object, and so can be accessed using the dot notation; but since tuple fields have no names they are accessed using a name created with an underscore followed by the position index.".p, 
                 """satıryaz((3,'c')._1)
satıryaz((3,'c')._2)""".c,
                 "However, tuple deconstruction is more frequently used to access the fields of a Tuple. In an assignment involving a tuple, the Scala compiler associates or binds the variable names in the left of the assignemnt with the corresponding values in the tuple. For example,".p,
                 "val (i,c)=(3,'a')".c,
                 "You will see how this is used in the following program to create a letter frequency table from a words list. First create the list.".p,
"""val wl=List("kojo","is","a","great","way","to","learn","scala")""".c,
                 "The approach taken is to first flatten 'wl', convert to upper case and then sort the the list of characters.".p,
                 "Then a fold will be used to count the duplicate letters. You have already seen that a fold takes an input value and combines it successively with each list element to produce a new input value. In our case while the characters are the same a count needs to be incremented and when they differ the character and count need to be added to the freqency table.".p,

                 "The objective is to produce a list of tuples with two entries - a letter, and a count. This is our frequency table.".p, 
                 "First create a sorted list of characters.".p, 
    
                 "val ltrs=wl.flatMap(_.toList).map(_.toUpper).sortWith(_<_)".c,
                 "Now the fold. The initial condition is an empty output frequency table, a list of tuples. The fold will expect a function that takes a list of tuples combines with a Char, the next character in the list, and returns a list of tuples.".p,

                 """    
ltrs.foldLeft(List[(Char,Int)]()){
    case ((prevchr, cnt) :: tl, chr) if(prevchr==chr) => (prevchr,cnt+1) :: tl
    case (tbl, chr) => (chr, 1) :: tbl
}
""".c,

                 "To understand what is happening here, some new pieces of Scala syntax need to be understood.".p,

                 "First, a sequence of cases (i.e. alternatives) in curly braces can be used anywhere a function literal can be used. It acts as a function with input parameter(s) and an implied match at the begining of the block. You can consider this as a convenient shorthand for the standard anonymous function form which would have been:".p, 
                 "(a,b)=>(a,b) match {case ...}".p,    

                 "Second, you saw earlier that case is used to pattern match. Here that pattern matching is used to deconstruct our arguments and the list of tuples. '(prevchr,cnt)::tl' matches a list with a head element and tail and associates the names with those items. While 'chr' refers to the second fold arguement, the next list character.".p,

                 "Third, case syntactic pattern matches may not be a precise constraint, as is the case here so a pattern guard can be used. The guard starts with an if and can contain an arbitrary boolean expression. In this case the guard is used to restrict the case to just those calls where the previous character is the same as the current one.".p,

                 "So now we can read the expression as: For each character in the (sorted) input list, if there is already an entry for it in the head of the frequency table, replace the head with a new head with the count incremented. In the other case simply add a new entry to the frequency table with a count of one.".p,        

                 "Pretty neat and powerful, right?".p,

                 "Of course you could always use a more Java like approach too.".p,

                 """def iFreqCount(in:List[Char]):List[(Char,Int)]={
  var Tbl=List[(Char,Int)]()
  if(in.isEmpty)Tbl
  else{
    var prevChr=in.head
    var nxt=in.tail
    var Cnt=1
    while (!nxt.isEmpty){
      if(nxt.head==prevChr)Cnt+=1
      else {
        Tbl=(prevChr,Cnt)::Tbl
        Cnt=1
        prevChr=nxt.head
        }
      nxt=nxt.tail
      }
    (prevChr,Cnt)::Tbl
  }
}""".c,
                 "iFreqCount(ltrs)".c

)
)

pages += Page(
    name = "MF",
    body = tPage("Matematiksel İşlevler",
                 "Matematik"h3,
                 "Here are some useful Maths functions. Remember to import them before using.".p,
                 "import math._".c,
                 "Math Constants".h3,
                 "Two common constants are defined in the math class.".p,
                 table(
            row("E".c,"Value of e, 2.718282..., base of the natural logarithms."),
            row("Pi".c,"Value of pi, 3.14159265 ....")
        ),
                 "Trigonometric Methods".h3,
                 "All trigonometric method parameters are measured in radians, the normal mathematical system of angles, and not in degrees, the normal human angular measurement system. Use the toRadians or toDegrees methods to convert between these systems, or use the fact that there are 2*PI radians in 360 degrees. In addition to the methods below, the arc methods are also available.".p,
                 "Note - in the descriptions that follow, all the function parameters are labeled in order, and will be of the form functionName(P1, P2...).".p,
                 table(
            row("sin(Pi/6)".c,"sine of P1."),
            row("cos(Pi/6)".c,"cosine of P1."),
            row("tan(Pi/6)".c,"tangent of P1."),
            row("toRadians(45)".c,"P1 (angle in degrees) converted to radians."),
            row("toDegrees(Pi/2)".c,"P1 (angle in radians) converted to degrees.")
        ),
                 "Exponential Methods".h3,
                 "The two basic functions for logarithms and power are available. These both use the base e (Math.E) as is the usual case in mathematics.".p,
                 table(
            row("exp(Pi)".c,"e (2.71...) to the power P1."),
            row("pow(6,3)".c,"P1 raised to P2."),
            row("log(10)".c,"logarithm of P1 to base e.")
        ),
                 "Misc Methods".h3,
                 table(
            row("sqrt(225)".c,"square root of P1."),
            row("abs(-7)".c,"absolute value of P1 with same type as the parameter: int, long, float, or double."),
            row("max(8,3)".c,"maximum of P1 and P2 with same type as the parameter: int, long, float, or double."),
            row("min(8,3)".c,"minimum of P1 and P2 with same type as the parameter: int, long, float, or double.")
        ),
                 "Integer Related Methods".h3,
                 "The following methods translate floating point values to integer values, although these values may still be stored in a double.".p,
                 table(
            row("floor(3.12)".c,"closest integer-valued double which is equal to or küçüktür P1."),
            row("ceil(3.12)".c,"closest integer-valued double which is equal to or büyüktür P1."),
            row("rint(3.51)".c,"closest integer-valued double to P1."),
            row("round(3.48)".c,"long which is closest in value to the double P1."),
            row("round(2.6F)".c,"int which is closest in value to the float P1.")
        ),
                 "Random Numbers".h3,
                 table(
            row("random".c,"returns a Double pseudo-random number between 0.0 and 1.0")
        )
    )
)

pages += Page(
    name = "OPA",
    body = tPage("İşlem Önceliği ve Birleşmeliği",
                 "İşlem Önceliği".h3,
                 "Operators are any valid identifier, but their precedence within expressions is according to the table below, highest precedence first. The precedence of multi-character operators is defined by the first character. For example an operator +* would have the precedence given by the + sign.".p, 
                 "(all other special characters)".p,
                 """( * / % , + - : , = ! , < > , & , ^ , | ) highest precedence on left""".p, 
                 "(all letters)".p,
                 "(all assignment operators) eg = += -= *= /= etc".p,
                 "İşlem Birleşmeliği".h3,
                 "The associativity of an operator in Scala is determined by its last character. Any method that ends in a ':' character is invoked on its right operand, passing in the left operand. Methods that end in any other character are the other way around. They are invoked on their left operand, passing in the right operand. So a * b yields a.*(b), but a ::: b yields b.:::(a).".p
    )
)

pages += Page(
    name = "US",
    body = tPage("Using Strings",
                 "Strings".h2,
                 "String manipulation is a frequent task. Here are some useful functions and definitions. You will find that other sequences like lists have similar methods.".p,  
                 "Note once again that - in the descriptions that follow, all the function parameters are labeled in order, and so will be of the form P1.functionName(P2, P3...).".p,
                 "Escape characters for strings.".h3,
                 table(
            row("""\n""", "line feed","""\b""", "backspace","""\t""",      "tab","""\f""", "form feed"),
            row("""\r""", "carriage return","""\" """, "double quote", """\'""", "single quote","""\\""", "backslash")
        ),
        "Concatenation".h3,
        "Strings can be concatenated using the + symbol. The original strings are left unaffected. Strings are immutable.".p,
        """val a = "Big"
val b = "Bang"
val c = a + " " + b        
satıryaz( a,b,c)
""".c,
"Nearly all objects have a toString method to create a character representation.".p,
"""val x = (2).toString + " " + (3.1F).toString
satıryaz(x)
""".c, 
                 "Length".h3,
                 table(
            row(""""four".length""".c,"length of the string P1.")
        ),
                 "Comparison".h3, 
                 table(
            row(""""high".compareTo("higher")""".c,"compares to P1. returns <0 if P1 < P2, 0 if ==, >0 if P1>P2"),
            row(""""high".compareToIgnoreCase("High")""".c,"same as above, but upper and lower case are same"),
            row(""""book".equals("loot")""".c,"true if the two strings have equal values"),
            row(""""book".equalsIgnoreCase("BOOK")""".c,"same as above ignoring case"),
            row(""""book".startsWith("bo")""".c,"true if P1 starts with P2"),
            row(""""bookkeeper".startsWith("keep",4)""".c,"true if P2 occurs starting at index P3"),
            row(""""bookmark".endsWith("ark")""".c,"true if P1 ends with P2")
        ),
                 "Searching".h3,
                 """Note: All "indexOf" methods return -1 if the string/char is not found. Indexes are all zero base.""".p,
                 table(
            row(""""rerender".contains("ren")""".c,"True if P2 can be found in P1."),
            row(""""rerender".indexOf("nd")""".c,"index of the first occurrence of String P2 in P1."),
            row(""""rerender".indexOf("er",5)""".c,"index of String P2 at or after position P3 in P1."),
            row(""""rerender".indexOf('r')""".c,"index of the first occurrence of char P2 in P1."),
            row(""""rerender".indexOf('r',4)""".c,"index of char P2 at or after position i in P1."),
            row(""""rerender".lastIndexOf('e')""".c,"index of last occurrence of P2 in P1."),
            row(""""rerender".lastIndexOf('e',4)""".c,"index of last occurrence of P2 on or before position P3 in P1."),
            row(""""rerender".lastIndexOf("er")""".c,"index of last occurrence of P2 in P1."),
            row(""""rerender".lastIndexOf("er",5)""".c,"index of last occurrence of P2 on or before position P3 in P1.")
        ),
                 "Getting parts".h3,
                 table(
            row(""""polarbear".charAt(3)""".c,"char at position P2 in P1."),
            row(""""polarbear".substring(5)""".c,"substring from index P2 to the end of P1."),
            row(""""polarbear".substring(3,5)""".c,"substring from index P2 to BEFORE index P3 of P1.")
        ),
                 "Creating a new string from the original".h3,
                 table(
            row(""""Toni".toLowerCase""".c,"new String with all chars lowercase"),
            row(""""Toni".toUpperCase""".c,"new String with all chars uppercase"),
            row(""""  Toni   ".trim""".c,"new String with whitespace deleted from front and back"),
            row(""""similar".replace('i','e')""".c,"new String with all P2 characters replaced by character P3."),
            row(""""ToniHanson".replace("on","er")""".c,"new String with all P2 substrings replaced by P3.")
        ),
                 "Methods for Converting to String".h3,
                 table(
            row("String.valueOf(List(1,2,3))".c,"Converts P1 to String, where P1 is any value (primitive or object).")
        )
    )
)


pages += Page(
    name = "UL",
    body = tPage("Dizinlerin (List) Kullanılışı",
                 "Dizinler".h2,
                 "Lists provide a common sequence structure that is used for many functional style algorithms. The following functions enable Lists to be manipulated easily and effectively. The first example creates the List that is used for other examples.".p,
 
                 "Note:  _+_ is a shorthand for an anonymous function x,y=>x+y. Since binary operators are frequently used, this is a nice abbreviation. Similarly _.method is a shorthand for v => v.method. When there is more than one argument, the first underscore represents the first argument, the second underscore the second one, and so on.".p,
 
                 table(
            row("""val lst = "Tempus" :: "fugit" ::
  "irreparabile" :: Nil""".c,"""Creates a new List[String] with the three values "Tempus", "fugit", and "irreparabile" """), 
            row("List()".c,"or use Nil for the empty List"),    
            row("""List("Time", "flys", "irrecoverably")""".c,"""Creates a new List[String] with the three entries "Time", "flys", and "irrecoverably" """),
            row("""List("tick", "tock") ::: List("cuk", "oo")""".c,"Operator that concatenates two lists"),  
            row("lst(2)".c,"Returns the item at 0 based index 2 in lst"),
            row("lst.count(str => str.length == 5)".c,"Counts the string elements in lst that are of length 5"),
            row("""lst.exists(str => str == "irreparabile")""".c,"""Determines whether a string element exists in lst that has the value "irreparabile" """),  
            row("lst.drop(2)".c,"""Returns lst without the first 2 elements (returns List("irreparabile"))"""),
            row("lst.dropRight(2)".c,"""Returns lst without the rightmost 2 elements (returns List("Tempus"))"""),
            row("lst.filter(str => str.length == 5)".c,"Returns a list of all elements, in order, from lst that have length 5"),  
            row("lst.flatMap(_.toList)".c,"Applies the given function f to each element of this list, then concatenates the results"),
            row("""lst.forall(str =>str.endsWith("e"))""".c,"""true if all elements in lst end with the letter "e" else false"""),
            row("lst.foreach(str => print(str))".c,"Executes the print function for each of the strings in the lst"), 
            row("lst.foreach(print)".c,"Same as the previous, but more concise"),  
            row("lst.head".c,"Returns the first item in lst"), 
            row("lst.tail".c,"Returns a list that is lst without its first item"),
            row("lst.init".c,"Returns a list of all but the last element in lst"), 
            row("lst.isEmpty".c,"true if lst is empty"), 
            row("lst.last".c,"Returns the last item in lst"), 
            row("lst.length".c,"Returns the number of items in the lst"), 
            row("""lst.map(str => str + "?")""".c,"""Returns a list created by adding "?" to each string item in lst"""),
            row("""lst.mkString(", ")""".c,"Makes a string with the elements of the list"),  
            row("lst.filterNot(str => str.length == 4)".c,"Returns a list of all items in lst, in order, excepting any of length 4"),
            row("List(1,6,2,1,6,3).distinct".c,"Removes redundant elements from the list. Uses the method == to decide. "),  
            row("lst.reverse".c,"Returns a list containing all elements of the lst list in reverse order"), 
            row("lst.sortWith((str, t) => str.toLowerCase < t.toLowerCase)".c,"Returns a list containing all items of lst in alphabetical order in lowercase.")
        ),
                 "Some more useful list operations. First define a list of integers to use.".p,
                 "val lsti=List(1,7,2,8,5,6,3,9,14,12,4,10)".c,
                 table(
            row("lsti.foldLeft(0)(_+_)".c,"Combines elements of list using a binary function starting from left, initial one with a 0 in this case."),
            row("lsti.foldRight(0x20)(_|_)".c,"Combines elements of list using a binary function starting from Right, initial one with a hex 20 in this case.")
        )
    )
)
pages += Page(
    name = "UT",
    body = tPage("Kamplumbacığın Kullanılışı",
                 "The Turtle can be moved with a set of commands, many of which are listed below. Just try them out to see what they make the Turtle do. You can clear the Turtle Canvas at any time by right-clicking on it and then clicking Clear".p,
                 "The following example defines a procedure (or command) that draws a triangle. This will be used in other examples further on, so try it first. Notice that multiple commands can be used on one line if they are separated by a semi-colon. Also 'repeat' is a useful command for carrying out the same set of commands a number of times.".p,
  """def üçgen()= yinele(3){ ileri(100);sağ(120) }
sil()
üçgen()
sol()
üçgen()
""".c,
      table(
            row("ileri(100)".c, "Moves the turtle ileri( a 100 steps."),
            row("back(50)".c,"Moves the turtle back 50 steps."),
            row("setPosition(100, 100)".c, "Sends the turtle to the point (x, y) without drawing a line. The turtle's heading is not changed."),
            row("lineTo(20, 30)".c, "Turns the turtle towards (x, y) and moves the turtle to that point."),
            row("turn(30)".c, "Turns the turtle through a specified angle. Angles are positive for counter-clockwise turns."),
            row("sağ()".c, "Turns the turtle 90 degrees right (clockwise)."),
            row("sağ(60)".c, "Turns the turtle 60 degrees right (clockwise)."),
            row("left()".c, "Turns the turtle 90 degrees left (counter-clockwise)."),
            row("left(30)".c, "Turns the turtle angle degrees left (counter-clockwise)."), 
            row("towards(40, 60)".c, "Turns the turtle towards the point (x, y)."),
            row("setHeading(30)".c, "Sets the turtle's heading to angle (0 is towards the right side of the screen ('east'), 90 is up ('north'))."),
            row("heading".c, "Queries the turtle's heading (0 is towards the right side of the screen ('east'), 90 is up ('north"),
            row("home()".c, "Moves the turtle to its original location, and makes it point north."),
            row("position".c, "Queries the turtle's position."),

            row("""penUp()
  ileri(100)
  penDown()
  ileri(100)""".c, "penDown makes the turtle draw lines as it moves while with penUp the Turtle moves without drawing a line."), 
            row("""setPenColor(blue)
  triangle()""".c, "Specifies the color of the pen that the turtle draws with."),
            row("""sil()
  setFillColor(red)
  triangle()
  """.c, "Specifies the fill color of the figures drawn by the turtle."),

            row("""
  sil()
  setPenThickness(10)
  triangle()
  setPenThickness(1)
  """.c, "Specifies the width of the pen that the turtle draws with."),
            row("beamsOn()".c, "Shows crossbeams centered on the turtleto help with solving puzzles."),
            row("beamsOff()".c, "Hides the turtle crossbeams."),
            row("""sil()
  invisible()
  ileri(100)
  visible()
  turn(120)
  ileri(100)""".c, "invisible hides the turtle while visible makes it visible again."),
            row("""write("hello world")""".c, "Makes the turtle write the specified object as a string at its current location."),
            row("""
  sil()
  ileri(-100)
  setAnimationDelay(10)
  turn(120)
  ileri(100)""".c, "Sets the turtle's speed. The specified delay is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps. The default is 1000."),
            row("animationDelay".c, "Queries the turtle's delay setting."),

            row("newTurtle(50, 50)".c, "Makes a new turtle located at the point (x, y)."),
            row("turtle0".c, "Gives you a handle to the default turtle."),
            row("sil()".c, "Clears the screen, and brings the turtle to the center of the window."),
            row("""
  sil()
  triangle()
  zoom(0.5, 10, 10)
  """.c, "Zooms in by the given factor, and positions (cx, cy) at the center of the turtle canvas."),

            row("gridOn()".c, "Shows a grid on the canvas."),
            row("gridOff()".c, "Hides the grid."),
            row("axesOn()".c, "Shows the X and Y axes on the canvas."),
            row("axesOff()".c, "Hides the X and Y axes.")
        )
    )
)
    
pages += Page(
    name = "GAG",
    body = tPage("Çizim ve Oyun",
                 "Peter Lewerin has contributed 'Staging' to Kojo. Staging gives you some neat graphics and the potential to make games. These Staging features originated in a Java project called Processing and were ported to Kojo by Peter. This capability is worth a whole kılavuzcuk in itself. However to give you a taste of what is possible and a starting point for experimentation, here are a couple examples.".p,
                 "The section on Staging will be expanded in a later version of the kılavuzcuk. You can find a more complete list of Staging features and examples at:".p,
                 "Commands or Methods".link("lewerin.se/peter/kojo/staging.html"),
                 "Examples and Description".link("code.google.com/p/kojo/wiki/StagingModule"),
                 "The package contains commands and functions that allow you to draw sophisticated shapes and images, and a frame loop that allows you to animate the graphics.".p,
                 "In the first example you can see that a Staging environment is initialized, and the screen cleared. A ball is created, and then the ball bouncing movement is defined in the animation loop. Staging causes this loop to be executed every 20 to 32 milli-seconds giving a frame rate of around 30 to 50 frames per second depending on your computer performance. Using these principles you can create sophisticated animated graphics.".p,
  
                 """import Staging._
import Staging.{circle, clear, animate} // explicitly import names that clash
clear()
gridOn()
val ball = circle(-200, -100, 5)

var y = 0 ; var x = 0 // ball position
var dy = 10; var dx = 3 // ball speed
// animation is around 30 - 50 frames per second depending on the computer system 
animate {
    ball.setPosition(x,y)
    // update ball position, detect end of bounce area
    dx =  if(x < 0 || x > 200) -dx else dx
    x += dx
    dy =  if(y < 0 || y > 100) -dy else dy
    y += dy  
}
""".c,
                 "The next example is a simple game - a single player version of what must be one of the oldest games ever played on computers called 'Pong'. The idea is to hit the ball back with a paddle which you can move with the mouse. Each of your misses will be recorded. Have fun!".p,
                 """import Staging._
import Staging.{circle, clear, animate, setFillColor, wipe, mouseX, mouseY} // explicitly import names that clash
clear()
var x = 0 ; var y = 0  // ball position
var dy = 10 ; var dx = 3 // ball speed
var padx = 0.0 ; var pady = 0.0 // paddle position
val padl = 80 // paddle length
var miss = 0
// Court
line(-200,-100,-200,100)
line(-200,-100,200,-100)
line(-200,100,200,100)
// the ball
setFillColor(blue)
val ball=circle(-200, -100, 5) 
// animation is about 30 frames per second or 32 milliseconds per frame
animate {
    wipe()
    padx=mouseX; pady=mouseY
    line(padx, pady, padx, pady+padl) // the paddle
    // detect a hit
    dx =if((dx>0)&&(padx-x<15)&&(x-padx<15)&&(y>pady)&&(y<pady+padl)) -dx else dx
    ball.setPosition(x,y)
    // update ball position and check for walls
    dx =  if(x+dx < -200) -dx else dx
    if(x+dx>200){x= -200;miss+=1}  // a miss
    x += dx
    dy =  if((y+dy < -100 )|| (y + dy > 100)) -dy else dy
    y += dy
    // Keep Score
    text(miss.toString + " missed",0,0)
}
""".c,
                 "Now that you have the basics, try adding more balls, randomising their speed or changing the paddle size. Also see if you can fix the bug - sometimes the ball appears to pass through the paddle.".p,
                 "Keyboard Input".h3,
                 "It is very useful in games to use the keyboard to get player commands. Here is a simple example that allows you to draw using the left/right/up/down arrows to steer the Turtle.".p,

                 "Run the example by clicking on the following. The example associates a set of actions to keyboard events. Don't forget to click on the Turtle canvas to give it focus first.".p,
                 """sil(); visible(); setAnimationDelay(100)
onKeyPress{ k  => k match {
        case Kc.VK_LEFT => setHeading(180)
        case Kc.VK_RIGHT => setHeading(0)
        case Kc.VK_UP => setHeading(90)
        case Kc.VK_DOWN => setHeading(270)
        case _ => // Any other character just move ileri(
    }
    ileri(20)            
}
activateCanvas()
""".c,
                 "You can modify the actions and re-run to see what happens. Type Kc. to find out what other key events can be recognised.".p,

                 "Clock".h3,
                 "Here is a short example that illustrates how to use the staging graphics to display a clock.".p, 
                 "The Date library functions are used to find current time and date.".p,
   """import Staging._
import Staging.{circle, clear, animate, wipe, setPenColor} // explicitly import names that clash
clear
val Sc=100
val Pi2=2.0*math.Pi // 2*Pi radians in a circle
def clkFace={
  circle(0,0,Sc)
  for(i<-0 to 59){
    val ra=Pi2*i/60
    val x=Sc*sin(ra);val y=Sc*cos(ra)
    val tks=if(i%5==0) 0.9 else 0.95
    line(tks*x,tks*y,x,y)
    }
}
// the animate function runs around 30-50 times a second
animate{
  var d=new java.util.Date
  wipe
  setPenColor(red)
  clkFace
  setPenColor(blue)

  val s=Pi2*d.getSeconds/60
  line(0,0,0.9*Sc*sin(s),0.9*Sc*cos(s))

  val m=Pi2*d.getMinutes/60
  line(0,0,0.8*Sc*sin(m),0.8*Sc*cos(m))
  
  val h=Pi2*d.getHours/12+m/12
  line(0,0,0.6*Sc*sin(h),0.6*Sc*cos(h))
  
  text(d.toString, -Sc, -Sc-20)
}
""".c,

      "Conway'in Yaşam Oyunu".h3,
      """İngilizce adıyla "The Game of Life" o kadar meşhur ki, bilgisayarcılar arasında basitçe Life yani yaşam adıyla tanınır! Aslında o bir hücresel otomaton yani basit hücrelerden oluşan ve onların yerel etkileşimleri sayesinde kendi kendine devinen en basit program türlerinden biri. İngiltere doğumlu Amerika'da Princeton üniversitesinde matematik araştırmaları yapan John Horton Conway tarafından 1970 yılında icat edilmiş. Belki de keşfedilmiş demek lazım. Kimbilir. Sen nedersin?""".p,
      "Wikipedia ansiklopedisinden bakabilirsin".link("""tr.wikipedia.org/wiki/Conway%27in_Hayat_Oyunu"""),
      "Ana fikir çok basit. Basit bir avuç kurala göre hücreler canlanır ya da can verir. Her hücrenin sekiz komşusu var. Doğru mu? Canlı olanlara arkadaş diyelim. Bakın dört kural var:".p,
      table(row("Canlı bir hücrenin ikiden az arkadaşı varsa canı çıkar. Canı sıkılmış sanki."),
        row("Eğer iki ya da üç arkadaşı varsa hayatta kalır."),
        row("Eğer başına üçten fazla arkadaş toplanırsa canı çıkar! Bilmem neden. Sanki çok kalabalık olmuş gibi."),
        row("Cansız bir hücrenin tam üç tane arkadaşı varsa kendisi de canlanır. Allah'ın hakkı üç denir ya!")
      ),
      "Bu yazılımcık 'foldLeft' adlı metodu kullanarak çok önemli bir kavram olan üst derece işlevlere örnek oluyor. Ne demek üst derece işlev? Başka işlevleri girdi olarak kabul eden onları kullanarak akıl almaz derecede becerikli olan komutlar. En başlarda kullandığımız 'for' yerine 'foldLeft' kullanarak bütün dünyayı baştan çiziveriyoruz. 'foldLeft' soldan katla gibi bir anlama geliyor. Dünyayı temsil eden hücrelerin kümesindeki elemanları teker teker işleyiveriyor.".p,
      "Bu yaşam ya da hayat oyunu sıfır oyuncuyla oynanıyor! Çok sıkıcı mı dedin? Yok, çok ilginç aslında. Aslında sen çok önemlisin. Çünkü bu oyunun başlaması için en başta canlı hücrelere gerek var. Bunları sen belirleyebilirsin. Ama önce hazır bazı desenlerle başlamak daha kolay olur. 'başlangıç' adlı komudu bul. Onun ikinci girdisi 'desen'. Deseni seçmek için yapman gereken tek şey 'seç' adındaki değeri değiştirmek. Birinciyle başlıyoruz. Ama sıfırdan ona kadar hepsini deneyebilirsin. Sonra hatta kendin de yeni desenler ekleyebilirsin.".p,

"Bu simulasyonun hızını 'oran' değerini değiştirerek ayarlayabilirsin.".p,  
                 """import Staging._ // Staging birimindeki komutları kullanmak için çağıralım.
// Farklı birimlerin bazı benzer komutları oluyor ve aynı adı kullanıyorlar.
// Onun için hangisini kullanmak istiyoruz açık açık belirtmemiz gerek:
import Staging.{ animate, circle, clear, setFillColor, wipe }

çıktıyıSil; clear(); gridOn(); axesOn(); setFillColor(mavi)

// bu yazılımcıkta hızıKur gibi kaplumba komutları bir işe yaramıyor,
// çünkü çizimleri yapan kaplumba değil Staging biriminin komutları

// bu oyunun dünyası yani tahtası büyük bir kare. Kenarı KU uzunluğunda olsun
// Nasıl satranç tahtası 8x8, bu tahta 128x128 kare.
val KU = 128
// karenın kenarı kaplumbanın on adımına denk

// ilk önce, bütün kareler cansız olmalı
var dünya = (0 until KU * KU).foldLeft(Sayılar())((x, y) => x :+ 0)
satıryaz(s"Dünyamızda $KU'in karesi yani ${dünya.size} tane hane var.")
yaz(s"Ekranımız ${(canvasBounds.width / 10).toInt} kare eninde ")
satıryaz(s"ve ${(canvasBounds.height / 10).toInt} kare boyunda.")

val oran = 5 // canlandırmayı yavaşlatmak için bunu arttır.
// En hızlısı 1. 40'a eşitlersen saniyede bir nesil ilerliyor yaklaşık olarak.
// Nasıl mı? Canlandırma komutu (adı animate) bir saniyede 30 kere çalıştırılıyor. 

val gösterVeDur = yanlış // bunu doğru yaparsan deseni gösterip dururuz
val sonundaDur = doğru   // her desenin bir durağı var. Ondan sonra fazla bir şey değişmiyor.
// Ama, yine de çalışmaya devam etsin isterse, bunu yanlışa çevir.

// deseni seçelim:
val seç = 1
// block1 ve block2 bir kaç füze yolluyor ve sonra 1000. nesil civarı gibi duruyor.
val (desen, adı, durak) = seç match {
    case 0 => (üçlüler, "üçlüler", 20)
    case 1 => (kayGit, "kayGit", 500) /* makineli tüfek gibi */
    case 2 => (esaslı, "esaslı", 1111) /* Yaklaşık 1000 nesil canlı sonra peryodik */
    case 3 => (dokuzcanlı, "dokuzcanlı", 130) /* 131 nesil sonra can kalmıyor */
    case 4 => (blok1, "block1", 1200) //
    case 5 => (blok2, "block2", 1200) //
    case 6 => (küçücük, "tiny", 700) // küçücük
    case 7 => (ü2a, "ü2a", 60) // üçlülere ek
    case 8 => (ü2b, "ü2b", 60) // benzeri
    case 9 => (dörtlü, "dörtlü", 30) // üçlü üretiyor
    case _ => (tohum, "tohum", 2200) // ne muhteşem bir meşe palamudu!
}

dünya = başlangıç(dünya, desen)

yaz(s"$seç. desende ${desen.size} tane canlı kare var. Adı $adı.\nNesilleri sayalım: ")

var zaman = 0
val z0 = epochTime // şimdiki zamanı (geçmişte bir ana göre) anımsayalım
animate {
    val nesil = zaman / oran + 1
    if (zaman % oran == 0) {
        wipe() // sil
        çiz(dünya)
        dünya = (0 until KU * KU).foldLeft(Sayılar())((x, y) => x :+ yeniNesil(dünya, y))
        yaz(s"$nesil ")
        if (gösterVeDur) stopAnimation
    }
    zaman += 1
    if (sonundaDur && nesil == durak) {
        val z1 = epochTime - z0
        satıryaz(s"\n${round(z1, 2)} saniye geçti. Durduk.")
        stopAnimation()
    }
}

// deseni kuralım
def başlangıç(v: Sayılar, desen: Dizin[(Sayı, Sayı)]) = desen.foldLeft(v)((x, y) => x.updated((y._1 + KU / 2) * KU + y._2 + KU / 2, 1))

// yeni nesli bulalım
def yeniNesil(v: Sayılar, ix: Sayı) = {
    val kural = Vector(0, 0, 0, 1, 1, 0, 0, 0, 0, 0) // oyunun kuralları
    val x = ix / KU; val y = ix % KU
    val t = (0 until 3).foldLeft(0)((st, i) => {
        st + (0 until 3).foldLeft(0)((s, j) => {
            val xt = x + i - 1; val yt = y + j - 1
            s + (if ((xt < 0) || (xt >= KU) || (yt < 0) || (yt >= KU)) 0 else v(xt * KU + yt))
        })
    })
    if (v(ix) == 1) kural(t) else { if (t == 3) 1 else 0 }
}
// canlı kareleri çizelim. Can mavi çember içi kırmızı daire. Yarıçapı 5
val yarıçap = 5
def çiz(v: Sayılar) = for (i <- 0 until KU * KU)
    if (v(i) == 1) circle(
        (i / KU) * 2 * yarıçap - KU * yarıçap,
        (i % KU) * 2 * yarıçap - KU * yarıçap, yarıçap)

// Meşhur olmuş desenlerden birkaçı
def esaslı = Dizin((0, 1), (1, 0), (1, 1), (1, 2), (2, 2)) // orijinal adı: fpent
// İki küçücük grup var ve kolay kolay ölmüyor
def dokuzcanlı = Dizin((0, 1), (1, 0), (1, 1), (5, 0), (6, 0), (7, 0), (6, 2)) // diehard
def tohum = Dizin((0, 0), (1, 0), (1, 2), (3, 1), (4, 0), (5, 0), (6, 0))
// glider adlı meşhur üretken desen
def kayGit = Dizin((-18, 3), (-18, 4), (-17, 3), (-17, 4), (-8, 2), (-8, 3), (-8, 4), (-7, 1), (-7, 5),
    (-6, 0), (-6, 6), (-5, 0), (-5, 6), (-4, 3), (-3, 1), (-3, 5), (-2, 2), (-2, 3), (-2, 4),
    (-1, 3), (2, 4), (2, 5), (2, 6), (3, 4), (3, 5), (3, 6), (4, 3), (4, 7),
    (6, 2), (6, 3), (6, 7), (6, 8), (16, 5), (16, 6), (17, 5), (17, 6))
def blok1 = Dizin((0, 0), (2, 0), (2, 1), (4, 2), (4, 3), (4, 4), (6, 3), (6, 4), (6, 5), (7, 4))
def blok2 = Dizin((0, 0), (0, 3), (0, 4), (1, 1), (1, 4), (2, 0), (2, 1), (2, 4), (3, 2), (4, 0),
    (4, 1), (4, 2), (4, 4))
def küçücük = Dizin((-18, 0), (-17, 0), (-16, 0), (-15, 0), (-14, 0), (-13, 0), (-12, 0), (-11, 0), (-9, 0), (-8, 0),
    (-7, 0), (-6, 0), (-5, 0), (-1, 0), (0, 0), (1, 0), (8, 0), (9, 0), (10, 0),
    (11, 0), (12, 0), (13, 0), (14, 0), (16, 0), (17, 0), (18, 0), (19, 0), (20, 0))
def üçlüler = Dizin((0, 2), (0, 3), (0, 4), (0, -2), (0, -3), (0, -4),
    (-2, 0), (-3, 0), (-4, 0), (2, 0), (3, 0), (4, 0))
// üçlülerden dikey olanları bağlayalım
def ü2a = Dizin((0, 0), (0, 1), (0, -1)) ++ üçlüler
// öbür türlü, yani yatay olanları bağlayalım
def ü2b = Dizin((0, 0), (1, 0), (-1, 0)) ++ üçlüler
def dörtlü = Dizin((0, 0), (1, 0), (-1, 0), (0, 2)) // dokuzcanlı'nın altkümesi

// sepet sepet yumurta
// sakın beni unutma
// şimdilik bu kadar
// yaşamın tadını çıkar
""".c,
    
"Tangle".h3,
"Here is a game that illustrates how you can use the Scala collections and mouse drag-and-drop to create a fun game to play.".p,
"The game was based on a game called Planarity. The idea is to use the mouse to re-arrange the circles so that none of the joining lines cross one and other. Press the left mouse button on a circle to drag it. A new game is started by clicking on the red square.".p,
"You can increase the difficulty of the game by changing the value ES in the program. Larger values make it more difficult.".p,
"The inspiration for Tangle is Planarity".link("http://www.planarity.net/"),
                 
"""import Staging._
import Staging.{circle, clear, animate} // explicitly import names that clash
import math.pow,math.random
// Tangle based on Planarity
clear()
// ES sets difficulty level
val ES=4;val AS=ES*ES
val Ra=10
// Edge is a line between two nodes
case class EdgeP(n1:NodeP,n2:NodeP){
var e=line(n1.x,n1.y,n2.x,n2.y)
}
// edges is all the edges, initially empty
var edges=Vector[EdgeP]()
// Node is a circle which is dragable. Redraws edges when dragged
case class NodeP(var x:Double,var y:Double){
  val n=circle(x,y,Ra)
  n.setFillColor(blue)
  def goTo(gx:Double,gy:Double){
   x=gx ; y=gy
   n.setPosition(gx,gy)   
  }
  n.onMouseDrag{(mx, my) => {n.setPosition(mx, my);x=mx;y=my;drawEdges(edges)}}
}
// Create and link all nodes topologically in a square 
val p=(0 until AS).foldLeft(Vector[NodeP]())((v,i)=>{v :+ NodeP(0,0)})

// Create all edges, link to adjacent nodes   
edges=(0 until AS).foldLeft(Vector[EdgeP]())(
    (ev,i)=>{
        val x=i/ES; val y=i%ES 
        val te=if(y<ES-1) {ev :+ EdgeP(p(i),p(i+1))} else ev
        if(x<ES-1) {te :+ EdgeP(p(i),p(i+ES))} else te
    })
// draw all edges
putRand(p)
// Button for new game
val b=square(-ES*35,-ES*35, 20)
b.setFillColor(red)
b.onMouseClick { (x, y) =>putRand(p)}
// randomise node positions
def putRand(p:Vector[NodeP]){
   p.foreach(tn=>tn.goTo(ES*Ra*6*(random - 0.5),ES*Ra*6*(random - 0.5)))    
   drawEdges(edges) 
   }    
//draw edges between nodes and start line from circumference of circle
def drawEdges(ev:Vector[EdgeP]){ 
   ev.foreach(te=>{
     val x1=te.n1.x ; val y1=te.n1.y
     val x2=te.n2.x ; val y2=te.n2.y
     val len=sqrt(pow(x2-x1,2) + pow(y2-y1,2))
     val xr=Ra/len*(x2-x1) ; val yr=Ra/len*(y2-y1) 
     te.e.erase;
     te.e=line(x1+xr,y1+yr,x2-xr,y2-yr)
     }) 
   }
""".c
    )
)
    
pages += Page(
    name = "LM",
    body = tPage("Daha Çok Öğrenelim",

                 "Next Steps".h2,
                 "This kılavuzcuk has covered part of what is a very deep language. By now, you are familiar with the essential Scala language features. Already you can write quite sophisticated programs in Scala and have fun with the graphical environment offered by Kojo.".p,
                 "If you are already a Java programmer you can no doubt already see how you can use Scala with all the libraries from your existing Java environment too. Scala and Java integrate seamlessly.".p,

                 "To learn more a good book is invaluable. Programming in Scala by Martin Odersky, Lex Spoon and Bill Venners is one excellent place to continue.".p,
"It can be found by clicking here.".link("www.artima.com/shop/programming_in_scala"),
 "The Scala community site has a lot of good material and references to other learning materials.".p,
"Click here to go to Scala-lang.org.".link("www.scala-lang.org"), 
"For the professional programmer, you can also download Scala, Akka, and the Eclipse IDE.".p,
"They can be found here.".link("typesafe.com"),
"The Netbeans IDE also has good Scala support (and Kojo itself has been written, in Scala, using Netbeans).".p,
"More information here".link("wiki.netbeans.org/Scala"),
"A small subset of the Scala library functions have been used in this kılavuzcuk".p,
"Click here to see a complete list of all the Scala library available.".link("www.scala-lang.org/api/current/index.html"),
"We wish you lots of fun using Scala!".p
                                  
    )
)

val story = Story(pages.toSeq: _*)
clearOutput()
stClear()
stAddLinkHandler("example", story) {idx: Int =>
    stSetScript(codeExamples(idx))
    stClickRunInterpreterButton()
    clearOutput
}
stPlayStory(story)
