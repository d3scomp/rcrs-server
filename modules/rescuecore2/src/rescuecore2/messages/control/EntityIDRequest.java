package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;

import java.io.InputStream;
import java.io.IOException;

/**
   A message from a simulator requesting a new EntityID.
*/
public class EntityIDRequest extends AbstractMessage implements Control {
    private IntComponent count;

    /**
       Construct an EntityIDRequest message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public EntityIDRequest(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct an EntityIDRequest message.
       @param number The number of IDs requested.
     */
    public EntityIDRequest(int number) {
        this();
        this.count.setValue(number);
    }

    private EntityIDRequest() {
        super(ControlMessageURN.ENTITY_ID_REQUEST);
        count = new IntComponent("Number of IDs");
        addMessageComponent(count);
    }

    /**
       Get the number of IDs requested.
       @return The number of IDs requested.
    */
    public int getCount() {
        return count.getValue();
    }
}