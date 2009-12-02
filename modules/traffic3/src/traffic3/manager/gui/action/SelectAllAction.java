package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import static traffic3.log.Logger.log;

import traffic3.objects.area.TrafficArea;

/**
 * Select all in the world manager action.
 */
public class SelectAllAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SelectAllAction() {
        super("Select All");
        putValue("MnemonicKey", KeyEvent.VK_M);
        putValue("ShorDescription", "Select all");
        putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));

    }

    /**
     * Select all in the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        log(">select all");
        for (TrafficArea area : getWorldManagerGUI().getWorldManager().getAreaList()) {
          getWorldManagerGUI().getTargetList().put(area.getID(), area);
        }
        getWorldManagerGUI().createImageInOtherThread();
    }
}