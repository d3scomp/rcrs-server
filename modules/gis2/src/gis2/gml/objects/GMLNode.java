package gis2.gml.objects;

import gis2.gml.manager.GMLWorldManager;

public class GMLNode extends GMLObject {

    private double x;
    private double y;

    public GMLNode(GMLID id, GMLWorldManager w, double x, double y) {
        super(id, w);
        setLocation(x, y);
    }

    public void setLocation(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String toString() {
        return "GMLNode[ID:" + getID() + ";X:" + getX() + ";Y:" + y + "]";
    }
}