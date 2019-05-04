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
package com.atlauncher.data.loaders.fabric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.HashableDownloadable;
import com.atlauncher.data.loaders.Loader;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class FabricLoader implements Loader {
    protected String yarn;
    protected String loader;
    protected String minecraft;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (metadata.containsKey("yarn") && metadata.containsKey("loader")) {
            this.yarn = (String) metadata.get("yarn");
            this.loader = (String) metadata.get("loader");
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Fabric version");
            FabricMetaVersion latestVersion = this.getLatestVersion();
            this.yarn = latestVersion.getMappings().getVersion();
            this.loader = latestVersion.getLoader().getVersion();
        }
    }

    public List<FabricMetaVersion> getLoaders() {
        try {
            Downloadable loaderVersions = new Downloadable(
                    "https://meta.fabricmc.net/v1/versions/loader/" + this.minecraft, false);

            String contents = loaderVersions.getContents();

            java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
            }.getType();
            return Gsons.DEFAULT.fromJson(contents, type);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public FabricMetaVersion getLatestVersion() {
        List<FabricMetaVersion> loaders = this.getLoaders();

        if (loaders == null || loaders.size() == 0) {
            return null;
        }

        return loaders.get(0);
    }

    @Override
    public void downloadAndExtractInstaller() {
        File saveTo = new File(App.settings.getLoadersDir(),
                "fabric-loader-" + this.yarn + "-" + this.loader + "-vanilla-profile.zip");

        try {
            HashableDownloadable download = new HashableDownloadable("https://fabricmc.net/download/vanilla/?yarn="
                    + URLEncoder.encode(this.yarn, "UTF-8") + "&loader=" + URLEncoder.encode(this.loader, "UTF-8"),
                    saveTo, instanceInstaller);

            if (download.needToDownload()) {
                this.instanceInstaller.addTotalDownloadedBytes(download.getFilesize());
                download.download(true);
            }
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        this.tempDir.mkdir();
        Utils.unzip(saveTo, this.tempDir);
    }

    public FabricInstallProfile getInstallProfile() {
        FabricInstallProfile installProfile = null;

        try {
            installProfile = Gsons.DEFAULT
                    .fromJson(
                            new FileReader(
                                    new File(this.tempDir,
                                            "fabric-loader-" + this.yarn + "-" + this.loader + "/fabric-loader-"
                                                    + this.yarn + "-" + this.loader + ".json")),
                            FabricInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return installProfile;
    }

    @Override
    public List<Downloadable> getDownloadableLibraries() {
        List<Downloadable> librariesToDownload = new ArrayList<Downloadable>();

        // We use Fabric installer for servers, so we don't need to worry about
        // libraries
        if (!this.instanceInstaller.isServer()) {
            File librariesDirectory = this.instanceInstaller.isServer() ? this.instanceInstaller.getLibrariesDirectory()
                    : App.settings.getGameLibrariesDir();

            FabricInstallProfile installProfile = this.getInstallProfile();

            for (Library library : installProfile.getLibraries()) {
                String libraryPath = Utils.convertMavenIdentifierToPath(library.getName());
                File downloadTo = new File(App.settings.getGameLibrariesDir(), libraryPath);
                File finalDownloadTo = new File(librariesDirectory, libraryPath);

                String url = library.getUrl() + Utils.convertMavenIdentifierToPath(library.getName());

                librariesToDownload.add(new HashableDownloadable(url, downloadTo, instanceInstaller, finalDownloadTo));
            }
        }

        return librariesToDownload;
    }

    public String getInstallerVersion() {
        Downloadable installerVersions = new Downloadable(
                "https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml", false);

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(installerVersions.getContents())));

            Node rootElement = document.getFirstChild();
            NodeList rootList = rootElement.getChildNodes();
            for (int i = 0; i < rootList.getLength(); i++) {
                Node child = rootList.item(i);
                if (child.getNodeName().equals("versioning")) {
                    NodeList versioningList = child.getChildNodes();
                    for (int j = 0; j < versioningList.getLength(); j++) {
                        Node versionChild = versioningList.item(j);
                        if (versionChild.getNodeName().equals("release")) {
                            return versionChild.getTextContent();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    @Override
    public void runProcessors() {
        if (this.instanceInstaller.isServer()) {
            this.instanceInstaller.getMinecraftJar()
                    .renameTo(new File(this.instanceInstaller.getRootDirectory(), "server.jar"));

            Utils.delete(this.instanceInstaller.getLibrariesDirectory()); // no libraries here

            String installerVersion = this.getInstallerVersion();

            if (installerVersion == null) {
                LogManager.error("Failed to find innstaller version for Fabric");
                instanceInstaller.cancel(true);
                return;
            }

            LogManager.debug("Downloading installer version " + installerVersion + " for Fabric");

            File installerFile = new File(App.settings.getLoadersDir(),
                    "fabric-installer-" + this.yarn + "-" + this.loader + ".jar");

            Downloadable installerDownload = new HashableDownloadable(
                    "https://maven.fabricmc.net/net/fabricmc/fabric-installer/" + installerVersion
                            + "/fabric-installer-" + installerVersion + ".jar",
                    installerFile, this.instanceInstaller);

            if (installerDownload.needToDownload()) {
                installerDownload.download();
            }

            try {
                List<String> arguments = new ArrayList<String>();
                String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                arguments.add(path);
                arguments.add("-jar");
                arguments.add("\"" + installerFile.getAbsolutePath() + "\"");
                arguments.add("server");
                arguments.add("-dir");
                arguments.add("\"" + this.instanceInstaller.getRootDirectory().getAbsolutePath() + "\"");
                arguments.add("-mappings");
                arguments.add("\"" + this.yarn + "\"");
                arguments.add("-loader");
                arguments.add("\"" + this.loader + "\"");
                LogManager.debug("Running Fabric installer with arguments " + arguments.toString());

                ProcessBuilder processBuilder = new ProcessBuilder(arguments);
                processBuilder.directory(this.instanceInstaller.getRootDirectory().getAbsoluteFile());
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    LogManager.debug("Fabric installer output: " + line);
                }

                LogManager.debug("Finished running Fabric installer");
            } catch (Throwable e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    @Override
    public List<String> getLibraries() {
        FabricInstallProfile installProfile = this.getInstallProfile();
        List<String> libraries = new ArrayList<String>();

        // We use Fabric installer for servers, so we don't need to worry about
        // libraries
        if (!this.instanceInstaller.isServer()) {
            for (Library library : installProfile.getLibraries()) {
                libraries.add(Utils.convertMavenIdentifierToPath(library.getName()));
            }
        }

        return libraries;
    }

    @Override
    public List<String> getArguments() {
        List<String> arguments = new ArrayList<String>();

        if (this.getInstallProfile().getArguments() != null
                || this.getInstallProfile().getArguments().containsKey("game")
                || this.getInstallProfile().getArguments().get("game").size() != 0) {
            for (String argument : this.getInstallProfile().getArguments().get("game")) {
                arguments.add(argument);
            }
        }

        return arguments;
    }

    @Override
    public String getMainClass() {
        return this.getInstallProfile().getMainClass();
    }

    @Override
    public String getServerJar() {
        return "fabric-server-launch.jar";
    }

    @Override
    public boolean useMinecraftLibraries() {
        // We use Fabric installer for servers, so we don't need to worry about
        // libraries
        return !this.instanceInstaller.isServer();
    }

    @Override
    public boolean useMinecraftArguments() {
        return true;
    }
}
