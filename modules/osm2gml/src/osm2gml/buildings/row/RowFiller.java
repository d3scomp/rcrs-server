package osm2gml.buildings.row;

import java.util.Set;

import osm2gml.gml.GMLFace;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLMap;

public interface RowFiller {
    public Set<GMLFace> fillRow(GMLDirectedEdge edge, GMLMap map);
}