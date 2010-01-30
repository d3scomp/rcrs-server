package gis2.gml.objects;

import gis2.gml.manager.GMLWorldManager;

public class GMLObject {

    private GMLID id;
    private GMLWorldManager manager;

    public GMLObject(GMLID idd, GMLWorldManager m) {
        id = idd;
        manager = m;
    }

    public GMLID getID() {
        return id;
    }

    public GMLWorldManager getManager() {
        return manager;
    }
}