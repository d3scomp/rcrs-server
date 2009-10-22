package osm2gml.osm;

import java.util.List;

public class OSMBuilding extends OSMWay {
    public OSMBuilding(Long id, List<Long> ids) {
        super(id, ids);
    }

    @Override
    public String toString() {
        return "OSMBuilding: id " + getID();
    }
}