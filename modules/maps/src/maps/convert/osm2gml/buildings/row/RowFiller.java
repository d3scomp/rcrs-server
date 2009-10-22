package maps.convert.osm2gml.buildings.row;

import java.util.Set;

import maps.gml.GMLFace;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLMap;

public interface RowFiller {
    public Set<GMLFace> fillRow(GMLDirectedEdge edge, GMLMap map);
}