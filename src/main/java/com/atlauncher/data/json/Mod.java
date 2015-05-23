/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.data.json;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Language;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Json
public class Mod {
    private String name;
    private String version;
    private String url;
    private String file;
    private String md5;
    private int filesize;
    private DownloadType download;
    private String website;
    private String donation;
    private List<String> authors;
    private String sha1;
    private String colour;
    private String warning;
    private Color compiledColour;
    private ModType type;
    private ExtractToType extractTo;
    private String extractFolder;
    private String decompFile;
    private DecompType decompType;
    private boolean filePattern = false;
    private String filePreference;
    private String fileCheck;
    private boolean client = true;
    private boolean server = true;
    private boolean serverSeparate = false;
    private String serverUrl;
    private String serverFile;
    private ModType serverType;
    private DownloadType serverDownload;
    private String serverMD5;
    private Boolean serverOptional;
    private boolean optional = false;
    private boolean selected = false;
    private boolean recommended = true;
    private boolean hidden = false;
    private boolean library = false;
    private String group;
    private String linked;
    private List<String> depends;
    private String filePrefix;
    private String description;

    public String getName() {
        return this.name;
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getVersion() {
        return this.version;
    }

    public String getUrl() {
        return this.url.replace("&amp;", "&").replace(" ", "%20");
    }

    public String getRawFile() {
        return this.file;
    }

    public String getFile() {
        if (this.hasFilePrefix()) {
            return this.filePrefix + this.file;
        }
        return this.file;
    }

    public String getMD5() {
        return this.md5;
    }

    public boolean hasMD5() {
        return this.md5 != null;
    }

    public int getFilesize() {
        return this.filesize;
    }

    public DownloadType getDownload() {
        return this.download;
    }

    public boolean hasWebsite() {
        return this.website != null;
    }

    public String getWebsite() {
        return this.website;
    }

    public String getDonation() {
        return this.donation;
    }

    public List<String> getAuthors() {
        return this.authors;
    }

    public String getPrintableAuthors() {
        StringBuilder sb = new StringBuilder();
        for (String author : this.authors) {
            sb.append(author + ", ");
        }
        return sb.toString();
    }

    public String getSha1() {
        return this.sha1;
    }

    public boolean hasColour() {
        return this.colour != null;
    }

    public String getColour() {
        return this.colour;
    }

    public boolean hasWarning() {
        return this.warning != null;
    }

    public String getWarning() {
        return this.warning;
    }

    public Color getCompiledColour() {
        return this.compiledColour;
    }

    public void setCompiledColour(Color colour) {
        this.compiledColour = colour;
    }

    public ModType getType() {
        return this.type;
    }

    public ExtractToType getExtractTo() {
        return this.extractTo;
    }

    public String getExtractFolder() {
        return this.extractFolder;
    }

    public String getDecompFile() {
        return this.decompFile;
    }

    public DecompType getDecompType() {
        return this.decompType;
    }

    public boolean isFilePattern() {
        return this.filePattern;
    }

    public String getFilePreference() {
        return this.filePreference;
    }

    public String getFileCheck() {
        return this.fileCheck;
    }

    public boolean installOnClient() {
        return this.client;
    }

    public boolean installOnServer() {
        return this.server;
    }

    public boolean isServerSeparate() {
        return this.serverSeparate;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public String getServerFile() {
        return this.serverFile;
    }

    public ModType getServerType() {
        return this.serverType;
    }

    public DownloadType getServerDownload() {
        return this.serverDownload;
    }

    public String getServerMD5() {
        return this.serverMD5;
    }

    public boolean hasServerMD5() {
        return this.serverMD5 != null;
    }

    public boolean isServerOptional() {
        return (this.serverOptional == null ? this.optional : this.serverOptional);
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isRecommended() {
        return this.recommended;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isLibrary() {
        return this.library;
    }

    public String getGroup() {
        return this.group;
    }

    public String getLinked() {
        return this.linked;
    }

    public List<String> getDepends() {
        return this.depends;
    }

    public String getFilePrefix() {
        return this.filePrefix;
    }

    public boolean hasFilePrefix() {
        return this.filePrefix != null;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean hasDepends() {
        return this.depends != null && this.depends.size() != 0;
    }

    public boolean isADependancy(Mod mod) {
        for (String name : this.depends) {
            if (name.equalsIgnoreCase(mod.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGroup() {
        return this.group != null && !this.group.isEmpty();
    }

    public boolean hasLinked() {
        return this.linked != null && !this.linked.isEmpty();
    }

    public boolean hasDescription() {
        return this.description != null && !this.description.isEmpty();
    }

    public FilenameFilter getFileNameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(file);
            }
        };
    }

    public void download(InstanceInstaller installer) {
        download(installer, 1);
    }

    public void download(InstanceInstaller installer, int attempt) {
        if (installer.isServer() && this.serverUrl != null) {
            downloadServer(installer, attempt);
        } else {
            downloadClient(installer, attempt);
        }
    }

    public void downloadClient(InstanceInstaller installer, int attempt) {
        Path fileLocation = FileSystem.DOWNLOADS.resolve(getFile());

        if (Files.exists(fileLocation)) {
            if (hasMD5()) {
                if (Utils.getMD5(fileLocation).equalsIgnoreCase(this.md5)) {
                    return; // File already exists and matches hash, don't download it
                } else {
                    Utils.delete(fileLocation); // File exists but is corrupt, delete it
                }
            } else {
                long size = 0;

                try {
                    size = Files.size(fileLocation);
                } catch (IOException e) {
                    App.settings.logStackTrace("Error getting file size of " + fileLocation, e);
                }

                if (size != 0) {
                    return; // No MD5, but file is there, can only assume it's fine
                }
            }
        }

        switch (this.download) {
            case browser:
                Path downloadsFolderFile = FileSystem.USER_DOWNLOADS.resolve(getFile());

                if (Files.exists(downloadsFolderFile)) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                }

                if (fileCheck != null && fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() : FileSystem
                            .DOWNLOADS.toFile()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                .DOWNLOADS).resolve(files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                        .DOWNLOADS).resolve(files[i]);
                                break;
                            }

                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                        .DOWNLOADS).resolve(files[i]);
                                break;
                            }
                        }
                    }
                }

                while (!Files.exists(fileLocation)) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            Utils.openBrowser(this.getUrl());
                        }
                        String[] options = new String[]{Language.INSTANCE.localize("common.openfolder"), Language
                                .INSTANCE.localize("instance.ivedownloaded")};
                        retValue = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                                (Language.INSTANCE.localizeWithReplace("instance" + "" +
                                ".browseropened", (serverFile == null ? (isFilePattern() ? getName() : getFile()) :
                                        (isFilePattern() ? getName() : getServerFile()))) + "<br/><br/>" +
                                Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>" +
                                (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : (isFilePattern() ?
                                        FileSystem.DOWNLOADS : FileSystem.DOWNLOADS + " or<br/>" + FileSystem
                                        .USER_DOWNLOADS))), Language.INSTANCE.localize("common.downloading") + " " +
                                (serverFile == null ? (isFilePattern() ? getName() : getFile()) : (isFilePattern() ?
                                        getName() : getServerFile())), JOptionPane.DEFAULT_OPTION, JOptionPane
                                .INFORMATION_MESSAGE, null, options, options[0]);
                        if (retValue == JOptionPane.CLOSED_OPTION) {
                            installer.cancel(true);
                            return;
                        } else if (retValue == 0) {
                            Utils.openExplorer(FileSystem.USER_DOWNLOADS);
                        }
                    } while (retValue != 1);

                    if (isFilePattern()) {
                        String[] files = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() :
                                FileSystem.DOWNLOADS.toFile()).list(getFileNameFilter());
                        if (files.length == 1) {
                            this.file = files[0];
                            fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                    .DOWNLOADS).resolve(files[0]);
                        } else if (files.length > 1) {
                            for (int i = 0; i < files.length; i++) {
                                if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                    this.file = files[i];
                                    fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS :
                                            FileSystem.DOWNLOADS).resolve(files[i]);
                                    break;
                                }
                                if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                    this.file = files[i];
                                    fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS :
                                            FileSystem.DOWNLOADS).resolve(files[i]);
                                    break;
                                }
                            }
                        }
                    } else {
                        if (!Files.exists(fileLocation)) {
                            // Check users downloads folder to see if it's there
                            if (Files.exists(downloadsFolderFile)) {
                                Utils.moveFile(downloadsFolderFile, fileLocation, true);
                            }

                            // Check to see if a browser has added a .zip to the end of the file
                            Path zipAddedFile = FileSystem.DOWNLOADS.resolve(getFile() + ".zip");
                            if (Files.exists(zipAddedFile)) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            } else {
                                zipAddedFile = FileSystem.USER_DOWNLOADS.resolve(getFile() + ".zip");
                                if (Files.exists(zipAddedFile)) {
                                    Utils.moveFile(zipAddedFile, fileLocation, true);
                                }
                            }
                        }
                    }
                }
                break;
            case direct:
                Downloadable download1 = new Downloadable(this.getUrl(), fileLocation, this.md5, installer, false);
                if (download1.needToDownload()) {
                    installer.resetDownloadedBytes(download1.getFilesize());
                    download1.download(true);
                }
                break;
            case server:
                Downloadable download2 = new Downloadable(this.getUrl(), fileLocation, this.md5, installer, true);
                if (download2.needToDownload()) {
                    download2.download(false);
                }
                break;
        }

        if (hasMD5() && !Utils.getMD5(fileLocation).equalsIgnoreCase(this.md5)) {
            if (attempt < 5) {
                Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                downloadClient(installer, ++attempt); // download again
            } else {
                LogManager.error("Cannot download " + fileLocation + ". Aborting install!");
                installer.cancel(true);
            }
        }
    }

    public void downloadServer(InstanceInstaller installer, int attempt) {
        Path fileLocation = FileSystem.DOWNLOADS.resolve(getServerFile());
        if (Files.exists(fileLocation)) {
            if (this.hasServerMD5()) {
                if (Utils.getMD5(fileLocation).equalsIgnoreCase(this.serverMD5)) {
                    return; // File already exists and matches hash, don't download it
                } else {
                    Utils.delete(fileLocation); // File exists but is corrupt, delete it
                }
            } else {
                return; // No MD5, but file is there, can only assume it's fine
            }
        }
        if (this.serverDownload == DownloadType.browser) {
            Path downloadsFolderFile = FileSystem.USER_DOWNLOADS.resolve(getServerFile());
            if (Files.exists(downloadsFolderFile)) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }

            if (fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() : FileSystem
                        .DOWNLOADS.toFile()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem.DOWNLOADS)
                            .resolve(files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                    .DOWNLOADS).resolve(files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                    .DOWNLOADS).resolve(files[i]);
                            break;
                        }
                    }
                }
            }

            while (!Files.exists(fileLocation)) {
                Utils.openBrowser(this.serverUrl);
                String[] options = new String[]{Language.INSTANCE.localize("instance.ivedownloaded")};
                int retValue = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                        (Language.INSTANCE.localizeWithReplace("instance" + "" +
                        ".browseropened", (serverFile == null ? getFile() : getServerFile())) +
                        "<br/><br/>" + Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>" +
                        (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem.DOWNLOADS + " " +
                                "or<br/>" + FileSystem.USER_DOWNLOADS)), Language.INSTANCE.localize("common" + "" +
                        ".downloading") + " " + (serverFile == null ? getFile() : getServerFile()), JOptionPane
                        .DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (retValue == JOptionPane.CLOSED_OPTION) {
                    installer.cancel(true);
                    return;
                }

                if (isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() : FileSystem
                            .DOWNLOADS.toFile()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                .DOWNLOADS).resolve(files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                        .DOWNLOADS).resolve(files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : FileSystem
                                        .DOWNLOADS).resolve(files[i]);
                                break;
                            }
                        }
                    }
                } else {
                    if (!Files.exists(fileLocation)) {
                        // Check users downloads folder to see if it's there
                        if (Files.exists(downloadsFolderFile)) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
                        }

                        // Check to see if a browser has added a .zip to the end of the file
                        Path zipAddedFile = FileSystem.DOWNLOADS.resolve(getServerFile() + ".zip");
                        if (Files.exists(zipAddedFile)) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = FileSystem.USER_DOWNLOADS.resolve(getServerFile() + ".zip");
                            if (Files.exists(zipAddedFile)) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }
                }
            }
        } else {
            Downloadable download = new Downloadable(this.serverUrl, fileLocation, this.serverMD5, installer, this
                    .serverDownload == DownloadType.server);
            if (download.needToDownload()) {
                download.download(false);
            }
        }

        if (hasServerMD5() && !Utils.getMD5(fileLocation).equalsIgnoreCase(this.serverMD5)) {
            if (attempt < 5) {
                Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                downloadServer(installer, ++attempt); // download again
            } else {
                LogManager.error("Cannot download " + fileLocation + ". Aborting install!");
                installer.cancel(true);
            }
        } else {
            return; // No MD5, but file is there, can only assume it's fine
        }
    }

    public void install(InstanceInstaller installer) {
        Path fileLocation;
        ModType thisType;

        if (installer.isServer() && this.serverUrl != null) {
            fileLocation = FileSystem.DOWNLOADS.resolve(getServerFile());
            thisType = this.serverType;
        } else {
            fileLocation = FileSystem.DOWNLOADS.resolve(getFile());
            thisType = this.type;
        }

        switch (thisType) {
            case jar:
            case forge:
                if (installer.isServer() && thisType == ModType.forge) {
                    Utils.copyFile(fileLocation, installer.getRootDirectory());
                    break;
                } else if (installer.isServer() && thisType == ModType.jar) {
                    Utils.unzip(fileLocation, installer.getTempJarDirectory());
                    break;
                }

                Utils.copyFile(fileLocation, installer.getJarModsDirectory());
                installer.addToJarOrder(getFile());
                break;
            case mcpc:
                if (installer.isServer()) {
                    Utils.copyFile(fileLocation, installer.getRootDirectory());
                    break;
                }
                break;
            case texturepack:
                if (!Files.exists(installer.getTexturePacksDirectory())) {
                    try {
                        Files.createDirectory(installer.getTexturePacksDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getTexturePacksDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getTexturePacksDirectory());
                break;
            case resourcepack:
                if (!Files.exists(installer.getResourcePacksDirectory())) {
                    try {
                        Files.createDirectory(installer.getResourcePacksDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getResourcePacksDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getResourcePacksDirectory());
                break;
            case texturepackextract:
                if (!Files.exists(installer.getTexturePacksDirectory())) {
                    try {
                        Files.createDirectory(installer.getTexturePacksDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getTexturePacksDirectory(), e);
                    }
                }

                Utils.unzip(fileLocation, installer.getTempTexturePackDirectory());
                installer.setTexturePackExtracted();
                break;
            case resourcepackextract:
                if (!Files.exists(installer.getResourcePacksDirectory())) {
                    try {
                        Files.createDirectory(installer.getResourcePacksDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getResourcePacksDirectory(), e);
                    }
                }

                Utils.unzip(fileLocation, installer.getTempResourcePackDirectory());
                installer.setResourcePackExtracted();
                break;
            case millenaire:
                Path tempDirMillenaire = FileSystem.TMP.resolve(getSafeName());
                Utils.unzip(fileLocation, tempDirMillenaire);
                for (String folder : tempDirMillenaire.toFile().list()) {
                    File thisFolder = tempDirMillenaire.resolve(folder).toFile();
                    for (String dir : thisFolder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            File thisFile = new File(dir, name);
                            return thisFile.isDirectory();
                        }
                    })) {
                        Utils.copyDirectory(thisFolder.toPath().resolve(dir), installer.getModsDirectory());
                    }
                }
                Utils.delete(tempDirMillenaire);
                break;
            case mods:
                Utils.copyFile(fileLocation, installer.getModsDirectory());
                break;
            case ic2lib:
                if (!Files.exists(installer.getIC2LibDirectory())) {
                    try {
                        Files.createDirectory(installer.getIC2LibDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getIC2LibDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getIC2LibDirectory());
                break;
            case flan:
                if (!Files.exists(installer.getFlanDirectory())) {
                    try {
                        Files.createDirectory(installer.getFlanDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getFlanDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getFlanDirectory());
                break;
            case denlib:
                if (!Files.exists(installer.getDenLibDirectory())) {
                    try {
                        Files.createDirectory(installer.getDenLibDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getDenLibDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getDenLibDirectory());
                break;
            case depandency:
            case dependency:
                if (!Files.exists(installer.getDependencyDirectory())) {
                    try {
                        Files.createDirectory(installer.getDependencyDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getDependencyDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getDependencyDirectory());
                break;
            case plugins:
                if (!Files.exists(installer.getPluginsDirectory())) {
                    try {
                        Files.createDirectory(installer.getPluginsDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getPluginsDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getPluginsDirectory());
                break;
            case coremods:
                if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                    if (!Files.exists(installer.getCoreModsDirectory())) {
                        try {
                            Files.createDirectory(installer.getCoreModsDirectory());
                        } catch (IOException e) {
                            App.settings.logStackTrace("Error creating folder " + installer.getCoreModsDirectory(), e);
                        }
                    }

                    Utils.copyFile(fileLocation, installer.getCoreModsDirectory());
                } else {
                    Utils.copyFile(fileLocation, installer.getModsDirectory());
                }
                break;
            case shaderpack:
                if (!Files.exists(installer.getShaderPacksDirectory())) {
                    try {
                        Files.createDirectory(installer.getShaderPacksDirectory());
                    } catch (IOException e) {
                        App.settings.logStackTrace("Error creating folder " + installer.getShaderPacksDirectory(), e);
                    }
                }

                Utils.copyFile(fileLocation, installer.getShaderPacksDirectory());
                break;
            case extract:
                Path tempDirExtract = FileSystem.TMP.resolve(getSafeName());
                Utils.unzip(fileLocation, tempDirExtract);
                Path folder = FileSystem.TMP.resolve(getSafeName()).resolve(this.extractFolder);
                switch (extractTo) {
                    case coremods:
                        if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                            if (!Files.exists(installer.getCoreModsDirectory())) {
                                try {
                                    Files.createDirectory(installer.getCoreModsDirectory());
                                } catch (IOException e) {
                                    App.settings.logStackTrace("Error creating folder " + installer
                                            .getCoreModsDirectory(), e);
                                }
                            }

                            Utils.copyDirectory(folder, installer.getCoreModsDirectory());
                        } else {
                            Utils.copyDirectory(folder, installer.getModsDirectory());
                        }
                        break;
                    case mods:
                        Utils.copyDirectory(folder, installer.getModsDirectory());
                        break;
                    case root:
                        Utils.copyDirectory(folder, installer.getRootDirectory());
                        break;
                    default:
                        LogManager.error("No known way to extract mod " + this.name + " with type " + this.extractTo);
                        break;
                }
                Utils.delete(tempDirExtract);
                break;
            case decomp:
                Path tempDirDecomp = FileSystem.TMP.resolve(getSafeName());
                Utils.unzip(fileLocation, tempDirDecomp);
                Path tempFileDecomp = tempDirDecomp.resolve(decompFile);
                if (Files.exists(tempFileDecomp)) {
                    switch (decompType) {
                        case coremods:
                            if (Files.isRegularFile(tempFileDecomp)) {
                                if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                                    if (!Files.exists(installer.getCoreModsDirectory())) {
                                        try {
                                            Files.createDirectory(installer.getCoreModsDirectory());
                                        } catch (IOException e) {
                                            App.settings.logStackTrace("Error creating folder " + installer
                                                    .getCoreModsDirectory(), e);
                                        }
                                    }

                                    Utils.copyFile(tempFileDecomp, installer.getCoreModsDirectory());
                                } else {
                                    Utils.copyFile(tempFileDecomp, installer.getModsDirectory());
                                }
                            } else {
                                if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                                    if (!Files.exists(installer.getCoreModsDirectory())) {
                                        try {
                                            Files.createDirectory(installer.getCoreModsDirectory());
                                        } catch (IOException e) {
                                            App.settings.logStackTrace("Error creating folder " + installer
                                                    .getCoreModsDirectory(), e);
                                        }
                                    }

                                    Utils.copyDirectory(tempFileDecomp, installer.getCoreModsDirectory());
                                } else {
                                    Utils.copyDirectory(tempFileDecomp, installer.getModsDirectory());
                                }
                            }
                            break;
                        case jar:
                            if (Files.isRegularFile(tempFileDecomp)) {
                                Utils.copyFile(tempFileDecomp, installer.getJarModsDirectory());
                                installer.addToJarOrder(decompFile);
                            } else {
                                Path newFile = installer.getJarModsDirectory().resolve(getSafeName() + ".zip");
                                Utils.zip(tempFileDecomp, newFile);
                                installer.addToJarOrder(getSafeName() + ".zip");
                            }
                            break;
                        case mods:
                            if (Files.isRegularFile(tempFileDecomp)) {
                                Utils.copyFile(tempFileDecomp, installer.getModsDirectory());
                            } else {
                                Utils.copyDirectory(tempFileDecomp, installer.getModsDirectory());
                            }
                            break;
                        case root:
                            if (Files.isRegularFile(tempFileDecomp)) {
                                Utils.copyFile(tempFileDecomp, installer.getRootDirectory());
                            } else {
                                Utils.copyDirectory(tempFileDecomp, installer.getRootDirectory());
                            }
                            break;
                        default:
                            LogManager.error("No known way to decomp mod " + this.name + " with type " + this
                                    .decompType);
                            break;
                    }
                } else {
                    LogManager.error("Couldn't find decomp file " + this.decompFile + " for mod " + this.name);
                }
                Utils.delete(tempDirDecomp);
                break;
            default:
                LogManager.error("No known way to install mod " + this.name + " with type " + thisType);
                break;
        }
    }

    public Path getInstalledFile(InstanceInstaller installer) {
        ModType thisType;
        String file;
        Path base = null;

        if (installer.isServer()) {
            file = getServerFile();
            thisType = this.serverType;
        } else {
            file = getFile();
            thisType = this.type;
        }

        switch (thisType) {
            case jar:
            case forge:
                if (installer.isServer() && thisType == ModType.forge) {
                    base = installer.getRootDirectory();
                    break;
                }
                base = installer.getJarModsDirectory();
                break;
            case mcpc:
                if (installer.isServer()) {
                    base = installer.getRootDirectory();
                    break;
                }
                break;
            case texturepack:
                base = installer.getTexturePacksDirectory();
                break;
            case resourcepack:
                base = installer.getResourcePacksDirectory();
                break;
            case mods:
                base = installer.getModsDirectory();
                break;
            case ic2lib:
                base = installer.getIC2LibDirectory();
                break;
            case denlib:
                base = installer.getDenLibDirectory();
                break;
            case plugins:
                base = installer.getPluginsDirectory();
                break;
            case coremods:
                if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                    base = installer.getCoreModsDirectory();
                } else {
                    base = installer.getModsDirectory();
                }
                break;
            case shaderpack:
                base = installer.getShaderPacksDirectory();
                break;
            default:
                LogManager.error("No known way to find installed mod " + this.name + " with type " + thisType);
                break;
        }

        if (base == null) {
            return null;
        }

        return base.resolve(file);
    }
}