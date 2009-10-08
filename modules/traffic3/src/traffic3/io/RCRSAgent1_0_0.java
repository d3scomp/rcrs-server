package traffic3.io;

import java.io.*;
import java.util.*;

import traffic3.manager.*;
import traffic3.objects.area.*;
import traffic3.objects.*;

import org.util.xml.parse.*;
import org.util.xml.parse.policy.*;
import org.util.xml.element.*;

public class RCRSAgent1_0_0 implements Parser {
    public String getVersion() {
	return "RCRSAgent[1.0.0]";
    }
    public String getDescription() {
	return getVersion();
    }

    public boolean isSupported(InputStream in) {
	final String[] version = new String[1];
	try{
	    ElementParser parser = new ElementParser(in);
	    final ParserPolicy policy = new ParserPolicy(){
		    public Element allowElement(Element element) {
			if(element.isTextElement()) return element;
			TagElement tag = (TagElement)element;
			if(tag.getKey().equals("rcrs:Version")) {
			    version[0] = tag.getValue();
			}
			return null;
		    }
		    public boolean checkEndTag() { return true; }
		    public boolean forceEmptyTag(java.lang.String key) { return false; }
		    public ParserPolicy getInnerPolicy(Element element) { return this; }
		    public String selectEncoding(java.lang.String last_tag_key) { return null; }
		    public boolean throwExceptionIfDocumentHasError() { return true; }
		    public boolean finished(){return false;}
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

	final ArrayList<TrafficAgent> agent_list = new ArrayList<TrafficAgent>();

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
		public boolean finished(){return false;}
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
		public boolean finished(){return false;}
	    };
	

	final ParserPolicy agent_policy = new ParserPolicy() {
		public Element allowElement(Element element) {
		    if(exception[0]!=null) return null;
		    if(!element.isTagElement()) return element;
		    TagElement tag = (TagElement)element;
		    Element result = null;
		    try{
			if(tag.getKey().equals("agent")) {
			    String type = tag.getAttributeValue("type");
			    TrafficAgent agent = new TrafficAgent(world_manager, 200, 0.7);
			    agent.setType(type);
			    TagElement location = tag.getTagChild("location");
			    double x = Double.parseDouble(location.getChildValue("x"));
			    double y = Double.parseDouble(location.getChildValue("y"));
			    double z = Double.parseDouble(location.getChildValue("z","0"));
			    agent.setLocation(x, y, z);
			    world_manager.appendWithoutCheck(agent);
			} else if(tag.getKey().equals("location")) {
			    result = element;
			} else if(tag.getKey().equals("x")) {
			    result = element;
			} else if(tag.getKey().equals("y")) {
			    result = element;
			} else if(tag.getKey().equals("area")) {
			    result = element;
			} else
			    System.out.println("skipped: "+tag.getKey());
		    }catch(Exception e){
			exception[0] = e;
		    }
		    return result;
		}
		public ParserPolicy getInnerPolicy(Element element) {
		    if(!element.isTagElement()) return hold_all_policy;
		    return hold_all_policy;
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public String selectEncoding(java.lang.String last_tag_key) { return null; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished(){return false;}
	    };





	final ParserPolicy agent_list_policy = new ParserPolicy() {
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
		    if(tag.getKey().equals("agent_list"))
			return agent_policy;
		    return this;
		}
		public boolean checkEndTag() { return true; }
		public boolean forceEmptyTag(java.lang.String key) { return false; }
		public String selectEncoding(java.lang.String last_tag_key) { return encoding; }
		public boolean throwExceptionIfDocumentHasError() { return true; }
		public boolean finished(){return false;}
	    };

	parser.setPolicy(agent_list_policy);
	parser.parse();

	if(exception[0]!=null) throw exception[0];


	world_manager.check();
    }

    public void output(WorldManager world_manager, OutputStream out) throws Exception {
	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
	bw.write("<agent_list xmlns:rcrs=\"http://sakura.meijo-u.ac.jp/..\">");
	bw.newLine();
	bw.write("   <rcrs:Version>"+getVersion()+"</rcrs:Version>");
	bw.newLine();
	for(TrafficAgent agent : world_manager.getAgentList()) {
	    bw.write("   <agent type=\""+agent.getType()+"\">");
	    bw.newLine();
	    bw.write("      <location type=\"CoordinateAndArea\">");
	    bw.newLine();
	    bw.write("         <x>"+agent.getX()+"</x>");
	    bw.newLine();
	    bw.write("         <y>"+agent.getY()+"</y>");
	    bw.newLine();
	    bw.write("         <area>"+agent.getArea().getID()+"</area>");
	    bw.newLine();
	    bw.write("      </location>");
	    bw.newLine();
	    bw.write("   </agent>");
	    bw.newLine();
	}
	bw.write("</agent_list>");
	bw.newLine();
	bw.flush();
	bw.close();
    }
}
