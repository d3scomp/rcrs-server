package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent<PoliceForce> {

    private List<EntityID> last_path;

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
    protected void think(int time, Collection<EntityID> changed, Collection<Command> heard) {
        for (Command next : heard) {
            System.out.println(me() + " heard " + next);
        }
        // Am I on a blocked road?
        StandardEntity location = location();
        if (location instanceof Road && ((Road)location).isBlockDefined() && ((Road)location).getBlock() > 0) {
            sendClear(time, location.getID());
            return;
        }
        // Plan a path to a blocked road
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            AKMove move = new AKMove(getID(), time, path);
            System.out.println(me() + " moving to road: " + move);
            send(move);
	    return;
        }
        System.out.println(me() + " couldn't plan a path to a blocked road.");

	if(last_path!=null && last_path.size()>1 && last_path.indexOf(location.getID())!=-1)
	    for(path=last_path; !path.get(0).equals(location.getID()); ) path.remove(0);
	else
	    path = randomWalk();
	send(new AKMove(entityID, path, time));
	last_path = path;
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Road> result = new ArrayList<Road>();
        for (StandardEntity next : e) {
            if (next instanceof Road) {
                Road r = (Road)next;
                if (r.isBlockDefined() && r.getBlock() > 0) {
                    result.add(r);
                }
            }
        }
        return result;
    }
}