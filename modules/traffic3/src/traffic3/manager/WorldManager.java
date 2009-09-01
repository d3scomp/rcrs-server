package traffic3.manager;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

import traffic3.objects.*;
import traffic3.objects.area.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import org.util.xml.parse.*;
import org.util.xml.parse.policy.*;
import org.util.xml.element.*;
import traffic3.io.*;

/**
 * 
 */
public class WorldManager {

    private WorldManager this_ = this;
    private HashMap<String,TrafficObject> map_id_trafficobject_ = new HashMap<String, TrafficObject>();
    private HashMap<String,TrafficArea> map_id_trafficarea_ = new HashMap<String, TrafficArea>();
    private HashMap<String,TrafficAreaNode> map_id_trafficareanode_ = new HashMap<String, TrafficAreaNode>();
    private HashMap<String,TrafficAreaEdge> map_id_trafficareaedge_ = new HashMap<String, TrafficAreaEdge>();
    private HashMap<String,TrafficAgent> map_id_trafficagent_ = new HashMap<String, TrafficAgent>();
    private HashMap<String,TrafficBlockade> map_id_trafficblockade_ = new HashMap<String, TrafficBlockade>();
    private AutoVersionSelectParser auto_parser_;
    private Parser[] available_parser_;
    private int unique_ = 1;
    private Object lock_ = new Object();
    
    private ArrayList<WorldManagerListener> change_listener_list_ = new ArrayList<WorldManagerListener>();

    public WorldManager() {
	Parser[] parser_list = new Parser[]{
	    new RCRSGML1_0_0(),
	    new RCRSAgent1_0_0(),
	};
	auto_parser_ = new AutoVersionSelectParser(parser_list, true);
	available_parser_ = parser_list;
	clear();
    }

    /**
     * Remove all the objects in this world.
     */
    public void clear() {
	map_id_trafficobject_.clear();
	map_id_trafficarea_.clear();
	map_id_trafficareanode_.clear();
	map_id_trafficareaedge_.clear();
	map_id_trafficagent_.clear();
	fireRemoved(this, null);
    }

    /**
     * Get unique id which starts with type.
     * @param type 
     * @return type[number]
     */
    public String getUniqueID(String type) {
	for(int i=0; ; i++) {
	    String id = type+i;
	    if(map_id_trafficobject_.get(id)==null)
		return id;
	}
	//return type+(unique_++);
    }

    public void appendWithoutCheck(TrafficObject[] tobject_list) throws Exception {
	for(TrafficObject tobject : tobject_list)
	    appendWithoutCheck(tobject);
    }


    /**
     * Append TrafficObject without checking parameters.
     * Check validity of TrafficObjects's parameter is depend on other TrafficObjects.
     * So you must call check() after inserting all the TrafficObjects.
     * @param tobject object that will be appended
     */
    public synchronized void appendWithoutCheck(TrafficObject tobject) throws Exception {
	String id = tobject.getID();
	if(map_id_trafficobject_.get(id)!=null) throw new Exception("id["+id+"] already exists.");
	map_id_trafficobject_.put(id, tobject);
	if(tobject instanceof TrafficArea)
	    map_id_trafficarea_.put(id, (TrafficArea)tobject);
	else if(tobject instanceof TrafficAreaNode)
	    map_id_trafficareanode_.put(id, (TrafficAreaNode)tobject);
	else if(tobject instanceof TrafficAreaEdge)
	    map_id_trafficareaedge_.put(id, (TrafficAreaEdge)tobject);
	else if(tobject instanceof TrafficAgent)
	    synchronized(map_id_trafficagent_) {
		map_id_trafficagent_.put(id, (TrafficAgent)tobject);
	    }
	else if(tobject instanceof TrafficBlockade) {
	    map_id_trafficblockade_.put(id, (TrafficBlockade)tobject);
	    tobject.addChangeListener(new ChangeListener(){
		    public void stateChanged(ChangeEvent e) {
			fireMapUpdated(e, null);
		    }
		});
	}
	log("append: "+tobject);
    }

    public synchronized void remove(TrafficObject tobject) throws Exception {
	String id = tobject.getID();
	if(map_id_trafficobject_.get(id)==null) throw new Exception("id["+id+"] doesnot exists.");
	map_id_trafficobject_.remove(id);
	if(tobject instanceof TrafficArea) {
	    map_id_trafficarea_.remove(id);
	    System.err.println("warning: this operation is not safe.");
	} else if(tobject instanceof TrafficAreaNode) {
	    map_id_trafficareanode_.remove(id);
	    System.err.println("warning: this operation is not safe.");
	}else if(tobject instanceof TrafficAreaEdge){
	    map_id_trafficareaedge_.remove(id);
	    System.err.println("warning: this operation is not safe.");
	}else if(tobject instanceof TrafficAgent)
	    synchronized(map_id_trafficagent_) {
		map_id_trafficagent_.remove(id);
		System.err.println("warning: this operation is not safe.");
	    }
	else if(tobject instanceof TrafficBlockade) {
	    map_id_trafficblockade_.remove(id);
	}
	log("append: "+tobject);
    }

    public void stepFinished(Object simulator) {
	fireAgentUpdated(simulator, null);
    }

    /**
     * Calculate range that containing  all the network node and area node.
     */
    public Rectangle2D.Double calcRange() {
	TrafficAreaNode[] tanl = map_id_trafficareanode_.values().toArray(new TrafficAreaNode[0]);
	Rectangle2D.Double range = null;
	for(int i=0; i<tanl.length; i++) {
	    if(range==null)
		range = new Rectangle2D.Double(tanl[i].getX(), tanl[i].getY(), 0, 0);
	    else
		range.add(tanl[i].getX(), tanl[i].getY());
	}

	return range;
    }

    /**
     * Check validity of all the TrafficObject.
     * For example, Traffic object has neighbors ids.
     * And this TrafficObjects create buf of neighbors instance at this time.
     */
    public void check() throws Exception {
	Exception exc = null;
	try{
	    synchronized(map_id_trafficobject_) {
		TrafficObject[] values = map_id_trafficobject_.values().toArray(new TrafficObject[0]);
		for(int i=0; i<values.length; i++)
		    values[i].checkObject();
	    }
	}catch(Exception e) {
	    exc = e;
	}
	fireChanged(this, null);
	if(exc != null) throw exc;
    }

    /**
     * Get copy of all the TrafficObjectList
     */
    public synchronized TrafficObject[] getAll() {
	return map_id_trafficobject_.values().toArray(new TrafficObject[0]);
    }
    public synchronized TrafficBlockade[] getBlockadeList() {
	return map_id_trafficblockade_.values().toArray(new TrafficBlockade[0]);
    }
    /**
     * Get copy of all the TrafficAreaList
     */
    public synchronized  TrafficArea[] getAreaList() {
	return map_id_trafficarea_.values().toArray(new TrafficArea[0]);
    }
    /**
     * Get copy of all the TrafficAreaConnectorEdgeList
     */
    public synchronized TrafficAreaEdge[] getAreaConnectorEdgeList() {
	return map_id_trafficareaedge_.values().toArray(new TrafficAreaEdge[0]);
    }
    /**
     * Get copy of all the TrafficAreaNodeList
     */
    public synchronized TrafficAreaNode[] getAreaNodeList() {
	return map_id_trafficareanode_.values().toArray(new TrafficAreaNode[0]);
    } 
    /**
     * Get copy of all the TrafficAgentList
     */
    public synchronized TrafficAgent[] getAgentList() {
	TrafficAgent[] result = null;
	synchronized(map_id_trafficagent_) {
	    result = map_id_trafficagent_.values().toArray(new TrafficAgent[0]);
	}
	return result;
    }
    /**
     * Import objects from file.
     */
    public void open(File file) throws Exception {
	if(!file.exists()) throw new FileNotFoundException();
	if(file.getName().endsWith(".gml") || file.getName().endsWith(".xml"))
	    ;//ok
	else
	    throw new Exception("File extension does not match: "+file.getName());
	log("open file:"+file, "information");
	//open(new FileInputStream(file));
	auto_parser_.input(this, file);
	fireInputted(this, null);
    }
    public void save(File file, Parser parser) throws Exception {
	parser.output(this, new FileOutputStream(file));
    }
    public Parser[] getParserList() {
	return available_parser_;
    }


    /**
     * find area 
     */
    public TrafficArea findArea(double x, double y) {
	TrafficArea[] area_list = getAreaList();
	for(int i=0; i<area_list.length; i++)
	    if(area_list[i].contains(x, y, 0))
		return area_list[i];
	return null;
    }

    /**
     * Get nearlest TrafficAreaNode
     */
    public TrafficAreaNode getNearlestAreaNode(double x, double y, double z) {
	TrafficAreaNode[] node_list = getAreaNodeList();
	if(node_list.length==0) return null;
	TrafficAreaNode min_object = node_list[0];
	double min_distance = min_object.getDistance(x,y,z);
	for(int i=1; i<node_list.length; i++) {
	    double distance = node_list[i].getDistance(x,y,z);
	    if(distance<min_distance) {
		min_distance = distance;
		min_object = node_list[i];
	    }
	}
	return min_object;
    }
    
    /**
     * Create AreaNode that locate (x, y, z).
     * If already exists the same location (it means this.x=x, this.y=y,this.z=z) then return the instance that already exists.
     * Else create a new Instance.
     */
    public TrafficAreaNode createAreaNode(double x, double y, double z) throws Exception {
	TrafficAreaNode nearlest = getNearlestAreaNode(x,y,z);
	if(nearlest.getDistance(x,y,z) == 0.0) return nearlest;
	TrafficAreaNode node = new TrafficAreaNode(this);
	node.setLocation(x, y, z);
	appendWithoutCheck(node);
	return node;
    }

    /**
     * Get object by id.
     */
    public TrafficObject getTrafficObject(String id) {
	return map_id_trafficobject_.get(id);
    }

    /**
     * Add change listener
     */
    public void addWorldManagerListener(WorldManagerListener listener) {
	change_listener_list_.add(listener);
    }
    /**
     * Add remove change listener
     */
    public boolean removeWorldManagerListener(WorldManagerListener listener) {
	return change_listener_list_.remove(listener);
    }

    public void notifyInputted(Object source) {
	fireInputted(source, null);
    }

    /**
     * fire change
     */
    protected void fireInputted(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.inputted(e);
	    }}, "Notify changed of world manager").start();
	}
    }
    protected void fireAdded(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.added(e);
	    }}, "Notify changed of world manager").start();
	}
    }
    
    protected void fireRemoved(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.removed(e);
	    }}, "Notify changed of world manager").start();
	}
    }
    protected void fireChanged(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.changed(e);
	    }}, "Notify changed of world manager").start();
	}
    }
    protected void fireMapUpdated(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.mapUpdated(e);
	    }}, "Notify changed of world manager").start();
	}
    }
    protected void fireAgentUpdated(Object source, TrafficObject[] changed) {
	final WorldManagerEvent e = new WorldManagerEvent(source, changed);
	for(WorldManagerListener it : change_listener_list_) {
	    final WorldManagerListener tmp = it;
	    new Thread(new Runnable(){public void run(){
		tmp.agentUpdated(e);
	    }}, "Notify changed of world manager").start();
	}
    }
}
