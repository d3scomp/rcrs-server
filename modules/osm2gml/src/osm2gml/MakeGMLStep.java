package osm2gml;

import osm2gml.OSMShape;

import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLEdge;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLFace;
import osm2gml.gml.GMLFaceShapeInfo;
import osm2gml.gml.FaceType;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Color;

import rescuecore2.misc.geometry.Point2D;

public class MakeGMLStep extends ConvertStep {
    private ScanOSMStep osmStep;
    private GMLMap gmlMap;

    /**
       Construct a MakeGMLStep.
       @param osmStep The ScanOSMStep to take data from.
       @param gmlMap The GMLMap to populate.
    */
    public MakeGMLStep(ScanOSMStep osmStep, GMLMap gmlMap) {
        super();
        this.osmStep = osmStep;
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Generating GML objects";
    }

    @Override
    protected void step() {
        List<RoadInfo> roads = osmStep.getRoads();
        List<IntersectionInfo> intersections = osmStep.getIntersections();
        List<BuildingInfo> buildings = osmStep.getBuildings();
        setProgressLimit(roads.size() + intersections.size() + buildings.size());
        gmlMap.setNearbyNodeThreshold(ConvertTools.nearbyThreshold(osmStep.getOSMMap()));
        generateRoadGML(roads);
        generateIntersectionGML(intersections);
        generateBuildingGML(buildings);
        setStatus("Created " + gmlMap.getNodes().size() + " nodes, " + gmlMap.getEdges().size() + " edges, " + gmlMap.getFaces().size() + " faces");
        //        debug.activate();
        //        debug.show("Initial GML shapes", ConvertTools.getAllGMLShapes(gmlMap));
        //        debug.deactivate();
    }

    private void generateRoadGML(List<RoadInfo> roads) {
        for (RoadInfo road : roads) {
            if (road.getArea() != null) {
                List<GMLDirectedEdge> edges = generateGMLFromShape(road);
                if (edges.size() > 2) {
                    GMLFace face = gmlMap.createFace(edges, FaceType.ROAD);
                }
            }
            bumpProgress();
        }
    }

    private void generateIntersectionGML(List<IntersectionInfo> intersections) {
        for (IntersectionInfo intersection : intersections) {
            if (intersection.getArea() != null) {
                List<GMLDirectedEdge> edges = generateGMLFromShape(intersection);
                if (edges.size() > 2) {
                    GMLFace face = gmlMap.createFace(edges, FaceType.INTERSECTION);
                }
            }
            bumpProgress();
        }
    }

    private void generateBuildingGML(List<BuildingInfo> buildings) {
        for (BuildingInfo building : buildings) {
            List<GMLDirectedEdge> edges = generateGMLFromShape(building);
            GMLFace face = gmlMap.createFace(edges, FaceType.BUILDING);
            face.setOriginalBuildingID(building.getBuildingID());
            bumpProgress();
        }
    }

    private List<GMLDirectedEdge> generateGMLFromShape(OSMShape s) {
        //        boolean verbose = (s instanceof IntersectionInfo) && ((IntersectionInfo)s).getCentre().getID() == 254435;
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>();
        Iterator<Point2D> it = s.getVertices().iterator();
        GMLNode first = gmlMap.ensureNodeNear(it.next());
        GMLNode previous = first;
        GMLDirectedEdge edge;
        /*
        if (verbose) {
            System.out.println("Creating intersection GML");
            System.out.println("Start: " + first);
        }
        */
        while (it.hasNext()) {
            GMLNode next = gmlMap.ensureNodeNear(it.next());
            if (previous.equals(next)) {
                continue;
            }
            edge = gmlMap.ensureDirectedEdge(previous, next);
            result.add(edge);
            previous = next;
            /*
            if (verbose) {
                System.out.println("Next: " + next);
            }
            */
        }
        // Close the shape if required
        if (previous != first) {
            edge = gmlMap.ensureDirectedEdge(previous, first);
            result.add(edge);
        }
        return result;
    }
}