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
package com.atlauncher.viewmodel.impl;

import com.atlauncher.App;
import com.atlauncher.data.WrapperNullType;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.tabs.VanillaPacksTab;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.utils.Pair;
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * 25 / 06 / 2022
 */
public class VanillaPacksViewModel implements IVanillaPacksViewModel, SettingsListener {


    private static final Logger LOG = LogManager.getLogger(VanillaPacksTab.class);

    private HashMap<LoaderType, List<LoaderVersion>> loaderVersionsMap = new HashMap<>();

    private final Map<VersionManifestVersionType, Boolean> minecraftVersionTypeFilters = new HashMap<>();
    private final PublishSubject<Map<VersionManifestVersionType, Boolean>>
        minecraftVersionTypeFiltersObservable = PublishSubject.create();

    @Nullable
    private String name = null;
    private boolean nameDirty = false;
    private final PublishSubject<String> nameObservable =
        PublishSubject.create();

    @Nullable
    private String description = null;
    private boolean descriptionDirty = false;
    private final PublishSubject<String> descriptionObservable =
        PublishSubject.create();

    @Nullable
    private String selectedMinecraftVersion = null;
    private final PublishSubject<WrapperNullType<String>> selectedMinecraftVersionObservable =
        PublishSubject.create();

    @Nullable
    private LoaderType selectedLoaderType = null;
    private final PublishSubject<WrapperNullType<LoaderType>> selectedLoaderTypeObservable =
        PublishSubject.create();

    @Nullable
    private LoaderVersion selectedLoaderVersion = null;
    private final PublishSubject<WrapperNullType<LoaderVersion>> selectedLoaderVersionObservable =
        PublishSubject.create();

    // Consumers

    /**
     * Represents the version list
     */
    private final Observable<MCVersionRow[]> versionsObservable =
        minecraftVersionTypeFiltersObservable.map(versionFilter -> {
            List<VersionManifestVersionType> versionList = new ArrayList<>();

            versionFilter.forEach((key, value) -> {
                if (value)
                    versionList.add(key);
            });

            return MinecraftManager
                .getFilteredMinecraftVersions(versionList)
                .stream()
                .map(it -> new MCVersionRow(it.id, it.releaseTime, it.id))
                .toArray(MCVersionRow[]::new);
        });

    private final PublishSubject<Boolean> loaderGroupEnabledObservable = PublishSubject.create();

    /**
     * Presents the loader versions for the user to select
     */
    private final Observable<WrapperNullType<LoaderVersion[]>> loaderVersions =
        selectedLoaderTypeObservable.zipWith(
            selectedMinecraftVersionObservable,
            Pair::new
        ).map(pair -> {
            LOG.debug("loaderVersions map");
            if (pair.left() instanceof WrapperNullType.NULL) return WrapperNullType.no();
            if (pair.right() instanceof WrapperNullType.NULL) return WrapperNullType.no();

            LoaderType loaderType = ((WrapperNullType.Value<LoaderType>) pair.left()).value;
            String version = ((WrapperNullType.Value<String>) pair.right()).value;

            loaderGroupEnabledObservable.onNext(false);

            List<LoaderVersion> loaderVersions;

            loaderVersions = this.loaderVersionsMap.get(loaderType);
            if (loaderVersions == null) {
                switch (((WrapperNullType.Value<LoaderType>) pair.left()).value) {
                    case FABRIC:
                        loaderVersions = FabricLoader.getChoosableVersions(version);
                        break;
                    case FORGE:
                        loaderVersions = ForgeLoader.getChoosableVersions(version);
                        break;
                    case QUILT:
                        loaderVersions = QuiltLoader.getChoosableVersions(version);
                        break;
                    default:
                        return WrapperNullType.no();
                }

                loaderVersionsMap.put(loaderType, loaderVersions);
            }

            loaderGroupEnabledObservable.onNext(true);
            if (loaderVersions.size() == 0) {
                return WrapperNullType.no();
            }

            return WrapperNullType.value(loaderVersions.toArray(loaderVersions.toArray(new LoaderVersion[0])));
        });

    private final Observable<Boolean> createServerVisibleObservable =
        selectedMinecraftVersionObservable
            .distinctUntilChanged()
            .map(selectedVersion -> {
                if (selectedVersion instanceof WrapperNullType.Value)
                    try {
                        VersionManifestVersion version = MinecraftManager.getMinecraftVersion(((WrapperNullType.Value<String>) selectedVersion).value);
                        return version.hasServer();
                    } catch (InvalidMinecraftVersion ignored) {
                    }
                return false;
            });


    private PublishSubject<Font> fontObservable = PublishSubject.create();

    private final Observable<Boolean> fabricVisibleObservable =
        selectedMinecraftVersionObservable
            .map(version -> !getFabricDisabledMCVersions().contains(version));

    private final Observable<Boolean> forgeVisibleObservable =
        selectedMinecraftVersionObservable
            .map(version -> !getForgeDisabledMCVersions().contains(version));

    private final Observable<Boolean> quiltVisibleObservable =
        selectedMinecraftVersionObservable
            .map(version -> !getQuiltDisabledMCVersions().contains(version));

    public VanillaPacksViewModel() {
        SettingsManager.addListener(this);
        selectedMinecraftVersionObservable.subscribe(version -> {
            if (version instanceof WrapperNullType.Value) {
                String defaultNameFieldValue = String.format(
                    "Minecraft %s",
                    ((WrapperNullType.Value<String>) version).value
                );
                if (name == null || name.isEmpty() || !nameDirty) {
                    nameDirty = false;
                    nameObservable.onNext(defaultNameFieldValue);
                }
                if (description == null || description.isEmpty() || !descriptionDirty) {
                    descriptionDirty = false;
                    descriptionObservable.onNext(defaultNameFieldValue);
                }
            }
        });
        minecraftVersionTypeFilters.put(VersionManifestVersionType.RELEASE, false);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
        selectedLoaderTypeObservable.onNext(WrapperNullType.no());
        selectedLoaderVersionObservable.onNext(WrapperNullType.no());
    }

    private void install(boolean isServer) {
        Installable installable;
        try {
            LoaderVersion selectedLoaderVersion = ((WrapperNullType.Value<LoaderVersion>) selectedLoaderVersionObservable.blockingFirst()).value;
            String selectedMinecraftVersion = ((WrapperNullType.Value<String>) selectedMinecraftVersionObservable.blockingFirst()).value;
            String description = descriptionObservable.blockingFirst();
            String name = nameObservable.blockingFirst();

            installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(selectedMinecraftVersion),
                selectedLoaderVersion, description);
            installable.instanceName = name;
            installable.isReinstall = false;
            installable.isServer = isServer;

            boolean success = installable.startInstall();

            if (success) {
                //loaderTypeNoneRadioButton.setSelected(true);
                //selectedLoaderTypeChanged(null);

                //minecraftVersionTable.setRowSelectionInterval(0, 0);
            }
        } catch (InvalidMinecraftVersion e) {
            LOG.error("error", e);
        }
    }

    @Override
    public void addOnFontChanged(Consumer<Font> consumer) {
        fontObservable.subscribe(consumer::accept);
        fontObservable.onNext(App.THEME.getBoldFont());
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
        nameDirty = true;
        nameObservable.onNext(name);
    }

    @Override
    public void addOnNameChanged(Consumer<String> name) {
        nameObservable.subscribe(name::accept);
    }

    @Override
    public void setDescription(@NotNull String description) {
        this.description = description;
        descriptionDirty = true;
        descriptionObservable.onNext(description);
    }

    @Override
    public void addOnDescriptionChanged(Consumer<String> name) {
        descriptionObservable.subscribe(name::accept);
    }

    @Override
    public boolean showReleaseOption() {
        return ConfigManager.getConfigItem("minecraft.release.enabled", true);
    }

    @Override
    public boolean showExperimentOption() {
        return ConfigManager.getConfigItem("minecraft.experiment.enabled", true);
    }

    @Override
    public boolean showSnapshotOption() {
        return ConfigManager.getConfigItem("minecraft.snapshot.enabled", true);
    }

    @Override
    public boolean showOldAlphaOption() {
        return ConfigManager.getConfigItem("minecraft.old_alpha.enabled", true);
    }

    @Override
    public boolean showOldBetaOption() {
        return ConfigManager.getConfigItem("minecraft.old_beta.enabled", true);
    }

    @Override
    public void setReleaseSelected(boolean b) {
        minecraftVersionTypeFilters.put(VersionManifestVersionType.RELEASE, b);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
    }

    @Nullable
    @Override
    public void addOnReleaseEnabledChanged(Consumer<Boolean> onChanged) {
        minecraftVersionTypeFiltersObservable.subscribe(it -> {
            onChanged.accept(
                it.getOrDefault(VersionManifestVersionType.RELEASE, false)
            );
        });
    }

    @Override
    public void setExperimentSelected(boolean b) {
        minecraftVersionTypeFilters.put(VersionManifestVersionType.EXPERIMENT, b);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
    }

    @Override
    public void addOnExperimentEnabledChanged(Consumer<Boolean> onChanged) {
        minecraftVersionTypeFiltersObservable.subscribe(it -> {
            onChanged.accept(
                it.getOrDefault(VersionManifestVersionType.EXPERIMENT, false)
            );
        });
    }

    @Override
    public void setSnapshotSelected(boolean b) {
        minecraftVersionTypeFilters.put(VersionManifestVersionType.SNAPSHOT, b);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
    }

    @Override
    public void addOnSnapshotEnabledChanged(Consumer<Boolean> onChanged) {
        minecraftVersionTypeFiltersObservable.subscribe(it -> {
            onChanged.accept(
                it.getOrDefault(VersionManifestVersionType.SNAPSHOT, false)
            );
        });
    }

    @Override
    public void setOldAlphaSelected(boolean b) {
        minecraftVersionTypeFilters.put(VersionManifestVersionType.OLD_ALPHA, b);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
    }

    @Override
    public void addOnOldAlphaEnabledChanged(Consumer<Boolean> onChanged) {
        minecraftVersionTypeFiltersObservable.subscribe(it -> {
            onChanged.accept(
                it.getOrDefault(VersionManifestVersionType.OLD_ALPHA, false)
            );
        });
    }

    @Override
    public void setOldBetaSelected(boolean b) {
        minecraftVersionTypeFilters.put(VersionManifestVersionType.OLD_BETA, b);
        minecraftVersionTypeFiltersObservable.onNext(minecraftVersionTypeFilters);
    }

    @Override
    public void addOnOldBetaEnabledChanged(Consumer<Boolean> onChanged) {
        minecraftVersionTypeFiltersObservable.subscribe(it -> {
            onChanged.accept(
                it.getOrDefault(VersionManifestVersionType.OLD_BETA, false)
            );
        });
    }

    @Override
    public void setMinecraftVersion(String newVersion) {
        this.selectedMinecraftVersion = newVersion;
        if (newVersion != null)
            selectedMinecraftVersionObservable.onNext(WrapperNullType.value(newVersion));
        else selectedMinecraftVersionObservable.onNext(WrapperNullType.no());
    }

    @Override
    public void addOnMinecraftVersionsChanged(Consumer<MCVersionRow[]> onChanged) {
        versionsObservable.subscribe(onChanged::accept);
    }

    @Override
    public boolean showFabricOption() {
        return ConfigManager.getConfigItem("loaders.fabric.enabled", true);
    }

    @Override
    public void addOnFabricVisibleChanged(Consumer<Boolean> onChanged) {
        fabricVisibleObservable.subscribe(onChanged::accept);
    }

    @Override
    public boolean showForgeOption() {
        return ConfigManager.getConfigItem("loaders.forge.enabled", true);
    }

    @Override
    public void addOnForgeVisibleChanged(Consumer<Boolean> onChanged) {
        forgeVisibleObservable.subscribe(onChanged::accept);
    }

    @Override
    public boolean showQuiltOption() {
        return ConfigManager.getConfigItem("loaders.quilt.enabled", false);
    }

    @Override
    public void addOnQuiltVisibleChanged(Consumer<Boolean> onChanged) {
        quiltVisibleObservable.subscribe(onChanged::accept);
    }

    @Nullable
    public LoaderType getLoaderType() {
        return ((WrapperNullType.Value<LoaderType>) selectedLoaderTypeObservable.blockingFirst()).value;
    }

    @Override
    public void setLoaderType(@Nullable LoaderType loader) {
        this.selectedLoaderType = loader;
        if (loader == null)
            selectedLoaderTypeObservable.onNext(WrapperNullType.no());
        else selectedLoaderTypeObservable.onNext(WrapperNullType.value(loader));

    }

    @Override
    public void setLoaderVersion(String loaderVersion) {
        loaderVersions.first(WrapperNullType.value(new LoaderVersion[0])).subscribe(versions -> {
            if (versions instanceof WrapperNullType.Value) {
                for (LoaderVersion version : ((WrapperNullType.Value<LoaderVersion[]>) versions).value) {
                    if (Objects.equals(version.version, loaderVersion)) {
                        selectedLoaderVersion = version;
                        selectedLoaderVersionObservable.onNext(WrapperNullType.value(version));
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void addOnLoaderVersionsChanged(Consumer<String[]> consumer) {
        loaderVersions.<WrapperNullType<String[]>>map(versions -> {
            if (versions instanceof WrapperNullType.Value) {
                return WrapperNullType.value(
                    Arrays.stream(((WrapperNullType.Value<LoaderVersion[]>) versions).value).map(it -> it.version).toArray(String[]::new)
                );
            }
            return WrapperNullType.no();
        }).subscribe(it -> {
            if (it instanceof WrapperNullType.Value)
                consumer.accept(((WrapperNullType.Value<String[]>) it).value);
        });
    }

    @Override
    public void addOnLoaderLoadingListener(Consumer<Boolean> consumer) {
        loaderGroupEnabledObservable.subscribe(consumer::accept);
    }

    @Override
    public List<String> getFabricDisabledMCVersions() {
        return ConfigManager.getConfigItem("loaders.fabric.disabledMinecraftVersions", new ArrayList<String>());
    }

    @Override
    public List<String> getForgeDisabledMCVersions() {
        return ConfigManager.getConfigItem("loaders.forge.disabledMinecraftVersions", new ArrayList<String>());
    }

    @Override
    public List<String> getQuiltDisabledMCVersions() {
        return ConfigManager.getConfigItem("loaders.quilt.disabledMinecraftVersions", new ArrayList<String>());
    }

    @Override
    public void addOnCreateServerVisibleChanged(Consumer<Boolean> consumer) {
        createServerVisibleObservable.subscribe(consumer::accept);
    }

    @Override
    public void createServer() {

    }

    @Override
    public void createInstance() {

    }

    @Override
    public void onSettingsSaved() {
        fontObservable.onNext(App.THEME.getBoldFont());
    }
}
