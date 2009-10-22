package osm2gml;

import osm2gml.OSMShape;

import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLEdge;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.gml.GMLFace;
import osm2gml.gml.FaceType;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import java.awt.Color;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.misc.gui.ShapeDebugFrame;

public class SplitIntersectingEdgesStep extends ConvertStep {
    private GMLMap gmlMap;
    private int splitCount;
    private int overlapCount;
    private int removedCount;
    private int newEdgeCount;
    private int inspectedCount;

    /**
       Construct a SplitIntersectingEdgesStep.
       @param gmlMap The GMLMap to use.
    */
    public SplitIntersectingEdgesStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Splitting intersecting edges";
    }

    @Override
    protected void step() {
        // Look at every pair of GMLEdges and check for intersections
        Deque<GMLEdge> toCheck = new ArrayDeque<GMLEdge>(gmlMap.getEdges());
        Set<GMLEdge> seen = new HashSet<GMLEdge>();
        setProgressLimit(toCheck.size());
        splitCount = 0;
        overlapCount = 0;
        removedCount = 0;
        newEdgeCount = 0;
        inspectedCount = 0;
        //        long start = System.currentTimeMillis();
        while (!toCheck.isEmpty()) {
            GMLEdge next = toCheck.pop();
            bumpProgress();
            if (!gmlMap.getEdges().contains(next)) {
                continue;
            }
            if (seen.contains(next)) {
                continue;
            }
            //            debug.setBackground(ConvertTools.getAllGMLShapes(gmlMap));
            //            debug.activate();
            ++inspectedCount;
            seen.add(next);
            Line2D l1 = ConvertTools.gmlEdgeToLine(next);
            boolean done = false;
            for (GMLEdge test : gmlMap.getEdges()) {
                if (test == next) {
                    continue;
                }
                Line2D l2 = ConvertTools.gmlEdgeToLine(test);
                Collection<GMLEdge> newEdges = null;
                if (GeometryTools2D.parallel(l1, l2)) {
                    newEdges = processParallelLines(next, l1, test, l2);
                }
                else {
                    // Look for intersection
                    Point2D intersection = GeometryTools2D.getSegmentIntersectionPoint(l1, l2);
                    //                    System.out.println(intersection);
                    //                    debug.show("Split intersection",
                    //                               new ShapeDebugFrame.Line2DShapeInfo(l1, "Line 1", Color.ORANGE, true, false),
                    //                               new ShapeDebugFrame.Line2DShapeInfo(l2, "Line 2", Color.BLUE, true, false));
                    if (intersection == null) {
                        // Maybe the intersection is within the GMLMap's "nearby node" tolerance?
                        intersection = GeometryTools2D.getIntersectionPoint(l1, l2);
                        // If the new node is one of the endpoints then we maybe had a real intersection
                        if (gmlMap.isNear(intersection, next.getStart()) || gmlMap.isNear(intersection, next.getEnd())) {
                            // Check that the new node is somewhere on the other line segment
                            double d = l2.getIntersection(l1);
                            if (d < 0 || d > 1) {
                                // Nope. Ignore it.
                                continue;
                            }
                        }
                        else if (gmlMap.isNear(intersection, test.getStart()) || gmlMap.isNear(intersection, test.getEnd())) {
                            // Check that the new node is somewhere on the other line segment
                            double d = l1.getIntersection(l2);
                            if (d < 0 || d > 1) {
                                // Nope. Ignore it.
                                continue;
                            }
                        }
                        else {
                            // Not on any endpoint. Ignore it. The node will be pruned later.
                            continue;
                        }
                    }
                    GMLNode node = gmlMap.ensureNodeNear(intersection);
                    // Split the two edges into 4 (maybe)
                    // Was the 'test' edge split?
                    newEdges = new HashSet<GMLEdge>();
                    if (!node.equals(test.getStart()) && !node.equals(test.getEnd())) {
                        newEdges.addAll(split(test, node));
                        ++splitCount;
                    }
                    // Was the 'next' edge split?
                    if (!node.equals(next.getStart()) && !node.equals(next.getEnd())) {
                        newEdges.addAll(split(next, node));
                        ++splitCount;
                        // No need to check for further intersections: 'next' is gone.
                        done = true;
                    }
                }
                if (newEdges != null && !newEdges.isEmpty()) {
                    toCheck.addAll(newEdges);
                    bumpMaxProgress(newEdges.size());
                    newEdgeCount += newEdges.size();
                }
                if (done) {
                    break;
                }
            }
        }
        //        long end = System.currentTimeMillis();
        //        long time = end - start;
        //        System.out.println("Intersection test took " + time + "ms for " + inspectedCount + " edges");
        //        System.out.println("That's an average of " + (time / inspectedCount) + "ms per edge");
        setStatus("Inspected " + inspectedCount + " edges: split " + splitCount + ", fixed " + overlapCount + " overlaps, deleted " + removedCount +" edges, added " + newEdgeCount + " new edges");
    }

    // Return the number of new edges
    private Collection<GMLEdge> split(GMLEdge edge, GMLNode split) {
        GMLEdge a = gmlMap.ensureEdge(edge.getStart(), split);
        GMLEdge b = gmlMap.ensureEdge(split, edge.getEnd());
        Collection<GMLEdge> result = new ArrayList<GMLEdge>();
        result.add(a);
        result.add(b);
        gmlMap.replaceEdge(edge, result);
        ++removedCount;
        return result;
    }

    private Collection<GMLEdge> processParallelLines(GMLEdge e1, Line2D l1, GMLEdge e2, Line2D l2) {
        Line2D shorter = l1.getDirection().getLength() < l2.getDirection().getLength() ? l1 : l2;
        Line2D longer = shorter == l1 ? l2 : l1;
        GMLEdge shorterEdge = shorter == l1 ? e1 : e2;
        GMLEdge longerEdge = shorter == l1 ? e2 : e1;
        return processParallelLines(shorterEdge, longerEdge);
    }


    private Collection<GMLEdge> processParallelLines(GMLEdge shorterEdge, GMLEdge longerEdge) {
        // Possible cases:
        // Shorter line entirely inside longer
        // Shorter line overlaps longer at longer start
        // Shorter line overlaps longer at longer end
        // Shorter line start point is same as longer start and end point is inside
        // Shorter line start point is same as longer end and end point is inside
        // Shorter line end point is same as longer start and start point is inside
        // Shorter line end point is same as longer end and start point is inside
        // Impossible for lines to be identical: ensureEdge does not return duplicate edges
        Line2D longer = ConvertTools.gmlEdgeToLine(longerEdge);
        Line2D shorter = ConvertTools.gmlEdgeToLine(shorterEdge);
        boolean shortStartLongStart = shorterEdge.getStart() == longerEdge.getStart();
        boolean shortStartLongEnd = shorterEdge.getStart() == longerEdge.getEnd();
        boolean shortEndLongStart = shorterEdge.getEnd() == longerEdge.getStart();
        boolean shortEndLongEnd = shorterEdge.getEnd() == longerEdge.getEnd();
        boolean startInside = !shortStartLongStart && !shortStartLongEnd && GeometryTools2D.contains(longer, shorter.getOrigin());
        boolean endInside = !shortEndLongStart && ! shortEndLongEnd && GeometryTools2D.contains(longer, shorter.getEndPoint());
        if (startInside || endInside) {
            ++overlapCount;
        }
        if (startInside && endInside) {
            return processInternalEdge(shorterEdge, longerEdge);
        }
        else if (startInside && !endInside) {
            // Either full overlap or coincident end point
            if (shortEndLongStart) {
                return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getEnd(), longerEdge.getStart());
            }
            else if (shortEndLongEnd) {
                return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getEnd(), longerEdge.getEnd());
            }
            else {
                // Full overlap
                GMLNode longerNodeInside = GeometryTools2D.contains(shorter, longer.getOrigin()) ? longerEdge.getStart() : longerEdge.getEnd();
                return processOverlap(shorterEdge, longerEdge, shorterEdge.getStart(), longerNodeInside);
            }
        }
        else if (endInside && !startInside) {
            // Either full overlap or coincident end point
            if (shortStartLongStart) {
                return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getStart(), longerEdge.getStart());
            }
            else if (shortStartLongEnd) {
                return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getStart(), longerEdge.getEnd());
            }
            else {
                // Full overlap
                GMLNode longerNodeInside = GeometryTools2D.contains(shorter, longer.getOrigin()) ? longerEdge.getStart() : longerEdge.getEnd();
                return processOverlap(shorterEdge, longerEdge, shorterEdge.getEnd(), longerNodeInside);
            }
        }
        else {
            return null;
        }
    }

    private Collection<GMLEdge> processInternalEdge(GMLEdge shorterEdge, GMLEdge longerEdge) {
        Line2D shorter = ConvertTools.gmlEdgeToLine(shorterEdge);
        Line2D longer = ConvertTools.gmlEdgeToLine(longerEdge);
        // Split longer into two chunks and cut out the middle
        double t1 = GeometryTools2D.positionOnLine(longer, shorter.getOrigin());
        double t2 = GeometryTools2D.positionOnLine(longer, shorter.getEndPoint());
        GMLNode first;
        GMLNode second;
        if (t1 < t2) {
            first = gmlMap.ensureNodeNear(shorter.getOrigin());
            second = gmlMap.ensureNodeNear(shorter.getEndPoint());
        }
        else {
            first = gmlMap.ensureNodeNear(shorter.getEndPoint());
            second = gmlMap.ensureNodeNear(shorter.getOrigin());
        }
        GMLNode originNode = gmlMap.ensureNodeNear(longer.getOrigin());
        GMLNode endNode = gmlMap.ensureNodeNear(longer.getEndPoint());
        Set<GMLEdge> newEdges = new HashSet<GMLEdge>();
        newEdges.add(shorterEdge);
        if (!originNode.equals(first)) {
            newEdges.add(gmlMap.ensureEdge(originNode, first));
        }
        if (!endNode.equals(second)) {
            newEdges.add(gmlMap.ensureEdge(second, endNode));
        }
        gmlMap.replaceEdge(longerEdge, newEdges);
        ++removedCount;
        return newEdges;
    }

    private Collection<GMLEdge> processCoincidentNode(GMLEdge shorterEdge, GMLEdge longerEdge, GMLNode shortOverlap, GMLNode longOverlap) {
        // Split the long edge at the non-overlapping point
        GMLNode cutPoint = shortOverlap == shorterEdge.getStart() ? shorterEdge.getEnd() : shorterEdge.getStart();
        GMLNode endPoint = longOverlap == longerEdge.getStart() ? longerEdge.getEnd() : longerEdge.getStart();
        GMLEdge newEdge = gmlMap.ensureEdge(cutPoint, endPoint);
        gmlMap.replaceEdge(longerEdge, shorterEdge, newEdge);
        ++removedCount;
        Set<GMLEdge> newEdges = new HashSet<GMLEdge>();
        newEdges.add(newEdge);
        return newEdges;
    }

    private Collection<GMLEdge> processOverlap(GMLEdge shorterEdge, GMLEdge longerEdge, GMLNode shorterNodeInside, GMLNode longerNodeInside) {
        // Shorten the two edges and create a new overlap edge
        GMLNode shorterNodeOutside = shorterNodeInside == shorterEdge.getStart() ? shorterEdge.getEnd() : shorterEdge.getStart();
        GMLNode longerNodeOutside = longerNodeInside == longerEdge.getStart() ? longerEdge.getEnd() : longerEdge.getStart();
        GMLEdge first = gmlMap.ensureEdge(shorterNodeOutside, longerNodeInside);
        GMLEdge second = gmlMap.ensureEdge(longerNodeInside, shorterNodeInside);
        GMLEdge third = gmlMap.ensureEdge(shorterNodeInside, longerNodeOutside);
        Set<GMLEdge> newEdges = new HashSet<GMLEdge>();
        newEdges.add(first);
        newEdges.add(second);
        newEdges.add(third);
        gmlMap.replaceEdge(shorterEdge, first, second);
        gmlMap.replaceEdge(longerEdge, second, third);
        ++removedCount;
        ++removedCount;
        return newEdges;
    }
}