package osm2gml;

import osm2gml.osm.OSMMap;
import osm2gml.osm.OSMNode;
import osm2gml.osm.OSMRoad;
import osm2gml.osm.OSMBuilding;
import osm2gml.osm.OSMWay;
import osm2gml.osm.OSMWayShapeInfo;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import java.awt.Color;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.misc.gui.ShapeDebugFrame;

public class CleanOSMStep extends ConvertStep {
    private OSMMap map;

    /**
       Construct a CleanOSMStep.
       @param progress The progress bar to update.
       @param status The status label to update.
       @param map The OSMMap to clean.
    */
    public CleanOSMStep(OSMMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Cleaning OpenStreetMap data";
    }

    @Override
    protected void step() {
        setProgressLimit(map.getNodes().size() + (map.getRoads().size() + map.getBuildings().size()) * 2 + map.getBuildings().size());
        setStatus("Looking for duplicate nodes");
        int nodes = fixNodes();
        setStatus("Fixing degenerate ways");
        int fixed = fixDegenerateWays(map.getRoads());
        fixed += fixDegenerateWays(map.getBuildings());
        setStatus("Looking for duplicate ways");
        int ways = fixDuplicateWays(map.getRoads());
        ways += fixDuplicateWays(map.getBuildings());
        setStatus("Fixing building direction");
        int b = fixBuildingDirection(map.getBuildings());
        setStatus("Removed " + nodes + " duplicate nodes and " + ways + " duplicate ways, fixed " + fixed + " degenerate ways, fixed " + b + " clockwise buildings");
    }

    private int fixNodes() {
        int count = 0;
        double threshold = ConvertTools.nearbyThreshold(map);
        Set<OSMNode> removed = new HashSet<OSMNode>();
        for (OSMNode next : map.getNodes()) {
            if (removed.contains(next)) {
                bumpProgress();
                continue;
            }
            for (OSMNode test : map.getNodes()) {
                if (next == test) {
                    continue;
                }
                if (removed.contains(test)) {
                    continue;
                }
                if (nearby(next, test, threshold)) {
                    // Remove the test node and replace all references to it with 'next'
                    map.replaceNode(test, next);
                    removed.add(test);
                    System.out.println("Removed duplicate node " + test.getID());
                    ++count;
                }
            }
            bumpProgress();
        }
        return count;
    }

    private int fixDegenerateWays(Collection<? extends OSMWay> ways) {
        int count = 0;
        for (OSMWay way : ways) {
            // Check that no nodes are listed multiple times in sequence
            List<Long> ids = new ArrayList<Long>(way.getNodeIDs());
            Iterator<Long> it = ids.iterator();
            if (!it.hasNext()) {
                // Empty way. Remove it.
                remove(way);
                ++count;
                continue;
            }
            long last = it.next();
            boolean fixed = false;
            while (it.hasNext()) {
                long next = it.next();
                if (next == last) {
                    // Duplicate node
                    it.remove();
                    System.out.println("Removed node " + next + " from way " + way.getID());
                    fixed = true;
                }
                last = next;
            }
            if (fixed) {
                way.setNodeIDs(ids);
                ++count;
            }
            bumpProgress();
        }
        return count;
    }

    private int fixDuplicateWays(Collection<? extends OSMWay> ways) {
        int count = 0;
        Set<OSMWay> removed = new HashSet<OSMWay>();
        for (OSMWay next : ways) {
            if (removed.contains(next)) {
                bumpProgress();
                continue;
            }
            // Look at all other roads and see if any are subpaths of this road
            for (OSMWay test : ways) {
                if (next == test) {
                    continue;
                }
                if (removed.contains(test)) {
                    continue;
                }
                List<Long> testIDs = test.getNodeIDs();
                if (isSubList(testIDs, next.getNodeIDs())) {
                    remove(test);
                    removed.add(test);
                    ++count;
                    System.out.println("Removed way " + test.getID());
                }
                else {
                    Collections.reverse(testIDs);
                    if (isSubList(testIDs, next.getNodeIDs())) {
                        remove(test);
                        removed.add(test);
                        ++count;
                        System.out.println("Removed way " + test.getID());
                    }
                }
            }
            bumpProgress();
        }
        return count;
    }

    /**
       Make sure all buildings have their nodes listed in clockwise order.
    */
    private int fixBuildingDirection(Collection<OSMBuilding> buildings) {
        // Sum the angles of all right-hand turns
        // If the total is +360 then order is clockwise, -360 means counterclockwise.
        int count = 0;
        for (OSMBuilding building : buildings) {
            if (ConvertTools.isClockwise(building, map)) {
                // Reverse the order
                List<Long> ids = building.getNodeIDs();
                Collections.reverse(ids);
                building.setNodeIDs(ids);
                ++count;
            }
            bumpProgress();
        }
        return count;
    }

    private boolean nearby(OSMNode first, OSMNode second, double threshold) {
        double dx = first.getLongitude() - second.getLongitude();
        double dy = first.getLatitude() - second.getLatitude();
        return (dx >= - threshold &&
                dx <= threshold &&
                dy >= - threshold &&
                dy <= threshold);
    }

    private boolean isSubList(List<Long> first, List<Long> second) {
        return Collections.indexOfSubList(second, first) != -1;
    }

    private void remove(OSMWay way) {
        if (way instanceof OSMRoad) {
            map.removeRoad((OSMRoad)way);
        }
        else if (way instanceof OSMBuilding) {
            map.removeBuilding((OSMBuilding)way);
        }
        else {
            throw new IllegalArgumentException("Don't know how to handle this type of OSMWay: " + way.getClass().getName());
        }
    }

    private Line2D makeLine(long first, long second) {
        OSMNode n1 = map.getNode(first);
        OSMNode n2 = map.getNode(second);
        return new Line2D(n1.getLongitude(), n1.getLatitude(), n2.getLongitude() - n1.getLongitude(), n2.getLatitude() - n1.getLatitude());
    }
}