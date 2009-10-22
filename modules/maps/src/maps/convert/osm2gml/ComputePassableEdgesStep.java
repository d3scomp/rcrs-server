package maps.convert.osm2gml;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLFace;
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

import maps.convert.ConvertStep;

public class ComputePassableEdgesStep extends ConvertStep {
    private GMLMap gmlMap;

    /**
       Construct a ComputePassableEdgesStep.
       @param gmlMap The GMLMap to use.
    */
    public ComputePassableEdgesStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Computing passable edges";
    }

    @Override
    protected void step() {
        /*
        setProgressLimit(gmlMap.getEdges().size());
        // For each edge see if it is shared by two road faces
        // If so, make it passable.
        int count = 0;
        for (GMLEdge next : gmlMap.getEdges()) {
            int roadCount = 0;
            for (GMLFace face : gmlMap.getAttachedFaces(next)) {
                switch (face.getFaceType()) {
                case ROAD:
                case INTERSECTION:
                    ++roadCount;
                    break;
                default:
                    // Ignore
                    break;
                }
            }
            if (roadCount > 1) {
                next.setPassable(true);
                ++count;
            }
            bumpProgress();
        }
        setStatus("Made " + count + " edges passable");
        */
    }
}