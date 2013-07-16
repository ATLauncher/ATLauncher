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
package com.atlauncher.data;

import com.atlauncher.App;

public class PrivatePack extends Pack {

    private String[] allowedPlayers;

    public PrivatePack(int id, String name, boolean createServer, boolean leaderboards,
            boolean logging, boolean latestlwjgl, String[] versions, String[] minecraftVersions,
            String devMinecraftVersion, String[] testers, String description, String supportURL,
            String websiteURL, String[] allowedPlayers) {
        super(id, name, createServer, leaderboards, logging, latestlwjgl, versions,
                minecraftVersions, devMinecraftVersion, testers, description, supportURL,
                websiteURL);
        this.allowedPlayers = allowedPlayers;
    }

    public boolean isAllowedPlayer() {
        Account account = App.settings.getAccount();
        if (account == null) {
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
