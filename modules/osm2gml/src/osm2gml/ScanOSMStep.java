package osm2gml;

import osm2gml.osm.OSMMap;
import osm2gml.osm.OSMNode;
import osm2gml.osm.OSMRoad;
import osm2gml.osm.OSMBuilding;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Color;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

public class ScanOSMStep extends ConvertStep {
    private OSMMap map;
    private Map<OSMNode, IntersectionInfo> nodeToIntersection;
    private List<IntersectionInfo> intersections;
    private List<RoadInfo> roads;
    private List<BuildingInfo> buildings;

    /**
       Construct a ScanOSMStep.
       @param map The OSMMap to scan.
    */
    public ScanOSMStep(OSMMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Scanning OpenStreetMap data";
    }

    @Override
    protected void step() {
        nodeToIntersection = new HashMap<OSMNode, IntersectionInfo>();
        intersections = new ArrayList<IntersectionInfo>();
        roads = new ArrayList<RoadInfo>();
        buildings = new ArrayList<BuildingInfo>();
        setProgressLimit(map.getRoads().size() + map.getBuildings().size());
        setStatus("Scanning roads and buildings");
        scanRoads();
        scanBuildings();
        double sizeOf1m = ConvertTools.sizeOf1Metre(map);
        setStatus("Generating intersections");
        setProgressLimit(intersections.size());
        setProgress(0);
        for (IntersectionInfo next : intersections) {
            next.process(sizeOf1m);
            bumpProgress();
        }
        setStatus("Created " + roads.size() + " roads, " + intersections.size() + " intersections, " + buildings.size() + " buildings");
    }

    public List<IntersectionInfo> getIntersections() {
        return intersections;
    }

    public List<RoadInfo> getRoads() {
        return roads;
    }

    public List<BuildingInfo> getBuildings() {
        return buildings;
    }

    public OSMMap getOSMMap() {
        return map;
    }

    private void scanRoads() {
        for (OSMRoad road : map.getRoads()) {
            Iterator<Long> it = road.getNodeIDs().iterator();
            OSMNode start = map.getNode(it.next());
            while (it.hasNext()) {
                OSMNode end = map.getNode(it.next());
                if (start == end) {
                    System.out.println("Degenerate road: " + road.getID());
                    continue;
                }
                IntersectionInfo from = nodeToIntersection.get(start);
                IntersectionInfo to = nodeToIntersection.get(end);
                if (from == null) {
                    from = new IntersectionInfo(start);
                    nodeToIntersection.put(start, from);
                    intersections.add(from);
                }
                if (to == null) {
                    to = new IntersectionInfo(end);
                    nodeToIntersection.put(end, to);
                    intersections.add(to);
                }
                RoadInfo roadInfo = new RoadInfo(start, end);
                from.addRoadSegment(roadInfo);
                to.addRoadSegment(roadInfo);
                start = end;
                roads.add(roadInfo);
            }
            bumpProgress();
        }
    }

    private void scanBuildings() {
        for (OSMBuilding building : map.getBuildings()) {
            buildings.add(new BuildingInfo(building, map));
            bumpProgress();
        }
    }
}