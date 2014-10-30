/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
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