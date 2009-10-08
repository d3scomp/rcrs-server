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

/**
 * 
 */
public class TrafficAgent extends TrafficObject {
   
    /**
     * Location.
     */
    private double x_, y_, z_;

    /**
     * Velocity.
     */
    private double vx_, vy_, vz_;

    /**
     * fource.
     */
    private double fx_, fy_, fz_;

    /**
     * Radius.
     */
    private double radius_;

    /**
     * Type.
     */
    private String type_;

    /**
     * Color
     */
    private Color color_;

    /**
     * The destination that this agent wants to go.
     */
    private TrafficAreaNode final_destination_;

    /**
     * 
     */
    private TrafficAreaNode now_destination_;

    /**
     * 
     */
    private TrafficAreaEdge now_destination_edge_;

    /**
     * 
     */
    private double v_limit_;
    // private double m_ = 10;
    // private TrafficNetworkPoint now_network_point_;

    /**
     * Now area that this agent is in.
     * This field can be null, and it means this agent is not in any existed area.
     */
    private TrafficArea now_area_;

    /**
     * Whether this agent is now network mode or area mode.
     */
    private boolean is_network_mode_;

    /**
     * Constractor
     * Id, radius, v_limit will automatically be set.
     * radius: 200
     * v_limit: 0.7+0.1*(Math.random()-0.5);
     * @param world_manager
     */
    public TrafficAgent(WorldManager world_manager) {
	super(world_manager);
	init();
    }

    /**
     * Constractor
     * 
     * @param world_manager
     * @param radius radius
     * @param v_limit velicity limit
     */
    public TrafficAgent(WorldManager world_manager, double radius, double v_limit) {
	super(world_manager);
	init(radius, v_limit);
    }

    /**
     * Constractor
     * @param id
     * @param world_manager
     * @param radius radius
     * @param v_limit velicity limit
     */
    public TrafficAgent(WorldManager world_manager, String id, double radius, double v_limit) {
	super(world_manager, id);
	init(radius, v_limit);
    }

    /**
     * Whether this agent is simulated as network model or area model
     */
    public boolean isNetworkMode() {
	return is_network_mode_;
    }

    /**
     * initialize by default parameter
     */
    private void init() {
	radius_ = 200; //[mm]
	v_limit_ = 0.7 + 0.1*(Math.random()-0.5);
	setColor(Color.green);
    }

    /**
     * initialize by specified parameter
     * @param radius
     * @param v_limit
     */
    private void init(double radius, double v_limit) {
	//v_limit_ = mm2map(0.1+Math.random()*0.4); // [mm/ms]
	// radius_ = mm2map(300+400*Math.random());
	// radius_ = mm2map(2000);
	// radius_ = mm2map(500);
	// radius_ = 200;
	//v_limit_ = radius_*0.001*(Math.random()*0.5+0.5);
	//alert("vlimit: "+v_limit_);
	//v_limit_ = 10.0/9.0; //[mm/ms]
	
	//radius_ = 200; //[mm]
	//v_limit_ = 0.7 + 0.1*(Math.random()-0.5);

	radius_ = radius;
	v_limit_ = v_limit;
	setColor(Color.green);
    }


    /**
     * Get limit of velocity.
     * @return limit of velocity.
     */
    public double getVLimit() {
	return v_limit_;
    }

    /**
     * Set limit of velocity.
     * @param v_limit limit of velocity.
     */
    public void setVLimit(double v_limit) {
	v_limit_ = v_limit;
    }
    
    /**
     * Get color
     * @return color
     */
    public Color getColor() {
	return color_;
    }

    /**
     * Set color
     */
    public void setColor(Color color) {
	color_ = color;
    }

    /**
     * Log 
     */
    private double total_distance_ = 0;

    /**
     * The Last area that agent is in, but except null.
     */
    private TrafficArea last_area_ = null;

    /**
     * 
     */
    public void clearLogDistance() {
	total_distance_ = 0;
    }

    /**
     * 
     */
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
	if(now_area_!=null) {
	    last_area_ = now_area_;
	    return now_area_;
	}
	return last_area_;
    }
    
    private ArrayList<Point2D> position_history = new ArrayList<Point2D>();
    public boolean save_position_history_ = true;
    public Point2D[] getPositionHistory() {
	return position_history.toArray(new Point2D[0]);
    }
    public void clearPositionHistory() {
	position_history.clear();
    }

    private int setLocation_count=0;
    public void setLocation(double x, double y, double z) {
	setLocation_count++;
	double dx = x-x_;
	double dy = y-y_;
	double dz = y-y_;

	if(setLocation_count%60==0 && save_position_history_) {
	    position_history.add(new Point2D.Double(x, y));
	}

	total_distance_ += Math.sqrt(dx*dx+dy*dy+dz*dz);

	x_ = x;
	y_ = y;
	z_ = z;
	if(now_area_==null || !now_area_.contains(x, y, z)) {
	    TrafficArea area = null;
	    if(now_area_!=null)
		for(TrafficArea a : now_area_.getNeighborList())
		    if(a.contains(x, y, z))
			area = a;
	    if(area==null)
		area = getManager().findArea(x, y);

	    if(area!=null) {
		now_destination_ = null;
		if(now_area_!=null)
		    now_area_.removeAgent(this);
		now_area_ = area;
		now_area_.addAgent(this);
		/*
		if(now_area_ == null)
		    is_network_mode_ = false;
		else
		    is_network_mode_ = !now_area_.isSimulateAsOpenSpace();
		*/
	    } else {
		if(now_area_!=null) {
		    now_area_.removeAgent(this);
		}
		now_area_ = null;
		log("cannot find area of agents: "+this);
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

    public TrafficAreaNode getNowDestination() {
	return now_destination_;
    }

    public void plan() {
	
	if(now_destination_ == null)
	    try{
		planDestination();
	    }catch(Exception e){ alert(e, "error"); }

	if(is_network_mode_)
	    plan_network();
	else
	    plan_area();
    }

    public void plan_network() {

	fx_ = 0;
	fy_ = 0;
	fz_ = 0;
	throw new RuntimeException("not supported network mode");
    }

    private final double[] dest = new double[3];
    private final double[] sumop = new double[3];
    private final double[] sumw = new double[3];

    public void plan_area() {
	calcDestinationForce(dest);
	calcAgentsForce(sumop);
	calcWallsForce(sumw);

	fx_ = dest[0] + sumop[0] + sumw[0];
	fy_ = dest[1] + sumop[1] + sumw[1];
	fz_ = dest[2] + sumop[2];

	if(Double.isNaN(fx_) || Double.isNaN(fy_) || Double.isNaN(fz_)) {
	    System.err.println("plan_area(): force is NaN!");
	    fx_=fy_=fz_=0;
	}
    }


    private double[] calcDestinationForce(double[] dest) {

	double destx = 0;
	double desty = 0;
	double destz = 0;
	if(now_destination_!=null) {
	    double dx = now_destination_.getX()-x_;
	    double dy = now_destination_.getY()-y_;
	    //double dz = now_destination_.getZ()-z_;
	    double dist = Math.sqrt(dx*dx+dy*dy);

	    if(now_destination_!=final_destination_ && now_destination_edge_!=null && dist<now_destination_edge_.length()/2) {
		double edist = now_destination_edge_.distance(x_, y_);
		TrafficAreaNode n1 = now_destination_edge_.getDirectedNodes()[0];
		TrafficAreaNode n2 = now_destination_edge_.getDirectedNodes()[1];
		double ndx = (n1.getX()-n2.getX());
		double ndy = (n1.getY()-n2.getY());
		//double ndz = (n1.getZ()-n2.getZ());
		double ndist = Math.sqrt(ndx*ndx+ndy*ndy);
		ndx /= ndist;
		ndy /= ndist;
		//ndz /= ndist;
		
		double cdx = x_ - n2.getX();
		double cdy = y_ - n2.getY();
		//double cdz = z_ - n2.getZ();
		double dd = cdx*ndx + cdy*ndy;
		
		dx = dd*ndx-cdx;
		dy = dd*ndy-cdy;

		assert !(dx==0 && dy==0);
		//dz = dd*ndz-cdz;
		dist = Math.sqrt(dx*dx+dy*dy);
	    }
	    if(dist == 0) {
		dx = 0;
		dy = 0;
	    } else {
		dx /= dist;
		dy /= dist;
		//dz /= dist;
	    }

	    if(now_destination_ == final_destination_) {
		dx = Math.min(v_limit_, 0.001*dist)*dx;
		dy = Math.min(v_limit_, 0.001*dist)*dy;
		//dz = Math.min(v_limit_, 0.001*dist)*dz;
	    } else {
		dx = (1.0)*dx;
		dy = (1.0)*dy;
		//dz = (1.0)*dz;
	    }

	    //destx = 0.0001*(dx-vx_);
	    //desty = 0.0001*(dy-vy_);
	    //destz = 0.0001*(dz-vz_);
	    
	    destx = 0.0002*(dx-vx_);
	    desty = 0.0002*(dy-vy_);
	    //destz = 0.0004*(dz-vz_);
	    assert (!Double.isNaN(vx_) && !Double.isNaN(vy_));
	    assert (!Double.isNaN(destx) && !Double.isNaN(desty));
	} else {
	    destx = 0.0001*(-vx_);
	    desty = 0.0001*(-vy_);
	    //destz = 0.0001*(-vz_);
	    assert (!Double.isNaN(destx) && !Double.isNaN(desty));
	}

	/*
	if(Double.isNaN(destx) || Double.isNaN(desty)) {
	    try{throw new Exception("NaN");}catch(Exception e){e.printStackTrace();}
	    destx=desty=0;
	}
	*/
	dest[0] = destx;
	dest[1] = desty;
	dest[2] = 0;
	return dest;
    }







    private final double AGENT_A = 0.0001;
    private final double AGENT_B = 1000.0;
    private final double AGENT_K = 0.000001;
    private final double RANDOM_X = Math.random();
    private final double RANDOM_Y = Math.random();
    
    private double[] calcAgentsForce(double[] sumop) {

	double sumopx = 0;
	double sumopy = 0;
	double sumopz = 0;

	if(now_area_==null) {
	    Arrays.fill(sumop, 0);
	    return sumop;
	}

	/*
	ArrayList<TrafficAgent> agent_list_pickup = new ArrayList<TrafficAgent>();
	for(TrafficAgent agent : now_area_.getAgentList())
	    agent_list_pickup.add(agent);
	
	//<?> this block should be fixed!
	for(TrafficArea area : now_area_.getNeighborList())
	    for(TrafficAgent agent : area.getAgentList())
		agent_list_pickup.add(agent);
	*/
		
	//	TrafficAgent[] agent_list = agent_list_pickup.toArray(new TrafficAgent[0]);
	//for(int i=0; i<agent_list.length; i++) {

	TrafficArea[] area_list = now_area_.getNeighborList();
	for(int j=-1; j<area_list.length; j++) {
	    TrafficAgent[] agent_list = null;
	    if(j==-1) {
		agent_list = now_area_.getAgentList();
	    } else {
		agent_list = area_list[j].getAgentList();
	    }
	    double opdx, opdy;
	    for(int i=0; i<agent_list.length; i++) {
		TrafficAgent op = agent_list[i];
		if(op==this) continue;
		opdx = op.getX()-x_;
		if(opdx<-3000 || 3000<opdx) continue;
		opdy = op.getY()-y_;
		if(opdy<-3000 || 3000<opdy) continue;
		//double opdz = op.getZ() - z_;
		double r = radius_ + op.getRadius();
		
		double opdist2 = opdx*opdx + opdy*opdy;
		if(opdist2 == 0) {
		    sumopx += 0.001*(RANDOM_X-0.5);
		    sumopy += 0.001*(RANDOM_Y-0.5);
		    continue;
		}
		double opdist = Math.sqrt(opdist2);
		
		double opdxn = opdx/opdist;
		double opdyn = opdy/opdist;
		//double opdzn = opdz/opdist;
		double opp = r - opdist;
		
		double tmp = -AGENT_A*Math.exp(opp/AGENT_B);
		if(Double.isInfinite(tmp)) {
		    System.out.println("calculateAgentsForce(): A result of exp is infinite: exp("+(opp/AGENT_B)+")");
		} else {
		    sumopx += tmp*opdxn;
		    sumopy += tmp*opdyn;
		}
		if(opp>0) {
		    sumopx += -AGENT_K*(opp)*opdxn;
		    sumopy += -AGENT_K*(opp)*opdyn;
		    //sumopz += -WALL_K*(opp)*opdzn;
		}
	    }
	}
	
	double d2 = sumopx*sumopx+sumopy*sumopy;
	//double d = Math.sqrt(sumopx*sumopx+sumopy*sumopy+sumopz*sumopz);
	double lim = 0.0001;
	double lim2 = 0.00000001;
	if(d2>lim2) {
	    //System.out.println("limit force: "+d2);
	    double lpd = 0.001/Math.sqrt(d2);
	    sumopx *= lpd;
	    sumopy *= lpd;
	    //sumopz *= lim/d;
	}

	assert (!Double.isNaN(sumopx) && !Double.isNaN(sumopy));

	sumop[0]=sumopx;
	sumop[1]=sumopy;
	sumop[2]=0;
	return sumop;
    }

    
    private final double WALL_A = 0.005; //0.001
    private final double WALL_B = 100.0; //100
    private final double WALL_K = 0.00001;
    private final double WALL_WIDTH = 0;

    private double[] calcWallsForce(double[] sumw) {

	//for wall
	double sumwx = 0;
	double sumwy = 0;
	double sumwz = 0;
	if(now_area_!=null){
	    Line2D[] line_list = now_area_.getNeighborWallList();
	    double r = getRadius()+WALL_WIDTH;
	    double dx, dy, dist;
	    
	    for(int j=0; j<line_list.length ; j++) {

		Point2D p1 = line_list[j].getP1();
		Point2D p2 = line_list[j].getP2();
		double p1p2_x = p2.getX() - p1.getX();
		double p1p2_y = p2.getY() - p1.getY();
		double p1p_x =  x_        - p1.getX();
		double p1p_y =  y_        - p1.getY();
		double p1p2_dist = Math.sqrt(p1p2_x*p1p2_x + p1p2_y*p1p2_y);
		if(p1p2_dist == 0) continue;
		double d = (p1p2_x*p1p_x + p1p2_y*p1p_y) / p1p2_dist;
		if(d<0) {
		    dist = p1.distance(x_, y_)-r;
		    dx = (x_ - p1.getX())/dist/2;
		    dy = (y_ - p1.getY())/dist/2;
		} else if(p1p2_dist<d) {
		    dist = p2.distance(x_, y_)-r;
		    dx = (x_ - p2.getX())/dist/2;
		    dy = (y_ - p2.getY())/dist/2;
		} else {
		    double p1p2_nx = p1p2_x/p1p2_dist;
		    double p1p2_ny = p1p2_y/p1p2_dist;
		    dx = -d*p1p2_nx + p1p_x;
		    dy = -d*p1p2_ny + p1p_y;
		    dist = Math.sqrt(dx*dx+dy*dy)-r;
		    dx /= dist;
		    dy /= dist;
		    if(Double.isNaN(dist)) {
			System.out.println("c: NaN: Math.sqrt("+(dx*dx+dy*dy)+"): "+dx+","+dy+": "+p1p2_dist);
		    }
		}

		if(dist>3000) continue;
		
		if(dist<0) {
		    //System.out.println("mark@");
		    sumwx += WALL_K*(dist)*dx;
		    sumwy += WALL_K*(dist)*dy;
		} else {
		    
		    double tmp = WALL_A*Math.exp(-(dist)/WALL_B);
		    if(Double.isInfinite(tmp)) {
			System.out.println("calculateWallForce(): A result of exp is infinite: exp("+(-(dist)/WALL_B)+")");
		    } else if(Double.isNaN(tmp)) {
			System.out.println("calculateWallForce(): A result of exp is NaN: exp("+(-(dist)/WALL_B)+")");
		    } else {
			sumwx += tmp*dx;
			sumwy += tmp*dy;
		    }
		}


		    
		
		/*
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
		double wdxn = wdx/wdist;
		double wdyn = wdy/wdist;
		if(wdist < r) {
		    sumwx += -WALL_K*(r-wdist)*wdxn;
		    sumwy += -WALL_K*(r-wdist)*wdyn;
		} else {
		    double tmp = -WALL_A*Math.exp((r-wdist)/WALL_B);
		    sumwx += tmp*wdxn;
		    sumwy += tmp*wdyn;
		}
		*/
	    }
	}
	if(!Double.isNaN(sumwx) && !Double.isNaN(sumwy)) {

	} else {
	    sumwx = 0;
	    sumwy = 0;
	}
	assert (!Double.isNaN(sumwx) && !Double.isNaN(sumwy));

	sumw[0] = sumwx;
	sumw[1] = sumwy;
	sumw[2] = sumwz;
	return sumw;
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
		//System.out.println("v limit");
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
