package maps.osm;

import javax.swing.JFrame;
import javax.swing.JComponent;

import java.awt.Point;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.io.Writer;
import java.io.File;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

/**
   This class extracts a portion of an OSMMap.
*/
public class OSMMapExtractor extends MouseAdapter {
    private static final int VIEWER_SIZE = 500;
    private static final Color DRAG_COLOUR = new Color(128, 128, 128, 64);

    private static JComponent glass;
    private static Point press;
    private static Point drag;
    private static Point release;

    private OSMMap map;
    private OSMMapViewer viewer;
    private Writer out;

    /**
       Construct an OSMMapExtractor.
       @param map The map.
       @param viewer The viewer.
       @param out The writer to write extracted data to.
    */
    public OSMMapExtractor(OSMMap map, OSMMapViewer viewer, Writer out) {
        this.map = map;
        this.viewer = viewer;
        this.out = out;
    }

    /**
       Start the OSMMapExtractor.
       @param args Command line arguments: source target.
    */
    public static void main(String[] args) {
        try {
            OSMMap map = new OSMMap(new File(args[0]));
            Writer out = new FileWriter(new File(args[1]));

            JFrame frame = new JFrame();
            glass = new DragGlass();
            frame.setGlassPane(glass);
            OSMMapViewer viewer = new OSMMapViewer(map);
            viewer.addMouseListener(new OSMMapExtractor(map, viewer, out));
            viewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            frame.setContentPane(viewer);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        // CHECKSTYLE:OFF:MagicNumber
        catch (Exception e) {
            e.printStackTrace();
        }
        // CHECKSTYLE:ON:MagicNumber
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            Point p = e.getPoint();
            Insets insets = viewer.getInsets();
            p.translate(-insets.left, -insets.top);
            press = new Point(p);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            Point p = e.getPoint();
            Insets insets = viewer.getInsets();
            p.translate(-insets.left, -insets.top);
            drag = new Point(p);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            Point p = e.getPoint();
            Insets insets = viewer.getInsets();
            p.translate(-insets.left, -insets.top);
            release = new Point(p);
            drag = null;
            glass.repaint();
            write();
        }
    }

    private void write() {
        double pressLat = viewer.getLatitude(press.y);
        double pressLon = viewer.getLongitude(press.x);
        double releaseLat = viewer.getLatitude(release.y);
        double releaseLon = viewer.getLongitude(release.x);
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
        // CHECKSTYLE:OFF:IllegalCatch
        catch (Exception ex) {
            ex.printStackTrace();
        }
        // CHECKSTYLE:ON:IllegalCatch
    }

    private static class DragGlass extends JComponent {
        public void paintComponent(Graphics g) {
            if (drag == null) {
                return;
            }
            g.setColor(DRAG_COLOUR);
            int x = Math.min(press.x, drag.x);
            int y = Math.max(press.y, drag.y);
            int width = (int)Math.abs(press.x - drag.x);
            int height = (int)Math.abs(press.y - drag.y);
            g.fillRect(x, y, width, height);
        }
    }
}