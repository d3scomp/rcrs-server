package osm2gml;

import osm2gml.osm.OSMMap;
import osm2gml.osm.OSMMapViewer;
import osm2gml.osm.OSMException;
import osm2gml.osm.OSMBuilding;
import osm2gml.osm.OSMRoad;
import osm2gml.osm.OSMNode;
import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLMapViewer;
import osm2gml.gml.GMLException;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLEdge;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLFace;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;

import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
//import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

public class Convert {
    // Nodes that are close are deemed to be co-located.
    private final static double TOLERANCE = 0.00001;
    private final static double NEARBY_NODE_THRESHOLD = 0.00015;

    // Roads are 7m wide
    private final static float ROAD_WIDTH = 7;

    // A Stroke for turning road lines into rectangles.
    private final static BasicStroke ROAD_STROKE = new BasicStroke(ROAD_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private OSMMap osmMap;
    private GMLMap gmlMap;
    private long nextID;
    private Area roadArea;
    private Area roadIntersectionArea;
    private Area roadBodyArea;
    private Area buildingArea;
    private List<Area> roadSegments;
    private List<Area> roadIntersectionSegments;
    private List<Area> roadBodySegments;
    private List<Area> buildingSegments;
    private Map<Shape, List<GMLNode>> shapeNodes;
    private Map<Shape, List<GMLDirectedEdge>> shapeEdges;
    private ExecutorService service;
    private ShapeDebugFrame debug;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Convert <osm-mapname>");
            return;
        }
        try {
            OSMMap osmMap = readOSMMap(args[0]);
            OSMMapViewer osmViewer = new OSMMapViewer(osmMap);
            Convert convert = new Convert();
            GMLMap gmlMap = convert.convert(osmMap);
            GMLMapViewer gmlViewer = new GMLMapViewer(gmlMap);
            JFrame frame = new JFrame("Convertor");
            JPanel main = new JPanel(new GridLayout(1, 2));
            osmViewer.setPreferredSize(new Dimension(500, 500));
            gmlViewer.setPreferredSize(new Dimension(500, 500));
            osmViewer.setBorder(BorderFactory.createTitledBorder("OSM map"));
            gmlViewer.setBorder(BorderFactory.createTitledBorder("GML map"));
            main.add(osmViewer);
            main.add(gmlViewer);
            frame.setContentPane(main);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OSMMap readOSMMap(String file) throws OSMException, IOException, DocumentException {
        File f = new File(file);
        return new OSMMap(f);
    }

    public GMLMap convert(final OSMMap map) throws InterruptedException, ExecutionException {
        osmMap = map;
        gmlMap = new GMLMap();
        nextID = 1;
        roadArea = new Area();
        roadIntersectionArea = new Area();
        roadBodyArea = new Area();
        buildingArea = new Area();
        roadSegments = new ArrayList<Area>();
        roadIntersectionSegments = new ArrayList<Area>();
        roadBodySegments = new ArrayList<Area>();
        buildingSegments = new ArrayList<Area>();
        shapeNodes = new LazyMap<Shape, List<GMLNode>>() {
            public List<GMLNode> createValue() {
                return new ArrayList<GMLNode>();
            }
        };
        shapeEdges = new LazyMap<Shape, List<GMLDirectedEdge>>() {
            public List<GMLDirectedEdge> createValue() {
                return new ArrayList<GMLDirectedEdge>();
            }
        };

        System.out.println("Converting OSM map with " + map.getRoads().size() + " roads and " + map.getBuildings().size() + " buildings");
        System.out.println("Map size: " + (map.getMaxLongitude() - map.getMinLongitude()) + " x " + (map.getMaxLatitude() - map.getMinLatitude()));
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Starting " + processors + " processing threads");
        service = Executors.newFixedThreadPool(processors);
        debug = new ShapeDebugFrame();
        // Generate all shapes
        generateBuildingShapes();
        generateRoadShapes();
        //        debug.show(new ShapeDebugFrame.ShapeInfo(buildingArea, "Buildings", Color.BLACK, true),
        //                   new ShapeDebugFrame.ShapeInfo(roadArea, "Roads", Color.BLACK, false));
        // Turn into GML
        generateGML2();
        generateBuildingEntrances();
        service.shutdown();
        return gmlMap;
    }

    private void generateRoadShapes() throws InterruptedException, ExecutionException {
        List<Future<Area>> futures = new ArrayList<Future<Area>>();
        System.out.println("Generating road shapes: " + osmMap.getRoads().size() + " road segments");
        for (OSMRoad road : osmMap.getRoads()) {
            Iterator<Long> it = road.getNodeIDs().iterator();
            OSMNode lastNode = osmMap.getNode(it.next());
            while (it.hasNext()) {
                OSMNode nextNode = osmMap.getNode(it.next());
                final OSMNode from = lastNode;
                final OSMNode to = nextNode;
                Callable<Area> callable = new Callable<Area>() {
                    public Area call() {
                        return generateRoadArea(from, to);
                    }
                };
                lastNode = nextNode;
                futures.add(service.submit(callable));
            };
        }
        int total = futures.size();
        int count = 0;
        for (Future<Area> next : futures) {
            Area a = next.get();
            roadSegments.add(a);
            roadArea.add(a);
            System.out.println((++count) + " of " + total);
        }
    }

    private void generateRoadShapes2() throws InterruptedException, ExecutionException {
        int total = osmMap.getRoads().size();
        int i = 0;
        System.out.println("Generating road shapes");
        System.out.println("Intersections");
        for (OSMRoad road : osmMap.getRoads()) {
            for (long next : road.getNodeIDs()) {
                OSMNode node = osmMap.getNode(next);
                Area area = new Area(new Ellipse2D.Double(node.getLongitude() - ROAD_WIDTH / 2, node.getLatitude() - ROAD_WIDTH / 2, ROAD_WIDTH, ROAD_WIDTH));
                area.subtract(roadArea);
                area.subtract(buildingArea);
                if (!area.isEmpty()) {
                    roadSegments.add(area);
                    roadIntersectionSegments.add(area);
                    roadArea.add(area);
                    roadIntersectionArea.add(area);
                }
            }
            System.out.println(i + " of " + total);
        }
        System.out.println("Roads");
        for (OSMRoad road : osmMap.getRoads()) {
            Iterator<Long> it = road.getNodeIDs().iterator();
            OSMNode from = osmMap.getNode(it.next());
            while (it.hasNext()) {
                OSMNode to = osmMap.getNode(it.next());
                Line2D line = new Line2D.Double(from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());
                Area area = new Area(ROAD_STROKE.createStrokedShape(line));
                area.subtract(roadArea);
                area.subtract(buildingArea);
                if (!area.isEmpty()) {
                    roadSegments.add(area);
                    roadBodySegments.add(area);
                    roadArea.add(area);
                    roadBodyArea.add(area);
                }
                from = to;
            }
            System.out.println(i + " of " + total);
        }
    }

    /**
       Generate the shape for a road segment.
    */
    private Area generateRoadArea(OSMNode from, OSMNode to) {
        Shape start = generateIntersectionArea(from);
        Shape end = generateIntersectionArea(to);
        // Create a rectangle covering this segment of road
        // We need to do some nasty conversion from latitude/longitude to metres and back again
        Point2D fromPoint = new Point2D(from.getLongitude(), from.getLatitude());
        Point2D toPoint = new Point2D(to.getLongitude(), to.getLatitude());
        Vector2D vector = fromPoint.minus(toPoint);
        // The normal to 'vector' tells us the right direction for finding the edges of the road.
        Vector2D normal = vector.getNormal().normalised().scale(ROAD_WIDTH / 2);
        LatLong fromRight = shift(from.getLatitude(), from.getLongitude(), normal.getY(), normal.getX());
        LatLong toRight = shift(to.getLatitude(), to.getLongitude(), normal.getY(), normal.getX());
        LatLong fromLeft = shift(from.getLatitude(), from.getLongitude(), -normal.getY(), -normal.getX());
        LatLong toLeft = shift(to.getLatitude(), to.getLongitude(), -normal.getY(), -normal.getX());
        double x1 = toLeft.longitudeValue(NonSI.DEGREE_ANGLE);
        double y1 = toLeft.latitudeValue(NonSI.DEGREE_ANGLE);
        double x2 = toRight.longitudeValue(NonSI.DEGREE_ANGLE);
        double y2 = toRight.latitudeValue(NonSI.DEGREE_ANGLE);
        double x3 = fromRight.longitudeValue(NonSI.DEGREE_ANGLE);
        double y3 = fromRight.latitudeValue(NonSI.DEGREE_ANGLE);
        double x4 = fromLeft.longitudeValue(NonSI.DEGREE_ANGLE);
        double y4 = fromLeft.latitudeValue(NonSI.DEGREE_ANGLE);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.closePath();

        Area result = new Area();
        result.add(new Area(path.createTransformedShape(null)));
        result.add(new Area(start));
        result.add(new Area(end));
        return result;
    }

    private Shape generateIntersectionArea(OSMNode node) {
        LatLong topLeft = shift(node.getLatitude(), node.getLongitude(), -ROAD_WIDTH / 2, -ROAD_WIDTH / 2);
        LatLong bottomRight = shift(node.getLatitude(), node.getLongitude(), ROAD_WIDTH / 2, ROAD_WIDTH / 2);
        double lon = topLeft.longitudeValue(NonSI.DEGREE_ANGLE);
        double lonRange = bottomRight.longitudeValue(NonSI.DEGREE_ANGLE) - lon;
        double lat = topLeft.latitudeValue(NonSI.DEGREE_ANGLE);
        double latRange = bottomRight.latitudeValue(NonSI.DEGREE_ANGLE) - lat;
        return new Ellipse2D.Double(lon, lat, lonRange, latRange);
    }

    private void generateBuildingShapes() throws InterruptedException, ExecutionException {
        List<Future<Area>> futures = new ArrayList<Future<Area>>();
        System.out.println("Generating building shapes: " + osmMap.getBuildings().size() + " buildings to process");
        for (OSMBuilding building : osmMap.getBuildings()) {
            final OSMBuilding b = building;
            Callable<Area> callable = new Callable<Area>() {
                public Area call() {
                    Path2D buildingShape = new Path2D.Double();
                    Iterator<Long> it = b.getNodeIDs().iterator();
                    OSMNode node = osmMap.getNode(it.next());
                    buildingShape.moveTo(node.getLongitude(), node.getLatitude());
                    while (it.hasNext()) {
                        node = osmMap.getNode(it.next());
                        buildingShape.lineTo(node.getLongitude(), node.getLatitude());
                    }
                    // Close the shape
                    buildingShape.closePath();
                    return new Area(buildingShape);
                }
            };
            futures.add(service.submit(callable));
        }
        int total = futures.size();
        int count = 0;
        for (Future<Area> next : futures) {
            Area a = next.get();
            a.subtract(buildingArea);
            buildingSegments.add(a);
            buildingArea.add(a);
            System.out.println((++count) + " of " + total);
        }
    }

    /**
       Populate a GMLMap with objects from the road and building network.
    */
    private void generateGML() throws InterruptedException, ExecutionException {
        // Build the list of road intersections and subtract any overlaps with buildings
        Area intersection = new Area(roadArea);
        Area nonIntersection = new Area();
        List<Future<Area>> futures = new ArrayList<Future<Area>>();
        System.out.println("Calculating road intersections");
        for (Area road : roadSegments) {
            final Area a = road;
            Callable<Area> callable = new Callable<Area>() {
                public Area call() {
                    Area result = new Area(a);
                    for (Area next : roadSegments) {
                        if (next == a) {
                            continue;
                        }
                        result.subtract(next);
                    }
                    for (Area next : buildingSegments) {
                        result.subtract(next);
                    }
                    return result;
                }
            };
            futures.add(service.submit(callable));
        }
        int total = futures.size();
        int count = 0;
        for (Future<Area> next : futures) {
            Area area = next.get();
            /*
              debug.show(new ShapeDebugFrame.ShapeInfo(intersection, "Intersections", Color.black, false),
              new ShapeDebugFrame.ShapeInfo(nonIntersection, "Non-intersections", Color.red, true),
              new ShapeDebugFrame.ShapeInfo(buildingArea, "Buildings", Color.black, true),
              new ShapeDebugFrame.ShapeInfo(area, "Next road segment", Color.blue, true));
            */
            intersection.subtract(area);
            nonIntersection.add(area);
            /*
              debug.show(new ShapeDebugFrame.ShapeInfo(intersection, "Intersections", Color.black, false),
              new ShapeDebugFrame.ShapeInfo(buildingArea, "Buildings", Color.black, true),
              new ShapeDebugFrame.ShapeInfo(nonIntersection, "Non-intersections", Color.red, true));
            */
            System.out.println((++count) + " of " + total);
        }
        for (Area next : buildingSegments) {
            intersection.subtract(next);
        }
        // Now generate the shapes and create nodes as required
        System.out.println("Generating GML: intersections...");
        generateGMLFromShape(intersection, true);
        System.out.println("roads...");
        generateGMLFromShape(nonIntersection, true);
        /*
          System.out.println("Generating GML: intersections...");
          generateGMLFromShape(roadIntersectionArea, true);
          System.out.println("roads...");
          generateGMLFromShape(roadBodyArea, true);
          System.out.println("buildings...");
        */
        for (Area next : buildingSegments) {
            generateGMLFromShape(next, false);
        }
        System.out.println("done");
    }

    /**
       Populate a GMLMap with objects from the road and building network.
    */
    private void generateGML2() throws InterruptedException, ExecutionException {
        generateGMLFromShape(roadArea, true);
        // Now generate internal edges for roads
        System.out.println("Generating internal edges");
        int i = 0;
        int total = gmlMap.getNodes().size();
        List<GMLEdge> newEdges = new ArrayList<GMLEdge>();
        for (GMLNode node : gmlMap.getNodes()) {
            System.out.println((++i) + " of " + total);
            // Look for nearby nodes
            for (GMLNode next : gmlMap.getNodes()) {
                if (next == node || edgeFromTo(node, next) != null) {
                    continue;
                }
                // See if the other node is nearby
                double dx = next.getX() - node.getX();
                double dy = next.getY() - node.getY();
                double distance = Math.hypot(dx, dy);
                if (distance < NEARBY_NODE_THRESHOLD) {
                    // Check that the line is entirely inside the road area
                    // We'll just check a few sample points for now. This is probably fragile.
                    if (!roadArea.contains(node.getX() + (dx / 2), node.getY() + (dy / 2))) {
                        continue;
                    }
                    if (!roadArea.contains(node.getX() + (dx / 4), node.getY() + (dy / 4))) {
                        continue;
                    }
                    if (!roadArea.contains(node.getX() + (3 * dx / 4), node.getY() + (3 * dy / 4))) {
                        continue;
                    }
                    // Create a passable edge
                    rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(node.getX(), node.getY(), dx, dy);
                    // Check if this line crosses any others
                    boolean cross = false;
                    for (Iterator<GMLEdge> edges = newEdges.iterator(); edges.hasNext() && !cross;) {
                        GMLEdge nextEdge = edges.next();
                        GMLNode start = nextEdge.getStart();
                        GMLNode end = nextEdge.getEnd();
                        //                        System.out.println("Checking for intersection with " + nextEdge);
                        dx = end.getX() - start.getX();
                        dy = end.getY() - start.getY();
                        rescuecore2.misc.geometry.Line2D testLine = new rescuecore2.misc.geometry.Line2D(start.getX(), start.getY(), dx, dy);
                        double d1 = line.getIntersection(testLine);
                        double d2 = testLine.getIntersection(line);
                        cross = !Double.isNaN(d1) && !Double.isNaN(d2) && d1 > 0 && d1 < 1 && d2 > 0 && d2 < 1;
                        //                        System.out.println("d1 = " + d1);
                        //                        System.out.println("d2 = " + d2);
                        //                        System.out.println(cross ? "Intersection" : "No intersection");
                        /*
                        debug.show(new ShapeDebugFrame.ShapeInfo(roadArea, "All roads", Color.LIGHT_GRAY, false),
                                   new ShapeDebugFrame.ShapeInfo(new Ellipse2D.Double(node.getX() - 0.00001, node.getY() - 0.00001, 0.00003, 0.00003), "Source node", Color.blue, true),
                                   new ShapeDebugFrame.ShapeInfo(new Ellipse2D.Double(next.getX() - 0.00001, next.getY() - 0.00001, 0.00003, 0.00003), "Target node", Color.black, true),
                                   new ShapeDebugFrame.ShapeInfo(new Line2D.Double(next.getX(), next.getY(), node.getX(), node.getY()), "Candidate edge", Color.blue, false),
                                   new ShapeDebugFrame.ShapeInfo(new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY()), "Test edge", Color.red, false)
                                   );
                        if (cross) {
                        System.out.println("Intersection found with edge from " + node + " to " + next + ": " + nextEdge);
                        debug.show(new ShapeDebugFrame.ShapeInfo(roadArea, "All roads", Color.BLACK, false),
                        new ShapeDebugFrame.ShapeInfo(new Ellipse2D.Double(node.getX() - 0.00001, node.getY() - 0.00001, 0.00003, 0.00003), "Source node", Color.blue, true),
                        new ShapeDebugFrame.ShapeInfo(new Ellipse2D.Double(next.getX() - 0.00001, next.getY() - 0.00001, 0.00003, 0.00003), "Target node", Color.black, true),
                        new ShapeDebugFrame.ShapeInfo(new Line2D.Double(next.getX(), next.getY(), node.getX(), node.getY()), "Candidate edge", Color.blue, false),
                        new ShapeDebugFrame.ShapeInfo(new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY()), "Test edge", Color.red, false)
                        );
                        System.out.println("d1 = " + d1);
                        System.out.println("d2 = " + d2);
                        }
                        */
                    }
                    if (!cross) {
                        GMLEdge edge = edgeFromTo(node, next);
                        if (edge == null) {
                            edge = new GMLEdge(nextID++, node, next, true);
                            gmlMap.addEdge(edge);
                            newEdges.add(edge);
                        }
                    }
                }
            }
        }
    }

    private void generateBuildingEntrances() {
        for (Area next : buildingSegments) {
            // Find a wall that is close to a road
            
        }
    }

    private void generateGMLFromShape(Shape shape, boolean sharedEdgesPassable) {
        PathIterator pi = shape.getPathIterator(null, 0.00001);
        double[] d = new double[6];
        GMLNode lastMove = null;
        GMLNode lastLine = null;
        //        debug.show(new ShapeDebugFrame.ShapeInfo(shape, "Shape -> GML", Color.black, true));
        while (!pi.isDone()) {
            int type = pi.currentSegment(d);
            GMLNode node = getNodeNear(d[0], d[1]);
            GMLEdge edge = null;
            if (type == PathIterator.SEG_MOVETO) {
                lastMove = node;
                lastLine = node;
            }
            else if (type == PathIterator.SEG_LINETO) {
                // Create an edge
                edge = edgeFromTo(lastLine, node);
                if (edge == null) {
                    if (lastLine != node) {
                        edge = new GMLEdge(nextID++, lastLine, node, false);
                        gmlMap.addEdge(edge);
                    }
                }
                else {
                    edge.setPassable(sharedEdgesPassable);
                }
            }
            else if (type == PathIterator.SEG_CLOSE) {
                // Create an edge
                edge = edgeFromTo(node, lastMove);
                if (edge == null) {
                    if (node != lastMove) {
                        edge = new GMLEdge(nextID++, node, lastMove, false);
                        gmlMap.addEdge(edge);
                    }
                }
                else {
                    edge.setPassable(sharedEdgesPassable);
                }
            }
            else {
                System.err.println("Don't know what to do with path iterator segment type " + type);
            }
            lastLine = node;
            pi.next();
            List<GMLNode> nodesForShape = shapeNodes.get(shape);
            List<GMLDirectedEdge> edgesForShape = shapeEdges.get(shape);
            nodesForShape.add(node);
            if (edge != null) {
                // Is this a forward or backward edge?
                boolean forward = edge.getStart() == node;
                GMLDirectedEdge de = new GMLDirectedEdge(edge, forward);
                edgesForShape.add(de);
            }
        }
    }

    private GMLNode getNodeNear(double x, double y) {
        for (GMLNode next : gmlMap.getNodes()) {
            double nodeX = next.getX();
            double nodeY = next.getY();
            if (nodeX >= x - TOLERANCE &&
                nodeX <= x + TOLERANCE &&
                nodeY >= y - TOLERANCE &&
                nodeY <= y + TOLERANCE) {
                return next;
            }
        }
        GMLNode node = new GMLNode(nextID++, x, y);
        //        System.out.println("New GMLNode at " + x + ", " + y);
        gmlMap.addNode(node);
        return node;
    }

    private GMLEdge edgeFromTo(GMLNode from, GMLNode to) {
        for (GMLEdge next : gmlMap.getEdges()) {
            if ((next.getStart() == from && next.getEnd() == to) ||
                (next.getStart() == to && next.getEnd() == from)) {
                return next;
            }
        }
        return null;
    }

    /**
       Shift a latitude/longitude coordinate by some distance in meters.
    */
    private LatLong shift(double lat, double lon, double dy, double dx) {
        UTM centre = UTM.latLongToUtm(LatLong.valueOf(lat, lon, NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);
        UTM result = UTM.valueOf(centre.longitudeZone(), centre.latitudeZone(), centre.eastingValue(SI.METRE) + dx, centre.northingValue(SI.METRE) + dy, SI.METRE);
        return UTM.utmToLatLong(result, ReferenceEllipsoid.WGS84);
    }
}