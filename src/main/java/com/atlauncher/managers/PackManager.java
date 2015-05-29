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

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.manager.PackChangeManager;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.utils.Hashing;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PackManager {
    private static List<String> semiPublicPackCodes = new LinkedList<>();

    /**
     * Loads the Packs for use in the Launcher.
     */
    public static void loadPacks() {
        LogManager.debug("Loading packs");

        try {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {
            }.getType();
            Data.PACKS.clear();
            Data.PACKS.addAll((List<Pack>) JsonFile.of("packs.json", type));
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        PackChangeManager.change();

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

    public static void setSemiPublicPackCodes(List<String> codes) {
        PackManager.semiPublicPackCodes = codes;
    }

    public static boolean canViewSemiPublicPackByCode(String packCode) {
        for (String code : PackManager.semiPublicPackCodes) {
            if (Hashing.md5(code).toString().equalsIgnoreCase(packCode)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSemiPublicPackCodes() {
        return PackManager.semiPublicPackCodes;
    }

    public static String getSemiPublicPackCodesForProperties() {
        StringBuilder sb = new StringBuilder("");

        for (String code : PackManager.semiPublicPackCodes) {
            sb.append(code);
            sb.append(",");
        }

        return sb.toString();
    }

    public static boolean semiPublicPackExistsFromCode(String packCode) {
        String packCodeMD5 = Hashing.md5(packCode).toString();
        for (Pack pack : PackManager.getPacks()) {
            if (pack.isSemiPublic()) {
                if (pack.getCode().equalsIgnoreCase(packCodeMD5)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Pack getSemiPublicPackByCode(String packCode) {
        String packCodeMD5 = Hashing.md5(packCode).toString();
        for (Pack pack : PackManager.getPacks()) {
            if (pack.isSemiPublic()) {
                if (pack.getCode().equalsIgnoreCase(packCodeMD5)) {
                    return pack;
                }
            }
        }

        return null;
    }

    public static boolean addSemiPublicPack(String packCode) {
        String packCodeMD5 = Hashing.md5(packCode).toString();
        for (Pack pack : PackManager.getPacks()) {
            if (pack.isSemiPublic() && !PackManager.canViewSemiPublicPackByCode(packCodeMD5)) {
                if (pack.getCode().equalsIgnoreCase(packCodeMD5)) {
                    if (pack.isTester()) {
                        return false;
                    }
                    PackManager.semiPublicPackCodes.add(packCode);
                    App.settings.saveProperties();
                    PackChangeManager.change();
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeSemiPublicPack(String packCode) {
        for (String code : PackManager.semiPublicPackCodes) {
            if (Hashing.md5(code).toString().equalsIgnoreCase(packCode) && PackManager.semiPublicPackCodes.contains
                    (code)) {
                PackManager.semiPublicPackCodes.remove(code);
                App.settings.saveProperties();
                PackChangeManager.change();
            }
        }
    }

    public static int getInstallableCount() {
        int count = 0;

        for (Pack pack : PackManager.getPacks()) {
            if (pack.canInstall()) {
                count++;
            }
        }

        return count;
    }
}
