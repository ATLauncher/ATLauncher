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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.JavaVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

@Json
public class Server{
    private static final Logger LOG = LogManager.getLogger(Server.class);

    public String name;
    public String pack;
    public Integer packId;
    public String version;
    public String hash;
    public boolean isPatchedForLog4Shell = false;

    public JavaVersion javaVersion;

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
        if (!isPatchedForLog4Shell) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Server Is Vulnerable"))
                    .setContent(new HTMLBuilder().text(GetText.tr(
                            "This server is currently vulnerable to the Log4Shell exploit.<br/><br/>For your safety, and of those on your server, please delete and recreate this server.<br/><br/>Do you still want to launch the server (not recommended)?"))
                            .center().build())
                    .setType(DialogManager.ERROR)
                    .show();

            if (ret != DialogManager.YES_OPTION) {
                return;
            }
        }

        boolean usesRunSh = Files.exists(getRoot().resolve("run.sh"));
        String serverScript = usesRunSh ? "run" : "LaunchServer";

        if (OS.isWindows()) {
            serverScript += ".bat";
        } else if (OS.isLinux() || OS.isMac()) {
            serverScript += ".sh";
        }

        LOG.info("Starting server " + name);
        List<String> arguments = new ArrayList<>();

        String javaPath = null;
        if (!usesRunSh && javaVersion != null && App.settings.useJavaProvidedByMinecraft) {
            Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(javaVersion.component)
                    .resolve(JavaRuntimes.getSystem()).resolve(javaVersion.component);

            if (Files.isDirectory(runtimeDirectory)) {
                javaPath = runtimeDirectory.toAbsolutePath().toString();
                LOG.debug(String.format("Using Java runtime %s (major version %d) at path %s",
                        javaVersion.component, javaVersion.majorVersion, javaPath));
            }
        }

        try {
            if (OS.isWindows()) {
                arguments.add("cmd");
                arguments.add("/K");
                arguments.add("start");
                arguments.add("\"" + name + "\"");
                arguments.add("/D");
                arguments.add("\"" + getRoot().toString() + "\"");
                arguments.add(serverScript);

                if (javaPath != null) {
                    arguments.add("ATLcustomjava");
                    arguments.add("\"" + javaPath + "\\bin\\java.exe" + "\"");
                }

                if (!args.isEmpty()) {
                    arguments.add(args);
                }
            } else if (OS.isLinux()) {
                // use some best guesses for some terminal programs if in path
                if (Utils.executableInPath("x-terminal-emulator")) {
                    arguments.add("x-terminal-emulator");
                    arguments.add("-e");

                    arguments.add(getRoot().resolve(serverScript).toString()
                            + (!usesRunSh && javaPath != null ? String.format(" ATLcustomjava %s",
                                    javaPath + "/bin/java ") : " ")
                            + args);
                } else if (Utils.executableInPath("exo-open")) {
                    arguments.add("exo-open");
                    arguments.add("--launch");
                    arguments.add("TerminalEmulator");
                    arguments.add("--working-directory");
                    arguments.add(getRoot().toAbsolutePath().toString());
                    arguments.add(String.format(
                            "./%s %s%s", serverScript, (!usesRunSh && javaPath != null
                                    ? String.format(" ATLcustomjava %s",
                                            javaPath + "/bin/java ")
                                    : ""),
                            args));
                } else {
                    DialogManager.okDialog().setTitle(GetText.tr("Failed To Launch Server"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "The server couldn't be launched as we don't know how to launcher it.<br/><br/>Please open the server folder and run the {0} file manually.",
                                    serverScript))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }
            } else if (OS.isMac()) {
                String launchCommand = serverScript;

                if (!usesRunSh) {
                    // unfortunately OSX doesn't allow us to pass arguments with open and Terminal
                    // so create a temporary script, run it and then delete after
                    List<String> launchScript = new ArrayList<>();

                    launchScript.add("cd \"`dirname \"$0\"`\" ; ./LaunchServer.sh");

                    if (javaPath != null) {
                        launchScript.add("ATLcustomjava");
                        launchScript.add(javaPath + "/jre.bundle/Contents/Home/bin/java");
                    }

                    launchScript.add(args);
                    launchScript.add("; rm -f ./.launcherrun.sh");

                    Path tempLaunchFile = getRoot().resolve(".launcherrun.sh");
                    Files.write(tempLaunchFile, String.join(" ", launchScript).toString().getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    tempLaunchFile.toFile().setExecutable(true);

                    LOG.info(String.format("Running \"%s\" from \".launcherrun.sh\"",
                            String.join(" ", launchScript)));

                    launchCommand = "./.launcherrun.sh";
                }

                arguments.add("open");
                arguments.add("-a");
                arguments.add("Terminal");
                arguments.add(launchCommand);
            }

            LOG.info("Launching server with the following arguments: {}", arguments);
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
            LOG.error("Failed to launch server", e);
        }
    }

    public void backup() {
        Analytics.sendEvent(pack + " - " + version, "Backup", "Server");

        Timestamp timestamp = new Timestamp(new Date().getTime());
        String time = timestamp.toString().replaceAll("[^0-9]", "_");
        String filename = "Server-" + getSafeName() + "-" + time.substring(0, time.lastIndexOf("_")) + ".zip";
        Path backupZip = FileSystem.BACKUPS.resolve(filename);

        ProgressDialog<Boolean> progressDialog = new ProgressDialog<>(GetText.tr("Backing Up {0}", name));
        progressDialog.addThread(new Thread(() -> {
            boolean success = ArchiveUtils.createZip(getRoot(), backupZip);

            progressDialog.setReturnValue(success);
            progressDialog.close();
        }));
        progressDialog.start();

        if (progressDialog.getReturnValue()) {
            App.TOASTER.pop(GetText.tr("Backup is complete"));
            LOG.info("Backup complete and stored at {}", backupZip);
        } else {
            App.TOASTER.popError(GetText.tr("Error making backup"));
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
                LOG.error("Error creating scaled image from the custom image of server " + this.name, e);
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
            LOG.error("error", e);
        }
    }
}
