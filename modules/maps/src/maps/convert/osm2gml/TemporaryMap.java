package maps.convert.osm2gml;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import maps.osm.OSMMap;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.collections.LazyMap;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

public class TemporaryMap {
    private static final Logger LOG = LogManager.getLogger(TemporaryMap.class);

    /**
       The threshold for determining if nodes are co-located in metres.
    */
    private static final double NEARBY_THRESHOLD_M = 1;

    private double threshold;

    private Set<Node> nodes;
    private Set<Edge> edges;
    private Map<Node, Set<Edge>> edgesAtNode;
    private Map<Edge, Set<TemporaryObject>> objectsAtEdge;
    private Set<TemporaryRoad> tempRoads;
    private Set<TemporaryIntersection> tempIntersections;
    private Set<TemporaryBuilding> tempBuildings;
    private Set<TemporaryObject> allObjects;

    private OSMMap osmMap;
    private Collection<OSMIntersectionInfo> osmIntersections;
    private Collection<OSMRoadInfo> osmRoads;
    private Collection<OSMBuildingInfo> osmBuildings;

    private int nextID;

    public TemporaryMap(OSMMap osmMap) {
        this.osmMap = osmMap;
        nextID = 0;
        nodes = new HashSet<Node>();
        edges = new HashSet<Edge>();
        threshold = ConvertTools.nearbyThreshold(osmMap, NEARBY_THRESHOLD_M);
        tempRoads = new HashSet<TemporaryRoad>();
        tempIntersections = new HashSet<TemporaryIntersection>();
        tempBuildings = new HashSet<TemporaryBuilding>();
        allObjects = new HashSet<TemporaryObject>();
        edgesAtNode = new LazyMap<Node, Set<Edge>>() {
            @Override
            public Set<Edge> createValue() {
                return new HashSet<Edge>();
            }            
        };
        objectsAtEdge = new LazyMap<Edge, Set<TemporaryObject>>() {
            @Override
            public Set<TemporaryObject> createValue() {
                return new HashSet<TemporaryObject>();
            }            
        };
    }

    /**
       Get the OSMMap.
       @return The OSMMap.
    */
    public OSMMap getOSMMap() {
        return osmMap;
    }

    /**
       Set the OSMMap information.
       @param intersections The set of intersections.
       @param roads The set of roads.
       @param buildings The set of buildings.
    */
    public void setOSMInfo(Collection<OSMIntersectionInfo> intersections, Collection<OSMRoadInfo> roads, Collection<OSMBuildingInfo> buildings) {
        osmIntersections = new HashSet<OSMIntersectionInfo>(intersections);
        osmRoads = new HashSet<OSMRoadInfo>(roads);
        osmBuildings = new HashSet<OSMBuildingInfo>(buildings);
    }

    /**
       Get the OSM intersection info.
       @return The OSM intersection info.
    */
    public Collection<OSMIntersectionInfo> getOSMIntersectionInfo() {
        return Collections.unmodifiableCollection(osmIntersections);
    }

    /**
       Get the OSM road info.
       @return The OSM road info.
    */
    public Collection<OSMRoadInfo> getOSMRoadInfo() {
        return Collections.unmodifiableCollection(osmRoads);
    }

    /**
       Get the OSM building info.
       @return The OSM building info.
    */
    public Collection<OSMBuildingInfo> getOSMBuildingInfo() {
        return Collections.unmodifiableCollection(osmBuildings);
    }

    public void addRoad(TemporaryRoad road) {
        tempRoads.add(road);
        addObject(road);
    }

    public void removeRoad(TemporaryRoad road) {
        tempRoads.remove(road);
        removeObject(road);
    }

    public void addIntersection(TemporaryIntersection intersection) {
        tempIntersections.add(intersection);
        addObject(intersection);
    }

    public void removeIntersection(TemporaryIntersection intersection) {
        tempIntersections.remove(intersection);
        removeObject(intersection);
    }

    public void addBuilding(TemporaryBuilding building) {
        tempBuildings.add(building);
        addObject(building);
    }

    public void removeBuilding(TemporaryBuilding building) {
        tempBuildings.remove(building);
        removeObject(building);
    }

    public void addTemporaryObject(TemporaryObject object) {
        if (object instanceof TemporaryRoad) {
            addRoad((TemporaryRoad)object);
        }
        if (object instanceof TemporaryIntersection) {
            addIntersection((TemporaryIntersection)object);
        }
        if (object instanceof TemporaryBuilding) {
            addBuilding((TemporaryBuilding)object);
        }
    }

    public void removeTemporaryObject(TemporaryObject object) {
        if (object instanceof TemporaryRoad) {
            removeRoad((TemporaryRoad)object);
        }
        if (object instanceof TemporaryIntersection) {
            removeIntersection((TemporaryIntersection)object);
        }
        if (object instanceof TemporaryBuilding) {
            removeBuilding((TemporaryBuilding)object);
        }
    }

    public Collection<TemporaryRoad> getRoads() {
        return new HashSet<TemporaryRoad>(tempRoads);
    }

    public Collection<TemporaryIntersection> getIntersections() {
        return new HashSet<TemporaryIntersection>(tempIntersections);
    }

    public Collection<TemporaryBuilding> getBuildings() {
        return new HashSet<TemporaryBuilding>(tempBuildings);
    }

    public Collection<TemporaryObject> getAllObjects() {
        return new HashSet<TemporaryObject>(allObjects);
    }

    public Collection<Node> getAllNodes() {
        return new HashSet<Node>(nodes);
    }

    public Collection<Edge> getAllEdges() {
        return new HashSet<Edge>(edges);
    }

    public Set<TemporaryObject> getAttachedObjects(Edge e) {
        return new HashSet<TemporaryObject>(objectsAtEdge.get(e));
    }
    
    public Set<Edge> getAttachedEdges(Node n) {
        return new HashSet<Edge>(edgesAtNode.get(n));
    }

    /**
       Set the threshold for deciding if two points are the same. The {@link #snap(double, double)} method uses this value to check if a new point needs to be registered.
       @param t The new threshold.
    */
    public void setNearbyThreshold(double t) {
        threshold = t;
    }

    /**
       Get the threshold for deciding if two points are the same. The {@link #snap(double, double)} method uses this value to check if a new point needs to be registered.
       @return The nearby threshold.
    */
    public double getNearbyThreshold() {
        return threshold;
    }

    /**
       Find out if two points are nearby.
       @param point1 The first point.
       @param point2 The second point.
       @return True iff the two points are within the nearby threshold.
    */
    public boolean isNear(Point2D point1, Point2D point2) {
        return isNear(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    /**
       Find out if two points are nearby.
       @param x1 The x coordinate of the first point.
       @param y1 The y coordinate of the first point.
       @param x2 The x coordinate of the second point.
       @param y2 The y coordinate of the second point.
       @return True iff the two points are within the nearby threshold.
    */
    public boolean isNear(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx >= - threshold &&
                dx <= threshold &&
                dy >= - threshold &&
                dy <= threshold);
    }

    public Node getNode(Point2D p) {
        return getNode(p.getX(), p.getY());
    }

    public Node getNode(double x, double y) {
        for (Node next : nodes) {
            if (isNear(x, y, next.getX(), next.getY())) {
                return next;
            }
        }
        return createNode(x, y);
    }

    public Edge getEdge(Node from, Node to) {
        for (Edge next : edges) {
            if (next.getStart().equals(from) && next.getEnd().equals(to)
                || next.getStart().equals(to) && next.getEnd().equals(from)) {
                return next;
            }
        }
        return createEdge(from, to);
    }

    public DirectedEdge getDirectedEdge(Node from, Node to) {
        Edge e = getEdge(from, to);
        return new DirectedEdge(e, from);
    }

    /**
       Replace an existing edge with a set of new edges.
       @param edge The old edge.
       @param newEdges The new edges.
    */
    public void replaceEdge(Edge edge, Collection<Edge> newEdges) {
        for (TemporaryObject next : getAttachedObjects(edge)) {
            next.replaceEdge(edge, newEdges);
            for (Edge nextEdge : newEdges) {
                objectsAtEdge.get(nextEdge).add(next);
            }
        }
        removeEdge(edge);
    }

    /**
       Split an edge into chunks.
       @param edge The edge to split.
       @param splitPoints The points to split the line. These must be in order along the line.
       @return The replacement edges.
    */
    public List<Edge> splitEdge(Edge edge, Node... splitPoints) {
        List<Edge> replacements = new ArrayList<Edge>();
        Edge current = edge;
        for (Node n : splitPoints) {
            if (n.equals(current.getStart()) || n.equals(current.getEnd())) {
                // Don't bother if the split point is at the origin or endpoint
                continue;
            }
            replacements.add(getEdge(current.getStart(), n));
            current = getEdge(n, current.getEnd());
        }
        if (!edge.equals(current)) {
            replacements.add(current);
        }
        if (!replacements.isEmpty()) {
            replaceEdge(edge, replacements);
        }
        return replacements;
    }

    private Node createNode(double x, double y) {
        Node result = new Node(nextID++, x, y);
        nodes.add(result);
        return result;
    }

    private Edge createEdge(Node from, Node to) {
        Edge result = new Edge(nextID++, from, to);
        edges.add(result);
        edgesAtNode.get(from).add(result);
        edgesAtNode.get(to).add(result);
        //        LOG.debug("Created edge " + result);
        return result;
    }

    private void addObject(TemporaryObject object) {
        allObjects.add(object);
        for (DirectedEdge next : object.getEdges()) {
            objectsAtEdge.get(next.getEdge()).add(object);
        }
    }

    private void removeNode(Node n) {
        nodes.remove(n);
        edgesAtNode.remove(n);
    }

    private void removeEdge(Edge e) {
        edges.remove(e);
        edgesAtNode.get(e.getStart()).remove(e);
        edgesAtNode.get(e.getEnd()).remove(e);
        objectsAtEdge.remove(e);
        //        LOG.debug("Removed edge " + e);
    }

    private void removeObject(TemporaryObject object) {
        allObjects.remove(object);
        for (DirectedEdge next : object.getEdges()) {
            objectsAtEdge.get(next.getEdge()).remove(object);
        }
    }
}