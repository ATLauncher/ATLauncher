/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
    private boolean noConfigs;

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
     * The warning messages that should be shown to the user when an optional mod is selected.
     */
    private Map<String, String> warnings;

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
        return this.noConfigs;
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
     * Checks to see if there is a warning message with the provided key.
     *
     * @param key The key/name given to the warning by the pack developer/s
     * @return true if the warning is defined, otherwise false
     */
    public boolean hasWarningMessage(String key) {
        return this.warnings.containsKey(key);
    }

    /**
     * Gets the warning message from the provided key.
     *
     * @param key The key/name given to the warning by the pack developer/s
     * @return the warning message to display to the user
     */
    public String getWarningMessage(String key) {
        return this.warnings.get(key);
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
