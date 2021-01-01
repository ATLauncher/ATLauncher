/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.data.minecraft.loaders.forge;

import java.io.File;

import com.atlauncher.annot.Json;
import com.atlauncher.utils.Utils;

@Json
public class Data {
    private final String client;
    private final String server;

    public Data(String client, String server) {
        this.client = client;
        this.server = server;
    }

    public String getValue(boolean isClient, File libraiesDir) {
        String value = isClient ? this.client : this.server;

        char start = value.charAt(0);
        char end = value.charAt(value.length() - 1);

        if (start == '[' && end == ']') {
            return Utils.convertMavenIdentifierToFile(value.substring(1, value.length() - 1), libraiesDir)
                    .getAbsolutePath();
        }

        return value;
    }
}
