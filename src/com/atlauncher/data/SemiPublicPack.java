/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.App;

public class SemiPublicPack extends Pack {

    public SemiPublicPack(int id, String name, boolean createServer, boolean leaderboards,
            boolean logging, boolean latestlwjgl, String[] versions, String[] minecraftVersions,
            String devMinecraftVersion, String[] testers, String description, String supportURL,
            String websiteURL) {
        super(id, name, createServer, leaderboards, logging, latestlwjgl, versions,
                minecraftVersions, devMinecraftVersion, testers, description, supportURL,
                websiteURL);
    }

    public boolean canInstall() {
        if (super.isTester() || (super.hasVersions() && App.settings.canViewSemiPublicPack(super.getName()))) {
            return true;
        } else {
            return false;
        }
    }

}
