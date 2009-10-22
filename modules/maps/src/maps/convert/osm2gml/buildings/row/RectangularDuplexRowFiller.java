package maps.convert.osm2gml.buildings.row;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import maps.convert.osm2gml.ConvertTools;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLFace;
import maps.gml.GMLMap;
import maps.gml.FaceType;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;

public class RectangularDuplexRowFiller implements RowFiller {
    private final static double WIDE_LOT_WIDTH_M = 25;
    private final static double WIDE_BUILDING_WIDTH_M = 21;
    private final static double WIDE_BUILDING_DEPTH_M = 7;
    private final static double WIDE_MIN_OFFSET_M = 2;
    private final static double WIDE_MAX_OFFSET_M = 10;

    private final static double LONG_LOT_WIDTH_M = 18;
    private final static double LONG_BUILDING_WIDTH_M = 15;
    private final static double LONG_BUILDING_DEPTH_M = 15;
    private final static double LONG_MIN_OFFSET_M = 1;
    private final static double LONG_MAX_OFFSET_M = 4;

    private final static int MIN_RUN_LENGTH = 1;
    private final static int MAX_RUN_LENGTH = 5;

    private final double LOT_WIDTH;
    private final double BUILDING_WIDTH;
    private final double BUILDING_DEPTH;
    private final double MIN_OFFSET;
    private final double MAX_OFFSET;

    private final Random random;

    public static RectangularDuplexRowFiller makeWideFiller(double sizeOf1m, Random random) {
        return new RectangularDuplexRowFiller(sizeOf1m, random, WIDE_LOT_WIDTH_M, WIDE_BUILDING_WIDTH_M, WIDE_BUILDING_DEPTH_M, WIDE_MIN_OFFSET_M, WIDE_MAX_OFFSET_M);
    }

    public static RectangularDuplexRowFiller makeLongFiller(double sizeOf1m, Random random) {
        return new RectangularDuplexRowFiller(sizeOf1m, random, LONG_LOT_WIDTH_M, LONG_BUILDING_WIDTH_M, LONG_BUILDING_DEPTH_M, LONG_MIN_OFFSET_M, LONG_MAX_OFFSET_M);
    }

    private RectangularDuplexRowFiller(double sizeOf1m, Random random, double lotWidth, double buildingWidth, double buildingDepth, double minOffset, double maxOffset) {
        LOT_WIDTH = lotWidth * sizeOf1m;
        BUILDING_WIDTH = buildingWidth * sizeOf1m;
        BUILDING_DEPTH = buildingDepth * sizeOf1m;
        MIN_OFFSET = minOffset * sizeOf1m;
        MAX_OFFSET = maxOffset * sizeOf1m;
        this.random = random;
    }

    @Override
    public Set<GMLFace> fillRow(GMLDirectedEdge edge, GMLMap map) {
        Set<GMLFace> result = new HashSet<GMLFace>();
        /*
        Line2D edgeLine = ConvertTools.gmlDirectedEdgeToLine(edge);
        Vector2D normal = edgeLine.getDirection().getNormal().normalised();
        // Create lots along the edge
        double edgeLength = edgeLine.getDirection().getLength();
        int lots = (int)(edgeLength / LOT_WIDTH);
        double trueLotWidth = edgeLength / lots;
        System.out.println("Creating " + lots + " lots");
        double offset = getRandomOffset();
        int runLength = getRandomRunLength();
        for (int i = 0; i < lots; ++i) {
            if (runLength-- == 0) {
                offset = getRandomOffset();
                runLength = getRandomRunLength();
            }
            double d1 = i;
            double d2 = i + 1;
            d1 /= lots;
            d2 /= lots;
            Point2D topRight = edgeLine.getPoint(d1);
            Point2D topLeft = edgeLine.getPoint(d2);
            Set<GMLFace> faces = createBuildingInLot(edgeLine, topRight, topLeft, normal, offset, map);
            result.addAll(faces);
        }
        */
        return result;
    }

    /*
    private Set<GMLFace> createBuildingInLot(Line2D edgeLine, Point2D lotTopRight, Point2D lotTopLeft, Vector2D edgeNormal, double depthOffset, GMLMap map) {
        // Create the building by moving in from the sides of the lot boundary
        Line2D topLine = new Line2D(lotTopRight, lotTopLeft);
        double lotWidth = topLine.getDirection().getLength();
        double widthSlack = ((lotWidth - BUILDING_WIDTH) / lotWidth) / 2;
        Point2D topRight = topLine.getPoint(widthSlack);
        Point2D topMiddle = topLine.getPoint(0.5);
        Point2D topLeft = topLine.getPoint(1.0 - widthSlack);
        // Offset from the top of the boundary
        topRight = topRight.plus(edgeNormal.scale(depthOffset));
        topMiddle = topMiddle.plus(edgeNormal.scale(depthOffset));
        topLeft = topLeft.plus(edgeNormal.scale(depthOffset));
        // Find the other end of the building
        Point2D bottomRight = topRight.plus(edgeNormal.scale(BUILDING_DEPTH));
        Point2D bottomMiddle = topMiddle.plus(edgeNormal.scale(BUILDING_DEPTH));
        Point2D bottomLeft = topLeft.plus(edgeNormal.scale(BUILDING_DEPTH));
        // Create new nodes and directed edges for the building
        GMLNode n1 = map.ensureNodeNear(topRight);
        GMLNode n2 = map.ensureNodeNear(topMiddle);
        GMLNode n3 = map.ensureNodeNear(topLeft);
        GMLNode n4 = map.ensureNodeNear(bottomLeft);
        GMLNode n5 = map.ensureNodeNear(bottomMiddle);
        GMLNode n6 = map.ensureNodeNear(bottomRight);
        // Create two new buildings
        List<GMLDirectedEdge> edges1 = new ArrayList<GMLDirectedEdge>();

//        List<GMLDirectedEdge> edges2 = new ArrayList<GMLDirectedEdge>();
//        edges1.add(map.ensureDirectedEdge(topRight, topMiddle));
//        edges1.add(map.ensureDirectedEdge(topMiddle, bottomMiddle));
//        edges1.add(map.ensureDirectedEdge(bottomMiddle, bottomRight));
//        edges1.add(map.ensureDirectedEdge(bottomRight, topRight));
//        edges2.add(map.ensureDirectedEdge(topMiddle, topLeft));
//        edges2.add(map.ensureDirectedEdge(topLeft, bottomLeft));
//        edges2.add(map.ensureDirectedEdge(bottomLeft, bottomMiddle));
//        edges2.add(map.ensureDirectedEdge(bottomMiddle, topMiddle));

        edges1.add(map.ensureDirectedEdge(topRight, topLeft));
        edges1.add(map.ensureDirectedEdge(topLeft, bottomLeft));
        edges1.add(map.ensureDirectedEdge(bottomLeft, bottomRight));
        edges1.add(map.ensureDirectedEdge(bottomRight, topRight));
        // TODO: Make the entrance faces
        Set<GMLFace> result = new HashSet<GMLFace>();
        result.add(map.createFace(edges1, FaceType.BUILDING));
        //        result.add(map.createFace(edges2, FaceType.BUILDING));
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
*/
}