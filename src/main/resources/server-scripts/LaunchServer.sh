#!/bin/bash
java -version
java -Xmx2G %%ARGUMENTS%% %%LOG4SHELLARGUMENTS%% -jar %%SERVERJAR%% "$@"
read -n1 -r -p "Press any key to close..."
