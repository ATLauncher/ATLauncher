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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.JavaVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;

@Json
public class Server {
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

    public transient Path ROOT;

    public Path getRoot() {
        return this.ROOT;
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

        String serverScript = getServerScript();
        if (serverScript == null) {
            DialogManager.okDialog().setTitle(GetText.tr("Cannot Launch Server"))
                    .setContent(new HTMLBuilder().text(GetText.tr(
                            "We cannot launch this server for you. Please \"Open Folder\" and run the server manually."))
                            .center().build())
                    .setType(DialogManager.ERROR)
                    .show();
            return;
        }

        boolean isATLauncherLaunchScript = isATLauncherLaunchScript();

        Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_run", this));

        LogManager.info("Starting server " + name);
        List<String> arguments = new ArrayList<>();

        String javaPath = null;
        if (isATLauncherLaunchScript && javaVersion != null && App.settings.useJavaProvidedByMinecraft) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();

            if (runtimesForSystem.containsKey(javaVersion.component)
                    && runtimesForSystem.get(javaVersion.component).size() != 0) {
                Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(javaVersion.component)
                        .resolve(JavaRuntimes.getSystem()).resolve(javaVersion.component);

                if (Files.isDirectory(runtimeDirectory)) {
                    javaPath = runtimeDirectory.toAbsolutePath().toString();
                    LogManager.info(String.format("Using Java runtime %s (major version %d) at path %s",
                            javaVersion.component, javaVersion.majorVersion, javaPath));
                }
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

                if (isPowershellScript()) {
                    arguments.add("powershell");
                    arguments.add("-NoExit");
                    arguments.add("./" + serverScript);
                } else {
                    arguments.add(serverScript);

                    if (javaPath != null) {
                        arguments.add("ATLcustomjava");
                        arguments.add("\"" + javaPath + "\\bin\\java.exe" + "\"");
                    }
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
                            + (isATLauncherLaunchScript && javaPath != null ? String.format(" ATLcustomjava %s",
                                    javaPath + "/bin/java ") : " ")
                            + args);
                } else if (Utils.executableInPath("exo-open")) {
                    arguments.add("exo-open");
                    arguments.add("--launch");
                    arguments.add("TerminalEmulator");
                    arguments.add("--working-directory");
                    arguments.add(getRoot().toAbsolutePath().toString());
                    arguments.add(String.format(
                            "./%s %s%s", serverScript, (isATLauncherLaunchScript && javaPath != null
                                    ? String.format(" ATLcustomjava %s",
                                            javaPath + "/bin/java ")
                                    : ""),
                            args));
                } else if (Utils.executableInPath("kitty")) {
                    arguments.add("kitty");
                    arguments.add(String.format(
                            "./%s %s", serverScript, (isATLauncherLaunchScript && javaPath != null
                                    ? String.format(" ATLcustomjava %s",
                                            javaPath + "/bin/java ")
                                    : ""))
                            .replace(" ", ""));// the trailing space char leads to file not found
                } else if (Utils.executableInPath("alacritty")) {
                    arguments.add("alacritty");
                    arguments.add("-e");
                    arguments.add(String.format(
                            "./%s %s", serverScript, (isATLauncherLaunchScript && javaPath != null
                                    ? String.format(" ATLcustomjava %s",
                                            javaPath + "/bin/java ")
                                    : ""))
                            .replace(" ", ""));// the trailing space char leads to file not found
                } else if (Utils.executableInPath("gnome-terminal")) {
                    arguments.add("gnome-terminal");
                    arguments.add("--");
                    arguments.add(String.format(
                            "./%s %s", serverScript, (isATLauncherLaunchScript && javaPath != null
                                    ? String.format(" ATLcustomjava %s",
                                            javaPath + "/bin/java ")
                                    : ""))
                            .replace(" ", ""));// the trailing space char leads to file not found
                } else {
                    DialogManager.okDialog().setTitle(GetText.tr("Failed To Launch Server"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "The server couldn't be launched as we don't know how to launch it.<br/><br/>Please open the server folder and run the {0} file manually.",
                                    serverScript))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }
            } else if (OS.isMac()) {
                String launchCommand = serverScript;

                if (isATLauncherLaunchScript) {
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

                    LogManager.info(String.format("Running \"%s\" from \".launcherrun.sh\"",
                            String.join(" ", launchScript)));

                    launchCommand = "./.launcherrun.sh";
                }

                arguments.add("open");
                arguments.add("-a");
                arguments.add("Terminal");
                arguments.add(launchCommand);
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
                Analytics.endSession();
                System.exit(0);
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to launch server", e);
        }
    }

    private String getServerScript() {
        if (OS.isWindows()) {
            if (Files.exists(this.ROOT.resolve("LaunchServer.bat"))) {
                return "LaunchServer.bat";
            }

            if (Files.exists(this.ROOT.resolve("start.ps1"))) {
                return "start.ps1";
            }

            if (Files.exists(this.ROOT.resolve("run.bat"))) {
                return "run.bat";
            }

            if (Files.exists(this.ROOT.resolve("StartServer.bat"))) {
                return "StartServer.bat";
            }

            if (Files.exists(this.ROOT.resolve("ServerStart.bat"))) {
                return "ServerStart.bat";
            }
        } else if (OS.isLinux() || OS.isMac()) {
            if (Files.exists(this.ROOT.resolve("LaunchServer.sh"))) {
                return "LaunchServer.sh";
            }

            if (Files.exists(this.ROOT.resolve("start.sh"))) {
                return "start.sh";
            }

            if (Files.exists(this.ROOT.resolve("run.sh"))) {
                return "run.sh";
            }

            if (Files.exists(this.ROOT.resolve("StartServer.sh"))) {
                return "StartServer.sh";
            }

            if (Files.exists(this.ROOT.resolve("ServerStart.sh"))) {
                return "ServerStart.sh";
            }
        }

        return null;
    }

    private boolean isPowershellScript() {
        if (OS.isWindows()) {
            return getServerScript().endsWith(".ps1");
        }

        return false;
    }

    private boolean isATLauncherLaunchScript() {
        if (OS.isWindows()) {
            return Files.exists(this.ROOT.resolve("LaunchServer.bat"));
        } else if (OS.isLinux() || OS.isMac()) {
            return Files.exists(this.ROOT.resolve("LaunchServer.sh"));
        }

        return false;
    }

    public void backup() {
        Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_backup", this));

        Timestamp timestamp = new Timestamp(new Date().getTime());
        String time = timestamp.toString().replaceAll("[^0-9]", "_");
        String filename = "Server-" + getSafeName() + "-" + time.substring(0, time.lastIndexOf("_")) + ".zip";
        Path backupZip = FileSystem.BACKUPS.resolve(filename);

        // #. {0} is the name of the server we're backing up
        ProgressDialog<Boolean> progressDialog = new ProgressDialog<>(GetText.tr("Backing Up {0}", name));
        progressDialog.addThread(new Thread(() -> {
            boolean success = ArchiveUtils.createZip(getRoot(), backupZip);

            progressDialog.setReturnValue(success);
            progressDialog.close();
        }));
        progressDialog.start();

        if (progressDialog.getReturnValue()) {
            App.TOASTER.pop(GetText.tr("Backup is complete"));
            LogManager.info(String.format("Backup complete and stored at %s", backupZip.toString()));
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
                if (img != null) {
                    Image dimg = img.getScaledInstance(300, 150, Image.SCALE_SMOOTH);
                    return new ImageIcon(dimg);
                }
            } catch (IIOException e) {
                LogManager.warn("Error creating scaled image from the custom image of server " + this.name
                        + ". Using default image.");
            } catch (Exception e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of server " + this.name, e,
                        false);
            }
        }

        if (getPack() != null) {
            File instancesImage = FileSystem.IMAGES.resolve(this.getSafePackName().toLowerCase(Locale.ENGLISH) + ".png")
                    .toFile();
            if (instancesImage.exists()) {
                return Utils.getIconImage(instancesImage);
            }
        }

        return Utils.getIconImage("/assets/image/default-image.png");
    }

    public void save() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(this.getRoot().resolve("server.json").toFile()), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(this, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }
}
