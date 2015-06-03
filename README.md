ATLauncher
====================================

[![Build Status](https://build.atlcdn.net/buildStatus/icon?job=ATLauncher 3.3.0.0)](https://build.atlcdn.net/job/ATLauncher%203.3.0.0/)

### What is it?

ATLauncher is a Launcher for Minecraft which integrates multiple different ModPacks to allow you to download and install ModPacks easily and quickly.

### The ATLauncher team

* [RyanTheAllmighty](https://github.com/RyanTheAllmighty) - [Twitter](https://twitter.com/RyanAllmighty1) - Project lead/owner
* [Laceh](https:///github.com/s0cks) - [Twitter](https://twitter.com/Asyncronous100)

### Links

* Facebook: https://www.facebook.com/ATLauncher
* Forums: https://forums.atlauncher.com
* IRC channel: #ATLauncher on EsperNet
* Reddit: http://www.reddit.com/r/ATLauncher
* Twitter: https://twitter.com/ATLauncher
* Website: https://www.atlauncher.com

### Coding Standards & Styling Guidelines

Please see the [STYLE.md](STYLE.md) file for coding standards and style guidelines.

### Contributing to ATLauncher

If you wish to contribute to ATLauncher in any way, take a look at [CONTRIBUTING.md](CONTRIBUTING.md)

### Testing

Please see the [TESTING.md](TESTING.md) file for information on how we write tests.

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

### Plugging In Your Data

To get started with the code and plug in your own data, you need to create a src/main/java/com/atlauncher/data/Constants.java file. Below is a starter to get you going:

    package com.atlauncher.data;
    
    import com.atlauncher.data.version.LauncherVersion;

    public class Constants {

    public static final LauncherVersion VERSION = new LauncherVersion(1, 0, 0, 0, 1);
    public static final String API_BASE_URL = "";
    public static final String PASTE_CHECK_URL = "";
    public static final String PASTE_API_URL = "";
    public static final Server[] SERVERS = new Server[] { new Server("Test", "my.file.server.com", true, true) };
    public static final String LAUNCHER_NAME = "MyLauncher";
    
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
This is an array of [Server](https://github.com/ATLauncher/ATLauncher/blob/master/src/main/java/com/atlauncher/data/Server.java) type elements the launcher uses as a base to download files.

#### LAUNCHER_NAME
This is the name of the launcher.

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

If you have questions please don't hesitate to [contact us](https://www.atlauncher.com/contact-us/)

### License

This work is licensed under the GNU General Public License v3.0. To view a copy of this license, visit http://www.gnu.org/licenses/gpl-3.0.txt.

A simple way to keep in terms of the license is by forking this repository and leaving it open source under the same license. We love free software, seeing people use our code and then not share the code, breaking the license, is saddening. So please take a look at the license and respect what we're doing.

Also, while we cannot enforce this under the license, you cannot use our CDN/files/assets/modpacks on your own launcher. Again we cannot enforce this under the license, but needless to say, we'd be very unhappy if you did that and really would like to leave cease and desist letters as a last resort.

If you have any questions or concerns as to the license or what we consider to be good and bad in your clone/fork, please use the contact link in the section right above this one.
