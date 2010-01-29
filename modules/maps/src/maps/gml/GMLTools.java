package maps.gml;

import java.util.List;
import java.util.Iterator;

import java.awt.geom.Rectangle2D;

/**
   Useful tools for manipulating GML.
*/
public final class GMLTools {
    private GMLTools() {
    }

    /**
       Turn a list of coordinates into a string suitable for putting into an XML document.
       @param coords The coordinate list.
       @return A string version of the list.
    */
    public static String getCoordinatesString(List<GMLCoordinates> coords) {
        StringBuilder result = new StringBuilder();
        for (Iterator<GMLCoordinates> it = coords.iterator(); it.hasNext();) {
            GMLCoordinates next = it.next();
            result.append(String.valueOf(next.getX()));
            result.append(",");
            result.append(String.valueOf(next.getY()));
            if (it.hasNext()) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
       Get the bounds of a set of coordinates.
       @param coords The coordinate list.
       @return The bounds of the coordinates.
    */
    public static Rectangle2D getBounds(List<GMLCoordinates> coords) {
        if (coords.isEmpty()) {
            return null;
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (GMLCoordinates next : coords) {
            minX = Math.min(minX, next.getX());
            minY = Math.min(minY, next.getY());
            maxX = Math.max(maxX, next.getX());
            maxY = Math.max(maxY, next.getY());
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}