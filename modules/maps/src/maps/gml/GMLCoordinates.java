package maps.gml;

/**
   A set of GML coordinates. These coordinates can be any unit.
*/
public class GMLCoordinates {
    private double x;
    private double y;

    /**
       Create a new GMLCoordinates object.
       @param x The X coordinate.
       @param y The Y coordinate.
     */
    public GMLCoordinates(double x, double y) {
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
       @return The y coordinate.
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