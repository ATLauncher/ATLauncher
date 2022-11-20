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

import com.atlauncher.data.Server;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.viewmodel.base.IServersTabViewModel;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 19 / 11 / 2022
 */
public class ServersTabViewModel implements IServersTabViewModel, ServerManager.Listener {
    private List<Server> sourceServers = ServerManager.getServersSorted();

    private Consumer<List<Server>> onChangeViewListener;

    private String search = null;
    private Consumer<String> onSearchChangeListener;

    public ServersTabViewModel() {
        ServerManager.addListener(this);
    }

    @Override
    public void addOnChangeViewListener(Consumer<List<Server>> consumer) {
        onChangeViewListener = consumer;
        post();
    }

    @Override
    public void setSearch(String search) {
        this.search = search;
        onSearchChangeListener.accept(search);
        post();
    }

    @Override
    public void addOnSearchChangeListener(Consumer<String> consumer) {
        consumer.accept(search);
        onSearchChangeListener = consumer;
    }

    private int currentPosition = 0;
    private Consumer<Integer> onViewPositionChangedListener;

    @Override
    public void setViewPosition(int position) {
        currentPosition = position;
    }

    @Override
    public void addOnViewPositionChangedListener(Consumer<Integer> consumer) {
        onViewPositionChangedListener = consumer;
        onViewPositionChangedListener.accept(currentPosition);
    }

    private void post() {
        List<Server> mutatedServers = sourceServers.stream().filter(server -> {
            if (search != null)
                return Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(server.name).find();
            else return true;
        }).collect(Collectors.toList());

        onChangeViewListener.accept(mutatedServers);
        if (onViewPositionChangedListener != null)
            onViewPositionChangedListener.accept(currentPosition);
    }

    @Override
    public void onServersChanged() {
        sourceServers = ServerManager.getServersSorted();
        post();
    }
}
