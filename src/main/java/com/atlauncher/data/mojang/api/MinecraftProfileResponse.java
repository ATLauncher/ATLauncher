/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
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
