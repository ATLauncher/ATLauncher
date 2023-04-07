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

import com.atlauncher.constants.Constants;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.viewmodel.base.IAboutTabViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * 13 / 06 / 2022
 */
public class AboutTabViewModel implements IAboutTabViewModel {
    private String info = null;

    @NotNull
    @Override
    public String[] getAuthors() {
        return AUTHORS_ARRAY;
    }

    /**
     * Optimization method to ensure about tab opens with information as fast
     * as possible.
     *
     * @return information on this launcher
     */
    @NotNull
    @Override
    public String getInfo() {
        if (info == null) {
            StringBuilder builder = new StringBuilder()
                .append(Constants.LAUNCHER_NAME)
                .append("\n")
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

    /**
     * Produced via "git shortlog -s -n --all --no-merges" then some edits
     * <p>
     * Use the following pattern to retrieve icons
     * "https://avatars.githubusercontent.com/USERNAME"
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    private static final String[] AUTHORS_ARRAY = {
        "Ryan Dowling",
        "RyanTheAllmighty",
        "doomsdayrs",
        "Asyncronous",
        "PORTB",
        "JakeJMattson",
        "Doomsdayrs",
        "Jamie (Lexteam)",
        "Ryan",
        "s0cks",
        "Jamie Mansfield",
        "flaw600",
        "Leah",
        "Alan Jenkins",
        "dgelessus",
        "Kihira",
        "Harald Krämer",
        "James Ross",
        "iarspider",
        "xz-dev",
        "Mysticpasta1",
        "Torsten Walluhn",
        "modmuss50",
        "Andrew Thurman",
        "Cassandra Caina",
        "Jamie (Lexware)",
        "Jowsey",
        "Shegorath123",
        "Tazz",
        "notfood",
        "Dallas Epperson",
        "Emma Waffle",
        "Hossam Mohsen",
        "JBMagination",
        "Jamie",
        "Laceh",
        "Mihail Yaremenko",
        "Sasha Sorokin",
        "TecCheck",
        "Trejkaz",
        "mac",
    };
}
