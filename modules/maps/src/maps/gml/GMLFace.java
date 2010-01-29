package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
   A GMLFace is a set of directed edges, an optional outline polygon and an optional type.
 */
public class GMLFace extends GMLObject {
    private List<GMLDirectedEdge> edges;
    private List<GMLCoordinates> points;
    private Area area;
    private FaceType type;
    private long originalBuildingID;

    /**
       Construct a new GMLFace that calculates it's own outline shape from the directed edges.
       @param id The ID of the face.
       @param type The type of this face.
       @param edges The edges of the face.
    */
    public GMLFace(long id, FaceType type, List<GMLDirectedEdge> edges) {
        this(id, type, edges, null);
    }

    /**
       Construct a new GMLFace.
       @param id The ID of the face.
       @param type The type of this face.
       @param edges The edges of the face.
       @param points The outline of the face. If this is null or empty then the outline will be calculated based on the directed edges.
    */
    public GMLFace(long id, FaceType type, List<GMLDirectedEdge> edges, List<GMLCoordinates> points) {
        super(id);
        this.type = type;
        this.edges = edges;
        if (points == null || points.isEmpty()) {
            this.points = new ArrayList<GMLCoordinates>();
            computeOutline();
        }
        else {
            this.points = points;
        }
        area = null;
    }

    /**
       Get the list of edges.
       @return The edges of this face.
    */
    public List<GMLDirectedEdge> getEdges() {
        return new ArrayList<GMLDirectedEdge>(edges);
    }

    /**
       Find out if this face is connected to a particular edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge.
    */
    public boolean isConnectedTo(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge) {
                return true;
            }
        }
        return false;
    }

    /**
       Find out if this face is connected to a particular edge on the left-hand side, i.e. in a "forward" direction along the edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge on the left-hand side.
    */
    public boolean isConnectedLeft(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge && next.isForward()) {
                return true;
            }
        }
        return false;
    }

    /**
       Find out if this face is connected to a particular edge on the right-hand side, i.e. in a "backward" direction along the edge.
       @param edge The edge to check.
       @return true iff this face has a directed edge connected to the given edge on the right-hand side.
    */
    public boolean isConnectedRight(GMLEdge edge) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge() == edge && !next.isForward()) {
                return true;
            }
        }
        return false;
    }

    /**
       Replace a GMLEdge with zero or more new edges. The GMLDirectedEdges of this face will be updated appropriately.
       @param edge The edge to replace.
       @param replacements The replacement edges.
    */
    public void replaceEdge(GMLEdge edge, Collection<GMLEdge> replacements) {
        if (replacements.isEmpty()) {
            // Just remove the edge
            for (Iterator<GMLDirectedEdge> it = edges.iterator(); it.hasNext();) {
                GMLDirectedEdge next = it.next();
                if (next.getEdge() == edge) {
                    it.remove();
                }
            }
        }
        else {
            for (ListIterator<GMLDirectedEdge> it = edges.listIterator(); it.hasNext();) {
                GMLDirectedEdge next = it.next();
                if (next.getEdge() == edge) {
                    it.remove();
                    Set<GMLEdge> replacementsSet = new HashSet<GMLEdge>(replacements);
                    // Create directed edges for the replacements
                    GMLNode start = next.getStartNode();
                    GMLNode end = next.getEndNode();
                    while (!start.equals(end)) {
                        GMLDirectedEdge newEdge = findNewEdge(start, replacementsSet);
                        replacementsSet.remove(newEdge.getEdge());
                        it.add(newEdge);
                        start = newEdge.getEndNode();
                    }
                    break;
                }
            }
        }
        computeOutline();
    }

    /**
       Get the coordinates of the outline of this face.
       @return The outline coordinates.
    */
    public List<GMLCoordinates> getOutline() {
        return new ArrayList<GMLCoordinates>(points);
    }

    /**
       Get the type of this face.
       @return The face type.
    */
    public FaceType getFaceType() {
        return type;
    }

    /**
       Get the Area described by this face.
       @return The area.
    */
    public Area getArea() {
        if (area == null) {
            Path2D.Double path = new Path2D.Double();
            Iterator<GMLDirectedEdge> it = edges.iterator();
            GMLDirectedEdge edge = it.next();
            GMLCoordinates c = edge.getStartCoordinates();
            path.moveTo(c.getX(), c.getY());
            while (it.hasNext()) {
                edge = it.next();
                c = edge.getStartCoordinates();
                path.lineTo(c.getX(), c.getY());
            }
            path.closePath();
            area = new Area(path.createTransformedShape(null));
        }
        return area;
    }

    /**
       Set the original building ID.
       @param id The original building ID.
    */
    public void setOriginalBuildingID(long id) {
        originalBuildingID = id;
    }

    /**
       Get the original building ID.
       @return The original building ID.
    */
    public long getOriginalBuildingID() {
        return originalBuildingID;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("GMLFace [");
        for (Iterator<GMLDirectedEdge> it = edges.iterator(); it.hasNext();) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
       Check if this face is a duplicate of another face. Faces are duplicates if they contain the same list of directed edges, possibly offset.
       @param other The other face to check against.
       @return True if this face is a duplicate of other, false otherwise.
    */
    public boolean isDuplicate(GMLFace other) {
        List<GMLDirectedEdge> myEdges = getEdges();
        List<GMLDirectedEdge> otherEdges = other.getEdges();
        if (myEdges.size() != otherEdges.size()) {
            return false;
        }
        Iterator<GMLDirectedEdge> it = myEdges.iterator();
        GMLDirectedEdge start = it.next();
        // See if we can find an equivalent edge in other
        Iterator<GMLDirectedEdge> ix = otherEdges.iterator();
        GMLDirectedEdge otherStart = null;
        while (ix.hasNext()) {
            GMLDirectedEdge test = ix.next();
            if (test.equals(start)) {
                // Found!
                otherStart = test;
                break;
            }
        }
        if (otherStart == null) {
            // Edge not found in other so can't be a duplicate
            return false;
        }
        // Check that edges are equivalent
        // Walk through the edge lists starting at the beginning for me and at the equivalent edge in other. When we reach the end of other go back to the start.
        while (ix.hasNext()) {
            GMLDirectedEdge a = it.next();
            GMLDirectedEdge b = ix.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        ix = otherEdges.iterator();
        while (it.hasNext()) {
            GMLDirectedEdge a = it.next();
            GMLDirectedEdge b = ix.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    /**
       Find out if this face is entirely inside another.
       @param other The face to check against.
       @return true iff this face is entirely inside other.
    */
    public boolean isEntirelyInside(GMLFace other) {
        Area a = getArea();
        Area b = other.getArea();
        Area intersection = new Area(a);
        intersection.intersect(b);
        return a.equals(intersection);
    }

    /**
       Find out if this face intersects another.
       @param other The face to check against.
       @return true iff this face intersects the other face.
    */
    public boolean intersects(GMLFace other) {
        Area a = getArea();
        Area b = other.getArea();
        Area intersection = new Area(a);
        intersection.intersect(b);
        return !intersection.isEmpty();
    }

    private GMLDirectedEdge findNewEdge(GMLNode from, Set<GMLEdge> candidates) {
        for (GMLEdge next : candidates) {
            if (next.getStart().equals(from)) {
                return new GMLDirectedEdge(next, true);
            }
            if (next.getEnd().equals(from)) {
                return new GMLDirectedEdge(next, false);
            }
        }
        return null;
    }

    private void computeOutline() {
        points.clear();
        Iterator<GMLDirectedEdge> it = edges.iterator();
        GMLDirectedEdge next = it.next();
        points.add(next.getStartNode().getCoordinates());
        points.add(next.getEndNode().getCoordinates());
        while (it.hasNext()) {
            next = it.next();
            points.add(next.getEndNode().getCoordinates());
        }
        area = null;
    }
}