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

import com.atlauncher.App;
import com.atlauncher.annot.Json;

import java.io.IOException;
import java.util.List;

@Json
public class MinecraftProfileResponse {
    private String id;
    private String name;
    private List<UserPropertyRaw> properties;

    public UserProperty getUserProperty(String name) {
        for (UserPropertyRaw property : this.properties) {
            if (property.getName().equals(name)) {
                try {
                    return property.parse();
                } catch (IOException e) {
                    App.settings.logStackTrace("Error parsing user property " + name + " for username " + name, e);
                }
            }
        }
        return null;
    }
}
