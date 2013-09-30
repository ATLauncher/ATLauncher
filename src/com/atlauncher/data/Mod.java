/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.Color;
import java.io.File;

import javax.swing.JOptionPane;

import com.atlauncher.App;
import com.atlauncher.gui.Utils;
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
    private String decompFile;
    private DecompType decompType;
    private boolean client;
    private boolean server;
    private String serverURL;
    private String serverFile;
    private Type serverType;
    private boolean optional;
    private boolean serverOptional;
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
            String decompFile, DecompType decompType, boolean client, boolean server,
            String serverURL, String serverFile, Type serverType, boolean optional,
            boolean serverOptional, Download download, boolean hidden, boolean library,
            String group, String linked, String[] depends, boolean recommended, String description) {
        this.name = name;
        this.version = version;
        this.url = url.replace("&amp;", "&");
        this.file = file;
        this.website = website;
        this.donation = donation;
        this.colour = colour;
        this.md5 = md5;
        this.type = type;
        this.extractTo = extractTo;
        this.decompFile = decompFile;
        this.decompType = decompType;
        this.client = client;
        this.server = server;
        this.serverURL = (serverURL == null) ? null : serverURL.replace("&amp;", "&");
        this.serverFile = serverFile;
        this.serverType = serverType;
        this.optional = optional;
        this.serverOptional = serverOptional;
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

    public String getMD5() {
        return this.md5;
    }

    public boolean compareMD5(String md5) {
        return this.md5.equalsIgnoreCase(md5);
    }

    public boolean hasMD5() {
        return !this.md5.isEmpty();
    }

    public boolean isOptional() {
        return this.optional;
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

    public void download(InstanceInstaller installer) {
        download(installer, 1);
    }

    public void download(InstanceInstaller installer, int attempt) {
        File fileLocation;
        if (serverFile == null) {
            fileLocation = new File(App.settings.getDownloadsDir(), getFile());
        } else {
            fileLocation = new File(App.settings.getDownloadsDir(), getServerFile());
        }
        if (fileLocation.exists()) {
            if (hasMD5()) {
                if (compareMD5(Utils.getMD5(fileLocation))) {
                    return; // File already exists and matches hash, don't download it
                } else {
                    Utils.delete(fileLocation); // File exists but is corrupt, delete it
                }
            } else {
                return; // No MD5, but file is there, can only assume it's fine
            }
        }
        switch (download) {
            case browser:
                while (!fileLocation.exists()) {
                    if (serverURL == null) {
                        Utils.openBrowser(getURL());
                    } else {
                        Utils.openBrowser(getServerURL());
                    }
                    String[] options = new String[] { App.settings
                            .getLocalizedString("instance.ivedownloaded") };
                    int retValue = JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("instance.browseropened",
                                            (serverFile == null ? getFile() : getServerFile()))
                                    + "<br/><br/>"
                                    + App.settings.getLocalizedString("instance.pleasesave")
                                    + "<br/><br/>"
                                    + (App.settings.isUsingMacApp() ? App.settings
                                            .getMacAppDownloadsDir().getAbsolutePath()
                                            : App.settings.getDownloadsDir().getAbsolutePath())
                                    + "</center></html>",
                            App.settings.getLocalizedString("common.downloading") + " "
                                    + (serverFile == null ? getFile() : getServerFile()),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            options, options[0]);
                    if (retValue == JOptionPane.CLOSED_OPTION) {
                        installer.cancel(true);
                        return;
                    }
                    if (App.settings.isUsingMacApp()) {
                        File macFile;
                        if (serverFile == null) {
                            macFile = new File(App.settings.getMacAppDownloadsDir(), getFile());
                        } else {
                            macFile = new File(App.settings.getMacAppDownloadsDir(),
                                    getServerFile());
                        }
                        if (macFile.exists()) {
                            Utils.copyFile(macFile, fileLocation, true);
                            Utils.delete(macFile);
                        }
                    }
                }
                break;
            case direct:
                if (serverURL == null) {
                    new Downloader(getURL(), fileLocation.getAbsolutePath(), installer).run();
                } else {
                    new Downloader(getServerURL(), fileLocation.getAbsolutePath(), installer).run();
                }
                break;
            case server:
                if (serverURL == null) {
                    new Downloader(App.settings.getFileURL(getURL()),
                            fileLocation.getAbsolutePath(), installer).run();
                } else {
                    new Downloader(App.settings.getFileURL(getServerURL()),
                            fileLocation.getAbsolutePath(), installer).run();
                }
                break;
        }
        if (hasMD5()) {
            if (compareMD5(Utils.getMD5(fileLocation))) {
                return; // MD5 hash matches
            } else {
                if (attempt < 5) {
                    Utils.delete(fileLocation); // MD5 hash doesn't match, delete it
                    download(installer, ++attempt); // download again
                } else {
                    App.settings.getConsole().log(
                            "Cannot download " + fileLocation.getAbsolutePath()
                                    + ". Aborting install", true);
                    installer.cancel(true);
                }
            }
        } else {
            return; // No MD5, but file is there, can only assume it's fine
        }
    }

    public void install(InstanceInstaller installer) {
        File fileLocation = new File(App.settings.getDownloadsDir(), getFile());
        if (installer.isNewLaunchMethod() && type == Type.coremods) {
            type = Type.mods;
        }
        switch (type) {
            case jar:
            case forge:
                if (installer.isServer() && type == Type.forge) {
                    Utils.copyFile(fileLocation, installer.getRootDirectory());
                    break;
                } else if (installer.isServer() && type == Type.jar) {
                    Utils.unzip(fileLocation, installer.getTempJarDirectory());
                    break;
                }
                Utils.copyFile(fileLocation, installer.getJarModsDirectory());
                installer.addToJarOrder(getFile());
                break;
            case texturepack:
                Utils.copyFile(fileLocation, installer.getTexturePacksDirectory());
                break;
            case resourcepack:
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
            case mods:
                Utils.copyFile(fileLocation, installer.getModsDirectory());
                break;
            case coremods:
                Utils.copyFile(fileLocation, installer.getCoreModsDirectory());
                break;
            case extract:
                File tempDirExtract = new File(App.settings.getTempDir(), getSafeName());
                Utils.unzip(fileLocation, tempDirExtract);
                switch (extractTo) {
                    case coremods:
                        Utils.copyDirectory(tempDirExtract, installer.getCoreModsDirectory());
                        break;
                    case mods:
                        Utils.copyDirectory(tempDirExtract, installer.getModsDirectory());
                        break;
                    case root:
                        Utils.copyDirectory(tempDirExtract, installer.getRootDirectory());
                        break;
                    default:
                        App.settings.getConsole().log(
                                "No known way to extract mod " + this.name + " with type "
                                        + this.extractTo);
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
                            App.settings.getConsole().log(
                                    "No known way to decomp mod " + this.name + " with type "
                                            + this.decompType);
                            break;
                    }
                } else {
                    App.settings.getConsole().log(
                            "Couldn't find decomp file " + this.decompFile + " for mod "
                                    + this.name);
                }
                Utils.delete(tempDirDecomp);
                break;
            default:
                App.settings.getConsole().log(
                        "No known way to install mod " + this.name + " with type " + this.type);
                break;
        }
    }

    public String getURL() {
        return this.url;
    }

    public String getFile() {
        return this.file;
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
