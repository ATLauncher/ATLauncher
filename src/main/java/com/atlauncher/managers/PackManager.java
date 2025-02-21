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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackUsers;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.utils.Hashing;
import com.google.common.hash.HashCode;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class PackManager {
    public static List<Pack> getPacks() {
        return Data.PACKS;
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    public static void loadPacks() {
        PerformanceManager.start();
        LogManager.debug("Loading packs");
        Data.PACKS.clear();
        try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(FileSystem.JSON.resolve("packsnew.json")),
                StandardCharsets.UTF_8)) {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {}.getType();
            List<Pack> packs = Gsons.DEFAULT.fromJson(fileReader, type);
            if (packs != null) {
                Data.PACKS.addAll(packs);
            }
        } catch (JsonSyntaxException | IOException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading packs");
        PerformanceManager.end();
    }

    /**
     * Get the Packs available in the Launcher sorted alphabetically
     *
     * @return The Packs available in the Launcher sorted alphabetically
     */
    public static List<Pack> getPacksSortedAlphabetically(boolean isFeatured, boolean sortDescending) {
        List<Pack> packs = new ArrayList<>();

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

        packs.sort(Comparator.comparing(p -> p.getName().toLowerCase(Locale.ENGLISH)));

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
        List<Pack> packs = new ArrayList<>();

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
        MicrosoftAccount selectedAccount = AccountManager.getSelectedAccount();

        if (pack != null && selectedAccount != null) {
            if (collapsed) {
                // Closed It
                if (!selectedAccount.collapsedPacks.contains(pack.getName())) {
                    selectedAccount.collapsedPacks.add(pack.getName());
                }
            } else {
                // Opened It
                selectedAccount.collapsedPacks.remove(pack.getName());
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
        HashCode hashToRemove = Hashing.toHashCode(packCode);
        boolean removed = App.settings.addedPacks.removeIf(code -> Hashing.md5(code).equals(hashToRemove));

        if (removed) {
            App.settings.save();
            App.launcher.refreshPacksBrowserPanel();
        }
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    public static void loadUsers() {
        PerformanceManager.start();
        LogManager.debug("Loading users");
        List<PackUsers> packUsers = new ArrayList<>();

        try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(FileSystem.JSON.resolve("users.json")),
                StandardCharsets.UTF_8)) {
            java.lang.reflect.Type type = new TypeToken<List<PackUsers>>() {}.getType();
            List<PackUsers> users = Gsons.DEFAULT.fromJson(fileReader, type);
            if (users != null) {
                packUsers.addAll(users);
            }
        } catch (JsonSyntaxException | IOException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }

        for (PackUsers pu : packUsers) {
            pu.addUsers();
        }

        LogManager.debug("Finished loading users");
        PerformanceManager.end();
    }

    public static void removeUnusedImages() {
        PerformanceManager.start();
        File[] files = FileSystem.IMAGES.toFile().listFiles();

        Set<String> packImageFilenames = Data.PACKS.stream()
                .map(p -> p.getSafeName().toLowerCase(Locale.ENGLISH) + ".png")
                .collect(Collectors.toSet());
        packImageFilenames.add("defaultimage.png");

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".png") && !packImageFilenames.contains(file.getName())) {
                    LogManager.info("Pack image no longer used, deleting file " + file.getName());
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
