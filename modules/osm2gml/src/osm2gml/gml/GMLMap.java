package osm2gml.gml;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.geometry.Point2D;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.Attribute;
import org.dom4j.XPath;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;

/**
   A GML map.
*/
public class GMLMap {
    private static final String GML_NAMESPACE_URI = "http://www.opengis.net/gml";
    private static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
    private static final String RCR_NAMESPACE_URI = "http://sakura.meijo-u.ac.jp/rcrs";
    private static final Namespace GML_NAMESPACE = DocumentHelper.createNamespace("gml", GML_NAMESPACE_URI);
    private static final Namespace XLINK_NAMESPACE = DocumentHelper.createNamespace("xlink", XLINK_NAMESPACE_URI);
    private static final Namespace RCR_NAMESPACE = DocumentHelper.createNamespace("rcrs", RCR_NAMESPACE_URI);

    private static final QName GML_ID_QNAME = DocumentHelper.createQName("id", GML_NAMESPACE);
    private static final QName GML_NODE_QNAME = DocumentHelper.createQName("Node", GML_NAMESPACE);
    private static final QName GML_EDGE_QNAME = DocumentHelper.createQName("Edge", GML_NAMESPACE);
    private static final QName GML_FACE_QNAME = DocumentHelper.createQName("Face", GML_NAMESPACE);
    private static final QName GML_POINT_PROPERTY_QNAME = DocumentHelper.createQName("pointProperty", GML_NAMESPACE);
    private static final QName GML_POINT_QNAME = DocumentHelper.createQName("Point", GML_NAMESPACE);
    private static final QName GML_COORDINATES_QNAME = DocumentHelper.createQName("coordinates", GML_NAMESPACE);
    private static final QName GML_ORIENTATION_QNAME = DocumentHelper.createQName("orientation");
    private static final QName GML_DIRECTED_NODE_QNAME = DocumentHelper.createQName("directedNode", GML_NAMESPACE);
    private static final QName GML_DIRECTED_EDGE_QNAME = DocumentHelper.createQName("directedEdge", GML_NAMESPACE);
    private static final QName GML_DIRECTED_FACE_QNAME = DocumentHelper.createQName("directedFace", GML_NAMESPACE);
    private static final QName GML_CENTRE_LINE_OF_QNAME = DocumentHelper.createQName("centerLineOf", GML_NAMESPACE);
    private static final QName GML_LINE_STRING_QNAME = DocumentHelper.createQName("LineString", GML_NAMESPACE);
    private static final QName GML_POLYGON_QNAME = DocumentHelper.createQName("polygon", GML_NAMESPACE);
    private static final QName GML_LINEAR_RING_QNAME = DocumentHelper.createQName("LinearRing", GML_NAMESPACE);

    private static final QName XLINK_HREF_QNAME = DocumentHelper.createQName("href", XLINK_NAMESPACE);

    private static final QName RCR_AREA_QNAME = DocumentHelper.createQName("Area", RCR_NAMESPACE);
    private static final QName RCR_NODE_LIST_QNAME = DocumentHelper.createQName("NodeList", RCR_NAMESPACE);
    private static final QName RCR_EDGE_LIST_QNAME = DocumentHelper.createQName("EdgeList", RCR_NAMESPACE);
    private static final QName RCR_FACE_LIST_QNAME = DocumentHelper.createQName("FaceList", RCR_NAMESPACE);
    private static final QName RCR_FACE_QNAME = DocumentHelper.createQName("Face", RCR_NAMESPACE);
    private static final QName RCR_TYPE_QNAME = DocumentHelper.createQName("Type", RCR_NAMESPACE);
    private static final QName RCR_VERSION_QNAME = DocumentHelper.createQName("Version", RCR_NAMESPACE);
    private static final String RCR_VERSION = "RCRSGML[1.0.0]";


    private static final Map<String, String> URIS = new HashMap<String, String>();
    private static final XPath NODE_PATH = DocumentHelper.createXPath("//gml:Node");
    private static final XPath NODE_COORDINATES_PATH = DocumentHelper.createXPath("gml:pointProperty/gml:Point/gml:coordinates/text()");
    //    private static final XPath EDGE_START_PATH = DocumentHelper.createXPath("gml:directedNode[@orientation='-']/@xlink:href");
    //    private static final XPath EDGE_END_PATH = DocumentHelper.createXPath("gml:directedNode[@orientation='+']/@xlink:href");

    private static final XPath EDGE_PATH = DocumentHelper.createXPath("//gml:Edge");
    private static final XPath EDGE_START_PATH = DocumentHelper.createXPath("gml:directedNode[1]/@xlink:href");
    private static final XPath EDGE_END_PATH = DocumentHelper.createXPath("gml:directedNode[2]/@xlink:href");
    private static final XPath EDGE_COORDINATES_PATH = DocumentHelper.createXPath("gml:centerLineOf/gml:LineString/gml:coordinates/text()");

    private static final XPath FACE_PATH = DocumentHelper.createXPath("//gml:Face");
    private static final XPath FACE_EDGE_PATH = DocumentHelper.createXPath("gml:directedEdge");
    private static final XPath FACE_SHAPE_PATH = DocumentHelper.createXPath("gml:polygon/gml:LinearRing/gml:coordinates/text()");

    private final static double DEFAULT_THRESHOLD = 0.000001;

    static {
        URIS.put("gml", GML_NAMESPACE_URI);
        URIS.put("xlink", XLINK_NAMESPACE_URI);
        NODE_PATH.setNamespaceURIs(URIS);
        NODE_COORDINATES_PATH.setNamespaceURIs(URIS);
        EDGE_PATH.setNamespaceURIs(URIS);
        EDGE_START_PATH.setNamespaceURIs(URIS);
        EDGE_END_PATH.setNamespaceURIs(URIS);
        EDGE_COORDINATES_PATH.setNamespaceURIs(URIS);
    }

    private Map<Long, GMLNode> nodes;
    private Map<Long, GMLEdge> edges;
    private Map<Long, GMLFace> faces;
    private Map<GMLNode, Set<GMLEdge>> nodeToEdges;
    private Map<GMLEdge, Set<GMLFace>> edgeToFaces;

    private Map<FaceType, Set<GMLFace>> facesByType;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private boolean boundsKnown;

    private long nextID;
    private double threshold;

    /**
       Construct an empty GML map.
     */
    public GMLMap() {
        nodes = new HashMap<Long, GMLNode>();
        edges = new HashMap<Long, GMLEdge>();
        faces = new HashMap<Long, GMLFace>();
        nodeToEdges = new LazyMap<GMLNode, Set<GMLEdge>>() {
            public Set<GMLEdge> createValue() {
                return new HashSet<GMLEdge>();
            }
        };
        edgeToFaces = new LazyMap<GMLEdge, Set<GMLFace>>() {
            public Set<GMLFace> createValue() {
                return new HashSet<GMLFace>();
            }
        };
        facesByType = new EnumMap<FaceType, Set<GMLFace>>(FaceType.class);
        boundsKnown = false;
        nextID = 1;
        threshold = DEFAULT_THRESHOLD;
    }

    /**
       Construct a GML map that reads an XML document.
       @param doc The Document to read.
       @throws GMLException If the document is invalid.
     */
    public GMLMap(Document doc) throws GMLException {
        this();
        read(doc);
    }

    /**
       Read an XML document and populate this map.
       @param doc The Document to read.
       @throws GMLException If the document is invalid.
     */
    public void read(Document doc) throws GMLException {
        nodes.clear();
        edges.clear();
        faces.clear();
        nodeToEdges.clear();
        edgeToFaces.clear();
        Element root = doc.getRootElement();
        if (!"Topology".equals(root.getName())) {
            throw new GMLException("Invalid map file: root element must be 'Topology', not " + root.getName());
        }
        for (Object next : NODE_PATH.selectNodes(doc)) {
            Element e = (Element)next;
            GMLNode node = processNode(e);
            addNode(node);
        }
        for (Object next : EDGE_PATH.selectNodes(doc)) {
            Element e = (Element)next;
            GMLEdge edge = processEdge(e);
            addEdge(edge);
        }
        for (Object next : FACE_PATH.selectNodes(doc)) {
            Element e = (Element)next;
            GMLFace face = processFace(e);
            addFace(face);
        }
        boundsKnown = false;
    }

    /**
       Turn this map into an XML Document.
       @param m The amount to multiply dimensions by to turn them into millimetres.
       @return A new Document.
    */
    public Document toXML(double m) {
        Element root = DocumentHelper.createElement("Topology");
        Document result = DocumentHelper.createDocument(root);
        root.addElement(RCR_VERSION_QNAME).setText(RCR_VERSION);
        Element area = root.addElement(RCR_AREA_QNAME);
        Element nodeList = area.addElement(RCR_NODE_LIST_QNAME);
        for (GMLNode node : nodes.values()) {
            Element nodeElement = nodeList.addElement(GML_NODE_QNAME);
            nodeElement.addAttribute(GML_ID_QNAME, String.valueOf(node.getID()));
            Element coord = nodeElement.addElement(GML_POINT_PROPERTY_QNAME).addElement(GML_POINT_QNAME).addElement(GML_COORDINATES_QNAME);
            coord.setText(convert(node.getX(), m) + "," + convert(node.getY(), m));
        }
        Element edgeList = area.addElement(RCR_EDGE_LIST_QNAME);
        for (GMLEdge edge : edges.values()) {
            Element edgeElement = edgeList.addElement(GML_EDGE_QNAME);
            edgeElement.addAttribute(GML_ID_QNAME, String.valueOf(edge.getID()));
            Element dNode = edgeElement.addElement(GML_DIRECTED_NODE_QNAME);
            dNode.addAttribute(GML_ORIENTATION_QNAME, "+");
            dNode.addAttribute(XLINK_HREF_QNAME, "#" + edge.getStart().getID());
            dNode = edgeElement.addElement(GML_DIRECTED_NODE_QNAME);
            dNode.addAttribute(GML_ORIENTATION_QNAME, "+");
            dNode.addAttribute(XLINK_HREF_QNAME, "#" + edge.getEnd().getID());
            StringBuilder coordString = new StringBuilder();
            coordString.append(String.valueOf(convert(edge.getStart().getX(), m)));
            coordString.append(",");
            coordString.append(String.valueOf(convert(edge.getStart().getY(), m)));
            coordString.append(" ");
            coordString.append(String.valueOf(convert(edge.getEnd().getX(), m)));
            coordString.append(",");
            coordString.append(String.valueOf(convert(edge.getEnd().getY(), m)));
            edgeElement.addElement(GML_CENTRE_LINE_OF_QNAME).addElement(GML_LINE_STRING_QNAME).addElement(GML_COORDINATES_QNAME).setText(coordString.toString());
            // Look for faces connected to this edge
            for (GMLFace face : edgeToFaces.get(edge)) {
                Element dFace = edgeElement.addElement(GML_DIRECTED_FACE_QNAME);
                dFace.addAttribute(GML_ORIENTATION_QNAME, "+");
                dFace.addAttribute(XLINK_HREF_QNAME, "#" + face.getID());
            }
        }
        Element faceList = area.addElement(RCR_FACE_LIST_QNAME);
        for (GMLFace face : faces.values()) {
            Element rcrFaceElement = faceList.addElement(RCR_FACE_QNAME);
            Element faceElement = rcrFaceElement.addElement(GML_FACE_QNAME);
            faceElement.addAttribute(GML_ID_QNAME, String.valueOf(face.getID()));
            if (face.getFaceType() == FaceType.BUILDING) {
                rcrFaceElement.addAttribute("type", "building");
            }
            StringBuilder coordString = new StringBuilder();
            //            System.out.println("Generating coordinates for face " + face);
            for (Iterator<GMLDirectedEdge> it = face.getEdges().iterator(); it.hasNext();) {
                GMLDirectedEdge edge = it.next();
                //                System.out.println("Next edge: " + edge);
                //                System.out.println("Start: " + edge.getStartNode());
                Element dEdge = faceElement.addElement(GML_DIRECTED_EDGE_QNAME);
                dEdge.addAttribute(GML_ORIENTATION_QNAME, (edge.getEdge().isPassable() ? "+" : "-"));
                dEdge.addAttribute(XLINK_HREF_QNAME, "#" + edge.getEdge().getID());
                coordString.append(String.valueOf(convert(edge.getStartNode().getX(), m)));
                coordString.append(",");
                coordString.append(String.valueOf(convert(edge.getStartNode().getY(), m)));
                coordString.append(" ");
                if (!it.hasNext()) {
                    //                    System.out.println("End: " + edge.getEndNode());
                    coordString.append(String.valueOf(convert(edge.getEndNode().getX(), m)));
                    coordString.append(",");
                    coordString.append(String.valueOf(convert(edge.getEndNode().getY(), m)));
                    coordString.append(" ");
                }
            }
            faceElement.addElement(GML_POLYGON_QNAME).addElement(GML_LINEAR_RING_QNAME).addElement(GML_COORDINATES_QNAME).setText(coordString.toString());
            //            System.out.println("Result: " + coordString.toString());
        }
        return result;
    }

    private long convert(double d, double multiplier) {
        return (long)(d * multiplier);
    }

    /**
       Get the minimum x coordinate.
       @return The minimum x coordinate.
     */
    public double getMinX() {
        calculateBounds();
        return minX;
    }

    /**
       Get the maximum x coordinate.
       @return The maximum x coordinate.
     */
    public double getMaxX() {
        calculateBounds();
        return maxX;
    }

    /**
       Get the minimum y coordinate.
       @return The minimum y coordinate.
     */
    public double getMinY() {
        calculateBounds();
        return minY;
    }

    /**
       Get the maximum y coordinate.
       @return The maximum y coordinate.
     */
    public double getMaxY() {
        calculateBounds();
        return maxY;
    }

    /**
       Create a new node.
       @param x The X coordinate of the node.
       @param y The Y coordinate of the node.
       @return A new node.
    */
    public GMLNode createNode(double x, double y) {
        GMLNode node = new GMLNode(nextID++, x, y);
        addNode(node);
        return node;
    }

    /**
       Create a new edge.
       @param from The "from" node.
       @param to The "to" node.
       @param passable Whether the new edge is passable or not.
       @return A new edge.
    */
    public GMLEdge createEdge(GMLNode from, GMLNode to, boolean passable) {
        GMLEdge edge = new GMLEdge(nextID++, from, to, passable);
        addEdge(edge);
        return edge;
    }

    /**
       Create a new face.
       @param edges The edge list.
       @param type The face type.
       @return A new face.
    */
    public GMLFace createFace(List<GMLDirectedEdge> edges, FaceType type) {
        if (edges.size() < 3) {
            throw new IllegalArgumentException("Faces must have at least three edges");
        }
        GMLFace face = new GMLFace(nextID++, type, edges);
        addFace(face);
        return face;
    }

    /**
       Get all the nodes in the map.
       @return A Collection of GMLNodes.
     */
    public Collection<GMLNode> getNodes() {
        return new HashSet<GMLNode>(nodes.values());
    }

    /**
       Get a node with a particular ID.
       @param id The ID to look up.
       @return The GMLNode with that ID, or null if nothing is found.
     */
    public GMLNode getNode(long id) {
        return nodes.get(id);
    }

    /**
       Add a node.
       @param node The node to add.
    */
    public void addNode(GMLNode node) {
        nodes.put(node.getID(), node);
    }

    /**
       Remove a node. This will also remove any edges attached to the node and any faces attached to those edges.
       @param node The node to remove.
    */
    public void removeNode(GMLNode node) {
        nodes.remove(node.getID());
        for (GMLEdge edge : getAttachedEdges(node)) {
            removeEdge(edge);
        }
        nodeToEdges.remove(node);
    }

    /**
       Get all the edges in the map.
       @return A Collection of GMLEdges.
     */
    public Collection<GMLEdge> getEdges() {
        return new HashSet<GMLEdge>(edges.values());
    }

    /**
       Get an edge with a particular ID.
       @param id The ID to look up.
       @return The GMLEdge with that ID, or null if nothing is found.
     */
    public GMLEdge getEdge(long id) {
        return edges.get(id);
    }

    /**
       Add an edge.
       @param face The edge to add.
    */
    public void addEdge(GMLEdge edge) {
        edges.put(edge.getID(), edge);
        nodeToEdges.get(edge.getStart()).add(edge);
        nodeToEdges.get(edge.getEnd()).add(edge);
    }

    /**
       Remove an edge. This will also remove any faces attached to the edge.
       @param edge The edge to remove.
    */
    public void removeEdge(GMLEdge edge) {
        edges.remove(edge.getID());
        nodeToEdges.get(edge.getStart()).remove(edge);
        nodeToEdges.get(edge.getEnd()).remove(edge);
        for (GMLFace next : getAttachedFaces(edge)) {
            removeFace(next);
        }
        edgeToFaces.remove(edge);
    }

    /**
       Get all the faces in the map.
       @return A Collection of GMLFaces.
     */
    public Collection<GMLFace> getFaces() {
        return new HashSet<GMLFace>(faces.values());
    }

    /**
       Get all the faces of a particular type in the map.
       @return A Collection of GMLFaces.
     */
    public Collection<GMLFace> getFaces(FaceType type) {
        Set<GMLFace> s = facesByType.get(type);
        if (s == null) {
            s = new HashSet<GMLFace>();
            facesByType.put(type, s);
        }
        return new HashSet<GMLFace>(s);
    }

    /**
       Get a face with a particular ID.
       @param id The ID to look up.
       @return The GMLFace with that ID, or null if nothing is found.
     */
    public GMLFace getFace(long id) {
        return faces.get(id);
    }

    /**
       Add a face.
       @param face The face to add.
    */
    public void addFace(GMLFace face) {
        faces.put(face.getID(), face);
        for (GMLDirectedEdge edge : face.getEdges()) {
            addAttachedEdge(face, edge.getEdge());
        }
        Set<GMLFace> s = facesByType.get(face.getFaceType());
        if (s == null) {
            s = new HashSet<GMLFace>();
            facesByType.put(face.getFaceType(), s);
        }
        s.add(face);
    }

    /**
       Remove a face.
       @param face The face to remove.
    */
    public void removeFace(GMLFace face) {
        faces.remove(face.getID());
        for (GMLDirectedEdge edge : face.getEdges()) {
            edgeToFaces.get(edge.getEdge()).remove(face);
        }
        Set<GMLFace> s = facesByType.get(face.getFaceType());
        if (s == null) {
            s = new HashSet<GMLFace>();
            facesByType.put(face.getFaceType(), s);
        }
        s.remove(face);
    }

    /**
       Get all edges attached to a node.
       @param node The node to look up.
       @return All edges attached to that node.
    */
    public Set<GMLEdge> getAttachedEdges(GMLNode node) {
        return new HashSet<GMLEdge>(nodeToEdges.get(node));
    }

    /**
       Get all faces attached to an edge.
       @param edge The edge to look up.
       @return All faces attached to that edge.
    */
    public Set<GMLFace> getAttachedFaces(GMLEdge edge) {
        return new HashSet<GMLFace>(edgeToFaces.get(edge));
    }

    /**
       Register an edge attached to a face.
       @param face The GMLFace.
       @param edge The attached edge.
    */
    public void addAttachedEdge(GMLFace face, GMLEdge edge) {
        edgeToFaces.get(edge).add(face);
    }

    /**
       Deregister an edge attached to a face.
       @param face The GMLFace.
       @param edge The no longer attached edge.
    */
    public void removeAttachedEdge(GMLFace face, GMLEdge edge) {
        edgeToFaces.get(edge).remove(face);
    }

    /**
       Set the threshold for deciding if two points are the same. The {@link #ensureNodeNear(double, double)} method uses this value to check if a new node needs to be created.
       @param t The new threshold.
    */
    public void setNearbyNodeThreshold(double t) {
        threshold = t;
    }

    /**
       Get the threshold for deciding if two points are the same. The {@link #ensureNodeNear(double, double)} method uses this value to check if a new node needs to be created.
       @return The nearby node threshold.
    */
    public double getNearbyNodeThreshold() {
        return threshold;
    }

    /**
       Find out if a point is within the nearby node threshold of a node.
       @param point The point to test.
       @param node The node to test.
       @return True iff the point is within the nearby node threshold of the node.
    */
    public boolean isNear(Point2D point, GMLNode node) {
        return isNear(point.getX(), point.getY(), node);
    }

    /**
       Find out if a point is within the nearby node threshold of a node.
       @param x The x coordinate of the point.
       @param y The y coordinate of the point.
       @param node The node to test.
       @return True iff the point is within the nearby node threshold of the node.
    */
    public boolean isNear(double x, double y, GMLNode node) {
        double dx = node.getX() - x;
        double dy = node.getY() - y;
        return (dx >= - threshold &&
                dx <= threshold &&
                dy >= - threshold &&
                dy <= threshold);
    }

    /**
       Get or create the GMLNode near a point.
       @param point The point to look up.
       @return The existing GMLNode that is within threshold distance of the given point, or a new node if there is no existing nearby node.
    */
    public GMLNode ensureNodeNear(Point2D point) {
        return ensureNodeNear(point.getX(), point.getY());
    }

    /**
       Get or create the GMLNode near a point.
       @param x The X coordinate of the point to look up.
       @param y The Y coordinate of the point to look up.
       @return The existing GMLNode that is within threshold distance of the given point, or a new node if there is no existing nearby node.
    */
    public GMLNode ensureNodeNear(double x, double y) {
        for (GMLNode next : nodes.values()) {
            if (isNear(x, y, next)) {
                return next;
            }
        }
        return createNode(x, y);
    }

    /**
       Get or create the GML edge between two points. This method may also create new nodes.
       @param from The from point.
       @param to The to point.
       @return The existing edge between the two points, or a new edge if there is no existing edge.
    */
    public GMLEdge ensureEdge(Point2D from, Point2D to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to must not be null");
        }
        if (Double.isNaN(from.getX()) || Double.isNaN(from.getY()) || Double.isNaN(to.getX()) || Double.isNaN(to.getY())) {
            throw new IllegalArgumentException("From and to must be defined points");
        }
        GMLNode fromNode = ensureNodeNear(from.getX(), from.getY());
        GMLNode toNode = ensureNodeNear(to.getX(), to.getY());
        return ensureEdge(fromNode, toNode);
    }

    /**
       Get or create the GML edge between two nodes.
       @param from The from node.
       @param to The to node.
       @return The existing edge between the two points, or a new edge if there is no existing edge.
    */
    public GMLEdge ensureEdge(GMLNode from, GMLNode to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to must not be null");
        }
        if (from == to) {
            throw new IllegalArgumentException("From and to must be different");
        }
        for (GMLEdge next : edges.values()) {
            if ((next.getStart() == from && next.getEnd() == to) ||
                (next.getStart() == to && next.getEnd() == from)) {
                return next;
            }
        }
        return createEdge(from, to, false);
    }

    /**
       Create a GML directed edge between two nodes. This may create the underlying GMLEdge object if required.
       @param from The from node.
       @param to The to node.
       @return A new GMLDirectedEdge.
    */
    public GMLDirectedEdge ensureDirectedEdge(GMLNode from, GMLNode to) {
        GMLEdge edge = ensureEdge(from, to);
        return new GMLDirectedEdge(edge, edge.getStart() == from);
    }

    /**
       Create a GML directed edge between two points. This may create the underlying GMLEdge object if required.
       @param from The from point.
       @param to The to point.
       @return A new GMLDirectedEdge.
    */
    public GMLDirectedEdge ensureDirectedEdge(Point2D from, Point2D to) {
        GMLNode fromNode = ensureNodeNear(from);
        GMLNode toNode = ensureNodeNear(to);
        return ensureDirectedEdge(fromNode, toNode);
    }

    /**
       Create a GML directed edge from a node along an edge.
       @param from The from point.
       @param edge The edge that the directed edge should follow.
       @return A new GMLDirectedEdge.
    */
    public GMLDirectedEdge ensureDirectedEdge(GMLNode from, GMLEdge edge) {
        return new GMLDirectedEdge(edge, edge.getStart() == from);
    }

    /**
       Replace an existing edge with a set of new edges.
       @param oldEdge The edge to remove.
       @param newEdges The new edges.
    */
    public void replaceEdge(GMLEdge oldEdge, GMLEdge... newEdges) {
        replaceEdge(oldEdge, Arrays.asList(newEdges));
    }

    /**
       Replace an existing edge with a set of new edges.
       @param oldEdge The edge to remove.
       @param newEdges The new edges.
    */
    public void replaceEdge(GMLEdge oldEdge, Collection<GMLEdge> newEdges) {
        // Update faces
        for (GMLFace face : getAttachedFaces(oldEdge)) {
            face.replaceEdge(oldEdge, newEdges);
            removeAttachedEdge(face, oldEdge);
            for (GMLEdge next : newEdges) {
                addAttachedEdge(face, next);
            }
        }
        // Remove the old edge
        removeEdge(oldEdge);
    }

    private void calculateBounds() {
        if (boundsKnown) {
            return;
        }
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
        for (GMLNode node : nodes.values()) {
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }
        for (GMLEdge edge : edges.values()) {
            for (Coordinates co : edge.getPoints()) {
                minX = Math.min(minX, co.getX());
                maxX = Math.max(maxX, co.getX());
                minY = Math.min(minY, co.getY());
                maxY = Math.max(maxY, co.getY());
            }
        }
        boundsKnown = true;
    }

    private GMLNode processNode(Element e) {
        long id = Long.parseLong(e.attributeValue(GML_ID_QNAME));
        // Find the coordinates
        String coordinates = ((Text)NODE_COORDINATES_PATH.evaluate(e)).getText();
        int index = coordinates.indexOf(",");
        String first = coordinates.substring(0, index).trim();
        String second = coordinates.substring(index + 1).trim();
        return new GMLNode(id, Long.parseLong(first), Long.parseLong(second));
    }

    private GMLEdge processEdge(Element e) {
        long id = Long.parseLong(e.attributeValue(GML_ID_QNAME));
        // Find the nodes
        String startID = ((Attribute)EDGE_START_PATH.evaluate(e)).getValue();
        String endID = ((Attribute)EDGE_END_PATH.evaluate(e)).getValue();
        GMLNode start = getNode(Long.parseLong(startID.substring(1)));
        GMLNode end = getNode(Long.parseLong(endID.substring(1)));
        // Find the coordinates
        String coordinatesString = ((Text)EDGE_COORDINATES_PATH.evaluate(e)).getText();
        List<Coordinates> coordinates = new ArrayList<Coordinates>();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
        while (tokens.hasMoreTokens()) {
            String next = tokens.nextToken();
            int index = next.indexOf(",");
            String first = next.substring(0, index).trim();
            String second = next.substring(index + 1).trim();
            coordinates.add(new Coordinates(Long.parseLong(first), Long.parseLong(second)));
        }
        return new GMLEdge(id, start, end, false, coordinates);
    }

    private GMLFace processFace(Element e) {
        long id = Long.parseLong(e.attributeValue(GML_ID_QNAME));
        String typeString = e.attributeValue(RCR_TYPE_QNAME);
        if (typeString == null) {
            typeString = "ROAD";
        }
        FaceType type = FaceType.valueOf(typeString.toUpperCase());
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        List<Coordinates> outline = new ArrayList<Coordinates>();
        for (Object next : FACE_EDGE_PATH.selectNodes(e)) {
            Element nextEdge = (Element)next;
            String orientation = nextEdge.attributeValue(GML_ORIENTATION_QNAME);
            String edgeID = nextEdge.attributeValue(XLINK_HREF_QNAME).substring(1);
            GMLDirectedEdge edge = new GMLDirectedEdge(getEdge(Long.parseLong(edgeID)), orientation.equals("+"));
            edges.add(edge);
        }
        String coordinatesString = ((Text)FACE_SHAPE_PATH.evaluate(e)).getText();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
        while (tokens.hasMoreTokens()) {
            String next = tokens.nextToken();
            int index = next.indexOf(",");
            String first = next.substring(0, index).trim();
            String second = next.substring(index + 1).trim();
            outline.add(new Coordinates(Long.parseLong(first), Long.parseLong(second)));
        }
        return new GMLFace(id, type, edges, outline);
    }
}