package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Area;
import rescuecore2.misc.Pair;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent<PoliceForce> {
    private static final Log LOG = LogFactory.getLog(SamplePoliceForce.class);

    @Override
    public String toString() {
        return "Sample police force";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
        search.setIgnoreBlockedRoads(false);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        for (Command next : heard) {
            LOG.debug(me() + " heard " + next);
        }
        // Am I on (or next to) a blocked road?
        EntityID target = getTargetBlockade();
        if (target != null) {
            LOG.debug(me() + " clearing blockade " + target);
            sendClear(time, target);
            return;
        }

        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            LOG.debug(me() + " moving: " + path);
            sendMove(time, path);
            return;
        }
        LOG.debug(me() + " couldn't plan a path to a blocked road.");
        sendMove(time, randomWalk());
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Road> result = new ArrayList<Road>();
        for (StandardEntity next : e) {
            Road r = (Road)next;
            if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
                result.add(r);
            }
        }
        return result;
    }

    private EntityID getTargetBlockade() {
        Area location = (Area)location();
        EntityID result = getTargetBlockade(location);
        if (result != null) {
            return result;
        }
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private EntityID getTargetBlockade(Area area) {
        if (!area.isBlockadesDefined()) {
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        if (ids.isEmpty()) {
            return null;
        }
        return ids.get(0);
    }

    /**
       Get the blockade that is nearest this agent.
       @return The EntityID of the nearest blockade, or null if there are no blockades in the agents current location.
    */
    public EntityID getNearestBlockade() {
        return getNearestBlockade((Area)location(), me().getX(), me().getY());
    }

    /**
       Get the blockade that is nearest a point.
       @param area The area to check.
       @param x The X coordinate to look up.
       @param y The X coordinate to look up.
       @return The EntityID of the nearest blockade, or null if there are no blockades in this area.
    */
    public EntityID getNearestBlockade(Area area, int x, int y) {
        double bestDistance = 0;
        EntityID best = null;
        LOG.debug("Finding nearest blockade");
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                LOG.debug("Checking " + blockadeID);
                StandardEntity entity = model.getEntity(blockadeID);
                LOG.debug("Found " + entity);
                if (entity == null) {
                    continue;
                }
                Pair<Integer, Integer> location = entity.getLocation(model);
                LOG.debug("Location: " + location);
                if (location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if (best == null || distance < bestDistance) {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        LOG.debug("Nearest blockade: " + best);
        return best;
    }
}