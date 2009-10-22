package osm2gml;

import osm2gml.osm.OSMMap;
import osm2gml.osm.OSMMapViewer;
import osm2gml.osm.OSMException;
import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLMapViewer;
import osm2gml.gml.GMLException;

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

public class ViewGMLMap {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ViewGMLMap <mapname>");
            return;
        }
        try {
            GMLMap map = readGMLMap(args[0]);
            GMLMapViewer gmlViewer = new GMLMapViewer(map);
            JFrame frame = new JFrame("GML Map");
            gmlViewer.setPreferredSize(new Dimension(500, 500));
            frame.setContentPane(gmlViewer);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static GMLMap readGMLMap(String file) throws GMLException, IOException, DocumentException {
        SAXReader reader = new SAXReader();
        File f = new File(file);
        Document doc = reader.read(f);
        return new GMLMap(doc);
    }
}