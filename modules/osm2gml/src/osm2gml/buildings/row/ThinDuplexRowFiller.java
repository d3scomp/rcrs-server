package osm2gml.buildings.row;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import osm2gml.ConvertTools;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLEdge;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLFace;
import osm2gml.gml.GMLMap;
import osm2gml.gml.FaceType;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;

public class ThinDuplexRowFiller implements RowFiller {
    private final static double BUILDING_WIDTH_M = 10;
    private final static double BUILDING_DEPTH_M = 20;
    private final static double MIN_OFFSET_M = 2;
    private final static double MAX_OFFSET_M = 3;
    private final static int MIN_RUN_LENGTH = 1;
    private final static int MAX_RUN_LENGTH = 5;

    private final double BUILDING_WIDTH;
    private final double BUILDING_DEPTH;
    private final double MIN_OFFSET;
    private final double MAX_OFFSET;

    private final Random random;

    public ThinDuplexRowFiller(double sizeOf1m, Random random) {
        BUILDING_WIDTH = BUILDING_WIDTH_M * sizeOf1m;
        BUILDING_DEPTH = BUILDING_DEPTH_M * sizeOf1m;
        MIN_OFFSET = MIN_OFFSET_M * sizeOf1m;
        MAX_OFFSET = MAX_OFFSET_M * sizeOf1m;
        this.random = random;
    }

    @Override
    public Set<GMLFace> fillRow(GMLDirectedEdge edge, GMLMap map) {
        Set<GMLFace> result = new HashSet<GMLFace>();
        Line2D edgeLine = ConvertTools.gmlDirectedEdgeToLine(edge);
        Vector2D normal = edgeLine.getDirection().getNormal().normalised();
        // Create buildings along the edge until we run out of room
        double edgeLength = edgeLine.getDirection().getLength();
        double offset = getRandomOffset();
        int runLength = getRandomRunLength();
        double d = 0;
        while (d < 1) {
            if (runLength-- == 0) {
                offset = getRandomOffset();
                runLength = getRandomRunLength();
            }
            double d1 = d;
            double d2 = d + (BUILDING_WIDTH / edgeLength);
            Point2D topRight = edgeLine.getPoint(d1);
            Point2D topLeft = edgeLine.getPoint(d2);
            result.addAll(createBuildingInLot(edgeLine, topRight, topLeft, normal, offset, map));
            d = d2;
        }
        return result;
    }

    private Set<GMLFace> createBuildingInLot(Line2D edgeLine, Point2D topRight, Point2D topLeft, Vector2D edgeNormal, double depthOffset, GMLMap map) {
        // Offset from the top of the boundary
        topRight = topRight.plus(edgeNormal.scale(depthOffset));
        topLeft = topLeft.plus(edgeNormal.scale(depthOffset));
        // Find the other end of the building
        Point2D bottomRight = topRight.plus(edgeNormal.scale(BUILDING_DEPTH));
        Point2D bottomLeft = topLeft.plus(edgeNormal.scale(BUILDING_DEPTH));
        // Create new nodes and directed edges for the lot
        GMLNode n1 = map.ensureNodeNear(topRight);
        GMLNode n2 = map.ensureNodeNear(topLeft);
        GMLNode n3 = map.ensureNodeNear(bottomLeft);
        GMLNode n4 = map.ensureNodeNear(bottomRight);
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        GMLDirectedEdge e1 = map.ensureDirectedEdge(topRight, topLeft);
        GMLDirectedEdge e2 = map.ensureDirectedEdge(topLeft, bottomLeft);
        GMLDirectedEdge e3 = map.ensureDirectedEdge(bottomLeft, bottomRight);
        GMLDirectedEdge e4 = map.ensureDirectedEdge(bottomRight, topRight);
        edges.add(e1);
        edges.add(e2);
        edges.add(e3);
        edges.add(e4);
        GMLFace buildingFace = map.createFace(edges, FaceType.BUILDING);
        // Make the entrance face
        Set<GMLFace> result = new HashSet<GMLFace>();
        result.add(buildingFace);
        return result;
    }

    private double getRandomOffset() {
        double d = random.nextDouble();
        double range = MAX_OFFSET - MIN_OFFSET;
        return MIN_OFFSET + (d * range);
    }

    private int getRandomRunLength() {
        return MIN_RUN_LENGTH + random.nextInt(MAX_RUN_LENGTH - MIN_RUN_LENGTH + 1);
    }
}