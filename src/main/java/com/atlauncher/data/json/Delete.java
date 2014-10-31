/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

@Json
public class Delete {
    private String base;
    private String target;

    public String getBase() {
        return this.base;
    }

    public String getTarget() {
        return this.target;
    }
}
