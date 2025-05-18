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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileHash;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeFingerprintedMod;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.JavaVersion;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthProjectType;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;

import io.github.asyncronous.toast.Toaster;

@Json
public class Server implements ModManagement {
    public String name;
    public String description;
    public String pack;
    public Integer packId;
    public String version;
    public String hash;
    public boolean isPatchedForLog4Shell = false;

    public LoaderVersion loaderVersion;

    public JavaVersion javaVersion;

    public boolean isDev;
    public List<DisableableMod> mods = new ArrayList<>();
    public List<String> ignoredUpdates = new ArrayList<>();

    public CurseForgeProject curseForgeProject;
    public CurseForgeFile curseForgeFile;
    public ModrinthProject modrinthProject;
    public ModrinthVersion modrinthVersion;
    public ModrinthModpackManifest modrinthManifest;

    public transient Path ROOT;

    @Override
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
                        "We cannot launch this server for you. Please use \"Open Folder\" and run the server manually.<br/><br/>If unsure how to do so, please contact the packs support pages for more further help."))
                    .center().build())
                .setType(DialogManager.ERROR)
                .show();
            return;
        }

        boolean isATLauncherLaunchScript = isATLauncherLaunchScript();

        Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_run", this));

        LogManager.info("Starting server " + name);

        if (curseForgeFile != null) {
            LogManager.info("Server was created from a CurseForge server file with file ID " + curseForgeFile.id
                + ". It has been downloaded and unzipped without any modifications.");
        }
        List<String> arguments = new ArrayList<>();

        String javaPath = null;
        if (isATLauncherLaunchScript && javaVersion != null && App.settings.useJavaProvidedByMinecraft) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();

            if (runtimesForSystem.containsKey(javaVersion.component)
                && !runtimesForSystem.get(javaVersion.component).isEmpty()) {
                Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(javaVersion.component)
                    .resolve(JavaRuntimes.getSystem()).resolve(javaVersion.component);

                if (Files.isDirectory(runtimeDirectory)) {
                    javaPath = runtimeDirectory.toAbsolutePath().toString();
                    LogManager.info(String.format(Locale.ENGLISH, "Using Java runtime %s (major version %d) at path %s",
                        javaVersion.component, javaVersion.majorVersion, javaPath));
                }
            }
        }

        try {
            getRoot().resolve(serverScript).toFile().setExecutable(true);

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

                    arguments.add(getRoot().resolve(serverScript)
                        + (isATLauncherLaunchScript && javaPath != null ? String.format(" ATLcustomjava %s",
                        javaPath + "/bin/java ") : " ")
                        + args);
                } else {
                    String format = String.format(
                        "./%s %s%s",
                        serverScript,
                        (isATLauncherLaunchScript && javaPath != null
                            ? String.format(" ATLcustomjava %s", javaPath + "/bin/java ")
                            : ""),
                        args);

                    if (Utils.executableInPath("exo-open")) {
                        arguments.add("exo-open");
                        arguments.add("--launch");
                        arguments.add("TerminalEmulator");
                        arguments.add("--working-directory");
                        arguments.add(getRoot().toAbsolutePath().toString());
                        arguments.add(format);
                    } else if (Utils.executableInPath("kitty")) {
                        arguments.add("kitty");
                        arguments.addAll(Arrays.asList(format
                            .split(" ")));
                    } else if (Utils.executableInPath("alacritty")) {
                        arguments.add("alacritty");
                        arguments.add("-e");
                        arguments.addAll(Arrays.asList(format
                            .split(" ")));
                    } else if (Utils.executableInPath("gnome-terminal")) {
                        arguments.add("gnome-terminal");
                        arguments.add("--");
                        arguments.addAll(Arrays.asList(format
                            .split(" ")));
                    } else {
                        DialogManager.okDialog().setTitle(GetText.tr("Failed To Launch Server"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "The server couldn't be launched as we don't know how to launch it.<br/><br/>Please open the server folder and run the {0} file manually.",
                                    serverScript))
                                .build())
                            .setType(DialogManager.ERROR).show();
                        return;
                    }
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
                    Files.write(tempLaunchFile, String.join(" ", launchScript).getBytes(StandardCharsets.UTF_8),
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
                LogManager.info(
                    "Server has started. No further logs will be shown in this console. Please check for a separate window or tab for the server logs and provide those logs (not these ones) if asking for help.");
                Toaster.instance().pop(GetText.tr("The server has been launched."));
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

        Path backupsPath = FileSystem.BACKUPS;
        if (App.settings.backupsPath != null) {
            backupsPath = Paths.get(App.settings.backupsPath);
        }

        Path backupZip = backupsPath.resolve(filename);

        // #. {0} is the name of the server we're backing up
        ProgressDialog<Boolean> progressDialog = new ProgressDialog<>(GetText.tr("Backing Up {0}", name));
        progressDialog.addThread(new Thread(() -> {
            boolean success = ArchiveUtils.createZip(getRoot(), backupZip);

            progressDialog.setReturnValue(success);
            progressDialog.close();
        }));
        progressDialog.start();

        if (Boolean.TRUE.equals(progressDialog.getReturnValue())) {
            App.TOASTER.pop(GetText.tr("Backup is complete"));
            LogManager.info(String.format("Backup complete and stored at %s", backupZip.toString()));
        } else {
            App.TOASTER.popError(GetText.tr("Error making backup"));
        }
    }

    public boolean hasUpdate() {
        Pack packObject = this.getPack();

        if (packObject != null) {
            if (packObject.hasVersions() && !this.isDev) {
                // Lastly check if the current version we installed is different than the latest
                // version of the Pack and that the latest version of the Pack is not restricted
                // to disallow updates.
                if (!packObject.getLatestVersion().version.equalsIgnoreCase(this.version)
                    && !packObject.isLatestVersionNoUpdate()) {
                    return true;
                }
            }

            if (this.isDev && (this.hash != null)) {
                PackVersion devVersion = packObject.getDevVersionByName(this.version);
                return devVersion != null && !devVersion.hashMatches(this.hash);
            }
        }

        return false;
    }

    public PackVersion getLatestVersion() {
        Pack packObject = this.getPack();

        if (packObject != null) {
            if (packObject.hasVersions() && !this.isDev) {
                return packObject.getLatestVersion();
            }

            if (this.isDev) {
                return packObject.getLatestDevVersion();
            }
        }

        return null;
    }

    public String getPackDescription() {
        Pack packObject = this.getPack();

        if (packObject != null) {
            return packObject.description;
        } else {
            if (description != null) {
                return description;
            }

            return GetText.tr("No Description");
        }
    }

    public ImageIcon getImage() {
        File customImage = this.getRoot().resolve("server.png").toFile();

        if (customImage.exists()) {
            try {
                BufferedImage img = ImageIO.read(customImage);
                if (img != null) {
                    // if a square image, then make it 300x150 (without stretching) centered
                    if (img.getHeight(null) == img.getWidth(null)) {
                        BufferedImage dimg = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);

                        Graphics2D g2d = dimg.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2d.drawImage(img, 75, 0, 150, 150, null);
                        g2d.dispose();

                        return new ImageIcon(dimg);
                    }

                    return new ImageIcon(img.getScaledInstance(300, 150, Image.SCALE_SMOOTH));
                }
            } catch (IOException e) {
                LogManager.warn("Error creating scaled image from the custom image of server " + this.name
                    + ". Using default image.");
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

    public void startChangeDescription() {
        JTextArea textArea = new JTextArea(description);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(300, 150);

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), new JScrollPane(textArea),
            GetText.tr("Changing Description"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret == 0) {
            Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_description_change", this));
            description = textArea.getText();
            save();
        }
    }

    public void startChangeImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
        int ret = chooser.showOpenDialog(App.launcher.getParent());
        if (ret == JFileChooser.APPROVE_OPTION) {
            File img = chooser.getSelectedFile();
            if (img.getAbsolutePath().endsWith(".png")) {
                Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_image_change", this));
                try {
                    Utils.safeCopy(img, getRoot().resolve("server.png").toFile());
                    save();
                } catch (IOException ex) {
                    LogManager.logStackTrace("Failed to set server image", ex);
                }
            }
        }
    }

    public boolean isCurseForgePack() {
        return curseForgeProject != null && curseForgeFile != null;
    }

    public boolean isModrinthPack() {
        return modrinthManifest != null && modrinthProject != null
            && modrinthVersion != null;
    }

    public boolean showGetHelpButton() {
        if (getPack() != null || isModrinthPack() || isCurseForgePack()) {
            return getDiscordInviteUrl() != null || getSupportUrl() != null || getWikiUrl() != null
                || getSourceUrl() != null;
        }

        return false;
    }

    public String getDiscordInviteUrl() {
        if (getPack() != null) {
            return getPack().discordInviteURL;
        }

        if (isModrinthPack()) {
            return modrinthProject.discordUrl;
        }

        return null;
    }

    public String getSupportUrl() {
        if (getPack() != null) {
            return getPack().supportURL;
        }

        if (isModrinthPack()) {
            return modrinthProject.issuesUrl;
        }

        if (isCurseForgePack() && curseForgeProject.hasIssuesUrl()) {
            return curseForgeProject.getIssuesUrl();
        }

        return null;
    }

    public boolean hasWebsite() {
        if (getPack() != null) {
            return getPack().websiteURL != null;
        }

        if (isCurseForgePack()) {
            return curseForgeProject.hasWebsiteUrl();
        }

        return isModrinthPack();
    }

    public String getWebsiteUrl() {
        if (getPack() != null) {
            return getPack().websiteURL;
        }

        if (isCurseForgePack() && curseForgeProject.hasWebsiteUrl()) {
            return curseForgeProject.getWebsiteUrl();
        }

        if (isModrinthPack()) {
            return String.format("https://modrinth.com/modpack/%s", modrinthProject.slug);
        }

        return null;
    }

    public String getWikiUrl() {
        if (isModrinthPack()) {
            return modrinthProject.wikiUrl;
        }

        if (isCurseForgePack() && curseForgeProject.hasWikiUrl()) {
            return curseForgeProject.getWikiUrl();
        }

        return null;
    }

    public String getSourceUrl() {
        if (isModrinthPack()) {
            return modrinthProject.sourceUrl;
        }

        return null;
    }

    @Override
    public void save() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            Files.newOutputStream(this.getRoot().resolve("server.json")), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(this, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMinecraftVersion() {
        return version;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return loaderVersion;
    }

    @Override
    public boolean supportsPlugins() {
        return loaderVersion != null && (loaderVersion.isPaper() || loaderVersion.isPurpur());
    }

    @Override
    public boolean isForgeLikeAndHasInstalledSinytraConnector() {
        if (loaderVersion == null || !(loaderVersion.isForge()
            || loaderVersion.isNeoForge())) {
            return false;
        }

        return mods.stream().anyMatch(m -> (m.isFromCurseForge()
            && m.getCurseForgeModId() == Constants.CURSEFORGE_SINYTRA_CONNECTOR_MOD_ID)
            || (m.isFromModrinth()
            && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_SINYTRA_CONNECTOR_MOD_ID)))
            && App.settings.showFabricModsWhenSinytraInstalled;
    }

    @Override
    public List<DisableableMod> getMods() {
        return mods;
    }

    @Override
    public void addMod(DisableableMod mod) {
        mods.add(mod);
    }

    @Override
    public void addMods(List<DisableableMod> modsToAdd) {
        mods.addAll(modsToAdd);
    }

    @Override
    public void removeMod(DisableableMod mod) {
        mods.remove(mod);
        FileUtils.delete(
            (mod.isDisabled()
                ? mod.getDisabledFile(this)
                : mod.getFile(this)).toPath(),
            true);
        save();

        // #. {0} is the name of a mod that was removed
        App.TOASTER.pop(GetText.tr("{0} Removed", mod.name));
    }

    @Override
    public void addFileFromCurseForge(CurseForgeProject mod, CurseForgeFile file, ProgressDialog<Void> dialog) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.getInstanceDirectoryPath(this.getRoot()).resolve(file.fileName);

        // find mods with the same CurseForge project id
        List<DisableableMod> sameMods = this.mods.stream()
            .filter(installedMod -> installedMod.isFromCurseForge()
                && installedMod.getCurseForgeModId() == mod.id)
            .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        Optional<CurseForgeFileHash> md5Hash = file.hashes.stream().filter(CurseForgeFileHash::isMd5)
            .findFirst();
        Optional<CurseForgeFileHash> sha1Hash = file.hashes.stream().filter(CurseForgeFileHash::isSha1)
            .findFirst();

        if (file.downloadUrl == null) {
            if (!App.settings.seenCurseForgeProjectDistributionDialog) {
                App.settings.seenCurseForgeProjectDistributionDialog = true;
                App.settings.save();

                DialogManager.okDialog().setType(DialogManager.WARNING)
                    .setTitle(GetText.tr("Mod Not Available"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We were unable to download this mod.<br/>This is likely due to the author of the mod disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to download the mod manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, we can continue with installing the mod.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                        .build())
                    .show();
            }

            dialog.setIndeterminate();
            String filename = file.fileName;
            String filename2 = file.fileName.replace(" ", "+");
            File fileLocation = downloadLocation.toFile();
            File fileLocation2 = FileSystem.DOWNLOADS.resolve(filename2).toFile();
            // if file downloaded already, but hashes don't match, delete it
            if (fileLocation.exists()
                && ((md5Hash.isPresent()
                && !Hashing.md5(fileLocation.toPath()).equals(Hashing.toHashCode(md5Hash.get().value)))
                || (sha1Hash.isPresent()
                && !Hashing.sha1(fileLocation.toPath())
                .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                FileUtils.delete(fileLocation.toPath());
            } else if (fileLocation2.exists()
                && ((md5Hash.isPresent()
                && !Hashing.md5(fileLocation2.toPath()).equals(Hashing.toHashCode(md5Hash.get().value)))
                || (sha1Hash.isPresent()
                && !Hashing.sha1(fileLocation2.toPath())
                .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                FileUtils.delete(fileLocation2.toPath());
            }

            if (!fileLocation.exists() && !fileLocation2.exists()) {
                File downloadsFolderFile = new File(FileSystem.getUserDownloadsPath().toFile(), filename);
                File downloadsFolderFile2 = new File(FileSystem.getUserDownloadsPath().toFile(), filename2);
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                } else if (downloadsFolderFile2.exists()) {
                    Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                }

                while (!fileLocation.exists() && !fileLocation2.exists()) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            OS.openWebBrowser(mod.getBrowserDownloadUrl(file));
                        }

                        retValue = DialogManager.optionDialog()
                            .setTitle(GetText.tr("Downloading") + " "
                                + filename)
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "Browser opened to download file {0}",
                                    filename)
                                    + "<br/><br/>" + GetText.tr("Please save this file to the following location")
                                    + "<br/><br/>"
                                    + (OS.isUsingMacApp()
                                    ? FileSystem.getUserDownloadsPath().toFile().getAbsolutePath()
                                    : FileSystem.DOWNLOADS.toAbsolutePath().toString()
                                        + " or<br/>"
                                        + FileSystem.getUserDownloadsPath().toFile()))
                                .build())
                            .addOption(GetText.tr("Open Folder"), true)
                            .addOption(GetText.tr("I've Downloaded This File")).setType(DialogManager.INFO)
                            .showWithFileMonitoring(file.fileLength, 1, fileLocation, fileLocation2,
                                downloadsFolderFile, downloadsFolderFile2);

                        if (retValue == DialogManager.CLOSED_OPTION) {
                            return;
                        } else if (retValue == 0) {
                            OS.openFileExplorer(FileSystem.DOWNLOADS);
                        }
                    } while (retValue != 1);

                    if (!fileLocation.exists() && !fileLocation2.exists()) {
                        // Check users downloads folder to see if it's there
                        if (downloadsFolderFile.exists()) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
                        } else if (downloadsFolderFile2.exists()) {
                            Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                        }
                        // Check to see if a browser has added a .zip to the end of the file
                        File zipAddedFile = FileSystem.DOWNLOADS.resolve(file.fileName + ".zip").toFile();
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(FileSystem.getUserDownloadsPath().toFile(), file.fileName + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }

                    // file downloaded, but hashes don't match, delete it
                    if (fileLocation.exists()
                        && ((md5Hash.isPresent() && !Hashing.md5(fileLocation.toPath())
                        .equals(Hashing.toHashCode(md5Hash.get().value)))
                        || (sha1Hash.isPresent()
                        && !Hashing.sha1(fileLocation.toPath())
                        .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                        FileUtils.delete(fileLocation.toPath());
                    } else if (fileLocation2.exists()
                        && ((md5Hash.isPresent() && !Hashing.md5(fileLocation2.toPath())
                        .equals(Hashing.toHashCode(md5Hash.get().value)))
                        || (sha1Hash.isPresent()
                        && !Hashing.sha1(fileLocation2.toPath())
                        .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                        FileUtils.delete(fileLocation2.toPath());
                    }
                }
            }

            if (Files.exists(finalLocation)) {
                FileUtils.delete(finalLocation);
            }

            if (!Files.isDirectory(finalLocation.getParent())) {
                FileUtils.createDirectory(finalLocation.getParent());
            }

            FileUtils.copyFile(downloadLocation, finalLocation, true);
        } else {
            com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(file.downloadUrl)
                .downloadTo(downloadLocation).size(file.fileLength)
                .withHttpClient(Network.createProgressClient(dialog));

            dialog.setTotalBytes(file.fileLength);

            if (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
                download = download.unzipTo(this.getRoot().resolve("saves"));
            } else {
                download = download.copyTo(finalLocation);
                if (Files.exists(finalLocation)) {
                    FileUtils.delete(finalLocation);
                }
            }

            if (md5Hash.isPresent()) {
                download = download.hash(md5Hash.get().value);
            } else if (sha1Hash.isPresent()) {
                download = download.hash(sha1Hash.get().value);
            }

            if (download.needToDownload()) {
                try {
                    download.downloadFile();
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                    DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                        .setContent("Failed to download " + file.fileName + ". Please try again later.").show();
                    return;
                }
            } else {
                download.copy();
            }
        }

        // remove any mods that are from the same mod on CurseForge from the master mod
        // list
        this.mods = this.mods.stream()
            .filter(installedMod -> !installedMod.isFromCurseForge() || installedMod.getCurseForgeModId() != mod.id)
            .collect(Collectors.toList());

        DisableableMod dm = new DisableableMod(mod.name, file.displayName, true, file.fileName,
            mod.getRootCategoryId() == Constants.CURSEFORGE_PLUGINS_SECTION_ID ? Type.plugins : Type.mods,
            null, mod.summary, false, true, true, false, mod, file);

        // check for mod on Modrinth
        if (!App.settings.dontCheckModsOnModrinth) {
            ModrinthVersion modVersion = ModrinthApi.getVersionFromSha1Hash(Hashing.sha1(finalLocation).toString());
            if (modVersion != null) {
                ModrinthProject project = ModrinthApi.getProject(modVersion.projectId);
                if (project != null) {
                    // add Modrinth information
                    dm.modrinthProject = project;
                    dm.modrinthVersion = modVersion;
                }
            }
        }

        // add this mod to the instance (if not a world)
        if (dm.type != com.atlauncher.data.Type.worlds) {
            mods.add(dm);
            this.save();
        }

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
    }

    @Override
    public void addFileFromModrinth(ModrinthProject project, ModrinthVersion version, ModrinthFile file,
        ProgressDialog<Void> dialog) {
        ModrinthFile fileToDownload = Optional.ofNullable(file).orElse(version.getPrimaryFile());
        boolean isMod = project.projectType == ModrinthProjectType.MOD && !version.loaders.contains("purpur")
            && !version.loaders.contains("paper")
            && !version.loaders.contains("spigot") && !version.loaders.contains("bukkit");

        Path downloadLocation = FileSystem.DOWNLOADS.resolve(fileToDownload.filename);
        Path finalLocation = isMod
            ? this.getRoot().resolve("mods").resolve(fileToDownload.filename)
            : this.getRoot().resolve("plugins").resolve(fileToDownload.filename);
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(fileToDownload.url)
            .downloadTo(downloadLocation).copyTo(finalLocation)
            .withHttpClient(Network.createProgressClient(dialog));

        if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha512")) {
            download = download.hash(fileToDownload.hashes.get("sha512"));
        } else if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha1")) {
            download = download.hash(fileToDownload.hashes.get("sha1"));
        }

        if (fileToDownload.size != null && fileToDownload.size != 0) {
            dialog.setTotalBytes(fileToDownload.size);
            download = download.size(fileToDownload.size);
        }

        if (Files.exists(finalLocation)) {
            FileUtils.delete(finalLocation);
        }

        // find mods with the same Modrinth id
        List<DisableableMod> sameMods = this.mods.stream().filter(
                installedMod -> installedMod.isFromModrinth()
                    && installedMod.modrinthProject.id.equalsIgnoreCase(project.id))
            .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        if (download.needToDownload()) {
            try {
                download.downloadFile();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                    .setContent("Failed to download " + fileToDownload.filename + ". Please try again later.")
                    .show();
                return;
            }
        } else {
            download.copy();
        }

        // remove any mods that are from the same mod from the master mod list
        this.mods = this.mods.stream().filter(
                installedMod -> !installedMod.isFromModrinth()
                    || !installedMod.modrinthProject.id.equalsIgnoreCase(project.id))
            .collect(Collectors.toList());

        DisableableMod dm = new DisableableMod(project.title, version.name, true, fileToDownload.filename,
            isMod ? Type.mods : Type.plugins,
            null, project.description, false, true, true, false, project, version);

        // check for mod on CurseForge
        if (!App.settings.dontCheckModsOnCurseForge) {
            try {
                CurseForgeFingerprint fingerprint = CurseForgeApi
                    .checkFingerprints(new Long[] { Hashing.murmur(finalLocation) });

                if (fingerprint != null && fingerprint.exactMatches != null && fingerprint.exactMatches.size() == 1) {
                    CurseForgeFingerprintedMod foundMod = fingerprint.exactMatches.get(0);

                    CurseForgeProject cfProject = CurseForgeApi.getProjectById(foundMod.id);
                    if (cfProject != null && cfProject.status == 4) {
                        dm.curseForgeProjectId = foundMod.id;
                        dm.curseForgeFile = foundMod.file;
                        dm.curseForgeFileId = foundMod.file.id;
                        dm.curseForgeProject = cfProject;
                    }
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        // add this mod
        this.mods.add(dm);
        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", project.title));
    }

    @Override
    public void scanMissingMods(Window parent) {
        PerformanceManager.start("Server::scanMissingMods - CheckForAddedMods");

        // files to scan
        List<Path> files = new ArrayList<>();

        // find the mods/plugins that have been added by the user manually
        for (Path path : Arrays.asList(ROOT.resolve("mods"), ROOT.resolve("plugins"))) {
            if (!Files.exists(path)) {
                continue;
            }

            com.atlauncher.data.Type fileType = path.equals(ROOT.resolve("plugins")) ? com.atlauncher.data.Type.plugins
                : com.atlauncher.data.Type.mods;

            try (Stream<Path> stream = Files.list(path)) {
                files.addAll(stream
                    .filter(file -> !Files.isDirectory(file) && Utils.isAcceptedModFile(file)).filter(
                        file -> mods.stream()
                            .noneMatch(mod -> mod.type == fileType
                                && mod.file.equals(file.getFileName().toString())))
                    .collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace("Error scanning missing mods", e);
            }
        }

        if (!files.isEmpty()) {
            final ProgressDialog<Void> progressDialog = new ProgressDialog<>(GetText.tr("Scanning New Mods"), 0,
                GetText.tr("Scanning New Mods"), parent);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> allMods = files.parallelStream()
                    .map(file -> {
                        com.atlauncher.data.Type fileType = file.getParent().equals(ROOT.resolve("plugins"))
                            ? com.atlauncher.data.Type.plugins
                            : com.atlauncher.data.Type.mods;

                        return DisableableMod.generateMod(file.toFile(), fileType,
                            !file.getParent().equals(ROOT.resolve("disabledmods")));
                    })
                    .collect(Collectors.toList());

                if (!App.settings.dontCheckModsOnCurseForge) {
                    Map<Long, DisableableMod> murmurHashes = new HashMap<>();

                    allMods.stream()
                        .filter(dm -> dm.curseForgeProject == null && dm.curseForgeFile == null)
                        .filter(dm -> dm.getFile(ROOT, version) != null).forEach(dm -> {
                            try {
                                long murmurHash = Hashing
                                    .murmur(dm.disabled ? dm.getDisabledFile(this).toPath()
                                        : dm
                                            .getFile(ROOT, version).toPath());
                                murmurHashes.put(murmurHash, dm);
                            } catch (IOException e) {
                                LogManager.logStackTrace(e);
                            }
                        });

                    if (!murmurHashes.isEmpty()) {
                        CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                            .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

                        if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                            int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                                .toArray();

                            if (projectIdsFound.length != 0) {
                                Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                                    .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    fingerprintResponse.exactMatches.stream()
                                        .filter(em -> em != null && em.file != null
                                            && murmurHashes.containsKey(em.file.packageFingerprint))
                                        .forEach(foundMod -> {
                                            DisableableMod dm = murmurHashes
                                                .get(foundMod.file.packageFingerprint);

                                            CurseForgeProject cfProject = foundProjects
                                                .get(foundMod.id);

                                            if (cfProject != null && cfProject.status == 4) {
                                                dm.curseForgeProjectId = foundMod.id;
                                                dm.curseForgeFile = foundMod.file;
                                                dm.curseForgeFileId = foundMod.file.id;
                                                dm.curseForgeProject = cfProject;
                                                dm.name = cfProject.name;
                                                dm.description = cfProject.summary;

                                                LogManager.debug("Found matching mod from CurseForge called "
                                                    + dm.curseForgeFile.displayName);
                                            }

                                            // reset if the file is not approved
                                            if (cfProject != null && cfProject.status != 4) {
                                                dm.curseForgeProjectId = null;
                                                dm.curseForgeFile = null;
                                                dm.curseForgeFileId = null;
                                                dm.curseForgeProject = null;

                                                File path = dm.getFile(this);
                                                MCMod mcMod = Utils.getMCModForFile(path);
                                                if (mcMod != null) {
                                                    dm.name = Optional.ofNullable(mcMod.name)
                                                        .orElse(path.getName());
                                                    dm.description = mcMod.description;
                                                } else {
                                                    FabricMod fabricMod = Utils.getFabricModForFile(path);
                                                    if (fabricMod != null) {
                                                        dm.name = Optional.ofNullable(fabricMod.name)
                                                            .orElse(path.getName());
                                                        dm.description = fabricMod.description;
                                                    }
                                                }
                                            }
                                        });
                                }
                            }
                        }
                    }
                }

                if (!App.settings.dontCheckModsOnModrinth) {
                    Map<String, DisableableMod> sha1Hashes = new HashMap<>();

                    allMods.stream()
                        .filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
                        .filter(dm -> dm.getFile(ROOT, version) != null).forEach(dm -> {
                            try {
                                sha1Hashes.put(Hashing
                                    .sha1(dm.disabled ? dm.getDisabledFile(this).toPath()
                                        : dm
                                            .getFile(ROOT, version).toPath())
                                    .toString(), dm);
                            } catch (Throwable t) {
                                LogManager.logStackTrace(t);
                            }
                        });

                    if (!sha1Hashes.isEmpty()) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                            .getVersionsFromSha1Hashes(keys.toArray(new String[0]));

                        if (modrinthVersions != null && !modrinthVersions.isEmpty()) {
                            String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                .toArray(String[]::new);

                            if (projectIdsFound.length != 0) {
                                Map<String, ModrinthProject> foundProjects = ModrinthApi
                                    .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                        ModrinthVersion mrVersion = entry.getValue();
                                        ModrinthProject mrProject = foundProjects.get(mrVersion.projectId);

                                        if (mrProject != null) {
                                            DisableableMod dm = sha1Hashes.get(entry.getKey());

                                            // add Modrinth information
                                            dm.modrinthProject = mrProject;
                                            dm.modrinthVersion = mrVersion;

                                            if (!dm.isFromCurseForge()
                                                || App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                                                dm.name = mrProject.title;
                                                dm.description = mrProject.description;
                                            }

                                            LogManager.debug(String.format(
                                                "Found matching mod from Modrinth called %s with file %s",
                                                mrProject.title, mrVersion.name));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                allMods.forEach(mod -> LogManager.info("Found extra mod with name of " + mod.file));
                mods.addAll(allMods);
                save();
                progressDialog.close();
            }));

            progressDialog.start();
        }
        PerformanceManager.end("Server::scanMissingMods - CheckForAddedMods");

        PerformanceManager.start("Server::scanMissingMods - CheckForRemovedMods");

        // next remove any mods that the no longer exist in the filesystem
        List<DisableableMod> removedMods = mods.parallelStream().filter(mod -> {
            if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                return false;
            }

            if (mod.disabled) {
                return (mod.getDisabledFile(this) != null && !mod.getDisabledFile(this).exists());
            } else {
                return (mod.getFile(this) != null && !mod.getFile(this).exists());
            }
        }).collect(Collectors.toList());

        if (!removedMods.isEmpty()) {
            removedMods.forEach(mod -> LogManager.info("Mod no longer in filesystem: " + mod.file));
            mods.removeAll(removedMods);
            save();
        }
        PerformanceManager.end("Server::scanMissingMods - CheckForRemovedMods");

        PerformanceManager.start("Server::scanMissingMods - CheckForDuplicateMods");

        Set<File> seenModFiles = new HashSet<>();
        List<DisableableMod> duplicateMods = mods.stream()
            .filter(mod -> {
                if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                    return false;
                }

                File file;
                if (mod.disabled) {
                    file = mod.getDisabledFile(this);
                } else {
                    file = mod.getFile(this);
                }

                if (file == null) {
                    return false;
                }

                return !seenModFiles.add(file);
            })
            .collect(Collectors.toList());

        if (!duplicateMods.isEmpty()) {
            duplicateMods.forEach(mod -> LogManager.info("Mod is a duplicate: " + mod.file));
            mods.removeAll(duplicateMods);
            save();
        }
        PerformanceManager.end("Server::scanMissingMods - CheckForDuplicateMods");
    }
}
