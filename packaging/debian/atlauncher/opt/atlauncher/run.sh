#!/bin/bash

INSTDIR="${XDG_DATA_HOME-$HOME/.local/share}/atlauncher"

if [[ ! -d ${INSTDIR} ]]; then
    mkdir -p $INSTDIR
fi

cd $INSTDIR

if [[ ! -f ${INSTDIR}/ATLauncher.jar ]]; then
    wget "https://download.nodecdn.net/containers/atl/ATLauncher.jar" 2>&1
fi

java -jar ATLauncher.jar "$@"
