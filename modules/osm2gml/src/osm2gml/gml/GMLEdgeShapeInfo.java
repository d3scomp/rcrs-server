package osm2gml.gml;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.gui.DrawingTools;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

/**
   A ShapeInfo that knows how to draw GMLEdges.
*/
public class GMLEdgeShapeInfo extends ShapeDebugFrame.Line2DShapeInfo {
    private GMLEdge edge;

    /**
       Create a new GMLEdgeShapeInfo.
       @param edge The edge to draw.
       @param name The name of the edge.
       @param colour The colour to draw the edge.
       @param thick Whether to draw the edge thick or not.
     */
    public GMLEdgeShapeInfo(GMLEdge edge, String name, Color colour, boolean thick)  {
        super(gmlEdgeToLine(edge), name, colour, thick, false);
        this.edge = edge;
    }

    @Override
    public Object getObject() {
        return edge;
    }

    private static Line2D gmlEdgeToLine(GMLEdge edge) {
        if (edge == null) {
            return null;
        }
        GMLNode start = edge.getStart();
        GMLNode end = edge.getEnd();
        Point2D origin = new Point2D(start.getX(), start.getY());
        Point2D endPoint = new Point2D(end.getX(), end.getY());
        return new Line2D(origin, endPoint);
    }
}