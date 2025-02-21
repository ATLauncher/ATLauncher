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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.atlauncher.data.Server;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.viewmodel.base.IServersTabViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ServersTabViewModel implements IServersTabViewModel {
    private final Observable<List<Server>> sourceServers = ServerManager.getServersObservable()
            .map(servers -> servers.stream()
                    .sorted(Comparator.comparing(s -> s.name))
                    .collect(Collectors.toList()))
            .subscribeOn(Schedulers.computation());

    private final BehaviorSubject<Optional<String>> searchSubject = BehaviorSubject.createDefault(Optional.empty());

    private final BehaviorSubject<Integer> currentPositionSubject = BehaviorSubject.createDefault(0);

    private final Flowable<List<Server>> servers = Observable.combineLatest(sourceServers, searchSubject,
            (servers, searchOptional) -> servers
                    .stream()
                    .filter(server -> {
                        String search = searchOptional.orElse(null);
                        if (search != null)
                            return Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(server.name)
                                    .find();
                        else
                            return true;
                    })
                    .collect(Collectors.toList()))
            .throttleLatest(100, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST) // Backpressure first, as down stream is the edt thread
            .observeOn(SwingSchedulers.edt());

    @Override
    public Flowable<List<Server>> getServersObservable() {
        return servers;
    }

    @Override
    public Observable<Optional<String>> getSearchObservable() {
        return searchSubject.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setSearchSubject(@Nullable String search) {
        searchSubject.onNext(Optional.ofNullable(search));
    }

    @Override
    public Observable<Integer> getViewPosition() {
        return currentPositionSubject.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setViewPosition(int position) {
        currentPositionSubject.onNext(position);
    }
}
