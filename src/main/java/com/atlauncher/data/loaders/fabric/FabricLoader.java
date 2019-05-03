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

import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.ForgeXzDownloadable;
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
    public void set(String version, String minecraft, String yarn, String loader, boolean latest, boolean recommended,
            File tempDir, InstanceInstaller instanceInstaller) {
        this.minecraft = minecraft;
        this.yarn = yarn;
        this.loader = loader;
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;
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

        FabricInstallProfile installProfile = this.getInstallProfile();

        for (Library library : installProfile.getLibraries()) {
            File downloadTo = Utils.convertMavenIdentifierToFile(library.getName(), App.settings.getGameLibrariesDir());

            String url = library.getUrl() + Utils.convertMavenIdentifierToPath(library.getName());

            librariesToDownload.add(new HashableDownloadable(url, downloadTo, instanceInstaller));
        }

        return librariesToDownload;
    }

    @Override
    public void runProcessors() {

    }

    @Override
    public List<String> getLibraries() {
        FabricInstallProfile installProfile = this.getInstallProfile();
        List<String> libraries = new ArrayList<String>();

        for (Library library : installProfile.getLibraries()) {
            libraries.add(Utils.convertMavenIdentifierToPath(library.getName()));
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
}
