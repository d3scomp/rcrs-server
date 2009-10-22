package maps.gml;

import java.util.List;
import java.util.ArrayList;

public abstract class GMLShape {
    private long id;
    private List<GMLCoordinates> coordinates;

    protected GMLShape(long id) {
        this.id = id;
        this.coordinates = new ArrayList<GMLCoordinates>();
    }

    protected GMLShape(long id, List<GMLCoordinates> coordinates) {
        this(id);
        this.coordinates.addAll(coordinates);
    }

    public long getID() {
        return id;
    }

    public List<GMLCoordinates> getCoordinates() {
        return new ArrayList<GMLCoordinates>(coordinates);
    }
}