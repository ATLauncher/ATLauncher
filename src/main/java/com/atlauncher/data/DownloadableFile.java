/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.io.File;

import com.atlauncher.App;

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
        return new Downloadable("launcher/" + this.folder.toLowerCase() + "/" + this.name, file,
                this.sha1, this.size, null, true);
    }
}
