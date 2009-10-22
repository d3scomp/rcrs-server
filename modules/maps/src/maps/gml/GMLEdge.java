package maps.gml;

import java.util.List;
import java.util.ArrayList;

/**
   A GML edge. An edge is a line (not necessarily straight) between two nodes.
 */
public class GMLEdge extends GMLObject {
    private GMLNode start;
    private GMLNode end;
    private List<GMLCoordinates> points;
    private boolean passable;

    /**
       Construct a new GMLEdge.
       @param id The ID of this object.
       @param start The start node.
       @param end The end node.
       @param passable True if this directed edge is passable.
     */
    public GMLEdge(long id, GMLNode start, GMLNode end, boolean passable) {
        this(id, start, end, passable, null);
    }

    /**
       Construct a new GMLEdge.
       @param id The ID of this object.
       @param start The start node.
       @param end The end node.
       @param passable True if this directed edge is passable.
       @param points The points along the line of the edge. If non-null, this must include the start and end points. If null or empty then the start and end point coordinates will be used.
     */
    public GMLEdge(long id, GMLNode start, GMLNode end, boolean passable, List<GMLCoordinates> points) {
        super(id);
        this.start = start;
        this.end = end;
        this.passable = passable;
        if (points == null || points.isEmpty()) {
            this.points = new ArrayList<GMLCoordinates>();
            this.points.add(start.getCoordinates());
            this.points.add(end.getCoordinates());
        }
        else {
            this.points = points;
        }
    }

    /**
       Get the points along the edge.
       @return The coordinates along the edge.
     */
    public List<GMLCoordinates> getPoints() {
        return points;
    }

    /**
       Get the start node.
       @return The start node.
    */
    public GMLNode getStart() {
        return start;
    }

    /**
       Get the end node.
       @return The end node.
    */
    public GMLNode getEnd() {
        return end;
    }

    /**
       Find out if this edge is passable.
       @return True iff the edge is passable.
    */
    public boolean isPassable() {
        return passable;
    }

    /**
       Set the passable flag on this edge.
       @param b The new passable flag.
     */
    public void setPassable(boolean b) {
        passable = b;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("GMLEdge from ");
        result.append(start);
        result.append(" to ");
        result.append(end);
        if (!passable) {
            result.append(" (impassable)");
        }
        return result.toString();
    }
}