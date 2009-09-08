package traffic3.objects;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

import traffic3.objects.area.*;
import traffic3.manager.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

public class TrafficAgent extends TrafficObject {
    
    private int debug_mode_ = 0;
    private double x_, y_, z_;
    private double vx_, vy_, vz_;
    private double fx_, fy_, fz_;
    private double radius_;
    private String type_;
    private Color color_;

    // private TrafficNetworkPoint now_network_destination_;
    private TrafficAreaNode now_destination_;
    private TrafficAreaNode final_destination_;
    private TrafficAreaEdge now_destination_edge_;
    private double v_limit_;
    // private double m_ = 10;
    // private TrafficNetworkPoint now_network_point_;
    private TrafficArea now_area_;
    private boolean is_network_mode_;

    public TrafficAgent(WorldManager world_manager) {
	super(world_manager);
	init();
    }
    public TrafficAgent(WorldManager world_manager, String id) {
	super(world_manager, id);
	init();
    }

    public boolean isNetworkMode() {
	return is_network_mode_;
    }


    private void init() {
	//v_limit_ = mm2map(0.1+Math.random()*0.4); // [mm/ms]
	// radius_ = mm2map(300+400*Math.random());
	// radius_ = mm2map(2000);
	// radius_ = mm2map(500);
	// radius_ = 200;
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	radius_ = 0.2; //[m]
	v_limit_ = 10.0/9.0; //[m/s]
=======
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
=======
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	//v_limit_ = radius_*0.001*(Math.random()*0.5+0.5);
	//v_limit_ = 0.5 + 0.1*(Math.random()-0.5);
	//alert("vlimit: "+v_limit_);
	radius_ = 0.2; //[m]
	v_limit_ = 10.0/9.0; //[m/s]
    }

    public void setColor(Color color) {
	color_ = color;
    }
    public Color getColor() {
	return color_;
    }
    
    private double total_distance_ = 0;
    public void clearLogDistance() {
	total_distance_ = 0;
    }
    public double getLogDistance() {
	return total_distance_;
    }
    /*
    public void setLocation(TrafficNetworkPoint now_network_point) {
	is_network_mode_ = true;
	//if()
	    now_destination_ = null;
	now_network_point_ = now_network_point;
	double x = now_network_point_.getX();
	double y = now_network_point_.getY();
	double z = now_network_point_.getZ();
	double dx = x-x_;
	double dy = y-y_;
	double dz = y-y_;
	total_distance_ += Math.sqrt(dx*dx+dy*dy+dz*dz);
	x_ = x;
	y_ = y;
	z_ = z;
	if(now_area_!=null)
	    now_area_.removeAgent(this);
	
	//alert("newtwork move: "+now_network_point);
    }
    */

    public TrafficArea getArea() {
	return now_area_;
    }
    
    public void setLocation(double x, double y, double z) {
	double dx = x-x_;
	double dy = y-y_;
	double dz = y-y_;
	total_distance_ += Math.sqrt(dx*dx+dy*dy+dz*dz);
	x_ = x;
	y_ = y;
	z_ = z;
	if(now_area_==null || !now_area_.contains(x, y, z)) {
	    TrafficArea area = getManager().findArea(x, y);
	    if(area!=null) {
		now_destination_ = null;
		if(now_area_!=null)
		    now_area_.removeAgent(this);
		now_area_ = area;
		area.addAgent(this);
		if(now_area_ == null)
		    is_network_mode_ = false;
		else
		    is_network_mode_ = !now_area_.isSimulateAsOpenSpace();
	    } else {
		log("area is null!"+this);
	    }
	}
	//now_network_point_ = null;
	//alert("free space move: "+x+","+y+","+z);
    }
    public double getX() { return x_; }
    public double getY() { return y_; }
    public double getZ() { return z_; }
    public double getFX() { return fx_; }
    public double getFY() { return fy_; }
    public double getVX() { return vx_; }
    public double getVY() { return vy_; }

    public void setRadius(double radius) {
	radius_ = radius;
    }
    public double getRadius() {
	return radius_;
    }
    public void setDestination(TrafficAreaNode destination) {
	final_destination_ = destination;
	now_destination_ = null;
	//TrafficArea goal = getManager().findArea(destination.getX(), destination.getY());
	plan();
    }
    public TrafficAreaNode getFinalDestination() {
	return final_destination_;
    }
    public TrafficAreaNode getNextDestination() {
	return now_destination_;
    }
    /*
    public static double map2mm(double map) {
        return 40000000000.0*map/360.0;
    }
    public static double mm2map(double mm) {
	return 360.0*mm/40000000000.0;
    }
    */

    public void plan() {
	plan_area();
    }
    /*
    public void plan_network() {

	if(now_destination_ == null)
	    try{
		planDestination();
	    }catch(Exception e){ alert(e, "error"); }
	fx_ = 0;
	fy_ = 0;
	fz_ = 0;
    }
    */
    public void plan_area() {

	if(now_destination_ == null)
	    try{
		planDestination();
	    }catch(Exception e){ alert(e, "error"); }
	//	if(destination_ == null) return ;

	if(is_network_mode_) return ;
	
	double destx = 0;
	double desty = 0;
	double destz = 0;
	if(now_destination_!=null) {
	    double dx = now_destination_.getX()-x_;
	    double dy = now_destination_.getY()-y_;
	    double dz = now_destination_.getZ()-z_;
	    double dist = Math.sqrt(dx*dx+dy*dy+dz*dz);

	    if(now_destination_!=final_destination_ && now_destination_edge_!=null && dist<now_destination_edge_.length()/2) {
		double edist = now_destination_edge_.distance(x_, y_);
		TrafficAreaNode n1 = now_destination_edge_.getDirectedNodes()[0];
		TrafficAreaNode n2 = now_destination_edge_.getDirectedNodes()[1];
		double ndx = (n1.getX()-n2.getX());
		double ndy = (n1.getY()-n2.getY());
		double ndz = (n1.getZ()-n2.getZ());
		double ndist = Math.sqrt(ndx*ndx+ndy*ndy+ndz*ndz);
		ndx /= ndist;
		ndy /= ndist;
		ndz /= ndist;
		
		double cdx = x_ - n2.getX();
		double cdy = y_ - n2.getY();
		double cdz = z_ - n2.getZ();
		double dd = cdx*ndx + cdy*ndy + cdz*ndz;
		
		dx = dd*ndx-cdx;
		dy = dd*ndy-cdy;
		dz = dd*ndz-cdz;

		dist = Math.sqrt(dx*dx+dy*dy+dz*dz);

	    }
	    dx /= dist;
	    dy /= dist;
	    dz /= dist;


	    if(now_destination_ == final_destination_) {
		dx = Math.min(v_limit_, 0.001*dist)*dx;
		dy = Math.min(v_limit_, 0.001*dist)*dy;
		dz = Math.min(v_limit_, 0.001*dist)*dz;
	    } else {
		dx = v_limit_*dx;
		dy = v_limit_*dy;
		dz = v_limit_*dz;
	    }

	    //destx = 0.0001*(dx-vx_);
	    //desty = 0.0001*(dy-vy_);
	    //destz = 0.0001*(dz-vz_);

	    destx = 0.1*(dx-vx_);
	    desty = 0.1*(dy-vy_);
	    destz = 0.1*(dz-vz_);
	} else {
	    destx = 0.1*(-vx_);
	    desty = 0.1*(-vy_);
	    destz = 0.1*(-vz_);
	}
	if(Double.isNaN(destx) || Double.isNaN(desty) || Double.isNaN(destz))
	    destx=desty=destz=0;

	// for agents
	double sumopx = 0;
	double sumopy = 0;
	double sumopz = 0;
	{
	    //double A = 0.000004;
	    //double B = 1000.0;
	    //double k = 0.0000005;
	    double A = 0.000007;
	    double B = 1.0;
	    double k = 0.000001;

	    ArrayList<TrafficAgent> agent_list_pickup = new ArrayList<TrafficAgent>();
	    for(TrafficAgent agent : now_area_.getAgentList())
		agent_list_pickup.add(agent);
	    for(TrafficArea area : now_area_.getNeighborList())
		for(TrafficAgent agent : area.getAgentList())
		    agent_list_pickup.add(agent);

	    TrafficAgent[] agent_list = agent_list_pickup.toArray(new TrafficAgent[0]);
	    for(int i=0; i<agent_list.length; i++) {
		if(agent_list[i]==this) continue;
		if(agent_list[i].isNetworkMode()) continue;
		TrafficAgent op = agent_list[i];
		double r = radius_ + op.getRadius();
		double opdx = op.getX() - x_;
		double opdy = op.getY() - y_;
		double opdz = op.getZ() - z_;
		if(Double.isInfinite(opdx)||Double.isInfinite(opdy)) {
		    log("infinity:"+x_+","+op.getX());
		    sumopx += r*0.00001*(Math.random()-0.5);
		    sumopy += r*0.00001*(Math.random()-0.5);
		    continue;
		}
		double opdist = Math.sqrt(opdx*opdx+opdy*opdy+opdz*opdz);
		if(opdist==0||Double.isInfinite(opdist)) {
		    sumopx += r*0.00001*(Math.random()-0.5);
		    sumopy += r*0.00001*(Math.random()-0.5);
		    continue;
		}
		double opdxn = opdx/opdist;
		double opdyn = opdy/opdist;
		double opdzn = opdz/opdist;
		double opp = r-opdist;
		double dopx = -A*Math.exp(opp/B)*opdxn;
		double dopy = -A*Math.exp(opp/B)*opdyn;
		if(!Double.isInfinite(dopx) && !Double.isInfinite(dopy)) {
		    sumopx += dopx;
		    sumopy += dopy;
		}
		if(opp>0) {
		    sumopx += -k*(opp)*opdxn;
		    sumopy += -k*(opp)*opdyn;
		    sumopz += -k*(opp)*opdzn;
		}
		if(Double.isInfinite(sumopx))
		    log(k+","+opp+","+opdxn+":"+-k*(opp)*opdxn);
	    }
	}

	if(Double.isNaN(sumopx) || Double.isNaN(sumopy) || Double.isNaN(sumopz)) {
	    sumopx=sumopy=sumopz=0;
	}
	double d = Math.sqrt(sumopx*sumopx+sumopy*sumopy+sumopz*sumopz);
	double lim = 0.001;
	if(d>lim) {
	    System.out.println(d);
	    sumopx *= lim/d;
	    sumopy *= lim/d;
	    sumopz *= lim/d;
	}

	//for wall
	double sumwx = 0;
	double sumwy = 0;
	double sumwz = 0;
	if(now_area_!=null){
	    //double A = 0.00005;
	    //double B = 100.0;
	    //double k = 0.00001;
	    //double A = 0.00001;
	    //double B = 100.0;
	    //double k = 0.00000001;
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	    double A = 0.001;
	    double B = 0.1;
=======
	    double A = 0.0001; //0.001
	    double B = 200.0; //100
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
=======
	    double A = 0.0001; //0.001
	    double B = 200.0; //100
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	    double k = 0.00001;
	    double r = getRadius();
	    TrafficArea area = now_area_;
	    if(area!=null){
		Line2D[] line_list = area.getNeighborWallList();
		for(int j=0; j<line_list.length ; j++) {
		    Point2D p1 = line_list[j].getP1();
		    Point2D p2 = line_list[j].getP2();
		    double p1_dist = p1.distance(x_, y_);
		    double p2_dist = p2.distance(x_, y_);
		    double p12_dist = p1_dist+p2_dist;
		    p1_dist /= p12_dist;
		    p2_dist /= p12_dist;
		    double wdx = (p1.getX()*p2_dist+p2.getX()*p1_dist)-x_;
		    double wdy = (p1.getY()*p2_dist+p2.getY()*p1_dist)-y_;
		    double wdist = Math.sqrt(wdx*wdx + wdy*wdy);
		    double wdxn = wdx/Math.abs(wdist);
		    double wdyn = wdy/Math.abs(wdist);
		    sumwx += -A*Math.exp((r-wdist)/B)*wdxn;
		    sumwy += -A*Math.exp((r-wdist)/B)*wdyn;
		    if(wdist < getRadius()) {
			sumwx += -k*(r-wdist)*wdxn;
			sumwy += -k*(r-wdist)*wdyn;
		    }
		}
	    }
	}
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
<<<<<<< Updated upstream:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	if(Double.isNaN(sumwx) || Double.NaN(sumwy) || Double.NaN(sumwz))
=======

	if(Double.isNaN(sumwx) || Double.isNaN(sumwy) || Double.isNaN(sumwz))
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
=======

	if(Double.isNaN(sumwx) || Double.isNaN(sumwy) || Double.isNaN(sumwz))
>>>>>>> Stashed changes:modules/traffic3/src/traffic3/objects/TrafficAgent.java
	    sumwx=sumwy=sumwz=0;

	fx_ = destx + sumopx + sumwx;
	fy_ = desty + sumopy + sumwy;
	fz_ = destz + sumopz;

	if(Double.isNaN(fx_) || Double.isNaN(fy_) || Double.isNaN(fz_))
	    fx_=fy_=fz_=0;
	// log(fx_);
    }


    private void planDestination() throws Exception {

	if(final_destination_ == null) return ;
	TrafficArea start = getManager().findArea(getX(), getY());
	TrafficArea goal = getManager().findArea(final_destination_.getX(), final_destination_.getY());

	// this should be changed!
	if(start==null || goal==null) {
	    now_destination_ = final_destination_;
	    return ;
	}

	if(start.equals(goal)) {
	    now_destination_ = final_destination_;
	    return ;
	}

	HashMap<TrafficArea, Double> trace_area_map = new HashMap<TrafficArea, Double>();
	HashMap<TrafficArea, TrafficArea> trace_transfer = new HashMap<TrafficArea, TrafficArea>();
	ArrayList<TrafficArea> buf = new ArrayList<TrafficArea>();
	trace_area_map.put(start, 0.0);
	buf.add(start);
	
	//alert("start: "+start, "error");
	//alert("goal: "+goal, "error");
	
	for(int i=0; trace_transfer.get(goal) == null; i++) {
	    TrafficArea[] tmp = buf.toArray(new TrafficArea[0]);
	    buf.clear();
	    for(TrafficArea target : tmp) {
		double distance = trace_area_map.get(target);
		ArrayList<TrafficArea> neighbors = new ArrayList<TrafficArea>();
		for(TrafficArea t : target.getNeighborList()) {
		    neighbors.add(t);
		}

		for(TrafficArea n : neighbors) {
		    double new_distance = distance + target.getDistance(n);
		    
		    if(trace_area_map.get(n)==null || new_distance<trace_area_map.get(n)) {
			trace_area_map.put(n, new_distance);
			trace_transfer.put(n, target);
			buf.add(n);
		    }
		}
	    }
	    if(i>1000) throw new Exception("cannot trace to goal.(step>1000)");
	}
	buf.clear();
	TrafficArea last = goal;
	buf.add(last);
	while(last!=start) {
	    last = trace_transfer.get(last);
	    buf.add(last);
	}
	StringBuffer sblog = new StringBuffer("[");
	for(int i=0; i<buf.size(); i++) {
	    sblog.append(buf.get(i).getID()+",");
	}
	sblog.append("]\n");
	//alert(sblog, "error");
	//log(sblog.toString());
	TrafficArea tnn = buf.get(buf.size()-2);
	TrafficAreaEdge[] edge_list = tnn.getConnector(buf.get(buf.size()-1));
	TrafficAreaEdge selected = edge_list[0];
	double min = selected.distance(x_, y_);
	for(int i=1; i<edge_list.length; i++) {
	    double distance = edge_list[i].distance(x_, y_);
	    if(min>distance) {
		min = distance;
		selected = edge_list[i];
	    }
	}
	now_destination_edge_ = selected;
	TrafficAreaNode[] cons = selected.getDirectedNodes();
	double x = (cons[0].getX()+cons[1].getX())/2;
	double y = (cons[0].getY()+cons[1].getY())/2;
	TrafficAreaNode tan = getManager().createAreaNode(x, y, 0);
	now_destination_ = tan;
    }

    private double step_distance = 0;
    private double step_distance_max = 1000;

    public void step(double dt) {
	if(is_network_mode_) {
	    step_distance += v_limit_*dt;
	    if(debug_mode_>0 || step_distance>step_distance_max) { // debug mode
		// alert("network step: "+now_network_destination_);
		throw new RuntimeException("not supported yet");
	    } // debug mode
	    vx_ = vy_ = vz_ = fx_ = fy_ = fz_ = 0;
	} else {
	    double x = x_ + dt*vx_;
	    double y = y_ + dt*vy_;
	    double z = z_ + dt*vz_;
	    vx_ += dt*fx_;
	    vy_ += dt*fy_;
	    vz_ += dt*fz_;
	    double v = Math.sqrt(vx_*vx_+vy_*vy_+vz_*vz_);
	    if(v>v_limit_) {
		v /= v_limit_;
		vx_ /= v;
		vy_ /= v;
		vz_ /= v;
	    }
	    setLocation(x, y, z);
	}
    }

    public void setType(String type) {
	type_ = type;
    }
    public String getType() {
	return type_;
    }

    public boolean isChecked() { return checked_; }
    public void checkObject() throws Exception { checked_ = true; }
    public String toString() {
	StringBuffer sb = new StringBuffer("TrafficAgent[");
	sb.append("id:").append(getID()).append(";");
	sb.append("x:").append(getX()).append(";");
	sb.append("y:").append(getY()).append(";");
	sb.append("]");
	return sb.toString();
    }
    public String toLongString() {
	StringBuffer sb = new StringBuffer("TrafficAgent[");
	sb.append(getID());
	sb.append("x:").append(getX()).append(";");
	sb.append("y:").append(getY()).append(";");
	sb.append("z:").append(getZ()).append(";");
	sb.append("now area:").append(now_area_).append(";");
	sb.append("is network mode:").append(is_network_mode_).append(";");
	sb.append("now destination:").append(now_destination_).append(";");
	sb.append("final destination:").append(final_destination_).append(";");
	//	sb.append("now area:").append().append(";");
	sb.append("]");
	return sb.toString();
    }
}
