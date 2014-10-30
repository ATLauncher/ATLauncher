/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data;

import com.atlauncher.App;

import java.io.File;

public class DownloadableFile {
    private String name;
    private String folder;
    private int size;
    private String md5;
    private String sha1;

    public boolean isLauncher() {
        return this.name.equals("Launcher");
    }

    public String getMD5() {
        return this.md5;
    }

    public String getSHA1() {
        return this.sha1;
    }

    public Downloadable getDownloadable() {
        File file = new File(new File(App.settings.getConfigsDir(), this.folder), this.name);
        if (this.folder.equalsIgnoreCase("Skins")) {
            file = new File(App.settings.getSkinsDir(), this.name);
        }
        return new Downloadable("launcher/" + this.folder.toLowerCase() + "/" + this.name, file, this.sha1,
                this.size, null, true);
    }
}
