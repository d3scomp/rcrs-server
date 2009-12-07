package kernel;

import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.io.IOException;
import java.io.File;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.Constants;
import rescuecore2.Timestep;
import rescuecore2.score.ScoreFunction;

import rescuecore2.log.LogWriter;
import rescuecore2.log.FileLogWriter;
import rescuecore2.log.InitialConditionsRecord;
import rescuecore2.log.StartLogRecord;
import rescuecore2.log.EndLogRecord;
import rescuecore2.log.ConfigRecord;
import rescuecore2.log.PerceptionRecord;
import rescuecore2.log.CommandsRecord;
import rescuecore2.log.UpdatesRecord;
import rescuecore2.log.LogException;

/**
   The Robocup Rescue kernel.
 */
public class Kernel {
    //    private Config config;
    private Perception perception;
    private CommunicationModel communicationModel;
    private WorldModel<? extends Entity> worldModel;
    private LogWriter log;

    private Set<KernelListener> listeners;

    private Collection<AgentProxy> agents;
    private Collection<SimulatorProxy> sims;
    private Collection<ViewerProxy> viewers;
    private int time;
    private Timestep previousTimestep;

    private CommandFilter commandFilter;

    private TerminationCondition termination;
    private ScoreFunction score;
    private CommandCollector commandCollector;

    private boolean isShutdown;

    /**
       Construct a kernel.
       @param config The configuration to use.
       @param perception A perception calculator.
       @param communicationModel A communication model.
       @param worldModel The world model.
       @param commandFilter An optional command filter. This may be null.
       @param termination The termination condition.
       @param score The score function.
       @param collector The CommandCollector to use.
       @throws KernelException If there is a problem constructing the kernel.
    */
    public Kernel(Config config,
                  Perception perception,
                  CommunicationModel communicationModel,
                  WorldModel<? extends Entity> worldModel,
                  CommandFilter commandFilter,
                  TerminationCondition termination,
                  ScoreFunction score,
                  CommandCollector collector) throws KernelException {
        this.perception = perception;
        this.communicationModel = communicationModel;
        this.worldModel = worldModel;
        this.commandFilter = commandFilter;
        this.score = score;
        this.termination = termination;
        this.commandCollector = collector;
        listeners = new HashSet<KernelListener>();
        agents = new HashSet<AgentProxy>();
        sims = new HashSet<SimulatorProxy>();
        viewers = new HashSet<ViewerProxy>();
        time = 0;
        try {
            String logName = config.getValue("kernel.logname");
            System.out.println("Logging to " + logName);
            File logFile = new File(logName);
            if (logFile.getParentFile().mkdirs()) {
                System.out.println("Created log directory: " + logFile.getParentFile().getAbsolutePath());
            }
            if (logFile.createNewFile()) {
                System.out.println("Created log file: " + logFile.getAbsolutePath());
            }
            log = new FileLogWriter(logFile);
            log.writeRecord(new StartLogRecord());
            log.writeRecord(new InitialConditionsRecord(worldModel));
            log.writeRecord(new ConfigRecord(config));
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open log file for writing", e);
        }
        catch (LogException e) {
            throw new KernelException("Couldn't open log file for writing", e);
        }
        commandFilter.initialise(config, this);
        config.setValue(Constants.COMMUNICATION_MODEL_KEY, communicationModel.getClass().getName());
        config.setValue(Constants.PERCEPTION_KEY, perception.getClass().getName());

        score.initialise(worldModel, config);
        commandCollector.initialise(config);

        isShutdown = false;

        System.out.println("Kernel initialised");
        System.out.println("Perception module: " + perception);
        System.out.println("Communication module: " + communicationModel);
        System.out.println("Command filter: " + commandFilter);
        System.out.println("Score function: " + score);
        System.out.println("Termination condition: " + termination);
        System.out.println("Command collector: " + collector);
    }

    /**
       Add a KernelListener.
       @param l The listener to add.
    */
    public void addKernelListener(KernelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
       Remove a KernelListener.
       @param l The listener to remove.
    */
    public void removeKernelListener(KernelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Add an agent to the system.
       @param agent The agent to add.
    */
    public void addAgent(AgentProxy agent) {
        synchronized (this) {
            agents.add(agent);
        }
        fireAgentAdded(agent);
    }

    /**
       Remove an agent from the system.
       @param agent The agent to remove.
    */
    public void removeAgent(AgentProxy agent) {
        synchronized (this) {
            agents.remove(agent);
        }
        fireAgentRemoved(agent);
    }

    /**
       Get all agents in the system.
       @return An unmodifiable view of all agents.
    */
    public Collection<AgentProxy> getAllAgents() {
        synchronized (this) {
            return Collections.unmodifiableCollection(agents);
        }
    }

    /**
       Add a simulator to the system.
       @param sim The simulator to add.
    */
    public void addSimulator(SimulatorProxy sim) {
        synchronized (this) {
            sims.add(sim);
        }
        fireSimulatorAdded(sim);
    }

    /**
       Remove a simulator from the system.
       @param sim The simulator to remove.
    */
    public void removeSimulator(SimulatorProxy sim) {
        synchronized (this) {
            sims.remove(sim);
        }
        fireSimulatorRemoved(sim);
    }

    /**
       Get all simulators in the system.
       @return An unmodifiable view of all simulators.
    */
    public Collection<SimulatorProxy> getAllSimulators() {
        synchronized (this) {
            return Collections.unmodifiableCollection(sims);
        }
    }

    /**
       Add a viewer to the system.
       @param viewer The viewer to add.
    */
    public void addViewer(ViewerProxy viewer) {
        synchronized (this) {
            viewers.add(viewer);
        }
        fireViewerAdded(viewer);
    }

    /**
       Remove a viewer from the system.
       @param viewer The viewer to remove.
    */
    public void removeViewer(ViewerProxy viewer) {
        synchronized (this) {
            viewers.remove(viewer);
        }
        fireViewerRemoved(viewer);
    }

    /**
       Get all viewers in the system.
       @return An unmodifiable view of all viewers.
    */
    public Collection<ViewerProxy> getAllViewers() {
        synchronized (this) {
            return Collections.unmodifiableCollection(viewers);
        }
    }

    /**
       Run a single timestep.
       @throws InterruptedException If this thread is interrupted during the timestep.
       @throws KernelException If there is a problem executing the timestep.
       @throws LogException If there is a problem writing the log.
    */
    public void timestep() throws InterruptedException, KernelException, LogException {
        synchronized (this) {
            if (isShutdown) {
                return;
            }
            ++time;
            // Work out what the agents can see and hear (using the commands from the previous timestep).
            // Wait for new commands
            // Send commands to simulators and wait for updates
            // Collate updates and broadcast to simulators
            // Send perception, commands and updates to viewers
            Timestep nextTimestep = new Timestep(time);
            System.out.println("Timestep " + time);
            System.out.println("Sending agent updates");
            long start = System.currentTimeMillis();
            sendAgentUpdates(nextTimestep, previousTimestep == null ? new HashSet<Command>() : previousTimestep.getCommands());
            long perceptionTime = System.currentTimeMillis();
            System.out.println("Waiting for commands");
            Collection<Command> commands = waitForCommands(time);
            nextTimestep.setCommands(commands);
            log.writeRecord(new CommandsRecord(time, commands));
            long commandsTime = System.currentTimeMillis();
            System.out.println("Broadcasting commands");
            ChangeSet changes = sendCommandsToSimulators(time, commands);
            nextTimestep.setChangeSet(changes);
            log.writeRecord(new UpdatesRecord(time, changes));
            long updatesTime = System.currentTimeMillis();
            // Merge updates into world model
            worldModel.merge(changes);
            long mergeTime = System.currentTimeMillis();
            System.out.println("Broadcasting updates");
            sendUpdatesToSimulators(time, changes);
            sendToViewers(nextTimestep);
            long broadcastTime = System.currentTimeMillis();
            System.out.println("Computing score");
            double s = score.score(worldModel, nextTimestep);
            long scoreTime = System.currentTimeMillis();
            nextTimestep.setScore(s);
            System.out.println("Timestep " + time + " complete");
            System.out.println("Score: " + s);
            System.out.println("Perception took        : " + (perceptionTime - start) + "ms");
            System.out.println("Agent commands took    : " + (commandsTime - perceptionTime) + "ms");
            System.out.println("Simulator updates took : " + (updatesTime - commandsTime) + "ms");
            System.out.println("World model merge took : " + (mergeTime - updatesTime) + "ms");
            System.out.println("Update broadcast took  : " + (broadcastTime - mergeTime) + "ms");
            System.out.println("Score calculation took : " + (scoreTime - broadcastTime) + "ms");
            System.out.println("Total time             : " + (scoreTime - start) + "ms");
            fireTimestepCompleted(nextTimestep);
            previousTimestep = nextTimestep;
        }
    }

    /**
       Get the current time.
       @return The current time.
    */
    public int getTime() {
        synchronized (this) {
            return time;
        }
    }

    /**
       Get the world model.
       @return The world model.
    */
    public WorldModel<? extends Entity> getWorldModel() {
        return worldModel;
    }

    /**
       Shut down the kernel. This method will notify all agents/simulators/viewers of the shutdown.
    */
    public void shutdown() {
        synchronized (this) {
            if (isShutdown) {
                return;
            }
            System.out.println("Kernel is shutting down");
            for (AgentProxy next : agents) {
                next.shutdown();
            }
            for (SimulatorProxy next : sims) {
                next.shutdown();
            }
            for (ViewerProxy next : viewers) {
                next.shutdown();
            }
            try {
                log.writeRecord(new EndLogRecord());
                log.close();
            }
            catch (LogException e) {
                e.printStackTrace();
            }
            System.out.println("Kernel has shut down");
            isShutdown = true;
        }
    }

    /**
       Find out if the kernel has terminated.
       @return True if the kernel has terminated, false otherwise.
    */
    public boolean hasTerminated() {
        synchronized (this) {
            return isShutdown || termination.shouldStop(this);
        }
    }

    private void sendAgentUpdates(Timestep timestep, Collection<Command> commandsLastTimestep) throws InterruptedException, KernelException, LogException {
        perception.setTime(time);
        Map<AgentProxy, Collection<Command>> comms = communicationModel.process(time, agents, commandsLastTimestep);
        for (AgentProxy next : agents) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            ChangeSet visible = perception.getVisibleEntities(next);
            Collection<Command> heard = comms.get(next);
            EntityID id = next.getControlledEntity().getID();
            timestep.registerPerception(id, visible, heard);
            log.writeRecord(new PerceptionRecord(time, id, visible, heard));
            next.sendPerceptionUpdate(time, visible, heard);
        }
    }

    private Collection<Command> waitForCommands(int timestep) throws InterruptedException {
        Collection<Command> commands = commandCollector.getAgentCommands(agents, timestep);
        commandFilter.filter(commands);
        return commands;
    }

    /**
       Send commands to all simulators and return which entities have been updated by the simulators.
    */
    private ChangeSet sendCommandsToSimulators(int timestep, Collection<Command> commands) throws InterruptedException {
        for (SimulatorProxy next : sims) {
            next.sendAgentCommands(timestep, commands);
        }
        // Wait until all simulators have sent updates
        ChangeSet result = new ChangeSet();
        for (SimulatorProxy next : sims) {
            result.merge(next.getUpdates(timestep));
        }
        return result;
    }

    private void sendUpdatesToSimulators(int timestep, ChangeSet updates) throws InterruptedException {
        for (SimulatorProxy next : sims) {
            next.sendUpdate(timestep, updates);
        }
    }

    private void sendToViewers(Timestep timestep) {
        for (ViewerProxy next : viewers) {
            next.sendTimestep(timestep);
        }
    }

    private Set<KernelListener> getListeners() {
        Set<KernelListener> result;
        synchronized (listeners) {
            result = new HashSet<KernelListener>(listeners);
        }
        return result;
    }

    private void fireTimestepCompleted(Timestep timestep) {
        for (KernelListener next : getListeners()) {
            next.timestepCompleted(timestep);
        }
    }

    private void fireAgentAdded(AgentProxy agent) {
        for (KernelListener next : getListeners()) {
            next.agentAdded(agent);
        }
    }

    private void fireAgentRemoved(AgentProxy agent) {
        for (KernelListener next : getListeners()) {
            next.agentRemoved(agent);
        }
    }

    private void fireSimulatorAdded(SimulatorProxy sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorAdded(sim);
        }
    }

    private void fireSimulatorRemoved(SimulatorProxy sim) {
        for (KernelListener next : getListeners()) {
            next.simulatorRemoved(sim);
        }
    }

    private void fireViewerAdded(ViewerProxy viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerAdded(viewer);
        }
    }

    private void fireViewerRemoved(ViewerProxy viewer) {
        for (KernelListener next : getListeners()) {
            next.viewerRemoved(viewer);
        }
    }
}