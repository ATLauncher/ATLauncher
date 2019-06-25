/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Language;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.forge.Version;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonParseException;

public class NewInstanceInstaller extends InstanceInstaller {
    public List<Library> libraries = new ArrayList<>();
    public Loader loader;
    public LoaderVersion loaderVersion;
    public com.atlauncher.data.json.Version jsonVersion;

    public NewInstanceInstaller(String instanceName, com.atlauncher.data.Pack pack,
            com.atlauncher.data.PackVersion version, boolean isReinstall, boolean isServer, String shareCode,
            boolean showModsChooser, com.atlauncher.data.loaders.LoaderVersion loaderVersion) {
        super(instanceName, pack, version, isReinstall, isServer, shareCode, showModsChooser, loaderVersion);
    }

    public NewInstanceInstaller(String instanceName, com.atlauncher.data.Pack pack,
            com.atlauncher.data.PackVersion version, boolean isReinstall, boolean isServer, String shareCode,
            boolean showModsChooser, LoaderVersion loaderVersion) {
        super(instanceName, pack, version, isReinstall, isServer, shareCode, showModsChooser, null);

        this.loaderVersion = loaderVersion;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        LogManager.info("Started install of " + this.pack.getName() + " - " + this.version);

        try {
            this.jsonVersion = Gsons.DEFAULT.fromJson(this.pack.getJSON(version.getVersion()),
                    com.atlauncher.data.json.Version.class);

            return install();
        } catch (JsonParseException e) {
            LogManager.logStackTrace("Couldn't parse JSON of pack!", e);
        }

        return false;
    }

    private Boolean install() throws Exception {
        if (this.jsonVersion.hasMessages()) {
            if (this.isReinstall && this.jsonVersion.getMessages().hasUpdateMessage()) {
                if (this.jsonVersion.getMessages().showUpdateMessage(this.pack) != 0) {
                    LogManager.error("Instance Install Cancelled After Viewing Message!");
                    cancel(true);
                    return false;
                }
            } else if (this.jsonVersion.getMessages().hasInstallMessage()) {
                if (this.jsonVersion.getMessages().showInstallMessage(this.pack) != 0) {
                    LogManager.error("Instance Install Cancelled After Viewing Message!");
                    cancel(true);
                    return false;
                }
            }
        }

        this.jsonVersion.compileColours();

        determineModsToBeInstalled();

        this.instanceIsCorrupt = true; // From this point on the instance has become corrupt

        getTempDirectory().mkdirs(); // Make the temp directory
        backupSelectFiles();
        makeDirectories();
        addPercent(5);
        if (this.jsonVersion.hasLoader()) {
            setMainClass();
        }
        setExtraArguments();
        if (!this.isServer && this.version.getMinecraftVersion().getMojangVersion().getAssetIndex() != null) {
            downloadResources(); // Download Minecraft Resources
            if (isCancelled()) {
                return false;
            }
        }

        downloadMinecraft(); // Download Minecraft
        if (isCancelled()) {
            return false;
        }

        if (!this.isServer && this.version.getMinecraftVersion().getMojangVersion().hasLogging()) {
            downloadLoggingClient(); // Download logging client
        }

        if (this.jsonVersion.hasLoader()) {
            try {
                this.loader = this.jsonVersion.getLoader().getNewLoader(new File(this.getTempDirectory(), "loader"), this,
                        this.loaderVersion);
            } catch (Throwable e) {
                LogManager.logStackTrace(e);
                LogManager.error("Cannot install instance because the loader failed to create");
                return false;
            }

            downloadLoader(); // Download Loader
            if (isCancelled()) {
                return false;
            }

            installLoader(); // Install Loader
            if (isCancelled()) {
                return false;
            }
        }

        downloadLibraries(); // Download Libraries
        if (isCancelled()) {
            return false;
        }
        organiseLibraries(); // Organise the libraries
        if (isCancelled()) {
            return false;
        }
        addPercent(5);
        if (this.isServer && this.hasJarMods()) {
            fireTask(Language.INSTANCE.localize("server.extractingjar"));
            fireSubProgressUnknown();
            Utils.unzip(getMinecraftJar(), getTempJarDirectory());
        }
        if (!this.isServer && this.hasJarMods() && !this.hasForge()) {
            deleteMetaInf();
        }
        addPercent(5);
        if (selectedMods.size() != 0) {
            addPercent(40);
            fireTask(Language.INSTANCE.localize("instance.downloadingmods"));
            downloadMods(selectedMods);
            if (isCancelled()) {
                return false;
            }
            addPercent(40);
            installMods();
        } else {
            addPercent(80);
        }
        if (isCancelled()) {
            return false;
        }
        if (this.jsonVersion.shouldCaseAllFiles()) {
            doCaseConversions(getModsDirectory());
        }
        if (isServer && hasJarMods()) {
            fireTask(Language.INSTANCE.localize("server.zippingjar"));
            fireSubProgressUnknown();
            Utils.zip(getTempJarDirectory(), getMinecraftJar());
        }
        if (extractedTexturePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingtexturepackfiles"));
            fireSubProgressUnknown();
            if (!getTexturePacksDirectory().exists()) {
                getTexturePacksDirectory().mkdir();
            }
            Utils.zip(getTempTexturePackDirectory(), new File(getTexturePacksDirectory(), "TexturePack.zip"));
        }
        if (extractedResourcePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingresourcepackfiles"));
            fireSubProgressUnknown();
            if (!getResourcePacksDirectory().exists()) {
                getResourcePacksDirectory().mkdir();
            }
            Utils.zip(getTempResourcePackDirectory(), new File(getResourcePacksDirectory(), "ResourcePack.zip"));
        }
        if (isCancelled()) {
            return false;
        }
        if (hasActions()) {
            doActions();
        }
        if (isCancelled()) {
            return false;
        }
        if (!this.jsonVersion.hasNoConfigs()) {
            configurePack();
        }
        if (isCancelled()) {
            return false;
        }
        // Copy over common configs if any
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getRootDirectory());
        }
        restoreSelectFiles();
        if (isServer) {
            File batFile = new File(getRootDirectory(), "LaunchServer.bat");
            File shFile = new File(getRootDirectory(), "LaunchServer.sh");
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.bat"), batFile, "%%SERVERJAR%%",
                    getServerJar());
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.sh"), shFile, "%%SERVERJAR%%",
                    getServerJar());
            batFile.setExecutable(true);
            shFile.setExecutable(true);
        }

        // add in the deselected mods to the instance.json
        for (com.atlauncher.data.json.Mod mod : this.unselectedMods) {
            String file = mod.getFile();
            if (this.jsonVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.jsonVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }

            this.modsInstalled
                    .add(new com.atlauncher.data.DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                            com.atlauncher.data.Type.valueOf(com.atlauncher.data.Type.class, mod.getType().toString()),
                            this.jsonVersion.getColour(mod.getColour()), mod.getDescription(), false, false, false,
                            mod.getCurseModId(), mod.getCurseFileId()));
        }

        return true;
    }

    private void determineModsToBeInstalled() {
        this.allMods = sortMods(
                (this.isServer ? this.jsonVersion.getServerInstallMods() : this.jsonVersion.getClientInstallMods()));

        boolean hasOptional = this.allMods.stream().anyMatch(mod -> mod.isOptional());

        if (this.allMods.size() != 0 && hasOptional) {
            com.atlauncher.gui.dialogs.ModsChooser modsChooser = new com.atlauncher.gui.dialogs.ModsChooser(this);

            if (this.shareCode != null) {
                modsChooser.applyShareCode(shareCode);
            }

            if (this.showModsChooser) {
                modsChooser.setVisible(true);
            }

            if (modsChooser.wasClosed()) {
                this.cancel(true);
                return;
            }
            this.selectedMods = modsChooser.getSelectedMods();
            this.unselectedMods = modsChooser.getUnselectedMods();
        }

        if (!hasOptional) {
            this.selectedMods = this.allMods;
        }

        modsInstalled = new ArrayList<>();
        for (com.atlauncher.data.json.Mod mod : this.selectedMods) {
            String file = mod.getFile();
            if (this.jsonVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.jsonVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }
            this.modsInstalled
                    .add(new com.atlauncher.data.DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                            com.atlauncher.data.Type.valueOf(com.atlauncher.data.Type.class, mod.getType().toString()),
                            this.jsonVersion.getColour(mod.getColour()), mod.getDescription(), false, false, true,
                            mod.getCurseModId(), mod.getCurseFileId()));
        }

        if (this.isReinstall && instance.hasCustomMods()
                && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion().getVersion())) {
            for (com.atlauncher.data.DisableableMod mod : instance.getCustomDisableableMods()) {
                modsInstalled.add(mod);
            }
        }
    }
}
