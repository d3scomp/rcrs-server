package traffic3.objects.area;

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
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import org.util.xml.element.*;

public class TrafficAreaEdge extends TrafficObject {

    private String id1_;
    private String id2_;
    private GeneralPath path_;
    private ArrayList<Line2D> line_list_ = new ArrayList<Line2D>();
    private String[] directed_area_id_list_;
    private TrafficArea[] directed_area_list_;

    private TrafficAreaNode n1_;
    private TrafficAreaNode n2_;
    private TrafficAreaNode center_;

    public TrafficAreaEdge(WorldManager world_manager) {
	super(world_manager);
    }
    public TrafficAreaEdge(WorldManager world_manager, String id) {
	super(world_manager, id);
    }

    public void setDirectedNodes(String id1, String id2) {
	id1_ = id1;
	id2_ = id2;
	if(line_list_.size() == 0) {
	    TrafficAreaNode n1 = (TrafficAreaNode)getManager().getTrafficObject(id1_);
	    TrafficAreaNode n2 = (TrafficAreaNode)getManager().getTrafficObject(id2_);
	    line_list_.add(new Line2D.Double(n1.getX(), n1.getY(), n2.getX(), n2.getY()));
	    GeneralPath gp = new GeneralPath();
	    gp.moveTo(n1.getX(), n1.getY());
	    gp.lineTo(n2.getX(), n2.getY());
	    path_ = gp;
	}

	fireChanged();
    }

    public double length() {
	return n1_.getDistance(n2_);
    }

    public TrafficAreaNode[] getDirectedNodes() {
	return new TrafficAreaNode[]{n1_, n2_};
    }

    public TrafficNode getCenter() {
	if(center_==null) {
	    double x = (n1_.getX()+n2_.getX())/2;
	    double y = (n1_.getY()+n2_.getY())/2;
	    double z = 0;
	    try{
		center_ = getManager().createAreaNode(x, y, z);
	    }catch(Exception e) {
		e.printStackTrace();
	    }
	}
	return center_;
    }

    /*
    public void setLineList(ArrayList<Line2D> line_list) {
	GeneralPath gp = new GeneralPath();
	line_list_ = line_list;
	for(Line2D line : line_list)
	    gp.append(line);
	path_ = path;
    }
    */

    public String getID1() {
	return id1_;
    }
    public String getID2() {
	return id2_;
    }

    public void checkObject() throws Exception {
	directed_area_list_ = new TrafficArea[directed_area_id_list_.length];
	for(int i=0; i<directed_area_id_list_.length; i++) {
	    directed_area_list_[i] = (TrafficArea)getManager().getTrafficObject(directed_area_id_list_[i]);
	}
	n1_ = (TrafficAreaNode)getManager().getTrafficObject(id1_);
	n2_ = (TrafficAreaNode)getManager().getTrafficObject(id2_);

	if(n1_ == null) throw new Exception("Error: Node cannot be found: "+id1_+": "+toString());
	if(n2_ == null) throw new Exception("Error: Node cannot be found: "+id2_+": "+toString());

	checked_ = true;
    }

    public boolean has(Line2D o) {
	for(int i=0; i<line_list_.size(); i++) {
	    if((line_list_.get(i).getP1().getX()==o.getP1().getX() && line_list_.get(i).getP2().getX()==o.getP2().getX())
	       &&(line_list_.get(i).getP1().getY()==o.getP1().getY() && line_list_.get(i).getP2().getY()==o.getP2().getY()))
		return true;
	    if((line_list_.get(i).getP1().getX()==o.getP2().getX() && line_list_.get(i).getP2().getX()==o.getP1().getX())
	       &&(line_list_.get(i).getP1().getY()==o.getP2().getY() && line_list_.get(i).getP2().getY()==o.getP1().getY()))
		return true;
	    //System.out.print(line_list_.get(i).getP1()+o.getP1().toString()+", ");
	    //System.out.println(line_list_.get(i).getP2()+o.getP2().toString());
	}
	return false;
    }
    public TrafficArea getNextArea(TrafficArea now) {
	if(directed_area_list_.length == 1) return null;
	assert directed_area_list_.length==2 : "three directed area!";

	//alert("<html>"+now+" :</br> "+toLongString()+"</html>", "error");
	if(directed_area_list_[0]==now) return directed_area_list_[1];
	if(directed_area_list_[1]==now) return directed_area_list_[0];
	return null;
    }
    public TrafficArea[] getDirectedArea() {
	return directed_area_list_;
    }
    public void setDirectedAreaIDList(String[] directed_area_id_list) {
	directed_area_id_list_ = directed_area_id_list;
    }

    public Line2D[] getLineList() {
	return line_list_.toArray(new Line2D[0]);
    }

    public double distance(double x, double y) {
	double min = line_list_.get(0).ptSegDist(x, y);
	for(int i=1; i<line_list_.size(); i++)
	    min = Math.min(min ,line_list_.get(i).ptSegDist(x, y));
	return min;
    }

    public void setProperties(TagElement gml_element) throws Exception {
	// System.out.println("gml edge:"+gml_element);
	TagElement[] ids = gml_element.getTagChildren("gml:directedNode");
	String id1 = ids[0].getAttributeValue("xlink:href").replaceAll("#", "");
	String id2 = ids[1].getAttributeValue("xlink:href").replaceAll("#", "");
	String coordinates_text = gml_element.getTagChild("gml:centerLineOf").getTagChild("gml:LineString").getChildValue("gml:coordinates");
	String[] coordinates_text_list = coordinates_text.split(" ");
	GeneralPath gp = new GeneralPath();
	String[] x_y = coordinates_text_list[0].split(",");
	double lx = Double.parseDouble(x_y[0]);
	double ly = Double.parseDouble(x_y[1]);
	gp.moveTo(lx, ly);
	for(int i=1; i<coordinates_text_list.length; i++) {
	    x_y = coordinates_text_list[i].split(",");
	    double x = Double.parseDouble(x_y[0]);
      	    double y = Double.parseDouble(x_y[1]);
	    gp.lineTo(x, y);
	    line_list_.add(new Line2D.Double(lx, ly, x, y));
	    lx = x;
	    ly = y;
	}
	path_ = gp;
	TagElement[] fids = gml_element.getTagChildren("gml:directedFace");
	directed_area_id_list_ = new String[fids.length];
	for(int i=0; i<fids.length; i++)
	    directed_area_id_list_[i] = fids[i].getAttributeValue("xlink:href").replaceAll("#", "");
	
	setDirectedNodes(id1, id2);
    }
    public GeneralPath getPath() {
	return path_;
    }

    public String toString() {
	return "TrafficAreaEdge[id:"+getID()+";id1:"+id1_+";id2:"+id2_+";]";
    }
    public String toLongString() {
	StringBuffer sb = new StringBuffer();
	sb.append("<div><div style='font-size:18;'>TrafficAreaEdge(id:"+getID()+")</div>");
	sb.append("<div>id: ").append(getID()).append("</div>");
	sb.append("<div style='margin-left:50px;'>");
	sb.append("<div>node: ").append(n1_).append("</div>");
	sb.append("<div>node: ").append(n2_).append("</div>");
	if(directed_area_list_!=null) {
	    for(int i=0; i<directed_area_list_.length; i++) {
		sb.append("<div>directed face").append(i+1).append(":  ").append(directed_area_list_[i]).append("</div>");		
	    }
	}
	sb.append("</div>");
	sb.append("</div>");
	return sb.toString();
    }
}
