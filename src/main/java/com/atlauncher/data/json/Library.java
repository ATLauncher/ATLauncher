/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

@Json
public class Library {
    private String url;
    private String file;
    private String server;
    private String md5;
    private DownloadType download;
    private String depends;
    private String dependsGroup;

    public String getUrl() {
        return this.url;
    }

    public String getFile() {
        return this.file;
    }

    public String getServer() {
        return this.server;
    }

    public String getMD5() {
        return this.md5;
    }

    public DownloadType getDownloadType() {
        return this.download;
    }

    public String getDepends() {
        return this.depends;
    }

    public String getDependsGroup() {
        return this.dependsGroup;
    }

    public boolean hasDepends() {
        return this.depends != null;
    }

    public boolean hasDependsGroup() {
        return this.dependsGroup != null;
    }

    public boolean forServer() {
        return this.server != null;
    }
}
