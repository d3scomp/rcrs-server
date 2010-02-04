package injury;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.misc.RoundingNumberGenerator;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardPropertyURN;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.BinomialGenerator;
import org.uncommons.maths.random.DiscreteUniformGenerator;
import org.uncommons.maths.random.PoissonGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.Maths;

/**
   A simulator that computes injuries caused by collapsing buildings.
*/
public class InjurySimulator extends StandardSimulator {
    private static final String[] CODES = {"wood", "steel", "concrete"};

    private static final String BURIEDNESS_PREFIX = "injury.buriedness.";
    private static final String SLIGHT_SUFFIX = ".slight";
    private static final String MODERATE_SUFFIX = ".moderate";
    private static final String SEVERE_SUFFIX = ".severe";
    private static final String DESTROYED_SUFFIX = ".destroyed";

    private static final String CIVILIAN_RATE_KEY = "injury.civilians.frequency";

    private static final int SLIGHT = 25;
    private static final int MODERATE = 50;
    private static final int SEVERE = 75;
    private static final int DESTROYED = 100;

    private static final String GAUSSIAN = "gaussian";
    private static final String UNIFORM = "uniform";
    private static final String BINOMIAL = "binomial";    

    private BuriednessStats[] stats;
    private Set<Building> changedBuildings;

    @Override
    protected void postConnect() {
        super.postConnect();
        changedBuildings = new HashSet<Building>();
        stats = new BuriednessStats[CODES.length];
        for (int i = 0; i < CODES.length; ++i) {
            stats[i] = new BuriednessStats(i, config);
        }
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
        changedBuildings.clear();
        ChangeSet changes = u.getChangeSet();
        for (EntityID next : changes.getChangedEntities()) {
            Entity e = model.getEntity(next);
            if (e instanceof Building) {
                Building b = (Building)e;
                // Has brokenness changed?
                Property p = changes.getChangedProperty(next, StandardPropertyURN.BROKENNESS.name());
                if (p != null) {
                    changedBuildings.add(b);
                }
            }
        }
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        // For each newly collapsed building see if anyone should be injured.
        List<NewCivInfo> info = new ArrayList<NewCivInfo>();
        for (Building next : changedBuildings) {
            int civs = countCivs(next);
            System.out.println("Putting " + civs + " civs in " + next);
            for (int i = 0; i < civs; ++i) {
                int buriedness = calculateBuriedness(next);
                System.out.println("Civ " + i + " buriedness: " + buriedness);
                info.add(new NewCivInfo(next.getID(), buriedness));
            }
        }
        int count = info.size();
        try {
            List<EntityID> ids = requestNewEntityIDs(count);
            Iterator<NewCivInfo> it = info.iterator();
            Iterator<EntityID> ix = ids.iterator();
            for (int i = 0; i < count; ++i) {
                NewCivInfo n = it.next();
                Civilian civ = new Civilian(ix.next());
                if (n.getBuriedness() > 0) {
                    civ.setBuriedness(n.getBuriedness());
                    civ.setPosition(n.getPosition());
                    civ.setPositionExtra(0);
                    changes.addChange(civ, civ.getBuriednessProperty());
                    changes.addChange(civ, civ.getPositionProperty());
                    changes.addChange(civ, civ.getPositionExtraProperty());
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Injury simulator";
    }

    private int calculateBuriedness(Building b) {
        if (!b.isBuildingCodeDefined()) {
            return 0;
        }
        int code = b.getBuildingCode();
        return Maths.restrictRange(stats[code].computeBuriedness(b), 0, Integer.MAX_VALUE);
    }

    private int countCivs(Building b) {
        if (!b.isBuildingCodeDefined()) {
            return 0;
        }
        int code = b.getBuildingCode();
        return stats[code].countCivs(b);
    }

    private static NumberGenerator<Integer> makeGenerator(List<String> configEntry, Config config) {
        String type = configEntry.remove(0);
        if (UNIFORM.equals(type)) {
            return makeUniformGenerator(configEntry, config);
        }
        if (GAUSSIAN.equals(type)) {
            return makeGaussianGenerator(configEntry, config);
        }
        if (BINOMIAL.equals(type)) {
            return makeBinomialGenerator(configEntry, config);
        }
        throw new IllegalArgumentException("Don't know how to deal with generator type " + type);
    }

    private static NumberGenerator<Integer> makeGaussianGenerator(List<String> entries, Config config) {
        double mean = Double.parseDouble(entries.remove(0));
        double sd = Double.parseDouble(entries.remove(0));
        return new RoundingNumberGenerator(new GaussianGenerator(mean, sd, config.getRandom()));
    }

    private static NumberGenerator<Integer> makeBinomialGenerator(List<String> entries, Config config) {
        int n = Integer.parseInt(entries.remove(0));
        double p = Double.parseDouble(entries.remove(0));
        return new BinomialGenerator(n, p, config.getRandom());
    }

    private static NumberGenerator<Integer> makeUniformGenerator(List<String> entries, Config config) {
        int min = Integer.parseInt(entries.remove(0));
        int max = Integer.parseInt(entries.remove(0));
        return new DiscreteUniformGenerator(min, max, config.getRandom());
    }

    private static class NewCivInfo {
        private EntityID position;
        private int buriedness;

        public NewCivInfo(EntityID position, int buriedness) {
            this.position = position;
            this.buriedness = buriedness;
        }

        EntityID getPosition() {
            return position;
        }

        int getBuriedness() {
            return buriedness;
        }
    }

    private static class BuriednessStats {
        private NumberGenerator<Integer> destroyed;
        private NumberGenerator<Integer> severe;
        private NumberGenerator<Integer> moderate;
        private NumberGenerator<Integer> slight;
        private double civilianRate;
        private AdjustableNumberGenerator<Double> civsRate;
        private NumberGenerator<Integer> civs;

        BuriednessStats(int code, Config config) {
            destroyed = makeGenerator(config.getArrayValue(BURIEDNESS_PREFIX + CODES[code]  + DESTROYED_SUFFIX), config);
            severe = makeGenerator(config.getArrayValue(BURIEDNESS_PREFIX + CODES[code]  + SEVERE_SUFFIX), config);
            moderate = makeGenerator(config.getArrayValue(BURIEDNESS_PREFIX + CODES[code]  + MODERATE_SUFFIX), config);
            slight = makeGenerator(config.getArrayValue(BURIEDNESS_PREFIX + CODES[code]  + SLIGHT_SUFFIX), config);
            civilianRate = config.getFloatValue(CIVILIAN_RATE_KEY);
            civsRate = new AdjustableNumberGenerator<Double>(civilianRate);
            civs = new PoissonGenerator(civsRate, config.getRandom());
        }

        int countCivs(Building b) {
            civsRate.setValue(b.getTotalArea() * civilianRate);
            return civs.nextValue();
        }

        int computeBuriedness(Building b) {
            if (!b.isBrokennessDefined()) {
                return 0;
            }
            int damage = b.getBrokenness();
            if (damage < SLIGHT) {
                return 0;
            }
            if (damage < MODERATE) {
                return slight.nextValue();
            }
            if (damage < SEVERE) {
                return moderate.nextValue();
            }
            if (damage < DESTROYED) {
                return severe.nextValue();
            }
            return destroyed.nextValue();
        }
    }
}