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
package com.atlauncher.data;

import java.util.ArrayList;

public class Addons {
    
    private ArrayList<Addon> addons;

    public Addons() {
        addons = new ArrayList<Addon>();
    }

    public void addAddon(Addon addon) {
        addons.add(addon);
    }

    public int totalAddons() {
        return addons.size();
    }

    public Addon getAddon(int index) {
        return addons.get(index);
    }

}
