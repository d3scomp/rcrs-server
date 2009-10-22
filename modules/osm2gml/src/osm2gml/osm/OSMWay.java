package osm2gml.osm;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public abstract class OSMWay extends OSMObject {
    private List<Long> ids;

    public OSMWay(Long id, List<Long> ids) {
        super(id);
        this.ids = ids;
    }

    public List<Long> getNodeIDs() {
        return new ArrayList<Long>(ids);
    }

    public void setNodeIDs(List<Long> newIDs) {
        ids = newIDs;
    }

    public void replace(Long oldID, Long newID) {
        Collections.replaceAll(ids, oldID, newID);
    }
}