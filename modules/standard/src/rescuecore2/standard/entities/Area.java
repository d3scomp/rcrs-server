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
import java.util.Set;

/**
   The Area object.
 */
public abstract class Area extends StandardEntity {
    private IntProperty x;
    private IntProperty y;
    private IntArrayProperty apexes;
    private EntityRefListProperty neighbours;
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
        apexes = new IntArrayProperty(StandardPropertyURN.APEXES);
	neighbours = new EntityRefListProperty(StandardPropertyURN.NEIGHBOURS);
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
        apexes = new IntArrayProperty(other.apexes);
	neighbours = new EntityRefListProperty(other.neighbours);
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
        case APEXES:
            return apexes;
        case NEIGHBOURS:
            return neighbours;
        case BLOCKADES:
            return blockades;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Set<Property> getProperties() {
        Set<Property> result = super.getProperties();
        result.add(x);
        result.add(y);
        result.add(apexes);
        result.add(neighbours);
        result.add(blockades);
        return result;
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
       Get the apexes property.
       @return The apexes property.
     */
    public IntArrayProperty getApexesProperty() {
        return apexes;
    }

    /**
       Get the apexes of this area.
       @return The apexes.
     */
    public int[] getApexes() {
        return apexes.getValue();
    }

    /**
       Set the apexes.
       @param apexes The new apexes.
    */
    public void setApexes(int[] apexes) {
        this.apexes.setValue(apexes);
    }

    /**
       Find out if the apexes property has been defined.
       @return True if the apexes property has been defined, false otherwise.
     */
    public boolean isApexesDefined() {
        return apexes.isDefined();
    }

    /**
       Undefine the apexes property.
    */
    public void undefineApexes() {
        apexes.undefine();
    }

    /**
       Get the neighbours property.
       @return The neighbours property.
     */
    public EntityRefListProperty getNeighboursProperty() {
        return neighbours;
    }

    /**
       Get the neighbours of this area.
       @return The neighbours.
     */
    public List<EntityID> getNeighbours() {
        return neighbours.getValue();
    }

    /**
       Set the neighbours of this area.
       @param neighbours The new neighbours.
    */
    public void setNeighbours(List<EntityID> neighbours) {
        this.neighbours.setValue(neighbours);
    }

    /**
       Find out if the neighbours property has been defined.
       @return True if the neighbours property has been defined, false otherwise.
     */
    public boolean isNeighboursDefined() {
        return neighbours.isDefined();
    }

    /**
       Undefine the neighbours property.
    */
    public void undefineNeighbours() {
        neighbours.undefine();
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
       Set the center of this area.
       @param x The x coordinate of the center.
       @param y The y coordinate of the center.
    */
    /*
    public void setCenter(int x, int y) {
	setX(x);
	setY(y);
    }
    */
}