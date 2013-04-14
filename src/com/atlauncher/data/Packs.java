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

public class Packs {

    private ArrayList<Pack> packs;

    public Packs() {
        packs = new ArrayList<Pack>();
    }

    public void addPack(Pack pack) {
        packs.add(pack);
    }

    public int totalPacks() {
        return packs.size();
    }

    public String getName(int index) {
        Pack pack = packs.get(index);
        return pack.getName();
    }

    public Pack getPack(int index) {
        return packs.get(index);
    }

    public String getDescription(int index) {
        Pack pack = packs.get(index);
        return pack.getDescription();
    }

    public Version[] getVersions(int index) {
        Pack pack = packs.get(index);
        return pack.getVersions();
    }

}
