package maps.convert.osm2gml;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.debug.GMLEdgeShapeInfo;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLFace;
import maps.gml.debug.GMLFaceShapeInfo;
import maps.gml.FaceType;

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

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import maps.convert.ConvertStep;

public class SplitFacesStep extends ConvertStep {
    private GMLMap gmlMap;

    /**
       Construct a SplitFacesStep.
       @param gmlMap The GMLMap to use.
    */
    public SplitFacesStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Splitting face shapes";
    }

    @Override
    protected void step() {
        /*
        Collection<GMLFace> allFaces = gmlMap.getFaces();
        setProgressLimit(allFaces.size());
        int count = 0;
        int i = 0;
        debug.setBackground(ConvertTools.getAllGMLShapes(gmlMap));
        for (GMLFace face : allFaces) {
            switch (face.getFaceType()) {
            case ROAD:
            case INTERSECTION:
            case BUILDING:
                count += splitFaceIfRequired(face);
                break;
            default:
                // Ignore
                break;
            }
            bumpProgress();
            ++i;
        }
        setStatus("Added " + count + " new road faces");
        */
    }

    /*
    private int splitFaceIfRequired(GMLFace face) {
        Set<GMLDirectedEdge> edgesRemaining = new HashSet<GMLDirectedEdge>(face.getEdges());
        boolean firstFace = true;
        int newFaceCount = 0;
        //        System.out.println("Splitting " + face);
        //        debug.show("Splitting faces", new GMLFaceShapeInfo(face, "Face", Constants.BLACK, Constants.TRANSPARENT_ORANGE, true));
        while (!edgesRemaining.isEmpty()) {
            //            System.out.println(edgesRemaining.size() + " edges remaining");
            GMLDirectedEdge edge = edgesRemaining.iterator().next();
            GMLNode start = edge.getStartNode();
            GMLNode end = edge.getEndNode();
            List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>();
            result.add(edge);
            GMLEdge lastEdge = edge.getEdge();
            Line2D lastLine = ConvertTools.gmlEdgeToLine(lastEdge);
            // Now walk around
            //            System.out.println("Starting walk from " + edge);
            //            System.out.println("Start: " + start);
            //            System.out.println("End: " + end);
            while (!end.equals(start)) {
                Set<GMLEdge> candidates = gmlMap.getAttachedEdges(end);
                GMLEdge turn = ConvertTools.findLeftTurn(edge, candidates);
//                System.out.println("Last edge: " + edge);
//                System.out.println("Candidate edges");
//                Line2D l1 = ConvertTools.gmlDirectedEdgeToLine(edge);
//                for (GMLEdge candidate : candidates) {
//                    if (candidate == edge.getEdge()) {
//                        continue;
//                    }
//                    Line2D l2 = ConvertTools.gmlEdgeToLine(candidate, edge.getEndNode());
//                    System.out.println("  " + candidate);
//                    System.out.println("  " + (GeometryTools2D.isRightTurn(l1, l2) ? "Right turn" : "Left turn"));
//                    System.out.println("  " + (GeometryTools2D.isRightTurn(l1, l2) ? GeometryTools2D.getRightAngleBetweenLines(l1, l2) : GeometryTools2D.getLeftAngleBetweenLines(l1, l2)));
//                }
//                System.out.println("Best turn: " + turn);
//                debug.show("Splitting faces",
//                           new GMLFaceShapeInfo(face, "Face", Constants.BLACK, Constants.TRANSPARENT_ORANGE, true),
//                           new GMLEdgeShapeInfo(lastEdge, "Edge", Constants.BLUE, true),
//                           new GMLEdgeShapeInfo(turn, "Turn", Constants.RED, true));
                edge = gmlMap.ensureDirectedEdge(end, turn);
                lastEdge = turn;
                end = edge.getEndNode();
                result.add(edge);
                //                System.out.println("Added " + edge);
                //                System.out.println("New end: " + end);
            }
            edgesRemaining.removeAll(result);
            if (!firstFace || !edgesRemaining.isEmpty()) {
                // Didn't cover all edges so new faces are needed.
                if (firstFace) {
                    gmlMap.removeFace(face);
                    firstFace = false;
                }
                else {
                    ++newFaceCount;
                }
                GMLFace newFace = gmlMap.createFace(result, face.getFaceType());
                newFace.setOriginalBuildingID(face.getOriginalBuildingID());
//                debug.show("Splitting road faces",
//                           new GMLFaceShapeInfo(face, "Original face", Constants.BLACK, null, true),
//                           new GMLFaceShapeInfo(newFace, "New face", Constants.RED, Constants.TRANSPARENT_RED, true));
            }
        }
        return newFaceCount;
    }
*/
}