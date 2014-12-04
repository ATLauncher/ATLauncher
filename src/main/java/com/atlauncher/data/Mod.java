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
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;

public class Mod {
    private String name;
    private String version;
    private String url;
    private String file;
    private String website;
    private String donation;
    private Color colour;
    private String warning;
    private String md5;
    private Type type;
    private ExtractTo extractTo;
    private String extractFolder;
    private String decompFile;
    private DecompType decompType;
    private boolean filePattern;
    private String filePreference;
    private String fileCheck;
    private boolean client;
    private boolean server;
    private String serverURL;
    private String serverFile;
    private Download serverDownload;
    private String serverMD5;
    private Type serverType;
    private boolean optional;
    private boolean serverOptional;
    private boolean selected;
    private Download download;
    private boolean hidden;
    private boolean library;
    private String group;
    private String category;
    private String linked;
    private String[] depends;
    private String filePrefix;
    private boolean recommended;
    private String description;

    public Mod(String name, String version, String url, String file, String website, String donation, Color colour,
               String warning, String md5, Type type, ExtractTo extractTo, String extractFolder, String decompFile,
               DecompType decompType, boolean filePattern, String filePreference, String fileCheck, boolean client,
               boolean server, String serverURL, String serverFile, Download serverDownload, String serverMD5, Type
            serverType, boolean optional, boolean serverOptional, boolean selected, Download download, boolean
            hidden, boolean library, String group, String category, String linked, String[] depends, String
            filePrefix, boolean recommended, String description) {
        this.name = name;
        this.version = version;
        this.url = url.replace("&amp;", "&").replace(" ", "%20");
        this.file = file;
        this.website = website;
        this.donation = donation;
        this.colour = colour;
        this.warning = warning;
        this.md5 = md5;
        this.type = type;
        this.extractTo = extractTo;
        this.extractFolder = (extractFolder == null) ? null : extractFolder.replace("%s%", File.separator);
        this.decompFile = decompFile;
        this.decompType = decompType;
        this.filePattern = filePattern;
        this.filePreference = filePreference;
        this.fileCheck = fileCheck;
        this.client = client;
        this.server = server;
        this.serverURL = (serverURL == null) ? null : serverURL.replace("&amp;", "&").replace(" ", "%20");
        this.serverFile = serverFile;
        this.serverDownload = serverDownload;
        this.serverMD5 = serverMD5;
        this.serverType = serverType;
        this.optional = optional;
        this.serverOptional = serverOptional;
        this.selected = selected;
        this.download = download;
        this.hidden = hidden;
        this.library = library;
        this.group = group;
        this.category = category;
        this.linked = linked;
        this.depends = depends;
        this.filePrefix = filePrefix;
        this.recommended = recommended;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getVersion() {
        return this.version;
    }

    public Type getType() {
        return this.type;
    }

    public boolean isFilePattern() {
        return this.filePattern;
    }

    public DecompType getDecompType() {
        return this.decompType;
    }

    public String getMD5() {
        return this.md5;
    }

    public String getServerMD5() {
        return this.serverMD5;
    }

    public boolean hasFilePrefix() {
        return this.filePrefix != null;
    }

    public boolean compareMD5(String md5) {
        return this.md5.equalsIgnoreCase(md5);
    }

    public boolean compareServerMD5(String md5) {
        return this.serverMD5.equalsIgnoreCase(md5);
    }

    public boolean hasMD5() {
        return !this.md5.isEmpty();
    }

    public boolean hasServerMD5() {
        return this.serverMD5 != null;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public FilenameFilter getFileNameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(file);
            }
        };
    }

    public boolean isRecommeneded() {
        return this.recommended;
    }

    public Color getColour() {
        return this.colour;
    }

    public boolean hasWarning() {
        return this.warning != null;
    }

    public String getWarning() {
        return this.warning;
    }

    public boolean isServerOptional() {
        return this.serverOptional;
    }

    public void setSelected() {
        this.selected = true;
    }

    public void setNotSelected() {
        this.selected = false;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean hasColour() {
        return this.colour != null;
    }

    public String getLinked() {
        return this.linked;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean installOnClient() {
        return this.client;
    }

    public boolean installOnServer() {
        if (this.serverFile == null) {
            return this.server;
        } else {
            return true;
        }
    }

    public boolean isBrowserDownload() {
        return (this.download == Download.browser);
    }

    public boolean isDirectDownload() {
        return (this.download == Download.direct);
    }

    public boolean isServerDownload() {
        return (this.download == Download.server);
    }

    public boolean isBrowserDownloadServer() {
        if (this.serverDownload == null) {
            return this.isBrowserDownload();
        }
        return (this.serverDownload == Download.browser);
    }

    public boolean isDirectDownloadServer() {
        if (this.serverDownload == null) {
            return this.isDirectDownload();
        }
        return (this.serverDownload == Download.direct);
    }

    public boolean isServerDownloadServer() {
        if (this.serverDownload == null) {
            return this.isServerDownload();
        }
        return (this.serverDownload == Download.server);
    }

    public void download(InstanceInstaller installer) {
        download(installer, 1);
    }

    public void download(InstanceInstaller installer, int attempt) {
        if (installer.isServer() && this.serverURL != null) {
            downloadServer(installer, attempt);
        } else {
            downloadClient(installer, attempt);
        }
    }

    public void downloadClient(InstanceInstaller installer, int attempt) {
        File fileLocation = new File(App.settings.getDownloadsDir(), getFile());
        if (fileLocation.exists()) {
            if (hasMD5()) {
                if (compareMD5(Utils.getMD5(fileLocation))) {
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
        switch (download) {
            case browser:
                File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(), getFile());
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                }
                if (fileCheck != null && fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App
                            .settings.getDownloadsDir()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() :
                                App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                        .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                        .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                        }
                    }
                }
                while (!fileLocation.exists()) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            Utils.openBrowser(getURL());
                        }
                        String[] options = new String[]{Language.INSTANCE.localize("common.openfolder"), Language
                                .INSTANCE.localize("instance.ivedownloaded")};
                        retValue = JOptionPane.showOptionDialog(App.settings.getParent(), "<html><p " +
                                "align=\"center\">" + Language.INSTANCE.localizeWithReplace("instance" + "" +
                                        ".browseropened", (serverFile == null ? (isFilePattern() ? getName() :
                                getFile()) : (isFilePattern() ? getName() : getServerFile()))) + "<br/><br/>" +
                                Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>" +
                                        (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                                .getAbsolutePath() : (isFilePattern() ? App.settings.getDownloadsDir
                                                ().getAbsolutePath() : App.settings.getDownloadsDir().getAbsolutePath
                                                () + " or<br/>" + App.settings.getUsersDownloadsDir())) +
                                        "</p></html>", Language.INSTANCE.localize("common.downloading") + " " +
                                        (serverFile == null ? (isFilePattern() ? getName() : getFile()) :
                                                (isFilePattern() ? getName() : getServerFile())), JOptionPane
                                .DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                        if (retValue == JOptionPane.CLOSED_OPTION) {
                            installer.cancel(true);
                            return;
                        } else if (retValue == 0) {
                            Utils.openExplorer(App.settings.getDownloadsDir());
                        }
                    } while (retValue != 1);

                    if (isFilePattern()) {
                        String[] files = (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App
                                .settings.getDownloadsDir()).list(getFileNameFilter());
                        if (files.length == 1) {
                            this.file = files[0];
                            fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir
                                    () : App.settings.getDownloadsDir()), files[0]);
                        } else if (files.length > 1) {
                            for (int i = 0; i < files.length; i++) {
                                if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                    this.file = files[i];
                                    fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                            .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
                                    break;
                                }
                                if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                    this.file = files[i];
                                    fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                            .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
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
                Downloadable download1 = new Downloadable(getURL(), fileLocation, this.md5, installer, false);
                if (download1.needToDownload()) {
                    installer.resetDownloadedBytes(download1.getFilesize());
                    download1.download(true);
                }
                break;
            case server:
                Downloadable download2 = new Downloadable(getURL(), fileLocation, this.md5, installer, true);
                if (download2.needToDownload()) {
                    download2.download(false);
                }
                break;
        }
        if (hasMD5()) {
            if (compareMD5(Utils.getMD5(fileLocation))) {
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
            if (hasServerMD5()) {
                if (compareServerMD5(Utils.getMD5(fileLocation))) {
                    return; // File already exists and matches hash, don't download it
                } else {
                    Utils.delete(fileLocation); // File exists but is corrupt, delete it
                }
            } else {
                return; // No MD5, but file is there, can only assume it's fine
            }
        }
        if (isBrowserDownloadServer()) {
            File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(), getServerFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }

            if (fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App.settings
                        .getDownloadsDir()).list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App
                            .settings.getDownloadsDir()), files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir
                                    () : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir
                                    () : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                    }
                }
            }

            while (!fileLocation.exists()) {
                Utils.openBrowser(getServerURL());
                String[] options = new String[]{Language.INSTANCE.localize("instance.ivedownloaded")};
                int retValue = JOptionPane.showOptionDialog(App.settings.getParent(), "<html><p align=\"center\">" +
                        Language.INSTANCE.localizeWithReplace("instance" + "" +
                                ".browseropened", (serverFile == null ? getFile() : getServerFile())) + "<br/><br/>"
                        + Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>" + (App.settings
                        .isUsingMacApp() ? App.settings.getUsersDownloadsDir().getAbsolutePath() : App.settings
                        .getDownloadsDir().getAbsolutePath() + " or<br/>" + App.settings.getUsersDownloadsDir()) +
                        "</p></html>", Language.INSTANCE.localize("common" + "" +
                                ".downloading") + " " + (serverFile == null ? getFile() : getServerFile()),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (retValue == JOptionPane.CLOSED_OPTION) {
                    installer.cancel(true);
                    return;
                }

                if (isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() : App
                            .settings.getDownloadsDir()).list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File((App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir() :
                                App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                        .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File((App.settings.isUsingMacApp() ? App.settings
                                        .getUsersDownloadsDir() : App.settings.getDownloadsDir()), files[i]);
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
        } else if (isDirectDownloadServer()) {
            Downloadable download = new Downloadable(getServerURL(), fileLocation, this.serverMD5, installer, false);
            if (download.needToDownload()) {
                download.download(false);
            }
        } else if (isServerDownloadServer()) {
            Downloadable download = new Downloadable(getServerURL(), fileLocation, this.serverMD5, installer, true);
            if (download.needToDownload()) {
                download.download(false);
            }
        }
        if (hasServerMD5()) {
            if (compareServerMD5(Utils.getMD5(fileLocation))) {
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
        Type thisType;
        if (installer.isServer() && this.serverURL != null) {
            fileLocation = new File(App.settings.getDownloadsDir(), getServerFile());
            thisType = this.serverType;
        } else {
            fileLocation = new File(App.settings.getDownloadsDir(), getFile());
            thisType = this.type;
        }
        switch (thisType) {
            case jar:
            case forge:
                if (installer.isServer() && thisType == Type.forge) {
                    Utils.copyFile(fileLocation, installer.getRootDirectory());
                    break;
                } else if (installer.isServer() && thisType == Type.jar) {
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
                    for (String dir : thisFolder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            File thisFile = new File(dir, name);
                            return thisFile.isDirectory();
                        }
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

    public String getURL() {
        return this.url;
    }

    public String getFile() {
        if (hasFilePrefix()) {
            return this.filePrefix + this.file;
        }
        return this.file;
    }

    public File getInstalledFile(InstanceInstaller installer) {
        Type thisType;
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
                if (installer.isServer() && thisType == Type.forge) {
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

    public boolean hasGroup() {
        return !(this.group.isEmpty());
    }

    public boolean hasDepends() {
        return (this.depends != null);
    }

    public String getGroup() {
        return this.group;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean hasCategory() {
        return !this.category.isEmpty();
    }

    public String getServerURL() {
        return this.serverURL;
    }

    public String getServerFile() {
        if (hasFilePrefix()) {
            return this.filePrefix + this.serverFile;
        }
        return this.serverFile;
    }

    public boolean hasWebsite() {
        return (this.website != null && this.website.substring(0, 4).equalsIgnoreCase("http"));
    }

    public String getWebsite() {
        return this.website;
    }

    public boolean hasDonation() {
        return (this.donation != null && this.donation.substring(0, 4).equalsIgnoreCase("http"));
    }

    public String getDonation() {
        return this.donation;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String[] getDependancies() {
        return this.depends;
    }

    public boolean isADependancy(Mod mod) {
        for (String name : depends) {
            if (name.equalsIgnoreCase(mod.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isLibrary() {
        return this.library;
    }

}
