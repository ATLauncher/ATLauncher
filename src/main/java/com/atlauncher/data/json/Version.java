/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains information about a pack's version. This is a singular version and contains all the information
 * necessary to install the pack.
 */
@Json
public class Version {
    /**
     * The version this pack's version is.
     */
    private String version;

    /**
     * The version of Minecraft this version is for.
     */
    private String minecraft;

    /**
     * The minimum amount of memory/ram to use when launching this version.
     */
    private int memory;

    /**
     * The minimum amount of PermGen/MetaSpace to use when launching this version.
     */
    private int permGen;

    /**
     * If this version has no configs.
     */
    private boolean noconfigs;

    /**
     * If this version should uppercase/lowercase all files.
     */
    private CaseType caseAllFiles;

    /**
     * The details about the MainClass to use when launching Minecraft.
     */
    private MainClass mainClass;

    /**
     * Details about any extra arguments this version uses when launching Minecraft, usually including the tweakClass
     * for Forge.
     */
    private ExtraArguments extraArguments;

    /**
     * The deletes which should be made when updating/reinstalling this version.
     */
    private Deletes deletes;

    /**
     * The messages that should be shown to the user upon various different conditions such as a new install or update.
     */
    private Messages messages;

    /**
     * A list of Libraries this version requires.
     */
    private List<Library> libraries;

    /**
     * A map of the difference colours used in this version for things such as mod display.
     */
    private Map<String, String> colours;

    /**
     * A list of mods to be installed with this version.
     */
    private List<Mod> mods;

    /**
     * A list of actions to perform on this version.
     */
    private List<Action> actions;

    /**
     * Sets the default empty objects which are later overwritten by GSON if they exist. If they don't exist, having
     * these here will ensure no NPE's.
     */
    public Version() {
        this.libraries = new ArrayList<Library>();
        this.colours = new HashMap<String, String>();
        this.mods = new ArrayList<Mod>();
        this.actions = new ArrayList<Action>();
    }

    /**
     * Gets the version string of this version.
     *
     * @return the version string
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the Minecraft version used by this version.
     *
     * @return The Minecraft version
     */
    public String getMinecraft() {
        return this.minecraft;
    }

    /**
     * Gets the minimum memory specified by this version to use when launching the pack.
     *
     * @return The minimum memory to use when launching this version
     */
    public int getMemory() {
        return this.memory;
    }

    public int getPermGen() {
        return this.permGen;
    }

    public boolean hasNoConfigs() {
        return this.noconfigs;
    }

    public CaseType getCaseAllFiles() {
        return this.caseAllFiles;
    }

    public boolean shouldCaseAllFiles() {
        return this.caseAllFiles != null;
    }

    public MainClass getMainClass() {
        return this.mainClass;
    }

    public boolean hasMainClass() {
        return this.mainClass != null && this.mainClass.getMainClass() != null;
    }

    public ExtraArguments getExtraArguments() {
        return this.extraArguments;
    }

    public boolean hasExtraArguments() {
        return this.extraArguments != null && this.extraArguments.getArguments() != null;
    }

    public Deletes getDeletes() {
        return this.deletes;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public boolean hasMessages() {
        return this.messages != null;
    }

    public List<Library> getLibraries() {
        return this.libraries;
    }

    public Map<String, String> getColours() {
        return this.colours;
    }

    public List<Mod> getMods() {
        return this.mods;
    }

    public List<Mod> getClientInstallMods() {
        List<Mod> mods = new ArrayList<Mod>();
        for (Mod mod : this.mods) {
            if (mod.installOnClient()) {
                mods.add(mod);
            }
        }
        return mods;
    }

    public List<Mod> getServerInstallMods() {
        List<Mod> mods = new ArrayList<Mod>();
        for (Mod mod : this.mods) {
            if (mod.installOnServer()) {
                mods.add(mod);
            }
        }
        return mods;
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public boolean hasActions() {
        return this.actions != null && this.actions.size() != 0;
    }

    /**
     * This checks to see if there is a colour with the provided key.
     *
     * @param key The key/name given to the colour by the pack developer/s
     * @return true if the colour is defined, otherwise false
     */
    public boolean isColour(String key) {
        return this.colours.containsKey(key);
    }

    /**
     * Returns a Color object of a given key specified in a mods colour field. If the key is not found or the code given
     * is incorrect, it will return null and create a warning log message.
     *
     * @param key The key/name given to the colour by the pack developer/s
     * @return a {@link Color} object of the colour matching the key or null if there was an issue with the value given
     */
    public Color getColour(String key) {
        if (key == null) {
            return null;
        }
        if (!this.isColour(key)) {
            LogManager.warn("Colour with key " + key + " not found!");
            return null;
        }
        String colour = this.colours.get(key);
        if (colour.substring(0, 1).equals("#")) {
            colour = colour.replace("#", "");
        }
        if (!colour.matches("[0-9A-Fa-f]{6}")) {
            LogManager.warn("Colour with key " + key + " has invalid value of " + colour + "!");
            return null;
        }
        int r, g, b;
        try {
            r = Integer.parseInt(colour.substring(0, 2), 16);
            g = Integer.parseInt(colour.substring(2, 4), 16);
            b = Integer.parseInt(colour.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            LogManager.warn("Colour with key " + key + " failed to create object with value of " + colour + "!");
            return null;
        }
    }

    public void compileColours() {
        for (Mod mod : this.mods) {
            if (mod.hasColour()) {
                mod.setCompiledColour(this.getColour(mod.getColour()));
            }
        }
    }
}
