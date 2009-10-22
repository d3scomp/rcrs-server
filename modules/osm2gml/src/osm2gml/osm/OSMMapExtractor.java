package osm2gml.osm;

import javax.swing.JFrame;

import java.awt.Point;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.io.Writer;
import java.io.File;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

public class OSMMapExtractor extends MouseAdapter {
    private OSMMap map;
    private OSMMapViewer viewer;
    private Writer out;
    private double pressLat;
    private double pressLon;

    public static void main(String[] args) {
        try {
            OSMMap map = new OSMMap(new File(args[0]));
            Writer out = new FileWriter(new File(args[1]));

            JFrame frame = new JFrame();
            OSMMapViewer viewer = new OSMMapViewer(map);
            viewer.addMouseListener(new OSMMapExtractor(map, viewer, out));
            frame.setContentPane(viewer);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OSMMapExtractor(OSMMap map, OSMMapViewer viewer, Writer out) {
        this.map = map;
        this.viewer = viewer;
        this.out = out;
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point p = e.getPoint();
            Insets insets = viewer.getInsets();
            p.translate(-insets.left, -insets.top);
            pressLat = viewer.getLatitude(p.y);
            pressLon = viewer.getLongitude(p.x);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point p = e.getPoint();
            Insets insets = viewer.getInsets();
            p.translate(-insets.left, -insets.top);
            double releaseLat = viewer.getLatitude(p.y);
            double releaseLon = viewer.getLongitude(p.x);
            try {
                OSMMap newMap = new OSMMap(map,
                                           Math.min(pressLat, releaseLat),
                                           Math.min(pressLon, releaseLon),
                                           Math.max(pressLat, releaseLat),
                                           Math.max(pressLon, releaseLon));
                Document d = newMap.toXML();
                XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
                writer.write(d);
                writer.flush();
                writer.close();
                System.out.println("Wrote map");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}