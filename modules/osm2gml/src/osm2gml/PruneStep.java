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

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

public class PruneStep extends ConvertStep {
    private GMLMap gmlMap;

    /**
       Construct a PruneStep.
       @param gmlMap The GMLMap to use.
    */
    public PruneStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Pruning nodes and edges";
    }

    @Override
    protected void step() {
        setProgressLimit(gmlMap.getEdges().size() + gmlMap.getNodes().size());
        int edgeCount = 0;
        int nodeCount = 0;
        // Any edge that is not part of a face can be pruned
        setStatus("Pruning edges");
        for (GMLEdge next : gmlMap.getEdges()) {
            if (gmlMap.getAttachedFaces(next).isEmpty()) {
                gmlMap.removeEdge(next);
                ++edgeCount;
            }
            bumpProgress();
        }
        // Any node that is not part of an edge can be pruned
        setStatus("Pruning nodes");
        for (GMLNode next : gmlMap.getNodes()) {
            if (gmlMap.getAttachedEdges(next).isEmpty()) {
                gmlMap.removeNode(next);
                ++nodeCount;
            }
            bumpProgress();
        }
        setStatus("Removed " + edgeCount + " edges and " + nodeCount + " nodes");
    }
}