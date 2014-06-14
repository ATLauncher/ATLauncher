ATLauncher
====================================

### What is it?

ATLauncher is a Launcher for Minecraft which integrates multiple different ModPacks to allow you to download and install ModPacks easily and quickly.


### Links
[ATLauncher Website](http://www.atlauncher.com)

[ATLauncher Facebook](http://www.facebook.com/ATLauncher)

[ATLauncher Reddit](http://www.reddit.com/r/ATLauncher)

[ATLauncher Twitter](http://twitter.com/ATLauncher)


### Things to Note

Please note that I (RyanTheAllmighty) have created this alone up until now and being a 1st year Computer Science student, my coding may not be the best, most accurate or efficient, so please keep that in mind.

Other than that, I am releasing this as an Open Source project in hopes that others may contribute to better our Launcher and because I think Open Source is a good thing.

### Coding Standards

Please keep all line lengths to 100 characters and use 4 spaces rather than tab characters

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

### Pluging In Your Data

To get started with the code and plug in your own data, you need to create a src/main/java/com/atlauncher/data/Constants.java file. Below is a starter to get you going:

    package com.atlauncher.data;

    public class Constants {

    public static final String VERSION = "";
    public static final String API_BASE_URL = "";
    public static final String PASTE_CHECK_URL = "";
    public static final String PASTE_API_URL = "";
    public static final Server[] SERVERS = new Server[] { new Server("Test", "my.file.server.com", true) };
    
    }

See below for exmplanations as to what each constant means.

#### API_BASE_URL
This is a link to your server side API for processing of leaderboard times and pack installs. This is optional and can be removed. We do not give implementation code, this is your own doing.

#### PASTE_CHECK_URL
This is a link to the url where an instance of [stikked](https://github.com/claudehohl/Stikked) is running (For instance http://www.mypaste.com) this is how the launcher knows if the paste was successful by checking the response from the API for the url of the software.

#### PASTE_API_URL
This is a link to the create api command for the instance of [stikked](https://github.com/claudehohl/Stikked) is running (For instance http://www.mypaste.com/api/create/)

#### SERVERS
This is an array of Server type elements the launcher uses as a base to download files.

#### How to make your data

To make the data the Launcher needs you will need to figure out your own server side way of doing that. You can create a system to do it automatically or you can manually do it by just popping the files on the server. The best way to get the file structure and contents is to examine the source code and the ATLauncher files it downloads.

### Need Help/Have Questions?

If you have questions or need any help please don't hesitate to email sourcecode@atlauncher.com

### License

We have released this code under a Creative Commons license. In order to use this work, you must share the work under the same license as well as give proper attribution. In order to attribute us correctly you must provide a link to this original repository as well as give attribution to our website at http://www.atlauncher.com

If you do use our code as a base, we require that you don't use our data from our servers and instead plug in your own data. Our servers are tuned to only accept data from our Launcher and block all other traffic. So it's imperative that you use your own data in your forked code.

You must also establish yourself as not being a part of our Launcher, and should rebrand. The one and only official ATLauncher is on [our website](http://www.atlauncher.com)

![CC SA](http://i.creativecommons.org/l/by-sa/3.0/88x31.png)

This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
