Welcome.

For a visual guide on how to run servers, please visit our wiki at https://wiki.atlauncher.com/getting-started/starting-a-server/

You can run this server in 2 ways.

The recommended way is to use ATLauncher's built in server launcher. This will launch the server with the correct Java version required for this server.

Alternatively you can run the server using the LaunchServer scripts.

If you're on Windows, you can run the LaunchServer.bat file. If you're on Linux or Mac, you can run the LaunchServer.sh file.





## Allocating More Memory

Visual guide available at https://wiki.atlauncher.com/guides/changing-servers-memory/

In order to change the memory allocated to the server, you can edit the LaunchServer.bat/sh file and changing the line near the top.

For Windows, it should look like this:

set maxmemory=2G

And to change the memory to say 8GB, you would change it to:

set maxmemory=8G

Or if you're on Linux or Mac, you can change the line near the top that says:

MEMORY="2G"

And to change the memory to say 8GB, you would change it to:

MEMORY="8G"





## Changing the Java version

Visual guide available at https://wiki.atlauncher.com/guides/changing-servers-java-version/

If you're not running the server through ATLauncher, you can change the Java version used to launch the server by editing the LaunchServer.bat/sh file and changing the line near the top.

For Windows, it should look like this:

set javapath="java"

And changing it to something like:

set javapath="C:\Program Files\Java\jre-17.0.3\bin\java.exe"

Or if you're on Linux or Mac, you can change the line near the top that says:

JAVAPATH="java"

And changing it to something like:

JAVAPATH="/usr/lib/jvm/jre-17.0.3/bin/java"





## Adding JVM arguments

Visual guide available at https://wiki.atlauncher.com/guides/changing-servers-jvm-arguments/

In order to add JVM arguments to the server, you can edit the LaunchServer.bat/sh file and changing the line near the top.

For Windows, it should look like this:

set jvmargs=""

Or if you're on Linux or Mac, you can change the line near the top that says:

JVMARGS=""

You can add JVM arguments within the quotes. But note that you should not add any -Xmx arguments as that is already handled by the maxmemory/MEMORY variable.