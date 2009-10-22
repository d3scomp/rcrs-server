package osm2gml.buildings;

import osm2gml.gml.GMLFace;
import osm2gml.gml.FaceType;
import osm2gml.gml.GMLMap;
import osm2gml.gml.GMLNode;
import osm2gml.gml.GMLDirectedEdge;
import osm2gml.ConvertTools;
import osm2gml.buildings.row.RowFiller;
import osm2gml.buildings.row.RectangularDuplexRowFiller;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;

/**
   A BuildingSpaceFiller that fills a space with row housing.
*/
public class RowHousingBuildingSpaceFiller implements BuildingSpaceFiller {
    private ShapeDebugFrame debug;
    private double sizeOf1m;
    private Random random;

    /**
       Construct a new RowHousingBuildingSpaceFiller.
       @param sizeOf1m The size of 1m in GMLMap units.
       @param random The random number generator to use.
       @param debug The ShapeDebugFrame to use.
    */
    public RowHousingBuildingSpaceFiller(double sizeOf1m, Random random, ShapeDebugFrame debug) {
        this.debug = debug;
        this.sizeOf1m = sizeOf1m;
        this.random = random;
    }

    @Override
    public void createBuildings(GMLFace space, GMLMap map) {
        // Sort the edges of the space by length
        List<GMLDirectedEdge> allEdges = space.getEdges();
        Collections.sort(allEdges, new EdgeLengthComparator());
        Set<GMLFace> newFaces = new HashSet<GMLFace>();
        RowFiller filler = createRandomFiller();
        for (GMLDirectedEdge next : allEdges) {
            Set<GMLFace> edgeFaces = filler.fillRow(next, map);
            //            debug.show("Next row faces", ConvertTools.createGMLDebugShapes(edgeFaces));
            // Remove new faces that overlap with existing ones
            for (Iterator<GMLFace> it = edgeFaces.iterator(); it.hasNext();) {
                GMLFace newFace = it.next();
                boolean good = true;
                for (GMLFace testFace : map.getFaces()) {
                    if (testFace == newFace) {
                        continue;
                    }
                    if (newFace.intersects(testFace)) {
                        good = false;
                        break;
                    }
                }
                if (good) {
                    newFaces.add(newFace);
                }
                else {
                    map.removeFace(newFace);
                    it.remove();
                }
            }
            //            debug.show("Pruned next row faces", ConvertTools.createGMLDebugShapes(edgeFaces));
        }
        debug.show("All new faces", ConvertTools.createGMLDebugShapes(newFaces));
    }

    private RowFiller createRandomFiller() {
        if (random.nextBoolean()) {
            return RectangularDuplexRowFiller.makeWideFiller(sizeOf1m, random);
        }
        else {
            return RectangularDuplexRowFiller.makeLongFiller(sizeOf1m, random);
        }
    }

    private final static class EdgeLengthComparator implements Comparator<GMLDirectedEdge> {
        public int compare(GMLDirectedEdge e1, GMLDirectedEdge e2) {
            GMLNode start1 = e1.getStartNode();
            GMLNode end1 = e1.getEndNode();
            GMLNode start2 = e2.getStartNode();
            GMLNode end2 = e2.getEndNode();
            double dx1 = end1.getX() - start1.getX();
            double dy1 = end1.getY() - start1.getY();
            double dx2 = end2.getX() - start2.getX();
            double dy2 = end2.getY() - start2.getY();
            double l1 = Math.hypot(dx1, dy1);
            double l2 = Math.hypot(dx2, dy2);
            if (l1 < l2) {
                return 1;
            }
            if (l2 < l1) {
                return -1;
            }
            return 0;
        }
    }
}