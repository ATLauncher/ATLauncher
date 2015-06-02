/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

package com.atlauncher;

import com.atlauncher.adapter.ColorTypeAdapter;
import com.atlauncher.adapter.HashCodeAdapter;
import com.atlauncher.data.Server;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.atlauncher.utils.Hashing;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.awt.Color;
import java.io.File;
import java.net.Proxy;
import java.util.Date;

public final class Gsons {
    public static final Gson DEFAULT = new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .registerTypeAdapter(Hashing.HashCode.class, new HashCodeAdapter()).setPrettyPrinting().create();

    public static final Gson THEMES = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Color.class, new
            ColorTypeAdapter()).create();

    public static final Gson DEFAULT_ALT = new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .registerTypeAdapter(Date.class, new DateTypeAdapter()).registerTypeAdapter(File.class, new
                    FileTypeAdapter()).create();

    public static final JsonParser PARSER = new JsonParser();
}
