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

import com.atlauncher.data.Server;

import java.util.List;
import java.util.function.Consumer;

/**
 * 19 / 11 / 2022
 */
public interface IServersTabViewModel {
    /**
     * Whenever the view changes, this listener will be invoked with new data
     *
     * @param consumer listener to invoke
     */
    void addOnChangeViewListener(Consumer<List<Server>> consumer);

    void setSearch(String search);

    void addOnSearchChangeListener(Consumer<String> consumer);

    /**
     * Used to save the position the user was looking at
     *
     * @param position position the user was at
     */
    void setViewPosition(int position);

    /**
     * Watch when the position is changed, this is used to restore the view.
     * <p>
     * Note, that the consumer is only called
     *  after a change to search or servers.
     *
     * @param consumer consumer to take new scroll position
     */
    void addOnViewPositionChangedListener(Consumer<Integer> consumer);
}
