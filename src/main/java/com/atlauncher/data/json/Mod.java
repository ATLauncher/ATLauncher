/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Language;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

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
    private boolean force;
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
        return (this.website != null && this.website.substring(0, 4).equalsIgnoreCase("http"));
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

    public boolean shouldForce() {
        return this.force;
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
        return (dir, name) -> name.matches(file);
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
        File fileLocation = new File(App.settings.getDownloadsDir(), getFile());

        if (fileLocation.exists()) {
            if (this.shouldForce()) {
                Utils.delete(fileLocation); // File exists but is corrupt, delete it
            } else if (this.download != DownloadType.direct) {
                if (hasMD5()) {
                    if (Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.md5))) {
                        return; // File already exists and matches hash, don't download it
                    } else {
                        Utils.delete(fileLocation); // File exists but is corrupt, delete it
                    }
                } else {
                    if (fileLocation.length() != 0) {
                        return; // No MD5, but file is there, can only assume it's fine
                    }
                }
            }
        }
        switch (this.download) {
        case browser:
            File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(), getFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }
            if (fileCheck != null && fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                        : App.settings.getDownloadsDir()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File(
                            (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App.settings.getDownloadsDir()),
                            files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                    : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                    : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                    }
                }
            }
            while (!fileLocation.exists()) {
                int retValue = 1;
                do {
                    if (retValue == 1) {
                        OS.openWebBrowser(this.getUrl());
                    }

                    retValue = DialogManager.optionDialog()
                            .setTitle(Language.INSTANCE.localize("common.downloading") + " "
                                    + (serverFile == null ? (isFilePattern() ? getName() : getFile())
                                            : (isFilePattern() ? getName() : getServerFile())))
                            .setContent(HTMLUtils.centerParagraph(Language.INSTANCE.localizeWithReplace(
                                    "instance" + "" + ".browseropened",
                                    (serverFile == null ? (isFilePattern() ? getName() : getFile())
                                            : (isFilePattern() ? getName() : getServerFile())))
                                    + "<br/><br/>" + Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>"
                                    + (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir().getAbsolutePath()
                                            : (isFilePattern() ? App.settings.getDownloadsDir().getAbsolutePath()
                                                    : App.settings.getDownloadsDir().getAbsolutePath() + " or<br/>"
                                                            + App.settings.getUsersDownloadsDir()))))
                            .addOption(Language.INSTANCE.localize("common.openfolder"), true)
                            .addOption(Language.INSTANCE.localize("instance.ivedownloaded")).setType(DialogManager.INFO)
                            .show();

                    if (retValue == DialogManager.CLOSED_OPTION) {
                        installer.cancel(true);
                        return;
                    } else if (retValue == 0) {
                        OS.openFileExplorer(FileSystem.DOWNLOADS);
                    }
                } while (retValue != 1);

                if (isFilePattern()) {
                    String[] files = (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                            : App.settings.getDownloadsDir()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                : App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                        }
                    }
                } else {
                    if (!fileLocation.exists()) {
                        // Check users downloads folder to see if it's there
                        if (downloadsFolderFile.exists()) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
                        }
                        // Check to see if a browser has added a .zip to the end of the file
                        File zipAddedFile = new File(App.settings.getDownloadsDir(), getFile() + ".zip");
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(App.settings.getUsersDownloadsDir(), getFile() + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }
                }
            }
            break;
        case direct:
            Downloadable download1 = new Downloadable(this.getUrl(), fileLocation, this.md5, installer, false);
            download1.checkForNewness();
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
        if (hasMD5()) {
            if (Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.md5))) {
                return; // MD5 hash matches
            } else {
                if (attempt < 5) {
                    Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                    downloadClient(installer, ++attempt); // download again
                } else {
                    LogManager.error("Cannot download " + fileLocation.getAbsolutePath() + ". Aborting install!");
                    installer.cancel(true);
                }
            }
        } else {
            return; // No MD5, but file is there, can only assume it's fine
        }
    }

    public void downloadServer(InstanceInstaller installer, int attempt) {
        File fileLocation = new File(App.settings.getDownloadsDir(), getServerFile());
        if (fileLocation.exists()) {
            if (this.shouldForce()) {
                Utils.delete(fileLocation); // File exists but is corrupt, delete it
            } else if (this.download != DownloadType.direct) {
                if (this.hasServerMD5()) {
                    if (Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.serverMD5))) {
                        return; // File already exists and matches hash, don't download it
                    } else {
                        Utils.delete(fileLocation); // File exists but is corrupt, delete it
                    }
                } else {
                    return; // No MD5, but file is there, can only assume it's fine
                }
            }
        }
        if (this.serverDownload == DownloadType.browser) {
            File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(), getServerFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }

            if (fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                        : App.settings.getDownloadsDir()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File(
                            (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App.settings.getDownloadsDir()),
                            files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                    : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                    : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                    }
                }
            }

            while (!fileLocation.exists()) {
                OS.openWebBrowser(this.serverUrl);

                int ret = DialogManager.optionDialog()
                        .setTitle(Language.INSTANCE.localize("common.downloading") + " "
                                + (serverFile == null ? getFile() : getServerFile()))
                        .setContent(HTMLUtils
                                .centerParagraph(Language.INSTANCE.localizeWithReplace("instance.browseropened",
                                        (serverFile == null ? getFile() : getServerFile())) + "<br/><br/>"
                                        + Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>"
                                        + (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir().getAbsolutePath()
                                                : App.settings.getDownloadsDir().getAbsolutePath() + " or<br/>"
                                                        + App.settings.getUsersDownloadsDir())))
                        .setType(DialogManager.INFO)
                        .addOption(Language.INSTANCE.localize("instance.ivedownloaded"), true).show();

                if (ret == DialogManager.CLOSED_OPTION) {
                    installer.cancel(true);
                    return;
                }

                if (isFilePattern()) {
                    String[] files = (OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                            : App.settings.getDownloadsDir()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                : App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                        }
                    }
                } else {
                    if (!fileLocation.exists()) {
                        // Check users downloads folder to see if it's there
                        if (downloadsFolderFile.exists()) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
                        }
                        // Check to see if a browser has added a .zip to the end of the file
                        File zipAddedFile = new File(App.settings.getDownloadsDir(), getServerFile() + ".zip");
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(App.settings.getUsersDownloadsDir(), getServerFile() + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }
                }
            }
        } else if (this.serverDownload == DownloadType.direct) {
            Downloadable download = new Downloadable(this.serverUrl, fileLocation, this.serverMD5, installer, false);
            download.checkForNewness();
            if (download.needToDownload()) {
                download.download(false);
            }
        } else if (this.serverDownload == DownloadType.server) {
            Downloadable download = new Downloadable(this.serverUrl, fileLocation, this.serverMD5, installer, true);
            if (download.needToDownload()) {
                download.download(false);
            }
        }
        if (hasServerMD5()) {
            if (Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.serverMD5))) {
                return; // MD5 hash matches
            } else {
                if (attempt < 5) {
                    Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                    downloadServer(installer, ++attempt); // download again
                } else {
                    LogManager.error("Cannot download " + fileLocation.getAbsolutePath() + ". Aborting install!");
                    installer.cancel(true);
                }
            }
        } else {
            return; // No MD5, but file is there, can only assume it's fine
        }
    }

    public void install(InstanceInstaller installer) {
        File fileLocation;
        ModType thisType;
        if (installer.isServer() && this.serverUrl != null) {
            fileLocation = new File(App.settings.getDownloadsDir(), getServerFile());
            thisType = this.serverType;
        } else {
            fileLocation = new File(App.settings.getDownloadsDir(), getFile());
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
            if (!installer.getTexturePacksDirectory().exists()) {
                installer.getTexturePacksDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getTexturePacksDirectory());
            break;
        case resourcepack:
            if (!installer.getResourcePacksDirectory().exists()) {
                installer.getResourcePacksDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getResourcePacksDirectory());
            break;
        case texturepackextract:
            if (!installer.getTexturePacksDirectory().exists()) {
                installer.getTexturePacksDirectory().mkdir();
            }
            Utils.unzip(fileLocation, installer.getTempTexturePackDirectory());
            installer.setTexturePackExtracted();
            break;
        case resourcepackextract:
            if (!installer.getResourcePacksDirectory().exists()) {
                installer.getResourcePacksDirectory().mkdir();
            }
            Utils.unzip(fileLocation, installer.getTempResourcePackDirectory());
            installer.setResourcePackExtracted();
            break;
        case millenaire:
            File tempDirMillenaire = new File(App.settings.getTempDir(), getSafeName());
            Utils.unzip(fileLocation, tempDirMillenaire);
            for (String folder : tempDirMillenaire.list()) {
                File thisFolder = new File(tempDirMillenaire, folder);
                for (String dir : thisFolder.list((dir, name) -> {
                    File thisFile = new File(dir, name);
                    return thisFile.isDirectory();
                })) {
                    Utils.copyDirectory(new File(thisFolder, dir), installer.getModsDirectory());
                }
            }
            Utils.delete(tempDirMillenaire);
            break;
        case mods:
            Utils.copyFile(fileLocation, installer.getModsDirectory());
            break;
        case ic2lib:
            if (!installer.getIC2LibDirectory().exists()) {
                installer.getIC2LibDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getIC2LibDirectory());
            break;
        case flan:
            if (!installer.getFlanDirectory().exists()) {
                installer.getFlanDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getFlanDirectory());
            break;
        case denlib:
            if (!installer.getDenLibDirectory().exists()) {
                installer.getDenLibDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getDenLibDirectory());
            break;
        case depandency:
        case dependency:
            if (!installer.getDependencyDirectory().exists()) {
                installer.getDependencyDirectory().mkdirs();
            }
            Utils.copyFile(fileLocation, installer.getDependencyDirectory());
            break;
        case plugins:
            if (!installer.getPluginsDirectory().exists()) {
                installer.getPluginsDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getPluginsDirectory());
            break;
        case coremods:
            if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                if (!installer.getCoreModsDirectory().exists()) {
                    installer.getCoreModsDirectory().mkdir();
                }
                Utils.copyFile(fileLocation, installer.getCoreModsDirectory());
            } else {
                Utils.copyFile(fileLocation, installer.getModsDirectory());
            }
            break;
        case shaderpack:
            if (!installer.getShaderPacksDirectory().exists()) {
                installer.getShaderPacksDirectory().mkdir();
            }
            Utils.copyFile(fileLocation, installer.getShaderPacksDirectory());
            break;
        case extract:
            File tempDirExtract = new File(App.settings.getTempDir(), getSafeName());
            Utils.unzip(fileLocation, tempDirExtract);
            File folder = new File(new File(App.settings.getTempDir(), getSafeName()), this.extractFolder);
            switch (extractTo) {
            case coremods:
                if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                    if (!installer.getCoreModsDirectory().exists()) {
                        installer.getCoreModsDirectory().mkdir();
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
            File tempDirDecomp = new File(App.settings.getTempDir(), getSafeName());
            Utils.unzip(fileLocation, tempDirDecomp);
            File tempFileDecomp = new File(tempDirDecomp, decompFile);
            if (tempFileDecomp.exists()) {
                switch (decompType) {
                case coremods:
                    if (tempFileDecomp.isFile()) {
                        if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                            if (!installer.getCoreModsDirectory().exists()) {
                                installer.getCoreModsDirectory().mkdir();
                            }
                            Utils.copyFile(tempFileDecomp, installer.getCoreModsDirectory());
                        } else {
                            Utils.copyFile(tempFileDecomp, installer.getModsDirectory());
                        }
                    } else {
                        if (installer.getVersion().getMinecraftVersion().usesCoreMods()) {
                            if (!installer.getCoreModsDirectory().exists()) {
                                installer.getCoreModsDirectory().mkdir();
                            }
                            Utils.copyDirectory(tempFileDecomp, installer.getCoreModsDirectory());
                        } else {
                            Utils.copyDirectory(tempFileDecomp, installer.getModsDirectory());
                        }
                    }
                    break;
                case jar:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.getJarModsDirectory());
                        installer.addToJarOrder(decompFile);
                    } else {
                        File newFile = new File(installer.getJarModsDirectory(), getSafeName() + ".zip");
                        Utils.zip(tempFileDecomp, newFile);
                        installer.addToJarOrder(getSafeName() + ".zip");
                    }
                    break;
                case mods:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.getModsDirectory());
                    } else {
                        Utils.copyDirectory(tempFileDecomp, installer.getModsDirectory());
                    }
                    break;
                case root:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.getRootDirectory());
                    } else {
                        Utils.copyDirectory(tempFileDecomp, installer.getRootDirectory());
                    }
                    break;
                default:
                    LogManager.error("No known way to decomp mod " + this.name + " with type " + this.decompType);
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

    public File getInstalledFile(InstanceInstaller installer) {
        ModType thisType;
        String file;
        File base = null;
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
        return new File(base, file);
    }
}
