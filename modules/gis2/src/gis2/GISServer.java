package gis2;

import java.io.File;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.awt.geom.Point2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import gis2.gml.objects.GMLID;
import gis2.gml.objects.GMLNode;
import gis2.gml.objects.GMLEdge;
import gis2.gml.objects.GMLDirectedEdge;
import gis2.gml.objects.GMLFace;
import gis2.gml.objects.GMLAgent;
import gis2.gml.manager.GMLWorldManager;
import gis2.gml.manager.GMLWorldManagerException;
import gis2.connection.TCPBufferedConnection;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.util.xml.element.Element;
import org.util.xml.element.TagElement;
import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.*;
import org.util.xml.io.XMLConfigManager;
import org.util.xml.parse.XMLParseException;

import rescuecore2.config.Config;

import rescuecore2.worldmodel.*;
import rescuecore2.connection.*;
import rescuecore2.messages.*;
import rescuecore2.messages.control.*;
import rescuecore2.standard.entities.*;

/**
 * GIS Server.<br/>
 * java gis2.Main config/gis2.xml
 */
public class GISServer implements Runnable {

    private Config config;
    private PrintStream out;
    private PrintStream err;
    private String PORT_KEY = "gis2.port";
    private GMLWorldManager gmlWorldManager = new GMLWorldManager();
    private Map<EntityID, Entity> rcrsMap = new HashMap<EntityID, Entity>();
    private Map<GMLID, EntityID> gml2rcrs = new HashMap<GMLID, EntityID>();
    private boolean running = false;

    /**
     * Constructor.
     * config should have following options.
     * <table>
     * <tr><th>key</th><th>type of value</th></tr>
     * <tr><td>gis2.map.list</td><td>List</td><td>gml map file path list</td></tr>
     * <tr><td>gis2.agent.list</td><td>List</td><td>agent file path list</td></tr>
     * <tr><td>gis2.port</td><td>int</td><td>gis waiting port</td></tr>
     * </table>
     *
     * Call run method to start process.
     */
    public GISServer(Config c, PrintStream o, PrintStream e) {
        config = c;
        out = o;
        err = e;
    }

    /**
     * start process
     */
    public void run() {
        if (running) {
            throw new IllegalStateException("gis server already started");
        }
        running = true;
        try {
            List<String> mapFiles = config.getArrayValue("gis2.map.list");
            List<String> agentFiles = config.getArrayValue("gis2.agent.list");
            int port = config.getIntValue("gis2.port");

            readFiles(mapFiles);
            readFiles(agentFiles);
            checkImportedInformation();
            createRCRSInformation();
            waitConnectionFromKernel(port);
        }
        catch(ConnectionException e) {
            log(e);
        }
        catch(XMLParseException e) {
            log(e);
        }
        catch(FileNotFoundException e) {
            log(e);
        }
        catch(IOException e) {
            log(e);
        }
        running = false;
    }

    /**
     * check 
     */
    public void checkImportedInformation() {
        checkExistance();
        checkAgentsLocation();

        StringBuffer sb = new StringBuffer();
        sb.append("\nImported GML Objects:\n");
        sb.append("   GML Node: ").append(gmlWorldManager.nodeSize()).append("\n");
        sb.append("   GML Edge: ").append(gmlWorldManager.edgeSize()).append("\n");
        sb.append("   GML Face: ").append(gmlWorldManager.faceSize()).append("\n");
        sb.append("   Agent: ").append(gmlWorldManager.agentSize()).append("\n");
        log(sb);
    }

    public void checkExistance() {
        
    }
    public void checkAgentsLocation() {
        for (GMLAgent agent : gmlWorldManager.toAgentArray(new GMLAgent[0])) {
            double x = agent.getX();
            double y = agent.getY();
            GMLID aid = agent.getAreaID();
            GMLFace face = gmlWorldManager.getFace(aid);
            boolean error = false;
            if (face != null) {
                if (!face.getShape().contains(x, y)) {
                    error = true;
                }
            }
            else if (face == null) {
                error = true;
            }
            if (error) {
                GMLFace[] faces= gmlWorldManager.findFace(x, y);
                if (faces.length > 0) {
                    agent.setLocation(x, y, faces[0].getID());
                }
                else {
                    throw new RuntimeException("error: cannont found face of agent.");
                }
            }
        }
    }


    public void readFiles(List<String> filenames) throws FileNotFoundException, IOException, XMLParseException {
        for (String filename : filenames) {
            readFile(filename);
        }
    }

    public void readFile(String filename) throws FileNotFoundException, IOException, XMLParseException {
        File file = new File(filename);
        log("read: " + file.getAbsolutePath());
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        ElementParser parser = new ElementParser(new FileInputStream(file));
        parser.setPolicy(new XMLParserPolicy() {
                public Element allowElement(Element element) {
                    if (element.isTagElement()) {
                        TagElement tag = (TagElement)element;
                        String key = tag.getKey().toLowerCase();
                        if (!"agent".equals(key) || !"topology".equals(key)) {
                            log("skipped: " + tag.getKey());
                        }
                    }
                    return null;
                }
		else {
		    EntityID id = getID(next.getID(), gmlid_rcrsid_map);
		    nexts.add(id);
		}
                Edge edge = new Edge(lx, ly, x, y, neighbour);
                edges.add(edge);
		ln = n;
		lx = x;
		ly = y;
	    }
	    n = node_list[0];
            
            x = n.getX();
            y = n.getY();
            for(TrafficAreaDirectedEdge e : traffic_area.getDirectedEdges()) {
                if (e.getAreas().length >= 2) {
                    if(e.getEdge().has(new java.awt.geom.Line2D.Double(x, y, lx, ly))) {
                        next = e.getEdge().getNextArea(traffic_area);
                        break;
                    }
                    return new XMLParserPolicy();
                }
            }
            if (rcrsid.getValue() == 113) {
                System.err.println("last:"+next);
            }
            if(next == null) {
                nexts.add(new EntityID(-1));
            }
            else {
                EntityID id = getID(next.getID(), gmlid_rcrsid_map);
                nexts.add(id);
            }
            
            //nexts.add(new EntityID(-1));
            if (rcrsid.getValue() == 113) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i< nexts.size(); i++)
                    sb.append(nexts.get(i)).append(",");
                System.err.println(sb.toString());
            }
            */

	    
	    rcrs_area.setEdges(edges);
	    pool.put(rcrs_area.getID(), rcrs_area);

	    ArrayList<EntityID> eidl = new ArrayList<EntityID>();
	    for(TrafficBlockade tb : traffic_area.getBlockadeList()) {
		Blockade blockade = new Blockade(new EntityID(getUniqueNumber()));
		eidl.add(blockade.getID());
		blockade.setX((int)tb.getCenterX());
                blockade.setY((int)tb.getCenterY());
		Line2D[] line_list = tb.getLineList();
		int[] xy = new int[(line_list.length)*2];
		//xy[0] = (int)line_list[0].getP1().getX();
		//xy[1] = (int)line_list[0].getP1().getY();
		for(int i=0; i<line_list.length; i++) {
		    xy[i*2] = (int)line_list[i].getP1().getX();
		    xy[i*2+1] = (int)line_list[i].getP1().getY();
		}
		blockade.setApexes(xy);
		EntityID area_id = rcrs_area.getID();
		blockade.setPosition(area_id);
		Area area = (Area)pool.get(area_id);
		ArrayList<EntityID> blockade_list = new ArrayList<EntityID>();
		blockade_list.add(blockade.getID());
		area.setBlockades(blockade_list);
		pool.put(blockade.getID(), blockade);

	    }
	    rcrs_area.setBlockades(eidl);
	}


	for(TrafficAgent traffic_agent : world_manager.getAgentList()) {
	    EntityID id = new EntityID(getUniqueNumber());
	    Human human = null;
	    String type = traffic_agent.getType();
	    if(type==null) type="unknown type";
	    else type = type.toLowerCase();
	    
	    if(type.startsWith("ambulanceteam"))
		human = new AmbulanceTeam(id);
	    else if(type.startsWith("firebrigade"))
		human = new FireBrigade(id);
	    else if(type.startsWith("policeforce"))
		human = new PoliceForce(id);
	    else
		human = new Civilian(id);
	    double x = traffic_agent.getX();
	    double y = traffic_agent.getY();
	    double z = 0;
	    human.setHP(10000);
	    EntityID position = gmlid_rcrsid_map.get(traffic_agent.getArea());
	    if(position==null) {
		for(TrafficArea area : world_manager.getAreaList())
		    if(area.contains(x, y, z))
			position = gmlid_rcrsid_map.get(area.getID());
	    }
	    human.setPosition(position, (int)x, (int)y);
	    pool.put(human.getID(), human);
	}


	/*
	for(TrafficBlockade tblockade : world_manager.getBlockadeList()) {
	    Blockade blockade = new Blockade(new EntityID(getUniqueNumber()));
	    blockade.setCenter(455000,408000);
	    int[] xy = new int[]{452000, 403000, 457000, 413000, 451000, 414000, 447000, 406000};
	    blockade.setShape(xy);
	    EntityID area_id = gmlid_rcrsid_map.get("95");
	    blockade.setArea(area_id);
	    Area area = (Area)pool.get(area_id);
	    ArrayList<EntityID> blockade_list = new ArrayList<EntityID>();
	    blockade_list.add(blockade.getID());
	    area.setBlockadeList(blockade_list);
	    pool.put(blockade.getID(), blockade);
	}
	*/
	//addTestBlockade(pool);
	
	

	System.out.println(gmlid_rcrsid_map);
	
	return pool;
    }

    private KeepAllPolicy keepAllPolicy = new KeepAllPolicy();
    private ReadNodePolicy readNodePolicy = new ReadNodePolicy();
    private ReadEdgePolicy readEdgePolicy = new ReadEdgePolicy();
    private ReadFacePolicy readFacePolicy = new ReadFacePolicy();
    private ReadAgentPolicy readAgentPolicy = new ReadAgentPolicy();

    private class KeepAllPolicy extends XMLParserPolicy {
        public Element allowElement(Element element) {
            return element;
        }
        public ParserPolicy getInnerPolicy(Element element) {
            return this;
        }
    }

    private int agentCount = 0;
    private class ReadNodePolicy extends XMLParserPolicy {
        public Element allowElement(Element element) {
            if (element.isTagElement()) {
                TagElement tag = (TagElement)element;
                String key = tag.getKey().toLowerCase();
                if ("gml:node".equals(key)) {
                    GMLID id = new GMLID(tag.getAttributeValue("gml:id"));
                    TagElement pointPropertyTag = tag.getTagChild("gml:pointProperty");
                    TagElement pointTag = pointPropertyTag.getTagChild("gml:Point");
                    String coordinates = pointTag.getChildValue("gml:coordinates");
                    coordinates = coordinates.replaceAll("\n", " ");
                    while (coordinates.indexOf(" ") != -1) {
                        coordinates = coordinates.replaceAll("  ", " ");
                    }
                    String[] xyText = coordinates.split(",");
                    double x = Double.parseDouble(xyText[0]);
                    double y = Double.parseDouble(xyText[1]);
                    GMLNode node = new GMLNode(id, gmlWorldManager, x, y);
                    try {
                        gmlWorldManager.add(node);
                    }
                    catch (GMLWorldManagerException exc) {
                        exc.printStackTrace();
                    }
                    //nodeMap.put(node.getID(), node);
                    log(node);
                }
            }
            return null;
        }
        public ParserPolicy getInnerPolicy(Element element) {
            return keepAllPolicy;
        }
    }

    private class ReadEdgePolicy extends XMLParserPolicy {
        public Element allowElement(Element element) {
            if (element.isTagElement()) {
                TagElement tag = (TagElement)element;
                String key = tag.getKey().toLowerCase();
                if ("gml:edge".equals(key)) {
                    GMLID id = new GMLID(tag.getAttributeValue("gml:id"));
                    TagElement[] nodeTags = tag.getTagChildren("gml:directedNode");
                    GMLID[] nodes = new GMLID[nodeTags.length];
                    for (int i = 0; i < nodeTags.length; i++) {
                        String nn = nodeTags[i].getAttributeValue("xlink:href");
                        nodes[i] = new GMLID(nn.replace("#", ""));
                    }
                    TagElement[] faceTags = tag.getTagChildren("gml:directedFace");
                    GMLID[] faces = new GMLID[faceTags.length];
                    for (int i = 0; i < faceTags.length; i++) {
                        String ff = faceTags[i].getAttributeValue("xlink:href");
                        faces[i] = new GMLID(ff.replace("#", ""));
                    }
                    TagElement centerLineOf = tag.getTagChild("gml:centerLineOf");
                    TagElement lineString = centerLineOf.getTagChild("gml:LineString");
                    TagElement coordinates = lineString.getTagChild("gml:coordinates");
                    String[] xytexts = coordinates.getValue().split(" ");
                    Point2D[] points = new Point2D[xytexts.length];
                    for (int i = 0; i < xytexts.length; i++) {
                        String[] xytext = xytexts[i].split(",");
                        double x = Double.parseDouble(xytext[0]);
                        double y = Double.parseDouble(xytext[1]);
                        points[i] = new Point2D.Double(x, y);
                    }
                    GMLEdge edge = new GMLEdge(id, gmlWorldManager, nodes, faces);
                    edge.setBorder(points);
                    log(edge);
                    try {
                        gmlWorldManager.add(edge);
                    }
                    catch (GMLWorldManagerException exc) {
                        exc.printStackTrace();
                    }
                    //edgeMap.put(edge.getID(), edge);
                }
            }
        }
    }

    private class ReadFacePolicy extends XMLParserPolicy {
        public Element allowElement(Element element) {
            if (element.isTagElement()) {
                TagElement tag = (TagElement)element;
                String key = tag.getKey().toLowerCase();
                if ("rcrs:face".equals(key)) {
                    String type = tag.getAttributeValue("type").toLowerCase();
                    //out.println(tag);
                    TagElement gtag = tag.getTagChild("gml:Face");
                    GMLID gid = new GMLID(gtag.getAttributeValue("gml:id"));
                    TagElement[] edgeTags = gtag.getTagChildren("gml:directedEdge");
                    //out.println(java.util.Arrays.toString(edgeTags));
                    GMLDirectedEdge[] dedges = new GMLDirectedEdge[edgeTags.length];
                    for (int i = 0; i < edgeTags.length; i++) {
                        boolean or = "+".equals(edgeTags[i].getAttributeValue("orientation"));
                        String nn = edgeTags[i].getAttributeValue("xlink:href");
                        dedges[i] = new GMLDirectedEdge(or, new GMLID(nn.replaceAll("#", "")));
                    }
                    GMLFace face = new GMLFace(gid, gmlWorldManager, dedges);
                    face.setType(type);
                    log(face);
                    try {
                        gmlWorldManager.add(face);
                    }
                    catch (GMLWorldManagerException exc) {
                        exc.printStackTrace();
                    }
                    //faceMap.put(face.getID(), face);
                }
            }
            return null;
        }
        public ParserPolicy getInnerPolicy(Element element) {
            return keepAllPolicy;
        }
    }

    private class ReadAgentPolicy extends XMLParserPolicy {
        public Element allowElement(Element element) {
            if (element.isTagElement()) {
                TagElement tag = (TagElement)element;
                String key = tag.getKey().toLowerCase();
                if ("agent".equals(key)) {
                    String idtext = tag.getAttributeValue("id");
                    if (idtext == null) {
                        idtext = "agent" + (++agentCount);
                    }
                    GMLID id = new GMLID(idtext);
                    //out.println(tag);
                    String type = tag.getAttributeValue("type");
                    TagElement locationTag = tag.getTagChild("location");
                    GMLID area = new GMLID(locationTag.getChildValue("area"));
                    double x = Double.parseDouble(locationTag.getChildValue("x"));
                    double y = Double.parseDouble(locationTag.getChildValue("y"));
                    GMLAgent agent = new GMLAgent(id, gmlWorldManager, type, x, y, area);
                    log(agent);
                    try {
                        gmlWorldManager.add(agent);
                    }
                    catch (GMLWorldManagerException exc) {
                        exc.printStackTrace();
                    }
                    //agentMap.put(agent.getID(), agent);
                }
            }
            return null;
        }
        public ParserPolicy getInnerPolicy(Element element) {
            return keepAllPolicy;
        }
    }

    public void createRCRSInformation() {
        log("create RCRS Entity Information");

        log("   create Area");
        for (GMLFace face : gmlWorldManager.toFaceArray(new GMLFace[0])) {
            String type = face.getType().toLowerCase();
            List<GMLID> nextFaceIDList = new ArrayList<GMLID>();
            List<Point2D> nodeList = new ArrayList<Point2D>();
            for (GMLDirectedEdge dedge : face.getDirectedEdges()) {
                boolean orientation = dedge.getOrientation();
                //GMLEdge edge = edgeMap.get(dedge.getEdgeID());
                GMLEdge edge = gmlWorldManager.getEdge(dedge.getEdgeID());
                Point2D last = null;
                for (Point2D p : edge.createDirectedBorder(orientation)) {
                    if (last == null || !last.equals(p)) {
                        GMLID nextFaceID = edge.getNextFaceID(face.getID());
                        nextFaceIDList.add(nextFaceID);
                        nodeList.add(p);
                        last = p;
                    }
                }
            }
            double sumX = 0;
            double sumY = 0;
            int[] apexes = new int[nodeList.size() * 2];
            List<EntityID> nextList = new ArrayList<EntityID>();
            for (int i = 0; i < nodeList.size(); i++) {
                double x = nodeList.get(i).getX();
                double y = nodeList.get(i).getY();
                apexes[i * 2] = (int)x;
                apexes[i * 2 + 1] = (int)y;
                EntityID eid = mapID(nextFaceIDList.get(i));
                //System.out.println(nextFaceIDList.get(i) + " => " + eid);
                nextList.add(eid);
                sumX += x;
                sumY += y;
            }
            int centerX = (int)(sumX / nodeList.size());
            int centerY = (int)(sumY / nodeList.size());
            Area area = null;
            if ("building".equals(type)) {
                area = new Building(mapID(face.getID()));
            }
            else if ("refuge".equals(type)) {
                area = new Refuge(mapID(face.getID()));
            }
            else {
                area = new Area(mapID(face.getID()));
            }
            area.setApexes(apexes, nextList);
            area.setCenter(centerX, centerY);
            //area.setType();
            area.setBlockadeList(new ArrayList<EntityID>());
            rcrsMap.put(area.getID(), area);
        }

        log("   create Agent");
        for (GMLAgent agent : gmlWorldManager.toAgentArray(new GMLAgent[0])) {
            Human human = null;
            String type = agent.getType().toLowerCase();
            if ("ambulanceteam".equals(type)) {
                human = new AmbulanceTeam(mapID(agent.getID()));
            }
            else if ("firebrigade".equals(type)) {
                human = new FireBrigade(mapID(agent.getID()));
            }
            else if ("policeforce".equals(type)) {
                human = new PoliceForce(mapID(agent.getID()));
            }
            else if ("civilian".equals(type)) {
                human = new Civilian(mapID(agent.getID()));
            }
            EntityID areaID = mapID(agent.getAreaID());
            int x = (int)agent.getX();
            int y = (int)agent.getY();
            int hp = 10000;
            List<EntityID> positionHistory = new ArrayList<EntityID>();
            positionHistory.add(areaID);
            int stamina = 1000;
            int buriedness = 1000;
            int damage = 0;
            int direction = 0;
            human.setPosition(areaID, x, y);
            human.setHP(hp);
            human.setPositionHistory(positionHistory);
            human.setStamina(stamina);
            human.setBuriedness(buriedness);
            human.setDamage(damage);
            human.setDirection(direction);
            rcrsMap.put(human.getID(), human);
        }
    }

    public void waitConnectionFromKernel(int port) throws IOException, ConnectionException {
        TCPBufferedConnection connection = null;
        try {
            log("\nport: " + port);
            log("waiting for the kernel");
            ServerSocket server = new ServerSocket(port);
            server.setSoTimeout(1000);
            Socket socket = null;
            for (int time = 1; socket == null; time++) {
                try {
                    socket = server.accept();
                }
                catch (SocketTimeoutException ee) {
                    log("wait: " + time + "[s]");
                }
            }
            log("connected");

            connection = new TCPBufferedConnection(socket);
            
            log("waiting KGConnect...");
            KGConnect kg_connect = (KGConnect)connection.receiveMessage();
            log("received [KGConnect]");
            log("version: " + kg_connect.getVersion());
            
            GKConnectOK gk_connect_ok = new GKConnectOK(rcrsMap.values());
            //print("gk_connect_ok: "+gk_connect_ok);
            connection.sendMessage(gk_connect_ok);
            log("send [GKConnectOK[");
            
            KGAcknowledge kg_acknowledge = (KGAcknowledge)connection.receiveMessage();
            log("received [KGAcknowledge]");

        }
        catch (IOException e) {
            throw e;
        }
        catch (ConnectionException e) {
            throw e;
        }
        finally {
            if (connection != null) {
                connection.shutdown();
            }
        }
    }

    private int rcrsCounter = 0;
    public EntityID mapID(GMLID gmlID) {
        if (gmlID == null) {
            return new EntityID(-1);
        }
        EntityID id = gml2rcrs.get(gmlID);
        if (id == null) {
            id = new EntityID(++rcrsCounter);
            gml2rcrs.put(gmlID, id);
        }
        return id;
    }

    public void log(Object m) {
        if (m instanceof Exception) {
            Exception e = (Exception)m;
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            err.println("Error:\n" + sw.toString());
        }
        else {
            out.println(String.valueOf(m));
        }
    }
}