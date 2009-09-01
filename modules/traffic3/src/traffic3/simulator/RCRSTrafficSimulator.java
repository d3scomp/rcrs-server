package traffic3.simulator;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import traffic3.manager.*;
import traffic3.objects.area.*;
import traffic3.objects.*;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

import rescuecore2.connection.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.*;
import rescuecore2.messages.control.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import org.util.xml.io.*;


public class RCRSTrafficSimulator {

    private final int DEFAULT_PORT = 7000;
    private WorldManager world_manager_;
    private double dt_;
    private double time_ = 0;
    private int port_;
    private XMLConfigManager config_manager_;
    private int state_ = 0;
    private int simulator_id_;
    private int request_id_;
    private int rcrs_time_;
    private ArrayList<Entity> update_list_ = new ArrayList<Entity>();
    private HashMap<EntityID, TrafficAgent> human_trafficagent_map_ = new HashMap<EntityID, TrafficAgent>();
    private HashMap<EntityID, TrafficArea> area_trafficarea_map_ = new HashMap<EntityID, TrafficArea>();
    private HashMap<EntityID, TrafficBlockade> blockade_trafficblockade_map_ = new HashMap<EntityID, TrafficBlockade>();
    private HashMap<EntityID, Entity> entityid_entity_map_ = new HashMap<EntityID, Entity>();

    public RCRSTrafficSimulator(WorldManager world_manager, XMLConfigManager config_manager,  double dt) {

	MessageRegistry.register(StandardMessageFactory.INSTANCE);
	EntityRegistry.register(StandardEntityFactory.INSTANCE);

	world_manager_ = world_manager;
	config_manager_ = config_manager;
	dt_ = dt;
        port_ = Integer.parseInt(config_manager.getValue("launch/mode_rcrs/port", String.valueOf(DEFAULT_PORT)));
	log("port: "+port_);
    }

    public void start() throws Exception {
	request_id_ = 1;
	simulator_id_ = 1; // kernel id
	
	final ArrayList<Area> area_list_ = new ArrayList<Area>();
	final ArrayList<Human> agent_list_ = new ArrayList<Human>();
	final ArrayList<Blockade> blockade_list_ = new ArrayList<Blockade>();
	
	TCPConnection connection = new TCPConnection(port_);
	connection.addConnectionListener(new ConnectionListener() {
		public void messageReceived(Connection c, Message msg) {
		    if(state_==0 && msg instanceof KSConnectOK) {
			KSConnectOK co = (KSConnectOK)msg;
			//alert(co, "error");
			simulator_id_ = co.getSimulatorID();
			request_id_ = co.getRequestID();
			Collection<Entity> entities = co.getEntities();

			for(Entity ent : entities) {
			    if(ent instanceof Area)
				area_list_.add((Area)ent);
			    else if(ent instanceof Human)
				agent_list_.add((Human)ent);
			    else if(ent instanceof Blockade)
				blockade_list_.add((Blockade)ent);
			    else
				log("skipped: "+ent);
			}
			receivedEntities(area_list_, agent_list_, blockade_list_);

			state_ = 1;
			try{
			    c.sendMessage(new SKAcknowledge(request_id_, simulator_id_));
			} catch(Exception e) {
			    e.printStackTrace();
			}
		    } else if(state_==1 && msg instanceof Commands) {
			Commands com = (Commands)msg;
			log(com);
			rcrs_time_ = com.getTime();
			state_ = 2;

			for(Command command : com.getCommands()) {
			    if(command instanceof AKMove) {
				AKMove akmove = (AKMove)command;
				java.util.List<EntityID> list = akmove.getPath();
				EntityID destination_id = list.get(list.size()-1);
				////				Entity destination = entityid_entity_map_.get(destination_id);
				TrafficArea traffic_area = area_trafficarea_map_.get(destination_id);
				assert traffic_area!=null : "cannot find traffic area: "+destination_id;
				Human human = (Human)entityid_entity_map_.get(akmove.getAgentID());
				TrafficAgent agent = human_trafficagent_map_.get(human.getID());
				double cx = traffic_area.getCenterX();
				double cy = traffic_area.getCenterY();
				double cz = 0;
				try{
				    agent.setDestination(world_manager_.createAreaNode(cx, cy, cz));
				}catch(Exception exc) {
				    alert(exc, "error");
				}
			    } else if(command instanceof AKClear) {
				AKClear akclear = (AKClear)command;
				TrafficAgent agent = human_trafficagent_map_.get(akclear.getAgentID());
				TrafficBlockade blockade = blockade_trafficblockade_map_.get(akclear.getTarget());
				try{
				    TrafficAreaNode node = world_manager_.createAreaNode(blockade.getCenterX(), blockade.getCenterY(), 0);
				    agent.setDestination(node);
				}catch(Exception exc) {
				    exc.printStackTrace();
				}
			    } else if(command instanceof AKLoad) {
				AKLoad akload = (AKLoad)command;
				Human human = (Human)entityid_entity_map_.get(akload.getTarget());
				TrafficAgent agent = human_trafficagent_map_.get(human.getID());
				alert(agent);
			    }
			}

			update_list_.clear();
			if(rcrs_time_>2)
			    rcrsStep();

			for(Human human : agent_list_) {
			    TrafficAgent agent = human_trafficagent_map_.get(human.getID());
			    EntityID id = transID(agent.getArea().getID());
			    human.setPosition(id, (int)agent.getX(), (int)agent.getY());
			    update_list_.add(human);
			}

			try{
			    c.sendMessage(new SKUpdate(simulator_id_, rcrs_time_, update_list_));
			    update_list_.clear();
			}catch(Exception e) {
			    e.printStackTrace();
			}
		    } else if(state_==2 && msg instanceof Update) {
			Update up = (Update)msg;
			Collection<Entity> entities = up.getUpdatedEntities();
			for(Entity ent : entities) {
			    if(ent instanceof Blockade) {
				TrafficBlockade tb = blockade_trafficblockade_map_.get(ent.getID());
				tb.setLineList(((Blockade)ent).getShape());
				
			    } else if(ent instanceof Area) {
				Area area = (Area)ent;
				//Area parea = (Area)entityid_entity_map_.get(area.getID());
				TrafficArea tarea = area_trafficarea_map_.get(area.getID());
				assert tarea!=null : "Error!";
				java.util.List<EntityID> id_list = area.getBlockadeList();
				TrafficBlockade[] tblockade_list = tarea.getBlockadeList();
				if(id_list.size()==0 && tblockade_list.length==0) {
				}else{
				    ArrayList<TrafficBlockade> blist = new ArrayList<TrafficBlockade>();
				    for(EntityID beid : id_list) {
					TrafficBlockade tblockade = blockade_trafficblockade_map_.get(beid);
					
					blist.add(tblockade);
				    }

				    for(TrafficBlockade blockade : tblockade_list) {
					if(!blist.contains(blockade)) {
					    try{
						world_manager_.remove(blockade);
					    }catch(Exception exc){
						exc.printStackTrace();
					    }
					}
				    }

				    tarea.setBlockadeList(blist.toArray(new TrafficBlockade[0]));
				}
			    }
			    //alert(ent, "error");
			}
			//alert(up, "error");
			state_ = 1;
		    } else {
			alert("unknown command: "+msg, "error");
		    }
		}
	    });
	connection.startup();
	connection.sendMessage(new SKConnect(request_id_, simulator_id_));
    }
    
    private EntityID transID(String id) {
	return new EntityID(Integer.parseInt(id.substring(5, id.length()-1)));
    }

    private void receivedEntities(ArrayList<Area> area_list, ArrayList<Human> agent_list, ArrayList<Blockade> blockade_list) {
	try{
	    for(Area area : area_list) {
		entityid_entity_map_.put(area.getID(), area);
		java.util.List<EntityID> next_area_id_list = area.getNextArea();
		String[] next_area_id_text_list = new String[next_area_id_list.size()];
		for(int i=0; i<next_area_id_list.size(); i++)
		    next_area_id_text_list[i] = "rcrs("+next_area_id_list.get(i).getValue()+")";
		double cx = area.getCenterX();
		double cy = area.getCenterY();
		TrafficArea traffic_area = new TrafficArea(world_manager_, "rcrs("+area.getID()+")", cx, cy, area.getShape(), next_area_id_text_list);
		if(area instanceof Building) {
		    traffic_area.setType("building");
		} else {
		    traffic_area.setType("open space");
		}
		world_manager_.appendWithoutCheck(traffic_area);
		area_trafficarea_map_.put(area.getID(), traffic_area);
	    }

	    world_manager_.check();


	    for(Human human : agent_list) {
		entityid_entity_map_.put(human.getID(), human);
		TrafficAgent agent = new TrafficAgent(world_manager_);
		if(human instanceof PoliceForce) {
		    agent.setType("PoliceForce");
		    agent.setRadius(500);
		    agent.setColor(Color.blue);
		} else if(human instanceof AmbulanceTeam) {
		    agent.setType("AmbulanceTeam");
		    agent.setRadius(500);
		    agent.setColor(Color.white);
		} else if(human instanceof Civilian) {
		    agent.setType("Civilian");
		    agent.setRadius(200);
		    agent.setColor(Color.green);
		} else if(human instanceof FireBrigade) {
		    agent.setType("FireBrigade");
		    agent.setRadius(500);
		    agent.setColor(Color.red);
		} else {
		    agent.setType("Unknown");
		    agent.setColor(Color.black);
		}

		rescuecore2.misc.Pair<java.lang.Integer,java.lang.Integer> loc = human.getLocation(null); 
		agent.setLocation(loc.first(), loc.second(), 0);
		human_trafficagent_map_.put(human.getID(), agent);
		world_manager_.appendWithoutCheck(agent);
	    }


	    for(Blockade blockade : blockade_list) {
		
		double cx = blockade.getCenterX();
		double cy = blockade.getCenterY();
		//Area a = (Area)entityid_entity_map_.get(blockade.getArea());
		TrafficArea area = area_trafficarea_map_.get(blockade.getArea());
		int[] xy = blockade.getShape();
		String id = "rcrs("+blockade.getID().getValue()+")";
		TrafficBlockade tblockade = new TrafficBlockade(world_manager_, id, cx, cy, blockade.getShape());
		
		area.addBlockade(tblockade);
		blockade_trafficblockade_map_.put(blockade.getID(), tblockade);
		world_manager_.appendWithoutCheck(tblockade);
	    }


	    world_manager_.notifyInputted(this);
	}catch(Exception e) {
	    alert(e, "error");
	}

    }


    private long step_start_;
    private long step_end_;
    private long last_time_;
    private long plan_sum_;
    private long step_sum_;
    private long draw_sum_;

    public void rcrsStep() {
	step_start_ = System.currentTimeMillis();
	last_time_ = step_start_;
	//int length = (int)(1000*60/dt_);
	int length = (int)(1000*60/dt_)*10;
	for(int i=0; i<length; i++)
	    step(); 
	step_end_ = System.currentTimeMillis();
	//System.err.println("step: "+(step_end_-step_start_)+"[ms]");
	//System.err.println("   plan: "+(plan_sum_)+"[ms]");
	//System.err.println("   step: "+(step_sum_)+"[ms]");
	//System.err.println("   draw: "+(draw_sum_)+"[ms]");
	StringBuffer sb = new StringBuffer("<html>");
	sb.append("<table>");
	sb.append("<tr><td>step</td><td>"+(step_end_-step_start_)+"[ms]("+length+"[step])</td></tr>");
	sb.append("<tr><td>calculate force</td><td>"+(plan_sum_)+"[ms]</td></tr>");
	sb.append("<tr><td>move</td><td>"+(step_sum_)+"[ms]</td></tr>");
	sb.append("<tr><td>draw</td><td>"+(draw_sum_)+"[ms]</td></tr>");
	sb.append("</table>");
	sb.append("</html>");
	alert(sb.toString());
	plan_sum_ = step_sum_ = draw_sum_ = 0;
    }

    private void step() {
	TrafficAgent[] agent_list = world_manager_.getAgentList();
	for(int i=0; i<agent_list.length; i++) {
	    TrafficAgent agent = agent_list[i];
	    agent.plan();
	}

	long now_time = System.currentTimeMillis();
	plan_sum_ += (now_time-last_time_);
	last_time_ = now_time;

	for(int i=0; i<agent_list.length; i++) {
	    TrafficAgent agent = agent_list[i];
	    agent.step(dt_);
	}

	now_time = System.currentTimeMillis();
	step_sum_ += (now_time-last_time_);
	last_time_ = now_time;

	world_manager_.stepFinished(this);

	now_time = System.currentTimeMillis();
	draw_sum_ += (now_time-last_time_);
	last_time_ = now_time;

	time_ += dt_;
    }


    public void setTime(double time) {
	time_ = time;
    }
    public double getTime() {
	return time_;
    }
}
