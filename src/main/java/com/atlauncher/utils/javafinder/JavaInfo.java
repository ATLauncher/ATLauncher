/*
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 */
package com.atlauncher.utils.javafinder;

import com.atlauncher.utils.Java;

/**
 * Helper struct to hold information about one installed java version
 ****************************************************************************/
public class JavaInfo {
    public String path; // ! Full path to java.exe executable file
    public String version; // ! Version string. "Unkown" if the java process returned non-standard version
                           // string
    public Integer majorVersion; // The major version
    public boolean is64bits; // ! true for 64-bit javas, false for 32

    /**
     * Calls 'javaPath -version' and parses the results
     *
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo(String javaPath) {
        String versionInfo = RuntimeStreamer.execute(new String[] { javaPath, "-version" });
        String[] tokens = versionInfo.split("\"");

        if (tokens.length < 2) {
            this.version = "Unkown";
        } else {
            this.version = tokens[1];
            this.majorVersion = Java.parseJavaVersionNumber(this.version);
        }

        this.is64bits = versionInfo.toUpperCase().contains("64-BIT");
        this.path = javaPath;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString() {
        return this.path + " (" + (this.is64bits ? "64-bit" : "32-bit") + ")";
    }
}
