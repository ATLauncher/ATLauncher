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
package com.atlauncher.data.minecraft.loaders.forge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

@Json
public class Processor {
    private String jar;
    private List<String> classpath;
    private List<String> args;
    private Map<String, String> outputs;

    public String getJar() {
        return this.jar;
    }

    public List<String> getClasspath() {
        return this.classpath;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public Map<String, String> getOutputs() {
        return this.outputs;
    }

    public boolean hasOutputs() {
        return this.outputs != null && this.outputs.size() != 0;
    }

    public void process(ForgeInstallProfile installProfile, File extractedDir, InstanceInstaller instanceInstaller)
            throws IOException {
        // delete any outputs that are invalid. They still need to run
        if (!this.needToRun(installProfile, extractedDir, instanceInstaller)) {
            LogManager.debug("No need to run processor " + this.jar + " since outputs all match hashes");
            return;
        }

        File librariesDirectory = instanceInstaller.isServer ? instanceInstaller.root.resolve("libraries").toFile()
                : FileSystem.LIBRARIES.toFile();

        File jarPath = Utils.convertMavenIdentifierToFile(this.jar, librariesDirectory);
        LogManager.debug("Jar path is " + jarPath);
        if (!jarPath.exists() || !jarPath.isFile()) {
            LogManager.error("Failed to process processor with jar " + this.jar + " as the jar doesn't exist");
            instanceInstaller.cancel(true);
            return;
        }

        JarFile jarFile = new JarFile(jarPath);
        String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        jarFile.close();
        LogManager.debug("Found mainclass of " + mainClass);

        if (mainClass == null || mainClass.isEmpty()) {
            LogManager.error("Failed to process processor with jar " + this.jar + " as the mainclass wasn't found");
            instanceInstaller.cancel(true);
            return;
        }

        List<URL> classpath = new ArrayList<>();
        classpath.add(jarPath.toURI().toURL());

        for (String classpathItem : this.getClasspath()) {
            LogManager.debug("Adding classpath " + classpathItem);
            File classpathFile = Utils.convertMavenIdentifierToFile(classpathItem, FileSystem.LIBRARIES.toFile());

            if (!classpathFile.exists() || !classpathFile.isFile()) {
                LogManager.error("Failed to process processor with jar " + this.jar
                        + " as the classpath item with file " + classpathFile.getAbsolutePath() + " doesn't exist");
                instanceInstaller.cancel(true);
                return;
            }

            classpath.add(classpathFile.toURI().toURL());
        }

        List<String> args = new ArrayList<>();

        for (String arg : this.getArgs()) {
            LogManager.debug("Processing argument " + arg);
            char start = arg.charAt(0);
            char end = arg.charAt(arg.length() - 1);

            if (start == '{' && end == '}') {
                String key = arg.substring(1, arg.length() - 1);
                LogManager.debug("Getting data with key of " + key);
                String value = installProfile.data.get(key).getValue(!instanceInstaller.isServer, librariesDirectory);

                if (value == null || value.isEmpty()) {
                    LogManager.error("Failed to process processor with jar " + this.jar + " as the argument with name "
                            + arg + " as the data item with key " + key + " was empty or null");
                    instanceInstaller.cancel(true);
                    return;
                }

                LogManager.debug("Got value of " + value);
                // checking for local file paths returned "/data/client.lzma" and then makes
                // sure we localise it to the libraries folder if it's indeed local
                if (value.charAt(0) == '/') {
                    if (value.toLowerCase()
                            .contains(instanceInstaller.root.resolve("libraries").toFile().toString().toLowerCase())
                            || value.toLowerCase().contains(FileSystem.LIBRARIES.toString().toLowerCase())) {
                        args.add(value);
                    } else {
                        File localFile = new File(extractedDir, value);
                        LogManager.debug("Got argument with local file of " + localFile.getAbsolutePath());

                        if (!localFile.exists() || !localFile.isFile()) {
                            LogManager.error("Failed to process argument with value of " + value + " as the local file "
                                    + localFile.getAbsolutePath() + " doesn't exist");
                            instanceInstaller.cancel(true);
                            return;
                        }

                        args.add(localFile.getAbsolutePath());
                    }
                } else {
                    args.add(value);
                }
            } else if (start == '[' && end == ']') {
                String artifact = arg.substring(1, arg.length() - 1);
                File artifactFile = Utils.convertMavenIdentifierToFile(artifact, FileSystem.LIBRARIES.toFile());
                LogManager.debug("Got argument with file of " + artifactFile.getAbsolutePath());

                if (!artifactFile.exists() || !artifactFile.isFile()) {
                    LogManager.error("Failed to process argument with value of " + arg + " as the file "
                            + artifactFile.getAbsolutePath() + " doesn't exist");
                    instanceInstaller.cancel(true);
                    return;
                }

                args.add(artifactFile.getAbsolutePath());
            } else {
                args.add(arg);
            }
        }

        // we pass in some extra params for the forge installer tools DEOBF_REALMS task
        if (this.args.contains("DEOBF_REALMS")) {
            args.add("--json");
            args.add(instanceInstaller.temp.resolve("minecraft.json").toAbsolutePath().toString());
            args.add("--libs");
            args.add(FileSystem.LIBRARIES.toFile().getAbsolutePath());
        }

            ClassLoader cl = new URLClassLoader(classpath.toArray(new URL[0]),
                Processor.class.getClassLoader());
        try {
            LogManager.debug("Running processor");
            Class<?> cls = Class.forName(mainClass, true, cl);
            Method main = cls.getDeclaredMethod("main", String[].class);
            main.invoke(null, (Object) args.toArray(new String[0]));
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
            LogManager.error(
                    "Failed to process processor with jar " + this.jar + " as there was an error invoking the jar");
            instanceInstaller.cancel(true);
        }
    }

    public boolean needToRun(ForgeInstallProfile installProfile, File extractedDir,
            InstanceInstaller instanceInstaller) {
        if (!this.hasOutputs()) {
            return true;
        }

        File librariesDirectory = instanceInstaller.isServer ? instanceInstaller.root.resolve("libraries").toFile()
                : FileSystem.LIBRARIES.toFile();

        for (Entry<String, String> entry : this.outputs.entrySet()) {
            String key = entry.getKey();
            LogManager.debug("Processing output for " + key);

            char start = key.charAt(0);
            char end = key.charAt(key.length() - 1);

            if (start == '{' && end == '}') {
                LogManager.debug("Getting data with key of " + key.substring(1, key.length() - 1));
                String dataItem = installProfile.data.get(key.substring(1, key.length() - 1))
                        .getValue(!instanceInstaller.isServer, librariesDirectory);
                if (dataItem == null || dataItem.isEmpty()) {
                    LogManager.error("Failed to process processor with jar " + this.jar + " as the output with key "
                            + key + " doesn't have a corresponding data entry");
                    instanceInstaller.cancel(true);
                    return true;
                }

                String value = entry.getValue();
                File outputFile = new File(dataItem);

                if (!outputFile.exists() || !outputFile.isFile()) {
                    return true;
                }

                char valueStart = value.charAt(0);
                char valueEnd = value.charAt(value.length() - 1);

                if (valueStart == '{' && valueEnd == '}') {
                    LogManager.debug("Getting data with key of " + value.substring(1, value.length() - 1));
                    String valueDataItem = installProfile.data.get(value.substring(1, value.length() - 1))
                            .getValue(!instanceInstaller.isServer, librariesDirectory);
                    if (dataItem == null || dataItem.isEmpty()) {
                        LogManager.error("Failed to process processor with jar " + this.jar
                                + " as the output with value " + value + " doesn't have a corresponding data entry");
                        instanceInstaller.cancel(true);
                        return true;
                    }

                    String sha1Hash = Hashing.sha1(outputFile.toPath()).toString();
                    String expectedHash = valueDataItem.charAt(0) == '\''
                            ? valueDataItem.substring(1, valueDataItem.length() - 1)
                            : valueDataItem;

                    LogManager.debug("Expecting " + sha1Hash + " to equal " + sha1Hash);
                    if (!sha1Hash.equals(expectedHash)) {
                        Utils.delete(outputFile);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
