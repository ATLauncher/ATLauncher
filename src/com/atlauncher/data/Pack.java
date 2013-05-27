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


public class Pack {

    private int id;
    private String name;
    private Player owner;
    private Version[] versions;
    private Version[] minecraftVersions;
    private String description;

    public Pack(int id, String name, Player owner, Version[] versions,
            Version[] minecraftVersions, String description) {
        this.name = name;
        this.owner = owner;
        this.versions = versions;
        this.minecraftVersions = minecraftVersions;
        this.description = description;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Player getOwner() {
        return this.owner;
    }

    public Version[] getVersions() {
        return this.versions;
    }

    public Version[] getMinecraftVersions() {
        return this.minecraftVersions;
    }

    public String getDescription() {
        return this.description;
    }

    public int getVersionCount() {
        return this.versions.length;
    }

    public Version getVersion(int index) {
        return this.versions[index];
    }

}
