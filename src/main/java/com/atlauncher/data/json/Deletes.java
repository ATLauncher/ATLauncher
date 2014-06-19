/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import java.util.List;

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