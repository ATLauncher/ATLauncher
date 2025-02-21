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

import java.awt.Font;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.MCVersionRow;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.graphql.GetLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.ICreatePackViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import static io.reactivex.rxjava3.core.Observable.combineLatest;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CreatePackViewModel implements SettingsListener, ICreatePackViewModel {
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
    public final BehaviorSubject<Optional<String>> createInstanceDisabledReason = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeFabricEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeForgeEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeLegacyFabricEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeNeoForgeEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypePaperEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypePurpurEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeNoneEnabled = BehaviorSubject.create();
    // was true
    public final BehaviorSubject<Boolean> loaderTypeQuiltEnabled = BehaviorSubject.create();
    // was lazy
    public final Boolean showReleaseOption = ConfigManager.getConfigItem("minecraft.release.enabled", true);
    // was lazy
    public final Boolean showExperimentOption = ConfigManager.getConfigItem(
            "minecraft.experiment.enabled",
            true);
    // was lazy
    public final Boolean showSnapshotOption = ConfigManager.getConfigItem("minecraft.snapshot.enabled", true);
    // was lazy
    public final Boolean showOldAlphaOption = ConfigManager.getConfigItem(
            "minecraft.old_alpha.enabled",
            true);
    // was lazy
    public final Boolean showOldBetaOption = ConfigManager.getConfigItem("minecraft.old_beta.enabled", true);
    // was lazy
    public final Boolean showFabricOption = ConfigManager.getConfigItem("loaders.fabric.enabled", true);
    // was lazy
    public final Boolean showForgeOption = ConfigManager.getConfigItem("loaders.forge.enabled", true);
    // was lazy
    public final Boolean showLegacyFabricOption = ConfigManager.getConfigItem(
            "loaders.legacyfabric.enabled",
            true);
    // was lazy
    public final Boolean showNeoForgeOption = ConfigManager.getConfigItem(
            "loaders.neoforge.enabled",
            true);
    // was lazy
    public final Boolean showPaperOption = ConfigManager.getConfigItem(
            "loaders.paper.enabled",
            true);
    // was lazy
    public final Boolean showPurpurOption = ConfigManager.getConfigItem(
            "loaders.purpur.enabled",
            true);
    // was lazy
    public final Boolean showQuiltOption = ConfigManager.getConfigItem("loaders.quilt.enabled", false);
    public final Observable<Integer> selectedLoaderVersionIndex = combineLatest(
            loaderVersions,
            selectedLoaderVersion,
            (versionsOptional, selectedOptional) -> {
                int index = -1;

                if (versionsOptional.isPresent()) {
                    List<LoaderVersion> versions = versionsOptional.get();
                    for (int i = 0; i < versions.size(); i++) {
                        if (Objects.equals(versions.get(i), selectedOptional.orElse(null))) {
                            index = i;
                            break;
                        }
                    }
                }

                if (index == -1)
                    return 0;
                return index;
            }).subscribeOn(Schedulers.computation());
    /**
     * Filters applied to the version table
     */
    private final BehaviorSubject<Map<VersionManifestVersionType, Boolean>> minecraftVersionTypeFiltersPublisher = BehaviorSubject
            .create();

    /**
     * Represents the version list
     */
    public final Observable<List<MCVersionRow>> minecraftVersions = minecraftVersionTypeFiltersPublisher
            .map(versionFilter -> {
                final List<VersionManifestVersionType> filtered = versionFilter
                        .entrySet()
                        .stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                final DateTimeFormatter fmt = DateTimeFormat.forPattern(App.settings.dateFormat);

                return MinecraftManager.getFilteredMinecraftVersions(filtered).stream().map(it -> new MCVersionRow(
                        it.id, fmt.print(ISODateTimeFormat.dateTimeParser().parseDateTime(it.releaseTime)),
                        it.type.toString())).collect(Collectors.toList());
            }).subscribeOn(Schedulers.io());

    public final Observable<Boolean> releaseSelected = minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.RELEASE, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> releaseEnabled = combineLatest(
            releaseSelected, minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (b.values().stream().filter(it -> it).toArray().length == 1)))
                    .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> experimentSelected = minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.EXPERIMENT, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> experimentEnabled = combineLatest(experimentSelected,
            minecraftVersionTypeFiltersPublisher, (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1)))
                    .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> snapshotSelected = minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.SNAPSHOT, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> snapshotEnabled = combineLatest(
            snapshotSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldAlphaSelected = minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.OLD_ALPHA, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldAlphaEnabled = combineLatest(
            oldAlphaSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))).subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldBetaSelected = minecraftVersionTypeFiltersPublisher
            .map(it -> it.getOrDefault(VersionManifestVersionType.OLD_BETA, false))
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> oldBetaEnabled = combineLatest(
            oldBetaSelected,
            minecraftVersionTypeFiltersPublisher,
            (a, b) -> !(a && (count(b, Map.Entry::getValue) == 1))).subscribeOn(Schedulers.computation());

    // was null
    private final BehaviorSubject<Optional<String>> selectedMinecraftVersionFlow = BehaviorSubject.create();

    public final Observable<Integer> selectedMinecraftVersionIndex = combineLatest(
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

                if (index == -1)
                    return 0;
                return index;
            }).subscribeOn(Schedulers.computation());

    private final BehaviorSubject<Optional<LoaderType>> selectedLoaderType = BehaviorSubject.create();

    public final Observable<Boolean> loaderTypeFabricSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.FABRIC)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeForgeSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.FORGE)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeLegacyFabricSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.LEGACY_FABRIC)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeNeoForgeSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.NEOFORGE)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypePaperSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.PAPER)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypePurpurSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.PURPUR)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeNoneSelected = selectedLoaderType.map(it -> it.orElse(null) == null)
            .subscribeOn(Schedulers.computation());

    public final Observable<Boolean> loaderTypeQuiltSelected = selectedLoaderType
            .map(it -> it.orElse(null) == LoaderType.QUILT)
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> fabricDisabledMCVersions = ConfigManager
            .getConfigItem("loaders.fabric.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isFabricVisible = selectedMinecraftVersionFlow
            .map(version -> !fabricDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> legacyFabricDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.fabric.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isLegacyFabricVisible = selectedMinecraftVersionFlow
            .map(version -> !legacyFabricDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> neoForgeDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.neoforge.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isNeoForgeVisible = selectedMinecraftVersionFlow
            .map(version -> !neoForgeDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> paperDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.paper.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isPaperVisible = selectedMinecraftVersionFlow
            .map(version -> !paperDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> purpurDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.purpur.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isPurpurVisible = selectedMinecraftVersionFlow
            .map(version -> !purpurDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> forgeDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.forge.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isForgeVisible = selectedMinecraftVersionFlow
            .map(version -> !forgeDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> quiltDisabledMCVersions = ConfigManager.getConfigItem(
            "loaders.quilt.disabledMinecraftVersions", emptyList());

    public final Observable<Boolean> isQuiltVisible = selectedMinecraftVersionFlow
            .map(version -> !quiltDisabledMCVersions.contains(version.orElse(null)))
            .subscribeOn(Schedulers.computation());

    // was lazy
    private final List<String> disabledQuiltVersions = ConfigManager.getConfigItem(
            "loaders.quilt.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledFabricVersions = ConfigManager.getConfigItem(
            "loaders.fabric.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledLegacyFabricVersions = ConfigManager.getConfigItem(
            "loaders.legacyfabric.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledNeoForgeVersions = ConfigManager.getConfigItem(
            "loaders.neoforge.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledPaperVersions = ConfigManager.getConfigItem(
            "loaders.paper.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledPurpurVersions = ConfigManager.getConfigItem(
            "loaders.purpur.disabledVersions", emptyList());

    // was lazy
    private final List<String> disabledForgeVersions = ConfigManager.getConfigItem(
            "loaders.forge.disabledVersions", emptyList());

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

    public CreatePackViewModel() {
        SettingsManager.addListener(this);
        setLoaderType(null); // Happen first to prevent race condition
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
        createInstanceDisabledReason.onNext(Optional.empty());

        loaderTypeNoneEnabled.onNext(true);
        loaderTypeQuiltEnabled.onNext(true);
        loaderTypeFabricEnabled.onNext(true);
        loaderTypeForgeEnabled.onNext(true);
        loaderTypeLegacyFabricEnabled.onNext(true);
        loaderTypeNeoForgeEnabled.onNext(true);
        loaderTypePaperEnabled.onNext(true);
        loaderTypePurpurEnabled.onNext(true);

        selectedMinecraftVersionFlow.onNext(Optional.empty());

        selectedMinecraftVersionFlow
                .observeOn(Schedulers.computation())
                .subscribe(selectedVersion -> {
                    if (selectedVersion.isPresent()) {
                        try {
                            final VersionManifestVersion version = MinecraftManager
                                    .getMinecraftVersion(selectedVersion.get());
                            createInstanceEnabled.onNext(true);
                            createServerEnabled.onNext(version.hasServer());
                        } catch (InvalidMinecraftVersion ignored) {
                            createInstanceEnabled.onNext(false);
                            createServerEnabled.onNext(false);
                        }
                    } else {
                        createInstanceEnabled.onNext(false);
                        createServerEnabled.onNext(false);
                    }
                });

        combineLatest(selectedLoaderType, selectedMinecraftVersionFlow, Pair::new)
                .observeOn(Schedulers.io())
                .subscribe(optionalOptionalPair -> {
                    Optional<LoaderType> loaderTypeOptional = optionalOptionalPair.left();
                    Optional<String> selectedMinecraftVersionOptional = optionalOptionalPair.right();

                    if (!selectedMinecraftVersionOptional.isPresent()) {
                        return;
                    }

                    if (!loaderTypeOptional.isPresent()) {
                        // update the name and description fields if they're not dirty
                        updateNameAndDescription(selectedMinecraftVersionOptional.get(), null);
                        loaderVersions.onNext(Optional.empty());
                        setLoaderGroupEnabled(true);
                        selectedLoaderVersion.onNext(Optional.empty());
                        loaderVersionsDropDownEnabled.onNext(false);
                        return;
                    }
                    LoaderType loaderType = loaderTypeOptional.get();
                    String selectedMinecraftVersion = selectedMinecraftVersionOptional.get();

                    updateNameAndDescription(selectedMinecraftVersion, loaderType);

                    loaderLoading.onNext(true);
                    loaderTypeNoneEnabled.onNext(false);
                    loaderTypeQuiltEnabled.onNext(false);
                    loaderTypeFabricEnabled.onNext(false);
                    loaderTypeForgeEnabled.onNext(false);
                    loaderTypeLegacyFabricEnabled.onNext(false);
                    loaderTypeNeoForgeEnabled.onNext(false);
                    loaderTypePaperEnabled.onNext(false);
                    loaderTypePurpurEnabled.onNext(false);
                    loaderVersionsDropDownEnabled.onNext(false);

                    setLoaderGroupEnabled(false);

                    // Legacy Forge doesn't support servers easily
                    final boolean enableCreateServers = (loaderType == LoaderType.FORGE || !Utils.matchVersion(
                            selectedMinecraftVersion, "1.5", true, true));
                    final List<LoaderVersion> loaders = loadLoaderVersions(loaderType, selectedMinecraftVersion);

                    loaderVersions.onNext(Optional.of(loaders));

                    if (!loaders.isEmpty()) {
                        if (loaderType == LoaderType.FORGE) {
                            Optional<LoaderVersion> optionalLoaderType = first(loaders, it -> it.recommended);
                            if (optionalLoaderType.isPresent()) {
                                selectedLoaderVersion.onNext(optionalLoaderType);
                            } else {
                                selectedLoaderVersion.onNext(loaders.stream().findFirst());
                            }
                        } else {
                            selectedLoaderVersion.onNext(loaders.stream().findFirst());
                        }
                    }

                    boolean hasLoaderVersions = !loaders.isEmpty() && loaders.get(0) != noLoaderVersions;

                    setLoaderGroupEnabled(hasLoaderVersions, hasLoaderVersions && enableCreateServers,
                            hasLoaderVersions && loaderType != LoaderType.PAPER && loaderType != LoaderType.PURPUR);
                    loaderTypeNoneEnabled.onNext(true);
                    loaderTypeQuiltEnabled.onNext(true);
                    loaderTypeFabricEnabled.onNext(true);
                    loaderTypeForgeEnabled.onNext(true);
                    loaderTypeLegacyFabricEnabled.onNext(true);
                    loaderTypeNeoForgeEnabled.onNext(true);
                    loaderTypePaperEnabled.onNext(true);
                    loaderTypePurpurEnabled.onNext(true);
                });
    }

    private static <K, V> long count(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream().filter(predicate).count();
    }

    private static <T> Optional<T> first(@Nonnull List<T> list, @Nonnull Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
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
    public Observable<Optional<String>> createInstanceDisabledReason() {
        return createInstanceDisabledReason.observeOn(SwingSchedulers.edt());
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
    public Observable<Boolean> loaderTypeNeoForgeEnabled() {
        return loaderTypeNeoForgeEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypePaperEnabled() {
        return loaderTypePaperEnabled.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypePurpurEnabled() {
        return loaderTypePurpurEnabled.observeOn(SwingSchedulers.edt());
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
    public Boolean showNeoForgeOption() {
        return showNeoForgeOption;
    }

    @Override
    public Boolean showPaperOption() {
        return showPaperOption;
    }

    @Override
    public Boolean showPurpurOption() {
        return showPurpurOption;
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
    public Observable<Boolean> loaderTypeNeoForgeSelected() {
        return loaderTypeNeoForgeSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypePaperSelected() {
        return loaderTypePaperSelected.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> loaderTypePurpurSelected() {
        return loaderTypePurpurSelected.observeOn(SwingSchedulers.edt());
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
    public Observable<Boolean> isNeoForgeVisible() {
        return isNeoForgeVisible.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isPaperVisible() {
        return isPaperVisible.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<Boolean> isPurpurVisible() {
        return isPurpurVisible.observeOn(SwingSchedulers.edt());
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
        if (AccountManager.getSelectedAccount() == null) {
            DialogManager dialog = DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"));

            if (isServer) {
                dialog.setContent(GetText.tr("Cannot create server as you have no account selected."));
            } else {
                dialog.setContent(GetText.tr("Cannot create instance as you have no account selected."));
            }

            dialog.setType(DialogManager.ERROR).show();
            if (AccountManager.getAccounts().isEmpty()) {
                App.navigate(UIConstants.LAUNCHER_ACCOUNTS_TAB);
            }
            return;
        }

        final Installable installable;
        try {
            final @Nullable LoaderVersion selectedLoaderVersion = this.selectedLoaderVersion.getValue().orElse(null);
            final Optional<String> selectedMinecraftVersionOptional = this.selectedMinecraftVersionFlow.getValue();

            if (!selectedMinecraftVersionOptional.isPresent()) {
                return;
            }

            final @Nonnull String selectedMinecraftVersion = selectedMinecraftVersionOptional.get();
            final @Nullable String description = this.description.getValue().orElse(null);
            final @Nullable String name = this.name.getValue().orElse(null);

            installable = new VanillaInstallable(
                    MinecraftManager.getMinecraftVersion(selectedMinecraftVersion),
                    selectedLoaderVersion,
                    description);
            installable.instanceName = name;
            installable.isReinstall = false;
            installable.isServer = isServer;
            final boolean success = installable.startInstall();

            if (success) {
                final String defaultNameField;
                if (selectedLoaderVersion == null) {
                    defaultNameField = String.format("Minecraft %s", selectedMinecraftVersion);
                } else {
                    defaultNameField = String.format("Minecraft %s with %s", selectedMinecraftVersion,
                            selectedLoaderVersion.type);
                }

                nameDirty = false;
                this.name.onNext(Optional.of(defaultNameField));

                descriptionDirty = false;
                this.description.onNext(Optional.of(defaultNameField));
            }
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }
    }

    private boolean isNameDirty() {
        return !(Objects.equals(name.getValue().orElse(null),
                String.format("Minecraft %s", selectedMinecraftVersionFlow.getValue().orElse(null))) ||
                (selectedLoaderType.getValue().isPresent() &&
                        Objects.equals(name.getValue().orElse(null),
                                String.format("Minecraft %s with %s",
                                        selectedMinecraftVersionFlow.getValue().orElse(null),
                                        selectedLoaderType.getValue().orElse(null)))));
    }

    /**
     * Separate thread to check if the name is dirty or not.
     * This is because setName needs to be as fast as possible.
     */
    private Thread nameCheckDirtyThread = null;

    @Override
    public void setName(String name) {
        this.name.onNext(Optional.of(name));
        if (nameCheckDirtyThread == null || !nameCheckDirtyThread.isAlive()) {
            nameCheckDirtyThread = new Thread(() -> nameDirty = isNameDirty());
            nameCheckDirtyThread.start();
        }
    }

    private boolean isDescriptionDirty() {
        return !(Objects.equals(description.getValue().orElse(null),
                String.format("Minecraft %s", selectedMinecraftVersionFlow.getValue().orElse(null))) ||
                (selectedLoaderType.getValue().isPresent() &&
                        Objects.equals(description.getValue().orElse(null),
                                String.format("Minecraft %s with %s",
                                        selectedMinecraftVersionFlow.getValue().orElse(null),
                                        selectedLoaderType.getValue().orElse(null)))));
    }

    /**
     * Separate thread to check if the description is dirty or not.
     * This is because setDescription needs to be as fast as possible.
     */
    private Thread descCheckDirtyThread = null;

    @Override
    public void setDescription(String description) {
        this.description.onNext(Optional.of(description));
        if (descCheckDirtyThread == null || !descCheckDirtyThread.isAlive()) {
            descCheckDirtyThread = new Thread(() -> descriptionDirty = isDescriptionDirty());
            descCheckDirtyThread.start();
        }
    }

    @Override
    public void setReleaseSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(
                minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.RELEASE, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    @Override
    public void setExperimentSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(
                minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.EXPERIMENT, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    @Override
    public void setSnapshotSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(
                minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.SNAPSHOT, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    @Override
    public void setOldAlphaSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(
                minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.OLD_ALPHA, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    @Override
    public void setOldBetaSelected(Boolean b) {
        final HashMap<VersionManifestVersionType, Boolean> map = new HashMap<>(
                minecraftVersionTypeFiltersPublisher.getValue());
        map.put(VersionManifestVersionType.OLD_BETA, b);
        minecraftVersionTypeFiltersPublisher.onNext((HashMap) map.clone());
    }

    @Override
    public void setSelectedMinecraftVersion(@Nullable String newVersion) {
        selectedMinecraftVersionFlow.onNext(Optional.ofNullable(newVersion));
    }

    @Override
    public void setLoaderType(@Nullable LoaderType loader) {
        selectedLoaderType.onNext(Optional.ofNullable(loader));
    }

    @Override
    public void setLoaderVersion(@Nonnull LoaderVersion loaderVersion) {
        Optional<LoaderVersion> currentOptional = selectedLoaderVersion.getValue();
        // Do not push two of the same?
        if (currentOptional.isPresent()) {
            if (currentOptional.get() == loaderVersion)
                return;
        }
        selectedLoaderVersion.onNext(Optional.of(loaderVersion));
    }

    @Override
    public Observable<Integer> selectedLoaderVersionIndex() {
        return selectedLoaderVersionIndex.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void createServer() {
        install(true);
    }

    @Override
    public void createInstance() {
        install(false);
    }

    @Override
    public void onSettingsSaved() {
        font.onNext(App.THEME.getBoldFont());
    }

    @Override
    public Boolean warnUserAboutServer() {
        return InstanceManager.getInstances().isEmpty();
    }

    private List<LoaderVersion> loadLoaderVersions(LoaderType selectedLoader,
            @Nonnull String selectedMinecraftVersion) {
        try {
            ApolloQueryCall<GetLoaderVersionsForMinecraftVersionQuery.Data> call = GraphqlClient.apolloClient.query(
                    new GetLoaderVersionsForMinecraftVersionQuery(
                            selectedMinecraftVersion))
                    .toBuilder().httpCachePolicy(
                            new HttpCachePolicy.Policy(
                                    HttpCachePolicy.FetchStrategy.CACHE_FIRST, 5, TimeUnit.MINUTES, false))
                    .build();

            Response<GetLoaderVersionsForMinecraftVersionQuery.Data> response = Rx3Apollo.from(call).blockingFirst();

            final ArrayList<LoaderVersion> loaderVersionsList = new ArrayList<>();
            final GetLoaderVersionsForMinecraftVersionQuery.Data data = response.getData();

            if (data != null)
                switch (selectedLoader) {
                    case FABRIC:
                        loaderVersionsList.addAll(data.loaderVersions().fabric().stream()
                                .filter(fv -> !disabledFabricVersions.contains(fv.version()))
                                .map(version -> new LoaderVersion(version.version(), false, "Fabric"))
                                .collect(Collectors.toList()));
                        break;

                    case FORGE:
                        loaderVersionsList.addAll(data.loaderVersions().forge().stream()
                                .filter(fv -> !disabledForgeVersions.contains(fv.version()))
                                .map(version -> {
                                    final LoaderVersion lv = new LoaderVersion(
                                            version.version(), version.rawVersion(), version.recommended(), "Forge");
                                    if (version.installerSha1Hash() != null && version.installerSize() != null) {
                                        lv.downloadables.put("installer", new Pair<>(
                                                version.installerSha1Hash(), version.installerSize().longValue()));
                                    }
                                    if (version.universalSha1Hash() != null && version.universalSize() != null) {
                                        lv.downloadables.put("universal", new Pair<>(
                                                version.universalSha1Hash(), version.universalSize().longValue()));
                                    }
                                    if (version.clientSha1Hash() != null && version.clientSize() != null) {
                                        lv.downloadables.put("client", new Pair<>(
                                                version.clientSha1Hash(), version.clientSize().longValue()));
                                    }
                                    if (version.serverSha1Hash() != null && version.serverSize() != null) {
                                        lv.downloadables.put("server", new Pair<>(
                                                version.serverSha1Hash(), version.serverSize().longValue()));
                                    }
                                    return lv;
                                }).collect(Collectors.toList()));
                        break;

                    case LEGACY_FABRIC:
                        loaderVersionsList.addAll(data.loaderVersions().legacyfabric()
                                .stream()
                                .filter(fv -> !disabledLegacyFabricVersions.contains(fv.version()))
                                .map(version -> new LoaderVersion(
                                        version.version(),
                                        false,
                                        "LegacyFabric"))
                                .collect(Collectors.toList()));
                        break;

                    case NEOFORGE:
                        loaderVersionsList.addAll(data.loaderVersions().neoforge()
                                .stream()
                                .filter(fv -> !disabledNeoForgeVersions.contains(fv.version()))
                                .map(version -> {
                                    LoaderVersion lv = new LoaderVersion(
                                            version.version(),
                                            false,
                                            "NeoForge");
                                    lv.rawVersion = version.rawVersion();
                                    return lv;
                                })
                                .collect(Collectors.toList()));
                        break;

                    case PAPER:
                        loaderVersionsList.addAll(data.loaderVersions().paper()
                                .stream()
                                .filter(fv -> !disabledPaperVersions.contains(Integer.toString(fv.build())))
                                .map(version -> new LoaderVersion(
                                        Integer.toString(version.build()),
                                        false,
                                        "Paper"))
                                .collect(Collectors.toList()));
                        break;

                    case PURPUR:
                        loaderVersionsList.addAll(data.loaderVersions().purpur()
                                .stream()
                                .filter(fv -> !disabledPurpurVersions.contains(Integer.toString(fv.build())))
                                .map(version -> new LoaderVersion(
                                        Integer.toString(version.build()),
                                        false,
                                        "Purpur"))
                                .collect(Collectors.toList()));
                        break;

                    case QUILT:
                        loaderVersionsList.addAll(data.loaderVersions().quilt().stream()
                                .filter(fv -> !disabledQuiltVersions.contains(fv.version()))
                                .map(version -> new LoaderVersion(version.version(), false, "Quilt"))
                                .collect(Collectors.toList()));
                        break;
                }
            if (loaderVersionsList.isEmpty()) {
                setLoaderGroupEnabled(false);
                return singletonList(noLoaderVersions);
            }
            return loaderVersionsList;
        } catch (RuntimeException e) {
            LogManager.logStackTrace("Error fetching loading versions", e);
            setLoaderGroupEnabled(false);
            return singletonList(errorLoadingVersions);
        }
    }

    private void setLoaderGroupEnabled(Boolean enabled) {
        setLoaderGroupEnabled(enabled, enabled);
    }

    private void setLoaderGroupEnabled(Boolean enabled, Boolean enableCreateServers) {
        setLoaderGroupEnabled(enabled, enableCreateServers, enabled);
    }

    private void setLoaderGroupEnabled(Boolean enabled, Boolean enableCreateServers, Boolean enableCreateInstances) {
        loaderTypeNoneEnabled.onNext(true);
        loaderTypeFabricEnabled.onNext(true);
        loaderTypeForgeEnabled.onNext(true);
        loaderTypeLegacyFabricEnabled.onNext(true);
        loaderTypeNeoForgeEnabled.onNext(true);
        loaderTypePaperEnabled.onNext(true);
        loaderTypePurpurEnabled.onNext(true);
        loaderTypeQuiltEnabled.onNext(true);

        createServerEnabled.onNext(enableCreateServers);
        createInstanceEnabled.onNext(enableCreateInstances);
        createInstanceDisabledReason
                .onNext(enableCreateInstances ? Optional.empty()
                        : Optional.of(GetText.tr("Disabled as the loader you selected only supports servers")));
        loaderVersionsDropDownEnabled.onNext(enabled);
    }

    /**
     * Update the name and description fields if they're not dirty with loader type
     * information
     */
    private void updateNameAndDescription(
            @Nonnull String selectedMinecraftVersion,
            @Nullable LoaderType selectedLoader) {
        final String defaultNameField;

        if (selectedLoader == null) {
            defaultNameField = String.format("Minecraft %s", selectedMinecraftVersion);
        } else {
            defaultNameField = String.format("Minecraft %s with %s", selectedMinecraftVersion, selectedLoader);
        }

        if (!name.getValue().isPresent() || name.getValue().get().isEmpty() || !nameDirty) {
            nameDirty = false;
            name.onNext(Optional.of(defaultNameField));
        }
        if (!description.getValue().isPresent() || description.getValue().get().isEmpty() || !descriptionDirty) {
            descriptionDirty = false;
            description.onNext(Optional.of(defaultNameField));
        }
    }
}