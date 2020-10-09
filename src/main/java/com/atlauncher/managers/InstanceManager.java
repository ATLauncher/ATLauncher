/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class InstanceManager {
    public static List<Instance> getOldInstances() {
        return Data.INSTANCES_OLD;
    }

    public static List<InstanceV2> getInstances() {
        return Data.INSTANCES;
    }

    /**
     * Get the Instances available in the Launcher sorted alphabetically
     *
     * @return The Instances available in the Launcher sorted alphabetically
     */
    public static ArrayList<Instance> getInstancesSorted() {
        ArrayList<Instance> instances = new ArrayList<>(Data.INSTANCES_OLD);
        instances.sort(Comparator.comparing(Instance::getName));
        return instances;
    }

    public static ArrayList<InstanceV2> getInstancesV2Sorted() {
        ArrayList<InstanceV2> instances = new ArrayList<>(Data.INSTANCES);
        instances.sort(Comparator.comparing(i -> i.launcher.name));
        return instances;
    }

    /**
     * Loads the user installed Instances
     */
    public static void loadInstances() {
        PerformanceManager.start();
        LogManager.debug("Loading instances");
        Data.INSTANCES.clear();
        Data.INSTANCES_OLD.clear();

        for (String folder : Optional.of(FileSystem.INSTANCES.toFile().list(Utils.getInstanceFileFilter()))
                .orElse(new String[0])) {
            File instanceDir = FileSystem.INSTANCES.resolve(folder).toFile();

            Instance instance = null;
            InstanceV2 instanceV2 = null;

            try {
                try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                    instanceV2 = Gsons.MINECRAFT.fromJson(fileReader, InstanceV2.class);
                    instanceV2.ROOT = instanceDir.toPath();
                    LogManager.debug("Loaded V2 instance from " + instanceDir);

                    if (instanceV2.launcher == null) {
                        instanceV2 = null;
                        throw new JsonSyntaxException("Error parsing instance.json as InstanceV2");
                    }
                } catch (JsonIOException | JsonSyntaxException ignored) {
                    try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                        instance = Gsons.DEFAULT.fromJson(fileReader, Instance.class);
                        instance.ROOT = instanceDir.toPath();
                        instance.convert();
                        LogManager.debug("Loaded V1 instance from " + instanceDir);
                    } catch (JsonIOException | JsonSyntaxException e) {
                        LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e);
                        continue;
                    }
                }
            } catch (Exception e2) {
                LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e2);
                continue;
            }

            if (instance == null && instanceV2 == null) {
                LogManager.error("Failed to load instance in the folder " + instanceDir);
                continue;
            }

            if (instance != null) {
                if (!instance.getDisabledModsDirectory().exists()) {
                    instance.getDisabledModsDirectory().mkdir();
                }

                if (PackManager.isPackByName(instance.getPackName())) {
                    instance.setRealPack(PackManager.getPackByName(instance.getPackName()));
                }

                Data.INSTANCES_OLD.add(instance);
            }

            if (instanceV2 != null) {
                Data.INSTANCES.add(instanceV2);
            }
        }

        LogManager.debug("Finished loading instances");
        PerformanceManager.end();
    }

    public static void saveInstances() {
        for (Instance instance : Data.INSTANCES_OLD) {
            File instanceFile = new File(instance.getRootDirectory(), "instance.json");
            FileWriter fw = null;
            BufferedWriter bw = null;
            try {
                if (!instanceFile.exists()) {
                    instanceFile.createNewFile();
                }

                fw = new FileWriter(instanceFile);
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
                    LogManager.logStackTrace(
                            "Exception while trying to close FileWriter/BufferedWriter for saving instances "
                                    + "json file.",
                            e);
                }
            }
        }
    }

    public static void setInstanceVisbility(Instance instance, boolean collapsed) {
        if (instance != null && AccountManager.getSelectedAccount().isReal()) {
            if (collapsed) {
                // Closed It
                if (!AccountManager.getSelectedAccount().getCollapsedInstances().contains(instance.getName())) {
                    AccountManager.getSelectedAccount().getCollapsedInstances().add(instance.getName());
                }
            } else {
                // Opened It
                if (AccountManager.getSelectedAccount().getCollapsedInstances().contains(instance.getName())) {
                    AccountManager.getSelectedAccount().getCollapsedInstances().remove(instance.getName());
                }
            }
            AccountManager.saveAccounts();
            App.launcher.reloadInstancesPanel();
        }
    }

    public static void setInstanceVisbility(InstanceV2 instanceV2, boolean collapsed) {
        if (instanceV2 != null && AccountManager.getSelectedAccount().isReal()) {
            if (collapsed) {
                // Closed It
                if (!AccountManager.getSelectedAccount().getCollapsedInstances().contains(instanceV2.launcher.name)) {
                    AccountManager.getSelectedAccount().getCollapsedInstances().add(instanceV2.launcher.name);
                }
            } else {
                // Opened It
                if (AccountManager.getSelectedAccount().getCollapsedInstances().contains(instanceV2.launcher.name)) {
                    AccountManager.getSelectedAccount().getCollapsedInstances().remove(instanceV2.launcher.name);
                }
            }
            AccountManager.saveAccounts();
            App.launcher.reloadInstancesPanel();
        }
    }

    public static void setInstanceUnplayable(Instance instance) {
        instance.setUnplayable();
        saveInstances();
        App.launcher.reloadInstancesPanel();
    }

    /**
     * Removes an instance from the Launcher
     *
     * @param instance The Instance to remove from the launcher.
     */
    public static void removeInstance(Instance instance) {
        if (Data.INSTANCES_OLD.remove(instance)) {
            Utils.delete(instance.getRootDirectory());
            saveInstances();
            App.launcher.reloadInstancesPanel();
        }
    }

    public static void removeInstance(InstanceV2 instance) {
        if (Data.INSTANCES.remove(instance)) {
            FileUtils.deleteDirectory(instance.getRoot());
            App.launcher.reloadInstancesPanel();
        }
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     *
     * @param name The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public static boolean isInstance(String name) {
        for (Instance instance : Data.INSTANCES_OLD) {
            if (instance.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""))) {
                return true;
            }
        }
        return Data.INSTANCES.stream()
                .anyMatch(i -> i.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceByName(String name) {
        for (Instance instance : Data.INSTANCES_OLD) {
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
        for (Instance instance : Data.INSTANCES_OLD) {
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
        for (Instance instance : Data.INSTANCES_OLD) {
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
        for (Instance instance : Data.INSTANCES_OLD) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    public static void cloneInstance(InstanceV2 instance, String clonedName) {
        InstanceV2 clonedInstance = Gsons.MINECRAFT.fromJson(Gsons.MINECRAFT.toJson(instance), InstanceV2.class);

        if (clonedInstance == null) {
            LogManager.error("Error Occurred While Cloning Instance! Instance Object Couldn't Be Cloned!");
        } else {
            clonedInstance.launcher.name = clonedName;
            clonedInstance.ROOT = FileSystem.INSTANCES.resolve(clonedInstance.getSafeName());
            FileUtils.createDirectory(clonedInstance.getRoot());
            Utils.copyDirectory(instance.getRoot().toFile(), clonedInstance.getRoot().toFile());
            clonedInstance.save();
            Data.INSTANCES.add(clonedInstance);
            App.launcher.reloadInstancesPanel();
        }
    }
}
