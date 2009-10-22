package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;

public class ManagedLine2D extends Line2D {
    private int id;

    public ManagedLine2D(ManagedPoint2D origin, ManagedPoint2D end, int id) {
        super(origin, end);
        this.id = id;
        if (origin.equals(end)) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ManagedLine2D) {
            return ((ManagedLine2D)o).id == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public ManagedPoint2D getOrigin() {
        return (ManagedPoint2D)super.getOrigin();
    }

    @Override
    public ManagedPoint2D getEndPoint() {
        return (ManagedPoint2D)super.getEndPoint();
    }

    @Override
    public String toString() {
        return "ManagedLine2D " + id + ": " + super.toString();
    }
}