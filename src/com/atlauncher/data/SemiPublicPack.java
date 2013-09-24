/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.App;

public class SemiPublicPack extends Pack {

    private String code;

    public SemiPublicPack(int id, String name, String code, boolean createServer,
            boolean leaderboards, boolean logging, boolean latestlwjgl, String[] versions,
            String[] noUpdateVersions, String[] minecraftVersions, String[] devVersions,
            String[] devMinecraftVersions, String[] testers, String description, String supportURL,
            String websiteURL) {
        super(id, name, createServer, leaderboards, logging, latestlwjgl, versions,
                noUpdateVersions, minecraftVersions, devVersions, devMinecraftVersions, testers,
                description, supportURL, websiteURL);
        this.code = code;
    }

    public boolean canInstall() {
        if (super.isTester()
                || (super.hasVersions() && App.settings.canViewSemiPublicPackByCode(this.code))) {
            return true;
        } else {
            return false;
        }
    }

    public String getCode() {
        return this.code;
    }

}
