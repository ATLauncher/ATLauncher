#!/bin/bash

# When setting the memory below make sure to include the amount of ram letter. M = MB, G = GB. Don't use 1GB for example, it's 1G
MEMORY="2G"

# The path to the Java to use. Wrap in double quotes ("/opt/jre-17/bin/java"). Use "java" to point to system default install.
JAVAPATH="java"

# Any additional arguments to pass to Java such as Metaspace, GC or anything else
JVMARGS=""

# Don't edit past this point

cd "`dirname "$0"`"

LAUNCHARGS="$@"
# Launcher can specify path to java using a custom token
if [ "$1" = "ATLcustomjava" ]; then
    LAUNCHARGS="${@:2}"

    echo "Using launcher provided Java from $2"
    JAVAPATH="$2"
fi

echo
echo "Printing Java version, if the Java version doesn't show below, your Java path is incorrect"
$JAVAPATH -version
echo

echo "Launching %%SERVERJAR%% with '$MEMORY' max memory, jvm args '$JVMARGS' and arguments '$LAUNCHARGS'"

$JAVAPATH -Xmx$MEMORY $JVMARGS %%ARGUMENTS%% %%LOG4SHELLARGUMENTS%% -jar %%SERVERJAR%% "$LAUNCHARGS"
read -n1 -r -p "Press any key to close..."
