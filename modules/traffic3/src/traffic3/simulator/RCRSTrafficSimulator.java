package traffic3.simulator;

//import java.util.*;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.awt.Color;
import java.awt.geom.Point2D;

import traffic3.manager.WorldManager;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.ChangeSet;

import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSConnectOK;
import rescuecore2.messages.control.SKAcknowledge;
import rescuecore2.messages.control.SKConnect;
import rescuecore2.messages.control.SKUpdate;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.StandardMessageFactory;
import rescuecore2.connection.ConnectionException;

import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import org.util.xml.io.XMLConfigManager;
import traffic3.manager.WorldManagerException;

/**
 *
 */
public class RCRSTrafficSimulator {

    private static final int DEFAULT_PORT = 7000;
    private WorldManager worldManager;
    private double stepTime;
    private double simulationTime = 0;
    private int port;
    private XMLConfigManager configManager;
    private int state = 0;
    private int simulatorId;
    private int requestId;
    private int rcrsTimeStep;
    private List<Entity> updateList = new ArrayList<Entity>();
    private Map<EntityID, TrafficAgent> humanTrafficAgentMap = new HashMap<EntityID, TrafficAgent>();
    private Map<EntityID, TrafficArea> areaTrafficareaMap = new HashMap<EntityID, TrafficArea>();
    private Map<EntityID, TrafficBlockade> blockadeTrafficblockadeMap = new HashMap<EntityID, TrafficBlockade>();
    private Map<EntityID, Entity> entityidEntityMap = new HashMap<EntityID, Entity>();
    private long stepStart;
    private long stepEnd;
    private long lastTime;
    private long planSum;
    private long stepSum;
    private long drawSum;
    private List<Area> areaListBuf;
    private List<Human> agentListBuf;
    private List<Blockade> blockadeListBuf;


    /*
    public RCRSTrafficSimulator(WorldManager wm, ) {
        Registry.getCurrentRegistry().registerMessageFactory(StandardMessageFactory.INSTANCE);
        Registry.getCurrentRegistry().registerEntityFactory(StandardEntityFactory.INSTANCE);
        Registry.getCurrentRegistry().registerPropertyFactory(StandardPropertyFactory.INSTANCE);
        worldManager = wm;
        stepTime = Double.parseDouble(property.getProperty("rcrs.traffic3.microStep", 100));
        log("stepTime: " + stepTime + "[ms]");
        port = Integer.parseInt(property.getProperty("rcrs.traffic3.port", 7000));
        log("port: " + port);
    }
    */

    /**
     * Constructor.
     * @param wm world manager
     * @param cm config manager
     * @param dt dt
     */
    public RCRSTrafficSimulator(WorldManager wm, XMLConfigManager cm) {
        Registry.getCurrentRegistry().registerMessageFactory(StandardMessageFactory.INSTANCE);
        Registry.getCurrentRegistry().registerEntityFactory(StandardEntityFactory.INSTANCE);
        Registry.getCurrentRegistry().registerPropertyFactory(StandardPropertyFactory.INSTANCE);

        worldManager = wm;
        configManager = cm;
        stepTime = cm.getDouble("rcrs/traffic3/microStep", 100);
        port = Integer.parseInt(configManager.getValue("rcrs/traffic3/port", String.valueOf(DEFAULT_PORT)));
        log("port: " + port);
    }

    /**
     * start.
     * @throws Exception exception
     */
    public void start() throws Exception {
        requestId = 1;
        simulatorId = 1; // kernel id
        areaListBuf = new ArrayList<Area>();
        agentListBuf = new ArrayList<Human>();
        blockadeListBuf = new ArrayList<Blockade>();

        log("try to connect to the Kernel...");
        TCPConnection connection = new TCPConnection(port);
        connection.addConnectionListener(new ConnectionManager());
        connection.startup();
        log("connected to the Kernel");
        log("send SKConnect");
        connection.sendMessage(new SKConnect(requestId, simulatorId, traffic3.Main.getVersion()));
    }

    private EntityID transID(String id) {
        final int start = 5;
        return new EntityID(Integer.parseInt(id.substring(start, id.length() - 1)));
    }

    private void receivedEntities() {
        try {
            for (Area area : areaListBuf) {
                entityidEntityMap.put(area.getID(), area);
                java.util.List<EntityID> nextAreaIdList = area.getNeighbours();
                String[] nextAreaIdTextList = new String[nextAreaIdList.size()];
                for (int i = 0; i < nextAreaIdList.size(); i++) {
                    nextAreaIdTextList[i] = "rcrs(" + nextAreaIdList.get(i).getValue() + ")";
                }
                double cx = area.getX();
                double cy = area.getY();
                TrafficArea trafficArea = new TrafficArea(worldManager, "rcrs(" + area.getID() + ")", cx, cy, area.getApexes(), nextAreaIdTextList);
                if (area instanceof Building) {
                    trafficArea.setType("building");
                }
                else {
                    trafficArea.setType("open space");
                }
                worldManager.appendWithoutCheck(trafficArea);
                areaTrafficareaMap.put(area.getID(), trafficArea);
            }

            worldManager.check();
            final int rescueAgentRadius = 500;
            final int civilianAgentRadius = 200;
            final double a = 0.2;
            final double b = 0.002;
            final double civilianVLimit = a + Math.random() * b;

            for (Human human : agentListBuf) {
                entityidEntityMap.put(human.getID(), human);
                TrafficAgent agent = new TrafficAgent(worldManager);
                if (human instanceof PoliceForce) {
                    agent.setType("PoliceForce");
                    agent.setRadius(rescueAgentRadius);
                    agent.setColor(Color.blue);
                }
                else if (human instanceof AmbulanceTeam) {
                    agent.setType("AmbulanceTeam");
                    agent.setRadius(rescueAgentRadius);
                    agent.setColor(Color.white);
                }
                else if (human instanceof Civilian) {
                    agent.setType("Civilian");
                    agent.setRadius(civilianAgentRadius);
                    agent.setColor(Color.green);
                    agent.setVLimit(civilianVLimit);
                }
                else if (human instanceof FireBrigade) {
                    agent.setType("FireBrigade");
                    agent.setRadius(rescueAgentRadius);
                    agent.setColor(Color.red);
                }
                else {
                    agent.setType("Unknown");
                    agent.setColor(Color.black);
                }
                System.err.println(human);
                rescuecore2.misc.Pair<java.lang.Integer, java.lang.Integer> loc = human.getLocation(null);
                agent.setLocation(loc.first(), loc.second(), 0);
                humanTrafficAgentMap.put(human.getID(), agent);
                worldManager.appendWithoutCheck(agent);
            }


            for (Blockade blockade : blockadeListBuf) {
                double cx = blockade.getX();
                double cy = blockade.getY();
                //Area a = (Area)entityidEntityMap.get(blockade.getArea());
                TrafficArea area = areaTrafficareaMap.get(blockade.getPosition());
                int[] xy = blockade.getApexes();
                String id = "rcrs(" + blockade.getID().getValue() + ")";
                TrafficBlockade tblockade = new TrafficBlockade(worldManager, id, cx, cy, blockade.getApexes());
                area.addBlockade(tblockade);
                blockadeTrafficblockadeMap.put(blockade.getID(), tblockade);
                worldManager.appendWithoutCheck(tblockade);
            }


            worldManager.notifyInputted(this);
        }
        catch (WorldManagerException e) {
            alert(e, "error");
        }
    }


    private void receiveKSConnectOK(Connection c, KSConnectOK co) {
        //alert(co, "error");
        simulatorId = co.getSimulatorID();
        requestId = co.getRequestID();
        Collection<Entity> entities = co.getEntities();
        for (Entity ent : entities) {
            if (ent instanceof Area) {
                areaListBuf.add((Area)ent);
            }
            else if (ent instanceof Human) {
                agentListBuf.add((Human)ent);
            }
            else if (ent instanceof Blockade) {
                blockadeListBuf.add((Blockade)ent);
            }
            else {
                log("skipped: " + ent);
            }
        }
        receivedEntities();
        try {
            c.sendMessage(new SKAcknowledge(requestId, simulatorId));
        }
        catch (ConnectionException e) {
            log(e);
            e.printStackTrace();
        }
        alert("\n[initialized]\n");
    }

    private void receiveCommands(Connection c, KSCommands com) {
        log(com);
        rcrsTimeStep = com.getTime();
        for (Command command : com.getCommands()) {
            if (command instanceof AKMove) {
                AKMove akmove = (AKMove)command;
                java.util.List<EntityID> list = akmove.getPath();
                EntityID destinationId = list.get(list.size() - 1);
                //Entity destination = entityidEntityMap.get(destinationId);
                Entity entity = entityidEntityMap.get(destinationId);
                Human human = (Human)entityidEntityMap.get(akmove.getAgentID());
                TrafficAgent agent = humanTrafficAgentMap.get(human.getID());
                if (entity instanceof Area) {
                    TrafficArea trafficArea = areaTrafficareaMap.get(destinationId);
                    double cx = trafficArea.getCenterX();
                    double cy = trafficArea.getCenterY();
                    double cz = 0;
                    try {
                        agent.setDestination(worldManager.createAreaNode(cx, cy, cz));
                    }
                    catch (WorldManagerException exc) {
                        alert(exc, "error");
                    }
                }
                else if (entity instanceof Human) {
                    TrafficAgent destinationAgent = humanTrafficAgentMap.get(destinationId);
                    agent.setDestination(destinationAgent);
                }
                else {
                    System.err.println("warning: unknown entity: " + entity);
                }
            }
            else if (command instanceof AKClear) {
                AKClear akclear = (AKClear)command;
                TrafficAgent agent = humanTrafficAgentMap.get(akclear.getAgentID());
                TrafficBlockade blockade = blockadeTrafficblockadeMap.get(akclear.getTarget());
                try {
                    TrafficAreaNode node = worldManager.createAreaNode(blockade.getCenterX(), blockade.getCenterY(), 0);
                    agent.setDestination(node);
                }
                catch (WorldManagerException exc) {
                    log(exc);
                    exc.printStackTrace();
                }
            }
            else if (command instanceof AKLoad) {
                AKLoad akload = (AKLoad)command;
                Human human = (Human)entityidEntityMap.get(akload.getTarget());
                TrafficAgent agent = humanTrafficAgentMap.get(human.getID());
                alert(agent);
            }
        }

        updateList.clear();
        if (rcrsTimeStep > 2) {
            rcrsStep();
        }

        for (Human human : agentListBuf) {
            TrafficAgent agent = humanTrafficAgentMap.get(human.getID());
            EntityID id = transID(agent.getArea().getID());
            Point2D[] pList = agent.getPositionHistory();
            int[] rcrsPList = new int[pList.length * 2];
            for (int i = 0; i < pList.length; i++) {
                Point2D p = pList[i];
                rcrsPList[i * 2] = (int)p.getX();
                rcrsPList[i * 2 + 1] = (int)p.getY();
            }
            agent.clearPositionHistory();
            human.setPosition(id, (int)agent.getX(), (int)agent.getY());
            human.setPositionHistory(rcrsPList);
            updateList.add(human);
        }
        ChangeSet changeSet = new ChangeSet();
        changeSet.addAll(updateList);
        try {
            c.sendMessage(new SKUpdate(simulatorId, rcrsTimeStep, changeSet));
            updateList.clear();
        }
        catch (ConnectionException e) {
            log(e);
            e.printStackTrace();
        }
    }


    private void receiveUpdate(Connection c, KSUpdate up) {

        Collection<EntityID> entityIDs = up.getChangeSet().getChangedEntities();
        List<Entity> entities = new ArrayList<Entity>();
        for (EntityID eid : entityIDs) {
            entities.add(entityidEntityMap.get(eid));
        }
        for (Entity ent : entities) {
            if (ent instanceof Blockade) {
                TrafficBlockade tb = blockadeTrafficblockadeMap.get(ent.getID());
                tb.setLineList(((Blockade)ent).getApexes());
            }
            else if (ent instanceof Area) {
                Area area = (Area)ent;
                //Area parea = (Area)entityidEntityMap.get(area.getID());
                TrafficArea tarea = areaTrafficareaMap.get(area.getID());
                assert tarea != null : "Error!";
                java.util.List<EntityID> idList = area.getBlockades();
                TrafficBlockade[] tBlockadeList = tarea.getBlockadeList();
                if (idList.size() != 0 || tBlockadeList.length != 0) {
                    List<TrafficBlockade> blist = new ArrayList<TrafficBlockade>();
                    for (EntityID beid : idList) {
                        TrafficBlockade tblockade = blockadeTrafficblockadeMap.get(beid);
                        blist.add(tblockade);
                    }

                    for (TrafficBlockade blockade : tBlockadeList) {
                        if (!blist.contains(blockade)) {
                            try {
                                worldManager.remove(blockade);
                            }
                            catch (WorldManagerException exc) {
                                log(exc);
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
    }

    private class ConnectionManager implements ConnectionListener {

        public void messageReceived(Connection c, Message msg) {

            if (state == 0 && msg instanceof KSConnectOK) {

                receiveKSConnectOK(c, (KSConnectOK)msg);
                state = 1;
            }
            else if (state == 1 && msg instanceof KSCommands) {

                receiveCommands(c, (KSCommands)msg);
                state = 2;
            }
            else if (state == 2 && msg instanceof KSUpdate) {

                receiveUpdate(c, (KSUpdate)msg);
                state = 1;
            }
            else {
                alert("unknown command: " + msg, "error");
            }
        }
    }

    /**
     * rcrs step.
     */
    public void rcrsStep() {
        stepStart = System.currentTimeMillis();
        lastTime = stepStart;
        //int length = (int)(1000*60/stepTime);
        final int minute = 60;
        final int test = 1;
        int length = (int)(1000 * minute / stepTime) * test;

        for (int i = 0; i < length; i++) {
            step();
        }
        stepEnd = System.currentTimeMillis();
        System.out.println("step: " + (stepEnd-stepStart) + "[ms]");
        System.out.println("   plan: " + (planSum) + "[ms]");
        System.out.println("   step: " + (stepSum) + "[ms]");
        System.out.println("   draw: " + (drawSum) + "[ms]");
        StringBuffer sb = new StringBuffer("<html>");
        sb.append("<table>");
        sb.append("<tr><td>step</td><td>" + (stepEnd - stepStart) + "[ms](" + length + "[step])</td></tr>");
        sb.append("<tr><td>calculate force</td><td>" + (planSum) + "[ms]</td></tr>");
        sb.append("<tr><td>move agents</td><td>" + (stepSum) + "[ms]</td></tr>");
        sb.append("<tr><td>draw</td><td>" + (drawSum) + "[ms]</td></tr>");
        sb.append("</table>");
        sb.append("</html>");
        log(sb.toString());
        //System.out.println(sb.toString());
        planSum = 0;
        stepSum = 0;
        drawSum = 0;
    }

    private void step() {
        TrafficAgent[] agentList = worldManager.getAgentList();
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            agent.plan();
        }

        long nowTime = System.currentTimeMillis();
        planSum += (nowTime - lastTime);
        lastTime = nowTime;

        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            agent.step(stepTime);
        }

        nowTime = System.currentTimeMillis();
        stepSum += (nowTime - lastTime);
        lastTime = nowTime;

        worldManager.stepFinished(this);

        nowTime = System.currentTimeMillis();
        drawSum += (nowTime - lastTime);
        lastTime = nowTime;

        simulationTime += stepTime;
    }

    /**
     * set simulation time.
     * @param simulation time
     */
    private void setTime(double time) {
        simulationTime = time;
    }

    /**
     * get simulation time.
     * @return simulation time
     */
    public double getTime() {
        return simulationTime;
    }
}