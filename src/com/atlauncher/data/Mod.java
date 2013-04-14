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

public class Mod {

    private String name;
    private String version;
    private String url;
    private String file;
    private String website;
    private String donation;
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
    private String description;

    public Mod(String name, String version, String url, String file,
            String website, String donation, Type type, ExtractTo extractTo,
            String decompFile, DecompType decompType, boolean server,
            String serverURL, String serverFile, Type serverType,
            boolean optional, boolean directDownload, String description) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.file = file;
        this.website = website;
        this.donation = donation;
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
        this.description = description;
    }

}
