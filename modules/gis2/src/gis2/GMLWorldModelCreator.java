package gis2;

import kernel.WorldModelCreator;
import kernel.KernelException;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.GeometryTools2D;

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
import maps.ConstantConversion;
import maps.IdentityConversion;
import maps.MapTools;

import java.util.List;
import java.util.ArrayList;

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

    private static final String MAP_DIRECTORY_KEY = "gis.map.dir";
    private static final String MAP_FILE_KEY = "gis.map.file";
    private static final String DEFAULT_MAP_FILE = "map.gml";
    private static final String SCENARIO_FILE_KEY = "gis.map.scenario";
    private static final String DEFAULT_SCENARIO_FILE = "scenario.xml";

    // CHECKSTYLE:OFF:MagicNumber
    private static final double AREA_SCALE_FACTOR = 1.0 / 10000.0;
    // CHECKSTYLE:ON:MagicNumber

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
            File dir = new File(config.getValue(MAP_DIRECTORY_KEY));
            File mapFile = new File(dir, config.getValue(MAP_FILE_KEY, DEFAULT_MAP_FILE));
            File scenarioFile = new File(dir, config.getValue(SCENARIO_FILE_KEY, DEFAULT_SCENARIO_FILE));
            readMapData(mapFile, result);
            readScenarioData(scenarioFile, result);
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
        catch (ScenarioException e) {
            throw new KernelException("Invalid scenario file", e);
        }
    }

    @Override
    public EntityID generateID() {
        return new EntityID(nextID++);
    }

    private GMLMap readMap(String fileName) throws GMLException {
        return MapReader.readGMLMap(fileName);
    }

    private void readMapData(File mapFile, StandardWorldModel result) throws GMLException {
        GMLMap map = MapReader.readGMLMap(mapFile);
        CoordinateConversion conversion = getCoordinateConversion(map);
        for (GMLBuilding next : map.getBuildings()) {
            // Create a new Building entity
            EntityID id = new EntityID(next.getID());
            Building b = new Building(id);
            List<Point2D> vertices = convertShapeToPoints(next, conversion);
            double area = GeometryTools2D.computeArea(vertices);
            Point2D centroid = GeometryTools2D.computeCentroid(vertices);

            LOG.debug("Building vertices: " + vertices);
            LOG.debug("Area: " + area);
            LOG.debug("Centroid: " + centroid);

            // Building properties
            b.setFloors(1);
            b.setFieryness(0);
            b.setBrokenness(0);
            b.setBuildingCode(0);
            b.setBuildingAttributes(0);
            b.setGroundArea((int)Math.abs(area * AREA_SCALE_FACTOR));
            b.setTotalArea((int)Math.abs(area * AREA_SCALE_FACTOR));
            b.setImportance(1);
            // Area properties
            b.setEdges(createEdges(next, conversion));
            b.setX((int)centroid.getX());
            b.setY((int)centroid.getY());
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

    private void readScenarioData(File scenarioFile, StandardWorldModel result) throws DocumentException, ScenarioException {
        if (scenarioFile.exists()) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(scenarioFile);
            Scenario scenario = new Scenario(doc);
            scenario.apply(result);
        }
    }

    private List<Edge> createEdges(GMLShape s, CoordinateConversion conversion) {
        LOG.debug("Computing edges for " + s);
        List<Edge> result = new ArrayList<Edge>();
        for (GMLDirectedEdge edge : s.getEdges()) {
            GMLCoordinates start = edge.getStartCoordinates();
            GMLCoordinates end = edge.getEndCoordinates();
            Integer neighbourID = s.getNeighbour(edge);
            EntityID id = neighbourID == null ? null : new EntityID(neighbourID);
            //            LOG.debug("Edge: " + start + " -> " + end);
            double sx = conversion.convertX(start.getX());
            double sy = conversion.convertY(start.getY());
            double ex = conversion.convertX(end.getX());
            double ey = conversion.convertY(end.getY());
            LOG.debug(edge.getEdge() + " : " + sx + "," + sy + " -> " + ex + "," + ey);
            result.add(new Edge((int)sx,
                                (int)sy,
                                (int)ex,
                                (int)ey,
                                id));
        }
        return result;
    }

    private List<Point2D> convertShapeToPoints(GMLShape shape, CoordinateConversion conversion) {
        List<Point2D> points = new ArrayList<Point2D>();
        for (GMLCoordinates next : shape.getCoordinates()) {
            points.add(new Point2D(conversion.convertX(next.getX()), conversion.convertY(next.getY())));
        }
        return points;
    }

    private CoordinateConversion getCoordinateConversion(GMLMap map) {
        switch (map.getCoordinateSystem()) {
        case LATLON:
            // Convert lat/lon to millimeters
            double scale = 1000.0 / MapTools.sizeOf1Metre((map.getMinX() + map.getMaxX()) / 2, (map.getMinY() + map.getMaxY()) / 2);
            return new ScaleConversion(map.getMinX(), map.getMinY(), scale, scale);
        case M:
            return new ConstantConversion(1000);
        case MM:
            return new IdentityConversion();
        default:
            throw new IllegalArgumentException("Unrecognised coordinate system: " + map.getCoordinateSystem());
        }
    }
}