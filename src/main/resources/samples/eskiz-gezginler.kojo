// Sketch example: wanderers that leave trails.
// Each wanderer takes a small random step every frame and leaves a colored trail behind it.
// The trails fade away: every frame, a very transparent black veil is drawn over the canvas.
//
// Why a sketch? If we drew a new picture (say Picture.circle) for every step, Kojo would
// hit the "Too many pics" limit (60000) within a few minutes and stop drawing (#87).
// A sketch is a single picture: what it draws is just pixels, so there is no such limit.
cleari()
originBottomLeft() // the sketch picture starts at the bottom-left corner of the canvas

val w = canvasBounds.width
val h = canvasBounds.height
val wandererCount = 12
val stepSize = 7.0

class Wanderer(hue: Double) {
    var x = w / 2
    var y = h / 2

    def step(cd: CanvasDraw) {
        val oldX = x
        val oldY = y
        x = math.min(w, math.max(0.0, x + randomDouble(-stepSize, stepSize))) // stops at the edge
        y = math.min(h, math.max(0.0, y + randomDouble(-stepSize, stepSize)))
        cd.stroke(cm.hsla(hue, 1, 0.5, 0.9))
        cd.strokeWeight(2)
        cd.line(oldX, oldY, x, y)
    }
}

class Wanderers {
    val wanderers = (1 to wandererCount).map(i => new Wanderer(i * 360.0 / wandererCount))

    def setup(cd: CanvasDraw) {
        cd.background(black)
    }

    def drawLoop(cd: CanvasDraw) {
        cd.fill(0, 0, 0, 20) // the fading veil
        cd.noStroke()
        cd.rect(0, 0, w, h)
        wanderers.foreach(_.step(cd))
    }
}

draw(Picture.fromSketch(new Wanderers))
