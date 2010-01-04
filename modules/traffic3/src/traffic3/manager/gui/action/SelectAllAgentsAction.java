package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import static traffic3.log.Logger.log;

import traffic3.objects.TrafficAgent;;

/**
 * Select all in the world manager action.
 */
public class SelectAllAgentsAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SelectAllAgentsAction() {
        super("Select All Agents");

    }

    /**
     * Select all in the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        log(">select all Agents");
        for (TrafficAgent agent : getWorldManagerGUI().getWorldManager().getAgentList()) {
            getWorldManagerGUI().getTargetList().put(agent.getID(), agent);
        }
        getWorldManagerGUI().createImageInOtherThread();
    }
}