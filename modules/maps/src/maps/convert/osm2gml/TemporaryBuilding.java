package maps.convert.osm2gml;

import java.util.List;

public class TemporaryBuilding extends TemporaryObject {
    private long id;

    public TemporaryBuilding(List<DirectedEdge> edges, long id) {
        super(edges);
        this.id = id;
    }

    public long getBuildingID() {
        return id;
    }
}