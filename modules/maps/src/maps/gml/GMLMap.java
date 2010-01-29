package maps.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

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

    /**
       Add a building.
       @param b The building to add.
    */
    public void addBuilding(GMLBuilding b) {
        buildings.put(b.getID(), b);
        allShapes.add(b);
        boundsKnown = false;
    }

    /**
       Remove a building.
       @param b The building to remove.
    */
    public void removeBuilding(GMLBuilding b) {
        buildings.remove(b.getID());
        allShapes.remove(b);
        boundsKnown = false;
    }

    /**
       Get a building by ID.
       @param id The ID to look up.
       @return The building with that ID or null if the ID is not found.
    */
    public GMLBuilding getBuilding(long id) {
        return buildings.get(id);
    }

    /**
       Get all buildings in the map.
       @return All buildings.
    */
    public Set<GMLBuilding> getBuildings() {
        return new HashSet<GMLBuilding>(buildings.values());
    }

    /**
       Remove all buildings.
    */
    public void removeAllBuildings() {
        allShapes.removeAll(buildings.values());
        buildings.clear();
        boundsKnown = false;
    }

    /**
       Add a road.
       @param r The road to add.
    */
    public void addRoad(GMLRoad r) {
        roads.put(r.getID(), r);
        allShapes.add(r);
        boundsKnown = false;
    }

    /**
       Remove a road.
       @param r The road to remove.
    */
    public void removeRoad(GMLRoad r) {
        roads.remove(r.getID());
        allShapes.remove(r);
        boundsKnown = false;
    }

    /**
       Get a road by ID.
       @param id The ID to look up.
       @return The road with that ID or null if the ID is not found.
    */
    public GMLRoad getRoad(long id) {
        return roads.get(id);
    }

    /**
       Get all roads in the map.
       @return All roads.
    */
    public Set<GMLRoad> getRoads() {
        return new HashSet<GMLRoad>(roads.values());
    }

    /**
       Remove all roads.
    */
    public void removeAllRoads() {
        allShapes.removeAll(roads.values());
        roads.clear();
        boundsKnown = false;
    }

    /**
       Add a space.
       @param s The space to add.
    */
    public void addSpace(GMLSpace s) {
        spaces.put(s.getID(), s);
        allShapes.add(s);
        boundsKnown = false;
    }

    /**
       Remove a space.
       @param s The space to remove.
    */
    public void removeSpace(GMLSpace s) {
        spaces.remove(s.getID());
        allShapes.remove(s);
        boundsKnown = false;
    }

    /**
       Get a space by ID.
       @param id The ID to look up.
       @return The space with that ID or null if the ID is not found.
    */
    public GMLSpace getSpace(long id) {
        return spaces.get(id);
    }

    /**
       Get all spaces in the map.
       @return All spaces.
    */
    public Set<GMLSpace> getSpaces() {
        return new HashSet<GMLSpace>(spaces.values());
    }

    /**
       Remove all spaces.
    */
    public void removeAllSpaces() {
        allShapes.removeAll(spaces.values());
        spaces.clear();
        boundsKnown = false;
    }

    /**
       Get all shapes in the map.
       @return All shapes.
    */
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