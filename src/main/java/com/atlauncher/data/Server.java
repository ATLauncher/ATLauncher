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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;

import org.mini2Dx.gettext.GetText;

@Json
public class Server {
    public String name;
    public String pack;
    public Integer packId;
    public String version;
    public String hash;

    public boolean isDev;
    public List<DisableableMod> mods = new ArrayList<>();
    public List<String> ignoredUpdates = new ArrayList<>();

    public Path getRoot() {
        return FileSystem.SERVERS.resolve(this.getSafeName());
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    public Pack getPack() {
        try {
            return PackManager.getPackByID(this.packId);
        } catch (InvalidPack e) {
            return null;
        }
    }

    public void launch(boolean close) {
        launch("", close);
    }

    public void launch(String args, boolean close) {
        LogManager.info("Starting server " + name);
        List<String> arguments = new ArrayList<>();

        try {
            if (OS.isWindows()) {
                arguments.add("cmd");
                arguments.add("/K");
                arguments.add("start");
                arguments.add("\"" + name + "\"");
                arguments.add(getRoot().resolve("LaunchServer.bat").toString());
                arguments.add(args);
            } else if (OS.isLinux()) {
                // this only covers Gnome like systems, will need to monitor user reports for
                // alternatives
                arguments.add("x-terminal-emulator");
                arguments.add("-e");
                arguments.add(getRoot().resolve("LaunchServer.sh").toString() + " " + args);
            } else if (OS.isMac()) {
                // unfortunately OSX doesn't allow us to pass arguments with open and Terminal
                // :(
                arguments.add("open");
                arguments.add("-a");
                arguments.add("Terminal");
                arguments.add(getRoot().resolve("LaunchServer.command").toString());
            }

            LogManager.info("Launching server with the following arguments: " + arguments.toString());
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(getRoot().toFile());
            processBuilder.command(arguments);
            processBuilder.start();

            if (!close) {
                DialogManager.okDialog().setTitle(GetText.tr("Server Launched"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "The server has been launched in an external window.<br/><br/>You can close ATLauncher which will not stop the server."))
                                .build())
                        .setType(DialogManager.INFO).show();
            } else {
                System.exit(0);
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to launch server", e);
        }
    }

    public boolean hasUpdate() {
        Pack pack = this.getPack();

        if (pack != null) {
            if (pack.hasVersions() && !this.isDev) {
                // Lastly check if the current version we installed is different than the latest
                // version of the Pack and that the latest version of the Pack is not restricted
                // to disallow updates.
                if (!pack.getLatestVersion().version.equalsIgnoreCase(this.version)
                        && !pack.isLatestVersionNoUpdate()) {
                    return true;
                }
            }

            if (this.isDev && (this.hash != null)) {
                PackVersion devVersion = pack.getDevVersionByName(this.version);
                if (devVersion != null && !devVersion.hashMatches(this.hash)) {
                    return true;
                }
            }
        }

        return false;
    }

    public PackVersion getLatestVersion() {
        Pack pack = this.getPack();

        if (pack != null) {
            if (pack.hasVersions() && !this.isDev) {
                return pack.getLatestVersion();
            }

            if (this.isDev) {
                return pack.getLatestDevVersion();
            }
        }

        return null;
    }

    public String getPackDescription() {
        Pack pack = this.getPack();

        if (pack != null) {
            return pack.description;
        } else {
            return GetText.tr("No Description");
        }
    }

    public ImageIcon getImage() {
        File customImage = this.getRoot().resolve("server.png").toFile();

        if (customImage.exists()) {
            try {
                BufferedImage img = ImageIO.read(customImage);
                Image dimg = img.getScaledInstance(300, 150, Image.SCALE_SMOOTH);
                return new ImageIcon(dimg);
            } catch (IOException e) {
                LogManager.logStackTrace("Error creating scaled image from the custom image of server " + this.name, e);
            }
        }

        if (getPack() != null) {
            File instancesImage = FileSystem.IMAGES.resolve(this.getSafePackName().toLowerCase() + ".png").toFile();
            if (instancesImage.exists()) {
                return Utils.getIconImage(instancesImage);
            }
        }

        return Utils.getIconImage("/assets/image/default-image.png");
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(this.getRoot().resolve("server.json").toFile())) {
            Gsons.MINECRAFT.toJson(this, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }
}
