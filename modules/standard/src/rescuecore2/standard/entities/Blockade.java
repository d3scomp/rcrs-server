package rescuecore2.standard.entities;

import java.util.List;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;


/**
   The Area object.
 */
public class Blockade extends StandardEntity {

    private IntProperty center_x;
    private IntProperty center_y;
    private EntityRefProperty area;
    private IntArrayProperty shape;
    private IntProperty repair_cost;

    /**
       Construct a Area object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Blockade(EntityID id) {
	this(id, StandardEntityType.BLOCKADE);
    }
    
    public Blockade(EntityID id, StandardEntityType type) {
        super(id, type);
	center_x = new IntProperty(StandardPropertyType.X);
	center_y = new IntProperty(StandardPropertyType.Y);
	area = new EntityRefProperty(StandardPropertyType.AREA);
	shape = new IntArrayProperty(StandardPropertyType.AREA_APEXES);
      	repair_cost = new IntProperty(StandardPropertyType.REPAIR_COST);
        addProperties(center_x, center_y, shape, area, repair_cost);
    }
    
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
	return new Pair<Integer, Integer>(center_x.getValue(), center_y.getValue());
    }
    
    @Override
    protected Entity copyImpl() {
        return new Area(getID());
    }

    /**
       Get the value of the center_x kind property.
       @return The center_x kind.
     */
    public int getCenterX() {
        return center_x.getValue();
    }

    public void setCenter(int x, int y) {
	setCenterX(x);
	setCenterY(y);
    }

    /**
       Set the value of the center_x kind property.
       @param newKind The new center_x kind.
    */
    public void setCenterX(int newKind) {
        this.center_x.setValue(newKind);
    }

    public int getCenterY() {
        return center_y.getValue();
    }
    public void setCenterY(int newKind) {
        this.center_y.setValue(newKind);
    }

    public EntityID getArea() {
	return area.getValue();
    }
    public void setArea(EntityID id) {
	area.setValue(id);
    }

    public int[] getShape() {
        return shape.getValue();
    }
    public void setShape(int[] newShape) {
        this.shape.setValue(newShape);
    }

}