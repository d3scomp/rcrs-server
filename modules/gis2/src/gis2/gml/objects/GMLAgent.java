package gis2.gml.objects;

import gis2.gml.manager.GMLWorldManager;

public class GMLAgent extends GMLObject {

    private GMLID areaID;
    private String type;
    private double x;
    private double y;

    public GMLAgent(GMLID idd, GMLWorldManager w, String t, double xx, double yy, GMLID aid) {
        super(idd, w);
        setType(t);
        setLocation(xx, yy, aid);
    }

    public void setType(String t) {
        type = t;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public GMLID getAreaID() {
        return areaID;
    }

    public void setLocation(double xx, double yy, GMLID aid) {
        x = xx;
        y = yy;
        areaID = aid;
    }

    public String toString() {
        return "GMLAgent[ID:" + getID() + ";Type:" + getType() + ";X:" + getX() + ";Y:" + getY() + "]";
    }
}