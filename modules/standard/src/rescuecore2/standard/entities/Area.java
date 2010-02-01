package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.Property;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
   The Area object.
 */
public abstract class Area extends StandardEntity {
    private IntProperty x;
    private IntProperty y;
    private EdgeListProperty edges;
    private EntityRefListProperty blockades;

    /**
       Construct a subclass of Area with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
     */
    protected Area(EntityID id, StandardEntityURN type) {
        super(id, type);
	x = new IntProperty(StandardPropertyURN.X);
	y = new IntProperty(StandardPropertyURN.Y);
        edges = new EdgeListProperty(StandardPropertyURN.EDGES);
	blockades = new EntityRefListProperty(StandardPropertyURN.BLOCKADES);
    }

    /**
       Area copy constructor.
       @param other The Area to copy.
     */
    protected Area(Area other) {
        super(other);
	x = new IntProperty(other.x);
	y = new IntProperty(other.y);
        edges = new EdgeListProperty(other.edges);
	blockades = new EntityRefListProperty(other.blockades);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
        return new Pair<Integer, Integer>(x.getValue(), y.getValue());
    }
    
    @Override
    public Property getProperty(String urn) {
        StandardPropertyURN type;
        try {
            type = StandardPropertyURN.valueOf(urn);
        }
        catch (IllegalArgumentException e) {
            return super.getProperty(urn);
        }
        switch (type) {
        case X:
            return x;
        case Y:
            return y;
        case EDGES:
            return edges;
        case BLOCKADES:
            return blockades;
        default:
            return super.getProperty(urn);
        }
    }

    /**
       Get the X property.
       @return The X property.
     */
    public IntProperty getXProperty() {
        return x;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
     */
    public int getX() {
        return x.getValue();
    }

    /**
       Set the X coordinate.
       @param x The new X coordinate.
    */
    public void setX(int x) {
        this.x.setValue(x);
    }

    /**
       Find out if the X property has been defined.
       @return True if the X property has been defined, false otherwise.
     */
    public boolean isXDefined() {
        return x.isDefined();
    }

    /**
       Undefine the X property.
    */
    public void undefineX() {
        x.undefine();
    }

    /**
       Get the Y property.
       @return The Y property.
     */
    public IntProperty getYProperty() {
        return y;
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
     */
    public int getY() {
        return y.getValue();
    }

    /**
       Set the Y coordinate.
       @param x The new y coordinate.
    */
    public void setY(int y) {
        this.y.setValue(y);
    }

    /**
       Find out if the Y property has been defined.
       @return True if the Y property has been defined, false otherwise.
     */
    public boolean isYDefined() {
        return y.isDefined();
    }

    /**
       Undefine the Y property.
    */
    public void undefineY() {
        y.undefine();
    }

    /**
       Get the edges property.
       @return The edges property.
     */
    public EdgeListProperty getEdgesProperty() {
        return edges;
    }

    /**
       Get the edges of this area.
       @return The edges.
     */
    public List<Edge> getEdges() {
        return edges.getValue();
    }

    /**
       Set the edges.
       @param edges The new edges.
    */
    public void setEdges(List<Edge> edges) {
        this.edges.setEdges(edges);
    }

    /**
       Find out if the edges property has been defined.
       @return True if the edges property has been defined, false otherwise.
     */
    public boolean isEdgesDefined() {
        return edges.isDefined();
    }

    /**
       Undefine the edges property.
    */
    public void undefineEdges() {
        edges.undefine();
    }

    /**
       Get the blockades property.
       @return The blockades property.
     */
    public EntityRefListProperty getBlockadesProperty() {
        return blockades;
    }

    /**
       Get the blockades in this area.
       @return The blockades.
     */
    public List<EntityID> getBlockades() {
        return blockades.getValue();
    }

    /**
       Set the blockades in this area.
       @param blockades The new blockades.
    */
    public void setBlockades(List<EntityID> blockades) {
        this.blockades.setValue(blockades);
    }

    /**
       Find out if the blockades property has been defined.
       @return True if the blockades property has been defined, false otherwise.
     */
    public boolean isBlockadesDefined() {
        return blockades.isDefined();
    }

    /**
       Undefine the blockades property.
    */
    public void undefineBlockades() {
        blockades.undefine();
    }

    /**
       Get the neighbours of this area.
       @return The neighbours.
     */
    public List<EntityID> getNeighbours() {
        List<EntityID> result = new ArrayList<EntityID>();
        for (Edge next : edges.getValue()) {
            if (next.isPassable()) {
                result.add(next.getNeighbour());
            }
        }
        return result;
    }

    /**
       Get the list of apexes for this area.
       @return The list of apexes.
    */
    public int[] getApexList() {
        List<Edge> edges = getEdges();
        int[] apexes = new int[edges.size() * 2];
        int i = 0;
        for (Edge next : edges) {
            apexes[i++] = next.getStartX();
            apexes[i++] = next.getStartY();
        }
        return apexes;
    }
}