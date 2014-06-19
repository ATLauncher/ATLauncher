/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import java.util.List;
import java.util.Map;

public class Version {
    private String version;
    private String minecraft;
    private int memory;
    private int permgen;
    private boolean noconfigs;
    private String caseAllFiles;
    private MainClass mainClass;
    private ExtraArguments extraArguments;
    private Deletes deletes;
    private Messages messages;
    private List<Library> libraries;
    private Map<String, String> colours;
    private List<Mod> mods;
    private List<Action> actions;

    public String getVersion() {
        return this.version;
    }

    public String getMinecraft() {
        return this.minecraft;
    }

    public int getMemory() {
        return this.memory;
    }

    public int getPermgen() {
        return this.permgen;
    }

    public boolean hasNoConfigs() {
        return this.noconfigs;
    }

    public String getCaseAllFiles() {
        return this.caseAllFiles;
    }

    public MainClass getMainClass() {
        return this.mainClass;
    }

    public ExtraArguments getExtraArguments() {
        return this.extraArguments;
    }

    public Deletes getDeletes() {
        return this.deletes;
    }

    public Messages getMessages() {
        return this.messages;
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

    public List<Action> getActions() {
        return this.actions;
    }
}
