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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.Contributor;
import com.atlauncher.data.LauncherLibrary;
import com.atlauncher.graphql.GetLauncherContributorsQuery;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.viewmodel.base.IAboutTabViewModel;

/**
 * 13 / 06 / 2022
 */
public class AboutTabViewModel implements IAboutTabViewModel {

    private String info = null;

    @Nonnull
    @Override
    public List<Contributor> getContributors() {
        GetLauncherContributorsQuery.Data response = GraphqlClient
                            .callAndWait(new GetLauncherContributorsQuery(), 1, TimeUnit.DAYS);

        return response.about().contributors().stream().map(contributor -> {
            return new Contributor(contributor.name(), contributor.url(), contributor.avatarUrl());
        }).collect(Collectors.toList());
    }

    /**
     * Optimization via stored string to ensure about tab opens with information as fast
     * as possible.
     *
     * @return information on this launcher
     */
    @Nonnull
    @Override
    public String getInfo() {
        if (info == null) {
            StringBuilder builder = new StringBuilder()
                .append("Version:\t").append(Constants.VERSION)
                .append("\n")
                .append("OS:\t").append(System.getProperty("os.name"));

            if (OS.isUsingFlatpak())
                builder.append(" (Flatpak)");

            builder.append("\n").append("Java:\t")
                .append(String.format("Java %d (%s)",
                    Java.getLauncherJavaVersionNumber(),
                    Java.getLauncherJavaVersion()
                ));
            info = builder.toString();
        }

        return info;
    }

    @Nonnull
    @Override
    public String getCopyInfo() {
        return getInfo();
    }

    @Nonnull
    @Override
    public List<LauncherLibrary> getLibraries() {
        return new ArrayList<>();
    }
}
