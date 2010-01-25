DIR=`pwd`
BASEDIR="`cd .. && pwd`"

# Wait for a regular expression to appear in a file.
# $1 is the log to check
# $2 is the regex to wait for
# $3 is the optional output frequency. Messages will be output every n sleeps. Default 1.
# $4 is the optional sleep time. Defaults to 1 second.
function waitFor {
    SLEEP_TIME=1
    FREQUENCY=1
    if [ ! -z "$3" ]; then
        FREQUENCY=$3
    fi
    if [ ! -z "$4" ]; then
        SLEEP_TIME=$4
    fi
    F=$FREQUENCY
    echo "Waiting for '$1' to exist..."
    while [[ ! -e $1 ]]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$1' to exist..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
    echo "Waiting for '$2'..."
    while [ -z "`grep \"$2\" \"$1\"`" ]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$2'..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
}

# Make a classpath argument by looking in a directory of jar files.
# Positional parameters are the directories to look in
function makeClasspath {
    RESULT=""
    while [[ ! -z "$1" ]]; do
        for NEXT in $1/*.jar; do
            RESULT="$RESULT:$NEXT"
        done
        shift
    done
    CP=${RESULT#:}
}

# Print the usage statement
function printUsage {
    echo "Usage: $0 [options]"
    echo "Options"
    echo "======="
    echo "-m    --map       <mapdir>      Set the map directory. Default is \"$BASEDIR/maps/Kobe\""
    echo "-l    --log       <logdir>      Set the log directory. Default is \"logs\""
    echo "-s    --timestamp               Append a timestamp, the team name and map name to the log directory name"
    echo "-t    --team      <teamname>    Set the team name. Default is \"\""
}

# Process arguments
function processArgs {
    LOGDIR="logs"
    MAP="$BASEDIR/maps/Kobe"
    TEAM=""
    TIMESTAMP_LOGS=""

    while [[ ! -z "$1" ]]; do
        case "$1" in
            -m | --map)
                MAP="$2"
                shift 2
                ;;
            -l | --log)
                LOGDIR="$2"
                shift 2
                ;;
            -t | --team)
                TEAM="$2"
                shift 2
                ;;
            -s | --timestamp)
                TIMESTAMP_LOGS="yes";
                shift
                ;;
            -h | --help)
                printUsage
                exit 1;
                ;;
            
            *)
                echo "Unrecognised option: $1"
                printUsage
                exit 1
                ;;
        esac
    done

    if [ -z $MAP ] ; then
        printUsage
        exit 1
    fi
    if [ ! -d $MAP ] ; then
        echo "$MAP is not a directory"
        printUsage
        exit 1
    fi
    if [[ ( ! -e $MAP/road.bin ) || ( ! -e $MAP/node.bin ) || ( ! -e $MAP/building.bin ) || ( ! -e $MAP/gisini.txt ) ]]; then
        echo "$MAP is not a valid map directory"
        printUsage
        exit 1
    fi

    if [ ! -z "$TIMESTAMP_LOGS" ] ; then
        TIME="`date +%m%d-%H%M%S`"
        MAPNAME="`basename $MAP`"
        if [ -z "$TEAM" ]; then
            LOGDIR="$LOGDIR/$TIME-$MAPNAME"
        else
            LOGDIR="$LOGDIR/$TIME-$TEAM-$MAPNAME"
        fi
    fi
    LOGDIR=`readlink -f $LOGDIR`
    mkdir -p $LOGDIR
}

# Start the GIS
function startGIS {
    makeClasspath $BASEDIR/jars $BASEDIR/lib
    GIS_OPTIONS="./config/gis2.cfg"
    xterm -T gis2 -e "java -cp $CP gis2.Main $GIS_OPTIONS 2>&1 | tee $LOGDIR/gis2.log" &
    GIS=$!
    waitFor $LOGDIR/gis2.log "waiting for the kernel"
}

# Start the kernel
function startKernel {
    KERNEL_OPTIONS="-c $DIR/config --gis.map.dir=$MAP --kernel.logname=$LOGDIR/rescue.log --kernel.simulators.auto= --kernel.viewers.auto= --kernel.agents.auto= --kernel.gis.auto=rescuecore2.standard.kernel.StandardWorldModelCreator --kernel.perception.auto=rescuecore2.standard.kernel.StandardPerception --kernel.communication.auto=rescuecore2.standard.kernel.ChannelCommunicationModel --just-run $*"
    makeClasspath $BASEDIR/jars $BASEDIR/lib
    xterm -T kernel -e "java -cp $CP kernel.StartKernel $KERNEL_OPTIONS 2>&1 | tee $LOGDIR/kernel.log" &
    KERNEL=$!
    # Wait for the kernel to start
    waitFor $LOGDIR/kernel.log "Listening for connections"
}

# Start the viewer and simulators
function startSims {
    makeClasspath $BASEDIR/lib
    # Viewer
    xterm -T viewer -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleViewer 2>&1 | tee $LOGDIR/viewer.log" &
    VIEWER=$!
    # Simulators
    xterm -T misc -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/misc.jar rescuecore2.LaunchComponents misc.MiscSimulator 2>&1 | tee $LOGDIR/misc.log" &
    MISC=$!
    #xterm -T traffic -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic-old.jar rescuecore2.LaunchComponents traffic.TrafficSimulatorWrapper 2>&1 | tee $LOGDIR/traffic.log" &
    #TRAFFIC=$!
    xterm -T traffic -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic3.jar traffic3.Launch -log=stdout -mode=rcrs -rcrs.traffic3.setting=./config/traffic3.cfg 2>&1 | tee $LOGDIR/traffic3.log" &
    TRAFFIC=$!
    xterm -T fire -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/resq-fire.jar rescuecore2.LaunchComponents firesimulator.FireSimulatorWrapper 2>&1 | tee $LOGDIR/fire.log" &
    FIRE=$!
    xterm -T ignition -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/ignition.jar rescuecore2.LaunchComponents ignition.IgnitionSimulator 2>&1 | tee $LOGDIR/ignition.log" &
    IGNITION=$!
    xterm -T blockades -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/blockade.jar rescuecore2.LaunchComponents blockade.BlockadeSimulator 2>&1 | tee $LOGDIR/blockades.log" &
    BLOCKADES=$!
    xterm -T collapse -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/collapse.jar rescuecore2.LaunchComponents collapse.CollapseSimulator 2>&1 | tee $LOGDIR/collapse.log" &
    COLLAPSE=$!
    xterm -T civilian -e "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleCivilian*n 2>&1 | tee $LOGDIR/civilian.log" &
    CIVILIAN=$!
}
