package gis2.gml.objects;

import java.awt.Shape;
import java.awt.Polygon;

import gis2.gml.manager.GMLWorldManager;

public class GMLFace extends GMLObject {

    private GMLDirectedEdge[] dedges;
    private String type;
    private Shape shape;

    public GMLFace(GMLID id, GMLWorldManager w, GMLDirectedEdge[] es) {
        super(id, w);
        if (es == null) {
            throw new NullPointerException();
        }
        setDirectedEdges(es);
    }

    public void setType(String t) {
        type = t;
    }

    public String getType() {
        return type;
    }

    public void setDirectedEdges(GMLDirectedEdge[] es) {
        dedges = es;
        update();
    }

    public GMLDirectedEdge[] getDirectedEdges() {
        return dedges;
    }

    public void update() {
        Polygon polygon = new Polygon();
        for (GMLDirectedEdge dedge : getDirectedEdges()) {
            boolean orientation = dedge.getOrientation();
            GMLEdge edge = getManager().getEdge(dedge.getEdgeID());
            for (GMLID nid : edge.createDirectedNodeIDs(orientation)) {
                GMLNode node = getManager().getNode(nid);
                int x = (int)node.getX();
                int y = (int)node.getY();
                polygon.addPoint(x, y);
            }
        }
        shape = polygon;
    }

    public Shape getShape() {
        return shape;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(dedges[0]);
        for (int i = 1; i < dedges.length; i++) {
            sb.append(",").append(dedges[i]);
        }
        return "GMLFace[" + sb + "]";
    }
}