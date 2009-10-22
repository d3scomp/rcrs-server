package maps.gml;

import java.util.List;

public class GMLRoad extends GMLShape {
    public GMLRoad(long id) {
        super(id);
    }

    public GMLRoad(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}