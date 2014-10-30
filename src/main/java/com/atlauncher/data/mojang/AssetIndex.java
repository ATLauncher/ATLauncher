/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import java.util.HashSet;
import java.util.Map;

public class AssetIndex {

    private Map<String, AssetObject> objects;
    private boolean virtual;

    public Map<String, AssetObject> getObjects() {
        return this.objects;
    }

    public HashSet<AssetObject> getUniqueObjects() {
        return new HashSet<AssetObject>(this.objects.values());
    }

    public boolean isVirtual() {
        return this.virtual;
    }
}
