package osm2gml.gml;

/**
   A set of GML coordinates. These coordinates are latitude/longitude.
*/
public class Coordinates {
    private double x;
    private double y;

    /**
       Create a new Coordinates.
       @param x The X coordinate.
       @param y The Y coordinate.
     */
    public Coordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
     */
    public double getX() {
        return x;
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(x);
        result.append(", ");
        result.append(y);
        return result.toString();
    }
}