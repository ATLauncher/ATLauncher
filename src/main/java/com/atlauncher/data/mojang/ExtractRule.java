/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import java.util.List;

public class ExtractRule {

    private List<String> exclude;

    public boolean shouldExclude(String filename) {
        if (exclude == null) {
            return false;
        }
        for (String name : exclude) {
            if (filename.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

}
