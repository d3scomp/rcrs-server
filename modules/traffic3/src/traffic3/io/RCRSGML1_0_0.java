package traffic3.io;

import java.io.*;
import java.util.*;

import traffic3.manager.*;
import traffic3.objects.area.*;
import traffic3.objects.*;

import org.util.xml.parse.*;
import org.util.xml.parse.policy.*;
import org.util.xml.element.*;

public class RCRSGML1_0_0 implements Parser {
    public String getVersion() {
	return "RCRSGML[1.0.0]";
    }
    public String getDescription() {
	return getVersion();
    }

    public boolean isSupported(InputStream in) {

	final String[] version = new String[1];
	try{
	    ElementParser parser = new ElementParser(in);
	    final boolean[] flag = new boolean[]{ false };
	    final ParserPolicy policy = new ParserPolicy(){
		    public Element allowElement(Element element) {
			if(element.isTextElement()) return element;
			TagElement tag = (TagElement)element;
			if(tag.getKey().equals("rcrs:Version")) {
			    version[0] = tag.getValue();
			    flag[0] = true;
			}
			return null;
		    }
		    public boolean finished() {return flag[0];}
		    public boolean checkEndTag() { return true; }
		    public boolean forceEmptyTag(java.lang.String key) { return false; }
		    public ParserPolicy getInnerPolicy(Element element) { return this; }
		    public String selectEncoding(java.lang.String last_tag_key) { return null; }
		    public boolean throwExceptionIfDocumentHasError() { return true; }
		};
	    parser.setPolicy(policy);
	    parser.parse();
	}catch(Exception e) {
	    e.printStackTrace();
	    version[0] = null;
	}
	return getVersion().equals(version[0]);
    }




    public void input(final WorldManager world_manager, InputStream in) throws Exception {
	ElementParser parser = new ElementParser(in);
	final Exception[] exception = new Exception[1];

	//final ArrayList<TrafficObject> pool = new ArrayList<TrafficObject>();
	final ArrayList<TrafficAreaEdge> connector_list = new ArrayList<TrafficAreaEdge>();

	final ParserPolicy hold_all_policy = new ParserPolicy(){	
		public Element allowElement(Element element) {
		    if(exception[0]!=null) return null;
		    return element; 
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public ParserPolicy getInnerPolicy(Element element) { return this; }
		public String selectEncoding(java.lang.String last_tag_key) { return null; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished() {return false;}
	    };

	final ParserPolicy disable_all_policy = new ParserPolicy(){	
		public Element allowElement(Element element) {
		    return null;
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public ParserPolicy getInnerPolicy(Element element) { return this; }
		public String selectEncoding(java.lang.String last_tag_key) { return null; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished() {return false;}
	    };
	



	final ParserPolicy area_policy = new ParserPolicy() {
		public Element allowElement(Element element) {
		    if(exception[0]!=null) return null;
		    if(!element.isTagElement()) return element;
		    TagElement tag = (TagElement)element;

		    try{
			if(tag.getKey().equals("rcrs:Face")) {
			    //System.err.println(tag);
			    TagElement gtag = tag.getTagChild("gml:Face");
			    TrafficArea tnn = new TrafficArea(world_manager, gtag.getAttributeValue("gml:id"));
			    tnn.setProperties(gtag);
			    tnn.setType(tag.getAttributeValue("type"));
			    world_manager.appendWithoutCheck(tnn);
			    // System.out.println("+n:"+tag.getAttributeValue("gml:id"));

			} else if(tag.getKey().equals("gml:Node")) {
			    TrafficAreaNode tnn = new traffic3.objects.area.TrafficAreaNode(world_manager, tag.getAttributeValue("gml:id"));
			    tnn.setProperties(tag);
			    world_manager.appendWithoutCheck(tnn);
			} else if(tag.getKey().equals("gml:Edge")) {
			    TrafficAreaEdge tnn = new TrafficAreaEdge(world_manager, tag.getAttributeValue("gml:id"));
			    tnn.setProperties(tag);
			    connector_list.add(tnn);
			    world_manager.appendWithoutCheck(tnn);
			} else if(tag.getKey().equals("rcrs:BuildingProperty")) {
			} else if(tag.getKey().equals("rcrs:EdgeList")) {
			} else 
			    System.out.println("skipped: "+tag.getKey());
		    }catch(Exception e){
			exception[0] = e;
		    }
		    return null;
		}
		public ParserPolicy getInnerPolicy(Element element) {
		    if(!element.isTagElement()) return hold_all_policy;
		    TagElement tag = (TagElement)element;
		    if(tag.getKey().equals("rcrs:FaceList"))
			return this;
		    else if(tag.getKey().equals("rcrs:EdgeList"))
			return this;
		    else if(tag.getKey().equals("rcrs:NodeList"))
			return this;
		    else
			return hold_all_policy;
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public String selectEncoding(java.lang.String last_tag_key) { return null; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished() { return false; }
	    };


	
	final ParserPolicy topology_policy = new ParserPolicy() {
		String encoding;
		public Element allowElement(Element element) {
		    if(exception[0]!=null) return null;
		    if(encoding == null) {
			if(element.isTagElement()) {
			    TagElement te = (TagElement)element;
			    if(te.isPI())
				encoding = te.getAttributeValue("encoding");
			}
			if(encoding == null) encoding = "utf-8";
		    }
		    return element;
		}
		public ParserPolicy getInnerPolicy(Element element) {
		    if(!element.isTagElement()) return this;
		    TagElement tag = (TagElement)element;
		    //System.out.println("gml>"+tag.getKey());
		    if(tag.getKey().equals("Topology"))
			return this;
		    else if(tag.getKey().equals("rcrs:Area"))
			return area_policy;
		    return this;
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public String selectEncoding(java.lang.String last_tag_key) { return encoding; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished() { return false; }
	    };

	parser.setPolicy(topology_policy);
	parser.parse();

	if(exception[0]!=null) throw exception[0];

	world_manager.check();
    }

    public void output3(WorldManager world_manager, OutputStream out) throws Exception {
	HashMap<String, TrafficObject> area_list = new HashMap<String, TrafficObject>();
	HashMap<String, TrafficObject> edge_list = new HashMap<String, TrafficObject>();
	HashMap<String, TrafficObject> node_list = new HashMap<String, TrafficObject>();
	HashMap<String, TrafficObject> object_list = new HashMap<String, TrafficObject>();
	
	for(TrafficArea area : world_manager.getAreaList()) {

	}
    }


    public void output(WorldManager world_manager, OutputStream out) throws Exception {

	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
	bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	bw.newLine();
	bw.write("<Topology xmlns=\"http://www.opengis.net/app\"");
	bw.newLine();
	bw.write("   xmlns:sch=\"http://www.ascc.net/xml/schematron\"");
	bw.newLine();
	bw.write("   xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
	bw.newLine();
	bw.write("   xmlns:gml=\"http://www.opengis.net/gml\"");
	bw.newLine();
	bw.write("   xmlns:app=\"http://www.opengis.net/app\"");
	bw.newLine();
	bw.write("   xmlns:xsl=\"http://www.w3.org/2001/XMLSchema-instance\"");
	bw.newLine();
	bw.write("   xsl:schemaLocation=\"http://www.opengis.net/app/networkExamples.xsd\"");
	bw.newLine();
	bw.write("   xmlns:rcrs=\"http://sakura.meijo-u.ac.jp/rcrs\">");
	bw.newLine();
	bw.write("<rcrs:Version>"+getVersion()+"</rcrs:Version>");
	bw.newLine();
	bw.write("<rcrs:Description>"+"no name"+"</rcrs:Description>");
	bw.newLine();
	bw.write("<rcrs:Area>");
	bw.newLine();

	bw.write("<rcrs:NodeList>");
	bw.newLine();
	for(TrafficAreaNode n : world_manager.getAreaNodeList()) {
	    bw.write("<gml:Node gml:id=\""+n.getID()+"\">");
	    bw.newLine();
	    bw.write("<gml:pointProperty>");
	    bw.newLine();
	    bw.write("<gml:Point>");
	    bw.newLine();
	    bw.write("<gml:coordinates>"+n.getX()+","+n.getY()+"</gml:coordinates>");
	    bw.newLine();
	    bw.write("</gml:Point>");
	    bw.newLine();
	    bw.write("</gml:pointProperty>");
	    bw.newLine();
	    bw.write("</gml:Node>");
	    bw.newLine();
	}	    
	bw.write("</rcrs:NodeList>");
	bw.newLine();



	bw.write("<rcrs:EdgeList>");
	bw.newLine();
	for(TrafficAreaEdge e : world_manager.getAreaConnectorEdgeList()) {
	    bw.write("<gml:Edge gml:id=\""+e.getID()+"\">");
	    bw.newLine();
	    for(TrafficAreaNode node : e.getDirectedNodes()) {
		if(node == null) {
		    System.err.println(node);
		    System.err.println(e.toString());
		}
		bw.write("<gml:directedNode orientation=\"+\" xlink:href=\"#"+node.getID()+"\"/>");
		bw.newLine();
	    }
	    for(TrafficArea face : e.getDirectedArea()) {
		if(face == null) {
		    System.err.println("Edge's directed face is "+face);
		    System.err.println("Edge is "+e.toString());
		}
		bw.write("<gml:directedFace orientation=\"+\" xlink:href=\"#"+face.getID()+"\"/>");
		bw.newLine();
	    }
	    bw.write("<gml:centerLineOf>");
	    bw.newLine();
	    bw.write("<gml:LineString>");
	    bw.newLine();
	    
	    bw.write("<gml:coordinates>");
	    java.awt.geom.Point2D p = null;
	    for(java.awt.geom.Line2D line : e.getLineList()) {
		if(p==null) {
		    p = line.getP1();
		    bw.write(p.getX()+","+p.getY());
		}
		p = line.getP2();
		bw.write(" "+p.getX()+","+p.getY());
	    }
	    bw.write("</gml:coordinates>");

	    bw.newLine();
	    bw.write("</gml:LineString>");
	    bw.newLine();
	    bw.write("</gml:centerLineOf>");
	    bw.newLine();
	    bw.write("</gml:Edge>");
	    bw.newLine();
	}
	bw.write("</rcrs:EdgeList>");
	bw.newLine();



	bw.write("<rcrs:FaceList>");
	bw.newLine();
	for(TrafficArea area : world_manager.getAreaList()) {
	    String type = area.getType();
	    if(type==null) type = "open space";
	    bw.write("<rcrs:Face type=\""+type+"\">");
	    bw.newLine();
	    bw.write("<rcrs:BuildingProperty></rcrs:BuildingProperty>");
	    bw.newLine();
	    bw.write("<gml:Face gml:id=\""+area.getID()+"\">");
	    bw.newLine();
	    for(TrafficAreaEdge edge : area.getConnectorEdgeList()) {
		bw.write("<gml:directedEdge orientation=\"+\" xlink:href=\"#"+edge.getID()+"\"/>");
		bw.newLine();
	    }
	    for(TrafficAreaEdge edge : area.getUnConnectorEdgeList()) {
		bw.write("<gml:directedEdge orientation=\"-\" xlink:href=\"#"+edge.getID()+"\"/>");
		bw.newLine();
	    }
	    bw.write("<gml:polygon>");
	    bw.newLine();
	    bw.write("<gml:LinearRing>");
	    bw.newLine();
	    bw.write("<gml:coordinates>");
	    for(TrafficAreaNode node : area.getNodeList()) {
		bw.write(node.getX()+","+node.getY()+" ");
	    }
	    bw.write("</gml:coordinates>");
	    bw.newLine();
	    bw.write("</gml:LinearRing>");
	    bw.newLine();
	    bw.write("</gml:polygon>");
	    bw.newLine();

	    bw.write("</gml:Face>");
	    bw.newLine();
	    bw.write("</rcrs:Face>");
	    bw.newLine();
	    /*
    	      <gml:directedEdge orientation="+" xlink:href="#5803"/>
	      <gml:directedEdge orientation="-" xlink:href="#5811"/>
	      <gml:directedEdge orientation="+" xlink:href="#5807"/>
	      <gml:directedEdge orientation="-" xlink:href="#5810"/>
	      */
	}
	bw.write("</rcrs:FaceList>");
	bw.newLine();

	bw.write("</rcrs:Area>");
	bw.newLine();
	bw.write("</Topology>");
	bw.newLine();
	bw.flush();
	bw.close();
    }
}
