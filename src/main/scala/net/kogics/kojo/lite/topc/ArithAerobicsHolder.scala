package net.kogics.kojo.lite.topc

import javax.swing.JPanel
import net.kogics.kojo.util.Utils
import bibliothek.gui.dock.common.event.CFocusListener
import bibliothek.gui.dock.common.intern.CDockable
import net.kogics.kojo.lite.ArithAerobicsPane

class ArithAerobicsHolder(ae: ArithAerobicsPane)
  extends BaseHolder("AE", Utils.loadString("CTL_ArithAerobicsTopComponent"), ae) {

  this.addFocusListener(new CFocusListener {
    override def focusGained(dockable: CDockable): Unit = {
      ae.activated()
    }

    override def focusLost(dockable: CDockable): Unit = {
    }
  })

}