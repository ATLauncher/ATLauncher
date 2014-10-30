/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import com.atlauncher.utils.Utils;

import java.io.File;

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
