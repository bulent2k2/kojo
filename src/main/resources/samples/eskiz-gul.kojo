// Sketch example: a rose curve (rhodonea). r = cos(k * angle), k = n / d.
// The curve is drawn frame by frame, growing a little each frame; the color follows the angle.
// When the curve closes, we wait a few seconds and then pick a new (n, d).
//
// This also shows saving/restoring the transformation: we translate to the center to draw
// and leave the rest of the canvas untouched.
// A sketch is a single picture, so thousands of small lines never hit "Too many pics" (#87).
cleari()
originBottomLeft() // the sketch picture starts at the bottom-left corner of the canvas

val w = canvasBounds.width
val h = canvasBounds.height
val radius = 0.45 * math.min(w, h)
val choices = Seq((7, 4), (5, 3), (9, 4), (5, 2), (8, 5), (3, 1))
val stepsPerFrame = 40 // how many small lines per frame
val angleStep = 0.01

class Rose {
    var (n, d) = choices(0)
    var angle = 0.0
    var pause = 0

    def r(a: Double) = radius * math.cos(n.toDouble / d * a)

    def pickNew(cd: CanvasDraw) {
        val (n2, d2) = choices(random(choices.length))
        n = n2; d = d2
        angle = 0.0
        cd.background(black)
    }

    def setup(cd: CanvasDraw) {
        cd.background(black)
    }

    def drawLoop(cd: CanvasDraw) {
        if (angle > 2 * math.Pi * d) { // the curve has closed
            pause += 1
            if (pause > 180) { pause = 0; pickNew(cd) }
        }
        else {
            cd.pushMatrix()
            cd.translate(w / 2, h / 2)
            cd.strokeWeight(2)
            repeat(stepsPerFrame) {
                val a2 = angle + angleStep
                val (x1, y1) = (r(angle) * math.cos(angle), r(angle) * math.sin(angle))
                val (x2, y2) = (r(a2) * math.cos(a2), r(a2) * math.sin(a2))
                cd.stroke(cm.hsla((angle * 180 / math.Pi) % 360, 1, 0.55, 1))
                cd.line(x1, y1, x2, y2)
                angle = a2
            }
            cd.popMatrix()
        }
    }
}

draw(Picture.fromSketch(new Rose))
