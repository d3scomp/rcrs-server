package maps.osm;

public class OSMException extends Exception {
    public OSMException() {
        super();
    }

    public OSMException(String msg) {
        super(msg);
    }

    public OSMException(Throwable cause) {
        super(cause);
    }

    public OSMException(String msg, Throwable cause) {
        super(msg, cause);
    }
}