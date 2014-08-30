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
