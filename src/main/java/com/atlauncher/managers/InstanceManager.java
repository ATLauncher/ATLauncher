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
import com.atlauncher.FileSystemData;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.manager.InstanceChangeManager;
import com.atlauncher.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InstanceManager {
    public static List<Instance> getInstances() {
        return Data.INSTANCES;
    }

    /**
     * Get the Instances available in the Launcher sorted alphabetically
     *
     * @return The Instances available in the Launcher sorted alphabetically
     */
    public static List<Instance> getInstancesSorted() {
        ArrayList<Instance> instances = new ArrayList<>(Data.INSTANCES);
        Collections.sort(instances, new Comparator<Instance>() {
            public int compare(Instance result1, Instance result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });
        return instances;
    }

    /**
     * Loads the user installed Instances
     */
    public static void loadInstances() {
        LogManager.debug("Loading instances");
        try {
            Data.INSTANCES.clear();
            if (Files.exists(FileSystemData.INSTANCES_DATA)) {
                try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(FileSystemData.INSTANCES_DATA
                        .toFile()))) {
                    Object obj;
                    while ((obj = oin.readObject()) != null) {
                        Instance instance = (Instance) obj;
                        Path dir = FileSystem.INSTANCES.resolve(instance.getSafeName());
                        if (!Files.exists(dir)) {
                            continue;
                        }

                        if (!instance.hasBeenConverted()) {
                            LogManager.warn("Instance " + instance.getName() + " is being converted, this is normal " +
                                    "and should only appear once");
                            instance.convert();
                        }

                        if (!Files.exists(instance.root.resolve("disabledmods"))) {
                            FileUtils.createDirectory(instance.root.resolve("disabledmods"));
                        }

                        Data.INSTANCES.add(instance);
                        if (PackManager.isPackByName(instance.getPackName())) {
                            instance.setRealPack(PackManager.getPackByName(instance.getPackName()));
                        }
                    }
                } catch (EOFException ex) {
                    // Fallthrough
                }

                InstanceManager.saveInstances();
                Files.delete(FileSystemData.INSTANCES_DATA);
            } else {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.INSTANCES)) {
                    for (Path file : stream) {
                        Path instanceJson = file.resolve("instance.json");

                        if (!Files.exists(instanceJson)) {
                            LogManager.error("Failed to load instance in folder " + file.getFileName() + " due to " +
                                    "missing instance.json!");
                            continue;
                        }

                        byte[] bits = Files.readAllBytes(instanceJson);
                        Instance instance;
                        try {
                            instance = Gsons.DEFAULT.fromJson(new String(bits), Instance.class);
                        } catch (Exception e) {
                            LogManager.logStackTrace("Failed to load instance in the folder " + file.getFileName(), e);
                            continue;
                        }

                        if (instance == null) {
                            LogManager.error("Failed to load instance in folder " + file.getFileName());
                            continue;
                        }

                        if (!Files.exists(instance.getRootDirectory().resolve("disabledmods"))) {
                            FileUtils.createDirectory(instance.getRootDirectory().resolve("disabledmods"));
                        }

                        if (PackManager.isPackByName(instance.getPackName())) {
                            instance.setRealPack(PackManager.getPackByName(instance.getPackName()));
                        }

                        Data.INSTANCES.add(instance);
                    }
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading instances");
    }

    public static void saveInstances() {
        for (Instance instance : Data.INSTANCES) {
            Path instanceFile = instance.getRootDirectory().resolve("instance.json");
            FileWriter fw = null;
            BufferedWriter bw = null;
            try {
                if (!Files.exists(instanceFile)) {
                    Files.createFile(instanceFile);
                }

                fw = new FileWriter(instanceFile.toFile());
                bw = new BufferedWriter(fw);
                bw.write(Gsons.DEFAULT.toJson(instance));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace("Exception while trying to close FileWriter/BufferedWriter for saving " +
                            "an" + " instances json file.", e);
                }
            }
        }
    }

    public static void changeUserLocks() {
        LogManager.debug("Changing instances user locks to UUID's");

        boolean wereChanges = false;

        for (Instance instance : Data.INSTANCES) {
            if (instance.getInstalledBy() != null) {
                boolean found = false;

                for (Account account : AccountManager.getAccounts()) {
                    // This is the user who installed this so switch to their UUID
                    if (account.getMinecraftUsername().equalsIgnoreCase(instance.getInstalledBy())) {
                        found = true;
                        wereChanges = true;

                        instance.removeInstalledBy();

                        // If the accounts UUID is null for whatever reason, don't set the lock
                        if (!account.isUUIDNull()) {
                            instance.setUserLock(account.getUUIDNoDashes());
                        }
                        break;
                    }
                }

                // If there were no accounts with that username, we remove the lock and old installed by
                if (!found) {
                    wereChanges = true;

                    instance.removeInstalledBy();
                    instance.removeUserLock();
                }
            }
        }

        if (wereChanges) {
            AccountManager.saveAccounts();
            InstanceManager.saveInstances();
        }

        LogManager.debug("Finished changing instances user locks to UUID's");
    }

    public static void addInstance(Instance instance) {
        Data.INSTANCES.add(instance);
    }

    public static void setInstanceUnplayable(Instance instance) {
        instance.setUnplayable();
        InstanceManager.saveInstances();
        InstanceChangeManager.change();
    }

    /**
     * Removes an instance from the Launcher
     *
     * @param instance The Instance to remove from the launcher.
     */
    public static void removeInstance(Instance instance) {
        if (Data.INSTANCES.remove(instance)) {
            FileUtils.delete(instance.getRootDirectory());
            InstanceManager.saveInstances();
            InstanceChangeManager.change();
        }
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     *
     * @param name The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public static boolean isInstance(String name) {
        for (Instance instance : Data.INSTANCES) {
            if (instance.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceByName(String name) {
        for (Instance instance : Data.INSTANCES) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceBySafeName(String name) {
        for (Instance instance : Data.INSTANCES) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceByName(String name) {
        for (Instance instance : Data.INSTANCES) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceBySafeName(String name) {
        for (Instance instance : Data.INSTANCES) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    public static void cloneInstance(Instance instance, String clonedName) {
        Instance clonedInstance = (Instance) instance.clone();
        if (clonedInstance == null) {
            LogManager.error("Error occurred while cloning instance! Instance object couldn't be cloned!");
        } else {
            clonedInstance.setName(clonedName);

            FileUtils.createDirectory(clonedInstance.getRootDirectory());
            FileUtils.copyDirectory(instance.getRootDirectory(), clonedInstance.getRootDirectory());
            Data.INSTANCES.add(clonedInstance);
            InstanceManager.saveInstances();
            InstanceChangeManager.change();
        }
    }
}
