package traffic3;


/**
 *
 */
public class LaunchException extends Exception {

    /**
     * Constructor.
     * @param m message
     */
    public LaunchException(String m) {
        super(m);
    }

    /**
     * Constructor.
     * @param m exception
     */
    public LaunchException(Exception m) {
        super(m);
    }
}
