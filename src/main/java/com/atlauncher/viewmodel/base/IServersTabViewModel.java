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
import java.util.Optional;

import javax.annotation.Nullable;

import com.atlauncher.data.Server;

import io.reactivex.rxjava3.core.Observable;

/**
 * 19 / 11 / 2022
 */
public interface IServersTabViewModel {
    /**
     * Get observable list to subscribe to.
     */
    Observable<List<Server>> getServersObservable();

    /**
     * Get observable of text to subscribe to.
     *
     * @return Observable query
     */
    Observable<Optional<String>> getSearchObservable();

    /**
     * Set what to search for.
     *
     * @param search Query, else null.
     */
    void setSearchSubject(@Nullable String search);

    /**
     * Watch when the position is changed,
     * this is used to restore the view.
     * <p>
     * Note, that the observable is only updated after a change to search or servers.
     */
    Observable<Integer> getViewPosition();

    /**
     * Used to save the position the user was looking at
     *
     * @param position position the user was at
     */
    void setViewPosition(int position);
}
