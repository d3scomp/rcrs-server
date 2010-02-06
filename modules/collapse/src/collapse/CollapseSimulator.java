package collapse;

import rescuecore2.config.Config;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.collections.LazyMap;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Blockade;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.Maths;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;

//import rescuecore2.misc.gui.ShapeDebugFrame;
//import java.awt.Color;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A simple collapse simulator.
 */
public class CollapseSimulator extends StandardSimulator {
    private static final Log LOG = LogFactory.getLog(CollapseSimulator.class);

    private static final String CONFIG_PREFIX = "collapse.";
    private static final String DESTROYED_SUFFIX = ".p-destroyed";
    private static final String SEVERE_SUFFIX = ".p-severe";
    private static final String MODERATE_SUFFIX = ".p-moderate";
    private static final String SLIGHT_SUFFIX = ".p-slight";
    private static final String NONE_SUFFIX = ".p-none";

    private static final String DESTROYED_MEAN_SUFFIX = "destroyed.mean";
    private static final String DESTROYED_SD_SUFFIX = "destroyed.sd";
    private static final String SEVERE_MEAN_SUFFIX = "severe.mean";
    private static final String SEVERE_SD_SUFFIX = "severe.sd";
    private static final String MODERATE_MEAN_SUFFIX = "moderate.mean";
    private static final String MODERATE_SD_SUFFIX = "moderate.sd";
    private static final String SLIGHT_MEAN_SUFFIX = "slight.mean";
    private static final String SLIGHT_SD_SUFFIX = "slight.sd";

    private static final int MAX_COLLAPSE = 100;

    private static final double FLOOR_HEIGHT = 7000;

    private static final double REPAIR_COST_FACTOR = 0.000001;

    private GaussianGenerator destroyed;
    private GaussianGenerator severe;
    private GaussianGenerator moderate;
    private GaussianGenerator slight;

    private Map<StandardEntityConstants.BuildingCode, CollapseStats> stats;

    //    private ShapeDebugFrame debug;

    @Override
    public String getName() {
        return "Basic collapse simulator";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        //        debug = new ShapeDebugFrame();
        stats = new EnumMap<StandardEntityConstants.BuildingCode, CollapseStats>(StandardEntityConstants.BuildingCode.class);
        for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode.values()) {
            stats.put(code, new CollapseStats(code, config));
        }
        slight = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX + SLIGHT_MEAN_SUFFIX),
                                       config.getFloatValue(CONFIG_PREFIX + SLIGHT_SD_SUFFIX),
                                       config.getRandom());
        moderate = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX + MODERATE_MEAN_SUFFIX),
                                         config.getFloatValue(CONFIG_PREFIX + MODERATE_SD_SUFFIX),
                                         config.getRandom());
        severe = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX + SEVERE_MEAN_SUFFIX),
                                       config.getFloatValue(CONFIG_PREFIX + SEVERE_SD_SUFFIX),
                                       config.getRandom());
        destroyed = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX + DESTROYED_MEAN_SUFFIX),
                                          config.getFloatValue(CONFIG_PREFIX + DESTROYED_SD_SUFFIX),
                                          config.getRandom());
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        //        debug.activate();
        int time = c.getTime();
        Map<StandardEntityConstants.BuildingCode, Map<CollapseDegree, Integer>> count = new EnumMap<StandardEntityConstants.BuildingCode, Map<CollapseDegree, Integer>>(StandardEntityConstants.BuildingCode.class);
        Map<StandardEntityConstants.BuildingCode, Integer> total = new EnumMap<StandardEntityConstants.BuildingCode, Integer>(StandardEntityConstants.BuildingCode.class);
        for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode.values()) {
            Map<CollapseDegree, Integer> next = new EnumMap<CollapseDegree, Integer>(CollapseDegree.class);
            for (CollapseDegree cd : CollapseDegree.values()) {
                next.put(cd, 0);
            }
            count.put(code, next);
            total.put(code, 0);
        }
        Map<Road, Collection<Blockade>> newBlockades = new LazyMap<Road, Collection<Blockade>>() {
            @Override
            public Collection<Blockade> createValue() {
                return new ArrayList<Blockade>();
            }
        };
        if (time == 1) {
            // Work out what has collapsed
            for (StandardEntity next : model) {
                if (next instanceof Building) {
                    Building b = (Building)next;
                    StandardEntityConstants.BuildingCode code = b.getBuildingCodeEnum();
                    int damage = code == null ? 0 : stats.get(code).damage();
                    damage = Maths.restrictRange(damage, 0, MAX_COLLAPSE);
                    b.setBrokenness(damage);
                    changes.addChange(b, b.getBrokennessProperty());

                    CollapseDegree degree = CollapseDegree.get(damage);
                    count.get(code).put(degree, count.get(code).get(degree) + 1);
                    total.put(code, total.get(code) + 1);

                    if (damage > 0) {
                        createBlockages(b, newBlockades);
                    }
                }
            }
            LOG.info("Finished collapsing buildings: ");
            for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode.values()) {
                LOG.info("Building code " + code + ": " + total.get(code) + " buildings");
                Map<CollapseDegree, Integer> data = count.get(code);
                for (Map.Entry<CollapseDegree, Integer> entry : data.entrySet()) {
                    LOG.info("  " + entry.getValue() + " " + entry.getKey().toString().toLowerCase());
                }
            }
        }
        // Check for fire
        for (StandardEntity next : model) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (!b.isFierynessDefined()) {
                    continue;
                }
                int minDamage = 0;
                switch (b.getFierynessEnum()) {
                case HEATING:
                    minDamage = slight.nextValue().intValue();
                    break;
                case BURNING:
                    minDamage = moderate.nextValue().intValue();
                    break;
                case INFERNO:
                    minDamage = severe.nextValue().intValue();
                    break;
                case BURNT_OUT:
                    minDamage = destroyed.nextValue().intValue();
                    break;
                default:
                    break;
                }
                minDamage = Maths.restrictRange(minDamage, 0, MAX_COLLAPSE);
                int damage = b.isBrokennessDefined() ? b.getBrokenness() : 0;
                if (damage < minDamage) {
                    LOG.info(b + " damaged by fire. New brokenness: " + minDamage);
                    b.setBrokenness(minDamage);
                    changes.addChange(b, b.getBrokennessProperty());
                    createBlockages(b, newBlockades);
                }
            }
        }
        for (Map.Entry<Road, Collection<Blockade>> entry : newBlockades.entrySet()) {
            Road r = entry.getKey();
            List<EntityID> existing = r.getBlockades();
            List<EntityID> ids = new ArrayList<EntityID>();
            if (existing != null) {
                ids.addAll(existing);
            }
            for (Blockade b : entry.getValue()) {
                ids.add(b.getID());
            }
            r.setBlockades(ids);
            changes.addAll(entry.getValue());
            changes.addChange(r, r.getBlockadesProperty());
        }
        //        debug.deactivate();
    }

    private void createBlockages(Building b, Map<Road, Collection<Blockade>> roadBlockages) {
        // CHECKSTYLE:OFF:MagicNumber
        double d = FLOOR_HEIGHT * b.getFloors() * (1.0 + (b.getBrokenness() / 100.0));
        // CHECKSTYLE:ON:MagicNumber
        // Place some blockages on surrounding roads
        List<java.awt.geom.Area> wallAreas = new ArrayList<java.awt.geom.Area>();
        // Project each wall out and build a list of wall areas
        for (Edge edge : b.getEdges()) {
            projectWall(edge, wallAreas, d);
        }
        java.awt.geom.Area fullArea = new java.awt.geom.Area();
        for (java.awt.geom.Area wallArea : wallAreas) {
            fullArea.add(wallArea);
        }
        //        debug.show("Collapsed building",
        //                   new ShapeDebugFrame.AWTShapeInfo(buildingArea, "Original building area", Color.RED, true),
        //                   new ShapeDebugFrame.AWTShapeInfo(fullArea, "Expanded building area (d = " + d + ")", Color.BLACK, false)
        //                   );
        // Find existing blockade areas
        java.awt.geom.Area existing = new java.awt.geom.Area();
        for (StandardEntity e : model.getEntitiesOfType(StandardEntityURN.BLOCKADE)) {
            Blockade blockade = (Blockade)e;
            existing.add(blockadeToArea(blockade));
        }
        // Intersect wall areas with roads
        Map<Road, Collection<java.awt.geom.Area>> blockadesForRoad = createRoadBlockades(fullArea, existing);
        // Create blockades
        int count = 0;
        for (Collection<java.awt.geom.Area> c : blockadesForRoad.values()) {
            count += c.size();
        }
        try {
            LOG.debug("Requesting " + count + " new IDs");
            List<EntityID> newIDs = requestNewEntityIDs(count);
            Iterator<EntityID> it = newIDs.iterator();
            for (Map.Entry<Road, Collection<java.awt.geom.Area>> entry : blockadesForRoad.entrySet()) {
                Road r = entry.getKey();
                for (java.awt.geom.Area area : entry.getValue()) {
                    EntityID id = it.next();
                    Collection<Blockade> c = roadBlockages.get(r);
                    Blockade blockade = makeBlockade(id, area, r.getID());
                    if (blockade != null) {
                        c.add(blockade);
                    }
                }
            }
        }
        catch (InterruptedException e) {
            LOG.error("Interrupted while requesting IDs");
        }
    }

    private void projectWall(Edge edge, Collection<java.awt.geom.Area> areaList, double d) {
        Line2D wallLine = new Line2D(edge.getStartX(), edge.getStartY(), edge.getEndX() - edge.getStartX(), edge.getEndY() - edge.getStartY());
        Vector2D wallDirection = wallLine.getDirection();
        Vector2D offset = wallDirection.getNormal().normalised().scale(-d);
        Path2D path = new Path2D.Double();
        Point2D first = wallLine.getOrigin();
        Point2D second = wallLine.getEndPoint();
        Point2D third = second.plus(offset);
        Point2D fourth = first.plus(offset);
        path.moveTo(first.getX(), first.getY());
        path.lineTo(second.getX(), second.getY());
        path.lineTo(third.getX(), third.getY());
        path.lineTo(fourth.getX(), fourth.getY());
        java.awt.geom.Area wallArea = new java.awt.geom.Area(path);
        areaList.add(wallArea);
        // Also add circles at each corner
        double radius = offset.getLength();
        Ellipse2D ellipse1 = new Ellipse2D.Double(first.getX() - radius, first.getY() - radius, radius * 2, radius * 2);
        Ellipse2D ellipse2 = new Ellipse2D.Double(second.getX() - radius, second.getY() - radius, radius * 2, radius * 2);
        areaList.add(new java.awt.geom.Area(ellipse1));
        areaList.add(new java.awt.geom.Area(ellipse2));
        LOG.info("Edge from " + wallLine + " expanded to " + first + ", " + second + ", " + third + ", " + fourth);
        //        debug.show("Collapsed building",
        //                   new ShapeDebugFrame.AWTShapeInfo(buildingArea, "Original building area", Color.RED, true),
        //                   new ShapeDebugFrame.Line2DShapeInfo(wallLine, "Wall edge", Color.WHITE, true, true),
        //                   new ShapeDebugFrame.AWTShapeInfo(wallArea, "Wall area (d = " + d + ")", Color.GREEN, false),
        //                   new ShapeDebugFrame.AWTShapeInfo(ellipse1, "Ellipse 1", Color.BLUE, false),
        //                   new ShapeDebugFrame.AWTShapeInfo(ellipse2, "Ellipse 2", Color.ORANGE, false)
        //                   );
    }

    private Map<Road, Collection<java.awt.geom.Area>> createRoadBlockades(java.awt.geom.Area buildingArea, java.awt.geom.Area existing) {
        Map<Road, Collection<java.awt.geom.Area>> result = new HashMap<Road, Collection<java.awt.geom.Area>>();
        for (StandardEntity e : model.getEntitiesOfType(StandardEntityURN.ROAD)) {
            Road r = (Road)e;
            java.awt.geom.Area roadArea = areaToGeomArea(r);
            java.awt.geom.Area intersection = new java.awt.geom.Area(roadArea);
            intersection.intersect(buildingArea);
            intersection.subtract(existing);
            if (intersection.isEmpty()) {
                continue;
            }
            existing.add(intersection);
            List<java.awt.geom.Area> blockadeAreas = fix(intersection);
            result.put(r, blockadeAreas);
            //            debug.show("Road blockage",
            //                       new ShapeDebugFrame.AWTShapeInfo(buildingArea, "Building area", Color.BLACK, false),
            //                       new ShapeDebugFrame.AWTShapeInfo(roadArea, "Road area", Color.BLUE, false),
            //                       new ShapeDebugFrame.AWTShapeInfo(intersection, "Intersection", Color.GREEN, true)
            //                       );
        }
        return result;
    }

    private java.awt.geom.Area areaToGeomArea(rescuecore2.standard.entities.Area area) {
        Path2D result = new Path2D.Double();
        Iterator<Edge> it = area.getEdges().iterator();
        Edge e = it.next();
        result.moveTo(e.getStartX(), e.getStartY());
        result.lineTo(e.getEndX(), e.getEndY());
        while (it.hasNext()) {
            e = it.next();
            result.lineTo(e.getEndX(), e.getEndY());
        }
        return new java.awt.geom.Area(result);
    }

    private List<java.awt.geom.Area> fix(java.awt.geom.Area area) {
        List<java.awt.geom.Area> result = new ArrayList<java.awt.geom.Area>();
        if (area.isSingular()) {
            result.add(area);
            return result;
        }
        PathIterator it = area.getPathIterator(null);
        Path2D current = null;
        // CHECKSTYLE:OFF:MagicNumber
        double[] d = new double[6];
        while (!it.isDone()) {
            switch (it.currentSegment(d)) {
            case PathIterator.SEG_MOVETO:
                if (current != null) {
                    result.add(new java.awt.geom.Area(current));
                }
                current = new Path2D.Double();
                current.moveTo(d[0], d[1]);
                break;
            case PathIterator.SEG_LINETO:
                current.lineTo(d[0], d[1]);
                break;
            case PathIterator.SEG_QUADTO:
                current.quadTo(d[0], d[1], d[2], d[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                current.curveTo(d[0], d[1], d[2], d[3], d[4], d[5]);
                break;
            case PathIterator.SEG_CLOSE:
                current.closePath();
                break;
            }
            it.next();
        }
        // CHECKSTYLE:ON:MagicNumber
        if (current != null) {
            result.add(new java.awt.geom.Area(current));
        }
        return result;
    }

    private Blockade makeBlockade(EntityID id, java.awt.geom.Area area, EntityID roadID) {
        Blockade result = new Blockade(id);
        int[] apexes = getApexes(area);
        List<Point2D> points = GeometryTools2D.vertexArrayToPoints(apexes);
        int cost = (int)(GeometryTools2D.computeArea(points) * REPAIR_COST_FACTOR);
        if (cost == 0) {
            return null;
        }
        Point2D centroid = GeometryTools2D.computeCentroid(points);
        result.setApexes(apexes);
        result.setPosition(roadID);
        result.setX((int)centroid.getX());
        result.setY((int)centroid.getY());
        result.setRepairCost((int)cost);
        LOG.debug("Created new blockade: " + result.getFullDescription());
        return result;
    }

    private int[] getApexes(java.awt.geom.Area area) {
        LOG.debug("getApexes");
        List<Integer> apexes = new ArrayList<Integer>();
        // CHECKSTYLE:OFF:MagicNumber
        PathIterator it = area.getPathIterator(null, 100);
        double[] d = new double[6];
        int moveX = 0;
        int moveY = 0;
        while (!it.isDone()) {
            int x = 0;
            int y = 0;
            switch (it.currentSegment(d)) {
            case PathIterator.SEG_MOVETO:
                LOG.debug("Move to");
                x = (int)d[0];
                y = (int)d[1];
                moveX = x;
                moveY = y;
                break;
            case PathIterator.SEG_LINETO:
                LOG.debug("Line to");
                x = (int)d[0];
                y = (int)d[1];
                break;
            case PathIterator.SEG_QUADTO:
                LOG.debug("Quad to");
                x = (int)d[2];
                y = (int)d[3];
                break;
            case PathIterator.SEG_CUBICTO:
                LOG.debug("Cubic to");
                x = (int)d[4];
                y = (int)d[5];
                break;
            case PathIterator.SEG_CLOSE:
                LOG.debug("Close");
                x = moveX;
                y = moveY;
                break;
            }
            apexes.add(x);
            apexes.add(y);
            LOG.debug(x + ", " + y);
            it.next();
        }
        // CHECKSTYLE:ON:MagicNumber
        int[] result = new int[apexes.size()];
        int i = 0;
        for (Integer next : apexes) {
            result[i++] = next;
        }
        return result;
    }

    private java.awt.geom.Area blockadeToArea(Blockade b) {
        Path2D result = new Path2D.Double();
        int[] apexes = b.getApexes();
        result.moveTo(apexes[0], apexes[1]);
        for (int i = 2; i < apexes.length; i += 2) {
            result.lineTo(apexes[i], apexes[i + 1]);
        }
        result.closePath();
        return new java.awt.geom.Area(result);
    }

    private class CollapseStats {
        private double pDestroyed;
        private double pSevere;
        private double pModerate;
        private double pSlight;

        CollapseStats(StandardEntityConstants.BuildingCode code, Config config) {
            String s = CONFIG_PREFIX + code.toString().toLowerCase();
            pDestroyed = config.getFloatValue(s + DESTROYED_SUFFIX);
            pSevere = pDestroyed + config.getFloatValue(s + SEVERE_SUFFIX);
            pModerate = pSevere + config.getFloatValue(s + MODERATE_SUFFIX);
            pSlight = pModerate + config.getFloatValue(s + SLIGHT_SUFFIX);
        }

        int damage() {
            double d = random.nextDouble();
            if (d < pDestroyed) {
                return destroyed.nextValue().intValue();
            }
            if (d < pSevere) {
                return severe.nextValue().intValue();
            }
            if (d < pModerate) {
                return moderate.nextValue().intValue();
            }
            if (d < pSlight) {
                return slight.nextValue().intValue();
            }
            return 0;
        }
    }

    private enum CollapseDegree {
        NONE(0),
        SLIGHT(25),
        MODERATE(50),
        SEVERE(75),
        DESTROYED(100);

        private int max;

        private CollapseDegree(int max) {
            this.max = max;
        }

        public static CollapseDegree get(int d) {
            for (CollapseDegree next : values()) {
                if (d <= next.max) {
                    return next;
                }
            }
            throw new IllegalArgumentException("Don't know what to do with a damage value of " + d);
        }
    }
}