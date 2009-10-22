package maps.gml;

import java.util.List;

public class GMLSpace extends GMLShape {
    public GMLSpace(long id) {
        super(id);
    }

    public GMLSpace(long id, List<GMLCoordinates> coordinates) {
        super(id, coordinates);
    }
}