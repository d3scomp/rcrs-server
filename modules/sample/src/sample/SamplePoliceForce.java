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
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.misc.Pair;

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
	Pair<Integer, Integer> l = me().getLocation(world);

	//System.err.println(((Area)location).getNearlestBlockade(l.first(), l.second(), world));
	if(location instanceof Area && ((Area)location).getNearlestBlockade(l.first(), l.second(), world)!=null) {
	//if(location instanceof Area && ((Area)location).getBlockadeList().size()>0) {
	    EntityID blockade_id = ((Area)location).getNearlestBlockade(l.first(), l.second(), world);
            AKClear clear = new AKClear(getID(), time, blockade_id);
            //System.out.println(me() + " clear road: " + clear);
            //System.err.println(me() + ":" + location + " clear road: " + clear);
	    List<EntityID> bl = ((Area)location).getNearBlockadeList(world);
	    System.err.println(bl+", clear: "+blockade_id);

            send(clear);
	    return ;
	}

        List<EntityID> path = null;
        // Plan a path to a blocked area

        path = search.breadthFirstSearch(location(), getBlockedAreas());
        if (path != null) {
            AKMove move = new AKMove(getID(), time, path);
            System.out.println(me() + " moving to road: " + move);
            send(move);
	    return;
        }
        System.out.println(me() + " couldn't plan a path to a blocked road.");


	if(last_path!=null && last_path.size()>1 && last_path.indexOf(location.getID())!=-1)
	    for(path=last_path; !path.get(0).equals(location.getID()); ) path.remove(0);
	else {
	    path = randomWalk();
	}

	send(new AKMove(getID(), time, path));
	last_path = path;
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Area> getBlockedAreas() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityType.ROAD);
        List<Area> result = new ArrayList<Area>();
        for (StandardEntity next : e) {
            if (next instanceof Area) {
                Area a = (Area)next;
                if (a.getBlockadeList().size() > 0) {
                    result.add(a);
                }
            }
        }
        return result;
    }
}