package traffic3.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.geom.Rectangle2D;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import traffic3.objects.TrafficObject;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.simulator.SimulatorException;
import traffic3.io.ParserNotFoundException;

import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

import org.util.xml.parse.XMLParseException;
import traffic3.io.AutoVersionSelectParser;
import traffic3.io.Parser;
import traffic3.io.RCRSGML1;
import traffic3.io.RCRSGML0;
import traffic3.io.RCRSAgent1;

/**
 *
 */
public class WorldManager {

    private Map<String, TrafficObject> mapIDTrafficObject = new HashMap<String, TrafficObject>();
    private Map<String, TrafficArea> mapIDTrafficArea = new HashMap<String, TrafficArea>();
    private Map<String, TrafficAreaNode> mapIDTrafficAreaNode = new HashMap<String, TrafficAreaNode>();
    private Map<String, TrafficAreaEdge> mapIDTrafficAreaEdge = new HashMap<String, TrafficAreaEdge>();
    private Map<String, TrafficAgent> mapIDTrafficAgent = new HashMap<String, TrafficAgent>();
    private Map<String, TrafficBlockade> mapIDTrafficBlockade = new HashMap<String, TrafficBlockade>();
    private AutoVersionSelectParser autoParser;
    private Parser[] availableParser;
    private int unique = 1;

    private List<WorldManagerListener> changeListenerList = new ArrayList<WorldManagerListener>();

    /**
     * Constructor.
     */
    public WorldManager() {
        Parser[] parserList = new Parser[]{new RCRSGML0(),
                                            new RCRSGML1(),
                                            new RCRSAgent1()};
        autoParser = new AutoVersionSelectParser(parserList, true);
        availableParser = parserList;
        clear();
    }

    /**
     * Remove all the objects in this world.
     */
    public void clear() {
        mapIDTrafficObject.clear();
        mapIDTrafficArea.clear();
        mapIDTrafficAreaNode.clear();
        mapIDTrafficAreaEdge.clear();
        mapIDTrafficAgent.clear();
        fireRemoved(this, null);
    }

    /**
     * Get unique id which starts with type.
     * @param type type
     * @return type[number]
     */
    public String getUniqueID(String type) {
        /*
        for (int i = 0;; i++) {
            String id = type + i;
            if (mapIDTrafficObject.get(id) == null) {
                return id;
            }
        }
        */
        return type + (unique++);
    }

    /**
     * append object without check.
     * @param tobjectList tobject list
     * @throws WorldManagerException ex.id is not unique
     */
    public void appendWithoutCheck(TrafficObject... tobjectList) throws WorldManagerException {
        for (TrafficObject tobject : tobjectList) {
            appendWithoutCheck(tobject);
        }
        fireAdded(this, tobjectList);
    }

    /**
     * Append TrafficObject without checking parameters.
     * Check validity of TrafficObjects's parameter is depend on other TrafficObjects.
     * So you must call check() after inserting all the TrafficObjects.
     * @param tobject object that will be appended
     * @throws WorldManagerException ex.id is not unique
     */
    public void appendWithoutCheck(TrafficObject tobject) throws WorldManagerException {
        appendWithoutCheckSync(tobject);
        fireAdded(this, new TrafficObject[]{tobject});
    }

    private synchronized void appendWithoutCheckSync(TrafficObject tobject) throws WorldManagerException {
        String id = tobject.getID();
        if (mapIDTrafficObject.get(id) != null) {
            throw new WorldManagerException("id[" + id + "] already exists.");
        }

        mapIDTrafficObject.put(id, tobject);
        if (tobject instanceof TrafficArea) {
            mapIDTrafficArea.put(id, (TrafficArea)tobject);
        }
        else if (tobject instanceof TrafficAreaNode) {
            mapIDTrafficAreaNode.put(id, (TrafficAreaNode)tobject);
        }
        else if (tobject instanceof TrafficAreaEdge) {
            mapIDTrafficAreaEdge.put(id, (TrafficAreaEdge)tobject);
        }
        else if (tobject instanceof TrafficAgent) {
            synchronized (mapIDTrafficAgent) {
                mapIDTrafficAgent.put(id, (TrafficAgent)tobject);
            }
        }
        else if (tobject instanceof TrafficBlockade) {
            mapIDTrafficBlockade.put(id, (TrafficBlockade)tobject);
            tobject.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        fireMapUpdated(e, null);
                    }
                });
        }
        //log("append: "+tobject);
    }

    /**
     * remove object.
     * @param tobject object
     */
    public synchronized void remove(TrafficObject tobject) throws WorldManagerException {
        String id = tobject.getID();
        if (mapIDTrafficObject.get(id) == null) {
            throw new WorldManagerException("id[" + id + "] doesnot exists.");
        }
        mapIDTrafficObject.remove(id);
        if (tobject instanceof TrafficAgent) {
            synchronized (mapIDTrafficAgent) {
                TrafficAgent agent = (TrafficAgent)tobject;
                TrafficArea area = agent.getArea();
                if (area != null) {
                    area.removeAgent(agent);
                }
                mapIDTrafficAgent.remove(id);
                System.err.println("warning: this operation is not safe.");
            }
            fireAgentUpdated(this, new TrafficObject[]{tobject});
        }
        else {
            if (tobject instanceof TrafficArea) {
                mapIDTrafficArea.remove(id);
                System.err.println("warning: this operation is not safe.");
            }
            else if (tobject instanceof TrafficAreaNode) {
                mapIDTrafficAreaNode.remove(id);
                System.err.println("warning: this operation is not safe.");
            }
            else if (tobject instanceof TrafficAreaEdge) {
                mapIDTrafficAreaEdge.remove(id);
                System.err.println("warning: this operation is not safe.");
            }
            else if (tobject instanceof TrafficBlockade) {
                mapIDTrafficBlockade.remove(id);
            }
            fireMapUpdated(this, new TrafficObject[]{tobject});
        }
        log("append: " + tobject);
    }

    /**
     * notify step finished.
     * this mthod is for repaint
     * @param simulator simulator
     */
    public void stepFinished(Object simulator) {
        fireAgentUpdated(simulator, null);
    }

    /**
     * Calculate range that containing  all the network node and area node.
     * @return range
     */
    public Rectangle2D.Double calcRange() {
        TrafficAreaNode[] tanl = mapIDTrafficAreaNode.values().toArray(new TrafficAreaNode[0]);
        Rectangle2D.Double range = null;
        for (int i = 0; i < tanl.length; i++) {
            if (range == null) {
                range = new Rectangle2D.Double(tanl[i].getX(), tanl[i].getY(), 0, 0);
            }
            else {
                range.add(tanl[i].getX(), tanl[i].getY());
            }
        }
        return range;
    }

    /**
     * Check validity of all the TrafficObject.
     * For example, Traffic object has neighbors ids.
     * And this TrafficObjects create buf of neighbors instance at this time.
     */
    public void check() throws WorldManagerException {
        List<SimulatorException> exceptionList = new ArrayList<SimulatorException>();
        WorldManagerException exc = null;
        try {
            synchronized (mapIDTrafficObject) {
                TrafficObject[] values = mapIDTrafficObject.values().toArray(new TrafficObject[0]);
                for (int i = 0; i < values.length; i++) {
                    try {
                        values[i].checkObject();
                    }
                    catch (Exception exception) {
                        exceptionList.add(new SimulatorException(exception.getMessage()));
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            exc = new WorldManagerException(e.getMessage());
        }
        fireChanged(this, null);
        if (exceptionList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Exception e : exceptionList) {
                sb.append(e.getMessage()).append("\n");
            }
            alert(sb, "error");
            throw new WorldManagerException(sb.toString());
        }
        if (exc != null) {
            throw exc;
        }
    }

    /**
     * Get copy of all the TrafficObjectList.
     * @return all the objects
     */
    public synchronized TrafficObject[] getAll() {
        return mapIDTrafficObject.values().toArray(new TrafficObject[0]);
    }

    /**
     * Get copy of all the Blockade.
     * @return all the blockades
     */
    public synchronized TrafficBlockade[] getBlockadeList() {
        return mapIDTrafficBlockade.values().toArray(new TrafficBlockade[0]);
    }

    /**
     * Get copy of all the TrafficArea.
     * @return  all the areas
     */
    public synchronized  TrafficArea[] getAreaList() {
        return mapIDTrafficArea.values().toArray(new TrafficArea[0]);
    }

    /**
     * Get copy of all the TrafficAreaConnectorEdge.
     * @return all the edges (of area)
     */
    public synchronized TrafficAreaEdge[] getAreaConnectorEdgeList() {
        return mapIDTrafficAreaEdge.values().toArray(new TrafficAreaEdge[0]);
    }

    /**
     * Get copy of all the TrafficAreaNodeList.
     * @return all the nodes (of area)
     */
    public synchronized TrafficAreaNode[] getAreaNodeList() {
        return mapIDTrafficAreaNode.values().toArray(new TrafficAreaNode[0]);
    }

    /**
     * Get copy of all the TrafficAgentList.
     * @return all the agents
     */
    public synchronized TrafficAgent[] getAgentList() {
        TrafficAgent[] result = null;
        synchronized (mapIDTrafficAgent) {
            result = mapIDTrafficAgent.values().toArray(new TrafficAgent[0]);
        }
        return result;
    }

    /**
     * Import objects from file.
     * @param file file
     * @throws Exception exception
     */
    public void open(File file) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException, WorldManagerException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (!file.getName().endsWith(".gml") && !file.getName().endsWith(".xml")) {
            throw new IOException("File extension does not match: " + file.getName());
        }
        log("open file:" + file, "information");
        //open(new FileInputStream(file));
        autoParser.input(this, file);
        fireInputted(this, null);
    }

    /**
     * Import objects from url.
     * @param url url
     * @throws Exception exception
     */
    public void open(URL url) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException, WorldManagerException {
        log("open file:" + url, "information");
        //open(new FileInputStream(file));
        autoParser.input(this, url);
        fireInputted(this, null);
    }

    /**
     * save to file.
     * @param file file
     * @param parser parser
     * @throws Exception exception
     */
    public void save(File file, Parser parser) throws FileNotFoundException, IOException {
        parser.output(this, new FileOutputStream(file));
    }

    /**
     * get parser list.
     * @return list of available parser
     */
    public Parser[] getParserList() {
        return availableParser;
    }

    /**
     * find area.
     * @param x x
     * @param y y
     * @return area
     */
    public TrafficArea findArea(double x, double y) {
        TrafficArea[] areaList = getAreaList();
        for (int i = 0; i < areaList.length; i++) {
            if (areaList[i].contains(x, y, 0)) {
                return areaList[i];
            }
        }
        return null;
    }

    /**
     * Getf nearlest TrafficAreaNode.
     * @param x x
     * @param y y
     * @param z z
     * @return node
     */
    public TrafficAreaNode getNearlestAreaNode(double x, double y, double z) {
        TrafficAreaNode[] nodeList = getAreaNodeList();
        if (nodeList.length == 0) {
            return null;
        }
        TrafficAreaNode minObject = nodeList[0];
        double minDistance = minObject.getDistance(x, y, z);
        for (int i = 1; i < nodeList.length; i++) {
            double distance = nodeList[i].getDistance(x, y, z);
            if (distance < minDistance) {
                minDistance = distance;
                minObject = nodeList[i];
            }
        }
        return minObject;
    }

    /**
     * Create AreaNode that locate (x, y, z).
     * If already exists the same location (it means this.x=x, this.y=y,this.z=z) then return the instance that already exists.
     * Else create a new Instance.
     * @param x x
     * @param y y
     * @param z z
     * @return node
     */
    public TrafficAreaNode createAreaNode(double x, double y, double z) throws WorldManagerException {
        TrafficAreaNode nearlest = getNearlestAreaNode(x, y, z);
        if (nearlest.getDistance(x, y, z) == 0.0) {
            return nearlest;
        }
        TrafficAreaNode node = new TrafficAreaNode(this);
        node.setLocation(x, y, z);
        appendWithoutCheck(node);
        return node;
    }

    /**
     * Get object by id.
     * @param id id
     * @return object
     */
    public TrafficObject getTrafficObject(String id) {
        return mapIDTrafficObject.get(id);
    }

    /**
     * Add change listener.
     * @param listener listener
     */
    public void addWorldManagerListener(WorldManagerListener listener) {
        changeListenerList.add(listener);
    }

    /**
     * Add remove change listener.
     * @param listener listener
     * @return successed to remove
     */
    public boolean removeWorldManagerListener(WorldManagerListener listener) {
        return changeListenerList.remove(listener);
    }

    /**
     * notify inputted objects.
     * @param source source
     */
    public void notifyInputted(Object source) {
        fireInputted(source, null);
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireInputted(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.inputted(e);
            } }, "Notify changed of world manager").start();
        }
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireAdded(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.added(e);
            } }, "Notify changed of world manager").start();
        }
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireRemoved(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.removed(e);
            } }, "Notify changed of world manager").start();
        }
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireChanged(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.changed(e);
            } }, "Notify changed of world manager").start();
        }
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireMapUpdated(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.mapUpdated(e);
            } }, "Notify changed of world manager").start();
        }
    }

    /**
     * fire change.
     * @param source source
     * @param changed changed objects
     */
    protected void fireAgentUpdated(Object source, TrafficObject[] changed) {
        final WorldManagerEvent e = new WorldManagerEvent(source, changed);
        for (WorldManagerListener it : changeListenerList) {
            final WorldManagerListener tmp = it;
            new Thread(new Runnable() { public void run() {
                tmp.agentUpdated(e);
            } }, "Notify changed of world manager").start();
        }
    }
}