/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.atlauncher.App;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.data.mojang.Version;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class MinecraftVersion {

    private String version;
    private boolean server;
    private boolean legacy;
    private boolean coremods;
    private Version mojangVersion;

    public void loadVersion() {
        File versionFile = new File(App.settings.getVersionsDir(), this.version + ".json");
        Downloadable download = new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("versions/"
                + this.version + "/" + this.version + ".json"), versionFile, null, null, false);
        if (!versionFile.exists()) {
            download.download(false);
        }
        try {
            mojangVersion = Settings.altGson.fromJson(new FileReader(versionFile), Version.class);
        } catch (JsonSyntaxException e) {
            App.settings.logStackTrace(e);
        } catch (JsonIOException e) {
            App.settings.logStackTrace(e);
        } catch (FileNotFoundException e) {
            App.settings.logStackTrace(e);
        }
    }

    public boolean canCreateServer() {
        return this.server;
    }

    public String getVersion() {
        return this.version;
    }

    public Version getMojangVersion() {
        return this.mojangVersion;
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