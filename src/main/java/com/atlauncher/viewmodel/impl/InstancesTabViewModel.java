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
import com.atlauncher.data.Instance;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 20 / 11 / 2022
 */
public class InstancesTabViewModel implements IInstancesTabViewModel, InstanceManager.Listener {

    private List<Instance> instances = InstanceManager.getInstancesSorted();
    private Pattern searchPattern = null;
    private InstanceSortingStrategy sortingStrategy = App.settings.defaultInstanceSorting;
    private Consumer<List<Instance>> consumer = null;

    public InstancesTabViewModel() {
        InstanceManager.addListener(this);
    }

    @Override
    public void setSort(@NotNull InstanceSortingStrategy strategy) {
        sortingStrategy = strategy;
        post();
    }

    @Override
    public void setSearch(@Nullable Pattern search) {
        searchPattern = search;
        post();
    }

    @Override
    public void setOnViewChanged(@NotNull Consumer<List<Instance>> consumer) {
        this.consumer = consumer;
        post();
    }

    private static Predicate<Instance> createSearchFilter(final Pattern searchPattern) {
        return (val) -> searchPattern.matcher(val.launcher.name).find();
    }

    private void post() {
        Stream<Instance> stream = instances.stream();
        if (searchPattern != null) {
            stream = stream.filter(createSearchFilter(searchPattern));
        }

        if (sortingStrategy != null) {
            stream = stream.sorted(sortingStrategy);
        }

        if (consumer != null)
            consumer.accept(stream.collect(Collectors.toList()));
    }

    @Override
    public void onInstancesChanged() {
        instances = InstanceManager.getInstancesSorted();
        post();
    }
}
