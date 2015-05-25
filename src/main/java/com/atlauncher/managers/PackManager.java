/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.atlauncher.managers;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Pack;
import com.atlauncher.exceptions.InvalidPack;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PackManager {
    /**
     * Loads the Packs for use in the Launcher.
     */
    public static void loadPacks() {
        LogManager.debug("Loading packs");

        try {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {
            }.getType();
            byte[] bits = Files.readAllBytes(FileSystem.JSON.resolve("packs.json"));
            Data.PACKS.clear();
            Data.PACKS.addAll((List<Pack>) Gsons.DEFAULT.fromJson(new String(bits), type));
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading packs");
    }

    /**
     * Get the Packs available in the Launcher.
     *
     * @return The Packs available in the Launcher
     */
    public static List<Pack> getPacks() {
        return Data.PACKS;
    }

    /**
     * Get the Packs available in the Launcher sorted alphabetically.
     *
     * @return The Packs available in the Launcher sorted alphabetically
     */
    public static List<Pack> getPacksSortedAlphabetically() {
        List<Pack> packs = new LinkedList<>(Data.PACKS);

        Collections.sort(packs, new Comparator<Pack>() {
            public int compare(Pack result1, Pack result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });

        return packs;
    }

    /**
     * Get the Packs available in the Launcher sorted by position.
     *
     * @return The Packs available in the Launcher sorted by position
     */
    public static List<Pack> getPacksSortedPositionally() {
        List<Pack> packs = new LinkedList<>(Data.PACKS);

        Collections.sort(packs, new Comparator<Pack>() {
            public int compare(Pack result1, Pack result2) {
                return (result1.getPosition() < result2.getPosition()) ? -1 : ((result1.getPosition() == result2
                        .getPosition()) ? 0 : 1);
            }
        });

        return packs;
    }

    /**
     * Finds a Pack from the given ID number.
     *
     * @param id ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack If ID is not found
     */
    public static Pack getPackByID(int id) throws InvalidPack {
        for (Pack pack : PackManager.getPacks()) {
            if (pack.getID() == id) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with ID " + id);
    }

    /**
     * Checks if there is a pack by the given name.
     *
     * @param name name of the Pack to find
     * @return True if the pack is found from the name
     */
    public static boolean isPackByName(String name) {
        for (Pack pack : PackManager.getPacks()) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given name.
     *
     * @param name name of the Pack to find
     * @return Pack if the pack is found from the name
     */
    public static Pack getPackByName(String name) {
        for (Pack pack : PackManager.getPacks()) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Finds a Pack from the given safe name.
     *
     * @param name name of the Pack to find
     * @return Pack if the pack is found from the safe name
     */
    public static Pack getPackBySafeName(String name) {
        for (Pack pack : PackManager.getPacks()) {
            if (pack.getSafeName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }
}
