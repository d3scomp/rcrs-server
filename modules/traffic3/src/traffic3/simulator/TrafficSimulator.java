package traffic3.simulator;

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
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.StandardMessageFactory;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.connection.ConnectionException;

import java.util.Random;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.number.NumberGenerator;

/**
   The Area model traffic simulator.
 */
public class TrafficSimulator extends StandardSimulator {
    private static final int RESCUE_AGENT_RADIUS = 500;
    private static final int CIVILIAN_RADIUS = 200;
    private static final double RESCUE_AGENT_VELOCITY_MEAN = 0.7;
    private static final double RESCUE_AGENT_VELOCITY_SD = 0.1;
    private static final double CIVILIAN_VELOCITY_MEAN = 0.2;
    private static final double CIVILIAN_VELOCITY_SD = 0.002;

    private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
    private static final Color POLICE_FORCE_COLOUR = Color.BLUE;
    private static final Color AMBULANCE_TEAM_COLOUR = Color.WHITE;
    private static final Color CIVILIAN_COLOUR = Color.GREEN;

    private WorldManager worldManager;
    private int timestep;

    protected void postConnect() {
        worldManager = new WorldManager();
        for (StandardEntity next : model) {
            if (next instanceof Area) {
                convertAreaToTrafficArea((Area)next);
            }
        }
        worldManager.check();
        NumberGenerator<Double> agentVelocityGenerator = new GaussianGenerator(RESCUE_AGENT_VELOCITY_MEAN, RESCUE_AGENT_VELOCITY_SD, config.getRandom());
        NumberGenerator<Double> civilianVelocityGenerator = new GaussianGenerator(CIVILIAN_VELOCITY_MEAN, CIVILIAN_VELOCITY_SD, config.getRandom());
        for (StandardEntity next : model) {
            if (next instanceof Human) {
                convertHuman((Human)next, agentVelocityGenerator, civilianVelocityGenerator);
            }
        }
        for (StandardEntity next : model) {
            if (next instanceof Blockade) {
                convertBlockade((Blockade)next);
            }
        }
        worldManager.notifyInputted(this);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        timestep = c.getTime();
        for (Command next : c.getCommands()) {
            if (next instanceof AKMove) {
                handleMove((AKMove)next);
            }
        }
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
    }

    private void convertAreaToTrafficArea(Area area) {
        java.util.List<EntityID> neighbours = area.getNeighbours();
        String[] neighbourText = new String[neighbours.size()];
        for (int i = 0; i < neighbours.size(); i++) {
            neighbourText[i] = "rcrs(" + neighbours.get(i).getValue() + ")";
        }
        double cx = area.getX();
        double cy = area.getY();
        TrafficArea result = new TrafficArea(worldManager, "rcrs(" + area.getID() + ")", cx, cy, area.getApexList(), neighbourText);
        if (area instanceof Building) {
            result.setType("building");
        }
        else {
            result.setType("open space");
        }
        worldManager.appendWithoutCheck(result);
    }

    private void convertHuman(Human h, NumberGenerator<Double> agentVelocityGenerator, NumberGenerator<Double> civilianVelocityGenerator) {
        double radius = 0;
        double velocityLimit = 0;
        String type = null;
        Color colour = null;
        if (h instanceof FireBrigade) {
            type = "FireBrigade";
            radius = RESCUE_AGENT_RADIUS;
            colour = FIRE_BRIGADE_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof PoliceForce) {
            type = "PoliceForce";
            radius = RESCUE_AGENT_RADIUS;
            colour = POLICE_FORCE_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof AmbulanceTeam) {
            type = "AmbulanceTeam";
            radius = RESCUE_AGENT_RADIUS;
            colour = AMBULANCE_TEAM_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof Civilian) {
            type = "Civlian";
            radius = CIVILIAN_RADIUS;
            colour = CIVILIAN_COLOUR;
            velocityLimit = civilianVelocityGenerator.nextValue();
        }
        else {
            throw new IllegalArgumentException("Unrecognised agent type: " + h + " (" + h.getClass().getName() + ")");
        }
        String id = "rcrs(" + h.getID() + ")";
        TrafficAgent agent = new TrafficAgent(worldManager, id, radius, velocityLimit);
        agent.setLocation(h.getX(), h.getY(), 0);
        agent.setType(type);
        agent.setColor(colour);
        worldManager.appendWithoutCheck(agent);
    }

    private void convertBlockade(Blockade blockade) {
        double cx = blockade.getX();
        double cy = blockade.getY();
        TrafficArea area = (TrafficArea)worldManager.getTrafficObject("rcrs(" + blockade.getPosition().getValue() + ")");
        int[] apexes = blockade.getApexes();
        String id = "rcrs(" + blockade.getID().getValue() + ")";
        TrafficBlockade result = new TrafficBlockade(worldManager, id, cx, cy, apexes);
        area.addBlockade(result);
        worldManager.appendWithoutCheck(result);
    }

    private void handleMove(AKMove move) {
        List<EntityID> list = move.getPath();
        EntityID destinationID = list.get(list.size() - 1);
        TrafficArea trafficArea = (TrafficArea)worldManager.getTrafficObject("rcrs(" + destinationID + ")");
        Human human = (Human)model.getEntity(move.getAgentID());
        TrafficAgent agent = (TrafficAgent)worldManager.getTrafficObject("rcrs(" + human.getID() + ")");
        double cx = trafficArea.getCenterX();
        double cy = trafficArea.getCenterY();
        double cz = 0;
        agent.setDestination(worldManager.createAreaNode(cx, cy, cz));
    }




    /*
    private void receiveCommands(Connection c, KSCommands com) {
        log(com);
        rcrsTimeStep = com.getTime();
        for (Command command : com.getCommands()) {
            if (command instanceof AKMove) {
                AKMove akmove = (AKMove)command;
                java.util.List<EntityID> list = akmove.getPath();
                EntityID destinationId = list.get(list.size() - 1);
                //Entity destination = entityidEntityMap.get(destinationId);
                TrafficArea trafficArea = areaTrafficareaMap.get(destinationId);
                assert trafficArea != null : "cannot find traffic area: " + destinationId;
                Human human = (Human)entityidEntityMap.get(akmove.getAgentID());
                TrafficAgent agent = humanTrafficAgentMap.get(human.getID());
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
    */

    /**
     * rcrs step.
     */
    public void rcrsStep() {
        stepStart = System.currentTimeMillis();
        lastTime = stepStart;
        //int length = (int)(1000*60/stepTime);
        final int minute = 60;
        final int test = 10;
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