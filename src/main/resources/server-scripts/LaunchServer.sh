#!/bin/bash
java -version
java -Xmx2G %%ARGUMENTS%% -jar %%SERVERJAR%% "$@"
read -n1 -r -p "Press any key to close..."
