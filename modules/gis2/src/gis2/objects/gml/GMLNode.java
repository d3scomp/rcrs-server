package gis2.objects.gml;

public class GMLNode extends GMLObject {

    private double x;
    private double y;

    public GMLNode(GMLID id, double x, double y) {
        super(id);
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