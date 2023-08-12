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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * 20 / 11 / 2022
 */
public class InstancesTabViewModel implements IInstancesTabViewModel, SettingsListener {

    private final BehaviorSubject<String> instanceTitleFormat =
        BehaviorSubject.createDefault(App.settings.instanceTitleFormat);

    private final BehaviorSubject<Optional<Pattern>> searchPattern =
        BehaviorSubject.createDefault(Optional.empty());

    private final BehaviorSubject<InstanceSortingStrategy> sortingStrategy =
        BehaviorSubject.createDefault(App.settings.defaultInstanceSorting);

    private final Observable<List<Instance>> instancesSorted = InstanceManager.getInstancesObservable()
        .map(it ->
            it.stream()
                .sorted(Comparator.comparing(i -> i.launcher.name))
                .collect(Collectors.toList())
        ).subscribeOn(Schedulers.computation());

    public Observable<List<Instance>> instances =
        Observable.combineLatestArray(
            new ObservableSource[]{instancesSorted, searchPattern, sortingStrategy},
            it -> {
                List<Instance> instancesSorted = (List<Instance>) it[0];
                Optional<Pattern> searchPattern = (Optional<Pattern>) it[1];
                InstanceSortingStrategy sortingStrategy = (InstanceSortingStrategy) it[2];

                Stream<Instance> stream = instancesSorted.stream();

                if (searchPattern.isPresent()) {
                    stream = stream.filter(createSearchFilter(searchPattern.get()));
                }

                if (sortingStrategy != null) {
                    stream = stream.sorted(sortingStrategy);
                }

                return stream.collect(Collectors.toList());
            });

    public Observable<InstancesList> instancesList =
        Observable.combineLatest(instances, instanceTitleFormat, InstancesList::new);

    public InstancesTabViewModel() {
        SettingsManager.addListener(this);
    }

    private static Predicate<Instance> createSearchFilter(final Pattern searchPattern) {
        return (val) -> searchPattern.matcher(val.launcher.name).find();
    }

    @Override
    public void setSort(@NotNull InstanceSortingStrategy strategy) {
        sortingStrategy.onNext(strategy);
    }

    @Override
    public void setSearch(@Nullable Pattern search) {
        searchPattern.onNext(Optional.ofNullable(search));
    }

    @NotNull
    @Override
    public Observable<InstancesList> getInstancesList() {
        return instancesList;
    }

    @Override
    public void onSettingsSaved() {
        instanceTitleFormat.onNext(App.settings.instanceTitleFormat);
    }
}
