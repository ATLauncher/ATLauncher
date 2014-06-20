/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import java.awt.Color;
import java.util.List;

import com.atlauncher.annot.Json;

@Json
public class Mod {
    private String name;
    private String version;
    private String url;
    private String file;
    private String md5;
    private String download;
    private String website;
    private String donation;
    private String authors;
    private String sha1;
    private String colour;
    private Color compiledColour;
    private ModType type;
    private String extractTo;
    private String extractFolder;
    private String decompFile;
    private DecompType decompType;
    private boolean filePatten;
    private String filePreference;
    private String fileCheck;
    private boolean client;
    private boolean server;
    private boolean serverSeparate;
    private String serverUrl;
    private String serverFile;
    private ModType serverType;
    private String serverDownload;
    private boolean serverOptional;
    private boolean optional;
    private boolean selected;
    private boolean recommended;
    private boolean hidden;
    private boolean library;
    private String group;
    private String linked;
    private List<String> depends;
    private String filePrefix;
    private String description;

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getUrl() {
        return this.url;
    }

    public String getFile() {
        return this.file;
    }

    public String getMd5() {
        return this.md5;
    }

    public String getDownload() {
        return this.download;
    }

    public String getWebsite() {
        return this.website;
    }

    public String getDonation() {
        return this.donation;
    }

    public String getAuthors() {
        return this.authors;
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

    public Color getCompiledColour() {
        return this.compiledColour;
    }

    public void setCompiledColour(Color colour) {
        this.compiledColour = colour;
    }

    public ModType getType() {
        return this.type;
    }

    public String getExtractTo() {
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

    public boolean isFilePatten() {
        return this.filePatten;
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

    public String getServerDownload() {
        return this.serverDownload;
    }

    public boolean isServerOptional() {
        return this.serverOptional;
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
}