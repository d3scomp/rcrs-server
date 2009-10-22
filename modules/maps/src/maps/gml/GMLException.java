package maps.gml;

public class GMLException extends Exception {
    public GMLException() {
        super();
    }

    public GMLException(String msg) {
        super(msg);
    }

    public GMLException(Throwable cause) {
        super(cause);
    }

    public GMLException(String msg, Throwable cause) {
        super(msg, cause);
    }
}