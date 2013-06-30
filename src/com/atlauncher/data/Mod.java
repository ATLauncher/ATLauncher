/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.io.File;

import javax.swing.JOptionPane;

import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class Mod {

    private String name;
    private String version;
    private String url;
    private String file;
    private String website;
    private String donation;
    private String md5;
    private Type type;
    private ExtractTo extractTo;
    private String decompFile;
    private DecompType decompType;
    private boolean server;
    private String serverURL;
    private String serverFile;
    private Type serverType;
    private boolean optional;
    private boolean directDownload;
    private String linked;
    private String description;

    public Mod(String name, String version, String url, String file, String website,
            String donation, String md5, Type type, ExtractTo extractTo, String decompFile,
            DecompType decompType, boolean server, String serverURL, String serverFile,
            Type serverType, boolean optional, boolean directDownload, String linked, String description) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.file = file;
        this.website = website;
        this.donation = donation;
        this.md5 = md5;
        this.type = type;
        this.extractTo = extractTo;
        this.decompFile = decompFile;
        this.decompType = decompType;
        this.server = server;
        this.serverURL = serverURL;
        this.serverFile = serverFile;
        this.serverType = serverType;
        this.optional = optional;
        this.directDownload = directDownload;
        this.linked = linked;
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
    
    public String getLinked() {
        return this.linked;
    }
    
    public String getDescription() {
        return this.description;
    }

    public void download(InstanceInstaller installer) {
        File fileLocation = new File(LauncherFrame.settings.getDownloadsDir(), getFile());
        if (fileLocation.exists()) {
            if (hasMD5()) {
                if (compareMD5(Utils.getMD5(fileLocation))) {
                    return; // File already exists and matches hash, don't download it
                } else {
                    fileLocation.delete(); // File exists but is corrupt, delete it
                }
            } else {
                return; // No MD5, but file is there, can only assume it's fine
            }
        }
        if (isDirectDownload()) {
            if (getURL().contains("http://newfiles.atlauncher.com/")) {
                new Downloader(LauncherFrame.settings.getFileURL(getURL().replace(
                        "http://newfiles.atlauncher.com/", "")), fileLocation.getAbsolutePath(),
                        installer).runNoReturn();
            } else {
                new Downloader(getURL(), fileLocation.getAbsolutePath(), installer).runNoReturn();
            }
        } else {
            while (!fileLocation.exists()) {
                Utils.openBrowser(getURL());
                String[] options = new String[] { "I've Downloaded This File" };
                int retValue = JOptionPane
                        .showOptionDialog(
                                LauncherFrame.settings.getParent(),
                                "<html><center>Browser opened to download file "
                                        + getFile()
                                        + "<br/><br/>Please save this file to the following location<br/><br/>"
                                        + LauncherFrame.settings.getDownloadsDir()
                                                .getAbsolutePath() + "</center></html>",
                                "Downloading " + getFile(), JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (retValue == JOptionPane.CLOSED_OPTION) {
                    installer.cancel(true);
                    break;
                }
            }
        }
        if (hasMD5()) {
            if (compareMD5(Utils.getMD5(fileLocation))) {
                return; // MD5 hash matches
            } else {
                fileLocation.delete(); // MD5 hash doesn't match, delete it
                download(installer); // download again
            }
        } else {
            return; // No MD5, but file is there, can only assume it's fine
        }
    }

    public void install(InstanceInstaller installer) {
        File fileLocation = new File(LauncherFrame.settings.getDownloadsDir(), getFile());
        switch (type) {
            case jar:
                Utils.copyFile(fileLocation, installer.getJarModsDirectory());
                installer.addToJarOrder(getFile());
                break;
            case mods:
                Utils.copyFile(fileLocation, installer.getModsDirectory());
                break;
            case coremods:
                Utils.copyFile(fileLocation, installer.getCoreModsDirectory());
                break;
            case extract:
                File tempDirExtract = new File(LauncherFrame.settings.getTempDir(), getSafeName());
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
                        LauncherFrame.settings.getConsole().log(
                                "No known way to extract mod " + this.name + " with type "
                                        + this.extractTo);
                        break;
                }
                Utils.delete(tempDirExtract);
                break;
            case decomp:
                File tempDirDecomp = new File(LauncherFrame.settings.getTempDir(), getSafeName());
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
                                Utils.copyFile(tempFileDecomp, installer.getMinecraftDirectory());
                            } else {
                                Utils.copyDirectory(tempFileDecomp,
                                        installer.getMinecraftDirectory());
                            }
                            break;
                        default:
                            LauncherFrame.settings.getConsole().log(
                                    "No known way to decomp mod " + this.name + " with type "
                                            + this.decompType);
                            break;
                    }
                } else {
                    LauncherFrame.settings.getConsole().log(
                            "Couldn't find decomp file " + this.decompFile + " for mod "
                                    + this.name);
                }
                Utils.delete(tempDirDecomp);
                break;
            default:
                LauncherFrame.settings.getConsole().log(
                        "No known way to install mod " + this.name + " with type " + this.type);
                break;
        }
    }

    public boolean isDirectDownload() {
        return this.directDownload;
    }

    public String getURL() {
        return this.url;
    }

    public String getFile() {
        return this.file;
    }

}
