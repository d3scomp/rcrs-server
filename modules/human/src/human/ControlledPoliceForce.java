package human;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A basic police force agent that will try to clear a given target. Fully-blocked roads encountered along the way are also cleared. If there is no target then this agent does nothing.
*/
public class ControlledPoliceForce extends StandardAgent<PoliceForce> {
    private static final Log LOG = LogFactory.getLog(ControlledPoliceForce.class);

    private SampleSearch search;
    private Road target;

    /**
       Set the target for this police force.
       @param target The new target.
    */
    public void setTarget(Road target) {
        this.target = target;
    }

    @Override
    protected void think(int time, Collection<EntityID> changed, Collection<EntityID> deleted, Collection<Command> heard) {
        if (location() instanceof Road) {
            Road r = (Road)location();
            EntityID nearest = getNearestBlockade();
            if (nearest != null) {
                sendClear(time, nearest);
                return;
            }
        }
        if (target == null) {
            LOG.info(me() + " has nothing to do.");
            return;
        }
        List<EntityID> path = search.breadthFirstSearch(location(), target);
        if (path != null) {
            sendMove(time, path);
            return;
        }
        else {
            LOG.info(me() + " couldn't plan a path to target.");
        }
    }

    @Override
    public String[] getRequestedEntityURNs() {
        return new String[] {StandardEntityURN.POLICE_FORCE.name()};
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
    */
    protected StandardEntity location() {
        PoliceForce me = me();
        return me.getPosition(model);
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        search = new SampleSearch(model, false);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled police force";
        }
        return "Human controlled police force " + getID() + (target == null ? " (no target)" : " target: road " + target.getID() + " with " + (target.isBlockadesDefined() ? " unknown" : String.valueOf(target.getBlockades().size())) + " blockades");
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
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                StandardEntity entity = model.getEntity(blockadeID);
                Pair<Integer, Integer> location = entity.getLocation(model);
                if (location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if(best == null || distance < bestDistance) {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        return best;
    }
}

