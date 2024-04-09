@ECHO OFF
setlocal enabledelayedexpansion

:: When setting the memory below make sure to include the amount of ram letter. M = MB, G = GB. Don't use 1GB for example, it's 1G

:: This is the max memory
set maxmemory=2G

:: The path to the Java to use. Use "java" to point to system default install.
set javapath="java"

:: Any additional arguments to pass to Java such as Metaspace, GC or anything else
set jvmargs=""

::----------------------------::
:: Don't edit past this point ::
::----------------------------::

set launchargs=%*
:: Launcher can specify path to java using a custom token
IF "%1"=="ATLcustomjava" (
    for /f "tokens=2,* delims= " %%a in ("%*") do set launchargs=%%b

    echo Using launcher provided Java from %2
    SET javapath="%2"
)

:: Remove all existing double quotes from javapath
set "javapath=!javapath:"=!"

:: Add one set of double quotes around javapath
set "javapath="!javapath!""

echo.
echo Printing Java info below, if the Java version doesn't show below, your Java path is incorrect
echo -----------------------
echo Java path is %javapath%
%javapath% -version
echo.

echo Launching below command
echo -----------------------
echo %javapath% -Xmx%maxmemory% %jvmargs% %%ARGUMENTS%% %%LOG4SHELLARGUMENTS%% -jar %%SERVERJAR%% %launchargs%
echo.
%javapath% -Xmx%maxmemory% %jvmargs% %%ARGUMENTS%% %%LOG4SHELLARGUMENTS%% -jar %%SERVERJAR%% %launchargs%
PAUSE

