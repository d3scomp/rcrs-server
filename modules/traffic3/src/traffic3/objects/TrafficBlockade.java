package traffic3.objects;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

import traffic3.manager.*;
import traffic3.objects.*;
import traffic3.objects.area.*;
import traffic3.objects.area.event.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import org.util.xml.element.*;

public class TrafficBlockade extends TrafficObject {

    private ArrayList<TrafficAreaListener> area_listener_list_ = new ArrayList<TrafficAreaListener>();
    private ArrayList<TrafficAgent> agent_list_ = new ArrayList<TrafficAgent>();
    private boolean simulate_as_open_space_ = true;
    private String type_;

    // cannot be null
    private double center_x_;
    private double center_y_;
    //private String[] neighbor_area_id_list_;

    // can be null
    private String[] connector_id_list_;
    private String[] unconnector_id_list_;
    private TrafficAreaNode[] node_list_;
    private GeneralPath shape_;

    // these properties will be set at check
    private ArrayList<TrafficAreaEdge> connector_list_ = new ArrayList<TrafficAreaEdge>();
    private ArrayList<TrafficAreaEdge> unconnector_list_ = new ArrayList<TrafficAreaEdge>();
    private ArrayList<Line2D> unconnect_edge_list_ = new ArrayList<Line2D>();
    private ArrayList<TrafficArea> neighbor_area_list_ = new ArrayList<TrafficArea>();
    private ArrayList<Line2D> line_list_ = new ArrayList<Line2D>();

    public TrafficBlockade(WorldManager world_manager, String id) {
	super(world_manager, id);
    }

    public TrafficBlockade(WorldManager world_manager, String id, double cx, double cy, int[] xy_list) {
	super(world_manager, id);
	
	center_x_ = cx;
	center_y_ = cy;

	try{
	    ArrayList<TrafficAreaNode> node_buf = new ArrayList<TrafficAreaNode>();
	    ArrayList<String> directed_edge_id_list = new ArrayList<String>();
	    ArrayList<String> ndirected_edge_id_list = new ArrayList<String>();
	    GeneralPath gp = new GeneralPath();
	    
	    double last_x = xy_list[0];
	    double last_y = xy_list[1];
	    gp.moveTo(last_x, last_y);
	    TrafficAreaNode first_node = new TrafficAreaNode(world_manager);
	    first_node.setLocation(last_x, last_y, 0);
	    world_manager.appendWithoutCheck(first_node);
	    node_buf.add(first_node);

	    TrafficAreaNode last_node = first_node;
	    TrafficAreaEdge edge;

	    int length = xy_list.length/2;
	    double x, y;
	    for(int i=1; i<length; i++) {
		x = xy_list[i*2];
		y = xy_list[i*2+1];
		gp.lineTo(x, y);
		line_list_.add(new Line2D.Double(x, y, last_x, last_y));
		last_x = x;
		last_y = y;
	    }
	    x = xy_list[0];
	    y = xy_list[1];
	    line_list_.add(new Line2D.Double(x, y, last_x, last_y));


	    String edge_id = world_manager.getUniqueID("_");
	    edge = new TrafficAreaEdge(world_manager, edge_id);
	    edge.setDirectedNodes(last_node.getID(), first_node.getID());
	    edge.setDirectedAreaIDList(new String[]{id});
	    world_manager.appendWithoutCheck(edge);
	    ndirected_edge_id_list.add(edge_id);
	    
	    node_list_ = node_buf.toArray(new TrafficAreaNode[0]);
	    connector_id_list_ = directed_edge_id_list.toArray(new String[0]);
	    unconnector_id_list_ = ndirected_edge_id_list.toArray(new String[0]);
	    shape_ = gp;
	} catch(Exception e) {
	    alert(e, "error");
	}
    }

    public void checkObject() throws Exception {

	for(int i=0; i<connector_id_list_.length; i++) {
	    String edge_id = connector_id_list_[i];
	    TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edge_id);
	    connector_list_.add(edge);
	}

	for(int i=0; i<unconnector_id_list_.length; i++) {
	    String edge_id = unconnector_id_list_[i];
	    TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edge_id);
	    unconnector_list_.add(edge);
	}
	/*
	for(String area_id : neighbor_area_id_list_) {
	    TrafficArea area = (TrafficArea)getManager().getTrafficObject(area_id);
	    neighbor_area_list_.add(area);
	}
	*/
	createUnconnectEdgeList();
	checked_ = true;
    }

    public void setCenter(double center_x, double center_y) {
	center_x_ = center_x;
	center_y_ = center_y;
    }

    public void setType(String type) {
	type_ = type;
    }
    public String getType() {
	return type_;
    }

    public double getCenterX() {
	return center_x_;
    }
    public double getCenterY() {
	return center_y_;
    }
    public double getDistance(TrafficArea area) {
	double dx = getCenterX() - area.getCenterX();
	double dy = getCenterY() - area.getCenterY();
	return Math.sqrt(dx*dx + dy*dy);
    }


    public void setLineList(int[] xy_list) {
	GeneralPath gp = new GeneralPath();
	int length = xy_list.length/2;
	ArrayList<Line2D> line_list = new ArrayList<Line2D>();
	double x, y;
	double last_x = xy_list[0];
	double last_y = xy_list[1];
	gp.moveTo(last_x, last_y);
	for(int i=1; i<length; i++) {
	    x = xy_list[i*2];
	    y = xy_list[i*2+1];
	    gp.lineTo(x, y);
	    line_list.add(new Line2D.Double(x, y, last_x, last_y));
	    last_x = x;
	    last_y = y;
	}
	shape_ = gp;
	x = xy_list[0];
	y = xy_list[1];
	line_list.add(new Line2D.Double(x, y, last_x, last_y));
	line_list_ = line_list;
	fireChanged();
    }
    
    public Line2D[] getLineList() {
	return line_list_.toArray(new Line2D[0]);
    }
    

    public void setSimulateAsOpenSpace(boolean simulate_as_open_space) {
	simulate_as_open_space_ = simulate_as_open_space;
    }
    public boolean isSimulateAsOpenSpace() {
	return 	simulate_as_open_space_;
    }

    public void setProperties(TagElement gml_element) throws Exception {
	// alert("gml:"+gml_element, "error");
	String coordinates_text = gml_element.getTagChild("gml:polygon").getTagChild("gml:LinearRing").getChildValue("gml:coordinates");
	
	String[] coordinates_text_list = coordinates_text.split(" ");
	GeneralPath gp = new GeneralPath();
	String[] x_y = coordinates_text_list[0].split(",");
	double x = Double.parseDouble(x_y[0]);
	double y = Double.parseDouble(x_y[1]);
	node_list_ = new TrafficAreaNode[coordinates_text_list.length];
	gp.moveTo(x, y);
	TrafficAreaNode pos = new TrafficAreaNode(getManager());
	pos.setLocation(x, y, 0);
	getManager().appendWithoutCheck(pos);
	node_list_[0] = pos;
	double x_sum = x;
	double y_sum = y;
	for(int i=1; i<coordinates_text_list.length; i++) {
	    x_y = coordinates_text_list[i].split(",");
	    x = Double.parseDouble(x_y[0]);
      	    y = Double.parseDouble(x_y[1]);
	    x_sum += x;
	    y_sum += y;
	    gp.lineTo(x, y);
	    pos = getManager().createAreaNode(x, y, 0);
	    node_list_[i] = pos;
	}
	center_x_ = x_sum/coordinates_text_list.length;
	center_y_ = y_sum/coordinates_text_list.length;
	
	TagElement[] directed_edge_tag_list = gml_element.getTagChildren("gml:directedEdge");
	ArrayList<String> directed_edge_id_list = new ArrayList<String>();
	ArrayList<String> ndirected_edge_id_list = new ArrayList<String>();
	for(int i=0; i<directed_edge_tag_list.length; i++) {
	    String de = directed_edge_tag_list[i].getAttributeValue("xlink:href").replaceAll("#", "");
	    String or = directed_edge_tag_list[i].getAttributeValue("orientation");
	    if("+".equals(or)) {
		directed_edge_id_list.add(de);
	    } else
		ndirected_edge_id_list.add(de);
	}
	connector_id_list_ = directed_edge_id_list.toArray(new String[0]);
	unconnector_id_list_ = ndirected_edge_id_list.toArray(new String[0]);
	
	/*
	TagElement[] directed_area_tag_list = gml_element.getTagChildren("gml:directedFace");
	ArrayList<String> directed_area_id_list = new ArrayList<String>();
	for(int i=0; i<directed_area_tag_list.length; i++) {
	    String de = directed_area_tag_list[i].getAttributeValue("xlink:href").replaceAll("#", "");
	    String or = directed_area_tag_list[i].getAttributeValue("orientation");
	    if("+".equals(or))
		directed_area_id_list.add(de);
	}
	neighbor_area_id_list_ = directed_area_id_list.toArray(new String[0]);
	alert(neighbor_area_id_list_.length ,"error");
	*/

	shape_ = gp;
    }

    private void createUnconnectEdgeList() {
	for(TrafficAreaEdge edge : unconnector_list_) {
	    for(Line2D line : edge.getLineList())
		unconnect_edge_list_.add(line);
	}
    }

    public TrafficAreaEdge[] getConnectorEdgeList() {
	return connector_list_.toArray(new TrafficAreaEdge[0]);
    }
    public TrafficAreaEdge[] getUnConnectorEdgeList() {
	return unconnector_list_.toArray(new TrafficAreaEdge[0]);
    }
    public Line2D[] getUnconnectedEdgeList() {
	return unconnect_edge_list_.toArray(new Line2D[0]);
    }

    public GeneralPath getShape() {
	return shape_;
    }
    public boolean contains(double x, double y, double z) {
	return shape_.contains(x, y);
    }

    public TrafficAreaNode[] getNodeList() {
	return node_list_;
    }

    public void addTrafficAreaListener(TrafficAreaListener listener) {
	area_listener_list_.add(listener);
    }
    public void removeTrafficAreaListener(TrafficAreaListener listener) {
	area_listener_list_.remove(listener);
    }







    public String toString() {
	return "TrafficBlockade[id:"+getID()+";type:"+getType()+";]";
    }
    public String toLongString() {
	StringBuffer sb = new StringBuffer();
	sb.append("<div><div style='font-size:18;'>TrafficBlockade(id:"+getID()+")</div>");
	sb.append("type: "+getType()+"<br/>");
	if(isChecked())
	    sb.append("checked object.<br/>");
	else
	    sb.append("unchecked object.<br/>");
	sb.append("center: ("+center_x_+","+center_y_+")<br/>");

	sb.append("<div style='font-size:15;'>Node List</div>");
	sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
	for(int i=0; i<node_list_.length; i++)
	    sb.append(node_list_[i]).append("<br/>");
	sb.append("</div>");

	sb.append("<div style='font-size:15;'>Connected Edge List</div>");
	sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
	for(int i=0; i<connector_list_.size(); i++) {
	    TrafficAreaEdge tae = connector_list_.get(i);
	    if(tae==null) continue;
	    sb.append(tae.toLongString()).append("<br/>");
	}
	sb.append("</div>");
	

	sb.append("<div style='font-size:15;'>Unconnected Edge List</div>");
	sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
	for(int i=0; i<unconnector_list_.size(); i++)
	    sb.append(unconnector_list_.get(i).toString()).append("<br/>");
	sb.append("</div>");


	sb.append("</div>");
	return sb.toString();
    }
    // easy to access

}
