/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.api;

import com.atlauncher.annot.Json;

@Json
public class ProfileTexture {
    private String url;

    public String getUrl() {
        return this.url;
    }
}
