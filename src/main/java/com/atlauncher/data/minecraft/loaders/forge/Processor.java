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
package com.atlauncher.data.minecraft.loaders.forge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

@Json
public class Processor {
    private static final Logger LOG = LogManager.getLogger(Processor.class);

    public String jar;
    public List<String> sides;
    public List<String> classpath;
    public List<String> args;
    public Map<String, String> outputs;

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
            return;
        }

        File librariesDirectory = instanceInstaller.isServer ? instanceInstaller.root.resolve("libraries").toFile()
                : FileSystem.LIBRARIES.toFile();

        File jarPath = Utils.convertMavenIdentifierToFile(this.jar, librariesDirectory);
        LOG.debug("Jar path is " + jarPath);
        if (!jarPath.exists() || !jarPath.isFile()) {
            LOG.error("Failed to process processor with jar " + this.jar + " as the jar doesn't exist");
            instanceInstaller.cancel(true);
            return;
        }

        JarFile jarFile = new JarFile(jarPath);
        String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        jarFile.close();
        LOG.debug("Found mainclass of " + mainClass);

        if (mainClass == null || mainClass.isEmpty()) {
            LOG.error("Failed to process processor with jar " + this.jar + " as the mainclass wasn't found");
            instanceInstaller.cancel(true);
            return;
        }

        List<URL> classpath = new ArrayList<>();
        classpath.add(jarPath.toURI().toURL());

        for (String classpathItem : this.getClasspath()) {
            LOG.debug("Adding classpath " + classpathItem);
            File classpathFile = Utils.convertMavenIdentifierToFile(classpathItem, FileSystem.LIBRARIES.toFile());

            if (!classpathFile.exists() || !classpathFile.isFile()) {
                LOG.error("Failed to process processor with jar " + this.jar
                        + " as the classpath item with file " + classpathFile.getAbsolutePath() + " doesn't exist");
                instanceInstaller.cancel(true);
                return;
            }

            classpath.add(classpathFile.toURI().toURL());
        }

        List<String> args = new ArrayList<>();

        for (String arg : this.getArgs()) {
            if (arg.contains("{ROOT}")) {
                arg = arg.replace("{ROOT}",
                        installProfile.data.get("ROOT").getValue(!instanceInstaller.isServer, librariesDirectory));
            }

            LOG.debug("Processing argument " + arg);
            char start = arg.charAt(0);
            char end = arg.charAt(arg.length() - 1);

            if (start == '{' && end == '}') {
                String key = arg.substring(1, arg.length() - 1);
                LOG.debug("Getting data with key of " + key);
                String value = installProfile.data.get(key).getValue(!instanceInstaller.isServer, librariesDirectory);

                if (value == null || value.isEmpty()) {
                    LOG.error("Failed to process processor with jar " + this.jar + " as the argument with name "
                            + arg + " as the data item with key " + key + " was empty or null");
                    instanceInstaller.cancel(true);
                    return;
                }

                LOG.debug("Got value of " + value);
                // checking for local file paths returned "/data/client.lzma" and then makes
                // sure we localise it to the libraries folder if it's indeed local
                if (value.charAt(0) == '/') {
                    if (value.toLowerCase().contains(FileSystem.BASE_DIR.toString().toLowerCase())) {
                        // if the value starts with our base launcher dir, then it's likely already
                        // resolved (such as {INSTALLER})
                        args.add(value);
                    } else {
                        File localFile = new File(extractedDir, value);
                        LOG.debug("Got argument with local file of " + localFile.getAbsolutePath());

                        if (!localFile.exists() || !localFile.isFile()) {
                            LOG.error("Failed to process argument with value of " + value + " as the local file "
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
                LOG.debug("Got argument with file of " + artifactFile.getAbsolutePath());

                if (!artifactFile.exists() || !artifactFile.isFile()) {
                    LOG.error("Failed to process argument with value of " + arg + " as the file "
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

        ClassLoader parentClassLoader = null;
        try {
            Method getPlatform = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader");
            parentClassLoader = (ClassLoader) getPlatform.invoke(null);
        } catch (Exception e) {
        }

        ClassLoader cl = new URLClassLoader(classpath.toArray(new URL[0]), parentClassLoader);
        Thread currentThread = Thread.currentThread();
        ClassLoader threadClassloader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);

        try {
            LOG.debug("Running processor with args \"" + String.join(" ", args) + "\"");
            Class<?> cls = Class.forName(mainClass, true, cl);
            Method main = cls.getDeclaredMethod("main", String[].class);
            main.invoke(null, (Object) args.toArray(new String[args.size()]));
        } catch (InvocationTargetException ite) {
            Throwable e = ite.getCause();
            LOG.error("Failed to process processor with jar " + this.jar + " as there was an error invoking the jar",
                    e);
            instanceInstaller.cancel(true);
        } catch (Throwable e) {
            LOG.error("Failed to process processor with jar " + this.jar + " as there was an error invoking the jar",
                    e);
            instanceInstaller.cancel(true);
        } finally {
            currentThread.setContextClassLoader(threadClassloader);
        }
    }

    public boolean needToRun(ForgeInstallProfile installProfile, File extractedDir,
            InstanceInstaller instanceInstaller) {
        if (this.sides != null && !this.sides.contains(instanceInstaller.isServer ? "server" : "client")) {
            LOG.debug("No need to run processor " + this.jar + " since it's not needed for this side");
            return false;
        }

        if (!this.hasOutputs()) {
            return true;
        }

        File librariesDirectory = instanceInstaller.isServer ? instanceInstaller.root.resolve("libraries").toFile()
                : FileSystem.LIBRARIES.toFile();

        for (Entry<String, String> entry : this.outputs.entrySet()) {
            String key = entry.getKey();
            LOG.debug("Processing output for " + key);

            char start = key.charAt(0);
            char end = key.charAt(key.length() - 1);

            if (start == '{' && end == '}') {
                LOG.debug("Getting data with key of " + key.substring(1, key.length() - 1));
                String dataItem = installProfile.data.get(key.substring(1, key.length() - 1))
                        .getValue(!instanceInstaller.isServer, librariesDirectory);
                if (dataItem == null || dataItem.isEmpty()) {
                    LOG.error("Failed to process processor with jar " + this.jar + " as the output with key "
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
                    LOG.debug("Getting data with key of " + value.substring(1, value.length() - 1));
                    String valueDataItem = installProfile.data.get(value.substring(1, value.length() - 1))
                            .getValue(!instanceInstaller.isServer, librariesDirectory);
                    if (dataItem == null || dataItem.isEmpty()) {
                        LOG.error("Failed to process processor with jar " + this.jar
                                + " as the output with value " + value + " doesn't have a corresponding data entry");
                        instanceInstaller.cancel(true);
                        return true;
                    }

                    String sha1Hash = Hashing.sha1(outputFile.toPath()).toString();
                    String expectedHash = valueDataItem.charAt(0) == '\''
                            ? valueDataItem.substring(1, valueDataItem.length() - 1)
                            : valueDataItem;

                    LOG.debug("Expecting " + sha1Hash + " to equal " + sha1Hash);
                    if (!sha1Hash.equals(expectedHash)) {
                        Utils.delete(outputFile);
                        return true;
                    }
                }
            }
        }

        LOG.debug("No need to run processor " + this.jar + " since outputs all match hashes");

        return false;
    }
}
