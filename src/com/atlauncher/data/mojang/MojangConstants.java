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
package com.atlauncher.data.mojang;

public enum MojangConstants {

    LIBRARIES_BASE("https://libraries.minecraft.net/"),
    RESOURCES_BASE("http://resources.download.minecraft.net/"),
    DOWNLOAD_BASE("http://s3.amazonaws.com/Minecraft.Download/");

    private final String url;

    private MojangConstants(String url) {
        this.url = url;
    }

    public String getURL(String path) {
        return this.url + path;
    }
}