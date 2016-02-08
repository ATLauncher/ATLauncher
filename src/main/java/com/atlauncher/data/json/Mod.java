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

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Downloadable;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.workers.InstanceInstaller;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;

@Json
public final class Mod {
    public static final int MAX_ATTEMPTS = 5;

    public final String name;
    public final String version;
    public final String url;
    public final Hashing.HashCode md5;
    public final Hashing.HashCode serverMD5;
    public final String donation;
    public final String website;
    public final String description;
    public final String file;
    public final String serverUrl;
    public final String serverFile;
    public final String filePrefix;
    public final String decompFile;
    public final String group;
    public final String linked;
    public final String filePreference;
    public final String colour;
    public final String fileCheck;
    public final String warning;
    public final String[] depends;
    public final String[] authors;
    public final boolean client;
    public final boolean optional;
    public final boolean server;
    public final boolean recommended;
    public final boolean hidden;
    public final boolean library;
    public final boolean filePattern;
    public final boolean selected;
    public final Boolean serverOptional;
    public final int filesize;
    public final DownloadType download;
    public final DownloadType serverDownload;
    public final ModType type;
    public final ModType serverType;
    public final ExtractToType extractTo;
    public final DecompType decompType;
    public final boolean force;

    private Color compiledColour;

    public Mod(String name, String version, String url, Hashing.HashCode md5, Hashing.HashCode serverMD5, String
            donation, String website, String description, String file, String serverUrl, String serverFile, String
                       filePrefix, String decompFile, String group, String linked, String filePreference, String colour, String
                       fileCheck, String warning, String[] depends, String[] authors, boolean client, boolean optional, boolean
                       server, boolean recommended, boolean hidden, boolean library, boolean filePattern, Boolean
                       serverOptional, int filesize, DownloadType download, DownloadType serverDownload, ModType type, ModType
                       serverType, ExtractToType extractTo, DecompType decompType, boolean selected, boolean force) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.md5 = md5;
        this.serverMD5 = serverMD5;
        this.donation = donation;
        this.website = website;
        this.description = description;
        this.file = file;
        this.serverUrl = serverUrl;
        this.serverFile = serverFile;
        this.filePrefix = filePrefix;
        this.decompFile = decompFile;
        this.group = group;
        this.linked = linked;
        this.filePreference = filePreference;
        this.colour = colour;
        this.fileCheck = fileCheck;
        this.warning = warning;
        this.depends = depends;
        this.authors = authors;
        this.client = client;
        this.optional = optional;
        this.server = server;
        this.recommended = recommended;
        this.hidden = hidden;
        this.library = library;
        this.filePattern = filePattern;
        this.serverOptional = serverOptional;
        this.filesize = filesize;
        this.download = download;
        this.serverDownload = serverDownload;
        this.type = type;
        this.serverType = serverType;
        this.extractTo = extractTo;
        this.decompType = decompType;
        this.selected = selected;
        this.force = force;
    }

    public String getUrl() {
        return this.url.replace("&amp;", "&").replace(" ", "%20").replace("[", "%5B").replace("]", "%5D");
    }

    public boolean isServerOptional() {
        return (this.serverOptional == null ? this.optional : this.serverOptional);
    }

    public boolean hasDescription() {
        return this.description != null && !this.description.isEmpty();
    }

    public boolean hasMD5() {
        return this.md5 != null && !this.md5.equals(Hashing.HashCode.EMPTY);
    }

    public boolean hasWebsite() {
        return this.website != null && !this.website.isEmpty();
    }

    public boolean hasGroup() {
        return this.group != null && !this.group.isEmpty();
    }

    public boolean hasWarning() {
        return this.warning != null && !this.warning.isEmpty();
    }

    public boolean hasLinked() {
        return this.linked != null && !this.linked.isEmpty();
    }

    public boolean hasDepends() {
        return this.depends != null && this.depends.length != 0;
    }

    public boolean isDependencyOf(Mod mod) {
        for (String str : this.depends) {
            if (str.equalsIgnoreCase(mod.name)) {
                return true;
            }
        }

        return false;
    }

    public Path getInstalledFile(InstanceInstaller installer) {
        String file = (installer.server ? this.serverFile : this.getFile());
        Path base = (installer.server ? this.serverType : this.type).getInstallDirectory(installer, this);
        return base.resolve(file);
    }

    public DisableableMod generateDisableableMod(InstanceInstaller installer, String file) {
        return new DisableableMod(this.name, this.version, this.optional, file, this.type, installer.version
                .getColour(this.colour), this.description, false, false);
    }

    public Downloadable generateDownloadable(InstanceInstaller installer) {
        return new Downloadable(this.getUrl(), this.md5.toString(), FileSystem.DOWNLOADS.resolve(this.getFile()),
                this.filesize, true, installer);
    }

    private void downloadClient(InstanceInstaller installer, int attempt) throws Exception {
        Path fileLoc = FileSystem.DOWNLOADS.resolve(this.getFile());
        if (Files.exists(fileLoc)) {
            if (this.force) {
                FileUtils.delete(fileLoc);
            } else if (this.download != DownloadType.DIRECT) {
                if (this.hasMD5()) {
                    if (Hashing.md5(fileLoc).equals(this.md5)) {
                        return;
                    } else {
                        FileUtils.delete(fileLoc);
                    }
                } else {
                    long size = 0;
                    try {
                        size = Files.size(fileLoc);
                    } catch (Exception e) {
                        LogManager.logStackTrace("Error getting file size of " + fileLoc, e);
                    }
                    if (size != 0) {
                        return;
                    }
                }
            }
        }

        this.download.download(installer, fileLoc, this);

        if (this.hasMD5() && !Hashing.md5(fileLoc).equals(this.md5)) {
            if (attempt < MAX_ATTEMPTS) {
                FileUtils.delete(fileLoc);
                this.downloadClient(installer, attempt + 1);
            } else {
                LogManager.error("Cannot download " + fileLoc + ". Aborting install");
                installer.cancel(true);
            }
        }
    }

    private void downloadServer(InstanceInstaller installer, int attempt) throws Exception {
        Path fileLoc = FileSystem.DOWNLOADS.resolve(this.serverFile);
        if (Files.exists(fileLoc)) {
            if (this.force) {
                FileUtils.delete(fileLoc);
            } else if (this.download != DownloadType.DIRECT) {
                if (this.serverMD5 != null && !this.serverMD5.equals(Hashing.HashCode.EMPTY)) {
                    if (Hashing.md5(fileLoc).equals(this.serverMD5)) {
                        return;
                    } else {
                        FileUtils.delete(fileLoc);
                    }
                } else {
                    return;
                }
            }
        }

        if (this.serverDownload == DownloadType.BROWSER) {
            this.serverDownload.download(installer, fileLoc, this);
        } else {
            try {
                Downloadable dl = new Downloadable(this.serverUrl, this.serverMD5.toString(), fileLoc, -1, this
                        .serverDownload == DownloadType.SERVER, installer);

                if (this.serverDownload == DownloadType.DIRECT) {
                    dl.checkForNewness();
                }

                if (dl.needToDownload()) {
                    dl.download();
                }
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }

        if (this.serverMD5 != null && !this.serverMD5.equals(Hashing.HashCode.EMPTY)) {
            if (attempt < MAX_ATTEMPTS) {
                FileUtils.delete(fileLoc);
                this.downloadServer(installer, attempt + 1);
            } else {
                LogManager.error("Cannot download " + fileLoc + ". Aborting install");
                installer.cancel(true);
            }
        }
    }

    protected Downloadable generateDownloadable(Path to, InstanceInstaller installer, boolean server) {
        return new Downloadable(this.getUrl(), this.md5 == null ? null : this.md5.toString(), to, -1, server,
                installer);
    }

    public void download(InstanceInstaller installer) throws Exception {
        if (installer.server && this.serverUrl != null) {
            this.downloadServer(installer, 1);
        } else {
            this.downloadClient(installer, 1);
        }
    }

    public void install(InstanceInstaller installer) throws Exception {
        ModType type = this.getType(installer);
        if (type == null) {
            throw new IllegalStateException("Type == null on mod: " + this.name);
        }
        type.install(installer, this);
    }

    public String getFile() {
        if (this.filePrefix != null) {
            return this.filePrefix + this.file;
        } else {
            return this.file;
        }
    }

    protected Path getFile(InstanceInstaller installer) {
        if (installer.server && this.serverUrl != null) {
            return FileSystem.DOWNLOADS.resolve(this.serverFile);
        } else {
            return FileSystem.DOWNLOADS.resolve(this.file);
        }
    }

    protected ModType getType(InstanceInstaller installer) {
        if (installer.server && this.serverUrl != null) {
            return this.serverType;
        } else {
            return this.type;
        }
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public Color getCompiledColour() {
        return this.compiledColour;
    }

    public void setCompiledColour(Color compiledColour) {
        this.compiledColour = compiledColour;
    }
}