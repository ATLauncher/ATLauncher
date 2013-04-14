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
    private String description;
    private String changelog;

    public Pack(int id, String name, Player owner, Version[] versions,
            String description, String changelog) {
        this.name = name;
        this.owner = owner;
        this.versions = versions;
        this.description = description;
        this.changelog = changelog;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public Version[] getVersions() {
        return versions;
    }

    public String getDescription() {
        return description;
    }

    public String getChangelog() {
        return changelog;
    }
    
    public int getVersionCount() {
        return versions.length;
    }
    
    public Version getVersion(int index) {
        return versions[index];
    }

}
