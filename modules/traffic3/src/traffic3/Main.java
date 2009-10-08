package traffic3;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

import traffic3.manager.*;
import traffic3.manager.gui.*;
import traffic3.log.event.*;
import traffic3.simulator.*;
import org.util.xml.io.*;

public class Main {

    private static final String VERSION = "TrafficSimulator(3.0.15)";
    private static final String DEFAULT_CONFIG = "config.xml";

    public static void main(String[] args) throws Exception {

	System.out.println("started");
	String config_name = (args.length==1 ? args[0] : DEFAULT_CONFIG);
	final XMLConfigManager config_manager = new XMLConfigManager(config_name);
	new Thread(new Runnable(){public void run() {
	    try{
		guiStart(config_manager);
	    }catch(Exception e) {
		e.printStackTrace();
	    }
	}}, "gui start").start();
    }
    
    private static void guiStart(XMLConfigManager config_manager) throws Exception {
	final Exception[] exception = new Exception[1];
	final TextArea[] textarea = new TextArea[1];
	final Label progressbar = new Label();
	final boolean[] exit = new boolean[1];
	final Window[] window = new Window[1];
	final long[] start_end = new long[2];
	final WorldManagerGUI[] world_manager_gui = new WorldManagerGUI[1];
	final WorldManager[] world_manager = new WorldManager[1];
	
	// create and show splash window
	Thread create_splash = new Thread(new Runnable(){public void run(){
	    textarea[0] = new TextArea();
	    textarea[0].setEditable(false);
	    //window[0] = new Window((Frame)null);
	    window[0] = new Frame();
	    window[0].setSize(500, 300);
	    window[0].setLocationRelativeTo(null);
	    window[0].setVisible(true);
	    
	    Button button = new Button("exit");
	    button.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e) {
			System.exit(1);
		    }
		});
	    
	    Panel tmp = new Panel(new FlowLayout());
	    tmp.add(progressbar);
	    tmp.add(button);
	    
	    window[0].setLayout(new BorderLayout());
	    Label title = new Label("  "+getVersion()+"  ");
	    title.setFont(new Font("Arial",Font.BOLD,25));
	    window[0].add(title, BorderLayout.NORTH);
	    //window[0].add(new Label("  "), BorderLayout.EAST);
	    //window[0].add(new Label("  "), BorderLayout.WEST);
	    window[0].add(tmp, BorderLayout.SOUTH);
	    //window[0].setVisible(true);
	    window[0].add(textarea[0], BorderLayout.CENTER);
	    window[0].setVisible(true);
	}});
	create_splash.start();
	create_splash.join();
	
	if(exception[0]!=null) throw new Exception(exception[0]);
	
	world_manager[0] = new WorldManager();
	String launch_mode = config_manager.getValue("launch/mode", "plain");
	boolean rcrs_mode = (launch_mode.equals("rcrs"));
	String gui_mode = config_manager.getValue("launch/mode_rcrs/showGUI", "true");
	boolean show_gui_mode = !rcrs_mode || "true".equals(gui_mode);
	String log_enable = config_manager.getValue("log/enable", "true");
	String log_type = config_manager.getValue("log/type", "file");
	final String log_filename = config_manager.getValue("log/filename", "traffic3.log");
	
	
	start_end[0] = System.currentTimeMillis();
	if(show_gui_mode) {
	    // create and show main frame
	    SwingUtilities.invokeAndWait(new Runnable(){public void run(){try{
			    textarea[0].append(getVersion()+"\n");
			    textarea[0].append("set system look and feel\n");
			    try{
				UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
				for(int i=0; i<infos.length; i++)
				    System.out.println(infos[i]);
				String selected = UIManager.getSystemLookAndFeelClassName();
				//UIManager.setLookAndFeel(selected);
			    }catch(Exception exc){ textarea[0].append("cannot set system look and feel.\n"+exc.getMessage()+"\n"); }
			    
			    final JFrame frame = new JFrame();
			    
			    textarea[0].append("initialize logger\n");
			    File log_file = new File(log_filename);
			    textarea[0].append("log file:"+log_file.getAbsolutePath()+"\n");
			    traffic3.log.Logger.initialize(log_file);
			    final int[] alert_dialog_counter = new int[]{0};
			    traffic3.log.Logger.addLogListener(new LogListener(){
				    public void log(LogEvent e) {
					if(alert_dialog_counter[0]>10) {
					    System.err.println("alert dialog error: dialog counter > 10: internal error:"+e.getMessage());
					    return;
					}
					alert_dialog_counter[0]++;
					
					Object message = e.getMessage();
					if(message instanceof Exception) {
					    JPanel panel = new JPanel(new BorderLayout());
					    
					    Exception exc = (Exception)message;
					    StringBuffer sb = new StringBuffer(getLog(exc));
					    JTextArea title = new JTextArea();
					    if(message instanceof FileNotFoundException)
						title.setText("Cannot find file.");
					    else
						title.setText(exc.getMessage());
					    title.setEditable(false);
					    title.setFont(title.getFont().deriveFont((float)20).deriveFont(Font.BOLD));
					    title.setBackground(new Color(0,0,0,0));
					    title.setOpaque(false);
					    title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
					    title.setLineWrap(true);
					    JTextArea ta = new JTextArea();
					    ta.setText(sb.toString());
					    ta.setFont(title.getFont().deriveFont((float)11));
					    ta.setTabSize(3);
					    ta.setOpaque(false);
					    ta.setBackground(new Color(0,0,0,0));
					    JScrollPane sp = new JScrollPane(ta);
					    // sp.setBorder(null);
					    sp.setOpaque(false);
					    sp.setBackground(new Color(0,0,0,0));
					    panel.setPreferredSize(new Dimension(500, 300));
					    panel.add(title, BorderLayout.NORTH);
					    panel.add(sp, BorderLayout.CENTER);
					    JOptionPane.showMessageDialog(frame, panel, "message",e.getType());
					} else {
					    if(e.getType()==JOptionPane.ERROR_MESSAGE)
						JOptionPane.showMessageDialog(frame, message, "message",e.getType());
					    else
						world_manager_gui[0].setStatus(message.toString());
					}
					if(alert_dialog_counter[0]<=10)
				    alert_dialog_counter[0]--;
				    }
				});
			    textarea[0].append("successed to initialize logger\n");
			    
			    //textarea[0].append("cleate world manager\n");
			    
			    textarea[0].append("cleate gui\n");
			    world_manager_gui[0] = new WorldManagerGUI(world_manager[0]);
			    world_manager_gui[0].setBackground(Color.white);
			    
			    JPanel contentpane = new JPanel(new BorderLayout());
			    contentpane.add(world_manager_gui[0], BorderLayout.CENTER);
			    contentpane.add(world_manager_gui[0].getStatusBar(), BorderLayout.SOUTH);
			    
			    frame.setTitle(getVersion());
			    frame.setContentPane(contentpane);
			    frame.setJMenuBar(world_manager_gui[0].getMenuBar());
			    frame.pack();
			    frame.setSize(600, 400);
			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    frame.setLocationRelativeTo(null);
			    frame.setVisible(true);
			    
			    window[0].toFront();
			}catch(Exception e){ exception[0]=e; }}});
	} else {
	    textarea[0].append("initialize logger\n");
	    File log_file = new File(log_filename);
	    textarea[0].append("log file:"+log_file.getAbsolutePath()+"\n");
	    traffic3.log.Logger.initialize(log_file);
	    traffic3.log.Logger.addLogListener(new LogListener(){
		    public void log(LogEvent e) {
			Object message = e.getMessage();
			if(message instanceof Exception) {
			    System.err.println(message);
			    textarea[0].append(((Exception)message).getMessage());
			    textarea[0].append("\n");
			} else {
			    System.out.println(message);	
			    textarea[0].append(message.toString());
			    textarea[0].append("\n");	
			}
		    }
		});
	}

	if(exception[0]!=null) {
	    textarea[0].setBackground(new Color(255, 200, 200));
	    textarea[0].append(getLog(exception[0]));
	    throw new Exception(exception[0]);
	}
	// initialize all component
	
	// now launch process is successfully finished.
	
	// show infomation that  finished to launch.
	SwingUtilities.invokeAndWait(new Runnable(){public void run(){try{
			start_end[1] = System.currentTimeMillis();
			textarea[0].append("launch successfull in "+(start_end[1]-start_end[0]+"[ms]\n"));
		    }catch(Exception e){ exception[0]=e; }}});
	if(exception[0]!=null) throw new Exception(exception[0]);
	try{Thread.sleep(500);}catch(Exception exc){exc.printStackTrace();}
	
	
	
	if(show_gui_mode)
	    // dispose splash window
	    SwingUtilities.invokeAndWait(new Runnable(){public void run(){try{
			    start_end[1] = System.currentTimeMillis();
			    if((start_end[1]-start_end[0])<2000)
				try{Thread.sleep(2000-(start_end[1]-start_end[0]));}catch(Exception exc){exc.printStackTrace();}
			    
			    if(window[0]!=null) {
				window[0].dispose();
			    }
			    traffic3.log.Logger.log("successfully launched.");
			}catch(Exception e){exception[0]=e;}}});
	if(exception[0]!=null) throw new Exception(exception[0]);
	
	if(rcrs_mode) {
	    final RCRSTrafficSimulator rcrs_simulator = new RCRSTrafficSimulator(world_manager[0], config_manager, 100);
	    new Thread(new Runnable(){public void run(){
		try{
		    rcrs_simulator.start();
		}catch(Exception e){
		    e.printStackTrace();
		}
	    }}, "rcrs thread").start();
	} else
	    try{
		File file = new File(config_manager.getValue("launch/mode_plain/auto-import", "./data/auto-import.gml"));
		if(file.exists()) {
		    world_manager_gui[0].open(file);
		}
	    }catch(Exception e){ traffic3.log.Logger.alert(e, "error"); }
    }
    
    public static String getVersion() {
	return VERSION;
    }
    
    public static void alert(Object message) {
	System.out.println(message);
	// javax.swing.JOptionPane.showMessageDialog(null, message);
    }
    public static double inputValue(String msg,String a) {
	boolean continueflag = true;
	double tmpd = -1;
	while(continueflag)
	    try{
		tmpd = Double.parseDouble(JOptionPane.showInputDialog(msg,a));
		continueflag = false;
	    }catch(Exception e){}
	return tmpd;
    }

    public static String getLog(Exception exc) {
	StringWriter sw = new StringWriter();
	exc.printStackTrace(new PrintWriter(sw));
	return sw.toString();
    }
}
