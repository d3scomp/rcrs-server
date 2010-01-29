package maps.gml;

import java.util.List;
import java.util.ArrayList;

/**
   Abstract base class for shapes in GML space.
*/
public abstract class GMLShape {
    private long id;
    private List<GMLCoordinates> coordinates;

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
    */
    protected GMLShape(long id) {
        this.id = id;
        this.coordinates = new ArrayList<GMLCoordinates>();
    }

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
       @param coordinates The coordinates of the apexes of the shape.
    */
    protected GMLShape(long id, List<GMLCoordinates> coordinates) {
        this(id);
        this.coordinates.addAll(coordinates);
    }

    /**
       Get the ID of this shape.
       @return The ID.
    */
    public long getID() {
        return id;
    }

    /**
       Get the coordinates of the apexes of this shape.
       @return The apex coordinates.
    */
    public List<GMLCoordinates> getCoordinates() {
        return new ArrayList<GMLCoordinates>(coordinates);
    }
}