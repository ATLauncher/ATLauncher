/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
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
    private String download;
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

    public String getMd5() {
        return this.md5;
    }

    public String getDownload() {
        return this.download;
    }

    public String getDepends() {
        return this.depends;
    }

    public String getDependsGroup() {
        return this.dependsGroup;
    }
}
