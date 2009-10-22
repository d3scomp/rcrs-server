package maps.osm;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

public class OSMMap {
    private final static Collection<String> ROAD_MARKERS = new HashSet<String>();

    static {
        ROAD_MARKERS.add("motorway");
        ROAD_MARKERS.add("motorway_link");
        ROAD_MARKERS.add("trunk");
        ROAD_MARKERS.add("trunk_link");
        ROAD_MARKERS.add("primary");
        ROAD_MARKERS.add("primary_link");
        ROAD_MARKERS.add("secondary");
        ROAD_MARKERS.add("secondary_link");
        ROAD_MARKERS.add("tertiary");
        ROAD_MARKERS.add("unclassified");
        ROAD_MARKERS.add("road");
        ROAD_MARKERS.add("residential");
        ROAD_MARKERS.add("living_street");
        ROAD_MARKERS.add("service");
        ROAD_MARKERS.add("track");
        ROAD_MARKERS.add("services");
        ROAD_MARKERS.add("pedestrian");
    }

    private Map<Long, OSMNode> nodes;
    private Map<Long, OSMRoad> roads;
    private Map<Long, OSMBuilding> buildings;

    private boolean boundsCalculated;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;

    public OSMMap() {
        boundsCalculated = false;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
    }

    public OSMMap(Document doc) throws OSMException {
        this();
        read(doc);
    }

    public OSMMap(File file) throws OSMException, DocumentException, IOException {
        this();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        read(doc);
    }

    public OSMMap(OSMMap other, double minLat, double minLon, double maxLat, double maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        boundsCalculated = true;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
        // Copy all nodes inside the bounds
        for (OSMNode next : other.nodes.values()) {
            double lat = next.getLatitude();
            double lon = next.getLongitude();
            long id = next.getID();
            if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon) {
                this.nodes.put(id, new OSMNode(id, lat, lon));
            }
        }
        // Now copy the bits of roads and buildings that do not have missing nodes
        for (OSMRoad next : other.roads.values()) {
            List<Long> ids = new ArrayList<Long>(next.getNodeIDs());
            for (Iterator<Long> it = ids.iterator(); it.hasNext();) {
                Long nextID = it.next();
                if (!nodes.containsKey(nextID)) {
                    it.remove();
                }
            }
            if (!ids.isEmpty()) {
                roads.put(next.getID(), new OSMRoad(next.getID(), ids));
            }
        }
        for (OSMBuilding next : other.buildings.values()) {
            boolean allFound = true;
            for (Long nextID : next.getNodeIDs()) {
                if (!nodes.containsKey(nextID)) {
                    allFound = false;
                }
            }
            if (allFound) {
                buildings.put(next.getID(), new OSMBuilding(next.getID(), new ArrayList<Long>(next.getNodeIDs())));
            }
        }
    }

    public void read(Document doc) throws OSMException {
        boundsCalculated = false;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
        Element root = doc.getRootElement();
        if (!"osm".equals(root.getName())) {
            throw new OSMException("Invalid map file: root element must be 'osm', not " + root.getName());
        }
        for (Object next : root.elements("node")) {
            Element e = (Element)next;
            OSMNode node = processNode(e);
        }
        for (Object next : root.elements("way")) {
            Element e = (Element)next;
            processWay(e);
        }
    }

    public Document toXML() {
        Element root = DocumentHelper.createElement("osm");
        Element bounds = root.addElement("bounds");
        calculateBounds();
        bounds.addAttribute("minlat", String.valueOf(minLat));
        bounds.addAttribute("maxlat", String.valueOf(maxLat));
        bounds.addAttribute("minlon", String.valueOf(minLon));
        bounds.addAttribute("maxlon", String.valueOf(maxLon));
        for (OSMNode next : nodes.values()) {
            Element node = root.addElement("node");
            node.addAttribute("id", String.valueOf(next.getID()));
            node.addAttribute("lat", String.valueOf(next.getLatitude()));
            node.addAttribute("lon", String.valueOf(next.getLongitude()));
        }
        for (OSMRoad next : roads.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getID()));
            for (Long nextID : next.getNodeIDs()) {
                node.addElement("nd").addAttribute("ref", String.valueOf(nextID));
            }
            node.addElement("tag").addAttribute("k", "highway").addAttribute("v", "primary");
        }
        for (OSMBuilding next : buildings.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getID()));
            for (Long nextID : next.getNodeIDs()) {
                node.addElement("nd").addAttribute("ref", String.valueOf(nextID));
            }
            node.addElement("tag").addAttribute("k", "building").addAttribute("v", "yes");
        }
        return DocumentHelper.createDocument(root);
    }

    public double getMinLongitude() {
        calculateBounds();
        return minLon;
    }

    public double getMaxLongitude() {
        calculateBounds();
        return maxLon;
    }

    public double getCentreLongitude() {
        calculateBounds();
        return (maxLon + minLon) / 2;
    }

    public double getMinLatitude() {
        calculateBounds();
        return minLat;
    }

    public double getMaxLatitude() {
        calculateBounds();
        return maxLat;
    }

    public double getCentreLatitude() {
        calculateBounds();
        return (maxLat + minLat) / 2;
    }

    public Collection<OSMNode> getNodes() {
        return new HashSet<OSMNode>(nodes.values());
    }

    public void removeNode(OSMNode node) {
        nodes.remove(node.getID());
    }

    public OSMNode getNode(Long id) {
        return nodes.get(id);
    }

    public OSMNode getNearestNode(double lat, double lon) {
        double smallest = Double.MAX_VALUE;
        OSMNode best = null;
        for (OSMNode next : nodes.values()) {
            double d1 = next.getLatitude() - lat;
            double d2 = next.getLongitude() - lon;
            double d = (d1 * d1) + (d2 * d2);
            if (d < smallest) {
                best = next;
                smallest = d;
            }
        }
        return best;
    }

    public void replaceNode(OSMNode old, OSMNode replacement) {
        for (OSMRoad r : roads.values()) {
            r.replace(old.getID(), replacement.getID());
        }
        for (OSMBuilding b : buildings.values()) {
            b.replace(old.getID(), replacement.getID());
        }
        removeNode(old);
    }

    public Collection<OSMRoad> getRoads() {
        return new HashSet<OSMRoad>(roads.values());
    }

    public void removeRoad(OSMRoad road) {
        roads.remove(road.getID());
    }

    public Collection<OSMBuilding> getBuildings() {
        return new HashSet<OSMBuilding>(buildings.values());
    }

    public void removeBuilding(OSMBuilding building) {
        buildings.remove(building.getID());
    }

    private void calculateBounds() {
        if (boundsCalculated) {
            return;
        }
        minLat = Double.POSITIVE_INFINITY;
        maxLat = Double.NEGATIVE_INFINITY;
        minLon = Double.POSITIVE_INFINITY;
        maxLon = Double.NEGATIVE_INFINITY;
        for (OSMNode node : nodes.values()) {
            minLat = Math.min(minLat, node.getLatitude());
            maxLat = Math.max(maxLat, node.getLatitude());
            minLon = Math.min(minLon, node.getLongitude());
            maxLon = Math.max(maxLon, node.getLongitude());
        }
        boundsCalculated = true;
    }

    private OSMNode processNode(Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        double lat = Double.parseDouble(e.attributeValue("lat"));
        double lon = Double.parseDouble(e.attributeValue("lon"));
        OSMNode node = new OSMNode(id, lat, lon);
        nodes.put(id, node);
        return node;
    }

    private void processWay(Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        List<Long> ids = new ArrayList<Long>();
        for (Object next : e.elements("nd")) {
            Element nd = (Element)next;
            Long nextID = Long.parseLong(nd.attributeValue("ref"));
            ids.add(nextID);
        }
        // Is this way a road or a building?
        boolean road = false;
        boolean building = false;
        for (Object next : e.elements("tag")) {
            Element tag = (Element)next;
            building = building || tagSignifiesBuilding(tag);
            road = road || tagSignifiesRoad(tag);
        }
        if (building) {
            buildings.put(id, new OSMBuilding(id, ids));
        }
        else if (road) {
            roads.put(id, new OSMRoad(id, ids));
        }
    }

    private boolean tagSignifiesRoad(Element tag) {
        String key = tag.attributeValue("k");
        String value = tag.attributeValue("v");
        if (!key.equals("highway")) {
            return false;
        }
        return ROAD_MARKERS.contains(value);
    }

    private boolean tagSignifiesBuilding(Element tag) {
        String key = tag.attributeValue("k");
        String value = tag.attributeValue("v");
        if (key.equals("building")) {
            return "yes".equals(value);
        }
        if (key.equals("rcr:building")) {
            return "1".equals(value);
        }
        return false;
    }
}