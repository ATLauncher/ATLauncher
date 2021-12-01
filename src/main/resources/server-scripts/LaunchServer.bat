@ECHO OFF

:: When setting the memory below make sure to include the amount of ram letter. M = MB, G = GB. Don't use 1GB for example, it's 1G ::

:: This is 64-bit memory ::
set memsixtyfour=2G

:: This is 32-bit memory - maximum 1.2G ish::
set memthirtytwo=1G

:: The path to the Java to use. Wrap in double quotes ("C:\Path\To\Java\bin\java"). Use "java" to point to system default install.
set javapath="java"

:: Any additional arguments to pass to Java such as Metaspace, GC or anything else
set jvmargs=""

:: Don't edit past this point ::

if $SYSTEM_os_arch==x86 (
    echo OS is 32
    set mem=%memthirtytwo%
) else (
    echo OS is 64
    set mem=%memsixtyfour%
)

%javapath% -version

echo Launching %%SERVERJAR%% with '%mem%' max memory, jvm args '%jvmargs%' and arguments '%*'

:: add nogui to the end of this line to disable the gui ::
%javapath% -Xmx%mem% %jvm_args% %%ARGUMENTS%% -jar %%SERVERJAR%% %*
PAUSE

