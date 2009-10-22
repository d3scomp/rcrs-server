package osm2gml.buildings;

import osm2gml.gml.GMLFace;
import osm2gml.gml.GMLMap;

/**
   Interface for objects that know how to fill spaces with buildings.
*/
public interface BuildingSpaceFiller {
    /**
       Populate a space with buildings.
       @param space The space to fill.
       @param map The GMLMap to populate.
     */
    void createBuildings(GMLFace space, GMLMap map);
}