#! /bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startGIS
startKernel
startSims

echo "Start your agents"
waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $KERNEL $VIEWER $MISC $TRAFFIC $FIRE $BLOCKADES $COLLAPSE $GIS $IGNITION $CIVILIAN
