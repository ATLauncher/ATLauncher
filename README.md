ATLauncher
====================================

### What is it?

ATLauncher is a Launcher for Minecraft which integrates multiple different ModPacks to allow you to download and install ModPacks easily and quickly.


### Links
[ATLauncher Website](http://www.atlauncher.com)

[ATLauncher Facebook](http://www.facebook.com/ATLauncher)

[ATLauncher Reddit](http://www.reddit.com/r/ATLauncher)

[ATLauncher Twitter](http://twitter.com/ATLauncher)


### Coding Standards

+ Please keep all line lengths to 120 characters and use 4 spaces rather than tab characters
+ Please keep all variables at the top of the class
+ Please keep all inner classes at the bottom
+ Please don't use star imports
+ Please mark all classes that are to be de/serialized with Gson with the @Json annotation for other developers
+ Please use the IntelliJ-Coding-Style.xml for the project (if using IntelliJ) in order to keep all formatting consistent
+ Please update the CHANGELOG.md file when fixing/adding something so it's easier to keep track of than git commits. Feel free to add in a 'by MyUsername' to the end of the changes you've made.
+ Please don't do large commits. My preference is a single commit for a single fix/addition rather than bundled up commits.

### Building

#### Windows

##### Requirements

###### Java Development Kit (JDK)

Download and install the latest version from [Oracle's Java Downloads page](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html).

###### Apache Maven

Install Apache Maven via the official [Apache Maven Install Docs](http://maven.apache.org/download.cgi#Installation).

###### launch4j

Download and install [launch4j](http://sourceforge.net/projects/launch4j/files/launch4j-3/3.1.0-beta2/).

Make sure to add the directory containing launch4jc to your executable path which for me on 64bit Windows was:

```
C:\Program Files (x86)\Launch4j
```

#### Linux

Install the Java development Kit, Apache Maven and Launch4j packages for your Linux distribution:

Arch Linux Build Dependencies:
```
yaourt -S maven launch4j
```

### Plugging In Your Data

To get started with the code and plug in your own data, you need to create a src/main/java/com/atlauncher/data/Constants.java file. Below is a starter to get you going:

    package com.atlauncher.data;

    public class Constants {

    public static final LauncherVersion VERSION = new LauncherVersion(1, 0, 0, 0, 1);
    public static final String API_BASE_URL = "";
    public static final String PASTE_CHECK_URL = "";
    public static final String PASTE_API_URL = "";
    public static final Server[] SERVERS = new Server[] { new Server("Test", "my.file.server.com", true, true) };
    
    }

See below for explanations as to what each constant means.

#### VERSION
This is a LauncherVersion object passed in the reserved, major, minor, revision ints for this version of the launcher. See the 'Versioning System' section below.

#### API_BASE_URL
This is a link to your server side API for processing of leaderboard times and pack installs. This is optional and can be removed. We do not give implementation code, this is your own doing.

#### PASTE_CHECK_URL
This is a link to the url where an instance of [stikked](https://github.com/claudehohl/Stikked) is running (For instance http://www.mypaste.com) this is how the launcher knows if the paste was successful by checking the response from the API for the url of the software.

*Please note that the domain given above IS NOT REAL. You must install [stikked](https://github.com/claudehohl/Stikked) on your own domain and reference it, the domain is only there as an example of what a valid value is.*

#### PASTE_API_URL
This is a link to the create api command for the instance of [stikked](https://github.com/claudehohl/Stikked) is running (For instance http://www.mypaste.com/api/create/)

*Please note that the domain given above IS NOT REAL. You must install [stikked](https://github.com/claudehohl/Stikked) on your own domain and reference it, the domain is only there as an example of what a valid value is.*

#### SERVERS
This is an array of Server type elements the launcher uses as a base to download files.

#### How to make your data

To make the data the Launcher needs you will need to figure out your own server side way of doing that. You can create a system to do it automatically or you can manually do it by just popping the files on the server. The best way to get the file structure and contents is to examine the source code and the ATLauncher files it downloads.

### Versioning System

Starting with version 3.2.1.0 a new versioning system was put into place. It works off the following:

Reserved.Major.Minor.Revision.Build

So for 3.2.1.0.0 the major number is 2 and minor number is 1 and revision number is 0. Reserved is used as a base, only incremented on complete rewrites. The build number is optional and should be 0 on releases.

Major should be incremented when large changes/features are made.

Minor should be incremented when small changes/features are made.

Revision should be incremented when there are no new features and only contains bug fixes for the previous minor.

Build is used for beta releases allowing you to have higher version numbers but force users to update when the real release comes.

### Need Help/Have Questions?

If you have questions please don't hesitate to [contact us](http://www.atlauncher.com/contactus/)

### License

This work is licensed under the GNU General Public License v3.0. To view a copy of this license, visit http://www.gnu.org/licenses/gpl-3.0.txt.
