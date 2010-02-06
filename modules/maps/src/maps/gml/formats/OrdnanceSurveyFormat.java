package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.MapFormat;
import maps.gml.CoordinateSystem;
import maps.gml.debug.GMLShapeInfo;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.XPath;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.awt.Color;

import rescuecore2.misc.gui.ShapeDebugFrame;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

// TO DO: Handle inner boundaries

/**
   A MapFormat that can handle maps from the UK Ordnance Survey.
 */
public final class OrdnanceSurveyFormat implements MapFormat {
    private static final Log LOG = LogFactory.getLog(OrdnanceSurveyFormat.class);

    private static final String FEATURE_CODE_BUILDING = "10021";
    private static final String FEATURE_CODE_ROAD = "10172";
    private static final String FEATURE_CODE_FOOTPATH = "10183";

    private static final String FEATURE_CODE_OPEN_SPACE = "10053";
    private static final String FEATURE_CODE_GENERAL_SPACE = "10056";

    private static final String OSGB_NAMESPACE_URI = "http://www.ordnancesurvey.co.uk/xml/namespaces/osgb";

    private static final Namespace OSGB_NAMESPACE = DocumentHelper.createNamespace("osgb", OSGB_NAMESPACE_URI);

    private static final QName FEATURE_COLLECTION_QNAME = DocumentHelper.createQName("FeatureCollection", OSGB_NAMESPACE);
    private static final QName TOPOGRAPHIC_AREA_QNAME = DocumentHelper.createQName("TopographicArea", OSGB_NAMESPACE);

    private static final XPath BUILDING_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_BUILDING + "']]");
    private static final XPath ROAD_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_ROAD + "' or text()='" + FEATURE_CODE_FOOTPATH + "']]");
    private static final XPath SPACE_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_OPEN_SPACE + "' or text()='" + FEATURE_CODE_GENERAL_SPACE + "']]");
    private static final XPath SHAPE_XPATH = DocumentHelper.createXPath("osgb:polygon/gml:Polygon/gml:outerBoundaryIs/gml:LinearRing/gml:coordinates");

    // Map from uri prefix to uri for XPath expressions
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final Color NEW_BUILDING_COLOUR = new Color(255, 0, 0, 128);
    private static final Color NEW_ROAD_COLOUR = new Color(255, 0, 0, 128);
    private static final Color BUILDING_COLOUR = new Color(0, 128, 0, 128);
    private static final Color ROAD_COLOUR = new Color(128, 128, 128, 128);

    private static final int FID_PREFIX_LENGTH = 4;

    private ShapeDebugFrame debug;
    private List<GMLShapeInfo> background;

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("osgb", OSGB_NAMESPACE_URI);

        BUILDING_XPATH.setNamespaceURIs(URIS);
        ROAD_XPATH.setNamespaceURIs(URIS);
        SPACE_XPATH.setNamespaceURIs(URIS);
        SHAPE_XPATH.setNamespaceURIs(URIS);
    }

    /**
       Construct a new OrdnanceSurveyFormat instance.
    */
    public OrdnanceSurveyFormat() {
        debug = new ShapeDebugFrame();
        background = new ArrayList<GMLShapeInfo>();
        debug.setBackground(background);
    }

    @Override
    public String toString() {
        return "Ordnance survey";
    }

    @Override
    public boolean looksValid(Document doc) {
        Element root = doc.getRootElement();
        return root.getQName().equals(FEATURE_COLLECTION_QNAME);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap(CoordinateSystem.M);
        readBuildings(doc, result);
        readRoads(doc, result);
        readSpaces(doc, result);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        // Not implemented
        throw new RuntimeException("OrdnanceSurveyFormat.write not implemented");
    }

    private void readBuildings(Document doc, GMLMap result) {
        for (Object next : BUILDING_XPATH.selectNodes(doc)) {
            LOG.debug("Found building element: " + next);
            Element e = (Element)next;
            //            String fid = e.attributeValue("fid");
            //            long id = Long.parseLong(fid.substring(FID_PREFIX_LENGTH)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            List<GMLDirectedEdge> edges = readEdges(coordinatesString, result);
            GMLBuilding b = result.createBuilding(edges);
            debug.show("New building", new GMLShapeInfo(b, "New building", Color.BLACK, NEW_BUILDING_COLOUR));
            background.add(new GMLShapeInfo(b, "Buildings", Color.BLACK, BUILDING_COLOUR));
        }
    }

    private void readRoads(Document doc, GMLMap result) {
        debug.activate();
        for (Object next : ROAD_XPATH.selectNodes(doc)) {
            LOG.debug("Found road element: " + next);
            Element e = (Element)next;
            //            String fid = e.attributeValue("fid");
            //            long id = Long.parseLong(fid.substring(FID_PREFIX_LENGTH)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            List<GMLDirectedEdge> edges = readEdges(coordinatesString, result);
            GMLRoad road = result.createRoad(edges);
            debug.show("New road", new GMLShapeInfo(road, "New road", Color.BLACK, NEW_ROAD_COLOUR));
            background.add(new GMLShapeInfo(road, "Roads", Color.BLACK, ROAD_COLOUR));
        }
        debug.deactivate();
    }

    private void readSpaces(Document doc, GMLMap result) {
        /*
        for (Object next : SPACE_XPATH.selectNodes(doc)) {
            LOG.debug("Found space element: " + next);
            Element e = (Element)next;
            String fid = e.attributeValue("fid");
            long id = Long.parseLong(fid.substring(4)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            List<GMLEdge> edges = readEdges(coordinatesString, result);
            result.createSpace(edges);
        }
        */
    }

    private List<GMLDirectedEdge> readEdges(String coordinatesString, GMLMap map) {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
        GMLCoordinates lastApex = null;
        GMLNode fromNode = null;
        GMLNode toNode = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            GMLCoordinates nextApex = new GMLCoordinates(token);
            toNode = map.createNode(nextApex);
            if (lastApex != null) {
                edges.add(new GMLDirectedEdge(map.createEdge(fromNode, toNode), true));
            }
            lastApex = nextApex;
            fromNode = toNode;
        }
        return edges;
    }
}