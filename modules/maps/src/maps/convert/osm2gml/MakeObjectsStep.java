package maps.convert.osm2gml;

import maps.gml.GMLMap;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.convert.ConvertStep;

/**
   This step creates the final GML objects.
*/
public class MakeObjectsStep extends ConvertStep {
    private TemporaryMap map;
    private GMLMap gmlMap;

    /**
       Construct a MakeObjectsStep.
       @param map The TemporaryMap to read.
       @param gmlMap The GMLMap to populate.
    */
    public MakeObjectsStep(TemporaryMap map, GMLMap gmlMap) {
        super();
        this.map = map;
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Generating GML objects";
    }

    @Override
    protected void step() {
        setProgressLimit(map.getAllObjects().size());
        long nextID = 1;
        for (TemporaryBuilding b : map.getBuildings()) {
            gmlMap.addBuilding(new GMLBuilding(nextID++, b.makeGMLCoordinates()));
            bumpProgress();
        }
        for (TemporaryRoad r : map.getRoads()) {
            gmlMap.addRoad(new GMLRoad(nextID++, r.makeGMLCoordinates()));
            bumpProgress();
        }
        for (TemporaryIntersection i : map.getIntersections()) {
            gmlMap.addRoad(new GMLRoad(nextID++, i.makeGMLCoordinates()));
            bumpProgress();
        }
        setStatus("Created " + gmlMap.getRoads().size() + " roads and " + gmlMap.getBuildings().size() + " buildings");
    }
}