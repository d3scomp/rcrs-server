package maps.gml;

import java.util.List;

/**
   A building in GML space.
*/
public class GMLBuilding extends GMLShape {
    /**
       Construct a GMLBuilding.
       @param id The ID of the building.
    */
    public GMLBuilding(long id) {
        super(id);
    }

    /**
       Construct a GMLBuilding.
       @param id The ID of the building.
       @param coordinates The coordinates of the apexes of the building.
    */
    public GMLBuilding(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}