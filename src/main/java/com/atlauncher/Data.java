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

import com.atlauncher.collection.Accounts;
import com.atlauncher.collection.Newspaper;
import com.atlauncher.data.Instance;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.data.News;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Settings;
import com.atlauncher.data.version.MinecraftVersion;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class keeps all the data used by the launcher including the packs on the launcher, users instances and more.
 */
public final class Data {
    public static final List<Instance> INSTANCES = new LinkedList<>();
    public static final Accounts ACCOUNTS = new Accounts();
    public static final List<News> NEWS = new Newspaper();
    public static final List<Pack> PACKS = new LinkedList<>();
    public static final List<MinecraftServer> CHECKING_SERVERS = new LinkedList<>();

    public static final Map<String, MinecraftVersion> MINECRAFT_VERSIONS = new HashMap<>();
}