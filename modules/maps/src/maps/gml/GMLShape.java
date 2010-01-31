package maps.gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.awt.geom.Rectangle2D;

/**
   Abstract base class for shapes in GML space.
*/
public abstract class GMLShape extends GMLObject {
    private List<GMLDirectedEdge> edges;
    private Map<GMLDirectedEdge, Integer> neighbours;
    private Rectangle2D bounds;

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
    */
    protected GMLShape(int id) {
        super(id);
        this.edges = new ArrayList<GMLDirectedEdge>();
        neighbours = new HashMap<GMLDirectedEdge, Integer>();
    }

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
       @param edges The edges of the shape.
    */
    protected GMLShape(int id, List<GMLDirectedEdge> edges) {
        this(id);
        this.edges.addAll(edges);
        bounds = GMLTools.getBounds(getCoordinates());
    }

    /**
       Construct a GMLShape.
       @param id The ID of the shape.
       @param edges The edges of the shape.
       @param neighbours The neighbours of each edge.
    */
    protected GMLShape(int id, List<GMLDirectedEdge> edges, List<Integer> neighbours) {
        this(id, edges);
        Iterator<GMLDirectedEdge> it = edges.iterator();
        Iterator<Integer> ix = neighbours.iterator();
        while (it.hasNext() && ix.hasNext()) {
            setNeighbour(it.next(), ix.next());
        }
    }

    /**
       Get the edges of this shape.
       @return The edges.
    */
    public List<GMLDirectedEdge> getEdges() {
        return new ArrayList<GMLDirectedEdge>(edges);
    }

    /**
       Get the ID of the neighbour through a particular edge.
       @param edge The edge to look up.
       @return The ID of the neighbour through that edge or null.
    */
    public Integer getNeighbour(GMLDirectedEdge edge) {
        return neighbours.get(edge);
    }

    /**
       Set the ID of the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour ID for that edge. This may be null.
    */
    public void setNeighbour(GMLDirectedEdge edge, Integer neighbour) {
        if (neighbour == null) {
            neighbours.remove(edge);
        }
        else {
            neighbours.put(edge, neighbour);
        }
    }

    /**
       Find out if an edge has a neighbour.
       @param edge The edge to look up.
       @return True if there is a neighbour through that edge or false otherwise.
    */
    public boolean hasNeighbour(GMLDirectedEdge edge) {
        return neighbours.containsKey(edge);
    }

    /**
       Get the ID of the neighbour through a particular edge.
       @param edge The edge to look up.
       @return The ID of the neighbour through that edge or null.
    */
    public Integer getNeighbour(GMLEdge edge) {
        return getNeighbour(findDirectedEdge(edge));
    }

    /**
       Set the ID of the neighbour through a particular edge.
       @param edge The edge to set the neighbour of.
       @param neighbour The new neighbour ID for that edge. This may be null.
    */
    public void setNeighbour(GMLEdge edge, Integer neighbour) {
        setNeighbour(findDirectedEdge(edge), neighbour);
    }

    /**
       Find out if an edge has a neighbour.
       @param edge The edge to look up.
       @return True if there is a neighbour through that edge or false otherwise.
    */
    public boolean hasNeighbour(GMLEdge edge) {
        return neighbours.containsKey(findDirectedEdge(edge));
    }

    /**
       Get the coordinates of the apexes of this shape.
       @return The apex coordinates.
    */
    public List<GMLCoordinates> getCoordinates() {
        List<GMLCoordinates> result = new ArrayList<GMLCoordinates>();
        for (GMLDirectedEdge next : edges) {
            result.add(next.getStartCoordinates());
        }
        return result;
    }

    /**
       Get the x coordinate of the centre of this shape.
       @return The x coordinate of the centre.
    */
    public double getCentreX() {
        return bounds.getX() + (bounds.getWidth() / 2.0);
    }

    /**
       Get the y coordinate of the centre of this shape.
       @return The y coordinate of the centre.
    */
    public double getCentreY() {
        return bounds.getY() + (bounds.getHeight() / 2.0);
    }

    private GMLDirectedEdge findDirectedEdge(GMLEdge e) {
        for (GMLDirectedEdge next : edges) {
            if (next.getEdge().equals(e)) {
                return next;
            }
        }
        throw new IllegalArgumentException("Edge " + e + " not found");
    }
}