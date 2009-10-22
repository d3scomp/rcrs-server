package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Point2D;

public class ManagedPoint2D extends Point2D {
    private int id;

    public ManagedPoint2D(double x, double y, int id) {
        super(x, y);
        this.id = id;
    }

    public ManagedPoint2D(Point2D p, int id) {
        super(p.getX(), p.getY());
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ManagedPoint2D) {
            return ((ManagedPoint2D)o).id == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "ManagedPoint2D " + id + ": " + super.toString();
    }
}