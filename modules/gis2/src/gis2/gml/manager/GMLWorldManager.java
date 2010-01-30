package gis2.gml.manager;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gis2.gml.objects.GMLObject;
import gis2.gml.objects.GMLID;
import gis2.gml.objects.GMLNode;
import gis2.gml.objects.GMLEdge;
import gis2.gml.objects.GMLFace;
import gis2.gml.objects.GMLAgent;

public class GMLWorldManager {

    private Map<GMLID, GMLObject> mapIDObject = new HashMap<GMLID, GMLObject>();
    private Map<GMLID, GMLNode> mapIDNode = new HashMap<GMLID, GMLNode>();
    private Map<GMLID, GMLEdge> mapIDEdge = new HashMap<GMLID, GMLEdge>();
    private Map<GMLID, GMLFace> mapIDFace = new HashMap<GMLID, GMLFace>();
    private Map<GMLID, GMLAgent> mapIDAgent = new HashMap<GMLID, GMLAgent>();

    public GMLWorldManager () {
        
    }

    public GMLObject get(GMLID id) {
        return mapIDObject.get(id);
    }

    public GMLNode getNode(GMLID id) {
        return mapIDNode.get(id);
    }
    public int nodeSize() {
        return mapIDNode.size();
    }

    public GMLEdge getEdge(GMLID id) {
        return mapIDEdge.get(id);
    }
    public int edgeSize() {
        return mapIDEdge.size();
    }

    public GMLFace getFace(GMLID id) {
        return mapIDFace.get(id);
    }
    public int faceSize() {
        return mapIDFace.size();
    }

    public GMLFace[] findFace(double x, double y) {
        List<GMLFace> list = new ArrayList<GMLFace>();
        for (GMLFace face : toFaceArray(new GMLFace[0])) {
            if (face.getShape().contains(x, y)) {
                list.add(face);
            }
        }
        return list.toArray(new GMLFace[0]);
    }

    public GMLAgent getAgent(GMLID id) {
        return mapIDAgent.get(id);
    }
    public int agentSize() {
        return mapIDAgent.size();
    }

    public void add(GMLObject o) throws GMLWorldManagerException {
        GMLID id = o.getID();
        if (mapIDObject.get(id) != null) {
            throw new GMLWorldManagerException("cannot add object: " + id + " already exists.");
        }
        mapIDObject.put(id, o);
        if (o instanceof GMLNode) {
            mapIDNode.put(id, (GMLNode)o);
        }
        else if (o instanceof GMLEdge) {
            mapIDEdge.put(id, (GMLEdge)o);
        }
        else if (o instanceof GMLFace) {
            mapIDFace.put(id, (GMLFace)o);
        }
        else if (o instanceof GMLAgent) {
            mapIDAgent.put(id, (GMLAgent)o);
        }
        else {
            throw new GMLWorldManagerException("cannot add object: unknown type");
        }
    }

    public GMLFace[] toFaceArray(GMLFace[] arr) {
        return mapIDFace.values().toArray(arr);
    }

    public GMLAgent[] toAgentArray(GMLAgent[] arr) {
        return mapIDAgent.values().toArray(arr);
    }
}