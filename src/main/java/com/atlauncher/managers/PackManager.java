/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackUsers;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.utils.Hashing;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PackManager {
    private static final Logger LOG = LogManager.getLogger(PackManager.class);

    public static List<Pack> getPacks() {
        return Data.PACKS;
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    public static void loadPacks() {
        PerformanceManager.start();
        LOG.debug("Loading packs");
        Data.PACKS.clear();
        try {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {
            }.getType();
            Data.PACKS.addAll(Gsons.DEFAULT_ALT
                .fromJson(new FileReader(FileSystem.JSON.resolve("packsnew.json").toFile()), type));
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LOG.error("error: ", e);
        }
        LOG.debug("Finished loading packs");
        PerformanceManager.end();
    }

    /**
     * Get the Packs available in the Launcher sorted alphabetically
     *
     * @return The Packs available in the Launcher sorted alphabetically
     */
    public static List<Pack> getPacksSortedAlphabetically(boolean isFeatured, boolean sortDescending) {
        List<Pack> packs = new LinkedList<>();

        for (Pack pack : Data.PACKS) {
            if (isFeatured) {
                if (!pack.isFeatured()) {
                    continue;
                }
            }

            if (!pack.isSystem()) {
                packs.add(pack);
            }
        }

        packs.sort(Comparator.comparing(p -> p.getName().toLowerCase()));

        if (sortDescending) {
            Collections.reverse(packs);
        }

        return packs;
    }

    /**
     * Get the Packs available in the Launcher sorted by position
     *
     * @return The Packs available in the Launcher sorted by position
     */
    public static List<Pack> getPacksSortedPositionally(boolean isFeatured, boolean sortDescending) {
        List<Pack> packs = new LinkedList<>();

        for (Pack pack : Data.PACKS) {
            if (isFeatured) {
                if (!pack.isFeatured()) {
                    continue;
                }
            }

            if (!pack.isSystem()) {
                packs.add(pack);
            }
        }

        packs.sort(Comparator.comparingInt(Pack::getPosition));

        if (sortDescending) {
            Collections.reverse(packs);
        }

        return packs;
    }

    public static void setPackVisbility(Pack pack, boolean collapsed) {
        if (pack != null && AccountManager.getSelectedAccount() != null) {
            if (collapsed) {
                // Closed It
                if (!AccountManager.getSelectedAccount().collapsedPacks.contains(pack.getName())) {
                    AccountManager.getSelectedAccount().collapsedPacks.add(pack.getName());
                }
            } else {
                // Opened It
                AccountManager.getSelectedAccount().collapsedPacks.remove(pack.getName());
            }
            AccountManager.saveAccounts();
        }
    }

    public static String getPackInstallableCount() {
        int count = 0;
        for (Pack pack : Data.PACKS) {
            if (pack.canInstall()) {
                count++;
            }
        }
        return count + "";
    }

    /**
     * Finds a Pack from the given ID number
     *
     * @param id ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack If ID is not found
     */
    public static Pack getPackByID(int id) throws InvalidPack {
        for (Pack pack : Data.PACKS) {
            if (pack.getID() == id) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with ID " + id);
    }

    /**
     * Checks if there is a pack by the given name
     *
     * @param name name of the Pack to find
     * @return True if the pack is found from the name
     */
    public static boolean isPackByName(String name) {
        for (Pack pack : Data.PACKS) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given name
     *
     * @param name name of the Pack to find
     * @return Pack if the pack is found from the name
     */
    public static Pack getPackByName(String name) {
        for (Pack pack : Data.PACKS) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Finds a Pack from the given safe name
     *
     * @param name name of the Pack to find
     * @return Pack if the pack is found from the safe name
     */
    public static Pack getPackBySafeName(String name) {
        for (Pack pack : Data.PACKS) {
            if (pack.getSafeName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    public static boolean semiPublicPackExistsFromCode(String packCode) {
        for (Pack pack : Data.PACKS) {
            if (pack.isSemiPublic()) {
                if (Hashing.toHashCode(pack.getCode()).equals(Hashing.md5(packCode))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Pack getSemiPublicPackByCode(String packCode) {
        for (Pack pack : Data.PACKS) {
            if (pack.isSemiPublic()) {
                if (Hashing.toHashCode(pack.getCode()).equals(Hashing.md5(packCode))) {
                    return pack;
                }
            }
        }

        return null;
    }

    public static boolean addPack(String packCode) {
        for (Pack pack : Data.PACKS) {
            if (pack.isSemiPublic() && !canViewSemiPublicPackByCode(Hashing.md5(packCode).toString())) {
                if (Hashing.toHashCode(pack.getCode()).equals(Hashing.md5(packCode))) {
                    if (pack.isTester()) {
                        return false;
                    }
                    App.settings.addedPacks.add(packCode);
                    App.settings.save();
                    App.launcher.refreshPacksBrowserPanel();
                    return true;
                }
            }
        }
        return false;
    }

    public static void removePack(String packCode) {
        for (String code : App.settings.addedPacks) {
            if (Hashing.md5(code).equals(Hashing.toHashCode(packCode))) {
                App.settings.addedPacks.remove(packCode);
                App.settings.save();
                App.launcher.refreshPacksBrowserPanel();
            }
        }
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    public static void loadUsers() {
        PerformanceManager.start();
        LOG.debug("Loading users");
        List<PackUsers> packUsers = new ArrayList<>();

        try {
            java.lang.reflect.Type type = new TypeToken<List<PackUsers>>() {
            }.getType();
            packUsers.addAll(
                Gsons.DEFAULT_ALT.fromJson(new FileReader(FileSystem.JSON.resolve("users.json").toFile()), type));
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LOG.error("error: ", e);
        }

        for (PackUsers pu : packUsers) {
            pu.addUsers();
        }

        LOG.debug("Finished loading users");
        PerformanceManager.end();
    }

    public static void removeUnusedImages() {
        PerformanceManager.start();
        File[] files = FileSystem.IMAGES.toFile().listFiles();

        Set<String> packImageFilenames = Data.PACKS.stream().map(p -> p.getSafeName().toLowerCase() + ".png")
            .collect(Collectors.toSet());
        packImageFilenames.add("defaultimage.png");

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".png") && !packImageFilenames.contains(file.getName())) {
                    LOG.info("Pack image no longer used, deleting file " + file.getName());
                    file.delete();
                }
            }
        }

        PerformanceManager.end();
    }

    public static boolean canViewSemiPublicPackByCode(String packCode) {
        for (String code : App.settings.addedPacks) {
            if (Hashing.md5(code).equals(Hashing.toHashCode(packCode))) {
                return true;
            }
        }
        return false;
    }
}
