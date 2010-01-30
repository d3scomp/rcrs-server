package gis2.gml.objects;

import java.awt.geom.Point2D;

import gis2.gml.manager.GMLWorldManager;

public class GMLEdge extends GMLObject {

    private GMLID[] nodeIDs;
    private GMLID[] faceIDs;
    private Point2D[] border;

    public GMLEdge(GMLID id, GMLWorldManager w, GMLID[] ns, GMLID[] fs) {
        super(id, w);
        setNodeIDs(ns);
        setFaceIDs(fs);
    }

    public void setFaceIDs(GMLID[] fs) {
        faceIDs = fs;
    }

    public GMLID getNextFaceID(GMLID now) {
        if (faceIDs.length == 1) {
            return null;
        }
        if (faceIDs[0].equals(now)) {
            return faceIDs[1];
        }
        else {
            return faceIDs[0];
        }
    }

    public void setBorder(Point2D[] b) {
        border = b;
    }

    public Point2D[] getBorder() {
        return border;
    }

    public Point2D[] createDirectedBorder(boolean orientation) {
        if (orientation) {
            return border;
        }
        else {
            return invert(border, new Point2D[border.length]);
        }
    }

    public void setNodeIDs(GMLID[] ns) {
        nodeIDs = ns;
    }

    public GMLID[] getFaceIDs() {
        return faceIDs;
    }

    public GMLID[] getNodeIDs() {
        return nodeIDs;
    }

    public GMLID[] createDirectedNodeIDs(boolean orientation) {
        if (orientation) {
            return nodeIDs;
        }
        else {
            return invert(nodeIDs, new GMLID[nodeIDs.length]);
        }
    }

    private <T> T[] invert(T[] arr, T[] buf) {
        for (int i = 0; i < arr.length; i++) {
            buf[i] = arr[arr.length - 1 - i];
        }
        return buf;
     }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(nodeIDs[0]);
        for (int i = 1; i < nodeIDs.length; i++) {
            sb.append(",").append(nodeIDs[i]);
        }
        sb.append("}");
        return "GMLEdge[ID:" + getID() + ";nodes:" + sb + ";]";
    }
}