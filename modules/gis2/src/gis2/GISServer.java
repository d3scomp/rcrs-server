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
                public ParserPolicy getInnerPolicy(Element element) {
                    if (element.isTagElement()) {
                        TagElement tag = (TagElement)element;
                        String key = tag.getKey().toLowerCase();
                        if ("topology".equals(key) || "rcrs:area".equals(key)) {
                            return this;
                        }
                        else if ("agent_list".equals(key)) {
                            return readAgentPolicy;
                        }
                        else if ("rcrs:nodelist".equals(key)) {
                            return readNodePolicy;
                        }
                        else if ("rcrs:edgelist".equals(key)) {
                            return readEdgePolicy;
                        }
                        else if ("rcrs:facelist".equals(key)) {
                            return readFacePolicy;
                        }
                    }
                    return new XMLParserPolicy();
                }
            });
        parser.parse();
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
            return null; 
        }
        public ParserPolicy getInnerPolicy(Element element) {
            return keepAllPolicy;
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