package osm2gml.gml;

/**
   A GML node object.
 */
public class GMLNode extends GMLObject {
    private Coordinates coordinates;

    /**
       Construct a new GML node.
       @param id The ID of this node.
       @param x The X coordinate of this node.
       @param y The Y coordinate of this node.
     */
    public GMLNode(long id, double x, double y) {
        this(id, new Coordinates(x, y));
    }

    /**
       Construct a new GML node.
       @param id The ID of this node.
       @param coodinates The coordinates of this node.
     */
    public GMLNode(long id, Coordinates coordinates) {
        super(id);
        this.coordinates = coordinates;
    }

    /**
       Get the coordinates of this node.
       @return The node coordinates.
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
     */
    public double getX() {
        return coordinates.getX();
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
     */
    public double getY() {
        return coordinates.getY();
    }

    @Override
    public String toString() {
        return "GMLNode " + getID() + " at " + coordinates;
    }
}