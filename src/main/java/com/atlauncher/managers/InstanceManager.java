/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV1;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class InstanceManager {
    public static List<Instance> getInstances() {
        return Data.INSTANCES;
    }

    public static ArrayList<Instance> getInstancesSorted() {
        ArrayList<Instance> instances = new ArrayList<>(Data.INSTANCES);
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

        for (String folder : Optional.of(FileSystem.INSTANCES.toFile().list(Utils.getInstanceFileFilter()))
                .orElse(new String[0])) {
            File instanceDir = FileSystem.INSTANCES.resolve(folder).toFile();

            Instance instance = null;
            boolean converted = false;

            try {
                try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                    instance = Gsons.MINECRAFT.fromJson(fileReader, Instance.class);
                    instance.ROOT = instanceDir.toPath();
                    LogManager.debug("Loaded instance from " + instanceDir);

                    if (instance.launcher == null) {
                        instance = null;
                        throw new JsonSyntaxException("Error parsing instance.json as Instance");
                    }
                } catch (JsonIOException | JsonSyntaxException ignored) {
                    try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                        InstanceV1 instanceV1 = Gsons.DEFAULT.fromJson(fileReader, InstanceV1.class);
                        instanceV1.ROOT = instanceDir.toPath();
                        instanceV1.convert();

                        instance = instanceV1.convertToNewFormat(instanceDir.toPath());
                        Utils.copyFile(new File(instanceDir, "instance.json"),
                                new File(instanceDir, "instance-v1-backup.json"), true);
                        converted = true;
                        LogManager.debug("Converted V1 instance from " + instanceDir);
                    } catch (JsonIOException | JsonSyntaxException e) {
                        converted = false;
                        LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e);
                        continue;
                    }
                }

                if (instance == null) {
                    LogManager.error("Failed to load instance in the folder " + instanceDir);
                    continue;
                } else {
                    if (converted) {
                        instance.save();
                    }

                    if (instance.launcher.curseForgeManifest != null
                            && instance.launcher.curseForgeManifest.projectID != null
                            && instance.launcher.curseForgeManifest.fileID != null) {
                        LogManager.info(String.format("Converting instance \"%s\" CurseForge information",
                                instance.launcher.name));
                        instance.launcher.curseForgeProject = CurseForgeApi
                                .getProjectById(instance.launcher.curseForgeManifest.projectID);
                        instance.launcher.curseForgeFile = CurseForgeApi.getFileForProject(
                                instance.launcher.curseForgeManifest.projectID,
                                instance.launcher.curseForgeManifest.fileID);
                        instance.launcher.curseForgeManifest = null;

                        instance.save();
                    }

                    Data.INSTANCES.add(instance);
                }
            } catch (Exception e2) {
                LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e2);
                continue;
            }
        }

        LogManager.debug("Finished loading instances");
        PerformanceManager.end();
    }

    public static void setInstanceVisbility(Instance instance, boolean collapsed) {
        if (collapsed) {
            // Closed It
            if (!AccountManager.getSelectedAccount().collapsedInstances.contains(instance.launcher.name)) {
                AccountManager.getSelectedAccount().collapsedInstances.add(instance.launcher.name);
            }
        } else {
            // Opened It
            AccountManager.getSelectedAccount().collapsedInstances.remove(instance.launcher.name);
        }
        AccountManager.saveAccounts();
        App.launcher.reloadInstancesPanel();
    }

    public static void removeInstance(Instance instance) {
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
        return Data.INSTANCES.stream().anyMatch(i -> i.launcher.name.equalsIgnoreCase(name));
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceBySafeName(String name) {
        return Data.INSTANCES.stream().anyMatch(i -> i.getSafeName().equalsIgnoreCase(name));
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceByName(String name) {
        return Data.INSTANCES.stream().filter(i -> i.launcher.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceBySafeName(String name) {
        return Data.INSTANCES.stream().filter(i -> i.getSafeName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void cloneInstance(Instance instance, String clonedName) {
        Instance clonedInstance = Gsons.MINECRAFT.fromJson(Gsons.MINECRAFT.toJson(instance), Instance.class);

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
