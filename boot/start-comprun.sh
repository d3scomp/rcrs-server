#! /bin/bash

. functions.sh

processArgs $*

trap terminate INT
function terminate(){
	echo "Killing the tmux server $TMUX_SERVER"
	tmux -L $TMUX_SERVER kill-server
	exit 0
}

# Delete old logs
rm -f $LOGDIR/*.log

# Print out current tmux session
echo "Tmux server for this simulation is $TMUX_SERVER"
echo "To see or to kill the sessions Type:"
echo
echo "tmux -L $TMUX_SERVER list-sessions"
echo "tmux -L $TMUX_SERVER kill-server"
echo

#startGIS
startKernel --nomenu --autorun --nogui
startSims --nogui
#startSims --nogui --noviewer

echo "Start your agents"
waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

terminate