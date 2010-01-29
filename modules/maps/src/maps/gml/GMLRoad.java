package maps.gml;

import java.util.List;

/**
   A road in GML space.
*/
public class GMLRoad extends GMLShape {
    /**
       Construct a GMLRoad.
       @param id The ID of the road.
    */
    public GMLRoad(long id) {
        super(id);
    }

    /**
       Construct a GMLRoad.
       @param id The ID of the road.
       @param coordinates The coordinates of the apexes of the road.
    */
    public GMLRoad(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}