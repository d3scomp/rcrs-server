package traffic3.manager.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.net.URI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.BorderFactory;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.Icon;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Area;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerListener;
import traffic3.manager.WorldManagerEvent;
import traffic3.simulator.RCRSTrafficSimulator;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.io.Parser;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

/**
 *
 */
public class WorldManagerGUI extends JComponent {

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_DEFAULT = 30;

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_SLIDER_MIN = -100;

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_SLIDER_MAX = 100;

    /**
     *
     */
    public static final float STROKE_WIDTH_DEFAULT = 3f;

    /**
     *
     */
    public static final Color SELECTED_OBJECT_COLOR_DEFAULT = new Color(255, 100, 100);

    /**
     *
     */
    public static final Color AREA_CONNECTOR_COLOR_DEFAULT = new Color(255, 150, 150);

    /**
     *
     */
    public static final Color AREA_NODE_COLOR_DEFAULT = new Color(0, 0, 0);

    /**
     *
     */
    public static final Color AREA_EDGE_COLOR_DEFAULT = new Color(50, 50, 50);

    /**
     *
     */
    public static final Color AREA_COLOR_DEFAULT = new Color(255, 200, 200);

    /**
     *
     */
    public static final Color NETWORK_EDGE_COLOR_DEFAULT = new Color(100, 255, 100);

    /**
     *
     */
    public static final Color NETWORK_NODE_COLOR_DEFAULT = new Color(0, 100, 0);

    /**
     *
     */
    private static final int MOUSE_STATE_LABEL_FONT_SIZE_DEFAULT = 12;

    /**
     *
     */
    private static final int VIEW_ZOOM_DEFAULT = 40;

    private static final double AGENT_RADIUS_DEFAULT = 200;
    private static final double AGENT_VELOCITY_DEFAULT = 0.7;


    /**
     *
     */
    private WorldManagerGUI thisObject = this;

    /**
     *
     */
    private WorldManager worldManager;

    /**
     *
     */
    private JMenuBar menuBar;

    /**
     *
     */
    private BufferedImage offImage;

    /**
     *
     */
    private BufferedImage agentLayerImage;

    /**
     *
     */
    private BufferedImage recodeBufImage;

    /**
     *
     */
    private Map<String, ArrayList<TrafficAgent>> agentGroupList = new HashMap<String, ArrayList<TrafficAgent>>();


    /**
     *
     */
    private List<String> selectedAgentGroupList = new ArrayList<String>();

    /**
     *
     */
    private Map<String, TrafficObject> targetList = new HashMap<String, TrafficObject>();

    //private List<TrafficObject> destination_list_ = new ArrayList<TrafficObject>();

    /**
     *
     */
    private double viewZoom;

    /**
     *
     */
    private double viewOffsetX;

    /**
     *
     */
    private double viweOffsetY;

    /**
     *
     */
    private volatile boolean mouseDragging;

    /**
     *
     */
    private JCheckBox showNetworkCheckBox;

    /**
     *
     */
    private JCheckBox showAreaCheckBox;

    /**
     *
     */
    private JCheckBox showNodeOfAreaCheckBox;

    /**
     *
     */
    private JCheckBox showEdgeOfAreaCheckBox;

    /**
     *
     */
    private JCheckBox showConectorOfArea;

    /**
     *
     */
    private JCheckBox showIDOfNode;

    /**
     *
     */
    private JCheckBox isAntialiaseingOn;

    /**
     *
     */
    private JCheckBox isDetailedViewMode;



    /**
     *
     */
    private Color selectedObjectColor;

    /**
     *
     */
    private Color areaNodeColor;

    /**
     *
     */
    private Color areaEdgeColor;

    /**
     *
     */
    private Color areaColor;

    /**
     *
     */
    private Color networkEdgeColor;

    /**
     *
     */
    private Color networkNodeColor;

    /**
     *
     */
    private Color areaConnectorColor;



    /**
     *
     */
    private int pressedMousePressedButtonIndex;

    /**
     *
     */
    private Stroke areaEdgeStroke;

    /**
     *
     */
    private Stroke areaConnectorEdgeStroke;

    /**
     *
     */
    private Stroke networkEdgeStroke;

    /**
     *
     */
    private JRadioButton isSelectMode;

    /**
     *
     */
    private JRadioButton isInputAreaMode;

    /**
     *
     */
    private JRadioButton isInputNetworkMode;

    /**
     *
     */
    private JRadioButton isPutAgentMode;

    /**
     *
     */
    private JCheckBox isSimulating;

    /**
     *
     */
    private JLabel statusLabel;

    /**
     *
     */
    private JLabel mouseStatusLabel;

    // rendering option
    /**
     *
     */
    private volatile int imageUpdateTerm;

    /**
     *
     */
    private JSlider imageUpdateRateSlider;

    /**
     *
     */
    private JLabel imageUpdateRateLabel;

    /**
     *
     */
    private JCheckBox agentDirectRenderingMode;

    /**
     *
     */
    private long lastUpdateAgentLayerTime = -1;

    /**
     * Constructor.
     * @param worldManager WorldManager
     */
    public WorldManagerGUI(WorldManager worldManager) {
        imageUpdateTerm = IMAGE_UPDATE_RATE_DEFAULT;
        this.worldManager = worldManager;
        worldManager.addWorldManagerListener(new WorldManagerListener() {

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
            if (e.getSource() instanceof RCRSTrafficSimulator) {
                simulation_time_ = ((RCRSTrafficSimulator)e.getSource()).getTime();
                long updateAgentLayerTime = System.currentTimeMillis();
                if (imageUpdateTerm < 0) {
                    waitFor(-imageUpdateTerm);
                    updateAgentLayer();
                    repaint();
                    lastUpdateAgentLayerTime = updateAgentLayerTime;
                }
                else if ((updateAgentLayerTime - lastUpdateAgentLayerTime) > imageUpdateTerm) {
                    updateAgentLayer();
                    repaint();
                    lastUpdateAgentLayerTime = updateAgentLayerTime;
                }
            }
            else {
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

    private void waitFor(int time) {
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException exc) {
            exc.printStackTrace();
        }
    }

    /**
     *
     */
    public void createGUI() {

        Action createImageAction = new AbstractAction("create image action") {
                public void actionPerformed(ActionEvent e) {
                    createImageInOtherThread();
                }
            };
        showNetworkCheckBox = new JCheckBox("show network");
        showAreaCheckBox = new JCheckBox("show area");
        showNodeOfAreaCheckBox = new JCheckBox("show area node");
        showEdgeOfAreaCheckBox = new JCheckBox("show area edge");
        showConectorOfArea = new JCheckBox("show area connector");
        showIDOfNode = new JCheckBox("show node ids");
        isAntialiaseingOn = new JCheckBox("antialiasing");
        isSimulating = new JCheckBox("simulate");
        isDetailedViewMode = new JCheckBox("detailed rendering");
        agentDirectRenderingMode = new JCheckBox("direct rendering");
        imageUpdateRateSlider = new JSlider(IMAGE_UPDATE_RATE_SLIDER_MIN, IMAGE_UPDATE_RATE_SLIDER_MAX, IMAGE_UPDATE_RATE_DEFAULT);
        imageUpdateRateLabel = new JLabel("30");

        showNetworkCheckBox.setSelected(false);
        showAreaCheckBox.setSelected(true);
        showNodeOfAreaCheckBox.setSelected(false);
        showEdgeOfAreaCheckBox.setSelected(true);
        showConectorOfArea.setSelected(false);
        showIDOfNode.setSelected(false);
        isAntialiaseingOn.setSelected(false);
        isSimulating.setSelected(false);
        agentDirectRenderingMode.setSelected(true);
        isDetailedViewMode.setSelected(true);

        showNetworkCheckBox.addActionListener(createImageAction);
        showAreaCheckBox.addActionListener(createImageAction);
        showNodeOfAreaCheckBox.addActionListener(createImageAction);
        showEdgeOfAreaCheckBox.addActionListener(createImageAction);
        showConectorOfArea.addActionListener(createImageAction);
        showIDOfNode.addActionListener(createImageAction);
        isDetailedViewMode.addActionListener(createImageAction);
        isSimulating.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new Thread(new Runnable() {
                            public void run() {
                                if (isSimulating.isSelected()) {
                                    startSimulation();
                                }
                                else {
                                    stopSimulation();
                                }
                            }
                        }, "simulation thread").start();
                }
            });
        isAntialiaseingOn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createImageInOtherThread();
                }
            });
        imageUpdateRateSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    imageUpdateTerm = imageUpdateRateSlider.getValue();
                    imageUpdateRateLabel.setText(String.valueOf(imageUpdateTerm));
                }
            });
        //imageUpdateRateSlider.setPaintLabels(true);
        //imageUpdateRateSlider.setMajorTickSpacing(50);
        //imageUpdateRateSlider.setMinorTickSpacing(10);

        areaConnectorEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);
        areaEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);
        networkEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);

        selectedObjectColor = SELECTED_OBJECT_COLOR_DEFAULT;
        areaConnectorColor = AREA_CONNECTOR_COLOR_DEFAULT;
        areaNodeColor = AREA_NODE_COLOR_DEFAULT;
        areaEdgeColor = AREA_EDGE_COLOR_DEFAULT;
        areaColor = AREA_COLOR_DEFAULT;
        networkEdgeColor = NETWORK_EDGE_COLOR_DEFAULT;
        networkNodeColor = NETWORK_NODE_COLOR_DEFAULT;

        isSelectMode = new JRadioButton("select mode");
        isInputAreaMode = new JRadioButton("input area mode");
        isInputNetworkMode = new JRadioButton("input network mode");
        isPutAgentMode = new JRadioButton("put agent mode");
        ButtonGroup bg = new ButtonGroup();
        bg.add(isSelectMode);
        bg.add(isInputAreaMode);
        bg.add(isInputNetworkMode);
        bg.add(isPutAgentMode);
        isSelectMode.setSelected(true);

        createMenuBar();

        statusLabel = new JLabel("status");
        mouseStatusLabel = new JLabel("X, Y");
        mouseStatusLabel.setFont(new Font("Monospaced", Font.PLAIN, MOUSE_STATE_LABEL_FONT_SIZE_DEFAULT));

        setFocusable(true);
        viewZoom = VIEW_ZOOM_DEFAULT;
        viewOffsetX = 0;
        viweOffsetY = 0;
        transform_.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);

        addDragAndDropListener();
        addResizeListener();
        OrgMouseListener oml = new OrgMouseListener();
        addMouseListener(oml);
        addMouseMotionListener(oml);
        addMouseWheelListener(oml);
        addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) { // i, is already used in menubar.
                    switch(e.getKeyChar()) {
                    case 's': isSelectMode.setSelected(true); break;
                    case 'a': isInputAreaMode.setSelected(true); break;
                    case 'n': isInputNetworkMode.setSelected(true); break;
                    case 'p': isPutAgentMode.setSelected(true); break;
                    case 'l': isSimulating.doClick(); break;
                    default: System.out.println(e.getKeyCode());
                    }
                }
            });
        setFocusable(true);
    }

    private class OrgMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
        private int mouseLastX;
        private int mouseLastY;
        private boolean isSometihgSelected;
        public void mousePressed(MouseEvent e) {
            requestFocus();
            mouseDragging = true;
            pressedMousePressedButtonIndex = e.getButton();
            if (pressedMousePressedButtonIndex == 1) {

                if (isSelectMode.isSelected()) {

                    if (e.isControlDown()) {
                        try {
                            TrafficAreaNode tan = worldManager.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
                            if (selectedAgentGroupList.size() == 0) {
                                TrafficAgent[] agent = worldManager.getAgentList();
                                for (int i = 0; i < agent.length; i++) {
                                    agent[i].setDestination(tan);
                                }
                            }
                            else {
                                String[] seectedAgentGroupNameList = selectedAgentGroupList.toArray(new String[0]);
                                for (int j = 0; j < seectedAgentGroupNameList.length; j++) {
                                    List<TrafficAgent> agentList = agentGroupList.get(selectedAgentGroupList.get(j));
                                    for (TrafficAgent agent : agentList) {
                                        agent.setDestination(tan);
                                    }
                                }
                            }
                        }
                        catch (Exception exc) {
                            exc.printStackTrace();
                            alert(exc, "error");
			}
                    }
                    else {
                        isSometihgSelected = listAllHitsToTarget(sx2mx(e.getX()), sy2my(e.getY()), !e.isShiftDown());
                    }
                }
                else if (isInputAreaMode.isSelected()) {
                    alert(new UnsupportedOperationException(), "error");
                }
                else if (isInputNetworkMode.isSelected()) {
                    //alert(new UnsupportedOperationException(), "error");
                    try {        
                        TrafficAreaNode tan = worldManager.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
                    }
    catch (Exception exc) {
                        alert(exc, "error");
                    }
                }
            }
            else if (pressedMousePressedButtonIndex == MouseEvent.BUTTON3) {

                final double x = sx2mx(e.getX());
                final double y = sy2my(e.getY());
                final JPopupMenu popup = new JPopupMenu();
                mouseDragging = false;
                popup.add(getShowTargetsAction());

                popup.add(new AbstractAction("switch [open space simulation]<=>[network simulation]") {
                        public void actionPerformed(ActionEvent e) {
                            TrafficObject[] copyOfTargetList = createCopyOfTargetList();
                            for (TrafficObject o : copyOfTargetList) {
                                if (o instanceof TrafficArea) {
                                    throw new RuntimeException("not supported yet");
                                }
                            }
                            createImageInOtherThread();
                        }
                    });

                popup.add(new AbstractAction("put agent") {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() { public void run() {
                                try {
                                    int number = inputInt("How many agent do you want to put?");
                                    String groupName = inputString("Input group type.(If empty, new group will not be created.)");
                                    List<TrafficAgent> agentBuf = new ArrayList<TrafficAgent>();
				    double radius = AGENT_RADIUS_DEFAULT;
				    double velocity = AGENT_VELOCITY_DEFAULT;
                                    for (int i = 0; i < number; i++) {
                                        TrafficAgent agent = new TrafficAgent(worldManager, AGENT_RADIUS_DEFAULT, AGENT_VELOCITY_DEFAULT);
                                        agent.setType(groupName);
                                        agentBuf.add(agent);
                                        agent.setLocation(x, y, 0);
                                        worldManager.appendWithoutCheck(agent);
                                    }
                                    if (!isSimulating.isSelected()) {
                                        if (confirm("Simulation was not started.\n Do you want to start simulation now?")) {
                                            isSimulating.doClick();
					}
                                    }
                                }
    catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                            }}, "put agent").start();
                        }
                    });
                
                popup.add(new AbstractAction("set destination") {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() {public void run() {
                                try {
                                    TrafficAreaNode node = worldManager.createAreaNode(x, y, 0);
                                    for (TrafficAgent agent : worldManager.getAgentList()) {
                                        agent.setDestination(node);
                                    }
                                }
    catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                            }}, "set destination").start();
                        }
                    });
                
                
                popup.add(new AbstractAction("put blockade") {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() {public void run() {
                                
                                try {
                                    TrafficObject[] copyOfTargetList = createCopyOfTargetList();
                                    
                                    double width = inputDouble("input blockade width [0-100]%.");
                                    for (TrafficObject tobj : copyOfTargetList) {
                                        if (!(tobj instanceof TrafficArea)) {
                                            log("cannot put blockade to "+tobj);
                                            continue;
                                        }
                                        
                                        TrafficArea tarea = (TrafficArea)tobj;
                                        TrafficBlockade tblockade = new TrafficBlockade(worldManager, worldManager.getUniqueID("_"));
                                        TrafficAreaNode[] nodeList = tarea.getNodeList();
                                        //tblockade.setCenter(nodeList[0].getX(), nodeList[0].getY());
                                        
                                        double cx = tarea.getCenterX();
                                        double cy = tarea.getCenterY();
                                        int[] xy_list = new int[nodeList.length*2];
                                        int index = 0;
                                        for (TrafficAreaNode node : nodeList) {
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
                                        worldManager.appendWithoutCheck(tblockade);
                                        tarea.addBlockade(tblockade);
                                    }
                                    createImageInOtherThread();
                                }
    catch (Exception exc) {
                                    alert(exc, "error");
                                }
                            }}).start();
                            
                        }
                    });
                
                
                
        
                popup.add(new AbstractAction("put blockade2") {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() {public void run() {
                                try {
                                    TrafficObject[] copyOfTargetList = createCopyOfTargetList();
                                    
                                    //double width = inputDouble("input blockade width [0-100]%.");
                                    for (TrafficObject tobj : copyOfTargetList) {
                                        if (!(tobj instanceof TrafficArea)) {
                                            log("cannot put blockade to "+tobj);
                                            continue;
                                        }
                                        
                                        TrafficArea tarea = (TrafficArea)tobj;
                                        TrafficAreaNode[] nodeList = tarea.getNodeList();
                                        int length = nodeList.length;
                                        int[] xs = new int[length];
                                        int[] ys = new int[length];
                                        for (int i = 0; i < length; i++) {
                                            xs[i] = (int)nodeList[i].getX();
                                            ys[i] = (int)nodeList[i].getY();
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
                                        for (int j = 0; j < hcount; j++)
                                            for (int i = 0; i < wcount; i++) {
                                                sub_area = new Area(new Rectangle2D.Double(x+i*w, y+j*h, w, h));
                                                sub_area.intersect(area);
                                                PathIterator path_iterator = sub_area.getPathIterator(new AffineTransform(1, 0, 0, 1, 0, 0));
                                                ArrayList<Point2D> point_list = new ArrayList<Point2D>();
                                                double[] xyd_list = new double[6];
                                                for (; !path_iterator.isDone(); path_iterator.next()) {
                                                    int type = path_iterator.currentSegment(xyd_list);
                                                    point_list.add(new Point2D.Double(xyd_list[0], xyd_list[1]));
                                                }

                                                if (point_list.size()  ==  0) continue;
                                                
                                                TrafficBlockade tblockade = new TrafficBlockade(worldManager, worldManager.getUniqueID("_"));
                                                
                                                tblockade.setCenter(nodeList[0].getX(), nodeList[0].getY());
                                                int[] xy_list = new int[point_list.size()*2];
                                                int index = 0;
                                                double cx = point_list.get(0).getX();
                                                double cy = point_list.get(1).getY();
                                                double xsum = 0;
                                                double ysum = 0;
                                                for (Point2D p : point_list) {
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
                                                worldManager.appendWithoutCheck(tblockade);
                                                tarea.addBlockade(tblockade);
                                            }
                                    }
                                    createImageInOtherThread();
                                }
    catch (Exception exc) {
                                    alert(exc, "error");
                                }
                            }}).start();
                        }
                    });
                
                
                
                
                popup.add(new AbstractAction("set area type") {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() {public void run() {
                                try {
                                    String area_type = inputString("Input area type.");
                                    TrafficObject[] copyOfTargetList = createCopyOfTargetList();
                                    for (TrafficObject o : copyOfTargetList)
                                        if (o instanceof TrafficArea) {
                                            TrafficArea a = (TrafficArea)o;
                                            a.setType(area_type);
                                        }
                                    createImageInOtherThread();
                                }
    catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                            }}).start();
                        }
                    });
                popup.show(thisObject, e.getX(), e.getY());
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            isSometihgSelected=false;
            mouseDragging=false;
            pressedMousePressedButtonIndex = -1;
            createImageInOtherThread();
        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {
            mouseLastX = e.getX();
            mouseLastY = e.getY();
            double x = sx2mx(mouseLastX);
            double y = sy2my(mouseLastY);
            setMouseStatus("X:"+toNaturalString((int)x)+"[mm] Y:"+toNaturalString((int)y)+"[mm]");
        }
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (pressedMousePressedButtonIndex == 1 && !isSometihgSelected && isSelectMode.isSelected())
                drag(x-mouseLastX, y-mouseLastY);
            mouseLastX = x;
            mouseLastY = y;
        }
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoom((e.getWheelRotation() < 0 ? 1.1 : 0.9), e.getX(), e.getY());
        }
    }
    
    private String toNaturalString(int value) {
        boolean minus = (value <= 0);
        if (minus) value = -value;
        String buf = String.valueOf(value);
        StringBuffer result = new StringBuffer();
        if (minus)
            result.append("-");
        int length = buf.length();
        int part = length%3;
        int p = 0;
        if (part!=0) {
        result.append(buf.substring(p, p+part));
        p += part;
        if (p < length)
            result.append(",");
        }
        while(p < length) {
            result.append(buf.substring(p, p+3));
            p += 3;
            if (p < length)
                result.append(",");
        }
        return result.toString();
    }

    private double simulation_time_ = -1;
    private long simulation_start_wallclock_time_ = -1;
    private volatile boolean is_simulation_running_;
    public void startSimulation() {
    double dt = 100;
    if (is_simulation_running_) return;
    is_simulation_running_ = true;

    ArrayList<Runnable> end_task_list = new ArrayList<Runnable>();

    /*
    try { // monitor 19768
        String id = "19768";
        final BufferedWriter bw = new BufferedWriter(new FileWriter("id"+id+"_enter_monitor.log"));
        bw.write("enter monitor of "+id);
        bw.newLine();
        bw.write("Simulation Time, WallclockTime");
        bw.newLine();
        bw.newLine();
        final TrafficArea area = (TrafficArea)worldManager.getTrafficObject(id); 
        final TrafficAreaListener listener = new TrafficAreaListener() {
            public void entered(TrafficAreaEvent e) {try {
            bw.write(String.valueOf(simulation_time_));
            bw.write(", "+(System.currentTimeMillis()-simulation_start_wallclock_time_));
            bw.write(", "+e.getAgent().getID());
            bw.write(", "+e.getAgent().getLogDistance());
            bw.newLine();
            bw.flush();
            }
    catch (Exception exc) {alert(exc, "error");}}
            public void exited(TrafficAreaEvent e) {  }
        };
        area.addTrafficAreaListener(listener);
        end_task_list.add(new Runnable() {
            public void run() {
            try {
                bw.flush();
                bw.close();
            }
    catch (Exception e) {
                e.printStackTrace();
            }
            area.removeTrafficAreaListener(listener);
            }
        });
    }
    catch (Exception exc) { log(exc, "error"); }
    try { // monitor 19768
        String id = "2087";
        final BufferedWriter bw = new BufferedWriter(new FileWriter("id"+id+"_enter_monitor.log"));
        bw.write("enter monitor of "+id);
        bw.newLine();
        bw.write("Simulation Time, WallclockTime");
        bw.newLine();
        bw.newLine();
        final TrafficArea area = (TrafficArea)worldManager.getTrafficObject("2087"); 
        final TrafficAreaListener listener = new TrafficAreaListener() {
            public void entered(TrafficAreaEvent e) {try {
            bw.write(String.valueOf(simulation_time_));
            bw.write(", "+(System.currentTimeMillis()-simulation_start_wallclock_time_));
            bw.write(", "+e.getAgent().getID());
            bw.write(", "+e.getAgent().getLogDistance());
            bw.newLine();
            bw.flush();
            }
    catch (Exception exc) {alert(exc, "error");}}
            public void exited(TrafficAreaEvent e) {  }
        };
        area.addTrafficAreaListener(listener);
        end_task_list.add(new Runnable() {
            public void run() {
            try {
                bw.flush();
                bw.close();
            }
    catch (Exception e) {
                e.printStackTrace();
            }
            area.removeTrafficAreaListener(listener);
            }
        });
    }
    catch (Exception exc) { log(exc, "error"); }
    */







    try {
        TrafficAgent[] agent_list = worldManager.getAgentList();
        for (int i = 0; i < agent_list.length; i++) {
            agent_list[i].clearLogDistance();
        }
        traffic3.simulator.Simulator simulator = new traffic3.simulator.Simulator(worldManager, dt);
        simulation_start_wallclock_time_ = System.currentTimeMillis();
        while(is_simulation_running_) {
            simulator.step();
            simulation_time_ = simulator.getTime();
            long updateAgentLayerTime = System.currentTimeMillis();
            if (imageUpdateTerm < 0) {
                try {Thread.sleep(-imageUpdateTerm);
		}
		catch (Exception exc) {exc.printStackTrace();}
		updateAgentLayer();
		repaint();
		lastUpdateAgentLayerTime = updateAgentLayerTime;
	    } else if ((updateAgentLayerTime-lastUpdateAgentLayerTime) > imageUpdateTerm) {
		updateAgentLayer();
		repaint();
		lastUpdateAgentLayerTime = updateAgentLayerTime;
	    }
	    // try {Thread.sleep(1000);}
	}
    }
    catch (Exception e) {
        alert(e, "error");
    }finally{
        for (Runnable end_task : end_task_list) {
            end_task.run();
        }
    }
    
    }
    public void stopSimulation() {
    is_simulation_running_ = false;
    }
    

    private boolean listAllHitsToTarget(double mx, double my, boolean clear) {
    boolean is_added_ = false;
    if (clear)
        targetList.clear();

    TrafficAgent[] agent_list = worldManager.getAgentList();
    for (int i = 0; i < agent_list.length; i++) {
        TrafficAgent agent = agent_list[i];
        double dx = agent.getX()-mx;
        double dy = agent.getY()-my;
        if (agent.getRadius()*agent.getRadius() > dx*dx+dy*dy) {
        targetList.put(agent.getID(), agent);
        is_added_ = true;
        }
    }

    if (!is_added_ && showNodeOfAreaCheckBox.isSelected()) {
        TrafficAreaNode node = worldManager.getNearlestAreaNode(mx,my,0);
        if (node!=null) {
        double d = 3.0/viewZoom;
        System.out.println(node.getDistance(mx,my,0));
        if (node.getDistance(mx,my,0) < d) {
            targetList.put(node.getID(), node);
            is_added_ = true;
        }
        }
    }

    if (!is_added_ && showEdgeOfAreaCheckBox.isSelected()) {
        TrafficAreaEdge[] area_edge = worldManager.getAreaConnectorEdgeList();
        for (int i = 0; i < area_edge.length; i++) {
        TrafficAreaEdge edge = area_edge[i];
        if (area_edge[i].distance(mx, my) < 2/viewZoom) {
            targetList.put(area_edge[i].getID(), area_edge[i]);
            is_added_ = true;
        }
        }
    }

    if (!is_added_ && showAreaCheckBox.isSelected()) {
        TrafficArea[] area = worldManager.getAreaList();
        for (int i = 0; i < area.length; i++)
        if (area[i].getShape().contains(mx, my)) {
            targetList.put(area[i].getID(), area[i]);
            is_added_ = true;
        }
    }

    if (targetList.size() == 0)
        log("clear target");
    else {
        StringBuffer sb = new StringBuffer("setTarget[");
        sb.append(targetList.toString());
        sb.append("]");
        log(sb);
    }
    //getShowTargetsAction().setEnabled(targetList.size()!=0);
    return is_added_;
    }

    private void zoom(double dzoom, double x, double y) {
    viewOffsetX = (viewOffsetX-x)*dzoom + x;
    viweOffsetY = (viweOffsetY-y)*dzoom + y;
    viewZoom *= dzoom;
    transform_.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
    createImageInOtherThread();
    }

    private void drag(double dx, double dy) {
    viewOffsetX += dx;
    viweOffsetY += dy;
    transform_.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
    createImageInOtherThread();
    }

    private void addDragAndDropListener() {
    setTransferHandler(new TransferHandler(null) {
        public boolean canImport(TransferHandler.TransferSupport support) {
            Transferable trans = support.getTransferable();
            if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return true;
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) return true;
            return false;
        }
        public boolean importData(TransferHandler.TransferSupport support) {
            Transferable trans = support.getTransferable();
            if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                java.util.List<File> data = _(trans.getTransferData(DataFlavor.javaFileListFlavor));
                for (Iterator<File> it=data.iterator(); it.hasNext(); ) {
                File file = it.next();
                open(file);
                }
            }
    catch (Exception e) {
                alert(e, "error");
            }
            return true;
            }
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String data = (String)trans.getTransferData(DataFlavor.stringFlavor);
                String[] data_list = data.split("\r\n");
                for (int i = 0; i < data_list.length; i++)
                open(new URI(data_list[i]));
            }
    catch (Exception e) {
                alert(e, "error");
            }
            return true;
            }
            return false;
        }
        });
    }

    public void addResizeListener() {
    addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            createImageInOtherThread();
        }
        });
    }

    private void createMenuBar() {
    menuBar = new JMenuBar();
    
    JMenu file_menu = new JMenu("File");
    file_menu.setMnemonic(KeyEvent.VK_F);

    file_menu.add(new JMenuItem(getImportAction()));
    file_menu.add(new JMenuItem(getExportAction()));
    file_menu.add(new JMenuItem(getClearAllAction()));
    file_menu.add(new JMenuItem(getClearAgentAction()));
    file_menu.addSeparator();
    file_menu.add(new JMenuItem(getExitAction()));

    JMenu view_menu = new JMenu("View");
    view_menu.setMnemonic(KeyEvent.VK_V);
    view_menu.add(new JMenuItem(getFitViewAction()));
    view_menu.addSeparator();
    view_menu.add(showNetworkCheckBox);
    view_menu.add(showAreaCheckBox);
    view_menu.add(showNodeOfAreaCheckBox);
    view_menu.add(showEdgeOfAreaCheckBox);
    view_menu.add(showConectorOfArea);
    view_menu.add(showIDOfNode);
    view_menu.add(isAntialiaseingOn);
    view_menu.addSeparator();
    view_menu.add(getShowTargetsAction());
    view_menu.add(getShowAllAction());
    view_menu.add(getChangeAgentLayerUpdateRate());
    view_menu.add(imageUpdateRateSlider);
    view_menu.add(agentDirectRenderingMode);
    view_menu.add(isDetailedViewMode);
    
    JMenu edit_menu = new JMenu("Edit");
    edit_menu.setMnemonic(KeyEvent.VK_E);
    edit_menu.add(isSelectMode);
    edit_menu.add(isInputAreaMode);
    edit_menu.add(isInputNetworkMode);
    edit_menu.add(isPutAgentMode);
    edit_menu.add(new JMenuItem(getSelectAllAction()));
    edit_menu.add(new JMenuItem(getSelectByIDAction()));
    edit_menu.add(new JMenuItem(getSelectAgentGroupAction()));

    JMenu devel_menu = new JMenu("Debug");
    devel_menu.setMnemonic(KeyEvent.VK_D);

    devel_menu.add(isSimulating);
    devel_menu.add(new JMenuItem(getShowLogAction()));
    devel_menu.add(new JMenuItem(new AbstractAction("Version") {
        public void actionPerformed(ActionEvent e) {
            String message = "<html><div style='font-size:120%;'>"+traffic3.Main.getVersion()+"</div></html>";
            JOptionPane.showMessageDialog(thisObject, message);
        }
        }));
    devel_menu.add(new JMenuItem(getVideoRecAction()));
    
    menuBar.add(file_menu);
    menuBar.add(view_menu);
    menuBar.add(edit_menu);
    menuBar.add(devel_menu);
    }


    public void open(URI uri) throws Exception {
    if (uri.getScheme().equals("file"))
        open(new File(uri));
    else {
        throw new Exception("open url unsupported.");
        //worldManager.open(uri.toURL().openStream());
        //fitView();
        //alert("Successfuly imported.\n\tThere are "+worldManager.getAll().length+" objects.", "information");
    }
    }
    public void open(final File file) throws Exception {
    final boolean[] finished = new boolean[1];
    new Thread(new Runnable() {public void run() {
        try {
        alert("importing: "+file.getAbsolutePath());
        worldManager.open(file);
        fitView();
        alert("Successfuly imported.\n\tThere are "+worldManager.getAll().length+" objects.", "information");
        }
    catch (Exception e) {
        e.printStackTrace();
        }finally{
        finished[0] = true;
        }
    }}, "import from file").start();
    new Thread(new Runnable() {public void run() {
        String[] sample = new String[]{
        "import:<",
        "import: ^",
        "import:  >",
        "import: v",
        };
        for (int i = 0; !finished[0]; i++) {
        setStatus(String.valueOf(sample[i%sample.length]));
        try {Thread.sleep(100);}
    catch (Exception e) {}
        }
    }}, "notify running thread").start();
    }
        
    public Parser selectParser() throws Exception {
    Parser[] parser_list = worldManager.getParserList();
    String message = "select export type";
    String title = "select";
    int type = JOptionPane.INFORMATION_MESSAGE;
    Icon icon = null;
    Object[] choice = parser_list;
    Object selection = JOptionPane.showInputDialog(this, message, title, type, icon, choice, choice[0]);
    return (Parser)selection;
    }

    public void save() {
    final boolean[] finished = new boolean[1];
    new Thread(new Runnable() {public void run() {
        try {
        log(">export");
        Parser parser = selectParser();
        File file = IO.getSaveFile(thisObject, "GML file", "xml", "gml");
        log("selected file: "+file.getAbsolutePath());
        save(file, parser);
        }
    catch (UserCancelException exc) {
        log("cancelled by user.");
        }
    catch (Exception exc) {
        alert(exc, "error");
        }finally{
        finished[0] = true;
        }
    }}, "export to file").start();
    new Thread(new Runnable() {public void run() {
        String[] sample = new String[]{
        "import:<",
        "import: ^",
        "import:  >",
        "import: v",
        };
        for (int i = 0; !finished[0]; i++) {
        setStatus(String.valueOf(sample[i%sample.length]));
        try {Thread.sleep(100);}
    catch (Exception e) {}
        }
    }}, "notify running thread").start();
    }

    public void save(File file, Parser parser) throws Exception {
    worldManager.save(file, parser);
    alert("Successfuly outputed.");
    }

    private Thread create_image_thread_ = null;
    private final Object key = new Object();
    public void createImageInOtherThread() {
    if (create_image_thread_  ==  null) {
        create_image_thread_ = new Thread(new Runnable() {public void run() {
        while(true) {
            createImage();
            try {
            synchronized(key) {
                key.wait();
            }
            }
    catch (Exception e) {e.printStackTrace();}
        }
        }}, "create image");
        create_image_thread_.start();
    } else {
        try {
        synchronized(key) {
            key.notifyAll();
        }
        }
    catch (Exception e) {e.printStackTrace();}
    }
    }

    private volatile boolean drawing_ = false;
    private volatile boolean cancel_to_draw_ = false;
    private volatile AffineTransform transform_ = new AffineTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
    private volatile boolean non_stop_creating_image_thread_is_running_ = false;

    private void createImage() {
    int w = getWidth();
    int h = getHeight();
    if (offImage == null) {
        offImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        agentLayerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    } else if (offImage.getWidth() < w || offImage.getHeight() < h) {
        offImage = new BufferedImage(Math.max(offImage.getWidth(),w), Math.max(offImage.getHeight(),h), BufferedImage.TYPE_INT_RGB);
        agentLayerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
    draw((Graphics2D)offImage.getGraphics(), w, h);
    repaint();
    }

    public TrafficObject[] createCopyOfTargetList() {
	return targetList.values().toArray(new TrafficObject[0]);
    }


    private void draw(Graphics2D g, int w, int h) {

    if (drawing_) {
        cancel_to_draw_ = true;
        return;
    }

    drawing_ = true;
    cancel_to_draw_ = false;
    long draw_start = System.currentTimeMillis();
    boolean show_id = showIDOfNode.isSelected();

    // clear screen
    g.setColor(getBackground());
    g.fillRect(0,0,w,h);

    TrafficObject[] copyOfTargetList = createCopyOfTargetList();

    if (mouseDragging || !isAntialiaseingOn.isSelected()) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    } else { // This operation is heavy.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(areaConnectorEdgeStroke);

    }

    // fill area
    if (!mouseDragging && showAreaCheckBox.isSelected()) {
        TrafficArea[] area = worldManager.getAreaList();
        for (int i = 0; i < area.length; i++) {
        if (area[i].isSimulateAsOpenSpace())
            g.setColor(areaColor);
        else
            g.setColor(Color.gray);
        fill(g, area[i]);
        }
        g.setColor(selectedObjectColor);
        for (int i = 0; i < copyOfTargetList.length; i++)
        if (copyOfTargetList[i] instanceof TrafficArea)
            fill(g, (TrafficArea)copyOfTargetList[i]);
    }

    // fill blockade
    if (!mouseDragging && showAreaCheckBox.isSelected()) {
        for (TrafficBlockade blockade : worldManager.getBlockadeList()) {
        g.setColor(Color.black);
        fill(g, blockade);
        }
        /*
        g.setColor(selectedObjectColor);
        for (int i = 0; i < copyOfTargetList.length; i++)
        if (copyOfTargetList[i] instanceof TrafficArea)
            fill(g, (TrafficArea)copyOfTargetList[i]);
        */
    }

    // drwa area connector edge
    if (showConectorOfArea.isSelected()) {

        g.setColor(areaConnectorColor);

            TrafficArea[] area_list = worldManager.getAreaList();
        for (int i = 0; i < area_list.length; i++) {
        TrafficArea area = area_list[i];
        TrafficAreaEdge[] connector_list = area.getConnectorEdgeList();
        for (int k=0; k < connector_list.length; k++)
            draw(g, connector_list[k]);
        }
        g.setColor(Color.red);
        for (int i = 0; i < copyOfTargetList.length; i++)
        if (copyOfTargetList[i] instanceof TrafficAreaEdge)
            draw(g, (TrafficAreaEdge)copyOfTargetList[i]);
        
        /*
        TrafficAreaEdge[] area_edge = worldManager.getAreaConnectorEdgeList();
        for (int i = 0; i < area_edge.length; i++)
        draw(g, area_edge[i]);

        g.setColor(Color.red);
        for (int i = 0; i < copyOfTargetList.length; i++)
        if (copyOfTargetList[i] instanceof TrafficAreaEdge)
            draw(g, (TrafficAreaEdge)copyOfTargetList[i]);
        */
    }

    // draw area edge
    if (showEdgeOfAreaCheckBox.isSelected()) {
        /*
        TrafficArea[] area = worldManager.getAreaList();
        if (!mouseDragging)
        g.setStroke(areaEdgeStroke);
        g.setColor(areaEdgeColor);
        for (int i = 0; i < area.length; i++)
        draw(g, area[i]);
        */
        //if (!mouseDragging && isAntialiaseingOn.isSelected())        
        //g.setStroke(areaEdgeStroke);
        
        g.setColor(areaEdgeColor);
            TrafficArea[] area_list = worldManager.getAreaList();
        for (int i = 0; i < area_list.length; i++) {
        TrafficArea area = area_list[i];
        
        Line2D[] line_list = area.getUnconnectedEdgeList();
        for (int j = 0; j < line_list.length; j++) {
            Point2D p1 = line_list[j].getP1();
            Point2D p2 = line_list[j].getP2();
            g.draw(new Line2D.Double(mx2sx(p1.getX()), my2sy(p1.getY()), mx2sx(p2.getX()), my2sy(p2.getY())));
        }
        }
        g.setColor(Color.red);
        for (TrafficObject target : copyOfTargetList)
        if (target instanceof TrafficAreaEdge)
            draw(g, (TrafficAreaEdge)target);
    }
    
    
    if (showNodeOfAreaCheckBox.isSelected()) {
        g.setColor(areaNodeColor);
        /*
        TrafficArea[] area = worldManager.getAreaList();
        for (int i = 0; i < area.length; i++) {
        TrafficAreaNode[] node = area[i].getNodeList();
        for (int j = 0; j < node.length; j++)
            draw(g, node[j], show_id);
        }
        */
        TrafficAreaNode[] nodeList = worldManager.getAreaNodeList();
        for (int i = 0; i < nodeList.length; i++) {
        draw(g, nodeList[i], show_id);
        }
    }

    updateAgentLayer();

    g.setColor(Color.black);

    long draw_end = System.currentTimeMillis();
    g.drawString("["+(draw_end-draw_start)+"]", 2, 10);

    drawing_ = false;

    if (cancel_to_draw_) { // if cancelled then it is required to recreate image
        createImage();
    }
    }



    private BufferedImage agent_layer_image_buf_;

    public void updateAgentLayer() {
    if (agentDirectRenderingMode.isSelected()) return ;
    
    // int w = agentLayerImage.getWidth();
    // int h = agentLayerImage.getHeight();
    int w = getWidth();
    int h = getHeight();

    ///if (agent_layer_image_buf_ == null || w!= agent_layer_image_buf_.getWidth() || h!=agent_layer_image_buf_.getHeight()) {
    //agentLayerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        agent_layer_image_buf_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        //agent_layer_image_buf_.setRGB(0,0,w,h,agent_layer_buf_,0,w);
        //}
    //agentLayerImage.setRGB(0, 0, w, h, new int[w*h], 0, w);
    //agent_layer_image_buf_.setData(image_.getData());
    

    Graphics2D ag = (Graphics2D)agent_layer_image_buf_.getGraphics();
    drawAgentLayer(ag);
    }

    private void drawAgentLayer(Graphics2D ag) {

    ag.setColor(Color.green);
    boolean show_id = showIDOfNode.isSelected();
    TrafficAgent[] agent_list = worldManager.getAgentList();
    if (isDetailedViewMode.isSelected()) {
        for (int i = 0; i < agent_list.length; i++)
        draw2(ag, agent_list[i], show_id, agent_list[i].getColor());
    } else {
        for (int i = 0; i < agent_list.length; i++)
        draw(ag, agent_list[i], show_id, agent_list[i].getColor());
    }
        

    TrafficObject[] copyOfTargetList = createCopyOfTargetList();
    for (int i = 0; i < copyOfTargetList.length; i++)
        if (copyOfTargetList[i] instanceof TrafficAgent) {
        TrafficAgent agent = (TrafficAgent)copyOfTargetList[i];
        draw2(ag, agent, false, Color.red);
        //TrafficAreaNode destination = agent.getNextDestination();
        TrafficAreaNode fdestination = agent.getFinalDestination();
        TrafficAreaNode ndestination = agent.getNowDestination();
        double x1 = mx2sx(agent.getX());
        double y1 = my2sy(agent.getY());
        ag.setColor(Color.blue);
        if (fdestination!=null) {
            double x2 = mx2sx(fdestination.getX());
            double y2 = my2sy(fdestination.getY());
            ag.draw(new Line2D.Double(x1, y1, x2, y2));
        }
        ag.setColor(Color.green);
        if (ndestination!=null) {
            double x2 = mx2sx(ndestination.getX());
            double y2 = my2sy(ndestination.getY());
            ag.draw(new Line2D.Double(x1, y1, x2, y2));
        }
        }

    ag.setColor(Color.black);
    // if (isSimulating.isSelected())
        ag.drawString("Simulation Time: "+simulation_time_+"[ms]", 30, 20);

    if (video_frame_counter_ == 0)
        video_start_ = System.currentTimeMillis();

    long time = System.currentTimeMillis();
    ag.drawString("Real Time: "+time+"[ms] ("+(time-video_start_)+")", 30, 10);

    if (recodeBufImage!=null && (skip == 0 || ((skip_counter++)%skip == 0))) {
        
        Graphics vg = recodeBufImage.getGraphics();
        int vw = recodeBufImage.getWidth();
        int vh = recodeBufImage.getHeight();
        //vg.setColor(Color.white);
        //vg.fillRect(0,0,vw,vh);
        //vg.setColor(Color.blue);
        //vg.drawString("test", 10, 100);
        vg.drawImage(offImage, 0, 0, null);
        vg.drawImage(agent_layer_image_buf_, 0, 0, null);
        stepREC();
    }
    
    agentLayerImage = agent_layer_image_buf_;
    //agentLayerImage.setData(agent_layer_image_buf_.getData());
    }



    public void paintComponent(Graphics g) {
    int w = getWidth();
    int h = getHeight();

    if (offImage!=null) {
        g.drawImage(offImage, 0, 0, this);
        if (agentDirectRenderingMode.isSelected())
        drawAgentLayer((Graphics2D)g);
        else
        g.drawImage(agentLayerImage, 0, 0, this);
        if (recodeBufImage != null) {
        g.setColor(Color.red);
        g.fillOval(10,20,10,10);
        g.drawString("REC", 20, 30);
        g.setColor(Color.black);
        g.drawString(video_frame_counter_+"/"+video_frame_limit_, 50, 30);
        }
    } else {
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
    if (show_id)
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

    private final Ellipse2D draw_agent_shape_cache_ = new Ellipse2D.Double();
    public void draw(Graphics2D g, TrafficAgent agent, boolean show_id, Color color) {
    double x = mx2sx(agent.getX())-2;
    double y = my2sy(agent.getY())-2;
    draw_agent_shape_cache_.setFrame(x, y, 4, 4);
    if (g.getColor()!=color)
        g.setColor(color);
    g.fill(draw_agent_shape_cache_);
    /*
    double x2 = mx2sx(agent.getX()+agent.getFX()*1000000.0);
    double y2 = my2sy(agent.getY()+agent.getFY()*1000000.0);
    double x3 = mx2sx(agent.getX()+agent.getVX()*1000.0);
    double y3 = my2sy(agent.getY()+agent.getVY()*1000.0);
    g.setColor(Color.black);
    g.draw(new Line2D.Double(x, y, x3, y3));
    g.setColor(Color.red);
    g.draw(new Line2D.Double(x, y, x2, y2));
    if (show_id)
        g.drawString(agent.getID(), (int)x, (int)y); 
    */
    }

    public void draw2(Graphics2D g, TrafficAgent agent, boolean show_id, Color color) {
    double d = Math.max(viewZoom*agent.getRadius(), 2);
    double x = mx2sx(agent.getX());
    double y = my2sy(agent.getY());
    //Arc2D.Double arc = new Arc2D.Double(x-d, y-d, d*2, d*2, 0, 360,Arc2D.OPEN);
    Shape arc = new Ellipse2D.Double(x-d, y-d, d+d, d+d);
    g.setColor(color);
    g.fill(arc);
    g.setColor(Color.black);
    g.draw(arc);
    double x2 = mx2sx(agent.getX()+agent.getFX()*1000000.0);
    double y2 = my2sy(agent.getY()+agent.getFY()*1000000.0);
    double x3 = mx2sx(agent.getX()+agent.getVX()*1000.0);
    double y3 = my2sy(agent.getY()+agent.getVY()*1000.0);
    g.setColor(Color.black);
    g.draw(new Line2D.Double(x, y, x3, y3));
    g.setColor(Color.red);
    g.draw(new Line2D.Double(x, y, x2, y2));
    if (show_id)
        g.drawString(agent.getID(), (int)x, (int)y);
    }



    private double sx2mx(double mx) { return (mx-viewOffsetX)/viewZoom; }
    private double sy2my(double my) { return -(my-viweOffsetY)/viewZoom; }
    private double mx2sx(double sx) { return sx*viewZoom+viewOffsetX; }
    private double my2sy(double sy) { return -sy*viewZoom+viweOffsetY; }
    private Point2D.Double s2m(Point2D.Double s) { s.setLocation(sx2mx(s.getX()), sy2my(s.getY())); return s; }
    private Point2D.Double m2s(Point2D.Double m) { m.setLocation(mx2sx(m.getX()), my2sy(m.getY())); return m; }



    public void showTargetsInformation() {
    showInformation(targetList.values().toArray(new TrafficObject[0]));
    }
    public void showInformationByIDs(String... ids) {
    TrafficObject[] object_list = new TrafficObject[ids.length];
    for (int i = 0; i < ids.length; i++)
        object_list[i] = worldManager.getTrafficObject(ids[i]);
    showInformation(object_list);
    }
    public void showInformation(final TrafficObject... object_list) {
    new Thread(new Runnable() {public void run() {
        thisObject.requestFocus();
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        for (int i = 0; i < object_list.length; i++)
        sb.append(object_list[i].toLongString()).append("<br/>");
        sb.append("</html>");
        JTextPane tp = new JTextPane();
        tp.setContentType("text/html");
        tp.setText(sb.toString());
        tp.setBackground(getBackground());
        JScrollPane sp = new JScrollPane(tp);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(thisObject, sp);
    }}, "show targets").start();
    }

    public void setImageUpdateRate(int rate) {
    imageUpdateTerm = rate;
    }
    public int setImageUpdateRate() {
    return imageUpdateTerm;
    }

















    // Action
    public Action getExitAction() {
    AbstractAction action = new AbstractAction("Exit") {
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
    AbstractAction action = new AbstractAction("Import") {
        public void actionPerformed(ActionEvent e) {
        try {
            log(">import");
            File file = IO.getOpenFile(thisObject, "GML file", "xml", "gml");
            alert("opening file: "+file.getAbsolutePath());
            open(file);
            alert("finished!");
        }
    catch (UserCancelException exc) {
            log("cancelled by user.");
        }
    catch (Exception exc) {
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
    AbstractAction action = new AbstractAction("Export") {
        public void actionPerformed(ActionEvent e) {
        save();
        }
    };
    action.putValue("MnemonicKey", KeyEvent.VK_E);
    action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    action.putValue("ShortDescription", "<html>Export to file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
    return action;
    }

    public Action getClearAllAction() {
    AbstractAction action = new AbstractAction("Clear All") {
        public void actionPerformed(ActionEvent e) {
        log(">clear");
        worldManager.clear();
        }
    };
    action.putValue("MnemonicKey", KeyEvent.VK_C);
    action.putValue("ShortDescription", "Clear world data.");
    return action;
    }

    public Action getClearAgentAction() {
    AbstractAction action = new AbstractAction("Clear Agents") {
        public void actionPerformed(ActionEvent e) {
        log(">clear agents");
        StringBuffer successlog = new StringBuffer();
        StringBuffer errorlog = new StringBuffer();
        boolean error = false;
        for (TrafficAgent agent : worldManager.getAgentList()) {
            try {
            worldManager.remove(agent);
            successlog.append(agent.toLongString()).append("\n");
            } catch (Exception exc) {
            exc.printStackTrace();
            errorlog.append(agent.toLongString()).append("\n");
            errorlog.append("-----------------------------\n");
            errorlog.append(exc.getMessage()).append("\n");;
            errorlog.append(" == ===========================\n");
            error = true;
            }
        }
        if (error)
            alert("success:\n"+successlog+"\n\nerror:\n"+errorlog, "error");
        }
    };
    action.putValue("ShortDescription", "Clear all agents.");
    return action;
    }


    public JComponent createGroupPanel() {
    JPanel content = new JPanel();
    String[] group_name_list = agentGroupList.keySet().toArray(new String[0]);
    for (int i = 0; i < group_name_list.length; i++) {
        final String name = group_name_list[i];
        JCheckBox check = new JCheckBox(name);
        for (String tmp : selectedAgentGroupList)
        if (tmp.equals(name))
            check.setSelected(true);
        check.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            JCheckBox tmp_check = (JCheckBox)e.getSource();
            if (tmp_check.isSelected())
                selectedAgentGroupList.add(name);
            else
                for (String tmp : selectedAgentGroupList)
                if (tmp.equals(name))
                    selectedAgentGroupList.remove(name);
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
    AbstractAction action = new AbstractAction("Select Agent Group") {
        public void actionPerformed(ActionEvent e) {
        try {
            requestFocus();
            final JFrame log_frame= new JFrame("Agent Group Manager");

            //agentGroupList;
            //selectedAgentGroupList;
            
            JComponent content = createGroupPanel();
            
            final JPanel panel = new JPanel(new BorderLayout());
            panel.setFocusable(true);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            final JPanel control_pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            control_pane.add(new JButton(new AbstractAction("reset") {
                public void actionPerformed(ActionEvent e) {
                agentGroupList.clear();
                selectedAgentGroupList.clear();
                panel.removeAll();
                panel.add(createGroupPanel(), BorderLayout.CENTER);
                panel.add(control_pane, BorderLayout.SOUTH);
                panel.revalidate();
                }
            }));
            control_pane.add(new JButton(new AbstractAction("create grooup") {
                public void actionPerformed(ActionEvent e) {

                String[] choice = new String[]{"type", "building", "selected agent"};
                String selection = (String)JOptionPane.showInputDialog(thisObject, "Select", "Select", JOptionPane.INFORMATION_MESSAGE, null, choice, choice[0]);
                String saname = null;
                if (selection == choice[2]) {
                    saname=JOptionPane.showInputDialog(thisObject, "input name");
                }

                for (TrafficAgent agent : worldManager.getAgentList()) {
                    String name = null;

                    if (selection == choice[0]) name=agent.getType();
                    else if (selection == choice[1]) name=agent.getArea().getID();
                    else if (selection == choice[2]) {
                    for (TrafficObject o : targetList.values())
                        if (o  ==  agent) name=saname;
                    if (name == null) continue;
                    } else break;

                    ArrayList<TrafficAgent> tal = agentGroupList.get(name);
                    if (tal == null) {
                    tal = new ArrayList<TrafficAgent>();
                    agentGroupList.put(name, tal);
                    }
                    tal.add(agent);
                }
                panel.removeAll();
                panel.add(createGroupPanel(), BorderLayout.CENTER);
                panel.add(control_pane, BorderLayout.SOUTH);
                panel.revalidate();
                }
            }));
            control_pane.add(new JButton(new AbstractAction("close") {
                public void actionPerformed(ActionEvent e) {
                log_frame.dispose();
                }
            }));
            panel.add(content, BorderLayout.CENTER);
            panel.add(control_pane, BorderLayout.SOUTH);
            log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            log_frame.setContentPane(panel);
            log_frame.pack();
            log_frame.setLocationRelativeTo(thisObject);
            log_frame.setVisible(true);

        }
    catch (Exception exception) {exception.printStackTrace();}
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
    private int skip = 100;
    private int skip_counter = 0;

    public void switchREC() throws Exception {
    if (recodeBufImage!=null) {
        endREC();
        return ;
    }
    if (!directory.exists()) directory.mkdir();
    log(directory);
    int w = getWidth();
    int h = getHeight();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    //svbo_ = new SimpleVideoBufferedOutputStream(w, h, 10, file);
    recodeBufImage = image;
    video_frame_counter_ = -1;
    createImageInOtherThread();
    }

    private void stepREC() {
    if (recodeBufImage == null) return ;
    
    //svbo_.write(recodeBufImage);
    File file = new File(directory, video_frame_counter_+"."+video_ext_);
    try {
        javax.imageio.ImageIO.write(recodeBufImage, video_ext_, file);
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    video_frame_counter_++;
    if (video_frame_counter_>video_frame_limit_) endREC();
    }
    private void endREC() {
    recodeBufImage = null;
    //svbo_.close();
    try {
        InputStream is = null;
        File img_file = new File("data/img/handle.png");
        if (img_file.exists())
        is = new FileInputStream(img_file);
        else {
        URL img = ClassLoader.getSystemResource("data/img/handle.png");
        if (img != null)
            is = img.openStream();
        else {
            String traffic3 = ClassLoader.getSystemResource("traffic3/Main.class").toString();
            if (traffic3.startsWith("jar:")) {
            int start = "jar:".length();
            int end = traffic3.indexOf("!");
            File parent = new File(new URL(traffic3.substring(start, end)).toURI()).getParentFile();
            is = new FileInputStream(new File(parent, "data/img/handle.png"));
            }else
            throw new Exception("cannot find data/img/handle.png");
        }
        }
        FileOutputStream fos = new FileOutputStream(new File(directory, "handle.png"));
        for (int i=is.read(); i!=-1; i=is.read())
        fos.write(i);
        fos.flush();
        fos.close();
        is.close();
    }
    catch (Exception e) {
        alert(e, "error");
    }

    try {
        StringBuffer sb = new StringBuffer();
        //sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>Image</title><script type=\"text/javascript\"><!--\nvar counter = 0;var before = \""+"."+"/\";var after = \"."+video_ext_+"\";var wait = "+skip+";var max = "+video_frame_counter_+";function next() {target=document.getElementById(\"target\");target.setAttribute(\"src\", before+counter+after);counter = (counter + 1)%max;setTimeout(\"next()\", wait);}\n--></script></head><body onload=\"next();\" style=\"margin:0;\"><img id=\"target\" src=\"\" style=\"margin:auto;\"/></body></html>");
        sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <style type=\"text/css\"> h1{     text-align:center; } h2{     text-align:left; }  div.author{     text-align:center; } div.name{     text-align:right; } div.affiliation{     text-align:right;     font-size:60%; }   div.fig{     text-align:center;     margin:30px auto 30px auto; }  math{     white-space: nowrap;     margin:20px; }  div.caption{     font: caption;     font-weight: bold;     font-size: 70%;     margin-top:10px;     text-align:center; }  .test{     margin:10px auto 10px auto; }  .source{     font-size: 70%;     text-align:left;     font-family: monospace;     background: white;     width:80%;     padding-left:1em;     margin-left:auto;     margin-right:auto;     border-style:dotted;     border-width:1px; }  .none{     border:none;     background: none;     margin:0;     padding:0; }   table{     border-style:solid;     border-spacing:0;     border-width:1px 0 0 1px;     text-align:center;     background:white;     margin-left:auto;     margin-right:auto; } th{     background:#444444;     color:white;     padding:3px 10px;     border-style:solid;     border-width: 0 1px 1px 0;     border-color:black; } td{     border-style:solid;     padding:0 10px;     border-width:0 1px 1px 0;     text-align:left; }  div.slide{     margin-bottom:100px;     margin-left:auto;     margin-right:auto; } .note{     margin:10px;     border:dotted 1px;     background:white;     font-size:0px;     visibility:hidden; }  div.slide_cover{     margin-bottom:100px; }  ul.i0{     list-style-image:url(./../img/item/darkball_blue24.png); } ul.i1{     list-style-image:url(./../img/item/darkball_red24.png); } ul.i2{     list-style-image:url(./../img/item/darkball_yellow24.png); } ul.i3{     list-style-image:url(./../img/item/darkball_pink24.png); } ul.i4{     list-style-image:url(./../img/item/darkball_black24.png); } ul.i5{     list-style-image:url(./../img/item/darkball_green24.png); }  a:link{ text-decoration:none } a:active{ text-decoration:none } a:visited{ text-decoration:none }  html{     width:100%;     height:100%;     margin:0; } body{     width:90%;     height:100%;     margin:0 auto 400px auto;     background:#cccccc; } </style> <title>Image</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><script type=\"text/javascript\">/*<!--*/var is_playing = false;  var counter = 0;  var before = \"\"; var after = \"."+video_ext_+"\";  var wait = "+skip+"; var cmax = "+video_frame_counter_+"; function next() { if (counter>=cmax) { stop(); counter=0; }else{ counter = (counter + 1); } update(); if (is_playing) setTimeout(\"next()\", wait); }; function prev() { if (counter<=0) { counter=0; }else{ counter = counter-1; } update(); }; function update() { target=document.getElementById(\"target\"); target.setAttribute(\"src\", before+counter+after); document.getElementById(\"count_number\").innerHTML = counter; var point = parseInt(counter*100/cmax); document.getElementById(\"progress\").style.width=point+\"%\"; }; function play() { document.getElementById(\"play-button\").innerHTML=\"stop\"; is_playing=true; next(); }; function stop() { document.getElementById(\"play-button\").innerHTML=\"play\"; is_playing=false; }; function switchPlayStop() { if (is_playing) stop(); else play(); }; function draggedHandleTo(x, y) { var base = document.getElementById(\"progress-handle\"); var dx = x-down_x; /*alert(base.offsetLeft);*/ var dy = y-down_y; /*alert(down_x);*/ var pb = document.getElementById(\"progress-back\"); var width = parseInt(pb.offsetWidth); counter = parseInt(dx*cmax/width+down_counter); if (counter<0) counter=0; else if (counter>cmax) counter=cmax; update(); }; document.onmousedown = function(event) { return false; }; document.onmousemove = function(event) { return false; }; document.onmouseup = function(event) { down_flag=false; return false; }; var down_flag=false; var lastx=0; var lasty=0; var down_x,down_y,down_counter; function setFrame(frame) { counter=frame; update(); };//--></script> </head>   <body onload=\"setFrame(0);\" style=\"margin:auto;\" onmousemove=\"x=event.pageX;y=event.pageY;if (down_flag) { draggedHandleTo(x,y); };lastx=x;lasty=y;return true;\"> <div style=\"margin:100px;\"> <div class=\"test\" style=\"margin:0px;\"> <table id=\"video-player\" class=\"none\" style=\"border:solid 1px gray;\"> <tr>   <td class=\"none\">     <img id=\"target\" src=\"\"/>   </td> </tr> <tr>   <td class=\"none\">     <table class=\"none\">       <tr>       <td class=\"none\">       <div style=\"width:10px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"prev();\">&lt;</div>     </td>     <td class=\"none\">       <div id=\"play-button\" style=\"width:35px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"switchPlayStop();\">play</div>     </td>       <td class=\"none\">       <div style=\"width:10px;text-align:center;font-family:arial;font-size:13px;font-weight:bold;background:#aaaaaa;margin:1px;padding:0px 2px;border:solid 1px gray;cursor:pointer;\" onclick=\"next();\">&gt;</div>     </td>    <td class=\"none\" style=\"width:100%;\">       <div id=\"progress-back\" onmouseup=\"down_flag=false;\" onmouseexit=\"down_flag=false;\" style=\"height:4px;border:solid 1px gray;margin:5px;\">         <div id=\"progress\" style=\"margin:-1;border:solid 1px gray;height:100%;width:0;background:red;\">           <div id=\"progress-handle\" style=\"cursor:pointer;border:solid 0px;;height:10px;width:10px;margin:-7px 0px auto auto;\" onmousedown=\"down_flag=true; down_x=event.pageX; down_counter=counter;down_y=event.pageY;\">         <img src=\"handle.png\" width=\"18px\" height=\"18px\"/>           </div>         </div>       </div>     </td>     <td class=\"none\">       <div id=\"count_number\" style=\"width:30px;padding:2px;font-family:arial;font-size:13px;text-align:right;\">[ ]</div>     </td>       </tr>     </table>   </td> </tr> </table> </div> </div> </body> </html>");

        FileWriter fw = new FileWriter(new File(directory, "index.html"));
        fw.write(sb.toString());
        fw.flush();
        fw.close();
        alert("video recode successfully ended.");
    }
    catch (Exception e) {
        alert(e, "error");
    }
    }

    public Action getFitViewAction() {
    AbstractAction action = new AbstractAction("Fit View") {
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
    Rectangle2D.Double view = worldManager.calcRange();
    if (view != null)
        setView(view);
    }
    public void setView(Rectangle2D view) {
    log("set view:"+view);
    double dwidth = (double)getWidth()/view.getWidth();
    double dheight = (double)getHeight()/view.getHeight();
    if (dwidth < dheight) {
        viewZoom = dwidth*0.9;
        viewOffsetX = -view.getX()*viewZoom+view.getWidth()*(dwidth-viewZoom)*0.5;
        viweOffsetY = -view.getY()*viewZoom+view.getHeight()*(dheight-viewZoom)*0.5;
        // viweOffsetY = -view.getY()*viewZoom + view.getHeight()*(dheight-viewZoom)*0.5+getHeight();
    } else {
        viewZoom = dheight*0.9;
        viewOffsetX = -view.getX()*viewZoom+view.getWidth()*(dwidth-viewZoom)*0.5;
        viweOffsetY = -view.getY()*viewZoom+view.getHeight()*(dheight-viewZoom)*0.5;
        // viweOffsetY = -view.getY()*viewZoom + view.getHeight()*(dheight-viewZoom)*0.5+getHeight();
    }
    viweOffsetY = -viweOffsetY+getHeight();
    transform_.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);    
    createImageInOtherThread();
    }

    public Action getShowAllAction() {
    AbstractAction action = new AbstractAction("show all") {
        public void actionPerformed(ActionEvent e) {
            try {
            requestFocus();
            final JFrame log_frame= new JFrame("Log");
            JTextPane ta = new JTextPane();
            ta.setContentType("text/html");
            KeyAdapter ka = new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode()  ==  KeyEvent.VK_ESCAPE)
                    log_frame.dispose();
                }
                };
            TrafficObject[] all = worldManager.getAll();//.toArray(new TrafficObject[0]);
            TrafficAgent[] agent_list = worldManager.getAgentList();
            TrafficArea[] area_list = worldManager.getAreaList();
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
            control_pane.add(new JButton(new AbstractAction("close") {
                public void actionPerformed(ActionEvent e) {
                    log_frame.dispose();
                }
                }));

            panel.add(sp, BorderLayout.CENTER);
            panel.add(control_pane, BorderLayout.SOUTH);
            log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            log_frame.setContentPane(panel);
            log_frame.pack();
            log_frame.setLocationRelativeTo(thisObject);
            log_frame.setVisible(true);
            
            log_frame.addKeyListener(ka);
            ta.addKeyListener(ka);
            ta.requestFocus();
            ta.revalidate();
            
            }
    catch (Exception exception) {exception.printStackTrace();}
        }
        };
    
    return action;
    }

    public Action getShowLogAction() {
    AbstractAction action = new AbstractAction("Log") {
        public void actionPerformed(ActionEvent e) {
            //            new Thread(new Runnable() {public void run() {
            //SwingUtilities.invokeLater(new Runnable() {public void run() {
            try {
                requestFocus();
                final JFrame log_frame= new JFrame("Log");
                JTextArea ta = new JTextArea();
                KeyAdapter ka = new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode()  ==  KeyEvent.VK_ESCAPE)
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
                control_pane.add(new JButton(new AbstractAction("close") {
                    public void actionPerformed(ActionEvent e) {
                    log_frame.dispose();
                    }
                }));
                panel.add(sp, BorderLayout.CENTER);
                panel.add(control_pane, BorderLayout.SOUTH);
                log_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                log_frame.setContentPane(panel);
                log_frame.pack();
                log_frame.setLocationRelativeTo(thisObject);
                log_frame.setVisible(true);
                
                log_frame.addKeyListener(ka);
                ta.addKeyListener(ka);
                ta.requestFocus();
                ta.revalidate();

            }
    catch (Exception exception) {exception.printStackTrace();}

            //}});
            //        }}).start();
        }
        };
    action.putValue("MnemonicKey", KeyEvent.VK_L);
    action.putValue("ShortDescription", "See all the log information.");
    action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    return action;
    }
    AbstractAction getshowtargetsaction_;
    public Action getShowTargetsAction() {
    if (getshowtargetsaction_ == null)
        getshowtargetsaction_ = new AbstractAction("Show Targets as text") {
            // public boolean isEnabled() {
            // return (targetList.size()!=0);
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
    AbstractAction action = new AbstractAction("Add to selecttion by ID") {
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {public void run() {
            try {
            String id = inputString("id");
            TrafficObject o = worldManager.getTrafficObject(id);
            if (o!=null)
                targetList.put(o.getID(), o);
            else
                alert("cannot find id ["+id+"]", "error");
            createImageInOtherThread();
            }
    catch (Exception exc) {
            alert(exc, "error");
            }
            }}).start();
        }
        };
    //action.putValue("MnemonicKey", KeyEvent.VK_M);
    //action.putValue("ShorDescription", "Select all");
    //action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    return action;
    }

    public Action getSelectAllAction() {
    AbstractAction action = new AbstractAction("Select all Area") {
        public void actionPerformed(ActionEvent e) {
            for (TrafficArea area : worldManager.getAreaList())
            targetList.put(area.getID(), area);
            createImageInOtherThread();
        }
        };
    action.putValue("MnemonicKey", KeyEvent.VK_M);
    action.putValue("ShorDescription", "Select all");
    action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    return action;
    }

    public Action getChangeAgentLayerUpdateRate() {
    AbstractAction action = new AbstractAction("Change Update Rate") {
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {public void run() {
            try {
                int value = inputInt("input update rate["+imageUpdateTerm+"]");
                imageUpdateTerm = value;
            }
    catch (Exception exc) {
                alert(exc);
            }
            }}).start();
        }

        };
    action.putValue("MnemonicKey", KeyEvent.VK_M);
    action.putValue("ShorDescription", "Select all");
    action.putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    return action;
    }
    
    public Action getVideoRecAction() {
    AbstractAction action = new AbstractAction("REC") {
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {public void run() {
            try {
                switchREC();
            }
    catch (Exception exc) {
                alert(exc, "error");
            }
            }},"video recoder").start();
        }
        };
    action.putValue("AcceleratorKey", KeyStroke.getKeyStroke('r'));
    return action;
    }

    public void setMouseStatus(String message) {
    mouseStatusLabel.setText(message);
    }

    
    public void setStatus(final String message) {
    statusLabel.setText(message);
    new Thread(new Runnable() {
        public void run() {
            try {Thread.sleep(1000);}
    catch (Exception exc) {}
            if (statusLabel.getText().equals(message))
            statusLabel.setText("");
        }
        }).start();
    }

    public JMenuBar getMenuBar() {
    return menuBar;
    }

    public JComponent getStatusBar() {

    JPanel slider_panel = new JPanel(new BorderLayout());
    slider_panel.add(imageUpdateRateLabel, BorderLayout.WEST);
    slider_panel.add(imageUpdateRateSlider, BorderLayout.CENTER);
    slider_panel.setMaximumSize(new Dimension(300, 30));
    
    JPanel south_panel = new JPanel();
    south_panel.setLayout(new BoxLayout(south_panel, BoxLayout.X_AXIS));
    south_panel.add(mouseStatusLabel);
    south_panel.add(Box.createHorizontalGlue());
    south_panel.add(slider_panel);
    
    JPanel center_panel = new JPanel(new BorderLayout());
    center_panel.add(statusLabel, BorderLayout.CENTER);

    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(center_panel, BorderLayout.CENTER);
    panel.add(south_panel, BorderLayout.SOUTH);
    return panel;
    }

    public String inputString(final Object message) throws Exception {
    
    if (SwingUtilities.isEventDispatchThread())
        throw new Exception("input string method is called from Event Dispatch Thread!");
    
    final String[] result = new String[1];
    SwingUtilities.invokeLater(new Runnable() {public void run() {
        final JFrame frame = new JFrame("Input");
        final JTextField tf = new JTextField();
        final JButton button = new JButton("OK");

        AbstractAction finish_action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            button.requestFocus();
            result[0] = tf.getText();
            thisObject.requestFocus(true);
            frame.setVisible(false);
            frame.dispose();
            try {
                synchronized(result) {
                result.notifyAll();
                }
            }
    catch (Exception exc) {}
            }
        };
        tf.addActionListener(finish_action);

        JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        button.addActionListener(finish_action);
        buttonpanel.add(button);

        JComponent messagepanel = null;
        if (message instanceof JComponent) {
        messagepanel = (JComponent)message;
        } else {
        messagepanel = new JLabel(message.toString());
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messagepanel, BorderLayout.NORTH);
        panel.add(tf, BorderLayout.CENTER);
        panel.add(buttonpanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(thisObject);
        frame.setVisible(true);
        //result[0] = JOptionPane.showInputDialog(thisObject, message);
    }});
    try {
        synchronized(result) {
        result.wait();
        }
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    return result[0];
    }

    public double inputDouble(Object message) throws Exception {
    return Double.parseDouble(inputString(message));
    }

    public int inputInt(Object message) throws Exception {
    return Integer.parseInt(inputString(message));
    }

    public boolean confirm(Object message) throws Exception {
    
    int result = JOptionPane.showConfirmDialog(thisObject, message, "confirm", JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
    }
    
    @SuppressWarnings("unchecked") private final static <E> java.util.List<E>_(Object list) {
    return (java.util.List<E>)list;
    }
}
