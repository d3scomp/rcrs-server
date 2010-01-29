package maps.convert.osm2gml;

import maps.gml.GMLMap;

import maps.convert.ConvertStep;

/**
   This step computes which edges are passable.
*/
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