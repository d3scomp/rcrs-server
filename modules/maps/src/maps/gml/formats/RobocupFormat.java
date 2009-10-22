package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLCoordinates;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLTools;
import maps.gml.MapFormat;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.QName;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.DocumentHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

public final class RobocupFormat implements MapFormat {
    private static final String RCR_NAMESPACE_URI = "urn:roborescue:map:gml";
    private static final Namespace RCR_NAMESPACE = DocumentHelper.createNamespace("rcr", RCR_NAMESPACE_URI);

    private static final QName RCR_ROOT_QNAME = DocumentHelper.createQName("map", RCR_NAMESPACE);
    private static final QName RCR_BUILDING_QNAME = DocumentHelper.createQName("building", RCR_NAMESPACE);
    private static final QName RCR_ROAD_QNAME = DocumentHelper.createQName("road", RCR_NAMESPACE);

    private static final QName RCR_TYPE_QNAME = DocumentHelper.createQName("type", RCR_NAMESPACE);
    private static final QName RCR_PASSABLE_QNAME = DocumentHelper.createQName("passable", RCR_NAMESPACE);

    // Map from uri prefix to uri for writing XML documents
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final XPath BUILDING_XPATH = DocumentHelper.createXPath("//rcr:building");
    private static final XPath ROAD_XPATH = DocumentHelper.createXPath("//rcr:road");
    private static final XPath SHAPE_XPATH = DocumentHelper.createXPath("gml:polygon/gml:LinearRing/gml:coordinates/text()");


    /*
    // Node-related XPath expressions
    private static final XPath NODE_PATH = DocumentHelper.createXPath("//gml:Node");
    private static final XPath NODE_COORDINATES_PATH = DocumentHelper.createXPath("gml:pointProperty/gml:Point/gml:coordinates/text()");
    private static final XPath EDGE_START_PATH = DocumentHelper.createXPath("gml:directedNode[@orientation='-']/@xlink:href");
    private static final XPath EDGE_END_PATH = DocumentHelper.createXPath("gml:directedNode[@orientation='+']/@xlink:href");

    // Edge-related XPath expressions
    private static final XPath EDGE_PATH = DocumentHelper.createXPath("//gml:Edge");
    private static final XPath EDGE_COORDINATES_PATH = DocumentHelper.createXPath("gml:centerLineOf/gml:LineString/gml:coordinates/text()");

    // Face-related XPath expressions
    private static final XPath FACE_PATH = DocumentHelper.createXPath("gml:Face");
    private static final XPath FACE_EDGE_PATH = DocumentHelper.createXPath("gml:directedEdge");
    */

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("rcr", RCR_NAMESPACE_URI);

        BUILDING_XPATH.setNamespaceURIs(URIS);
        ROAD_XPATH.setNamespaceURIs(URIS);
        SHAPE_XPATH.setNamespaceURIs(URIS);

        /*
        NODE_PATH.setNamespaceURIs(URIS);
        NODE_COORDINATES_PATH.setNamespaceURIs(URIS);
        EDGE_PATH.setNamespaceURIs(URIS);
        EDGE_START_PATH.setNamespaceURIs(URIS);
        EDGE_END_PATH.setNamespaceURIs(URIS);
        EDGE_COORDINATES_PATH.setNamespaceURIs(URIS);
        FACE_PATH.setNamespaceURIs(URIS);
        FACE_EDGE_PATH.setNamespaceURIs(URIS);
        */
    }

    public RobocupFormat() {
    }

    @Override
    public String toString() {
        return "Robocup rescue";
    }

    @Override
    public boolean looksValid(Document doc) {
        Element root = doc.getRootElement();
        return root.getQName().equals(RCR_ROOT_QNAME);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap();
        readBuildings(doc, result);
        readRoads(doc, result);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        Element root = DocumentHelper.createElement(RCR_ROOT_QNAME);
        Document result = DocumentHelper.createDocument(root);
        writeBuildings(map, root);
        writeRoads(map, root);
        return result;
    }

    private void writeBuildings(GMLMap map, Element parent) {
        for (GMLBuilding next : map.getBuildings()) {
            Element b = parent.addElement(RCR_BUILDING_QNAME);
            // ID
            b.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            // Shape
            b.addElement(Common.GML_POLYGON_QNAME).addElement(Common.GML_LINEAR_RING_QNAME).addElement(Common.GML_COORDINATES_QNAME).setText(GMLTools.getCoordinatesString(next.getCoordinates()));
        }
    }

    private void writeRoads(GMLMap map, Element parent) {
        for (GMLRoad next : map.getRoads()) {
            Element r = parent.addElement(RCR_ROAD_QNAME);
            // ID
            r.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            // Shape
            r.addElement(Common.GML_POLYGON_QNAME).addElement(Common.GML_LINEAR_RING_QNAME).addElement(Common.GML_COORDINATES_QNAME).setText(GMLTools.getCoordinatesString(next.getCoordinates()));
        }
    }

    private void readBuildings(Document doc, GMLMap result) {
        for (Object next : BUILDING_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            String idString = e.attributeValue(Common.GML_ID_QNAME);
            long id = Long.parseLong(idString);
            // Find the boundary shape
            List<GMLCoordinates> outline = new ArrayList<GMLCoordinates>();
            String coordinatesString = ((Text)SHAPE_XPATH.evaluate(e)).getText();
            StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                int index = token.indexOf(",");
                String first = token.substring(0, index).trim();
                String second = token.substring(index + 1).trim();
                outline.add(new GMLCoordinates(Double.parseDouble(first), Double.parseDouble(second)));
            }
            result.addBuilding(new GMLBuilding(id, outline));
        }
    }

    private void readRoads(Document doc, GMLMap result) {
        for (Object next : ROAD_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            String idString = e.attributeValue(Common.GML_ID_QNAME);
            long id = Long.parseLong(idString);
            // Find the boundary shape
            List<GMLCoordinates> outline = new ArrayList<GMLCoordinates>();
            String coordinatesString = ((Text)SHAPE_XPATH.evaluate(e)).getText();
            StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                int index = token.indexOf(",");
                String first = token.substring(0, index).trim();
                String second = token.substring(index + 1).trim();
                outline.add(new GMLCoordinates(Double.parseDouble(first), Double.parseDouble(second)));
            }
            result.addRoad(new GMLRoad(id, outline));
        }
    }


    /*
    private GMLNode processNode(Element e) {
        long id = Long.parseLong(e.attributeValue(GML_ID_QNAME));
        // Find the coordinates
        String coordinates = ((Text)NODE_COORDINATES_PATH.evaluate(e)).getText();
        int index = coordinates.indexOf(",");
        String first = coordinates.substring(0, index).trim();
        String second = coordinates.substring(index + 1).trim();
        return new GMLNode(id, Double.parseDouble(first), Double.parseDouble(second));
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
        List<GMLCoordinates> coordinates = new ArrayList<GMLCoordinates>();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
        while (tokens.hasMoreTokens()) {
            String next = tokens.nextToken();
            int index = next.indexOf(",");
            String first = next.substring(0, index).trim();
            String second = next.substring(index + 1).trim();
            coordinates.add(new GMLCoordinates(Double.parseDouble(first), Double.parseDouble(second)));
        }
        String passableString = e.attributeValue(RCR_PASSABLE_QNAME);
        boolean passable = "true".equals(passableString);
        return new GMLEdge(id, start, end, passable, coordinates);
    }

    private GMLFace processFace(Element e) {
        long id = Long.parseLong(e.attributeValue(GML_ID_QNAME));
        String typeString = e.attributeValue(RCR_TYPE_QNAME);
        if (typeString == null) {
            typeString = "ROAD";
        }
        FaceType type = FaceType.valueOf(typeString.toUpperCase());
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        List<GMLCoordinates> outline = new ArrayList<GMLCoordinates>();
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
            outline.add(new GMLCoordinates(Double.parseDouble(first), Double.parseDouble(second)));
        }
        return new GMLFace(id, type, edges, outline);
    }

    private void createNodeXML(Element parent, CoordinateConversion convert) {
        for (GMLNode node : nodes.values()) {
            Element nodeElement = parent.addElement(GML_NODE_QNAME);
            nodeElement.addAttribute(GML_ID_QNAME, String.valueOf(node.getID()));
            Element coord = nodeElement.addElement(GML_POINT_PROPERTY_QNAME).addElement(GML_POINT_QNAME).addElement(GML_COORDINATES_QNAME);
            coord.setText(convert.convertX(node.getX()) + "," + convert.convertY(node.getY()));
        }
    }

    private void createEdgeXML(Element parent, CoordinateConversion convert) {
        for (GMLEdge edge : edges.values()) {
            Element edgeElement = parent.addElement(GML_EDGE_QNAME);
            edgeElement.addAttribute(GML_ID_QNAME, String.valueOf(edge.getID()));
            edgeElement.addAttribute(RCR_PASSABLE_QNAME, edge.isPassable() ? "true" : "false");
            Element dNode = edgeElement.addElement(GML_DIRECTED_NODE_QNAME);
            dNode.addAttribute(GML_ORIENTATION_QNAME, "-");
            dNode.addAttribute(XLINK_HREF_QNAME, "#" + edge.getStart().getID());
            dNode = edgeElement.addElement(GML_DIRECTED_NODE_QNAME);
            dNode.addAttribute(GML_ORIENTATION_QNAME, "+");
            dNode.addAttribute(XLINK_HREF_QNAME, "#" + edge.getEnd().getID());
            StringBuilder coordString = new StringBuilder();
            coordString.append(String.valueOf(convert.convertX(edge.getStart().getX())));
            coordString.append(",");
            coordString.append(String.valueOf(convert.convertY(edge.getStart().getY())));
            coordString.append(" ");
            coordString.append(String.valueOf(convert.convertX(edge.getEnd().getX())));
            coordString.append(",");
            coordString.append(String.valueOf(convert.convertY(edge.getEnd().getY())));
            edgeElement.addElement(GML_CENTRE_LINE_OF_QNAME).addElement(GML_LINE_STRING_QNAME).addElement(GML_COORDINATES_QNAME).setText(coordString.toString());
            // Look for faces connected to this edge
            for (GMLFace face : edgeToFaces.get(edge)) {
                Element dFace = edgeElement.addElement(GML_DIRECTED_FACE_QNAME);
                String direction = "+";
                if (face.isConnectedRight(edge)) {
                    direction = "-";
                }
                dFace.addAttribute(GML_ORIENTATION_QNAME, direction);
                dFace.addAttribute(XLINK_HREF_QNAME, "#" + face.getID());
            }
        }
    }

    private void createFaceXML(Element parent, CoordinateConversion convert) {
        for (GMLFace face : faces.values()) {
            Element faceElement = parent.addElement(GML_FACE_QNAME);
            faceElement.addAttribute(GML_ID_QNAME, String.valueOf(face.getID()));
            if (face.getFaceType() != null) {
                faceElement.addAttribute(RCR_TYPE_QNAME, face.getFaceType().toString());
            }
            StringBuilder coordString = new StringBuilder();
            for (Iterator<GMLDirectedEdge> it = face.getEdges().iterator(); it.hasNext();) {
                GMLDirectedEdge edge = it.next();
                Element dEdge = faceElement.addElement(GML_DIRECTED_EDGE_QNAME);
                dEdge.addAttribute(GML_ORIENTATION_QNAME, (edge.isForward() ? "+" : "-"));
                dEdge.addAttribute(XLINK_HREF_QNAME, "#" + edge.getEdge().getID());
                coordString.append(String.valueOf(convert.convertX(edge.getStartNode().getX())));
                coordString.append(",");
                coordString.append(String.valueOf(convert.convertY(edge.getStartNode().getY())));
                coordString.append(" ");
                if (!it.hasNext()) {
                    coordString.append(String.valueOf(convert.convertX(edge.getEndNode().getX())));
                    coordString.append(",");
                    coordString.append(String.valueOf(convert.convertY(edge.getEndNode().getY())));
                    coordString.append(" ");
                }
            }
            faceElement.addElement(GML_POLYGON_QNAME).addElement(GML_LINEAR_RING_QNAME).addElement(GML_COORDINATES_QNAME).setText(coordString.toString());
        }
    }
    */
}