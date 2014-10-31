/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
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