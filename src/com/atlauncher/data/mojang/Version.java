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
package com.atlauncher.data.mojang;

import java.util.Date;
import java.util.List;

public class Version {

    private String id;
    private String minecraftArguments;
    private String assets;
    private List<Library> libraries;
    private List<Rule> rules;
    private String mainClass;

    public String getId() {
        return id;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public String getAssets() {
        if (this.assets == null) {
            return "legacy";
        }
        return this.assets;
    }

    public List<Library> getLibraries() {
        return this.libraries;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public String getMainClass() {
        return this.mainClass;
    }

}
