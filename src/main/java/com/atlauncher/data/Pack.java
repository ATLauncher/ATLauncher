/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.utils.Utils;

public class Pack {
    public int id;
    public int externalId;
    public int position;
    public String name;
    public PackType type;
    public String code;
    public List<PackVersion> versions;
    public List<PackVersion> devVersions;
    public boolean createServer;
    public boolean logging;
    public boolean featured;
    public boolean system;
    public boolean hasDiscordImage;
    public String description;
    public CurseForgeProject curseForgeProject;
    public ModpacksChPackManifest modpacksChPack;
    public String discordInviteURL = null;
    public String supportURL = null;
    public String websiteURL = null;
    public List<String> testers = new ArrayList<>();
    public List<String> allowedPlayers = new ArrayList<>();
    public String json; // The JSON for a version of the pack
    public String jsonVersion; // The version the JSON above is for

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets a file safe and URL safe name which simply means replacing all non alpha
     * numerical characters with nothing
     *
     * @return File safe and URL safe name of the pack
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public int getPosition() {
        return this.position;
    }

    public ImageIcon getImage() {
        File imageFile = FileSystem.IMAGES.resolve(getSafeName().toLowerCase() + ".png").toFile();
        if (!imageFile.exists()) {
            return Utils.getIconImage("/assets/image/DefaultPackImage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    public boolean isPublic() {
        return this.type == PackType.PUBLIC;
    }

    public boolean isSemiPublic() {
        return this.type == PackType.SEMIPUBLIC;
    }

    public boolean isPrivate() {
        return this.type == PackType.PRIVATE;
    }

    public String getCode() {
        if (!isSemiPublic()) {
            return "";
        }
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDiscordInviteURL() {
        return this.discordInviteURL;
    }

    public String getSupportURL() {
        return this.supportURL;
    }

    public String getWebsiteURL() {
        return this.websiteURL;
    }

    public boolean canCreateServer() {
        return this.createServer;
    }

    public boolean isLoggingEnabled() {
        return this.logging;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public boolean isSystem() {
        return this.system;
    }

    public boolean hasDiscordImage() {
        return this.hasDiscordImage;
    }

    public void addTesters(List<String> users) {
        this.testers.addAll(users);
    }

    public void addAllowedPlayers(List<String> users) {
        this.allowedPlayers.addAll(users);
    }

    public List<PackVersion> getVersions() {
        return this.versions;
    }

    public List<PackVersion> getDevVersions() {
        return this.devVersions;
    }

    public boolean isTester() {
        AbstractAccount account = AccountManager.getSelectedAccount();
        if (account == null) {
            return false;
        }
        for (String tester : this.testers) {
            if (tester.equalsIgnoreCase(account.minecraftUsername)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVersions() {
        return this.versions.size() != 0;
    }

    public boolean hasDevVersions() {
        return this.devVersions.size() != 0;
    }

    public boolean canInstall() {
        if (this.type == PackType.PRIVATE) {
            if (isTester() || (hasVersions() && isAllowedPlayer())) {
                return true;
            }
        } else if (this.type == PackType.SEMIPUBLIC && this.code != null) {
            if (isTester() || (hasVersions() && PackManager.canViewSemiPublicPackByCode(this.code))) {
                return true;
            }
        } else {
            if (isTester() || hasVersions()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowedPlayer() {
        if (this.type != PackType.PRIVATE) {
            return true;
        }
        AbstractAccount account = AccountManager.getSelectedAccount();
        if (account == null) {
            return false;
        }
        for (String player : this.allowedPlayers) {
            if (player.equalsIgnoreCase(account.minecraftUsername)) {
                return true;
            }
        }
        return false;
    }

    public int getVersionCount() {
        return this.versions.size();
    }

    public int getDevVersionCount() {
        return this.devVersions.size();
    }

    public PackVersion getDevVersionByName(String name) {
        if (this.devVersions.size() == 0) {
            return null;
        }

        for (PackVersion devVersion : this.devVersions) {
            if (devVersion.versionMatches(name)) {
                return devVersion;
            }
        }

        return null;
    }

    public PackVersion getVersionByName(String name) {
        if (this.versions.size() == 0) {
            return null;
        }

        for (PackVersion version : this.versions) {
            if (version.versionMatches(name)) {
                return version;
            }
        }

        return null;
    }

    public PackVersion getLatestVersion() {
        if (this.versions.size() == 0) {
            return null;
        }
        return this.versions.get(0);
    }

    public PackVersion getLatestDevVersion() {
        if (this.devVersions.size() == 0) {
            return null;
        }
        return this.devVersions.get(0);
    }

    public boolean isLatestVersionNoUpdate() {
        if (this.versions.size() == 0) {
            return false;
        }
        if (!getLatestVersion().canUpdate) {
            return true;
        }
        return !getLatestVersion().isRecommended;
    }

    public Version getJsonVersion(String version) {
        return Gsons.DEFAULT.fromJson(this.getJSON(version), Version.class);
    }

    public String getJSON(String version) {
        return getJSON(version, true);
    }

    public String getJSON(String version, boolean redownload) {
        if (this.json == null || !this.jsonVersion.equalsIgnoreCase(version) || (isTester() && redownload)) {
            int tries = 1;
            do {
                this.json = com.atlauncher.network.Download.build().cached().setUrl(this.getJsonDownloadUrl(version))
                        .asString();
                tries++;
            } while (json == null && tries < 5);
            this.jsonVersion = version;
        }
        return this.json;
    }

    public String getJsonDownloadUrl(String version) {
        return String.format("%s/packs/%s/versions/%s/Configs.json", Constants.DOWNLOAD_SERVER, this.getSafeName(),
                version);
    }

    public String addInstall(String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/installed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }

    public String addServerInstall(String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/serverinstalled/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }

    public String addUpdate(String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/updated/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }
}
