package maps.gml;

import maps.gml.formats.RobocupFormat;
import maps.gml.formats.OrdnanceSurveyFormat;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
   A GML map viewer.
*/
public class ViewGMLMap {
    private static final int VIEWER_SIZE = 500;

    private static final Logger LOG = LogManager.getLogger(ViewGMLMap.class);

    /**
       Start the viewer.
       @param args Command-line arguments: mapname.
    */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ViewGMLMap <mapname>");
            return;
        }
        try {
            GMLMap map = readGMLMap(args[0]);
            GMLMapViewer gmlViewer = new GMLMapViewer(map);
            JFrame frame = new JFrame("GML Map");
            gmlViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            frame.setContentPane(gmlViewer);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        }
        // CHECKSTYLE:OFF:IllegalCatch
        catch (Exception e) {
            e.printStackTrace();
        }
        // CHECKSTYLE:ON:IllegalCatch
    }

    private static GMLMap readGMLMap(String file) throws GMLException, IOException, DocumentException {
        LOG.info("Reading GML file: " + file);
        SAXReader reader = new SAXReader();
        File f = new File(file);
        Document doc = reader.read(f);
        LOG.debug("Guessing format");
        MapFormat format = guessFormat(doc);
        if (format == null) {
            throw new GMLException("Unrecognised format");
        }
        LOG.debug("Parsing " + format.toString() + " format");
        return format.read(doc);
    }

    private static MapFormat guessFormat(Document doc) {
        List<MapFormat> all = new ArrayList<MapFormat>();
        all.add(new RobocupFormat());
        all.add(new OrdnanceSurveyFormat());
        for (MapFormat next : all) {
            if (next.looksValid(doc)) {
                return next;
            }
        }
        return null;
    }
}