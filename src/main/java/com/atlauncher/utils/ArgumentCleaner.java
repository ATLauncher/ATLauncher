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
package com.atlauncher.utils;

import java.util.Arrays;

/**
 * 23 / 12 / 2022
 * <p>
 * Class dedicated to cleaning arguments
 */
public class ArgumentCleaner {

    /**
     * Takes arguments and cleans them, removing new lines
     *
     * @param args arguments to clean
     * @return cleaned arguments
     */
    public static String[] clean(String[] args) {
        return Arrays.stream(args)
            .filter(it -> it.equals("\n"))
            .map(it -> it.replace("\n", ""))
            .toArray(String[]::new);
    }
}
