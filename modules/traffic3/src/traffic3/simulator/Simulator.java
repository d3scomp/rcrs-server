package traffic3.simulator;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import traffic3.manager.*;
import traffic3.objects.area.*;
import traffic3.objects.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

public class Simulator {

    private WorldManager world_manager_;
    private double dt_;
    private double time_ = 0;

    public Simulator(WorldManager world_manager, double dt) {
	world_manager_ = world_manager;
	dt_ = dt;
    }

    public void step() {
	TrafficAgent[] agent_list = world_manager_.getAgentList();
	long start = System.currentTimeMillis();
	for(int i=0; i<agent_list.length; i++) {
	    TrafficAgent agent = agent_list[i];
	    /*
	    TrafficAreaNode dest = agent.getDestination();
	    if(dest!=null) {
		// agent.setLocation(dest.getX(), dest.getY(), dest.getZ());
	    }
	    */
	    agent.plan();
	}
	for(int i=0; i<agent_list.length; i++) {
	    TrafficAgent agent = agent_list[i];
	    agent.step(dt_);
	}
	long end = System.currentTimeMillis();
	int diff = (int)(dt_/100 - (end-start));
	if(diff>0) try{Thread.sleep(diff);}catch(Exception exc){}
	time_ += dt_;
    }
    public void setTime(double time) {
	time_ = time;
    }
    public double getTime() {
	return time_;
    }
}
