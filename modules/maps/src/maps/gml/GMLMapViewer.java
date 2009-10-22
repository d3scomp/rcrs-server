package maps.gml;

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

public class GMLMapViewer extends JComponent {
    private GMLMap map;
    private ScreenTransform transform;
    private PanZoomListener panZoom;

    public final static Color BUILDING_COLOUR = new Color(0, 255, 0, 128); // Transparent lime
    public final static Color INTERSECTION_COLOUR = new Color(192, 192, 192, 128); // Transparent silver
    public final static Color ROAD_COLOUR = new Color(128, 128, 128, 128); // Transparent gray
    public final static Color SPACE_COLOUR = new Color(0, 128, 0, 128); // Transparent green
    public final static Color OUTLINE_COLOUR = Color.BLACK;

    //    private final static Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {1, 1}, 0);
    //    private final static Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    //    private final static Stroke IMPASSABLE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    //    private final static Color PASSABLE_COLOUR = new Color(0, 0, 255); // Blue
    //    private final static Color IMPASSABLE_COLOUR = new Color(0, 0, 128); // Navy

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
        for (GMLRoad next : map.getRoads()) {
            paint(next, g, ROAD_COLOUR);
        }
        for (GMLBuilding next : map.getBuildings()) {
            paint(next, g, BUILDING_COLOUR);
        }
        for (GMLSpace next : map.getSpaces()) {
            paint(next, g, SPACE_COLOUR);
        }
    }

    private void paint(GMLShape shape, Graphics2D g, Color fill) {
        Polygon p = makePolygon(shape);
        g.setColor(OUTLINE_COLOUR);
        g.draw(p);
        g.setColor(fill);
        g.fill(p);
    }

    private Polygon makePolygon(GMLShape shape) {
        List<GMLCoordinates> c = shape.getCoordinates();
        int[] xs = new int[c.size()];
        int[] ys = new int[c.size()];
        int i = 0;
        for (GMLCoordinates next : c) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            ++i;
        }
        return new Polygon(xs, ys, c.size());
    }
}