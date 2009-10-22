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

public class CreateEntrancesStep extends ConvertStep {
    private GMLMap gmlMap;

    /**
       Construct a CreateEntrancesStep.
       @param gmlMap The GMLMap to use.
    */
    public CreateEntrancesStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Creating building entrances";
    }

    @Override
    protected void step() {
        setProgressLimit(gmlMap.getFaces().size());
        int sharedCount = 0;
        int corridorCount = 0;
        for (GMLFace face : gmlMap.getFaces()) {
            if (FaceType.BUILDING.equals(face.getFaceType())) {
                // Look to see if we have any edges shared with a road
                boolean found = false;
                for (GMLDirectedEdge directedEdge : face.getEdges()) {
                    GMLEdge edge = directedEdge.getEdge();
                    if (isSharedWithRoad(edge)) {
                        // Make the edge passable
                        // TODO: Make part of the edge passable
                        // TODO: Make more edges passable if this edge is too short
                        edge.setPassable(true);
                        found = true;
                        ++sharedCount;
                        break;
                    }
                }
                // If we couldn't find a shared edge then we need to create a corridor that connects an edge to a road.
                if (!found) {
                    makeCorrider(face);
                    ++corridorCount;
                }
            }
            bumpProgress();
        }
        setStatus("Made " + sharedCount + " shared edges passable and created " + corridorCount + " corridors");
    }

    private boolean isSharedWithRoad(GMLEdge edge) {
        for (GMLFace face : gmlMap.getAttachedFaces(edge)) {
            if (FaceType.ROAD.equals(face.getFaceType())) {
                return true;
            }
        }
        return false;
    }

    private void makeCorrider(GMLFace face) {
        // Find an edge that is close to a road or intersection
        GMLEdge bestBuildingEdge = null;
        GMLEdge bestRoadEdge = null;
        for (GMLDirectedEdge next : face.getEdges()) {
            GMLEdge buildingEdge = next.getEdge();
            // Look for the nearest road or intersection edge
            
        }
    }
}