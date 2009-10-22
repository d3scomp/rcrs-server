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

/**
   A ShapeInfo that knows how to draw GMLNodes.
*/
public class GMLNodeShapeInfo extends ShapeDebugFrame.Point2DShapeInfo {
    private GMLNode node;

    /**
       Create a new GMLNodeShapeInfo.
       @param node The node to draw.
       @param name The name of the node.
       @param colour The colour to draw the node.
       @param square Whether to draw the node with a square or not.
     */
    public GMLNodeShapeInfo(GMLNode node, String name, Color colour, boolean square)  {
        super(gmlNodeToPoint(node), name, colour, square);
        this.node = node;
    }

    @Override
    public Object getObject() {
        return node;
    }

    private static Point2D gmlNodeToPoint(GMLNode node) {
        return new Point2D(node.getX(), node.getY());
    }
}