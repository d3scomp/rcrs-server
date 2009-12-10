package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.geom.Line2D;

import rescuecore2.standard.entities.Road;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders roads.
 */
public class RoadLayer extends StandardEntityViewLayer<Road> {
    private static final Color ROAD_COLOUR = new Color(185, 185, 185);

    /**
       Construct a road rendering layer.
     */
    public RoadLayer() {
        super(Road.class);
    }

    @Override
    public String getName() {
        return "Roads";
    }

    @Override
    public Shape render(Road r, Graphics2D g, ScreenTransform t) {
        int[] apexes = r.getApexes();
        int count = apexes.length / 2;
        int[] xs = new int[count];
        int[] ys = new int[count];
        for (int i = 0; i < count; ++i) {
            xs[i] = t.xToScreen(apexes[i * 2]);
            ys[i] = t.yToScreen(apexes[(i * 2) + 1]);
        }
        Polygon shape = new Polygon(xs, ys, count);
        g.setColor(ROAD_COLOUR);
        g.fill(shape);
        // Draw a line to each neighbour
        return shape;
    }
}