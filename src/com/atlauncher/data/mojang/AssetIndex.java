/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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
        return new HashSet(this.objects.values());
    }

    public boolean isVirtual() {
        return this.virtual;
    }
}
