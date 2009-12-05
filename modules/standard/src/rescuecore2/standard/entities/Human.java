package rescuecore2.standard.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.misc.Pair;

import java.util.List;
import java.util.Set;

/**
   Abstract base class for Humans.
 */
public abstract class Human extends StandardEntity {

    private IntProperty x;
    private IntProperty y;
    private EntityRefProperty position;
    private EntityRefListProperty positionHistory;
    private IntProperty travelDistance;
    private IntProperty direction;
    private IntProperty stamina;
    private IntProperty hp;
    private IntProperty damage;
    private IntProperty buriedness;

    /**
       Construct a subclass of a Human object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
    */
    protected Human(EntityID id, StandardEntityURN type) {
        super(id, type);
        x = new IntProperty(StandardPropertyURN.X);
        y = new IntProperty(StandardPropertyURN.Y);
        travelDistance = new IntProperty(StandardPropertyURN.TRAVEL_DISTANCE);
        position = new EntityRefProperty(StandardPropertyURN.POSITION);
        positionExtra = new IntProperty(StandardPropertyURN.POSITION_EXTRA);
        positionHistory = new EntityRefListProperty(StandardPropertyURN.POSITION_HISTORY);
        direction = new IntProperty(StandardPropertyURN.DIRECTION);
        stamina = new IntProperty(StandardPropertyURN.STAMINA);
        hp = new IntProperty(StandardPropertyURN.HP);
        damage = new IntProperty(StandardPropertyURN.DAMAGE);
        buriedness = new IntProperty(StandardPropertyURN.BURIEDNESS);
    }

    /**
       Human copy constructor.
       @param other The Human to copy.
     */
    public Human(Human other) {
        super(other);
        position = new EntityRefProperty(other.position);
        positionExtra = new IntProperty(other.positionExtra);
        positionHistory = new EntityRefListProperty(other.positionHistory);
        direction = new IntProperty(other.direction);
        stamina = new IntProperty(other.stamina);
        hp = new IntProperty(other.hp);
        damage = new IntProperty(other.damage);
        buriedness = new IntProperty(other.buriedness);
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
        case POSITION:
            return position;
        case POSITION_EXTRA:
            return positionExtra;
        case POSITION_HISTORY:
            return positionHistory;
        case DIRECTION:
            return direction;
        case STAMINA:
            return stamina;
        case HP:
            return hp;
        case DAMAGE:
            return damage;
        case BURIEDNESS:
            return buriedness;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Set<Property> getProperties() {
        Set<Property> result = super.getProperties();
        result.add(position);
        result.add(positionExtra);
        result.add(positionHistory);
        result.add(direction);
        result.add(stamina);
        result.add(hp);
        result.add(damage);
        result.add(buriedness);
        return result;
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
        if (x.isDefined() && y.isDefined())
            return new Pair<Integer, Integer>(x.getValue(), y.getValue());        
        if(position.isDefined()) {
            StandardEntity positionEntity = world.getEntity(position.getValue());
            System.err.println(positionEntity);
            return positionEntity.getLocation(world);
        }
        return null;
    }
    
    /**
       Get the position property.
       @return The position property.
     */
    public EntityRefProperty getPositionProperty() {
        return position;
    }

    /**
       Get the position of this human.
       @return The position.
     */
    public EntityID getPosition() {
        return position.getValue();
    }


    /**
       Set the position of this human.
       @param position The new position.
    */
    public void setPosition(EntityID position) {
        this.position.setValue(position);
    }

    /**
       Set the position of this human.
       @param position The new position.
    */
    public void setPosition(EntityID position, int x, int y) {
        this.position.setValue(position);
        this.x.setValue(x);
        this.y.setValue(y);
    }

    /**
       Find out if the position property has been defined.
       @return True if the position property has been defined, false otherwise.
     */
    public boolean isPositionDefined() {
        return position.isDefined();
    }

    /**
       Undefine the position property.
    */
    public void undefinePosition() {
        position.undefine();
    }

    /**
       Get the position history property.
       @return The position history property.
     */
    public EntityRefListProperty getPositionHistoryProperty() {
        return positionHistory;
    }

    /**
       Get the position history.
       @return The position history.
     */
    public List<EntityID> getPositionHistory() {
        return positionHistory.getValue();
    }

    /**
       Set the position history.
       @param history The new position history.
    */
    public void setPositionHistory(List<EntityID> history) {
        this.positionHistory.setValue(history);
    }

    /**
       Find out if the position history property has been defined.
       @return True if the position history property has been defined, false otherwise.
     */
    public boolean isPositionHistoryDefined() {
        return positionHistory.isDefined();
    }

    /**
       Undefine the position history property.
    */
    public void undefinePositionHistory() {
        positionHistory.undefine();
    }

    /**
       Get the direction property.
       @return The direction property.
     */
    public IntProperty getDirectionProperty() {
        return direction;
    }

    /**
       Get the direction.
       @return The direction.
     */
    public int getDirection() {
        return direction.getValue();
    }

    /**
       Set the direction.
       @param direction The new direction.
    */
    public void setDirection(int direction) {
        this.direction.setValue(direction);
    }

    /**
       Find out if the direction property has been defined.
       @return True if the direction property has been defined, false otherwise.
     */
    public boolean isDirectionDefined() {
        return direction.isDefined();
    }

    /**
       Undefine the direction property.
    */
    public void undefineDirection() {
        direction.undefine();
    }

    /**
       Get the stamina property.
       @return The stamina property.
     */
    public IntProperty getStaminaProperty() {
        return stamina;
    }

    /**
       Get the stamina of this human.
       @return The stamina.
     */
    public int getStamina() {
        return stamina.getValue();
    }

    /**
       Set the stamina of this human.
       @param stamina The new stamina.
    */
    public void setStamina(int stamina) {
        this.stamina.setValue(stamina);
    }

    /**
       Find out if the stamina property has been defined.
       @return True if the stamina property has been defined, false otherwise.
     */
    public boolean isStaminaDefined() {
        return stamina.isDefined();
    }

    /**
       Undefine the stamina property.
    */
    public void undefineStamina() {
        stamina.undefine();
    }

    /**
       Get the hp property.
       @return The hp property.
     */
    public IntProperty getHPProperty() {
        return hp;
    }

    /**
       Get the hp of this human.
       @return The hp of this human.
     */
    public int getHP() {
        return hp.getValue();
    }

    /**
       Set the hp of this human.
       @param newHP The new hp.
    */
    public void setHP(int newHP) {
        this.hp.setValue(newHP);
    }

    /**
       Find out if the hp property has been defined.
       @return True if the hp property has been defined, false otherwise.
     */
    public boolean isHPDefined() {
        return hp.isDefined();
    }

    /**
       Undefine the hp property.
    */
    public void undefineHP() {
        hp.undefine();
    }

    /**
       Get the damage property.
       @return The damage property.
     */
    public IntProperty getDamageProperty() {
        return damage;
    }

    /**
       Get the damage of this human.
       @return The damage of this human.
     */
    public int getDamage() {
        return damage.getValue();
    }

    /**
       Set the damage of this human.
       @param damage The new damage.
    */
    public void setDamage(int damage) {
        this.damage.setValue(damage);
    }

    /**
       Find out if the damage property has been defined.
       @return True if the damage property has been defined, false otherwise.
     */
    public boolean isDamageDefined() {
        return damage.isDefined();
    }

    /**
       Undefine the damage property.
    */
    public void undefineDamage() {
        damage.undefine();
    }

    /**
       Get the buriedness property.
       @return The buriedness property.
     */
    public IntProperty getBuriednessProperty() {
        return buriedness;
    }

    /**
       Get the buriedness of this human.
       @return The buriedness of this human.
     */
    public int getBuriedness() {
        return buriedness.getValue();
    }

    /**
       Set the buriedness of this human.
       @param buriedness The new buriedness.
    */
    public void setBuriedness(int buriedness) {
        this.buriedness.setValue(buriedness);
    }

    /**
       Find out if the buriedness property has been defined.
       @return True if the buriedness property has been defined, false otherwise.
     */
    public boolean isBuriednessDefined() {
        return buriedness.isDefined();
    }

    /**
       Undefine the buriedness property.
    */
    public void undefineBuriedness() {
        buriedness.undefine();
    }

    /**
       Get the entity represented by the position property. The result will be null if the position property has not been set or if the entity reference is invalid.
       @param model The WorldModel to look up entity references.
       @return The entity represented by the position property.
     */
    public StandardEntity getPosition(WorldModel<? extends StandardEntity> model) {
        if (!position.isDefined()) {
            return null;
        }
        return model.getEntity(position.getValue());
    }
}
