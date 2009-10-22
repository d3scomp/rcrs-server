package osm2gml.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
   A GMLDirectedEdge is an edge with an orientation.
 */
public class GMLDirectedEdge {
    private GMLEdge edge;
    private boolean forward;

    /**
       Construct a directed GML edge.
       @param The underlying edge.
       @param forward True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public GMLDirectedEdge(GMLEdge edge, boolean forward) {
        this.edge = edge;
        this.forward = forward;
    }

    /**
       Construct a directed GML edge.
       @param The underlying edge.
       @param The start node.
     */
    public GMLDirectedEdge(GMLEdge edge, GMLNode start) {
        this.edge = edge;
        this.forward = start.equals(edge.getStart());
    }

    /**
       Get the underlying edge.
       @return The underlying edge.
     */
    public GMLEdge getEdge() {
        return edge;
    }

    /**
       Is this directed edge in the direction of the underlying edge?
       @return True if this directed edge is aligned with the underlying edge direction, false otherwise.
     */
    public boolean isForward() {
        return forward;
    }

    /**
       Get the node at the start of the underlying edge.
       @return The start node.
     */
    public GMLNode getStartNode() {
        return forward ? edge.getStart() : edge.getEnd();
    }

    /**
       Get the node at the end of the underlying edge.
       @return The end node.
     */
    public GMLNode getEndNode() {
        return forward ? edge.getEnd() : edge.getStart();
    }

    /**
       Get the points of the underlying edge in the right order for this directed edge.
       @return The points of the underlying edge in the right order.
    */
    public List<Coordinates> getPoints() {
        List<Coordinates> result = new ArrayList<Coordinates>(edge.getPoints());
        if (!forward) {
            Collections.reverse(result);
        }
        return result;
    }

    /**
       Get the coordinates of the start of this edge.
       @return The coordinates of the start of this edge.
     */
    public Coordinates getStartCoordinates() {
        if (forward) {
            return edge.getStart().getCoordinates();
        }
        else {
            return edge.getEnd().getCoordinates();
        }
    }

    /**
       Get the coordinates of the end of this edge.
       @return The coordinates of the end of this edge.
     */
    public Coordinates getEndCoordinates() {
        if (forward) {
            return edge.getEnd().getCoordinates();
        }
        else {
            return edge.getStart().getCoordinates();
        }
    }

    @Override
    public String toString() {
        return "GMLDirectedEdge" + (forward ? "" : " backwards") + " along " + edge;
    }

    @Override
    public int hashCode() {
        return edge.hashCode() ^ (forward ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GMLDirectedEdge) {
            GMLDirectedEdge e = (GMLDirectedEdge)o;
            return this.forward == e.forward && this.edge.equals(e.edge);
        }
        return false;
    }
}