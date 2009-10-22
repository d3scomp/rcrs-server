package osm2gml.gml;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import javax.swing.JComponent;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.DrawingTools;
import rescuecore2.misc.gui.PanZoomListener;
import rescuecore2.misc.Pair;

import osm2gml.Constants;

public class GMLMapViewer extends JComponent {
    private GMLMap map;
    private ScreenTransform transform;
    private PanZoomListener panZoom;

    public final static Color BUILDING_COLOUR = Constants.TRANSPARENT_LIME;
    public final static Color INTERSECTION_COLOUR = Constants.TRANSPARENT_SILVER;
    public final static Color ROAD_COLOUR = Constants.TRANSPARENT_GRAY;

    //    private final static Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {1, 1}, 0);
    private final static Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private final static Stroke IMPASSABLE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private final static Color PASSABLE_COLOUR = Constants.BLUE;
    private final static Color IMPASSABLE_COLOUR = Constants.NAVY;

    public GMLMapViewer() {
        this(null);
    }

    public GMLMapViewer(GMLMap map) {
        panZoom = new PanZoomListener(this);
        setMap(map);
    }

    public void setMap(GMLMap map) {
        this.map = map;
        transform = null;
        if (map != null) {
            transform = new ScreenTransform(map.getMinX(), map.getMinY(), map.getMaxX(), map.getMaxY());
        }
        panZoom.setScreenTransform(transform);
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (map == null) {
            return;
        }
        Insets insets = getInsets();
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        Graphics2D g = (Graphics2D)graphics.create(insets.left, insets.top, width + 1 , height + 1);
        transform.rescale(width, height);
        for (GMLFace next : map.getFaces()) {
            List<Coordinates> cos = next.getOutline();
            int[] xs = new int[cos.size()];
            int[] ys = new int[cos.size()];
            int i = 0;
            for (Coordinates co : cos) {
                xs[i] = transform.xToScreen(co.getX());
                ys[i] = transform.yToScreen(co.getY());
                ++i;
            }
            switch (next.getFaceType()) {
            case ROAD:
                g.setColor(ROAD_COLOUR);
                break;
            case INTERSECTION:
                g.setColor(INTERSECTION_COLOUR);
                break;
            case BUILDING:
                g.setColor(BUILDING_COLOUR);
                break;
            }
            g.fill(new Polygon(xs, ys, i));
        }
        for (GMLEdge next : map.getEdges()) {
            g.setStroke(next.isPassable() ? PASSABLE_STROKE : IMPASSABLE_STROKE);
            g.setColor(next.isPassable() ? PASSABLE_COLOUR : IMPASSABLE_COLOUR);
            List<Coordinates> cos = next.getPoints();
            int[] xs = new int[cos.size()];
            int[] ys = new int[cos.size()];
            int i = 0;
            for (Coordinates co : cos) {
                xs[i] = transform.xToScreen(co.getX());
                ys[i] = transform.yToScreen(co.getY());
                ++i;
            }
            for (int j = 0; j < i - 1; ++j) {
                int startX = xs[j];
                int startY = ys[j];
                int endX = xs[j + 1];
                int endY = ys[j + 1];
                g.drawLine(startX, startY, endX, endY);
                // Draw arrow barbs
                //                DrawingTools.drawArrowHeads(startX, startY, endX, endY, g);
            }
        }
        g.setColor(Color.black);
        for (GMLNode next : map.getNodes()) {
            int x = transform.xToScreen(next.getX());
            int y = transform.yToScreen(next.getY());
            g.fillRect(x - 2, y - 2, 5, 5);
        }
    }
}