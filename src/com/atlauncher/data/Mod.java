/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JOptionPane;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class Mod {

    private String name;
    private String version;
    private String url;
    private String file;
    private String website;
    private String donation;
    private Color colour;
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
    private String linked;
    private String[] depends;
    private boolean recommended;
    private String description;

    public Mod(String name, String version, String url, String file, String website,
            String donation, Color colour, String md5, Type type, ExtractTo extractTo,
            String extractFolder, String decompFile, DecompType decompType, boolean filePattern,
            String filePreference, String fileCheck, boolean client, boolean server,
            String serverURL, String serverFile, Download serverDownload, String serverMD5,
            Type serverType, boolean optional, boolean serverOptional, boolean selected,
            Download download, boolean hidden, boolean library, String group, String linked,
            String[] depends, boolean recommended, String description) {
        this.name = name;
        this.version = version;
        this.url = url.replace("&amp;", "&").replace(" ", "%20");
        this.file = file;
        this.website = website;
        this.donation = donation;
        this.colour = colour;
        this.md5 = md5;
        this.type = type;
        this.extractTo = extractTo;
        this.extractFolder = (extractFolder == null) ? null : extractFolder.replace("%s%",
                File.separator);
        this.decompFile = decompFile;
        this.decompType = decompType;
        this.filePattern = filePattern;
        this.filePreference = filePreference;
        this.fileCheck = fileCheck;
        this.client = client;
        this.server = server;
        this.serverURL = (serverURL == null) ? null : serverURL.replace("&amp;", "&").replace(" ",
                "%20");
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
        this.linked = linked;
        this.depends = depends;
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
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.matches(file)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return filter;
    }

    public boolean isRecommeneded() {
        return this.recommended;
    }

    public Color getColour() {
        return this.colour;
    }

    public boolean isServerOptional() {
        return this.serverOptional;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean hasColour() {
        if (this.colour == null) {
            return false;
        } else {
            return true;
        }
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
                System.out.println("Downloading " + this.name);
                File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(), getFile());
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                }
                if (fileCheck != null && fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? App.settings
                            .getUsersDownloadsDir() : App.settings.getDownloadsDir())
                            .list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File(
                                (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File(
                                        (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                                : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last")
                                    && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File(
                                        (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                                : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                        }
                    }
                }
                while (!fileLocation.exists()) {
                    Utils.openBrowser(getURL());
                    String[] options = new String[] { App.settings
                            .getLocalizedString("instance.ivedownloaded") };
                    int retValue = JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("instance.browseropened",
                                            (serverFile == null ? (isFilePattern() ? getName()
                                                    : getFile()) : (isFilePattern() ? getName()
                                                    : getServerFile())))
                                    + "<br/><br/>"
                                    + App.settings.getLocalizedString("instance.pleasesave")
                                    + "<br/><br/>"
                                    + (App.settings.isUsingMacApp() ? App.settings
                                            .getUsersDownloadsDir().getAbsolutePath()
                                            : (isFilePattern() ? App.settings.getDownloadsDir()
                                                    .getAbsolutePath() : App.settings
                                                    .getDownloadsDir().getAbsolutePath()
                                                    + " or<br/>"
                                                    + App.settings.getUsersDownloadsDir()))
                                    + "</center></html>",
                            App.settings.getLocalizedString("common.downloading")
                                    + " "
                                    + (serverFile == null ? (isFilePattern() ? getName()
                                            : getFile()) : (isFilePattern() ? getName()
                                            : getServerFile())), JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (retValue == JOptionPane.CLOSED_OPTION) {
                        installer.cancel(true);
                        return;
                    }

                    if (isFilePattern()) {
                        String[] files = (App.settings.isUsingMacApp() ? App.settings
                                .getUsersDownloadsDir() : App.settings.getDownloadsDir())
                                .list(getFileNameFilter());
                        if (files.length == 1) {
                            this.file = files[0];
                            fileLocation = new File(
                                    (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                            : App.settings.getDownloadsDir()), files[0]);
                        } else if (files.length > 1) {
                            for (int i = 0; i < files.length; i++) {
                                if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                    this.file = files[i];
                                    fileLocation = new File(
                                            (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                                    : App.settings.getDownloadsDir()), files[i]);
                                    break;
                                }
                                if (this.filePreference.equalsIgnoreCase("last")
                                        && (i + 1) == files.length) {
                                    this.file = files[i];
                                    fileLocation = new File(
                                            (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
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
                        }
                    }
                }
                break;
            case direct:
                Downloadable download1 = new Downloadable(getURL(), fileLocation, this.md5,
                        installer, false);
                if (download1.needToDownload()) {
                    installer.resetDownloadedBytes(download1.getFilesize());
                    download1.download(true);
                }
                break;
            case server:
                Downloadable download2 = new Downloadable(getURL(), fileLocation, this.md5,
                        installer, true);
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
                    App.settings.log("Cannot download " + fileLocation.getAbsolutePath()
                            + ". Aborting install!", LogMessageType.error, false);
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
            File downloadsFolderFile = new File(App.settings.getUsersDownloadsDir(),
                    getServerFile());
            if (downloadsFolderFile.exists()) {
                Utils.moveFile(downloadsFolderFile, fileLocation, true);
            }

            if (fileCheck.equalsIgnoreCase("before") && isFilePattern()) {
                String[] files = (App.settings.isUsingMacApp() ? App.settings
                        .getUsersDownloadsDir() : App.settings.getDownloadsDir())
                        .list(getFileNameFilter());
                if (files.length == 1) {
                    this.file = files[0];
                    fileLocation = new File(
                            (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                    : App.settings.getDownloadsDir()), files[0]);
                } else if (files.length > 1) {
                    for (int i = 0; i < files.length; i++) {
                        if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                            this.file = files[i];
                            fileLocation = new File(
                                    (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                            : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                        if (this.filePreference.equalsIgnoreCase("last") && (i + 1) == files.length) {
                            this.file = files[i];
                            fileLocation = new File(
                                    (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                            : App.settings.getDownloadsDir()), files[i]);
                            break;
                        }
                    }
                }
            }

            while (!fileLocation.exists()) {
                Utils.openBrowser(getServerURL());
                String[] options = new String[] { App.settings
                        .getLocalizedString("instance.ivedownloaded") };
                int retValue = JOptionPane
                        .showOptionDialog(
                                App.settings.getParent(),
                                "<html><center>"
                                        + App.settings.getLocalizedString("instance.browseropened",
                                                (serverFile == null ? getFile() : getServerFile()))
                                        + "<br/><br/>"
                                        + App.settings.getLocalizedString("instance.pleasesave")
                                        + "<br/><br/>"
                                        + (App.settings.isUsingMacApp() ? App.settings
                                                .getUsersDownloadsDir().getAbsolutePath()
                                                : App.settings.getDownloadsDir().getAbsolutePath()
                                                        + " or<br/>"
                                                        + App.settings.getUsersDownloadsDir())
                                        + "</center></html>",
                                App.settings.getLocalizedString("common.downloading") + " "
                                        + (serverFile == null ? getFile() : getServerFile()),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                options, options[0]);
                if (retValue == JOptionPane.CLOSED_OPTION) {
                    installer.cancel(true);
                    return;
                }

                if (isFilePattern()) {
                    String[] files = (App.settings.isUsingMacApp() ? App.settings
                            .getUsersDownloadsDir() : App.settings.getDownloadsDir())
                            .list(getFileNameFilter());
                    if (files.length == 1) {
                        this.file = files[0];
                        fileLocation = new File(
                                (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                        : App.settings.getDownloadsDir()), files[0]);
                    } else if (files.length > 1) {
                        for (int i = 0; i < files.length; i++) {
                            if (this.filePreference.equalsIgnoreCase("first") && i == 0) {
                                this.file = files[i];
                                fileLocation = new File(
                                        (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
                                                : App.settings.getDownloadsDir()), files[i]);
                                break;
                            }
                            if (this.filePreference.equalsIgnoreCase("last")
                                    && (i + 1) == files.length) {
                                this.file = files[i];
                                fileLocation = new File(
                                        (App.settings.isUsingMacApp() ? App.settings.getUsersDownloadsDir()
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
                    }
                }
            }
        } else if (isDirectDownloadServer()) {
            Downloadable download = new Downloadable(getServerURL(), fileLocation, this.serverMD5,
                    installer, false);
            if (download.needToDownload()) {
                download.download(false);
            }
        } else if (isServerDownloadServer()) {
            Downloadable download = new Downloadable(getServerURL(), fileLocation, this.serverMD5,
                    installer, true);
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
                    App.settings.log("Cannot download " + fileLocation.getAbsolutePath()
                            + ". Aborting install!", LogMessageType.error, false);
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
                Utils.unzip(fileLocation, installer.getTempTexturePackDirectory());
                installer.setTexturePackExtracted();
                break;
            case resourcepackextract:
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
                            if (thisFile.isDirectory()) {
                                return true;
                            } else {
                                return false;
                            }
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
            case plugins:
                Utils.copyFile(fileLocation, installer.getPluginsDirectory());
                break;
            case coremods:
                Utils.copyFile(fileLocation, installer.getCoreModsDirectory());
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
                File folder = new File(new File(App.settings.getTempDir(), getSafeName()),
                        this.extractFolder);
                switch (extractTo) {
                    case coremods:
                        Utils.copyDirectory(folder, installer.getCoreModsDirectory());
                        break;
                    case mods:
                        Utils.copyDirectory(folder, installer.getModsDirectory());
                        break;
                    case root:
                        Utils.copyDirectory(folder, installer.getRootDirectory());
                        break;
                    default:
                        App.settings.log("No known way to extract mod " + this.name + " with type "
                                + this.extractTo, LogMessageType.error, false);
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
                                Utils.copyFile(tempFileDecomp, installer.getCoreModsDirectory());
                            } else {
                                Utils.copyDirectory(tempFileDecomp,
                                        installer.getCoreModsDirectory());
                            }
                            break;
                        case jar:
                            if (tempFileDecomp.isFile()) {
                                Utils.copyFile(tempFileDecomp, installer.getJarModsDirectory());
                                installer.addToJarOrder(decompFile);
                            } else {
                                File newFile = new File(installer.getJarModsDirectory(),
                                        getSafeName() + ".zip");
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
                            App.settings.log("No known way to decomp mod " + this.name
                                    + " with type " + this.decompType, LogMessageType.error, false);
                            break;
                    }
                } else {
                    App.settings.log("Couldn't find decomp file " + this.decompFile + " for mod "
                            + this.name, LogMessageType.error, false);
                }
                Utils.delete(tempDirDecomp);
                break;
            default:
                App.settings.log("No known way to install mod " + this.name + " with type "
                        + thisType, LogMessageType.error, false);
                break;
        }
    }

    public String getURL() {
        return this.url;
    }

    public String getFile() {
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
            case plugins:
                base = installer.getPluginsDirectory();
                break;
            case coremods:
                base = installer.getCoreModsDirectory();
                break;
            case shaderpack:
                base = installer.getShaderPacksDirectory();
                break;
            default:
                App.settings.log("No known way to find installed mod " + this.name + " with type "
                        + thisType, LogMessageType.error, false);
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

    public String getServerURL() {
        return this.serverURL;
    }

    public String getServerFile() {
        return this.serverFile;
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
