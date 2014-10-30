/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import java.util.List;

public class MojangVersion {

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
