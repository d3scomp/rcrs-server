package gis2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import org.util.xml.element.Element;
import org.util.xml.element.TagElement;
import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.*;

import java.util.*;
import rescuecore2.worldmodel.*;
import rescuecore2.connection.*;
import rescuecore2.messages.*;
import rescuecore2.messages.control.*;
import rescuecore2.standard.entities.*;

import traffic3.log.event.*;
import traffic3.manager.*;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.area.*;
import traffic3.objects.*;
import static traffic3.log.Logger.log;

import java.awt.geom.Line2D;

/**
 * GIS Server.<br/>
 * java gis2.Main config/gis2.xml
 */
public class GISServer {
    
    public static final File DEFAULT_CONFIG_FILE = new File("./config/gis2.gml");
    public static final File DEFAULT_AGENT_FILE = new File("./config/agent.xml");

    private static HashMap<String, EntityID> gmlid_rcrsid_map = new HashMap<String, EntityID>();

    private GISServer(){}

    public static void startProcess(File file1, File file2, int port) throws Exception {
	
	print("config file: " + file1.getAbsolutePath());
	print("config file: " + file2.getAbsolutePath());
	print("waiting port: " + port);
	System.err.println("started gis server");
	HashMap<EntityID, Entity> pool = importFromFile(file1, file2);
	checkObjects(pool);
	System.err.println("> now waiting for ECSKernel...");
	startWaiting(port, pool);
	System.err.println("> finished to send all the information.");
    }



    private static HashMap<EntityID, Entity> importFromFile(File file1, File file2) throws Exception {

	final HashMap<EntityID, Entity> pool = new HashMap<EntityID, Entity>();
	WorldManager world_manager = new WorldManager();
	int unique_number = 1;
	world_manager.open(file1);
	world_manager.open(file2);

	final WorldManagerGUI wmg = new WorldManagerGUI(world_manager);
	wmg.createGUI();
	wmg.setPreferredSize(new java.awt.Dimension(500, 500));
	final javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
	panel.add(wmg, java.awt.BorderLayout.CENTER);
	panel.add(wmg.getMenuBar(), java.awt.BorderLayout.NORTH);
	final Object finish_flag = new Object();
	// this should be fixed!! 
	// fitView() method should be called after appearing gui.
	javax.swing.SwingUtilities.invokeAndWait(new Runnable(){public void run(){
	    try{
		final javax.swing.JFrame frame = new javax.swing.JFrame();
		javax.swing.JPanel buttonpane = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		buttonpane.add(new javax.swing.JButton(new javax.swing.AbstractAction("Finished"){
			public void actionPerformed(java.awt.event.ActionEvent e) {
			    synchronized(finish_flag) {
				finish_flag.notifyAll();
				frame.setVisible(false);
				frame.dispose();
			    }
			}
		    }));
		javax.swing.JPanel contentpane = new javax.swing.JPanel(new java.awt.BorderLayout());
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("You can edit initial state.");
		sb.append("</html>");
		javax.swing.JLabel label = new javax.swing.JLabel(sb.toString());
		contentpane.add(label, java.awt.BorderLayout.NORTH);
		contentpane.add(panel, java.awt.BorderLayout.CENTER);
		contentpane.add(buttonpane, java.awt.BorderLayout.SOUTH);
		
		frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		frame.setContentPane(contentpane);
		frame.pack();
		frame.setVisible(true);
	    }catch(Exception exc){exc.printStackTrace();}
	}});

	wmg.fitView();

	synchronized(finish_flag) {
	    finish_flag.wait();
	}
	
	TrafficArea[] area_list = world_manager.getAreaList();
	for(TrafficArea traffic_area : area_list) {
	    String gmlid = traffic_area.getID();
	    EntityID rcrsid = getID(gmlid, gmlid_rcrsid_map);

	    Area rcrs_area = null;
	    if(traffic_area.getType().toLowerCase().equals("building"))
		rcrs_area = new Building(rcrsid);
	    else
		rcrs_area = new Area(rcrsid);
	    
	    rcrs_area.setCenter((int)traffic_area.getCenterX(), (int)traffic_area.getCenterY());
	    TrafficAreaNode[] node_list = traffic_area.getNodeList();
	    int[] shape = new int[node_list.length*2];
	    ArrayList<EntityID> nexts = new ArrayList<EntityID>();
	    
	    TrafficAreaNode n = node_list[0];
	    double x = n.getX();
	    double y = n.getY();
	    shape[0] = (int)x;
	    shape[1] = (int)y;
	    double lx = x;
	    double ly = y;
	    TrafficAreaNode ln = n;
	    for(int i=1; i<node_list.length; i++) {
		n = node_list[i];
		x = n.getX();
		y = n.getY();
		shape[i*2]   = (int)x;
		shape[i*2+1] = (int)y;
		TrafficArea next = null;
		for(TrafficAreaEdge e : traffic_area.getConnectorEdgeList()) {
		    if(e.has(new java.awt.geom.Line2D.Double(x, y, lx, ly))) {
			next = e.getNextArea(traffic_area);
			break;
		    }
		}
		if(next == null)
		    nexts.add(new EntityID(-1));
		else {
		    EntityID id = getID(next.getID(), gmlid_rcrsid_map);
		    nexts.add(id);
		}
		ln = n;
		lx = x;
		ly = y;
	    }
	    n = node_list[0];
	    nexts.add(new EntityID(-1));
	    
	    rcrs_area.setShape(shape, nexts);
	    pool.put(rcrs_area.getID(), rcrs_area);

	    ArrayList<EntityID> eidl = new ArrayList<EntityID>();
	    for(TrafficBlockade tb : traffic_area.getBlockadeList()) {
		Blockade blockade = new Blockade(new EntityID(getUniqueNumber()));
		eidl.add(blockade.getID());
		blockade.setCenter((int)tb.getCenterX(), (int)tb.getCenterY());
		Line2D[] line_list = tb.getLineList();
		int[] xy = new int[(line_list.length)*2];
		//xy[0] = (int)line_list[0].getP1().getX();
		//xy[1] = (int)line_list[0].getP1().getY();
		for(int i=0; i<line_list.length; i++) {
		    xy[i*2] = (int)line_list[i].getP1().getX();
		    xy[i*2+1] = (int)line_list[i].getP1().getY();
		}
		blockade.setShape(xy);
		EntityID area_id = rcrs_area.getID();
		blockade.setArea(area_id);
		Area area = (Area)pool.get(area_id);
		ArrayList<EntityID> blockade_list = new ArrayList<EntityID>();
		blockade_list.add(blockade.getID());
		area.setBlockadeList(blockade_list);
		pool.put(blockade.getID(), blockade);

	    }
	    rcrs_area.setBlockadeList(eidl);
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

    private static EntityID getID(String gml_id, HashMap<String, EntityID> map) {
	EntityID id = map.get(gml_id);
	if(id!=null) return id;
	id = new EntityID(getUniqueNumber());
	map.put(gml_id, id);
	return id;
    }

    private static int uniquenumber = 1;
    private static int getUniqueNumber() {
	return uniquenumber++;
    }

    private static ConnectionListener connection_listener_;
    private static ArrayList<Message> message_list_ = new ArrayList<Message>();
    private static Message receiveMessage() {
	synchronized(connection_listener_) {
	    while(true) {
		if(message_list_.size()!=0) break;
		try{
		    connection_listener_.wait();
		}catch(Exception e){e.printStackTrace();}
	    }
	}
	return message_list_.remove(0);
    }

    private static void startWaiting(int port, HashMap<EntityID, Entity> pool) throws Exception {
	print("waiting for kernel");
	ServerSocket server = new ServerSocket(port);
	Socket socket = server.accept();
	TCPConnection connection = new TCPConnection(socket);
	connection_listener_ = new ConnectionListener(){
		public void messageReceived(Connection c, Message m) {
		    message_list_.add(m);
		    synchronized(connection_listener_) {
			connection_listener_.notifyAll();
		    }
		}
	    };

	connection.addConnectionListener(connection_listener_);
	connection.startup();

	print("waiting kgconnect");
	KGConnect kg_connect = (KGConnect)receiveMessage();
	print("received kgconnect");
	print("version: "+kg_connect.getVersion());
	
	GKConnectOK gk_connect_ok = new GKConnectOK(pool.values());
	//print("gk_connect_ok: "+gk_connect_ok);
	connection.sendMessage(gk_connect_ok);
	
	KGAcknowledge kg_acknowledge = (KGAcknowledge)receiveMessage();
	print("received acknowledge");
    }

    public static void checkObjects(HashMap<EntityID, ? extends Entity> pool) {
	
	print("checking objects.");
    }
    
    private static void addTestBlockade(HashMap<EntityID, Entity> pool) {
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
	















    
    private static void print(String title){
        System.out.print("\n\n");
        System.out.println("< "+title+" ["+new Date()+"] >");
        System.out.print("\n\n");
    }
    
    private static String sspace = "  ______________________________________________________________________";
    private static String uspace = " /                                                                      \\";
    private static String dspace = " \\______________________________________________________________________/";
    private static long starttime_;

    private static void printStart(String title){
        System.out.print("\n");
        String message = "start " + title+" ["+new Date()+"]";
        StringBuffer space = new StringBuffer();
        for(int i=message.length();i<70;i++) space.append(" ");
        System.out.println(sspace);
        System.out.println(uspace);
        System.out.println("/ " + message + space + " \\\n");
        starttime_ = System.currentTimeMillis();
    }
    private static void printEnd(String title){
        long time = System.currentTimeMillis()-starttime_;
        String message = "end " + title+" ("+time+"[ms])["+new Date()+"]";
        StringBuffer space = new StringBuffer();
        for(int i=message.length();i<70;i++) space.append(" ");
        System.out.println("\n\\ " + message + space +" /");
        System.out.println(dspace);
        System.out.print("\n\n");
    }

    public static void alert(Object message) {
	javax.swing.JOptionPane.showMessageDialog(null, message);
    }
    public static void alert2(final Object message) {
	new Thread(new Runnable(){
		public void run() {
		    try{
			javax.swing.JOptionPane.showMessageDialog(null, message);
		    }catch(Exception e){ e.printStackTrace();}
		}
	    }, "alert").start();
    }
}
