/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.api;

import com.atlauncher.Gsons;
import com.atlauncher.annot.Json;
import com.atlauncher.utils.Base64;

import java.io.IOException;

@Json
public class UserPropertyRaw {
    private String name;
    private String value;
    private String signature;

    public String getName() {
        return this.name;
    }

    public UserProperty parse() throws IOException {
        return Gsons.DEFAULT.fromJson(new String(Base64.decode(this.value)), UserProperty.class);
    }
}
