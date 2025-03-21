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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.models.InstanceUIModel;
import com.atlauncher.managers.CurseForgeUpdateManager;
import com.atlauncher.managers.FTBUpdateManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.ModrinthModpackUpdateManager;
import com.atlauncher.managers.TechnicModpackUpdateManager;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class InstancesTabViewModel implements IInstancesTabViewModel, SettingsListener {

    private final BehaviorSubject<String> instanceTitleFormat = BehaviorSubject
            .createDefault(App.settings.instanceTitleFormat);

    private final BehaviorSubject<Optional<String>> searchQuery = BehaviorSubject.createDefault(Optional.empty());

    private final Observable<Optional<Pattern>> searchPattern = searchQuery.map(queryOptional -> queryOptional
            .map(query -> Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)));

    private final BehaviorSubject<InstanceSortingStrategies> sortingStrategy = BehaviorSubject
            .createDefault(App.settings.defaultInstanceSorting);

    /**
     * First filter out instances.
     * <p>
     * This is the first operation, as it lowers the computation time for further
     * operations.
     */
    @SuppressWarnings("unchecked") // it works trust me
    public Observable<List<Instance>> filteredInstances = Observable.combineLatestArray(
            new ObservableSource[] { InstanceManager.getInstancesObservable(), searchPattern, sortingStrategy },
            it -> {
                List<Instance> instancesSorted = (List<Instance>) it[0];
                Optional<Pattern> selectedSearchPattern = (Optional<Pattern>) it[1];
                InstanceSortingStrategy selectedSortingStrategy = (InstanceSortingStrategy) it[2];

                Stream<Instance> stream = instancesSorted.stream();

                if (selectedSearchPattern.isPresent()) {
                    stream = stream.filter(createSearchFilter(selectedSearchPattern.get()));
                }

                if (selectedSortingStrategy != null) {
                    stream = stream.sorted(selectedSortingStrategy);
                }

                return stream.collect(Collectors.toList());
            }).subscribeOn(Schedulers.io());

    /**
     * second operation is to derive if they have an update or not
     */
    private final Observable<List<InstanceUIModel>> instanceModels = filteredInstances.flatMap(instances -> {
        if (instances.isEmpty())
            return Observable.just(Collections.emptyList());

        return Observable.combineLatest(
                instances.stream()
                        .map(instance -> getHasUpdateObservable(instance)
                                .map(hasUpdate -> new InstanceUIModel(instance, hasUpdate)))
                        .collect(Collectors.toList()),
                objects -> {
                    ArrayList<InstanceUIModel> models = new ArrayList<>();
                    for (Object obj : objects) {
                        if (obj instanceof InstanceUIModel) {
                            models.add((InstanceUIModel) obj);
                        } else if (obj instanceof ArrayList) {
                            models.addAll((ArrayList<InstanceUIModel>) obj);
                        }
                    }
                    return models;
                });
    });

    /**
     * Third operation is to create a UI state object.
     */
    public Flowable<InstancesList> instancesList = Observable.combineLatest(instanceModels, instanceTitleFormat,
            InstancesList::new)
            .throttleLatest(100, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST) // Backpressure first, as down stream is the edt thread
            .observeOn(Schedulers.newThread());

    public InstancesTabViewModel() {
        SettingsManager.addListener(this);
    }

    private static Predicate<Instance> createSearchFilter(final Pattern searchPattern) {
        return (val) -> searchPattern.matcher(val.launcher.name).find();
    }

    @Override
    public void setSort(@Nonnull InstanceSortingStrategies strategy) {
        setIsLoading(true);
        sortingStrategy.onNext(strategy);
    }

    @Nonnull
    @Override
    public InstanceSortingStrategies getSort() {
        return sortingStrategy.getValue();
    }

    @Override
    public void setSearch(@Nullable String search) {
        setIsLoading(true);
        searchQuery.onNext(Optional.ofNullable(search));
    }

    @Nullable
    @Override
    public String getSearch() {
        return searchQuery.getValue().orElse(null);
    }

    @Nonnull
    @Override
    public Flowable<InstancesList> getInstancesList() {
        return instancesList;
    }

    private int scrollValue = 0;

    @Override
    public void setScroll(int value) {
        scrollValue = value;
    }

    @Override
    public int getScroll() {
        return scrollValue;
    }

    private final BehaviorSubject<Boolean> isLoadingSubject = BehaviorSubject.createDefault(true);

    @Override
    public Observable<Boolean> getIsLoading() {
        return isLoadingSubject.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setIsLoading(boolean isLoading) {
        isLoadingSubject.onNext(isLoading);
    }

    @Override
    public void onSettingsSaved() {
        instanceTitleFormat.onNext(App.settings.instanceTitleFormat);
    }

    /**
     * @param instance Instance to check for
     * @return an Observable that tells you if the instance has an update or not
     */
    public Observable<Boolean> getHasUpdateObservable(Instance instance) {
        if (instance.launcher.vanillaInstance) {
            // must be reinstalled
            return BehaviorSubject.createDefault(false);
        } else if (instance.isExternalPack()) {
            if (instance.isFTBPack()) {
                return FTBUpdateManager.getObservable(instance).map(latestVersion -> latestVersion.isPresent()
                        && latestVersion.get().id != instance.launcher.ftbPackVersionManifest.id);
            } else if (instance.isCurseForgePack()) {
                return CurseForgeUpdateManager.getObservable(instance).map(latestVersion -> latestVersion.isPresent()
                        && latestVersion.get().id != instance.launcher.curseForgeFile.id);
            } else if (instance.isTechnicPack()) {
                if (instance.isTechnicSolderPack()) {
                    return TechnicModpackUpdateManager.getSolderObservable(instance)
                            .map(latestVersion -> latestVersion.isPresent()
                                    && latestVersion.get().latest != null
                                    && !latestVersion.get().latest.equals(instance.launcher.version));
                } else {
                    return TechnicModpackUpdateManager.getObservable(instance)
                            .map(latestVersion -> latestVersion.isPresent()
                                    && latestVersion.get().version != null
                                    && !latestVersion.get().version.equals(instance.launcher.version));
                }
            } else if (instance.isModrinthPack()) {
                return ModrinthModpackUpdateManager.getObservable(instance)
                        .map(latestVersion -> latestVersion.isPresent()
                                && latestVersion.get().id != null
                                && !latestVersion.get().id.equals(instance.launcher.modrinthVersion.id));
            }
        } else {
            Pack pack = instance.getPack();

            if (pack != null) {
                if (pack.hasVersions() && !instance.launcher.isDev) {
                    // Lastly check if the current version we installed is different than the latest
                    // version of the Pack and that the latest version of the Pack is not restricted
                    // to disallow updates.
                    if (!pack.getLatestVersion().version.equalsIgnoreCase(instance.launcher.version)
                            && !pack.isLatestVersionNoUpdate()) {
                        return BehaviorSubject.createDefault(true);
                    }
                }

                if (instance.launcher.isDev && (instance.launcher.hash != null)) {
                    PackVersion devVersion = pack.getDevVersionByName(instance.launcher.version);
                    if (devVersion != null && !devVersion.hashMatches(instance.launcher.hash)) {
                        return BehaviorSubject.createDefault(true);
                    }
                }
            }
        }

        return BehaviorSubject.createDefault(false);
    }
}
