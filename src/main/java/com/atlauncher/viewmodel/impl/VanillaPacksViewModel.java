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

import static io.reactivex.rxjava3.core.Observable.combineLatest;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mini2Dx.gettext.GetText;

import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.atlauncher.App;
import com.atlauncher.data.MCVersionRow;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.graphql.GetLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;


/**
 * 25 / 06 / 2022
 */
public class VanillaPacksViewModel implements SettingsListener, IVanillaPacksViewModel {
    /**
     * Name to display
     */
    public final BehaviorSubject<Optional<String>> name = BehaviorSubject.create();
    /**
     * Description to display
     */
    public final BehaviorSubject<Optional<String>> description = BehaviorSubject.create();
    public final BehaviorSubject<Optional<LoaderVersion>> selectedLoaderVersion = BehaviorSubject.create();
    public final BehaviorSubject<Boolean> loaderLoading = BehaviorSubject.create();
    public final BehaviorSubject<Font> font = BehaviorSubject.create();
    // was false
    public final BehaviorSubject<Boolean> loaderVersionsDropDownEnabled = BehaviorSubject.create();
    /**
     * Presents the loader versions for the user to select
     */
    // was null
    public final BehaviorSubject<Optional<List<LoaderVersion>>> loaderVersions = BehaviorSubject.create();
    // was false
    public final BehaviorSubject<Boolean> createServerEnabled = BehaviorSubject.create();
    // was false
    public final BehaviorSubject<Boolean> createInstanceEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeFabricEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeForgeEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeLegacyFabricEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeNoneEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeQuiltEnabled = BehaviorSubject.create();
    // was lazy
    public final Boolean showReleaseOption =
        ConfigManager.getConfigItem("minecraft.release.enabled", true);
    // was lazy
    public final Boolean showExperimentOption =
        ConfigManager.getConfigItem(
            "minecraft.experiment.enabled",
            true
        );
    // was lazy
    public final Boolean showSnapshotOption = ConfigManager.getConfigItem("minecraft.snapshot.enabled", true);
    // was lazy
    public final Boolean showOldAlphaOption =
        ConfigManager.getConfigItem(
            "minecraft.old_alpha.enabled",
            true
        );
    // was lazy
    public final Boolean showOldBetaOption = ConfigManager.getConfigItem("minecraft.old_beta.enabled", true);
    // was lazy
    public final Boolean showFabricOption = ConfigManager.getConfigItem("loaders.fabric.enabled", true);
    // was lazy
    public final Boolean showForgeOption = ConfigManager.getConfigItem("loaders.forge.enabled", true);
    // was lazy
    public final Boolean showLegacyFabricOption =
        ConfigManager.getConfigItem(
            "loaders.legacyfabric.enabled",
            true
        );
    // was lazy
    public final Boolean showQuiltOption = ConfigManager.getConfigItem("loaders.quilt.enabled", false);
    /**
     * Filters applied to the version table
     */
    private final BehaviorSubject<Map<VersionManifestVersionType, Boolean>> minecraftVersionTypeFiltersPublisher =
        BehaviorSubject.create();

    /**
     * Represents the version list
     */
    public final Observable<List<MCVersionRow>> minecraftVersions =
        minecraftVersionTypeFiltersPublisher.map(versionFilter -> {
            final List<VersionManifestVersionType> filtered = versionFilter
                .entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            final DateTimeFormatter fmt = DateTimeFormat.forPattern(App.settings.dateFormat);

            return MinecraftManager.getFilteredMinecraftVersions(filtered).stream().map(it ->
                new MCVersionRow(
                    it.id, fmt.print(ISODateTimeFormat.dateTimeParser().parseDateTime(it.releaseTime)), it.id
                )
            ).collect(Collectors.toList());
        }).subscribeOn(Schedulers.io());

    public final Observable<Boolean> releaseSelected =
        minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.RELEASE, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> releaseEnabled =
        combineLatest(
            releaseSelected, minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (b.values().stream().filter(it -> it).toArray().length == 1))
        ).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> experimentSelected =
        minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.EXPERIMENT, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> experimentEnabled =
        combineLatest(experimentSelected,
            minecraftVersionTypeFiltersPublisher, (a, b) ->
                !(a && (count(b, Map.Entry::getValue) == 1))
        ).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> snapshotSelected =
        minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.SNAPSHOT, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> snapshotEnabled =
        combineLatest(
            snapshotSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))
        ).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldAlphaSelected =
        minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.OLD_ALPHA, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldAlphaEnabled =
        combineLatest(
            oldAlphaSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))
        ).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldBetaSelected =
        minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.OLD_BETA, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldBetaEnabled =
        combineLatest(
            oldBetaSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))
        ).subscribeOn(Schedulers.computation());

    // was null
    private final BehaviorSubject<Optional<String>> selectedMinecraftVersionFlow = BehaviorSubject.create();

    public final Observable<Integer> selectedMinecraftVersionIndex =
        combineLatest(
            minecraftVersions,
            selectedMinecraftVersionFlow,
            (versions, version) -> {

                int index = -1;

                for (int i = 0; i < versions.size(); i++) {
                    if (Objects.equals(versions.get(i).id, version.orElse(null))) {
                        index = i;
                        break;
                    }
                }

                if (index == -1) return 0;
                return index;
            }
        ).subscribeOn(Schedulers.computation());

    private final BehaviorSubject<Optional<LoaderType>> selectedLoaderType = BehaviorSubject.create();

    public final Observable<Boolean> loaderTypeFabricSelected =
        selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.FABRIC)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeForgeSelected =
        selectedLoaderType.map(it -> it.orElse(null) == LoaderType.FORGE)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeLegacyFabricSelected =
        selectedLoaderType.map(it -> it.orElse(null) == LoaderType.LEGACY_FABRIC)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeNoneSelected =
        selectedLoaderType.map(it -> it.orElse(null) == null)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeQuiltSelected =
        selectedLoaderType.map(it -> it.orElse(null) == LoaderType.QUILT)
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> fabricDisabledMCVersions =
        ConfigManager.getConfigItem("loaders.fabric.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isFabricVisible =
        selectedMinecraftVersionFlow
            .map(version -> !fabricDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> legacyFabricDisabledMCVersions =
        ConfigManager.getConfigItem(
            "loaders.fabric.disabledMinecraftVersions", emptyList()
        );

    public final Observable<Boolean> isLegacyFabricVisible =
        selectedMinecraftVersionFlow.map(version -> !legacyFabricDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> forgeDisabledMCVersions =
        ConfigManager.getConfigItem(
            "loaders.forge.disabledMinecraftVersions", emptyList()
        );

    public final Observable<Boolean> isForgeVisible =
        selectedMinecraftVersionFlow.map(version -> !forgeDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> quiltDisabledMCVersions =
        ConfigManager.getConfigItem(
            "loaders.quilt.disabledMinecraftVersions", emptyList()
        );

    public final Observable<Boolean> isQuiltVisible =
        selectedMinecraftVersionFlow.map(version -> !quiltDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> disabledQuiltVersions =
        ConfigManager.getConfigItem(
            "loaders.quilt.disabledVersions", emptyList()
        );

    // was lazy
    private final List<String> disabledFabricVersions =
        ConfigManager.getConfigItem(
            "loaders.fabric.disabledVersions", emptyList()
        );

    // was lazy
    private final List<String> disabledLegacyFabricVersions =
        ConfigManager.getConfigItem(
            "loaders.legacyfabric.disabledVersions", emptyList()
        );

    // was lazy
    private final List<String> disabledForgeVersions =
        ConfigManager.getConfigItem(
            "loaders.forge.disabledVersions", emptyList()
        );

    private final LoaderVersion noLoaderVersions = new LoaderVersion(GetText.tr("No Versions Found"));
    private final LoaderVersion errorLoadingVersions = new LoaderVersion(GetText.tr("Error Getting Versions"));
    /**
     * Name is dirty, as the user has inputted something
     */
    private boolean nameDirty = false;
    /**
     * Description is dirty, as the user has inputted something
     */
    private boolean descriptionDirty = false;

    public VanillaPacksViewModel() {
        SettingsManager.addListener(this);
        Map<VersionManifestVersionType, Boolean> map = new HashMap<>();
        map.put(VersionManifestVersionType.RELEASE, true);
        minecraftVersionTypeFiltersPublisher.onNext(map);

        name.onNext(Optional.empty());
        description.onNext(Optional.empty());
        selectedLoaderVersion.onNext(Optional.empty());

        font.onNext(App.THEME.getBoldFont());
        loaderVersionsDropDownEnabled.onNext(false);
        loaderVersions.onNext(Optional.empty());
        createServerEnabled.onNext(false);
        createInstanceEnabled.onNext(false);

        loaderTypeFabricEnabled.onNext(true);
        loaderTypeForgeEnabled.onNext(true);
        loaderTypeLegacyFabricEnabled.onNext(true);
        loaderTypeNoneEnabled.onNext(false);
        loaderTypeQuiltEnabled.onNext(false);

        selectedMinecraftVersionFlow.onNext(Optional.empty());

        selectedMinecraftVersionFlow
            .observeOn(Schedulers.computation())
            .subscribe(selectedVersion -> {
                if (selectedVersion.isPresent()) {
                    try {
                        final VersionManifestVersion version = MinecraftManager.getMinecraftVersion(selectedVersion.get());
                        createServerEnabled.onNext(version.hasServer());
                    } catch (InvalidMinecraftVersion ignored) {
                        createServerEnabled.onNext(false);
                    }
                } else {
                    createServerEnabled.onNext(false);
                }

                final String defaultNameFieldValue = String.format(
                    "Minecraft %s", selectedVersion.orElse(null)
                );
                if (!name.getValue().isPresent() || name.getValue().get().isEmpty() || !nameDirty) {
                    nameDirty = false;
                    name.onNext(Optional.of(defaultNameFieldValue));
                }
                if (!description.getValue().isPresent() || description.getValue().get().isEmpty() || !descriptionDirty) {
                    descriptionDirty = false;
                    description.onNext(Optional.of(defaultNameFieldValue));
                }
            });

        combineLatest(selectedLoaderType, selectedMinecraftVersionFlow, Pair::new)
            .observeOn(Schedulers.io())
            .subscribe(optionalOptionalPair -> {
                Optional<LoaderType> loaderTypeOptional = optionalOptionalPair.left();
                Optional<String> selectedMinecraftVersionOptional = optionalOptionalPair.right();

                if (!selectedMinecraftVersionOptional.isPresent()) return;

                loaderVersionsDropDownEnabled.onNext(false);

                if (!loaderTypeOptional.isPresent()) {
                    // update the name and description fields if they're not dirty
                    final String defaultNameFieldValue = String.format("Minecraft %s", selectedMinecraftVersionOptional.get());
                    if (!nameDirty) {
                        name.onNext(Optional.of(defaultNameFieldValue));
                    }
                    if (!descriptionDirty) {
                        description.onNext(Optional.of(defaultNameFieldValue));
                    }
                    loaderVersions.onNext(Optional.of(singletonList(new LoaderVersion(GetText.tr("Select Loader First")))));
                    return;
                }
                LoaderType loaderType = loaderTypeOptional.get();
                String selectedMinecraftVersion = selectedMinecraftVersionOptional.get();

                loaderLoading.onNext(true);

                setLoaderGroupEnabled(false);

                // Legacy Forge doesn't support servers easily
                final boolean enableCreateServers = (loaderType == LoaderType.FORGE || !Utils.matchVersion(
                    selectedMinecraftVersion, "1.5", true, true
                ));
                final List<LoaderVersion> loaders;
                if (ConfigManager.getConfigItem("useGraphql.vanillaLoaderVersions", false)) {
                    loaders = apolloLoad(loaderType, selectedMinecraftVersion, enableCreateServers);
                } else {
                    loaders = legacyLoad(loaderType, selectedMinecraftVersion, enableCreateServers);
                }

                loaderVersions.onNext(Optional.of(loaders));

                if (!loaders.isEmpty()) {
                    if (loaderType == LoaderType.FORGE) {
                        Optional<LoaderVersion> optionalLoaderType = first(loaders, it -> it.recommended);
                        if (optionalLoaderType.isPresent())
                            selectedLoaderVersion.onNext(optionalLoaderType);
                        else selectedLoaderVersion.onNext(loaders.stream().findFirst());
                    } else {
                        selectedLoaderVersion.onNext(loaders.stream().findFirst());
                    }
                }


                setLoaderGroupEnabled(true, enableCreateServers);

                updateNameAndDescription(selectedMinecraftVersion, loaderType);

            });
    }

    private static <K, V> long count(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream().filter(predicate).count();
    }

    private static <T> @Nonnull Optional<T> first(@Nonnull List<T> list, @Nonnull Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    private static <T> @Nullable T firstOrNull(@Nonnull List<T> list, @Nonnull Predicate<T> predicate) {
        return first(list, predicate).orElse(null);
    }

    @Override
    public Observable<Optional<String>> name() {
        return name.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Optional<String>> description() {
        return description.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Optional<LoaderVersion>> selectedLoaderVersion() {
        return selectedLoaderVersion.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderLoading() {
        return loaderLoading.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Font> font() {
        return font.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderVersionsDropDownEnabled() {
        return loaderVersionsDropDownEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Optional<List<LoaderVersion>>> loaderVersions() {
        return loaderVersions.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> createServerEnabled() {
        return createServerEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> createInstanceEnabled() {
        return createInstanceEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeFabricEnabled() {
        return loaderTypeFabricEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeForgeEnabled() {
        return loaderTypeForgeEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeLegacyFabricEnabled() {
        return loaderTypeLegacyFabricEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeNoneEnabled() {
        return loaderTypeNoneEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeQuiltEnabled() {
        return loaderTypeQuiltEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Boolean showReleaseOption() {
        return showReleaseOption;
    }

    @Override
    public Boolean showExperimentOption() {
        return showExperimentOption;
    }

    @Override
    public Boolean showSnapshotOption() {
        return showSnapshotOption;
    }

    @Override
    public Boolean showOldAlphaOption() {
        return showOldAlphaOption;
    }

    @Override
    public Boolean showOldBetaOption() {
        return showOldBetaOption;
    }

    @Override
    public Boolean showFabricOption() {
        return showFabricOption;
    }

    @Override
    public Boolean showForgeOption() {
        return showForgeOption;
    }

    @Override
    public Boolean showLegacyFabricOption() {
        return showLegacyFabricOption;
    }

    @Override
    public Boolean showQuiltOption() {
        return showQuiltOption;
    }

    @Override
    public Observable<List<MCVersionRow>> minecraftVersions() {
        return minecraftVersions.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> releaseSelected() {
        return releaseSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> releaseEnabled() {
        return releaseEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> experimentSelected() {
        return experimentSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> experimentEnabled() {
        return experimentEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> snapshotSelected() {
        return snapshotSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> snapshotEnabled() {
        return snapshotEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> oldAlphaSelected() {
        return oldAlphaSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> oldAlphaEnabled() {
        return oldAlphaEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> oldBetaSelected() {
        return oldBetaSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> oldBetaEnabled() {
        return oldBetaEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Integer> selectedMinecraftVersionIndex() {
        return selectedMinecraftVersionIndex.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeFabricSelected() {
        return loaderTypeFabricSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeForgeSelected() {
        return loaderTypeForgeSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeLegacyFabricSelected() {
        return loaderTypeLegacyFabricSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeNoneSelected() {
        return loaderTypeNoneSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypeQuiltSelected() {
        return loaderTypeQuiltSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isFabricVisible() {
        return isFabricVisible.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isLegacyFabricVisible() {
        return isLegacyFabricVisible.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isForgeVisible() {
        return isForgeVisible.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isQuiltVisible() {
        return isQuiltVisible.observeOn(SwingSchedulers.edt());
    }

    private void install(Boolean isServer) {
        final Installable installable;
        try {
            final @Nullable LoaderVersion selectedLoaderVersion = this.selectedLoaderVersion.getValue().orElse(null);
            final Optional<String> selectedMinecraftVersionOptional = this.selectedMinecraftVersionFlow.getValue();

            if (!selectedMinecraftVersionOptional.isPresent())
                return;

            final @Nonnull String selectedMinecraftVersion = selectedMinecraftVersionOptional.get();
            final @Nullable String description = this.description.getValue().orElse(null);
            final @Nullable String name = this.name.getValue().orElse(null);

            installable = new VanillaInstallable(
                MinecraftManager.getMinecraftVersion(selectedMinecraftVersion),
                selectedLoaderVersion,
                description
            );
            installable.instanceName = name;
            installable.isReinstall = false;
            installable.isServer = isServer;
            final boolean success = installable.startInstall();

            // if (success) {
            // - Reset the view, currently disabled
            //nameFieldDirty = false
            //descriptionFieldDirty = false
            //loaderTypeNoneRadioButton.isSelected = true
            //selectedLoaderTypeChanged(null)
            //minecraftVersionTable!!.setRowSelectionInterfinal void(0, 0)
            // }
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }
    }

    private boolean isNameDirty() {
        return !(
            Objects.equals(name.getValue().orElse(null), String.format("Minecraft %s", selectedMinecraftVersionFlow.getValue().orElse(null))) ||
                selectedLoaderType.getValue().isPresent() &&
                    Objects.equals(name.getValue().orElse(null), String.format("Minecraft %s with %s", selectedMinecraftVersionFlow.getValue().orElse(null), selectedLoaderType.getValue().orElse(null)))
        );
    }

    public void setName(String name) {
        LogManager.debug("setName $name");
        this.name.onNext(Optional.of(name));
        nameDirty = isNameDirty();
    }

    private boolean isDescriptionDirty() {
        return !(
            Objects.equals(description.getValue().orElse(null), String.format("Minecraft %s", selectedMinecraftVersionFlow.getValue().orElse(null))) ||
                selectedLoaderType.getValue().isPresent() &&
                    Objects.equals(description.getValue().orElse(null), String.format("Minecraft %s with %s", selectedMinecraftVersionFlow.getValue().orElse(null), selectedLoaderType.getValue().orElse(null)))
        );
    }

    public void setDescription(String description) {
        descriptionDirty = isDescriptionDirty();
        this.description.onNext(Optional.of(description));
    }

    public void setReleaseSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.RELEASE, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    public void setExperimentSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap(minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.EXPERIMENT, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    public void setSnapshotSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap(minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.SNAPSHOT, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    public void setOldAlphaSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap(minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.OLD_ALPHA, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    public void setOldBetaSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap(minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.OLD_BETA, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    public void setSelectedMinecraftVersion(@Nullable String newVersion) {
        selectedMinecraftVersionFlow.onNext(Optional.ofNullable(newVersion));
    }

    public void setLoaderType(@Nullable LoaderType loader) {
        selectedLoaderType.onNext(Optional.ofNullable(loader));
    }

    public void setLoaderVersion(String loaderVersion) {
        Optional<List<LoaderVersion>> versions = loaderVersions.getValue();

        if (versions.isPresent()) {
            for (LoaderVersion version : versions.get()) {
                if (version.version.equals(loaderVersion)) {
                    selectedLoaderVersion.onNext(Optional.of(version));
                    break;
                }
            }
        }
    }

    public void createServer() {
        install(true);
    }

    public void createInstance() {
        install(false);
    }

    public void onSettingsSaved() {
        font.onNext(App.THEME.getBoldFont());
    }

    @Override
    public Boolean warnUserAboutServer() {
        return InstanceManager.getInstances().size() == 0;
    }

    private List<LoaderVersion> apolloLoad(
        LoaderType selectedLoader, @Nonnull String selectedMinecraftVersion, Boolean enableCreateServers
    ) {
        try {
            ApolloQueryCall<GetLoaderVersionsForMinecraftVersionQuery.Data> call = GraphqlClient.apolloClient.query(
                new GetLoaderVersionsForMinecraftVersionQuery(
                    selectedMinecraftVersion
                )
            ).toBuilder().httpCachePolicy(
                new HttpCachePolicy.Policy(
                    HttpCachePolicy.FetchStrategy.CACHE_FIRST, 5, TimeUnit.MINUTES, false
                )
            ).build();

            Response<GetLoaderVersionsForMinecraftVersionQuery.Data> response = Rx3Apollo.from(call).blockingFirst();

            final ArrayList<LoaderVersion> loaderVersionsList = new ArrayList<>();
            final GetLoaderVersionsForMinecraftVersionQuery.Data data = response.getData();

            if (data != null)
                switch (selectedLoader) {
                    case FABRIC: {
                        loaderVersionsList.addAll(data.loaderVersions().fabric().stream()
                            .filter(fv -> !disabledFabricVersions.contains(fv.version()))
                            .map(version -> new LoaderVersion(version.version(), false, "Fabric"))
                            .collect(Collectors.toList())
                        );
                    }

                    case FORGE: {
                        loaderVersionsList.addAll(data.loaderVersions().forge().stream()
                            .filter(fv -> !disabledForgeVersions.contains(fv.version()))
                            .map(version -> {
                                final LoaderVersion lv = new LoaderVersion(
                                    version.version(), version.rawVersion(), version.recommended(), "Forge"
                                );
                                if (version.installerSha1Hash() != null && version.installerSize() != null) {
                                    lv.downloadables.put("installer", new Pair<>(
                                        version.installerSha1Hash(), version.installerSize().longValue()
                                    ));
                                }
                                if (version.universalSha1Hash() != null && version.universalSize() != null) {
                                    lv.downloadables.put("universal", new Pair<>(
                                        version.universalSha1Hash(), version.universalSize().longValue()
                                    ));
                                }
                                if (version.clientSha1Hash() != null && version.clientSize() != null) {
                                    lv.downloadables.put("client", new Pair<>(
                                        version.clientSha1Hash(), version.clientSize().longValue()
                                    ));
                                }
                                if (version.serverSha1Hash() != null && version.serverSize() != null) {
                                    lv.downloadables.put("server", new Pair<>(
                                        version.serverSha1Hash(), version.serverSize().longValue()
                                    ));
                                }
                                return lv;
                            }).collect(Collectors.toList())
                        );
                    }

                    case QUILT: {
                        loaderVersionsList.addAll(data.loaderVersions().quilt().stream()
                            .filter(fv -> !disabledQuiltVersions.contains(fv.version()))
                            .map(version -> new LoaderVersion(version.version(), false, "Quilt"))
                            .collect(Collectors.toList())
                        );
                    }

                    case LEGACY_FABRIC: {

                        loaderVersionsList.addAll(data.loaderVersions().legacyfabric()
                            .stream()
                            .filter(fv -> !disabledLegacyFabricVersions.contains(fv.version()))
                            .map(version ->
                                new LoaderVersion(
                                    version.version(),
                                    false,
                                    "LegacyFabric"
                                )
                            )
                            .collect(Collectors.toList())
                        );
                    }
                }
            if (loaderVersionsList.size() == 0) {
                setLoaderGroupEnabled(true, enableCreateServers);
                return singletonList(noLoaderVersions);
            }
            return loaderVersionsList;
        } catch (RuntimeException e) {
            LogManager.logStackTrace("Error fetching loading versions", e);
            setLoaderGroupEnabled(true, enableCreateServers);
            return singletonList(errorLoadingVersions);
        }
    }

    private void setLoaderGroupEnabled(Boolean enabled) {
        setLoaderGroupEnabled(enabled, enabled);
    }

    private void setLoaderGroupEnabled(Boolean enabled, Boolean enableCreateServers) {
        loaderTypeNoneEnabled.onNext(enabled);
        loaderTypeFabricEnabled.onNext(enabled);
        loaderTypeForgeEnabled.onNext(enabled);
        loaderTypeLegacyFabricEnabled.onNext(enabled);
        loaderTypeQuiltEnabled.onNext(enabled);
        createServerEnabled.onNext(enableCreateServers);
        createInstanceEnabled.onNext(enabled);
        loaderVersionsDropDownEnabled.onNext(enabled);
    }

    /**
     * Use legacy loading mechanic
     */
    List<LoaderVersion> legacyLoad(
        LoaderType selectedLoader, String selectedMinecraftVersion, Boolean enableCreateServers
    ) {
        final ArrayList<LoaderVersion> loaderVersionsList = new ArrayList<>();
        switch (selectedLoader) {
            case FABRIC:
                loaderVersionsList.addAll(FabricLoader.getChoosableVersions(selectedMinecraftVersion));
                break;
            case FORGE:
                loaderVersionsList.addAll(ForgeLoader.getChoosableVersions(selectedMinecraftVersion));
                break;
            case QUILT:
                loaderVersionsList.addAll(QuiltLoader.getChoosableVersions(selectedMinecraftVersion));
                break;
            case LEGACY_FABRIC:
                loaderVersionsList.addAll(LegacyFabricLoader.getChoosableVersions(selectedMinecraftVersion));
                break;
        }


        if (loaderVersionsList.size() == 0) {
            setLoaderGroupEnabled(true, enableCreateServers);
            return singletonList(noLoaderVersions);
        }

        return loaderVersionsList;
    }

    /**
     * Update the name and description fields if they're not dirty with loader type information
     */
    private void updateNameAndDescription(
        String selectedMinecraftVersion, LoaderType selectedLoader
    ) {
        final String defaultNameField = String.format(
            "Minecraft %s with %s", selectedMinecraftVersion, selectedLoader.toString()
        );

        if (!nameDirty) {
            name.onNext(Optional.of(defaultNameField));
        }
        if (!descriptionDirty) {
            description.onNext(Optional.of(defaultNameField));
        }
    }
}