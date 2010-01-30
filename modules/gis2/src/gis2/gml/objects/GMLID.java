package gis2.gml.objects;

public class GMLID {

    private String id;

    public GMLID(String idd) {
        if (idd == null) {
            throw new NullPointerException("GMLID cannot be null");
        }
        id = idd;
    }

    public String getStringValue() {
        return id;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return id;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof GMLID)) {
            return false;
        }
        GMLID og = (GMLID)o;
        String oID = og.getStringValue();
        return (id.equals(oID));
    }
}