package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

public class IDManager {
    private int nextID;

    public IDManager() {
        nextID = 0;
    }

    public int generateID() {
        return nextID++;
    }

    public ManagedPoint2D manage(Point2D p) {
        if (p instanceof ManagedPoint2D) {
            return (ManagedPoint2D)p;
        }
        return new ManagedPoint2D(p, generateID());
    }

    public ManagedLine2D manage(Line2D l) {
        if (l instanceof ManagedLine2D) {
            return (ManagedLine2D)l;
        }
        return new ManagedLine2D(manage(l.getOrigin()), manage(l.getEndPoint()), generateID());
    }
}