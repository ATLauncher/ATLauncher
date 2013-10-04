/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

/**
 * Class for Addons to packs
 * 
 * @author Ryan
 */
public class Addon {

    private int id; // ID of Addon
    private String name; // Name of Addon
    private String[] versions; // Versions of the Addon available
    private String description; // Description of Addon
    private Pack forPack; // Pack that this addon is for

    /**
     * Creates an addon for display in the Addons tab
     * 
     * @param id
     *            ID of Addon
     * @param name
     *            Name of Addon
     * @param versions
     *            Version array of versions available
     * @param description
     *            Description of the Addon
     * @param forPack
     *            Pack the Addon is for
     */
    public Addon(int id, String name, String[] versions, String description,
            Pack forPack) {
        this.id = id;
        this.name = name;
        this.versions = versions;
        this.description = description;
        this.forPack = forPack;
    }

    /**
     * Gets the ID of the Addon
     * 
     * @return ID of Addon
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the Name of the Addon
     * 
     * @return Name of Addon
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets an array of Version type for all available versions of the Addon
     * 
     * @return Array of Version type for all available versions of Addon
     */
    public String[] getVersions() {
        return this.versions;
    }

    /**
     * Gets the Description of the Addon
     * 
     * @return Description of Addon
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the Pack the Addon is for
     * 
     * @return Pack the Addon is for
     */
    public Pack getForPack() {
        return this.forPack;
    }

}
