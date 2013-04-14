/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

public class Version {

    private int major;
    private int minor;
    private int revision;

    public Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isNewer(Version thanThis) {
        if (this.major == thanThis.major) {
            if (this.minor == thanThis.minor) {
                if (this.revision == thanThis.revision) {
                    return false;
                } else if (this.revision < thanThis.revision) {
                    return false;
                }
            } else {
                if (this.minor < thanThis.minor) {
                    return false;
                } else {
                    if (this.revision == thanThis.revision) {
                        return false;
                    } else if (this.revision < thanThis.revision) {
                        return false;
                    }
                }
            }
        } else {
            if (this.major < thanThis.major) {
                return false;
            } else {
                if (this.minor == thanThis.minor) {
                    return false;
                } else if (this.minor < thanThis.minor) {
                    return false;
                } else {
                    if (this.revision == thanThis.revision) {
                        return false;
                    } else if (this.revision < thanThis.revision) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String toString() {
        return major + "." + minor + "." + revision;
    }

}
