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

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class InstanceManager {
    /**
     * Data holder for Instances.
     * <p>
     * Automatically updates subscribed entities downstream.
     */
    private static final BehaviorSubject<List<Instance>> INSTANCES = BehaviorSubject.createDefault(new LinkedList<>());

    /**
     * @return Observable list of instances.
     */
    public static Observable<List<Instance>> getInstancesObservable() {
        return INSTANCES;
    }

    /**
     * Non-reactive function for legacy operations.
     *
     * @return List of instances.
     */
    public static List<Instance> getInstances() {
        return INSTANCES.getValue();
    }

    /**
     * Loads the user installed Instances
     */
    public static void loadInstances() {
        PerformanceManager.start();
        LogManager.debug("Loading instances");
        List<Instance> newInstances = new LinkedList<>();

        for (String folder : Optional.of(FileSystem.INSTANCES.toFile().list(Utils.getInstanceFileFilter()))
                .orElse(new String[0])) {
            Path instanceDir = FileSystem.INSTANCES.resolve(folder);

            Instance instance = null;

            try {
                try (InputStreamReader fileReader = new InputStreamReader(
                    Files.newInputStream(instanceDir.resolve("instance.json")), StandardCharsets.UTF_8)) {
                    instance = Gsons.DEFAULT.fromJson(fileReader, Instance.class);
                    instance.ROOT = instanceDir;
                    LogManager.debug("Loaded instance from " + instanceDir);

                    if (instance.launcher == null) {
                        instance = null;
                        throw new JsonSyntaxException("Error parsing instance.json as Instance");
                    }
                } catch (JsonIOException | JsonSyntaxException e) {
                    LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e);
                    continue;
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

                if (instance.launcher.numPlays == null) {
                    LogManager.info(String.format("Converting instance \"%s\" numPlays/lastPlayed",
                            instance.launcher.name));
                    instance.launcher.numPlays = instance.numPlays;
                    instance.launcher.lastPlayed = instance.lastPlayed;

                    instance.save();
                }

                if (instance.launcher.account != null
                        && !AccountManager.isAccountByName(instance.launcher.account)) {
                    LogManager.warn(
                            String.format("No account with name of %s, so setting instance account back to default",
                                    instance.launcher.account));
                    instance.launcher.account = null;
                    instance.save();
                }

                newInstances.add(instance);
            } catch (Exception e2) {
                LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e2);
                continue;
            }
        }

        List<Map<String, String>> movedPacks = ConfigManager.getConfigItem("movedPacks", new ArrayList<>());

        newInstances.forEach(instance -> {
            // convert all old system instances into just a Vanilla instance
            if (instance.getPack() != null && instance.getPack().system) {
                instance.launcher.vanillaInstance = true;
                instance.launcher.packId = 0;
                instance.launcher.pack = "Minecraft";

                instance.save();
            }

            // convert packs marked as moved by ATLauncher to their new pack id & version
            try {
                if (instance.getPack() != null) {
                    Optional<Map<String, String>> packMove = movedPacks.stream()
                            .filter(mp -> Integer.parseInt(mp.get("fromPack")) == instance.launcher.packId).findFirst();

                    if (packMove.isPresent()) {
                        if (packMove.get().get("fromVersion").equals(instance.launcher.version)) {
                            Pack newPack = PackManager.getPackByID(Integer.parseInt(packMove.get().get("toPack")));

                            LogManager.info(String.format("Converting instance %s from pack %s to %s",
                                    instance.launcher.name, instance.launcher.pack, newPack.name));

                            instance.launcher.packId = newPack.id;
                            instance.launcher.pack = newPack.name;
                            instance.launcher.description = newPack.description;
                            instance.launcher.version = packMove.get().get("toVersion");

                            instance.save();
                        }
                    }
                }
            } catch (NumberFormatException | InvalidPack e) {
                LogManager.logStackTrace("Error converting moved pack", e);
            }
        });

        INSTANCES.onNext(newInstances);
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
    }

    /**
     * Removes an instance and deletes its directory.
     *
     * @param instance Instance to remove
     */
    public static void removeInstance(Instance instance) {
        List<Instance> instances = INSTANCES.getValue();
        if (instances.remove(instance)) {
            FileUtils.delete(instance.getRoot(), true);
            INSTANCES.onNext(instances);
        }
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     *
     * @param name The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public static boolean isInstance(String name) {
        return INSTANCES.getValue().stream()
                .anyMatch(i -> i.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceByName(String name) {
        return INSTANCES.getValue().stream().anyMatch(i -> i.launcher.name.equalsIgnoreCase(name));
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
     * @return True if the instance is found from the name
     */
    public static boolean isInstanceBySafeName(String name) {
        return INSTANCES.getValue().stream().anyMatch(i -> i.getSafeName().equalsIgnoreCase(name));
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceByName(String name) {
        return INSTANCES.getValue().stream().filter(i -> i.launcher.name.equalsIgnoreCase(name)).findFirst()
                .orElse(null);
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public static Instance getInstanceBySafeName(String name) {
        return INSTANCES.getValue().stream().filter(i -> i.getSafeName().equalsIgnoreCase(name)).findFirst()
                .orElse(null);
    }

    public static void cloneInstance(Instance instance, String clonedName) {
        Instance clonedInstance = Gsons.DEFAULT.fromJson(Gsons.DEFAULT.toJson(instance), Instance.class);

        if (clonedInstance == null) {
            LogManager.error("Error Occurred While Cloning Instance! Instance Object Couldn't Be Cloned!");
        } else {
            clonedInstance.launcher.name = clonedName;
            clonedInstance.ROOT = FileSystem.INSTANCES.resolve(clonedInstance.getSafeName());
            clonedInstance.uuid = UUID.randomUUID();
            FileUtils.createDirectory(clonedInstance.getRoot());
            Utils.copyDirectory(instance.getRoot().toFile(), clonedInstance.getRoot().toFile());
            clonedInstance.save();
            List<Instance> instances = INSTANCES.getValue();
            instances.add(clonedInstance);
            INSTANCES.onNext(instances);
        }
    }

    public static void addInstance(Instance instance) {
        List<Instance> instances = INSTANCES.getValue();
        instances.add(instance);
        INSTANCES.onNext(instances);
    }

    /**
     * Update the Instance with new data
     *
     * @param instance Instance to update
     */
    public static void updateInstance(Instance instance) {
        List<Instance> instances = INSTANCES.getValue();
        instances.removeIf(it -> it.getUUID().equals(instance.getUUID()));
        instances.add(instance);
        INSTANCES.onNext(instances);
    }
}
