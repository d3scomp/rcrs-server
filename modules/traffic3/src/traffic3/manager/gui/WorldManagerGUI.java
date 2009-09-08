package traffic3.manager.gui;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.geom.*;

import traffic3.manager.*;
import traffic3.simulator.*;
import traffic3.objects.*;
import traffic3.objects.area.*;
import traffic3.objects.area.event.*;
import traffic3.io.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

public class WorldManagerGUI extends JComponent {

    private WorldManagerGUI this_ = this;

    private WorldManager world_manager_;
    private JMenuBar menu_bar_;
    private BufferedImage image_;
    private BufferedImage agent_layer_image_;

    private BufferedImage video_image_;
    // private SimpleVideoBufferedOutputStream svbo_;

    // private BufferedImage offimage_;
    private HashMap<String, ArrayList<TrafficAgent>> agent_group_list_ = new HashMap<String, ArrayList<TrafficAgent>>();
    private ArrayList<String> selected_agent_group_list_ = new ArrayList<String>();
    private HashMap<String, TrafficObject> target_list_ = new HashMap<String, TrafficObject>();
    private ArrayList<TrafficObject> destination_list_ = new ArrayList<TrafficObject>();
    private double zoom_, offset_x_, offset_y_;
    private volatile boolean dragging_;
    private JCheckBox show_network_, show_area_area_, show_area_node_, show_area_edge_, show_area_connector_, show_node_ids_, antialiasing_on_;
    private Color selected_color_, area_node_color_, area_edge_color_, area_color_, network_edge_color_, network_node_color_, area_connector_color_;
    private int mouse_pressed_button_;
    private Stroke area_edge_stroke_, area_connector_edge_stroke_, network_edge_stroke_;
    private JRadioButton select_mode_, input_area_mode_, input_network_mode_, put_agent_mode_;
    private JCheckBox simulate_;
    private JLabel status_label_;

    // rendering option
    private int image_update_term_ = 30;
    private JCheckBox agent_direct_rendering_mode_;


    public WorldManagerGUI(WorldManager world_manager) {
	world_manager_ = world_manager;
	world_manager_.addWorldManagerListener(new WorldManagerListener(){
		private long last_update_agent_layer_time_ = -1;
		
		@Override
		public void added(WorldManagerEvent e) {
		    createImageInOtherThread();
		}
		@Override
		public void removed(WorldManagerEvent e) {
		    createImageInOtherThread();
		}
		@Override
		public void changed(WorldManagerEvent e) {
		    
		}
		@Override
		public void mapUpdated(WorldManagerEvent e) {
		    createImageInOtherThread();
		}
		@Override
		public void agentUpdated(WorldManagerEvent e) {
		    if(e.getSource() instanceof RCRSTrafficSimulator) {
			simulation_time_ = ((RCRSTrafficSimulator)e.getSource()).getTime();
			long update_agent_layer_time = System.currentTimeMillis();
			if((update_agent_layer_time-last_update_agent_layer_time_) > image_update_term_) {
			    updateAgentLayer();
			    repaint();
			    last_update_agent_layer_time_ = update_agent_layer_time;
			}

		    } else {
			updateAgentLayer();
			repaint();
		    }
		}
		@Override
		public void inputted(WorldManagerEvent e) {
		    fitView();
		    createImageInOtherThread();
		}

	    });
	createGUI();
	log("world manager is initialized");
    }

    public void createGUI() {

	AbstractAction create_image_action = new AbstractAction("create image action"){
		public void actionPerformed(ActionEvent e) {
		    createImageInOtherThread();
		}
	    };

	show_network_ = new JCheckBox("show network");
	show_area_area_ = new JCheckBox("show area");
	show_area_node_ = new JCheckBox("show area node");
	show_area_edge_ = new JCheckBox("show area edge");
	show_area_connector_ = new JCheckBox("show area connector");
	show_node_ids_ = new JCheckBox("show node ids");
	antialiasing_on_ = new JCheckBox("antialiasing");
	simulate_ = new JCheckBox("simulate");

	agent_direct_rendering_mode_ = new JCheckBox("direct rendering");

	show_network_.setSelected(false);
	show_area_area_.setSelected(true);
	show_area_node_.setSelected(false);
	show_area_edge_.setSelected(true);
	show_area_connector_.setSelected(false);
	show_node_ids_.setSelected(false);
	antialiasing_on_.setSelected(false);
	simulate_.setSelected(false);
	agent_direct_rendering_mode_.setSelected(true);

	show_network_.addActionListener(create_image_action);
	show_area_area_.addActionListener(create_image_action);
	show_area_node_.addActionListener(create_image_action);
	show_area_edge_.addActionListener(create_image_action);
	show_area_connector_.addActionListener(create_image_action);
	show_node_ids_.addActionListener(create_image_action);
	simulate_.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    new Thread(new Runnable(){public void run(){
			if(simulate_.isSelected())
			    startSimulation();
			else
			    stopSimulation();
		    }}, "simulation thread").start();
		}
	    });
	antialiasing_on_.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    createImageInOtherThread();
		}
	    });
	area_connector_edge_stroke_ = new BasicStroke(3f);
	area_edge_stroke_ = new BasicStroke(3f);
	network_edge_stroke_ = new BasicStroke(3f);
	selected_color_ = new Color(255, 100, 100);
	area_connector_color_ = new Color(255, 150, 150);
	area_node_color_ = new Color(0, 0, 0);
	area_edge_color_ = new Color(50, 50, 50);
	area_color_ = new Color(255, 200, 200);
	network_edge_color_ = new Color(100, 255, 100);
	network_node_color_ = new Color(0, 100, 0);

	select_mode_ = new JRadioButton("select mode");
	input_area_mode_ = new JRadioButton("input area mode");
	input_network_mode_ = new JRadioButton("input network mode");
	put_agent_mode_ = new JRadioButton("put agent mode");
	ButtonGroup bg = new ButtonGroup();
	bg.add(select_mode_);
	bg.add(input_area_mode_);
	bg.add(input_network_mode_);
	bg.add(put_agent_mode_);
	select_mode_.setSelected(true);

	createMenuBar();

	status_label_ = new JLabel("status");

	setFocusable(true);
	zoom_ = 40.0;
	offset_x_ = 0;
	offset_y_ = 0;
	transform_.setTransform(zoom_, 0, 0, -zoom_, offset_x_, offset_y_);
 
	addDragAndDropListener();
	addResizeListener();
	OrgMouseListener oml = new OrgMouseListener();
	addMouseListener(oml);
	addMouseMotionListener(oml);
	addMouseWheelListener(oml);
	addKeyListener(new KeyAdapter(){
		public void keyPressed(KeyEvent e) { // i, is already used in menubar.
		    switch(e.getKeyChar()) {
		    case 's': select_mode_.setSelected(true); break;
		    case 'a': input_area_mode_.setSelected(true); break;
		    case 'n': input_network_mode_.setSelected(true); break;
		    case 'p': put_agent_mode_.setSelected(true); break;
		    case 'l': simulate_.doClick(); break;
		    default: System.out.println(e.getKeyCode());
		    }

		}
	    });
	setFocusable(true);
    }

    private class OrgMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
	private int mouse_last_x, mouse_last_y;
	private boolean select_click;
	public void mousePressed(MouseEvent e){
	    requestFocus();
	    dragging_ = true;
	    mouse_pressed_button_ = e.getButton();
	    if(mouse_pressed_button_==1) {

		if(select_mode_.isSelected()) {
		    
		    if(e.isControlDown()) {
			try{
			    TrafficAreaNode tan = world_manager_.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
			    if(selected_agent_group_list_.size()==0) {
				TrafficAgent[] agent = world_manager_.getAgentList();
				for(int i=0; i<agent.length; i++)
				    agent[i].setDestination(tan);
			    } else {
				String[] selected_agent_group_name_list = selected_agent_group_list_.toArray(new String[0]);
				for(int j=0; j<selected_agent_group_name_list.length; j++) {
				    ArrayList<TrafficAgent> agent_list = agent_group_list_.get(selected_agent_group_list_.get(j));
				    for(TrafficAgent agent: agent_list)
					agent.setDestination(tan);
				}
			    }
			}catch(Exception exc) {
			    alert(exc, "error");
			}
		    } else {
			select_click = listAllHitsToTarget(sx2mx(e.getX()), sy2my(e.getY()), !e.isShiftDown());
		    }
		} else if(input_area_mode_.isSelected()) {
		    alert(new UnsupportedOperationException(), "error");
		} else if(input_network_mode_.isSelected()) {
		    //alert(new UnsupportedOperationException(), "error");
		    try{	    
			TrafficAreaNode tan = world_manager_.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
		    }catch(Exception exc){
			alert(exc, "error");
		    }
		}
	    } else if(mouse_pressed_button_==3) {

		final double x = sx2mx(e.getX());
		final double y = sy2my(e.getY());
		final JPopupMenu popup = new JPopupMenu();
		dragging_ = false;
		popup.add(getShowTargetsAction());

		popup.add(new AbstractAction("switch [open space simulation]<=>[network simulation]"){
			public void actionPerformed(ActionEvent e) {
			    TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);
			    for(TrafficObject o : target_list)
				if(o instanceof TrafficArea) {
				    throw new RuntimeException("not supported yet");
				}
			    createImageInOtherThread();
			}
		    });

		popup.add(new AbstractAction("put agent"){
			public void actionPerformed(ActionEvent e) {
			    try{
				int number = inputInt("How many agent do you want to put?");
				String group_name = inputString("Input group type.(If empty, new group will not be created.)");
				ArrayList<TrafficAgent> agent_list = new ArrayList<TrafficAgent>();
				for(int i=0; i<number; i++) {
				    TrafficAgent agent = new TrafficAgent(world_manager_);
				    agent.setType(group_name);
				    agent_list.add(agent);
				    agent.setLocation(x, y, 0);
				    world_manager_.appendWithoutCheck(agent);
				}
			    }catch(Exception exc) {
				exc.printStackTrace();
			    }
			}
		    });

		popup.add(new AbstractAction("put blockade"){
			public void actionPerformed(ActionEvent e) {
			    try{
				TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);
				
				double width = inputDouble("input blockade width [0-100]%.");
				for(TrafficObject tobj : target_list) {
				    if(!(tobj instanceof TrafficArea)) {
					log("cannot put blockade to "+tobj);
					continue;
				    }

				    TrafficArea tarea = (TrafficArea)tobj;
				    TrafficBlockade tblockade = new TrafficBlockade(world_manager_, world_manager_.getUniqueID("_"));
				    TrafficAreaNode[] node_list = tarea.getNodeList();
				    //tblockade.setCenter(node_list[0].getX(), node_list[0].getY());
				    
				    double cx = tarea.getCenterX();
				    double cy = tarea.getCenterY();
				    int[] xy_list = new int[node_list.length*2];
				    int index = 0;
				    for(TrafficAreaNode node : node_list) {
					//double d = 0.5+0.499999*Math.random();
					double d = width*(0.8+(Math.random()*0.2))/101.0;
					//double d = width/100.0;
					double x = node.getX()*d+cx*(1-d);
					double y = node.getY()*d+cy*(1-d);
					xy_list[index++] = (int)x;
					xy_list[index++] = (int)y;
				    }
				    tblockade.setCenter(cx, cy);
				    tblockade.setLineList(xy_list);
				    world_manager_.appendWithoutCheck(tblockade);
				    tarea.addBlockade(tblockade);
				}
				createImageInOtherThread();
			    }catch(Exception exc) {
				alert(exc, "error");
			    }
			}
		    });
		
		
		
		
		popup.add(new AbstractAction("put blockade2"){
			public void actionPerformed(ActionEvent e) {
			    try{
				TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);
				
				//double width = inputDouble("input blockade width [0-100]%.");
				for(TrafficObject tobj : target_list) {
				    if(!(tobj instanceof TrafficArea)) {
					log("cannot put blockade to "+tobj);
					continue;
				    }

				    TrafficArea tarea = (TrafficArea)tobj;
				    TrafficAreaNode[] node_list = tarea.getNodeList();
				    int length = node_list.length;
				    int[] xs = new int[length];
				    int[] ys = new int[length];
				    for(int i=0; i<length; i++) {
					xs[i] = (int)node_list[i].getX();
					ys[i] = (int)node_list[i].getY();
				    }
				    Polygon polygon = new Polygon(xs, ys, length);
				    //alert(polygon, "error");
				    Area area = new Area(polygon);
				    Rectangle rect = area.getBounds();
				    
				    Area sub_area = null;
				    int wcount = (int)(rect.getWidth()/5000+1);
				    int hcount = (int)(rect.getHeight()/5000+1);
				    double x = rect.getX();
				    double y = rect.getY();
				    double w = rect.getWidth()/wcount;
				    double h = rect.getHeight()/hcount;
				    for(int j=0; j<hcount; j++)
					for(int i=0; i<wcount; i++) {
					    sub_area = new Area(new Rectangle2D.Double(x+i*w, y+j*h, w, h));
					    sub_area.intersect(area);
					    PathIterator path_iterator = sub_area.getPathIterator(new AffineTransform(1, 0, 0, 1, 0, 0));
					    ArrayList<Point2D> point_list = new ArrayList<Point2D>();
					    double[] xyd_list = new double[6];
					    for(; !path_iterator.isDone(); path_iterator.next()) {
						int type = path_iterator.currentSegment(xyd_list);
						point_list.add(new Point2D.Double(xyd_list[0], xyd_list[1]));
					    }

					    if(point_list.size() == 0) continue;
					    
					    TrafficBlockade tblockade = new TrafficBlockade(world_manager_, world_manager_.getUniqueID("_"));
					    
					    tblockade.setCenter(node_list[0].getX(), node_list[0].getY());
					    int[] xy_list = new int[point_list.size()*2];
					    int index = 0;
					    double cx = point_list.get(0).getX();
					    double cy = point_list.get(1).getY();
					    double xsum = 0;
					    double ysum = 0;
					    for(Point2D p : point_list) {
						//double d = 0.5+0.499999*Math.random();
						//double d = width*(0.8+(Math.random()*0.2))/101.0;
						//double d = width/100.0;
						double d = 1;
						double xx = p.getX()+Math.random()*d-d/2;
						double yy = p.getY()+Math.random()*d-d/2;
						xsum += xx;
						ysum += yy;
						xy_list[index++] = (int)xx;
						xy_list[index++] = (int)yy;
					    }
					    cx = (xsum/point_list.size());
					    cy = (ysum/point_list.size());
					    tblockade.setCenter(cx, cy);
					    tblockade.setLineList(xy_list);
					    world_manager_.appendWithoutCheck(tblockade);
					    tarea.addBlockade(tblockade);
					}
				}
				createImageInOtherThread();
			    }catch(Exception exc) {
				alert(exc, "error");
			    }
			}
		    });
		
		
		
		
		popup.add(new AbstractAction("set area type"){
			public void actionPerformed(ActionEvent e) {
			    try{
				String area_type = inputString("Input area type.");
				TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);
				for(TrafficObject o : target_list)
				    if(o instanceof TrafficArea) {
					TrafficArea a = (TrafficArea)o;
					a.setType(area_type);
				    }
				createImageInOtherThread();
		    }catch(Exception exc) {
				exc.printStackTrace();
			    }
			}
		    });
		popup.show(this_, e.getX(), e.getY());
	    }
	}
	
	public void mouseReleased(MouseEvent e){
	    select_click=false;
	    dragging_=false;
	    mouse_pressed_button_ = -1;
	    createImageInOtherThread();
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseMoved(MouseEvent e){
	    mouse_last_x = e.getX();
	    mouse_last_y = e.getY();
	    StringBuffer sb = new StringBuffer();
	    double x = sx2mx(mouse_last_x);
	    double y = sy2my(mouse_last_y);
	    sb.append("(").append((int)(x/1000)).append("[m], ").append((int)(y/1000)).append("[m])");
	    sb.append("(").append((int)(x)).append("[mm], ").append((int)y).append("[mm])");
	    setStatus(sb.toString());
	}
	public void mouseDragged(MouseEvent e){
	    int x = e.getX();
	    int y = e.getY();
	    if(mouse_pressed_button_==1 && !select_click && select_mode_.isSelected())
		drag(x-mouse_last_x, y-mouse_last_y);
	    mouse_last_x = x;
	    mouse_last_y = y;
	}
	public void mouseWheelMoved(MouseWheelEvent e){
	    zoom((e.getWheelRotation()<0 ? 1.1 : 0.9), e.getX(), e.getY());
	}
    }

    private double simulation_time_ = -1;
    private long simulation_start_wallclock_time_ = -1;
    private volatile boolean is_simulation_running_;
    public void startSimulation() {
	double dt = 100;
	if(is_simulation_running_) return;
	is_simulation_running_ = true;

	ArrayList<Runnable> end_task_list = new ArrayList<Runnable>();

	/*
	try{ // monitor 19768
	    String id = "19768";
	    final BufferedWriter bw = new BufferedWriter(new FileWriter("id"+id+"_enter_monitor.log"));
	    bw.write("enter monitor of "+id);
	    bw.newLine();
	    bw.write("Simulation Time, WallclockTime");
	    bw.newLine();
	    bw.newLine();
	    final TrafficArea area = (TrafficArea)world_manager_.getTrafficObject(id); 
	    final TrafficAreaListener listener = new TrafficAreaListener() {
		    public void entered(TrafficAreaEvent e) {try{
			bw.write(String.valueOf(simulation_time_));
			bw.write(", "+(System.currentTimeMillis()-simulation_start_wallclock_time_));
			bw.write(", "+e.getAgent().getID());
			bw.write(", "+e.getAgent().getLogDistance());
			bw.newLine();
			bw.flush();
			}catch(Exception exc){alert(exc, "error");}}
		    public void exited(TrafficAreaEvent e) {  }
	    };
	    area.addTrafficAreaListener(listener);
	    end_task_list.add(new Runnable(){
		    public void run() {
			try{
			    bw.flush();
			    bw.close();
			}catch(Exception e){
			    e.printStackTrace();
			}
			area.removeTrafficAreaListener(listener);
		    }
		});
	}catch(Exception exc){ log(exc, "error"); }
	try{ // monitor 19768
	    String id = "2087";
	    final BufferedWriter bw = new BufferedWriter(new FileWriter("id"+id+"_enter_monitor.log"));
	    bw.write("enter monitor of "+id);
	    bw.newLine();
	    bw.write("Simulation Time, WallclockTime");
	    bw.newLine();
	    bw.newLine();
	    final TrafficArea area = (TrafficArea)world_manager_.getTrafficObject("2087"); 
	    final TrafficAreaListener listener = new TrafficAreaListener() {
		    public void entered(TrafficAreaEvent e) {try{
			bw.write(String.valueOf(simulation_time_));
			bw.write(", "+(System.currentTimeMillis()-simulation_start_wallclock_time_));
			bw.write(", "+e.getAgent().getID());
			bw.write(", "+e.getAgent().getLogDistance());
			bw.newLine();
			bw.flush();
			}catch(Exception exc){alert(exc, "error");}}
		    public void exited(TrafficAreaEvent e) {  }
	    };
	    area.addTrafficAreaListener(listener);
	    end_task_list.add(new Runnable(){
		    public void run() {
			try{
			    bw.flush();
			    bw.close();
			}catch(Exception e){
			    e.printStackTrace();
			}
			area.removeTrafficAreaListener(listener);
		    }
		});
	}catch(Exception exc){ log(exc, "error"); }
	*/







	try{
	    TrafficAgent[] agent_list = world_manager_.getAgentList();
	    for(int i=0; i<agent_list.length; i++) {
		agent_list[i].clearLogDistance();
	    }
	    traffic3.simulator.Simulator simulator = new traffic3.simulator.Simulator(world_manager_, dt);
	    simulation_start_wallclock_time_ = System.currentTimeMillis();
	    while(is_simulation_running_) {
		simulator.step();
		updateAgentLayer();
		simulation_time_ = simulator.getTime();
		repaint();
		// try{Thread.sleep(1000);}catch(Exception e){}
	    }
	}catch(Exception e) {
	    alert(e, "error");
	}finally{
	    for(Runnable end_task : end_task_list)
		end_task.run();
	}
	
    }
    public void stopSimulation() {
	is_simulation_running_ = false;
    }
    

    private boolean listAllHitsToTarget(double mx, double my, boolean clear) {
	boolean is_added_ = false;
	if(clear)
	    target_list_.clear();

	TrafficAgent[] agent_list = world_manager_.getAgentList();
	for(int i=0; i<agent_list.length; i++) {
	    TrafficAgent agent = agent_list[i];
	    double dx = agent.getX()-mx;
	    double dy = agent.getY()-my;
	    if(agent.getRadius()*agent.getRadius() > dx*dx+dy*dy) {
		target_list_.put(agent.getID(), agent);
		is_added_ = true;
	    }
	}

	if(!is_added_ && show_area_node_.isSelected()) {
	    TrafficAreaNode node = world_manager_.getNearlestAreaNode(mx,my,0);
	    if(node!=null) {
		double d = 3.0/zoom_;
		System.out.println(node.getDistance(mx,my,0));
		if(node.getDistance(mx,my,0) < d) {
		    target_list_.put(node.getID(), node);
		    is_added_ = true;
		}
	    }
	}

	if(!is_added_ && show_area_edge_.isSelected()) {
	    TrafficAreaEdge[] area_edge = world_manager_.getAreaConnectorEdgeList();
	    for(int i=0; i<area_edge.length; i++) {
		TrafficAreaEdge edge = area_edge[i];
		if(area_edge[i].distance(mx, my)<2/zoom_) {
		    target_list_.put(area_edge[i].getID(), area_edge[i]);
		    is_added_ = true;
		}
	    }
	}

	if(!is_added_ && show_area_area_.isSelected()) {
	    TrafficArea[] area = world_manager_.getAreaList();
	    for(int i=0; i<area.length; i++)
		if(area[i].getShape().contains(mx, my)) {
		    target_list_.put(area[i].getID(), area[i]);
		    is_added_ = true;
		}
	}

	if(target_list_.size()==0)
	    log("clear target");
	else {
	    StringBuffer sb = new StringBuffer("setTarget[");
	    sb.append(target_list_.toString());
	    sb.append("]");
	    log(sb);
	}
	//getShowTargetsAction().setEnabled(target_list_.size()!=0);
	return is_added_;
    }

    private void zoom(double dzoom, double x, double y) {
	offset_x_ = (offset_x_-x)*dzoom + x;
	offset_y_ = (offset_y_-y)*dzoom + y;
	zoom_ *= dzoom;
	transform_.setTransform(zoom_, 0, 0, -zoom_, offset_x_, offset_y_);
	createImageInOtherThread();
    }

    private void drag(double dx, double dy) {
	offset_x_ += dx;
	offset_y_ += dy;
	transform_.setTransform(zoom_, 0, 0, -zoom_, offset_x_, offset_y_);
	createImageInOtherThread();
    }

    private void addDragAndDropListener() {
	setTransferHandler(new TransferHandler(null){
		public boolean canImport(TransferHandler.TransferSupport support) {
		    Transferable trans = support.getTransferable();
		    if(trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return true;
		    if(trans.isDataFlavorSupported(DataFlavor.stringFlavor)) return true;
		    return false;
		}
		public boolean importData(TransferHandler.TransferSupport support) {
		    Transferable trans = support.getTransferable();
		    if(trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try{
			    java.util.List<File> data = _(trans.getTransferData(DataFlavor.javaFileListFlavor));
			    for(Iterator<File> it=data.iterator(); it.hasNext(); ) {
				File file = it.next();
				open(file);
			    }
			}catch(Exception e) {
			    alert(e, "error");
			}
			return true;
		    }
		    if(trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try{
			    String data = (String)trans.getTransferData(DataFlavor.stringFlavor);
			    String[] data_list = data.split("\r\n");
			    for(int i=0; i<data_list.length; i++)
				open(new URI(data_list[i]));
			}catch(Exception e){
			    alert(e, "error");
			}
			return true;
		    }
		    return false;
		}
	    });
    }

    public void addResizeListener() {
	addComponentListener(new ComponentAdapter(){
		public void componentResized(ComponentEvent e) {
		    createImageInOtherThread();
		}
	    });
    }

    private void createMenuBar() {
	menu_bar_ = new JMenuBar();
	
	JMenu file_menu = new JMenu("File");
	file_menu.setMnemonic(KeyEvent.VK_F);

	file_menu.add(new JMenuItem(getImportAction()));
	file_menu.add(new JMenuItem(getExportAction()));
	file_menu.add(new JMenuItem(getClearAllAction()));
	file_menu.addSeparator();
	file_menu.add(new JMenuItem(getExitAction()));

	JMenu view_menu = new JMenu("View");
	view_menu.setMnemonic(KeyEvent.VK_V);
	view_menu.add(new JMenuItem(getFitViewAction()));
	view_menu.addSeparator();
	view_menu.add(show_network_);
	view_menu.add(show_area_area_);
	view_menu.add(show_area_node_);
	view_menu.add(show_area_edge_);
	view_menu.add(show_area_connector_);
	view_menu.add(show_node_ids_);
	view_menu.add(antialiasing_on_);
	view_menu.addSeparator();
	view_menu.add(getShowTargetsAction());
	view_menu.add(getShowAllAction());
	view_menu.add(getChangeAgentLayerUpdateRate());
	view_menu.add(agent_direct_rendering_mode_);
	
	JMenu edit_menu = new JMenu("Edit");
	edit_menu.setMnemonic(KeyEvent.VK_E);
	edit_menu.add(select_mode_);
	edit_menu.add(input_area_mode_);
	edit_menu.add(input_network_mode_);
	edit_menu.add(put_agent_mode_);
	edit_menu.add(new JMenuItem(getSelectAllAction()));
	edit_menu.add(new JMenuItem(getSelectByIDAction()));
	edit_menu.add(new JMenuItem(getSelectAgentGroupAction()));

	JMenu devel_menu = new JMenu("Debug");
	devel_menu.setMnemonic(KeyEvent.VK_D);

	devel_menu.add(simulate_);
	devel_menu.add(new JMenuItem(getShowLogAction()));
	devel_menu.add(new JMenuItem(new AbstractAction("Version"){
		public void actionPerformed(ActionEvent e) {
		    String message = "<html><div style='font-size:120%;'>"+traffic3.Main.getVersion()+"</div></html>";
		    JOptionPane.showMessageDialog(this_, message);
		}
	    }));
	devel_menu.add(new JMenuItem(getVideoRecAction()));
	
	menu_bar_.add(file_menu);
	menu_bar_.add(view_menu);
	menu_bar_.add(edit_menu);
	menu_bar_.add(devel_menu);
    }


    public void open(URI uri) throws Exception {
	if(uri.getScheme().equals("file"))
	    open(new File(uri));
	else {
	    throw new Exception("open url unsupported.");
	    //world_manager_.open(uri.toURL().openStream());
	    //fitView();
	    //alert("Successfuly imported.\n\tThere are "+world_manager_.getAll().length+" objects.", "information");
	}
    }
    public void open(File file) throws Exception {
	world_manager_.open(file);
	fitView();
	alert("Successfuly imported.\n\tThere are "+world_manager_.getAll().length+" objects.", "information");
    }

    public void save(File file) throws Exception {
	Parser[] parser_list = world_manager_.getParserList();
	String message = "select export type";
	String title = "select";
	int type = JOptionPane.INFORMATION_MESSAGE;
	Icon icon = null;
	Object[] choice = parser_list;
	Object selection = JOptionPane.showInputDialog(this, message, title, type, icon, choice, choice[0]);
	save(file, (Parser)selection);
	alert("Successfuly exported.");
    }
    public void save(File file, Parser parser) throws Exception {
	world_manager_.save(file, parser);
    }

    public void createImageInOtherThread() {
	new Thread(new Runnable(){public void run(){
	    createImage();
	}}, "create image").start();
    }

    private volatile boolean drawing_ = false;
    private volatile boolean cancel_to_draw_ = false;
    private volatile AffineTransform transform_ = new AffineTransform(zoom_, 0, 0, -zoom_, offset_x_, offset_y_);
    private volatile boolean non_stop_creating_image_thread_is_running_ = false;

    private void createImage() {
	int w = getWidth();
	int h = getHeight();
	if(image_==null) {
	    image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    agent_layer_image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	} else if(image_.getWidth()<w || image_.getHeight()<h) {
	    image_ = new BufferedImage(Math.max(image_.getWidth(),w), Math.max(image_.getHeight(),h), BufferedImage.TYPE_INT_RGB);
	    agent_layer_image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	}
	draw((Graphics2D)image_.getGraphics(), w, h);
	repaint();
    }

    private void draw(Graphics2D g, int w, int h) {

	if(drawing_) {
	    cancel_to_draw_ = true;
	    return;
	}

	drawing_ = true;
	cancel_to_draw_ = false;
	long draw_start = System.currentTimeMillis();
	boolean show_id = show_node_ids_.isSelected();

	// clear screen
	g.setColor(getBackground());
	g.fillRect(0,0,w,h);

	TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);

	if(dragging_ || !antialiasing_on_.isSelected()) {
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	} else { // This operation is heavy.
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g.setStroke(area_connector_edge_stroke_);

	}

	// fill area
	if(!dragging_ && show_area_area_.isSelected()) {
	    TrafficArea[] area = world_manager_.getAreaList();
	    for(int i=0; i<area.length; i++) {
		if(area[i].isSimulateAsOpenSpace())
		    g.setColor(area_color_);
		else
		    g.setColor(Color.gray);
		fill(g, area[i]);
	    }
	    g.setColor(selected_color_);
	    for(int i=0; i<target_list.length; i++)
		if(target_list[i] instanceof TrafficArea)
		    fill(g, (TrafficArea)target_list[i]);
	}

	// fill blockade
	if(!dragging_ && show_area_area_.isSelected()) {
	    for(TrafficBlockade blockade : world_manager_.getBlockadeList()) {
		g.setColor(Color.black);
		fill(g, blockade);
	    }
	    /*
	    g.setColor(selected_color_);
	    for(int i=0; i<target_list.length; i++)
		if(target_list[i] instanceof TrafficArea)
		    fill(g, (TrafficArea)target_list[i]);
	    */
	}

	// drwa area connector edge
	if(show_area_connector_.isSelected()) {

	    g.setColor(area_connector_color_);

    	    TrafficArea[] area_list = world_manager_.getAreaList();
	    for(int i=0; i<area_list.length; i++) {
		TrafficArea area = area_list[i];
		TrafficAreaEdge[] connector_list = area.getConnectorEdgeList();
		for(int k=0; k<connector_list.length; k++)
		    draw(g, connector_list[k]);
	    }
	    g.setColor(Color.red);
	    for(int i=0; i<target_list.length; i++)
		if(target_list[i] instanceof TrafficAreaEdge)
		    draw(g, (TrafficAreaEdge)target_list[i]);
	    
	    /*
	    TrafficAreaEdge[] area_edge = world_manager_.getAreaConnectorEdgeList();
	    for(int i=0; i<area_edge.length; i++)
		draw(g, area_edge[i]);

	    g.setColor(Color.red);
	    for(int i=0; i<target_list.length; i++)
		if(target_list[i] instanceof TrafficAreaEdge)
		    draw(g, (TrafficAreaEdge)target_list[i]);
	    */
	}

	// draw area edge
	if(show_area_edge_.isSelected()) {
	    /*
	    TrafficArea[] area = world_manager_.getAreaList();
	    if(!dragging_)
		g.setStroke(area_edge_stroke_);
	    g.setColor(area_edge_color_);
	    for(int i=0; i<area.length; i++)
		draw(g, area[i]);
	    */
	    //if(!dragging_ && antialiasing_on_.isSelected())	    
	    //g.setStroke(area_edge_stroke_);
	    
	    g.setColor(area_edge_color_);
    	    TrafficArea[] area_list = world_manager_.getAreaList();
	    for(int i=0; i<area_list.length; i++) {
		TrafficArea area = area_list[i];
		
		Line2D[] line_list = area.getUnconnectedEdgeList();
		for(int j=0; j<line_list.length; j++) {
		    Point2D p1 = line_list[j].getP1();
		    Point2D p2 = line_list[j].getP2();
		    g.draw(new Line2D.Double(mx2sx(p1.getX()), my2sy(p1.getY()), mx2sx(p2.getX()), my2sy(p2.getY())));
		}
	    }
	    g.setColor(Color.red);
	    for(TrafficObject target : target_list)
		if(target instanceof TrafficAreaEdge)
		    draw(g, (TrafficAreaEdge)target);
	}
	
	
	if(show_area_node_.isSelected()) {
	    g.setColor(area_node_color_);
	    /*
	    TrafficArea[] area = world_manager_.getAreaList();
	    for(int i=0; i<area.length; i++) {
		TrafficAreaNode[] node = area[i].getNodeList();
		for(int j=0; j<node.length; j++)
		    draw(g, node[j], show_id);
	    }
	    */
	    TrafficAreaNode[] node_list = world_manager_.getAreaNodeList();
	    for(int i=0; i<node_list.length; i++) {
		draw(g, node_list[i], show_id);
	    }
	}

	updateAgentLayer();

	g.setColor(Color.black);

	long draw_end = System.currentTimeMillis();
	g.drawString("["+(draw_end-draw_start)+"]", 2, 10);

	drawing_ = false;

	if(cancel_to_draw_) { // if cancelled then it is required to recreate image
	    createImage();
	}
    }



    private BufferedImage agent_layer_image_buf_;

    public void updateAgentLayer() {
	if(agent_direct_rendering_mode_.isSelected()) return ;
	
	// int w = agent_layer_image_.getWidth();
	// int h = agent_layer_image_.getHeight();
	int w = getWidth();
	int h = getHeight();

	///if(agent_layer_image_buf_==null || w!= agent_layer_image_buf_.getWidth() || h!=agent_layer_image_buf_.getHeight()) {
	//agent_layer_image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    agent_layer_image_buf_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    //agent_layer_image_buf_.setRGB(0,0,w,h,agent_layer_buf_,0,w);
	    //}
	//agent_layer_image_.setRGB(0, 0, w, h, new int[w*h], 0, w);
	//agent_layer_image_buf_.setData(image_.getData());
	

	Graphics2D ag = (Graphics2D)agent_layer_image_buf_.getGraphics();
	drawAgentLayer(ag);
    }

    private void drawAgentLayer(Graphics2D ag) {

	ag.setColor(Color.green);
	boolean show_id = show_node_ids_.isSelected();
	TrafficAgent[] agent_list = world_manager_.getAgentList();
	for(int i=0; i<agent_list.length; i++)
	    draw(ag, agent_list[i], show_id, agent_list[i].getColor());

	TrafficObject[] target_list = target_list_.values().toArray(new TrafficObject[0]);
	for(int i=0; i<target_list.length; i++)
	    if(target_list[i] instanceof TrafficAgent) {
		TrafficAgent agent = (TrafficAgent)target_list[i];
		draw(ag, agent, false, Color.red);
		//TrafficAreaNode destination = agent.getNextDestination();
		TrafficAreaNode destination = agent.getFinalDestination();
		double x1 = mx2sx(agent.getX());
		double y1 = my2sy(agent.getY());
		ag.setColor(Color.blue);
		if(destination!=null) {
		    double x2 = mx2sx(destination.getX());
		    double y2 = my2sy(destination.getY());
		    ag.draw(new Line2D.Double(x1, y1, x2, y2));
		} else
		    ag.drawString("destination is not set.", (int)x1+10, (int)y1+10);
	    }

	ag.setColor(Color.black);
	// if(simulate_.isSelected())
	    ag.drawString("Simulation Time: "+simulation_time_+"[ms]", 30, 20);

	if(video_frame_counter_==0)
	    video_start_ = System.currentTimeMillis();

	long time = System.currentTimeMillis();
	ag.drawString("Real Time: "+time+"[ms] ("+(time-video_start_)+")", 30, 10);

	if(video_image_!=null && ((skip_counter++)%skip==0)) {
	    
	    Graphics vg = video_image_.getGraphics();
	    int vw = video_image_.getWidth();
	    int vh = video_image_.getHeight();
	    //vg.setColor(Color.white);
	    //vg.fillRect(0,0,vw,vh);
	    //vg.setColor(Color.blue);
	    //vg.drawString("test", 10, 100);
	    vg.drawImage(image_, 0, 0, null);
	    vg.drawImage(agent_layer_image_buf_, 0, 0, null);
	    stepREC();
	}
	
	agent_layer_image_ = agent_layer_image_buf_;
	//agent_layer_image_.setData(agent_layer_image_buf_.getData());
    }



    public void paintComponent(Graphics g) {
	if(image_!=null) {
	    g.drawImage(image_, 0, 0, this);
	    if(agent_direct_rendering_mode_.isSelected())
		drawAgentLayer((Graphics2D)g);
	    else
		g.drawImage(agent_layer_image_, 0, 0, this);
	    if(video_image_ != null) {
		g.setColor(Color.red);
		g.fillOval(10,20,10,10);
		g.drawString("REC", 20, 30);
		g.setColor(Color.black);
		g.drawString(video_frame_counter_+"/"+video_frame_limit_, 50, 30);
	    }
	} else {
	    int w = getWidth();
	    int h = getHeight();
	    g.setColor(getBackground());
	    g.fillRect(0,0,w,h);
	    g.setColor(getForeground());
	    g.drawString("Loading...", 1, 10);
	    createImageInOtherThread();
	}
    }

    public void draw(Graphics2D g, TrafficAreaEdge edge) {
	GeneralPath gp = (GeneralPath)(edge.getPath().clone());
	gp.transform(transform_);
	g.draw(gp);
    }
    public void draw(Graphics2D g, TrafficAreaNode node, boolean show_id) {
	double d = 2;
	//Shape arc = new Arc2D.Double(mx2sx(node.getX())-d, my2sy(node.getY())-d, d*2, d*2, 0, 360,Arc2D.OPEN);
	Shape arc = new Ellipse2D.Double(mx2sx(node.getX())-d, my2sy(node.getY())-d, d+d, d+d);
	g.fill(arc);
	if(show_id)
	    g.drawString(String.valueOf(node.getID()), (int)mx2sx(node.getX())+3, (int)my2sy(node.getY()));
    }
    public void fill(Graphics2D g, TrafficArea area) {
	GeneralPath gp = (GeneralPath)(area.getShape().clone());
	gp.transform(transform_);
	g.fill(gp);
    }
    public void draw(Graphics2D g, TrafficArea area) {
	GeneralPath gp = (GeneralPath)(area.getShape().clone());
	gp.transform(transform_);
	g.draw(gp);
    }
    public void fill(Graphics2D g, TrafficBlockade blockade) {
	GeneralPath gp = (GeneralPath)(blockade.getShape().clone());
	gp.transform(transform_);
	g.fill(gp);
    }
    public void draw(Graphics2D g, TrafficAgent agent, boolean show_id, Color color) {
	double d = Math.max(zoom_*agent.getRadius(), 2);
	double x = mx2sx(agent.getX());
	double y = my2sy(agent.getY());
	//Arc2D.Double arc = new Arc2D.Double(x-d, y-d, d*2, d*2, 0, 360,Arc2D.OPEN);
	Shape arc = new Ellipse2D.Double(x-d, y-d, d+d, d+d);
	g.setColor(color);
	g.fill(arc);
	g.setColor(Color.black);
	g.draw(arc);
	double x2 = mx2sx(agent.getX()+agent.getFX()*100000000.0);
	double y2 = my2sy(agent.getY()+agent.getFY()*100000000.0);
	double x3 = mx2sx(agent.getX()+agent.getVX()*10000.0);
	double y3 = my2sy(agent.getY()+agent.getVY()*10000.0);
	g.setColor(Color.black);
	g.draw(new Line2D.Double(x, y, x3, y3));
	g.setColor(Color.red);
	g.draw(new Line2D.Double(x, y, x2, y2));
	
	if(show_id)
	    g.drawString(agent.getID(), (int)x, (int)y);
    }



    private double sx2mx(double mx) { return (mx-offset_x_)/zoom_; }
    private double sy2my(double my) { return -(my-offset_y_)/zoom_; }
    private double mx2sx(double sx) { return sx*zoom_+offset_x_; }
    private double my2sy(double sy) { return -sy*zoom_+offset_y_; }
    private Point2D.Double s2m(Point2D.Double s) { s.setLocation(sx2mx(s.getX()), sy2my(s.getY())); return s; }
    private Point2D.Double m2s(Point2D.Double m) { m.setLocation(mx2sx(m.getX()), my2sy(m.getY())); return m; }



    public void showTargetsInformation() {
	showInformation(target_list_.values().toArray(new TrafficObject[0]));
    }
    public void showInformationByIDs(String... ids) {
	TrafficObject[] object_list = new TrafficObject[ids.length];
	for(int i=0; i<ids.length; i++)
	    object_list[i] = world_manager_.getTrafficObject(ids[i]);
	showInformation(object_list);
    }
    public void showInformation(final TrafficObject... object_list) {
	new Thread(new Runnable(){public void run(){
	    this_.requestFocus();
	    StringBuffer sb = new StringBuffer();
	    sb.append("<html>");
	    for(int i=0; i<object_list.length; i++)
		sb.append(object_list[i].toLongString()).append("<br/>");
	    sb.append("</html>");
	    JTextPane tp = new JTextPane();
	    tp.setContentType("text/html");
	    tp.setText(sb.toString());
	    tp.setBackground(getBackground());
	    JScrollPane sp = new JScrollPane(tp);
	    sp.setBorder(null);
	    sp.setPreferredSize(new Dimension(500, 300));
	    JOptionPane.showMessageDialog(this_, sp);
	}}, "show targets").start();
    }



    // Action
    public Action getExitAction() {
	AbstractAction action = new AbstractAction("Exit"){
	    public void actionPerformed(ActionEvent e) {
		log(">exit");
		System.exit(0);
	    }
	};
	action.putValue("MnemonicKey", KeyEvent.VK_E);
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	action.putValue("ShortDescription", "Exit this application");
	return action;
    }
    public Action getImportAction() {
	AbstractAction action = new AbstractAction("Import"){
	    public void actionPerformed(ActionEvent e) {
		try{
		    log(">import");
		    File file = IO.getOpenFile(this_, "GML file", "xml", "gml");
		    log("selected file: "+file.getAbsolutePath());
		    open(file);
		}catch(UserCancelException exc){
		    log("cancelled by user.");
		}catch(Exception exc){
		    alert(exc, "error");
		}
	    }
	};
	action.putValue("MnemonicKey", KeyEvent.VK_I);
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	action.putValue("ShortDescription", "<html>Import from file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
	return action;
    }

    public Action getExportAction() {
	AbstractAction action = new AbstractAction("Export"){
	    public void actionPerformed(ActionEvent e) {
		try{
		    log(">export");
		    File file = IO.getSaveFile(this_, "GML file", "xml", "gml");
		    log("selected file: "+file.getAbsolutePath());
		    save(file);
		}catch(UserCancelException exc){
		    log("cancelled by user.");
		}catch(Exception exc){
		    alert(exc, "error");
		}
	    }
	};
	action.putValue("MnemonicKey", KeyEvent.VK_E);
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	action.putValue("ShortDescription", "<html>Export to file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
	return action;
    }

    public Action getClearAllAction() {
	AbstractAction action = new AbstractAction("Clear"){
	    public void actionPerformed(ActionEvent e) {
		log(">clear");
		world_manager_.clear();
	    }
	};
	action.putValue("MnemonicKey", KeyEvent.VK_C);
	action.putValue("ShortDescription", "Clear world data.");
	return action;
    }

    public JComponent createGroupPanel() {
	JPanel content = new JPanel();
	String[] group_name_list = agent_group_list_.keySet().toArray(new String[0]);
	for(int i=0; i<group_name_list.length; i++) {
	    final String name = group_name_list[i];
	    JCheckBox check = new JCheckBox(name);
	    for(String tmp : selected_agent_group_list_)
		if(tmp.equals(name))
		    check.setSelected(true);
	    check.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e) {
			JCheckBox tmp_check = (JCheckBox)e.getSource();
			if(tmp_check.isSelected())
			    selected_agent_group_list_.add(name);
			else
			    for(String tmp : selected_agent_group_list_)
				if(tmp.equals(name))
				    selected_agent_group_list_.remove(name);
		    }
			    });
	    content.add(check);
	}
	//JScrollPane sp = new JScrollPane(content);
	//sp.setPreferredSize(new Dimension(600, 400));
	content.setPreferredSize(new Dimension(600, 400));
		    
	return content;
    }

    public Action getSelectAgentGroupAction() {
	AbstractAction action = new AbstractAction("Select Agent Group"){
	    public void actionPerformed(ActionEvent e) {
		try{
		    requestFocus();
		    final JFrame log_frame= new JFrame("Agent Group Manager");

		    //agent_group_list_;
		    //selected_agent_group_list_;
		    
		    JComponent content = createGroupPanel();
		    
		    final JPanel panel = new JPanel(new BorderLayout());
		    panel.setFocusable(true);
		    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		    final JPanel control_pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		    control_pane.add(new JButton(new AbstractAction("reset"){
			    public void actionPerformed(ActionEvent e) {
				agent_group_list_.clear();
				selected_agent_group_list_.clear();
				panel.removeAll();
				panel.add(createGroupPanel(), BorderLayout.CENTER);
				panel.add(control_pane, BorderLayout.SOUTH);
				panel.revalidate();
			    }
			}));
		    control_pane.add(new JButton(new AbstractAction("create grooup"){
			    public void actionPerformed(ActionEvent e) {

				String[] choice = new String[]{"type", "building", "selected agent"};
				String selection = (String)JOptionPane.showInputDialog(this_, "Select", "Select", JOptionPane.INFORMATION_MESSAGE, null, choice, choice[0]);
				String saname = null;
				if(selection==choice[2]) {
				    saname=JOptionPane.showInputDialog(this_, "input name");
				}

				for(TrafficAgent agent : world_manager_.getAgentList()) {
				    String name = null;

				    if(selection==choice[0]) name=agent.getType();
				    else if(selection==choice[1]) name=agent.getArea().getID();
				    else if(selection==choice[2]) {
					for(TrafficObject o : target_list_.values())
					    if(o == agent) name=saname;
					if(name==null) continue;
				    } else break;

				    ArrayList<TrafficAgent> tal = agent_group_list_.get(name);
				    if(tal==null) {
					tal = new ArrayList<TrafficAgent>();
					agent_group_list_.put(name, tal);
				    }
				    tal.add(agent);
				}
				panel.removeAll();
				panel.add(createGroupPanel(), BorderLayout.CENTER);
				panel.add(control_pane, BorderLayout.SOUTH);
				panel.revalidate();
			    }
			}));
		    control_pane.add(new JButton(new AbstractAction("close"){
			    public void actionPerformed(ActionEvent e) {
				log_frame.dispose();
			    }
			}));
		    panel.add(content, BorderLayout.CENTER);
		    panel.add(control_pane, BorderLayout.SOUTH);
		    log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    log_frame.setContentPane(panel);
		    log_frame.pack();
		    log_frame.setLocationRelativeTo(this_);
		    log_frame.setVisible(true);

		}catch(Exception exception){exception.printStackTrace();}
	    }
	};
	// action.putValue("MnemonicKey", KeyEvent.VK_C);
	// action.putValue("ShortDescription", "Clear world data.");
	return action;
    }


    private int video_frame_counter_ = 0;
    private File directory = new File("./TrafficSimulatorLogVideo").getAbsoluteFile();
    private String video_ext_ = "png";
    private int video_frame_limit_ = 10000;
    private long video_start_;

    public void switchREC() throws Exception {
	if(video_image_!=null) {
	    endREC();
	    return ;
	}
	if(!directory.exists()) directory.mkdir();
	log(directory);
	int w = getWidth();
	int h = getHeight();
	BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	//svbo_ = new SimpleVideoBufferedOutputStream(w, h, 10, file);
	video_image_ = image;
	video_frame_counter_ = -1;
	createImageInOtherThread();
    }
    private int skip = 50;
    private int skip_counter = 0;
    private void stepREC() {
	if(video_image_==null) return ;
	
	//svbo_.write(video_image_);
	File file = new File(directory, video_frame_counter_+"."+video_ext_);
	try{
	    javax.imageio.ImageIO.write(video_image_, video_ext_, file);
	}catch(Exception e){
	    e.printStackTrace();
	}
	video_frame_counter_++;
	if(video_frame_counter_>video_frame_limit_) endREC();
    }
    private void endREC() {
	video_image_ = null;
	//svbo_.close();
	try{
	    InputStream is = null;
	    File img_file = new File("data/img/handle.png");
	    if(img_file.exists())
		is = new FileInputStream(img_file);
	    else {
		URL img = ClassLoader.getSystemResource("data/img/handle.png");
		if(img != null)
		    is = img.openStream();
		else {
		    String traffic3 = ClassLoader.getSystemResource("traffic3/Main.class").toString();
		    if(traffic3.startsWith("jar:")) {
			int start = "jar:".length();
			int end = traffic3.indexOf("!");
			File parent = new File(new URL(traffic3.substring(start, end)).toURI()).getParentFile();
			is = new FileInputStream(new File(parent, "data/img/handle.png"));
		    }else
			throw new Exception("cannot find data/img/handle.png");
		}
	    }
	    FileOutputStream fos = new FileOutputStream(new File(directory, "handle.png"));
	    for(int i=is.read(); i!=-1; i=is.read())
		fos.write(i);
	    fos.flush();
	    fos.close();
	    is.close();
	}catch(Exception e){
	    alert(e, "error");
	}

	try{
	    StringBuffer sb = new StringBuffer();
	    //sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>Image</title><script type=\"text/javascript\"><!--\nvar counter = 0;var before = \""+"."+"/\";var after = \"."+video_ext_+"\";var wait = "+skip+";var max = "+video_frame_counter_+";function next() {target=document.getElementById(\"target\");target.setAttribute(\"src\", before+counter+after);counter = (counter + 1)%max;setTimeout(\"next()\", wait);}\n--></script></head><body onload=\"next();\" style=\"margin:0;\"><img id=\"target\" src=\"\" style=\"margin:auto;\"/></body></html>");
	    sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <style type=\"text/css\"> h1{     text-align:center; } h2{     text-align:left; }  div.author{     text-align:center; } div.name{     text-align:right; } div.affiliation{     text-align:right;     font-size:60%; }   div.fig{     text-align:center;     margin:30px auto 30px auto; }  math{     white-space: nowrap;     margin:20px; }  div.caption{     font: caption;     font-weight: bold;     font-size: 70%;     margin-top:10px;     text-align:center; }  .test{     margin:10px auto 10px auto; }  .source{     font-size: 70%;     text-align:left;     font-family: monospace;     background: white;     width:80%;     padding-left:1em;     margin-left:auto;     margin-right:auto;     border-style:dotted;     border-width:1px; }  .none{     border:none;     background: none;     margin:0;     padding:0; }   table{     border-style:solid;     border-spacing:0;     border-width:1px 0 0 1px;     text-align:center;     background:white;     margin-left:auto;     margin-right:auto; } th{     background:#444444;     color:white;     padding:3px 10px;     border-style:solid;     border-width: 0 1px 1px 0;     border-color:black; } td{     border-style:solid;     padding:0 10px;     border-width:0 1px 1px 0;     text-align:left; }  div.slide{     margin-bottom:100px;     margin-left:auto;     margin-right:auto; } .note{     margin:10px;     border:dotted 1px;     background:white;     font-size:0px;     visibility:hidden; }  div.slide_cover{     margin-bottom:100px; }  ul.i0{ 	list-style-image:url(./../img/item/darkball_blue24.png); } ul.i1{ 	list-style-image:url(./../img/item/darkball_red24.png); } ul.i2{ 	list-style-image:url(./../img/item/darkball_yellow24.png); } ul.i3{ 	list-style-image:url(./../img/item/darkball_pink24.png); } ul.i4{ 	list-style-image:url(./../img/item/darkball_black24.png); } ul.i5{ 	list-style-image:url(./../img/item/darkball_green24.png); }  a:link{ text-decoration:none } a:active{ text-decoration:none } a:visited{ text-decoration:none }  html{ 	width:100%; 	height:100%; 	margin:0; } body{ 	width:90%; 	height:100%; 	margin:0 auto 400px auto; 	background:#cccccc; } </style> <title>Image</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><script type=\"text/javascript\">/*<!--*/var is_playing = false;  var counter = 0;  var before = \"\"; var after = \"."+video_ext_+"\";  var wait = "+skip+"; var cmax = "+video_frame_counter_+"; function next() { if(counter>=cmax) { stop(); counter=0; }else{ counter = (counter + 1); } update(); if(is_playing) setTimeout(\"next()\", wait); }; function prev() { if(counter<=0) { counter=0; }else{ counter = counter-1; } update(); }; function update() { target=document.getElementById(\"target\"); target.setAttribute(\"src\", before+counter+after); document.getElementById(\"count_number\").innerHTML = counter; var point = parseInt(counter*100/cmax); document.getElementById(\"progress\").style.width=point+\"%\"; }; function play() { document.getElementById(\"play-button\").innerHTML=\"stop\"; is_playing=true; next(); }; function stop() { document.getElementById(\"play-button\").innerHTML=\"play\"; is_playing=false; }; function switchPlayStop() { if(is_playing) stop(); else play(); }; function draggedHandleTo(x, y) { var base = document.getElementById(\"progress-handle\"); var dx = x-down_x; /*alert(base.offsetLeft);*/ var dy = y-down_y; /*alert(down_x);*/ var pb = document.getElementById(\"progress-back\"); var width = parseInt(pb.offsetWidth); counter = parseInt(dx*cmax/width+down_counter); if(counter<0) counter=0; else if(counter>cmax) counter=cmax; update(); }; document.onmousedown = function(event) { return false; }; document.onmousemove = function(event) { return false; }; document.onmouseup = function(event) { down_flag=false; return false; }; var down_flag=false; var lastx=0; var lasty=0; var down_x,down_y,down_counter; function setFrame(frame) { counter=frame; update(); };//--></script> </head>   <body onload=\"setFrame(0);\" style=\"margin:auto;\" onmousemove=\"x=event.pageX;y=event.pageY;if(down_flag) { draggedHandleTo(x,y); };lastx=x;lasty=y;return true;\"> <div style=\"margin:100px;\"> <div class=\"test\" style=\"margin:0px;\"> <table id=\"video-player\" class=\"none\" style=\"border:solid 1px gray;\"> <tr>   <td class=\"none\">     <img id=\"target\" src=\"\"/>   </td> </tr> <tr>   <td class=\"none\">     <table class=\"none\">       <tr>       <td class=\"none\"> 	  <div style=\"width:10px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"prev();\">&lt;</div> 	</td> 	<td class=\"none\"> 	  <div id=\"play-button\" style=\"width:35px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"switchPlayStop();\">play</div> 	</td>       <td class=\"none\"> 	  <div style=\"width:10px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"next();\">&gt;</div> 	</td>	<td class=\"none\" style=\"width:100%;\"> 	  <div id=\"progress-back\" onmouseup=\"down_flag=false;\" onmouseexit=\"down_flag=false;\" style=\"height:4px;border:solid 1px gray;margin:5px;\"> 	    <div id=\"progress\" style=\"margin:-1;border:solid 1px gray;height:100%;width:0;background:red;\"> 	      <div id=\"progress-handle\" style=\"cursor:pointer;border:solid 0px;;height:10px;width:10px;margin:-7px 0px auto auto;\" onmousedown=\"down_flag=true; down_x=event.pageX; down_counter=counter;down_y=event.pageY;\"> 		<img src=\"handle.png\" width=\"18px\" height=\"18px\"/> 	      </div> 	    </div> 	  </div> 	</td> 	<td class=\"none\">       <div id=\"count_number\" style=\"width:30px;padding:2px;font-family:arial;font-size:13px;text-align:right;\">[ ]</div> 	</td>       </tr>     </table>   </td> </tr> </table> </div> </div> </body> </html>");

	    FileWriter fw = new FileWriter(new File(directory, "index.html"));
	    fw.write(sb.toString());
	    fw.flush();
	    fw.close();
	    alert("video recode successfully ended.");
	}catch(Exception e){
	    alert(e, "error");
	}
    }

    public Action getFitViewAction() {
	AbstractAction action = new AbstractAction("Fit View"){
	    public void actionPerformed(ActionEvent e) {
		log(">fit view");
		fitView();
	    }
	};
	action.putValue("MnemonicKey", KeyEvent.VK_V);
	action.putValue("ShortDescription", "Set view that all the object just can be seen .");
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke('f'));
	return action;
    }
    public void fitView() {
	Rectangle2D.Double view = world_manager_.calcRange();
	if(view != null)
	    setView(view);
    }
    public void setView(Rectangle2D view) {
	log("set view:"+view);
	double dwidth = (double)getWidth()/view.getWidth();
	double dheight = (double)getHeight()/view.getHeight();
	if(dwidth<dheight) {
	    zoom_ = dwidth*0.9;
	    offset_x_ = -view.getX()*zoom_+view.getWidth()*(dwidth-zoom_)*0.5;
	    offset_y_ = -view.getY()*zoom_+view.getHeight()*(dheight-zoom_)*0.5;
	    // offset_y_ = -view.getY()*zoom_ + view.getHeight()*(dheight-zoom_)*0.5+getHeight();
	} else {
	    zoom_ = dheight*0.9;
	    offset_x_ = -view.getX()*zoom_+view.getWidth()*(dwidth-zoom_)*0.5;
	    offset_y_ = -view.getY()*zoom_+view.getHeight()*(dheight-zoom_)*0.5;
	    // offset_y_ = -view.getY()*zoom_ + view.getHeight()*(dheight-zoom_)*0.5+getHeight();
	}
	offset_y_ = -offset_y_+getHeight();
	transform_.setTransform(zoom_, 0, 0, -zoom_, offset_x_, offset_y_);	
	createImageInOtherThread();
    }

    public Action getShowAllAction() {
	AbstractAction action = new AbstractAction("show all") {
		public void actionPerformed(ActionEvent e) {
		    try{
			requestFocus();
			final JFrame log_frame= new JFrame("Log");
			JTextPane ta = new JTextPane();
			ta.setContentType("text/html");
			KeyAdapter ka = new KeyAdapter(){
				public void keyPressed(KeyEvent e) {
				    if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					log_frame.dispose();
				}
			    };
			TrafficObject[] all = world_manager_.getAll();//.toArray(new TrafficObject[0]);
			TrafficAgent[] agent_list = world_manager_.getAgentList();
			TrafficArea[] area_list = world_manager_.getAreaList();
			StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append("<div style='font-size:120%;'>Information</div>");
			sb.append("<table>");
			sb.append("<tr><td>Objects</td><td>").append(all.length).append("</td></tr>");
			sb.append("<tr><td>Agents</td><td>").append(agent_list.length).append("</td></tr>");
			sb.append("<tr><td>Areas</td><td>").append(area_list.length).append("</td></tr>");
			sb.append("<table>");
			sb.append("</html>");
			ta.setText(sb.toString());

			
			JPanel panel = new JPanel(new BorderLayout());
			panel.setFocusable(true);
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JScrollPane sp = new JScrollPane(ta);
			sp.setPreferredSize(new Dimension(600, 400));
			JPanel control_pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			control_pane.add(new JButton(new AbstractAction("close"){
				public void actionPerformed(ActionEvent e) {
				    log_frame.dispose();
				}
			    }));

			panel.add(sp, BorderLayout.CENTER);
			panel.add(control_pane, BorderLayout.SOUTH);
			log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			log_frame.setContentPane(panel);
			log_frame.pack();
			log_frame.setLocationRelativeTo(this_);
			log_frame.setVisible(true);
			
			log_frame.addKeyListener(ka);
			ta.addKeyListener(ka);
			ta.requestFocus();
			ta.revalidate();
			
		    }catch(Exception exception){exception.printStackTrace();}
		}
	    };
	
	return action;
    }

    public Action getShowLogAction() {
	AbstractAction action = new AbstractAction("Log"){
		public void actionPerformed(ActionEvent e) {
		    //		    new Thread(new Runnable(){public void run(){
		    //SwingUtilities.invokeLater(new Runnable(){public void run(){
			try{
			    requestFocus();
			    final JFrame log_frame= new JFrame("Log");
			    JTextArea ta = new JTextArea();
			    KeyAdapter ka = new KeyAdapter(){
				    public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					    log_frame.dispose();
				    }
				};
			    ta.setText(traffic3.log.Logger.getLogAsText());
			    
			    JPanel panel = new JPanel(new BorderLayout());
			    panel.setFocusable(true);
			    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			    JScrollPane sp = new JScrollPane(ta);
			    sp.setPreferredSize(new Dimension(600, 400));
			    JPanel control_pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			    control_pane.add(new JButton(new AbstractAction("close"){
				    public void actionPerformed(ActionEvent e) {
					log_frame.dispose();
				    }
				}));
			    panel.add(sp, BorderLayout.CENTER);
			    panel.add(control_pane, BorderLayout.SOUTH);
			    log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			    log_frame.setContentPane(panel);
			    log_frame.pack();
			    log_frame.setLocationRelativeTo(this_);
			    log_frame.setVisible(true);
			    
			    log_frame.addKeyListener(ka);
			    ta.addKeyListener(ka);
			    ta.requestFocus();
			    ta.revalidate();

			}catch(Exception exception){exception.printStackTrace();}

			//}});
			//	    }}).start();
		}
	    };
	action.putValue("MnemonicKey", KeyEvent.VK_L);
	action.putValue("ShortDescription", "See all the log information.");
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	return action;
    }
    AbstractAction getshowtargetsaction_;
    public Action getShowTargetsAction() {
	if(getshowtargetsaction_==null)
	    getshowtargetsaction_ = new AbstractAction("Show Targets as text"){
		    // public boolean isEnabled() {
		    // return (target_list_.size()!=0);
		    //}
		public void actionPerformed(ActionEvent e) {
		    showTargetsInformation();
		}
	    };
	getshowtargetsaction_.putValue("AcceleratorKey", KeyStroke.getKeyStroke('i'));
	getshowtargetsaction_.putValue("ShortDescription", "Show targets as text.");
	return getshowtargetsaction_;
    }

    public Action getSelectByIDAction() {
	AbstractAction action = new AbstractAction("Add to selecttion by ID"){
		public void actionPerformed(ActionEvent e) {
		    try{
			String id = inputString("id");
			TrafficObject o = world_manager_.getTrafficObject(id);
			if(o!=null)
			    target_list_.put(o.getID(), o);
			else
			    alert("cannot find id ["+id+"]", "error");
			createImageInOtherThread();
		    }catch(Exception exc) {
			alert(exc, "error");
		    }
		}
	    };
	//action.putValue("MnemonicKey", KeyEvent.VK_M);
	//action.putValue("ShorDescription", "Select all");
	//action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	return action;
    }

    public Action getSelectAllAction(){
	AbstractAction action = new AbstractAction("Select all Area"){
		public void actionPerformed(ActionEvent e) {
		    for(TrafficArea area : world_manager_.getAreaList())
			target_list_.put(area.getID(), area);
		    createImageInOtherThread();
		}
	    };
	action.putValue("MnemonicKey", KeyEvent.VK_M);
	action.putValue("ShorDescription", "Select all");
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	return action;
    }

    public Action getChangeAgentLayerUpdateRate() {
	AbstractAction action = new AbstractAction("Change Update Rate"){
		public void actionPerformed(ActionEvent e) {
		    try{
			int value = inputInt("input update rate["+image_update_term_+"]");
			image_update_term_ = value;
		    }catch(Exception exc) {
			alert(exc);
		    }
		}
	    };
	action.putValue("MnemonicKey", KeyEvent.VK_M);
	action.putValue("ShorDescription", "Select all");
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
	return action;
    }
    
    public Action getVideoRecAction() {
	AbstractAction action = new AbstractAction("REC"){
		public void actionPerformed(ActionEvent e) {
		    new Thread(new Runnable(){public void run(){
			try{
			    switchREC();
			}catch(Exception exc){
			    alert(exc, "error");
			}
		    }},"video recoder").start();
		}
	    };
	action.putValue("AcceleratorKey", KeyStroke.getKeyStroke('r'));
	return action;
    }

    public void setStatus(String message) {
	status_label_.setText(message);
    } 

    public JMenuBar getMenuBar() {
	return menu_bar_;
    }

    public JComponent getStatusBar() {
	return status_label_;
    }

    public String inputString(Object message) throws Exception {
	return JOptionPane.showInputDialog(this, message);
    }

    public double inputDouble(Object message) throws Exception {
	return Double.parseDouble(inputString(message));
    }

    public int inputInt(Object message) throws Exception {
	return Integer.parseInt(inputString(message));
    }
    
    @SuppressWarnings("unchecked") private final static <E> java.util.List<E>_(Object list) {
	return (java.util.List<E>)list;
    }
}
