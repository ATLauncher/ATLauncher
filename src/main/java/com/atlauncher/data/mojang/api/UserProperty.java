/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.api;

import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;

import java.util.Map;

@Json
public class UserProperty {
    private long timestamp;
    private String profileId;
    private String profileName;
    private boolean isPublic;
    private Map<String, ProfileTexture> textures;

    public ProfileTexture getTexture(String name) {
        if(!textures.containsKey(name)) {
            LogManager.error("No texture " + name + " for account " + this.profileName);
            return null;
        }

        return textures.get(name);
    }
}
