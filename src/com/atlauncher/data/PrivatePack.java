/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.App;

public class PrivatePack extends Pack {

    private String[] allowedPlayers;

    public PrivatePack(int id, String name, boolean createServer, boolean leaderboards,
            boolean logging, boolean latestlwjgl, String[] versions, String[] noUpdateVersions,
            String[] minecraftVersions, String[] devVersions, String[] devMinecraftVersions,
            String description, String supportURL, String websiteURL) {
        super(id, name, createServer, leaderboards, logging, latestlwjgl, versions,
                noUpdateVersions, minecraftVersions, devVersions, devMinecraftVersions,
                description, supportURL, websiteURL);
    }

    public void addAllowedPlayers(String[] players) {
        this.allowedPlayers = players;
    }

    public boolean isAllowedPlayer() {
        Account account = App.settings.getAccount();
        if (account == null) {
            return false;
        }
        if (this.allowedPlayers == null) {
            return false;
        }
        for (String player : allowedPlayers) {
            if (player.equalsIgnoreCase(account.getMinecraftUsername())) {
                return true;
            }
        }
        return false;
    }

    public boolean canInstall() {
        if (super.isTester() || (super.hasVersions() && isAllowedPlayer())) {
            return true;
        } else {
            return false;
        }
    }

}
