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
import java.util.Collections;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

public class MergeFacesStep extends ConvertStep {
    private GMLMap map;

    /**
       Construct a MergeFacesStep.
       @param gmlMap The GMLMap to use.
    */
    public MergeFacesStep(GMLMap gmlMap) {
        super();
        this.map = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Merging adjacent faces";
    }

    @Override
    protected void step() {
        setProgressLimit(map.getNodes().size() + map.getEdges().size());
        // See if we can remove any nodes and merge all their faces together
        int nodeCount = 0;
        int edgeCount = 0;
        int faceCount = 0;
        for (GMLNode node : map.getNodes()) {
            Set<GMLFace> faces = getFacesToMerge(node);
            if (faces != null) {
                mergeFaces(faces, node);
                ++nodeCount;
                faceCount += faces.size() - 1;
            }
            bumpProgress();
        }
        // Now try removing edges
        for (GMLEdge edge : map.getEdges()) {
            Set<GMLFace> faces = getFacesToMerge(edge);
            if (faces != null) {
                mergeFaces(faces, edge);
                ++edgeCount;
                ++faceCount;
            }
            bumpProgress();
        }
        setStatus("Removed " + nodeCount + " nodes, " + edgeCount + " edges and " + faceCount + " faces");
    }

    private Set<GMLFace> getFacesToMerge(GMLNode node) {
        Set<GMLFace> result = new HashSet<GMLFace>();
        for (GMLEdge edge : map.getAttachedEdges(node)) {
            Set<GMLFace> edgeFaces = map.getAttachedFaces(edge);
            if (edgeFaces.size() != 2) {
                // This node has an edge that is a boundary wall. Can't remove it.
                return null;
            }
            result.addAll(edgeFaces);
        }
        // Now check that all faces have the same type and original building ID
        FaceType type = null;
        long id = 0;
        for (GMLFace face : result) {
            FaceType faceType = face.getFaceType();
            long faceID = face.getOriginalBuildingID();
            if (type != null && !type.equals(faceType)) {
                // Different type
                return null;
            }
            if (id != 0 && id != faceID) {
                // Different building ID
                return null;
            }
            type = faceType;
            id = faceID;
        }
        return result;
    }

    private Set<GMLFace> getFacesToMerge(GMLEdge edge) {
        Set<GMLFace> edgeFaces = map.getAttachedFaces(edge);
        if (edgeFaces.size() != 2) {
            // This node has an edge that is a boundary wall. Can't remove it.
            return null;
        }
        Iterator<GMLFace> it = edgeFaces.iterator();
        GMLFace first = it.next();
        GMLFace second = it.next();
        // Now check that both faces have the same type and original building ID
        FaceType type1 = first.getFaceType();
        FaceType type2 = second.getFaceType();
        long id1 = first.getOriginalBuildingID();
        long id2 = second.getOriginalBuildingID();
        if (!type1.equals(type2) ||
            id1 != id2) {
            return null;
        }
        return edgeFaces;
    }

    private void mergeFaces(Set<GMLFace> faces, GMLNode node) {
        GMLFace newFace = mergeFaces(faces, map.getAttachedEdges(node));
        if (newFace != null) {
            // Remove the old node, attached edges and old faces
            map.removeNode(node);
        }
    }

    private void mergeFaces(Set<GMLFace> faces, GMLEdge edge) {
        GMLFace newFace = mergeFaces(faces, Collections.singleton(edge));
        if (newFace != null) {
            // Remove the old edge and old faces
            map.removeEdge(edge);
        }
    }

    private GMLFace mergeFaces(Set<GMLFace> faces, Set<GMLEdge> sharedEdges) {
        GMLFace face = faces.iterator().next();
        FaceType type = face.getFaceType();
        long buildingID = face.getOriginalBuildingID();
        // Build a list of all edges in the merged set
        // Remove the shared edges
        // Then choose any old edge and walk the perimeter
        Set<GMLDirectedEdge> allEdges = new HashSet<GMLDirectedEdge>();
        for (GMLFace nextFace : faces) {
            for (GMLDirectedEdge edge : nextFace.getEdges()) {
                if (!sharedEdges.contains(edge.getEdge())) {
                    allEdges.add(edge);
                }
            }
        }
        GMLDirectedEdge currentEdge = allEdges.iterator().next();
        List<GMLDirectedEdge> newEdges = new ArrayList<GMLDirectedEdge>();
        newEdges.add(currentEdge);
        allEdges.remove(currentEdge);
        GMLNode first = currentEdge.getStartNode();
        GMLNode current = currentEdge.getEndNode();
        while (!current.equals(first)) {
            currentEdge = findNextEdge(current, allEdges);
            allEdges.remove(currentEdge);
            newEdges.add(currentEdge);
            current = currentEdge.getEndNode();
        }
        // Is the new shape convex or a building?
        if (!(FaceType.BUILDING.equals(type) || ConvertTools.isConvex(newEdges))) {
            // Don't create non-convex roads or intersections
            return null;
        }
        // Create a new face
        GMLFace newFace = map.createFace(newEdges, type);
        newFace.setOriginalBuildingID(buildingID);
        return newFace;
    }

    private GMLDirectedEdge findNextEdge(GMLNode start, Set<GMLDirectedEdge> all) {
        for (GMLDirectedEdge next : all) {
            if (start.equals(next.getStartNode())) {
                return next;
            }
        }
        throw new IllegalArgumentException("No edge with the right start node found");
    }
}