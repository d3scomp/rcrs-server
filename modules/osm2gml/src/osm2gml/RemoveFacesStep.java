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

public class RemoveFacesStep extends ConvertStep {
    private GMLMap gmlMap;

    /**
       Construct a RemoveFacesStep.
       @param gmlMap The GMLMap to use.
    */
    public RemoveFacesStep(GMLMap gmlMap) {
        super();
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Removing extraneous faces";
    }

    @Override
    protected void step() {
        Collection<GMLFace> allFaces = gmlMap.getFaces();
        setProgressLimit(allFaces.size() * 2);
        Set<GMLFace> removed = new HashSet<GMLFace>();
        setStatus("Removing duplicate faces");
        int duplicateCount = 0;
        int interiorCount = 0;
        duplicateCount += removeDuplicates(FaceType.BUILDING, removed, allFaces);
        duplicateCount += removeDuplicates(FaceType.INTERSECTION, removed, allFaces);
        duplicateCount += removeDuplicates(FaceType.ROAD, removed, allFaces);
        setStatus("Removing interior faces");
        interiorCount += removeInterior(FaceType.ROAD, removed, allFaces);
        interiorCount += removeInterior(FaceType.INTERSECTION, removed, allFaces);
        interiorCount += removeInterior(FaceType.BUILDING, removed, allFaces);
        setStatus("Removed " + removed.size() + " faces: " + duplicateCount + " duplicates and " + interiorCount + " interior");
    }

    /**
       Remove all duplicate faces for a particular type.
       @param type The type of face to check.
       @param removed The set of removed faces.
       @param allFaces All faces.
       @return The number of faces removed.
    */
    private int removeDuplicates(FaceType type, Set<GMLFace> removed, Collection<GMLFace> allFaces) {
        int count = 0;
        for (GMLFace first : gmlMap.getFaces(type)) {
            bumpProgress();
            if (removed.contains(first)) {
                continue;
            }
            for (GMLFace second : allFaces) {
                if (removed.contains(second)) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                if (first.isDuplicate(second)) {
                    gmlMap.removeFace(second);
                    removed.add(second);
                    ++count;
                }
            }
        }
        return count;
    }

    /**
       Remove any faces that are entirely inside another face.
       @param type The type of face to check.
       @param removed The set of removed faces.
       @param allFaces All faces.
       @return The number of removed faces.
    */
    private int removeInterior(FaceType type, Set<GMLFace> removed, Collection<GMLFace> allFaces) {
        int count = 0;
        for (GMLFace first : gmlMap.getFaces(type)) {
            bumpProgress();
            if (removed.contains(first)) {
                continue;
            }
            for (GMLFace second : allFaces) {
                if (removed.contains(second)) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                if (first.isEntirelyInside(second)) {
                    gmlMap.removeFace(first);
                    removed.add(first);
                    ++count;
                }
            }
        }
        return count;
    }
}