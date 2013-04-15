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

public class Instance {

    private String name;
    private String pack;
    private Version version;

    public Instance(String name, String pack, Version version) {
        this.name = name;
        this.pack = pack;
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public String getPackName() {
        return pack;
    }
    
    public Version getVersion() {
        return version;
    }
    
    public Version getLatestVersion() {
        return version;
    }

    public String toString() {
        return "Instance Name: " + this.name + ", Pack Name: " + pack
                + ", Version: " + version;
    }

}
