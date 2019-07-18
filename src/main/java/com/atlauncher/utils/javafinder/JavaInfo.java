/*
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 */
package com.atlauncher.utils.javafinder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.atlauncher.FileSystem;
import com.atlauncher.collection.Caching;
import com.atlauncher.utils.Java;

/**
 * Helper struct to hold information about one installed java version
 ****************************************************************************/
public class JavaInfo {
    public String path; // ! Full path to java.exe executable file
    public String rootPath; // ! Full path to install directory
    public String version; // ! Version string. "Unkown" if the java process returned non-standard version
                           // string
    public Integer majorVersion; // The major version
    public Integer minorVersion; // The minor version
    public boolean is64bits; // ! true for 64-bit javas, false for 32
    public boolean isRuntime; // if this is a runtime provided by ATLauncher
    private static final SoftReference<Caching.Cache<String, String>> versionInfos = new SoftReference<>(
            Caching.<String, String>newLRU());

    /**
     * Calls 'javaPath -version' and parses the results
     *
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo(String javaPath) {
        String versionInfo = JavaInfo.versionInfos.get().get(javaPath);

        if (versionInfo == null) {
            versionInfo = RuntimeStreamer.execute(new String[] { javaPath, "-version" });
            JavaInfo.versionInfos.get().put(javaPath, versionInfo);
        }

        String[] tokens = versionInfo.split("\"");

        if (tokens.length < 2) {
            this.version = "Unknown";
        } else {
            this.version = tokens[1];
            this.majorVersion = Java.parseJavaVersionNumber(this.version);
            this.minorVersion = Java.parseJavaBuildVersion(this.version);
        }

        this.is64bits = versionInfo.toUpperCase().contains("64-BIT");
        this.path = javaPath;
        this.rootPath = new File(javaPath).getParentFile().getParentFile().getAbsolutePath();

        try {
            this.isRuntime = Files.isSameFile(FileSystem.RUNTIMES, Paths.get(this.rootPath).getParent());
        } catch (IOException e) {
            this.isRuntime = false;
        }
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, Integer majorVersion, Integer minorVersion,
            boolean is64bits, boolean isRuntime) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.is64bits = is64bits;
        this.isRuntime = isRuntime;
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, Integer majorVersion, Integer minorVersion,
            boolean is64bits) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.is64bits = is64bits;
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, boolean is64bits) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.is64bits = is64bits;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString() {
        return this.path + " (" + (this.is64bits ? "64-bit" : "32-bit") + ")";
    }
}
