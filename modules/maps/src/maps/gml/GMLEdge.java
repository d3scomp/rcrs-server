package maps.gml;

/**
   A GML edge. An edge is a line between two nodes.
 */
public class GMLEdge extends GMLObject {
    private GMLNode start;
    private GMLNode end;
    private boolean passable;

    /**
       Construct a new GMLEdge.
       @param id The ID of this object.
       @param start The start node.
       @param end The end node.
       @param passable True if this directed edge is passable.
     */
    public GMLEdge(int id, GMLNode start, GMLNode end, boolean passable) {
        super(id);
        this.start = start;
        this.end = end;
        this.passable = passable;
    }

    /**
       Get the points along the edge.
       @return The coordinates along the edge.
     */
    //    public List<GMLCoordinates> getPoints() {
    //        return points;
    //    }

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