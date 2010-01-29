package maps.gml;

import java.util.List;

/**
   An open space in GML space.
*/
public class GMLSpace extends GMLShape {
    /**
       Construct a GMLSpace.
       @param id The ID of the space.
    */
    public GMLSpace(long id) {
        super(id);
    }

    /**
       Construct a GMLSpace.
       @param id The ID of the space.
       @param coordinates The coordinates of the apexes of the space.
    */
    public GMLSpace(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}