/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

import java.util.List;

@Json
public class Deletes {
    private List<Delete> files;
    private List<Delete> folders;

    public List<Delete> getFiles() {
        return this.files;
    }

    public List<Delete> getFolders() {
        return this.folders;
    }
}