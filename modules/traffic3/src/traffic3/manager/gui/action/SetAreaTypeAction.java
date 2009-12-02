package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.area.TrafficArea;
import static org.util.Handy.inputString;
import org.util.CannotStopEDTException;

import traffic3.objects.TrafficObject;


/**
 * Put agents.
 */
public class SetAreaTypeAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SetAreaTypeAction() {
        super("set area type");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">set ara type");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    try {
                        String areaType = inputString(wmgui, "Input area type.");
                        TrafficObject[] copyOfTargetList = wmgui.createCopyOfTargetList();
                        for (TrafficObject o : copyOfTargetList) {
                            if (o instanceof TrafficArea) {
                                TrafficArea a = (TrafficArea)o;
                                a.setType(areaType);
                            }
                        }
                        wmgui.createImageInOtherThread();
                    }
                    catch (CannotStopEDTException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "put agent2").start();
    }
}