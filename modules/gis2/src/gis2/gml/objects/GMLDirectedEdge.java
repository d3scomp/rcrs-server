package gis2.gml.objects;

public class GMLDirectedEdge {

    private boolean orientation;
    private GMLID edgeID;

    public GMLDirectedEdge(boolean o, GMLID eID) {
        orientation = o;
        edgeID = eID;
    }

    public boolean getOrientation() {
        return orientation;
    }

    public GMLID getEdgeID() {
        return edgeID;
    }
 
    public void setEdgeID(GMLID eID) {
        edgeID = eID;
    }

    public String toString() {
        if (orientation) {
            return "+" + edgeID;
        }
        else {
            return "-" + edgeID;
        }
    }
}