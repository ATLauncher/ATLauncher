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
package com.atlauncher.viewmodel.base;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.atlauncher.data.Instance;
import com.atlauncher.utils.sort.InstanceSortingStrategy;

import io.reactivex.rxjava3.core.Observable;

/**
 * 20 / 11 / 2022
 */
public interface IInstancesTabViewModel {
    /**
     * Set how the instances should be sorted.
     *
     * @param strategy Provided strategy
     */
    void setSort(@NotNull InstanceSortingStrategy strategy);

    /**
     * Pattern to filter the search by.
     *
     * @param search Query or null
     */
    void setSearch(@Nullable Pattern search);

    /**
     * Get an observable view state that includes title format.
     *
     * @return The view state
     */
    @Nonnull
    Observable<InstancesList> getInstancesList();

    /**
     * View state object.
     */
    class InstancesList {
        /**
         * Instances to display.
         */
        public final List<Instance> instances;

        /**
         * Title format for said instances.
         */
        public final String instanceTitleFormat;

        public InstancesList(List<Instance> instances, String instanceTitleFormat) {
            this.instances = instances;
            this.instanceTitleFormat = instanceTitleFormat;
        }
    }
}
