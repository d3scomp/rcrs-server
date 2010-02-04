package gis2;

import kernel.WorldModelCreator;
import kernel.KernelException;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Edge;

import maps.gml.GMLMap;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;
import maps.gml.GMLCoordinates;
import maps.gml.MapReader;
import maps.gml.GMLException;
import maps.CoordinateConversion;
import maps.ScaleConversion;
import maps.MapTools;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.math.BigDecimal;
import java.math.MathContext;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.File;

import rescuecore2.misc.gui.ShapeDebugFrame;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A WorldModelCreator that reads a GML file and scenario descriptor.
*/
public class GMLWorldModelCreator implements WorldModelCreator {
    private static final Log LOG = LogFactory.getLog(GMLWorldModelCreator.class);

    private static final String MAP_FILE_KEY = "gis.map";
    private static final String SCENARIO_FILE_KEY = "gis.scenario";

    private static final double AREA_SCALE_FACTOR = 1.0 / 10000.0;

    private ShapeDebugFrame debug;

    private int nextID;

    @Override
    public String toString() {
        return "GML world model creator";
    }

    @Override
    public WorldModel<? extends Entity> buildWorldModel(Config config) throws KernelException {
        try {
            StandardWorldModel result = new StandardWorldModel();
            readMapData(config, result);
            readScenarioData(config, result);
            for (Entity e : result) {
                nextID = Math.max(nextID, e.getID().getValue());
            }
            ++nextID;
            return result;
        }
        catch (GMLException e) {
            throw new KernelException("Couldn't read GML file", e);
        }
        catch (DocumentException e) {
            throw new KernelException("Couldn't read scenario file", e);
        }
    }

    @Override
    public EntityID generateID() {
        return new EntityID(nextID++);
    }

    private GMLMap readMap(String fileName) throws GMLException {
        return MapReader.readGMLMap(fileName);
    }

    private void readMapData(Config config, StandardWorldModel result) throws GMLException {
        GMLMap map = readMap(config.getValue(MAP_FILE_KEY));
        // Convert lat/lon to millimeters
        double scale = 1000.0 / MapTools.sizeOf1Metre((map.getMinX() + map.getMaxX()) / 2, (map.getMinY() + map.getMaxY()) / 2);
        CoordinateConversion conversion = new ScaleConversion(map.getMinX(), map.getMinY(), scale, scale);
        for (GMLBuilding next : map.getBuildings()) {
            // Create a new Building entity
            EntityID id = new EntityID(next.getID());
            Building b = new Building(id);
            // Building properties
            b.setFloors(1);
            b.setFieryness(0);
            b.setBrokenness(0);
            b.setBuildingCode(0);
            b.setBuildingAttributes(0);
            double area = computeArea(next, conversion);
            b.setGroundArea((int)Math.abs(area * AREA_SCALE_FACTOR));
            b.setTotalArea((int)Math.abs(area * AREA_SCALE_FACTOR));
            b.setImportance(1);
            // Area properties
            b.setEdges(createEdges(next, conversion));
            b.setX((int)computeCentroidX(next, conversion, area));
            b.setY((int)computeCentroidY(next, conversion, area));
            result.addEntity(b);
            //                LOG.debug(b.getFullDescription());
        }
        for (GMLRoad next : map.getRoads()) {
            // Create a new Road entity
            EntityID id = new EntityID(next.getID());
            Road r = new Road(id);
            // Road properties: None
            // Area properties
            r.setX((int)conversion.convertX(next.getCentreX()));
            r.setY((int)conversion.convertY(next.getCentreY()));
            r.setEdges(createEdges(next, conversion));
            result.addEntity(r);
            //                LOG.debug(b.getFullDescription());
        }
    }

    private void readScenarioData(Config config, StandardWorldModel result) throws DocumentException {
        String fileName = config.getValue(SCENARIO_FILE_KEY);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File(fileName));
        Scenario scenario = new Scenario(doc);
        scenario.apply(result);
    }

    private List<Edge> createEdges(GMLShape s, CoordinateConversion conversion) {
        LOG.debug("Computing edges for " + s);
        List<Edge> result = new ArrayList<Edge>();
        for (GMLDirectedEdge edge : s.getEdges()) {
            GMLCoordinates start = edge.getStartCoordinates();
            GMLCoordinates end = edge.getEndCoordinates();
            Integer neighbourID = s.getNeighbour(edge);
            EntityID id = neighbourID == null ? null : new EntityID(neighbourID);
            LOG.debug("Edge: " + start + " -> " + end);
            double sx = conversion.convertX(start.getX());
            double sy = conversion.convertY(start.getY());
            double ex = conversion.convertX(end.getX());
            double ey = conversion.convertY(end.getY());
            LOG.debug("Scaled edge: " + sx + "," + sy + " -> " + ex + "," + ey);
            result.add(new Edge((int)sx,
                                (int)sy,
                                (int)ex,
                                (int)ey,
                                id));
        }
        return result;
    }

    private double computeArea(GMLShape shape, CoordinateConversion conversion) {
        Iterator<GMLCoordinates> it = shape.getCoordinates().iterator();
        GMLCoordinates last = it.next();
        GMLCoordinates first = last;
        double sum = 0;
        while (it.hasNext()) {
            GMLCoordinates next = it.next();
            double lastX = conversion.convertX(last.getX());
            double lastY = conversion.convertY(last.getY());
            double nextX = conversion.convertX(next.getX());
            double nextY = conversion.convertY(next.getY());
            sum += (lastX * nextY) - (nextX * lastY);
            last = next;
        }
        double lastX = conversion.convertX(last.getX());
        double lastY = conversion.convertY(last.getY());
        double nextX = conversion.convertX(first.getX());
        double nextY = conversion.convertY(first.getY());
        sum += (lastX * nextY) - (nextX * lastY);
        sum /= 2.0;
        LOG.debug("Area of " + shape + ": " + Math.abs(sum));
        return sum;
    }

    private double computeCentroidX(GMLShape shape, CoordinateConversion conversion, double area) {
        Iterator<GMLCoordinates> it = shape.getCoordinates().iterator();
        GMLCoordinates last = it.next();
        GMLCoordinates first = last;
        double sum = 0;
        while (it.hasNext()) {
            GMLCoordinates next = it.next();
            double lastX = conversion.convertX(last.getX());
            double lastY = conversion.convertY(last.getY());
            double nextX = conversion.convertX(next.getX());
            double nextY = conversion.convertY(next.getY());
            sum += (lastX + nextX) * ((lastX * nextY) - (nextX * lastY));
            last = next;
        }
        double lastX = conversion.convertX(last.getX());
        double lastY = conversion.convertY(last.getY());
        double nextX = conversion.convertX(first.getX());
        double nextY = conversion.convertY(first.getY());
        sum += (lastX + nextX) * ((lastX * nextY) - (nextX * lastY));
        // CHECKSTYLE:OFF:MagicNumber
        sum /= 6.0 * area;
        // CHECKSTYLE:ON:MagicNumber
        LOG.debug("X centroid of " + shape + ": " + sum);
        return sum;        
    }

    private double computeCentroidY(GMLShape shape, CoordinateConversion conversion, double area) {
        Iterator<GMLCoordinates> it = shape.getCoordinates().iterator();
        GMLCoordinates last = it.next();
        GMLCoordinates first = last;
        double sum = 0;
        while (it.hasNext()) {
            GMLCoordinates next = it.next();
            double lastX = conversion.convertX(last.getX());
            double lastY = conversion.convertY(last.getY());
            double nextX = conversion.convertX(next.getX());
            double nextY = conversion.convertY(next.getY());
            sum += (lastY + nextY) * ((lastX * nextY) - (nextX * lastY));
            last = next;
        }
        double lastX = conversion.convertX(last.getX());
        double lastY = conversion.convertY(last.getY());
        double nextX = conversion.convertX(first.getX());
        double nextY = conversion.convertY(first.getY());
        sum += (lastY + nextY) * ((lastX * nextY) - (nextX * lastY));
        // CHECKSTYLE:OFF:MagicNumber
        sum /= 6.0 * area;
        // CHECKSTYLE:ON:MagicNumber
        LOG.debug("Y centroid of " + shape + ": " + sum);
        return sum;        
    }
}