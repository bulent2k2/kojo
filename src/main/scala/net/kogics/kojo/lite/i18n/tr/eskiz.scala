/*
 * Copyright (C) 2026
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
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
package net.kogics.kojo.lite.i18n.tr

/**
 * Processing tarzı eskiz: `kurulum` bir kez, `çizimDöngüsü` her karede çalışır; ikisi de aynı
 * tuvale (TuvalÇizim) çizer. İngilizcesi, `Picture.fromSketch`in beklediği `setup`/`drawLoop`
 * yapısal türüdür (../../Builtins.scala SketchRunner).
 *
 *   durum sınıf İzler() extends Eskiz {
 *     tanım kurulum(tuval: TuvalÇizim) = tuval.artalan(siyah)
 *     tanım çizimDöngüsü(tuval: TuvalÇizim) = { tuval.boya(kırmızı); tuval.elips(rastgele(100), rastgele(100), 5, 5) }
 *   }
 *   çiz(Resim.eskizden(İzler()))
 *
 * Neden eskiz: tuval tek bir resim, çizdikleri piksel. Her çizim için yeni bir Resim nesnesi
 * doğmadığından PicCache büyümez ve "Too many pics" sınırına (60000) takılmaz.
 */
trait Eskiz {
  def kurulum(tuval: net.kogics.kojo.lite.CanvasDraw): Birim
  def çizimDöngüsü(tuval: net.kogics.kojo.lite.CanvasDraw): Birim
}

/** Türkçe `Eskiz`i İngilizce yapısal türe (setup/drawLoop) uyarlar. Ad bilerek yalın: yansıma ile çağrılıyor. */
class EskizUyarlayıcı(eskiz: Eskiz) {
  def setup(cd: net.kogics.kojo.lite.CanvasDraw): Unit = eskiz.kurulum(cd)
  def drawLoop(cd: net.kogics.kojo.lite.CanvasDraw): Unit = eskiz.çizimDöngüsü(cd)
}
