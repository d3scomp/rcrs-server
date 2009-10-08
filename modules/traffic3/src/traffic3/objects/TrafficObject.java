package traffic3.objects;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import traffic3.manager.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

/**
 * 
 */
public abstract class TrafficObject {

    /**
     * ID
     * cannot be null
     */
    private String id_;

    /**
     * The world manager that this object belong to.
     * cannot be null
     */
    private WorldManager world_manager_;

    /**
     * 
     * 
     */
    private ArrayList<ChangeListener> change_listener_list_ = new ArrayList<ChangeListener>();

    /**
     * 
     * 
     */
    protected volatile boolean checked_ = false;

    /**
     * This constructor must not be called.
     */
    private TrafficObject() throws Exception {
	throw new Exception("must not be called.");
    }

    /**
     * ID will automattically be decided.
     * You have to add this object to world_manager manually.
     * This constructor use WorldManager.getUniqueID("_");
     * @param wrold_manager WorldManager which this object will belong to.
     */
    public TrafficObject(WorldManager world_manager) {
	this(world_manager, world_manager.getUniqueID("_"));
    }

    /**
     * 
     * You have to add this object to world_manager manually.
     * @param wrold_manager WorldManager which this object will belong to.
     * @param id id of this object
     */
    public TrafficObject(WorldManager world_manager, String id) {
	id_ = id;
	world_manager_ = world_manager;
    }

    /**
     * Whether parameters of this object is validated or not.
     * @return validated or not
     */
    public boolean isChecked() {
	return checked_;
    }

    /**
     * Check parameters of this object.
     * For example, If this object has a link to other object then instance must be exists in WorldManager.
     * This method have to handle checked_.
     */
    public abstract void checkObject() throws Exception;

    /**
     * get ID
     * not null
     * @return id of this object
     */
    public String getID() {
	return id_;
    }

    /**
     * 
     */
    public WorldManager getManager() {
	return world_manager_;
    }

    public void addChangeListener(ChangeListener listener) {
	change_listener_list_.add(listener);
    }
    public boolean removeChangeListener(ChangeListener listener) {
	return change_listener_list_.remove(listener);
    }
    protected void fireChanged() {
	ChangeEvent e = new ChangeEvent(this);
	for(Iterator<ChangeListener> it=change_listener_list_.iterator(); it.hasNext(); )
	    it.next().stateChanged(e);
    }
    public String toLongString() {
	return toString();
    }

}
