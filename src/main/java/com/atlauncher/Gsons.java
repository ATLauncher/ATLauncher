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
package com.atlauncher;

import java.awt.Color;
import java.util.Date;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.AccountTypeAdapter;
import com.atlauncher.data.ColorTypeAdapter;
import com.atlauncher.data.DateTypeAdapter;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.PackVersionTypeAdapter;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.OauthTokenResponseTypeAdapter;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.ArgumentsTypeAdapter;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LibraryTypeAdapter;
import com.atlauncher.data.minecraft.loaders.fabric.FabricMetaLauncherMeta;
import com.atlauncher.data.minecraft.loaders.fabric.FabricMetaLauncherMetaTypeAdapter;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLibrary;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLibraryTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Gsons {
    public static final Gson DEFAULT = new GsonBuilder()
            .registerTypeAdapter(AbstractAccount.class, new AccountTypeAdapter())
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(OauthTokenResponse.class, new OauthTokenResponseTypeAdapter()).setPrettyPrinting()
            .create();

    public static final Gson DEFAULT_ALT = new GsonBuilder().registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(PackVersion.class, new PackVersionTypeAdapter()).setPrettyPrinting().create();

    public static final Gson MINECRAFT = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(Library.class, new LibraryTypeAdapter())
            .registerTypeAdapter(Arguments.class, new ArgumentsTypeAdapter())
            .registerTypeAdapter(FabricMetaLauncherMeta.class, new FabricMetaLauncherMetaTypeAdapter())
            .registerTypeAdapter(ForgeLibrary.class, new ForgeLibraryTypeAdapter()).create();
}
