package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.misc.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

/**
   The Building object.
 */
public class Building extends Area {
    /**
       Enum defining different levels of fieryness.
     */
    public enum Fieryness {
        /** Not burnt at all. */
        UNBURNT,
        /** On fire a bit. */
        HEATING,
        /** On fire a bit more. */
        BURNING,
        /** On fire a lot. */
        INFERNO,
        /** Not burnt at all, but has water damage. */
        WATER_DAMAGE,
        /** Extinguished but minor damage. */
        MINOR_DAMAGE,
        /** Extinguished but moderate damage. */
        MODERATE_DAMAGE,
        /** Extinguished but major damage. */
        SEVERE_DAMAGE,
        /** Completely burnt out. */
        BURNT_OUT;
    }

    /**
       Fieryness levels that indicate burning. */
    public static final EnumSet<Fieryness> BURNING = EnumSet.of(Fieryness.HEATING, Fieryness.BURNING, Fieryness.INFERNO);

    private IntProperty floors;
    private BooleanProperty ignition;
    private IntProperty fieryness;
    private IntProperty brokenness;
    private IntProperty code;
    private IntProperty attributes;
    private IntProperty groundArea;
    private IntProperty totalArea;
    private IntProperty temperature;
    private IntProperty importance;

    /**
       Construct a Building object with entirely undefined property values.
       @param id The ID of this entity.
    */
    public Building(EntityID id) {
        this(id, StandardEntityURN.BUILDING);
    }

    /**
       Construct a subclass of a Building object with entirely undefined property values.
       @param id The ID of this entity.
       @param urn The real urn of this building.
    */
<<<<<<< HEAD
    protected Building(EntityID id, StandardEntityURN urn) {
        super(id, urn);
        x = new IntProperty(StandardPropertyURN.X);
        y = new IntProperty(StandardPropertyURN.Y);
        floors = new IntProperty(StandardPropertyURN.FLOORS);
        ignition = new BooleanProperty(StandardPropertyURN.IGNITION);
        fieryness = new IntProperty(StandardPropertyURN.FIERYNESS);
        brokenness = new IntProperty(StandardPropertyURN.BROKENNESS);
        code = new IntProperty(StandardPropertyURN.BUILDING_CODE);
        attributes = new IntProperty(StandardPropertyURN.BUILDING_ATTRIBUTES);
        groundArea = new IntProperty(StandardPropertyURN.BUILDING_AREA_GROUND);
        totalArea = new IntProperty(StandardPropertyURN.BUILDING_AREA_TOTAL);
        temperature = new IntProperty(StandardPropertyURN.TEMPERATURE);
        importance = new IntProperty(StandardPropertyURN.IMPORTANCE);
        apexes = new IntArrayProperty(StandardPropertyURN.BUILDING_APEXES);
        entrances = new EntityRefListProperty(StandardPropertyURN.ENTRANCES);
    }

    /**
       Building copy constructor.
       @param other The Building to copy.
     */
    public Building(Building other) {
        super(other);
        floors = new IntProperty(other.floors);
        ignition = new BooleanProperty(other.ignition);
        fieryness = new IntProperty(other.fieryness);
        brokenness = new IntProperty(other.brokenness);
        code = new IntProperty(other.code);
        attributes = new IntProperty(other.attributes);
        groundArea = new IntProperty(other.groundArea);
        totalArea = new IntProperty(other.totalArea);
        temperature = new IntProperty(other.temperature);
        importance = new IntProperty(other.importance);
        apexes = new IntArrayProperty(other.apexes);
        entrances = new EntityRefListProperty(other.entrances);
    }

    @Override
    protected Entity copyImpl() {
        return new Building(getID());
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
        case FLOORS:
            return floors;
        case IGNITION:
            return ignition;
        case FIERYNESS:
            return fieryness;
        case BROKENNESS:
            return brokenness;
        case BUILDING_CODE:
            return code;
        case BUILDING_ATTRIBUTES:
            return attributes;
        case BUILDING_AREA_GROUND:
            return groundArea;
        case BUILDING_AREA_TOTAL:
            return totalArea;
        case TEMPERATURE:
            return temperature;
        case IMPORTANCE:
            return importance;
        case BUILDING_APEXES:
            return apexes;
        case ENTRANCES:
            return entrances;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Set<Property> getProperties() {
        Set<Property> result = super.getProperties();
        result.add(x);
        result.add(y);
        result.add(floors);
        result.add(ignition);
        result.add(fieryness);
        result.add(brokenness);
        result.add(code);
        result.add(attributes);
        result.add(groundArea);
        result.add(totalArea);
        result.add(temperature);
        result.add(importance);
        result.add(apexes);
        result.add(entrances);
        return result;
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
        return new Pair<Integer, Integer>(getCenterX(), getCenterY());
    }

    /**
       Get the floors property.
       @return The floors property.
     */
    public IntProperty getFloorsProperty() {
        return floors;
    }

    /**
       Get the number of floors in this building.
       @return The number of floors.
     */
    public int getFloors() {
        return floors.getValue();
    }

    /**
       Set the number of floors in this building.
       @param floors The new number of floors.
    */
    public void setFloors(int floors) {
        this.floors.setValue(floors);
    }

    /**
       Find out if the floors property has been defined.
       @return True if the floors property has been defined, false otherwise.
     */
    public boolean isFloorsDefined() {
        return floors.isDefined();
    }

    /**
       Undefine the floors property.
    */
    public void undefineFloors() {
        floors.undefine();
    }

    /**
       Get the ignition property.
       @return The ignition property.
     */
    public BooleanProperty getIgnitionProperty() {
        return ignition;
    }

    /**
       Get the value of the ignition property.
       @return The ignition property value.
     */
    public boolean getIgnition() {
        return ignition.getValue();
    }

    /**
       Set the ignition property.
       @param ignition The new ignition value.
    */
    public void setIgnition(boolean ignition) {
        this.ignition.setValue(ignition);
    }

    /**
       Find out if the ignition property has been defined.
       @return True if the ignition property has been defined, false otherwise.
     */
    public boolean isIgnitionDefined() {
        return ignition.isDefined();
    }

    /**
       Undefine the ingition property.
    */
    public void undefineIgnition() {
        ignition.undefine();
    }

    /**
       Get the fieryness property.
       @return The fieryness property.
     */
    public IntProperty getFierynessProperty() {
        return fieryness;
    }

    /**
       Get the fieryness of this building.
       @return The fieryness property value.
     */
    public int getFieryness() {
        return fieryness.getValue();
    }

    /**
       Get the fieryness of this building as an enum constant. If fieryness is not defined then return null.
       @return The fieryness property value as a Fieryness enum, or null if fieryness is undefined.
     */
    public Fieryness getFierynessEnum() {
        if (!fieryness.isDefined()) {
            return null;
        }
        return Fieryness.values()[fieryness.getValue()];
    }

    /**
       Set the fieryness of this building.
       @param fieryness The new fieryness value.
    */
    public void setFieryness(int fieryness) {
        this.fieryness.setValue(fieryness);
    }

    /**
       Find out if the fieryness property has been defined.
       @return True if the fieryness property has been defined, false otherwise.
     */
    public boolean isFierynessDefined() {
        return fieryness.isDefined();
    }

    /**
       Undefine the fieryness property.
    */
    public void undefineFieryness() {
        fieryness.undefine();
    }

    /**
       Get the brokenness property.
       @return The brokenness property.
     */
    public IntProperty getBrokennessProperty() {
        return brokenness;
    }

    /**
       Get the brokenness of this building.
       @return The brokenness value.
     */
    public int getBrokenness() {
        return brokenness.getValue();
    }

    /**
       Set the brokenness of this building.
       @param brokenness The new brokenness.
    */
    public void setBrokenness(int brokenness) {
        this.brokenness.setValue(brokenness);
    }

    /**
       Find out if the brokenness property has been defined.
       @return True if the brokenness property has been defined, false otherwise.
     */
    public boolean isBrokennessDefined() {
        return brokenness.isDefined();
    }

    /**
       Undefine the brokenness property.
    */
    public void undefineBrokenness() {
        brokenness.undefine();
    }

    /**
       Get the building code property.
       @return The building code property.
     */
    public IntProperty getBuildingCodeProperty() {
        return code;
    }

    /**
       Get the building code of this building.
       @return The building code.
     */
    public int getBuildingCode() {
        return code.getValue();
    }

    /**
       Set the building code of this building.
       @param newCode The new building code.
    */
    public void setBuildingCode(int newCode) {
        this.code.setValue(newCode);
    }

    /**
       Find out if the building code has been defined.
       @return True if the building code has been defined, false otherwise.
     */
    public boolean isBuildingCodeDefined() {
        return code.isDefined();
    }

    /**
       Undefine the building code.
    */
    public void undefineBuildingCode() {
        code.undefine();
    }

    /**
       Get the building attributes property.
       @return The building attributes property.
     */
    public IntProperty getBuildingAttributesProperty() {
        return attributes;
    }

    /**
       Get the building attributes of this building.
       @return The building attributes.
     */
    public int getBuildingAttributes() {
        return attributes.getValue();
    }

    /**
       Set the building attributes of this building.
       @param newAttributes The new building attributes.
    */
    public void setBuildingAttributes(int newAttributes) {
        this.attributes.setValue(newAttributes);
    }

    /**
       Find out if the building attributes property has been defined.
       @return True if the building attributes property has been defined, false otherwise.
     */
    public boolean isBuildingAttributesDefined() {
        return attributes.isDefined();
    }

    /**
       Undefine the building attributes.
    */
    public void undefineBuildingAttributes() {
        attributes.undefine();
    }

    /**
       Get the ground area property.
       @return The ground area property.
     */
    public IntProperty getGroundAreaProperty() {
        return groundArea;
    }

    /**
       Get the ground area of this building.
       @return The ground area.
     */
    public int getGroundArea() {
        return groundArea.getValue();
    }

    /**
       Set the ground area of this building.
       @param ground The new ground area.
    */
    public void setGroundArea(int ground) {
        this.groundArea.setValue(ground);
    }

    /**
       Find out if the ground area property has been defined.
       @return True if the ground area property has been defined, false otherwise.
     */
    public boolean isGroundAreaDefined() {
        return groundArea.isDefined();
    }

    /**
       Undefine the ground area.
    */
    public void undefineGroundArea() {
        groundArea.undefine();
    }

    /**
       Get the total area property.
       @return The total area property.
     */
    public IntProperty getTotalAreaProperty() {
        return totalArea;
    }

    /**
       Get the total area of this building.
       @return The total area.
     */
    public int getTotalArea() {
        return totalArea.getValue();
    }

    /**
       Set the total area of this building.
       @param total The new total area.
    */
    public void setTotalArea(int total) {
        this.totalArea.setValue(total);
    }

    /**
       Find out if the total area property has been defined.
       @return True if the total area property has been defined, false otherwise.
     */
    public boolean isTotalAreaDefined() {
        return totalArea.isDefined();
    }

    /**
       Undefine the total area property.
    */
    public void undefineTotalArea() {
        totalArea.undefine();
    }

    /**
       Get the temperature property.
       @return The temperature property.
     */
    public IntProperty getTemperatureProperty() {
        return temperature;
    }

    /**
       Get the temperature of this building.
       @return The temperature.
     */
    public int getTemperature() {
        return temperature.getValue();
    }

    /**
       Set the temperature of this building.
       @param temperature The new temperature.
    */
    public void setTemperature(int temperature) {
        this.temperature.setValue(temperature);
    }

    /**
       Find out if the temperature property has been defined.
       @return True if the temperature property has been defined, false otherwise.
     */
    public boolean isTemperatureDefined() {
        return temperature.isDefined();
    }

    /**
       Undefine the temperature property.
    */
    public void undefineTemperature() {
        temperature.undefine();
    }

    /**
       Get the building importance property.
       @return The importance property.
     */
    public IntProperty getImportanceProperty() {
        return importance;
    }

    /**
       Get the importance of this building.
       @return The importance.
     */
    public int getImportance() {
        return importance.getValue();
    }

    /**
       Set the importance of this building.
       @param importance The new importance.
    */
    public void setImportance(int importance) {
        this.importance.setValue(importance);
    }

    /**
       Find out if the importance property has been defined.
       @return True if the importance property has been defined, false otherwise.
     */
    public boolean isImportanceDefined() {
        return importance.isDefined();
    }

    /**
       Undefine the importance property.
    */
    public void undefineImportance() {
        importance.undefine();
    }


    /**
       Get the apexes as a list of coordinates.
       @return A list of coordinates.
     */
    public List<Pair<Integer, Integer>> getApexesAsList() {
	throw new RuntimeException("not supported");
    }

    /**
       Find out if this building is on fire.
       @return True iff this buildings fieryness indicates that it is burning.
    */
    public boolean isOnFire() {
        if (!fieryness.isDefined()) {
            return false;
        }
        return BURNING.contains(getFierynessEnum());
    }
}