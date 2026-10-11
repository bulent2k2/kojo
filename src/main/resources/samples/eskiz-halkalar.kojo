// Sketch example: ripples on the water.
// Drops fall at random places; each drop leaves rings that grow and fade away.
// Every frame the canvas is repainted (background) and all the live rings are drawn again:
// no trails are left, but even hundreds of ellipses are still a single picture, a single sketch.
//
// Why a sketch and not Picture.circle? If every ring were a new picture in every frame,
// Kojo would soon hit the "Too many pics" limit (60000) (#87).
cleari()
originBottomLeft() // the sketch picture starts at the bottom-left corner of the canvas

val w = canvasBounds.width
val h = canvasBounds.height
val maxRadius = 0.18 * math.min(w, h)

class Ripple(x: Double, y: Double, hue: Double) {
    var age = 0.0
    def dead = age >= 1.0
    def step() { age += 0.012 }
    def show(cd: CanvasDraw) {
        val opacity = 1.0 - age // fades as it ages
        val r = maxRadius * age
        cd.noFill()
        cd.strokeWeight(3 * opacity + 0.5)
        cd.stroke(cm.hsla(hue, 0.8, 0.6, opacity))
        cd.ellipse(x, y, 2 * r, 2 * r)
        cd.ellipse(x, y, 1.3 * r, 1.3 * r) // inner ring
    }
}

class Water {
    val ripples = ArrayBuffer.empty[Ripple]

    def setup(cd: CanvasDraw) {
        cd.background(0, 20, 50)
    }

    def drawLoop(cd: CanvasDraw) {
        if (randomDouble(1) < 0.06) {
            ripples.append(new Ripple(randomDouble(w), randomDouble(h), randomDouble(160, 220)))
        }
        ripples.filterInPlace(r => !r.dead)
        cd.background(0, 20, 50)
        ripples.foreach { r =>
            r.step()
            r.show(cd)
        }
    }
}

draw(Picture.fromSketch(new Water))
