package ignition;

import rescuecore2.config.Config;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import org.uncommons.maths.random.PoissonGenerator;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   An IgnitionModel that ignites unburnt buildings in a random order. The number of ignitions per timestep is drawn from a Poisson distribution.
*/
public class RandomIgnitionModel implements IgnitionModel {
    private static final Log LOG = LogFactory.getLog(RandomIgnitionModel.class);

    private static final String MEAN_KEY = "ignition.random.lambda";

    private PoissonGenerator generator;
    private Iterator<Building> it;

    /**
       Construct a RandomIgnitionModel.
       @param world The world model.
       @param config The system configuration.
    */
    public RandomIgnitionModel(StandardWorldModel world, Config config) {
        List<Building> unburnt = new ArrayList<Building>();
        for (StandardEntity next : world) {
            if (next instanceof Building) {
                unburnt.add((Building)next);
            }
        }
        Collections.shuffle(unburnt, config.getRandom());
        double mean = config.getFloatValue(MEAN_KEY);
        generator = new PoissonGenerator(mean, config.getRandom());
        it = unburnt.iterator();
    }

    @Override
    public Set<Building> findIgnitionPoints(StandardWorldModel world, int time) {
        Set<Building> result = new HashSet<Building>();
        if (it.hasNext()) {
            int number = generator.nextValue();
            LOG.debug("Igniting " + number + " buildings");
            for (int i = 0; i < number && it.hasNext(); ++i) {
                result.add(it.next());
            }
        }
        return result;
    }
}