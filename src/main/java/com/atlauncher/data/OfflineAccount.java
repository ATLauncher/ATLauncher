/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.data;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * An account used for offline (no-authentication) play. It carries no real
 * credentials: a fixed access/session token of "0" and a UUID derived
 * deterministically from the username (the vanilla offline scheme).
 */
public class OfflineAccount extends AbstractAccount {
    private static final long serialVersionUID = 1L;

    public OfflineAccount(String username) {
        this.username = username;
        this.minecraftUsername = username;
        this.uuid = offlineUUID(username).toString().replace("-", "");
    }

    /**
     * Derives the offline-mode UUID for a username, matching vanilla Minecraft
     * and other launchers: a name-based (version 3) UUID of "OfflinePlayer:<name>".
     */
    public static UUID offlineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getAccessToken() {
        return "0";
    }

    @Override
    public String getSessionToken() {
        return "0";
    }

    @Override
    public String getUserType() {
        return "legacy";
    }

    @Override
    public String getCurrentUsername() {
        return minecraftUsername;
    }

    @Override
    public void updateSkinPreCheck() {
        // no-op: offline accounts have no remote profile
    }

    @Override
    public void changeSkinPreCheck() {
        // no-op: offline accounts have no remote profile
    }

    @Override
    public String getSkinUrl() {
        // offline accounts have no remote profile, so use the default skin
        return null;
    }
}
