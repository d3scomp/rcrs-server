package maps.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.geometry.Point2D;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.Attribute;
import org.dom4j.XPath;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;

import maps.CoordinateConversion;

/**
   A GML map.
*/
public class GMLMap {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private boolean boundsKnown;

    private Map<Long, GMLBuilding> buildings;
    private Map<Long, GMLRoad> roads;
    private Map<Long, GMLSpace> spaces;
    private Set<GMLShape> allShapes;

    /**
       Construct an empty GML map.
     */
    public GMLMap() {
        buildings = new HashMap<Long, GMLBuilding>();
        roads = new HashMap<Long, GMLRoad>();
        spaces = new HashMap<Long, GMLSpace>();
        allShapes = new HashSet<GMLShape>();
        boundsKnown = false;
    }

    public void addBuilding(GMLBuilding b) {
        buildings.put(b.getID(), b);
        allShapes.add(b);
        boundsKnown = false;
    }

    public void removeBuilding(GMLBuilding b) {
        buildings.remove(b.getID());
        allShapes.remove(b);
        boundsKnown = false;
    }

    public GMLBuilding getBuilding(long id) {
        return buildings.get(id);
    }

    public Set<GMLBuilding> getBuildings() {
        return new HashSet<GMLBuilding>(buildings.values());
    }

    public void removeAllBuildings() {
        allShapes.removeAll(buildings.values());
        buildings.clear();
        boundsKnown = false;
    }

    public void addRoad(GMLRoad r) {
        roads.put(r.getID(), r);
        allShapes.add(r);
        boundsKnown = false;
    }

    public void removeRoad(GMLRoad r) {
        roads.remove(r.getID());
        allShapes.remove(r);
        boundsKnown = false;
    }

    public GMLRoad getRoad(long id) {
        return roads.get(id);
    }

    public Set<GMLRoad> getRoads() {
        return new HashSet<GMLRoad>(roads.values());
    }

    public void removeAllRoads() {
        allShapes.removeAll(roads.values());
        roads.clear();
        boundsKnown = false;
    }

    public void addSpace(GMLSpace s) {
        spaces.put(s.getID(), s);
        allShapes.add(s);
        boundsKnown = false;
    }

    public void removeSpace(GMLSpace s) {
        spaces.remove(s.getID());
        allShapes.remove(s);
        boundsKnown = false;
    }

    public GMLSpace getSpace(long id) {
        return spaces.get(id);
    }

    public Set<GMLSpace> getSpaces() {
        return new HashSet<GMLSpace>(spaces.values());
    }

    public void removeAllSpaces() {
        allShapes.removeAll(spaces.values());
        spaces.clear();
        boundsKnown = false;
    }

    public Set<GMLShape> getAllShapes() {
        return Collections.unmodifiableSet(allShapes);
    }

    /**
       Get the minimum x coordinate.
       @return The minimum x coordinate.
     */
    public double getMinX() {
        calculateBounds();
        return minX;
    }

    /**
       Get the maximum x coordinate.
       @return The maximum x coordinate.
     */
    public double getMaxX() {
        calculateBounds();
        return maxX;
    }

    /**
       Get the minimum y coordinate.
       @return The minimum y coordinate.
     */
    public double getMinY() {
        calculateBounds();
        return minY;
    }

    /**
       Get the maximum y coordinate.
       @return The maximum y coordinate.
     */
    public double getMaxY() {
        calculateBounds();
        return maxY;
    }

    private void calculateBounds() {
        if (boundsKnown) {
            return;
        }
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
        for (GMLShape shape : allShapes) {
            for (GMLCoordinates next : shape.getCoordinates()) {
                minX = Math.min(minX, next.getX());
                maxX = Math.max(maxX, next.getX());
                minY = Math.min(minY, next.getY());
                maxY = Math.max(maxY, next.getY());
            }
        }
        boundsKnown = true;
    }
}