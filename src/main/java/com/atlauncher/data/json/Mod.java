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
package com.atlauncher.data.json;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.annotations.SerializedName;

import org.mini2Dx.gettext.GetText;

@Json
public class Mod {
    public String name;
    public String version;
    public String url;
    public String file;
    public String path;
    public String md5;
    public int filesize;
    public Long fingerprint = null;
    public DownloadType download;
    public String website;
    public String donation;
    public List<String> authors;
    public String sha1;
    public String colour;
    public String warning;
    public boolean force;
    public Color compiledColour;
    public ModType type;
    public ExtractToType extractTo;
    public String extractFolder;
    public String decompFile;
    public DecompType decompType;
    public boolean filePattern = false;
    public String filePreference;
    public String fileCheck;
    public boolean client = true;
    public boolean server = true;
    public boolean serverSeparate = false;
    public String serverUrl;
    public String serverFile;
    public ModType serverType;
    public DownloadType serverDownload;
    public String serverMD5;
    public Boolean serverOptional;
    public boolean optional = false;
    public boolean selected = false;
    public boolean recommended = true;
    public boolean hidden = false;
    public boolean library = false;
    public String group;
    public String linked;
    public List<String> depends;
    public String filePrefix;
    public String description;
    public CurseForgeProject curseForgeProject;
    public CurseForgeFile curseForgeFile;

    @SerializedName(value = "curseforge_project_id", alternate = { "curse_id" })
    public Integer curseForgeProjectId;

    @SerializedName(value = "curseforge_file_id", alternate = { "curse_file_id" })
    public Integer curseForgeFileId;

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

    public String getDownloadUrl() {
        if (this.download == DownloadType.server) {
            return String.format("%s/%s", Constants.DOWNLOAD_SERVER, this.getUrl());
        }

        return this.getUrl();
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
        return (this.website != null && this.website.length() >= 4
                && this.website.substring(0, 4).equalsIgnoreCase("http"));
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
            sb.append(author).append(", ");
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

    public Integer getCurseForgeProjectId() {
        return this.curseForgeProjectId;
    }

    public Integer getCurseForgeFileId() {
        return this.curseForgeFileId;
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
        if (installer.isServer && this.serverUrl != null) {
            downloadServer(installer, attempt);
        } else {
            downloadClient(installer, attempt);
        }
    }

    public void downloadClient(InstanceInstaller installer, int attempt) {
        File fileLocation = FileSystem.DOWNLOADS.resolve(getFile()).toFile();

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
            File downloadsFolderFile = new File(FileSystem.USER_DOWNLOADS.toFile(), getFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }
            if (fileCheck != null && fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                        : FileSystem.DOWNLOADS.toFile()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File(
                            (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() : FileSystem.DOWNLOADS.toFile()),
                            files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                    : FileSystem.DOWNLOADS.toFile()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                    : FileSystem.DOWNLOADS.toFile()), files[i]);
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
                            .setTitle(GetText.tr("Downloading") + " "
                                    + (serverFile == null ? (isFilePattern() ? getName() : getFile())
                                            : (isFilePattern() ? getName() : getServerFile())))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "Browser opened to download file {0}",
                                    (serverFile == null ? (isFilePattern() ? getName() : getFile())
                                            : (isFilePattern() ? getName() : getServerFile())))
                                    + "<br/><br/>" + GetText.tr("Please save this file to the following location")
                                    + "<br/><br/>"
                                    + (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile().getAbsolutePath()
                                            : (isFilePattern() ? FileSystem.DOWNLOADS.toAbsolutePath().toString()
                                                    : FileSystem.DOWNLOADS.toAbsolutePath().toString() + " or<br/>"
                                                            + FileSystem.USER_DOWNLOADS.toFile())))
                                    .build())
                            .addOption(GetText.tr("Open Folder"), true)
                            .addOption(GetText.tr("I've Downloaded This File")).setType(DialogManager.INFO).show();

                    if (retValue == DialogManager.CLOSED_OPTION) {
                        installer.cancel(true);
                        return;
                    } else if (retValue == 0) {
                        OS.openFileExplorer(FileSystem.DOWNLOADS);
                    }
                } while (retValue != 1);

                if (isFilePattern()) {
                    String[] files = (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                            : FileSystem.DOWNLOADS.toFile()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                : FileSystem.DOWNLOADS.toFile()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                        : FileSystem.DOWNLOADS.toFile()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                        : FileSystem.DOWNLOADS.toFile()), files[i]);
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
                        File zipAddedFile = FileSystem.DOWNLOADS.resolve(getFile() + ".zip").toFile();
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(FileSystem.USER_DOWNLOADS.toFile(), getFile() + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }
                }
            }
            break;
        case direct:
        case server:
            break;
        }

        if (!hasMD5()) {
            return;
        }

        if (!Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.md5))) {
            if (attempt < 5) {
                Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                downloadClient(installer, ++attempt); // download again
            } else {
                LogManager.error("Cannot download " + fileLocation.getAbsolutePath() + ". Aborting install!");
                installer.cancel(true);
            }
        }
    }

    public void downloadServer(InstanceInstaller installer, int attempt) {
        File fileLocation = FileSystem.DOWNLOADS.resolve(getServerFile()).toFile();
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
            File downloadsFolderFile = new File(FileSystem.USER_DOWNLOADS.toFile(), getServerFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }

            if (fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                        : FileSystem.DOWNLOADS.toFile()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File(
                            (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile() : FileSystem.DOWNLOADS.toFile()),
                            files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                    : FileSystem.DOWNLOADS.toFile()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                    : FileSystem.DOWNLOADS.toFile()), files[i]);
                            break;
                        }
                    }
                }
            }

            while (!fileLocation.exists()) {
                OS.openWebBrowser(this.serverUrl);

                int ret = DialogManager.optionDialog()
                        .setTitle(GetText.tr("Downloading") + " " + (serverFile == null ? getFile() : getServerFile()))
                        .setContent(new HTMLBuilder().center()
                                .text(GetText.tr("Browser opened to download file {0}",
                                        (serverFile == null ? getFile() : getServerFile())) + "<br/><br/>"
                                        + GetText.tr("Please save this file to the following location") + "<br/><br/>"
                                        + (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile().getAbsolutePath()
                                                : FileSystem.DOWNLOADS.toAbsolutePath().toString() + " or<br/>"
                                                        + FileSystem.USER_DOWNLOADS.toFile()))
                                .build())
                        .setType(DialogManager.INFO).addOption(GetText.tr("Open Folder"), true)
                        .addOption(GetText.tr("I've Downloaded This File")).show();

                if (ret == DialogManager.CLOSED_OPTION) {
                    installer.cancel(true);
                    return;
                }

                if (isFilePattern()) {
                    String[] files = (OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                            : FileSystem.DOWNLOADS.toFile()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                : FileSystem.DOWNLOADS.toFile()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                        : FileSystem.DOWNLOADS.toFile()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((OS.isUsingMacApp() ? FileSystem.USER_DOWNLOADS.toFile()
                                        : FileSystem.DOWNLOADS.toFile()), files[i]);
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
                        File zipAddedFile = FileSystem.DOWNLOADS.resolve(getServerFile() + ".zip").toFile();
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(FileSystem.USER_DOWNLOADS.toFile(), getServerFile() + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }
                }
            }
        }

        if (!hasServerMD5()) {
            return;
        }

        if (!Hashing.md5(fileLocation.toPath()).equals(Hashing.HashCode.fromString(this.serverMD5))) {
            if (attempt < 5) {
                Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                downloadServer(installer, ++attempt); // download again
            } else {
                LogManager.error("Cannot download " + fileLocation.getAbsolutePath() + ". Aborting install!");
                installer.cancel(true);
            }
        }
    }

    public void install(InstanceInstaller installer) {
        File fileLocation;
        ModType thisType;
        if (installer.isServer && this.serverUrl != null) {
            fileLocation = FileSystem.DOWNLOADS.resolve(getServerFile()).toFile();
            thisType = this.serverType;
        } else {
            fileLocation = FileSystem.DOWNLOADS.resolve(getFile()).toFile();
            thisType = this.type;
        }
        switch (thisType) {
        case jar:
        case forge:
            if (installer.isServer && thisType == ModType.forge) {
                Utils.copyFile(fileLocation, installer.root.toFile());
                break;
            } else if (installer.isServer && thisType == ModType.jar) {
                Utils.unzip(fileLocation, installer.temp.resolve("jar").toFile());
                break;
            }
            Utils.copyFile(fileLocation, installer.root.resolve("jarmods").toFile());
            break;
        case mcpc:
            if (installer.isServer) {
                Utils.copyFile(fileLocation, installer.root.toFile());
                break;
            }
            break;
        case texturepack:
            if (!installer.root.resolve("texturepacks").toFile().exists()) {
                installer.root.resolve("texturepacks").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("texturepacks").toFile());
            break;
        case resourcepack:
            if (!installer.root.resolve("resourcepacks").toFile().exists()) {
                installer.root.resolve("resourcepacks").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("resourcepacks").toFile());
            break;
        case texturepackextract:
            if (!installer.root.resolve("texturepacks").toFile().exists()) {
                installer.root.resolve("texturepacks").toFile().mkdir();
            }
            Utils.unzip(fileLocation, installer.root.resolve("texturepacks/extracted").toFile());
            break;
        case resourcepackextract:
            if (!installer.root.resolve("resourcepacks").toFile().exists()) {
                installer.root.resolve("resourcepacks").toFile().mkdir();
            }
            Utils.unzip(fileLocation, installer.root.resolve("resourcepacks/extracted").toFile());
            break;
        case millenaire:
            File tempDirMillenaire = FileSystem.TEMP.resolve(getSafeName()).toFile();
            Utils.unzip(fileLocation, tempDirMillenaire);
            for (String folder : tempDirMillenaire.list()) {
                File thisFolder = new File(tempDirMillenaire, folder);
                for (String dir : thisFolder.list((dir, name) -> {
                    File thisFile = new File(dir, name);
                    return thisFile.isDirectory();
                })) {
                    Utils.copyDirectory(new File(thisFolder, dir), installer.root.resolve("mods").toFile());
                }
            }
            Utils.delete(tempDirMillenaire);
            break;
        case mods:
            if (path != null) {
                if (!installer.root.resolve(path).toFile().exists()) {
                    installer.root.resolve(path).toFile().mkdirs();
                }

                Utils.copyFile(fileLocation, installer.root.resolve(path).toFile());
            } else {
                Utils.copyFile(fileLocation, installer.root.resolve("mods").toFile());
            }
            break;
        case ic2lib:
            if (!installer.root.resolve("mods/ic2").toFile().exists()) {
                installer.root.resolve("mods/ic2").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("mods/ic2").toFile());
            break;
        case flan:
            if (!installer.root.resolve("Flan").toFile().exists()) {
                installer.root.resolve("Flan").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("Flan").toFile());
            break;
        case denlib:
            if (!installer.root.resolve("mods/denlib").toFile().exists()) {
                installer.root.resolve("mods/denlib").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("mods/denlib").toFile());
            break;
        case depandency:
        case dependency:
            if (!installer.root.resolve("mods/" + installer.minecraftVersion.id).toFile().exists()) {
                installer.root.resolve("mods/" + installer.minecraftVersion.id).toFile().mkdirs();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("mods/" + installer.minecraftVersion.id).toFile());
            break;
        case plugins:
            if (!installer.root.resolve("plugins").toFile().exists()) {
                installer.root.resolve("plugins").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("plugins").toFile());
            break;
        case coremods:
            if (!installer.root.resolve("coremods").toFile().exists()) {
                installer.root.resolve("coremods").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("coremods").toFile());
            break;
        case shaderpack:
            if (!installer.root.resolve("shaderpacks").toFile().exists()) {
                installer.root.resolve("shaderpacks").toFile().mkdir();
            }
            Utils.copyFile(fileLocation, installer.root.resolve("shaderpacks").toFile());
            break;
        case extract:
            File tempDirExtract = FileSystem.TEMP.resolve(getSafeName()).toFile();
            Utils.unzip(fileLocation, tempDirExtract);
            File folder = FileSystem.TEMP.resolve(getSafeName() + "/" + this.extractFolder).toFile();
            switch (extractTo) {
            case coremods:
                if (!installer.root.resolve("coremods").toFile().exists()) {
                    installer.root.resolve("coremods").toFile().mkdir();
                }
                Utils.copyDirectory(folder, installer.root.resolve("coremods").toFile());
                break;
            case mods:
                Utils.copyDirectory(folder, installer.root.resolve("mods").toFile());
                break;
            case root:
                Utils.copyDirectory(folder, installer.root.toFile());
                break;
            default:
                LogManager.error("No known way to extract mod " + this.name + " with type " + this.extractTo);
                break;
            }
            Utils.delete(tempDirExtract);
            break;
        case decomp:
            File tempDirDecomp = FileSystem.TEMP.resolve(getSafeName()).toFile();
            Utils.unzip(fileLocation, tempDirDecomp);
            File tempFileDecomp = new File(tempDirDecomp, decompFile);
            if (tempFileDecomp.exists()) {
                switch (decompType) {
                case coremods:
                    if (tempFileDecomp.isFile()) {
                        if (!installer.root.resolve("coremods").toFile().exists()) {
                            installer.root.resolve("coremods").toFile().mkdir();
                        }
                        Utils.copyFile(tempFileDecomp, installer.root.resolve("coremods").toFile());
                    } else {
                        if (!installer.root.resolve("coremods").toFile().exists()) {
                            installer.root.resolve("coremods").toFile().mkdir();
                        }
                        Utils.copyDirectory(tempFileDecomp, installer.root.resolve("coremods").toFile());
                    }
                    break;
                case jar:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.root.resolve("jarmods").toFile());
                    } else {
                        File newFile = new File(installer.root.resolve("jarmods").toFile(), getSafeName() + ".zip");
                        Utils.zip(tempFileDecomp, newFile);
                    }
                    break;
                case mods:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.root.resolve("mods").toFile());
                    } else {
                        Utils.copyDirectory(tempFileDecomp, installer.root.resolve("mods").toFile());
                    }
                    break;
                case root:
                    if (tempFileDecomp.isFile()) {
                        Utils.copyFile(tempFileDecomp, installer.root.toFile());
                    } else {
                        Utils.copyDirectory(tempFileDecomp, installer.root.toFile());
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
        if (installer.isServer) {
            file = getServerFile();
            thisType = this.serverType;
        } else {
            file = getFile();
            thisType = this.type;
        }
        switch (thisType) {
        case jar:
        case forge:
            if (installer.isServer && thisType == ModType.forge) {
                base = installer.root.toFile();
                break;
            }
            base = installer.root.resolve("jarmods").toFile();
            break;
        case mcpc:
            if (installer.isServer) {
                base = installer.root.toFile();
                break;
            }
            break;
        case texturepack:
            base = installer.root.resolve("texturepacks").toFile();
            break;
        case resourcepack:
            base = installer.root.resolve("resourcepacks").toFile();
            break;
        case mods:
            base = installer.root.resolve("mods").toFile();
            break;
        case ic2lib:
            base = installer.root.resolve("mods/ic2").toFile();
            break;
        case denlib:
            base = installer.root.resolve("mods/denlib").toFile();
            break;
        case plugins:
            base = installer.root.resolve("plugins").toFile();
            break;
        case coremods:
            base = installer.root.resolve("coremods").toFile();
            break;
        case shaderpack:
            base = installer.root.resolve("shaderpacks").toFile();
            break;
        default:
            LogManager.error("No known way to find installed mod " + this.name + " with type " + thisType);
            break;
        }
        if (path != null) {
            base = installer.root.resolve(path).toFile();
        }
        if (base == null) {
            return null;
        }
        return new File(base, file);
    }
}
