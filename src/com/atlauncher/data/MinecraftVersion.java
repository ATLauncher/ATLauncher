/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

public class MinecraftVersion {

    private String version;
    private String type;
    private boolean canCreateServer;
    private boolean legacy;
    private boolean coremods;

    public MinecraftVersion(String version, String type, boolean canCreateServer, boolean legacy,
            boolean coremods) {
        this.version = version;
        this.type = type;
        this.canCreateServer = canCreateServer;
        this.legacy = legacy;
        this.coremods = coremods;
    }

    public boolean canCreateServer() {
        return this.canCreateServer;
    }

    public String getVersion() {
        return this.version;
    }

    public String getType() {
        return this.type;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public boolean usesCoreMods() {
        return this.coremods;
    }

    public String toString() {
        return this.version;
    }

}