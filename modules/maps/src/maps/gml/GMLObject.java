package maps.gml;

/**
   A GML map object.
*/
public abstract class GMLObject {
    private long id;

    /**
       Construct a GML object.
       @param id The id of the object.
     */
    protected GMLObject(long id) {
        this.id = id;
    }

    /**
       Get this object's ID.
       @return The object ID.
    */
    public long getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GMLObject) {
            return this.id == ((GMLObject)o).id;
        }
        return false;
    }
}