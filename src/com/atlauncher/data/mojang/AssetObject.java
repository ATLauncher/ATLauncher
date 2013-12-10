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

import java.io.File;

import com.atlauncher.utils.Utils;

public class AssetObject {

    private String hash;
    private long size;

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public boolean needToDownload(File file) {
        if (!file.exists() || !file.isFile()) {
            return true;
        }
        if (file.length() != this.size) {
            return true;
        }
        if (!this.hash.equalsIgnoreCase(Utils.getSHA1(file))) {
            return true;
        }
        return false;
    }
}
