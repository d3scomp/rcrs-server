package maps.gml;

import java.util.List;

public class GMLBuilding extends GMLShape {
    public GMLBuilding(long id) {
        super(id);
    }

    public GMLBuilding(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}