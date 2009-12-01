package rescuecore2.messages.control;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.EntityIDListComponent;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
   A message from a the kernel supplying a new EntityID.
*/
public class EntityIDResponse extends AbstractMessage implements Control {
    private EntityIDListComponent newID;

    /**
       Construct an EntityIDResponse message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public EntityIDResponse(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct an EntityIDResponse message.
       @param ids The new EntityIDs.
     */
    public EntityIDResponse(EntityID... ids) {
        this(Arrays.asList(ids));
    }

    /**
       Construct an EntityIDResponse message.
       @param ids The new EntityIDs.
     */
    public EntityIDResponse(List<EntityID> ids) {
        this();
        this.newID.setIDs(ids);
    }

    private EntityIDResponse() {
        super(ControlMessageURN.ENTITY_ID_RESPONSE);
        newID = new EntityIDListComponent("New entity IDs");
        addMessageComponent(newID);
    }

    /**
       Get the new entity IDs.
       @return The new entity IDs.
     */
    public List<EntityID> getEntityIDs() {
        return newID.getIDs();
    }
}