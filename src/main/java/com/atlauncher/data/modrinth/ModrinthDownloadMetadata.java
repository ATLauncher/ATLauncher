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
package com.atlauncher.data.modrinth;

import java.util.Locale;

import javax.annotation.Nullable;

import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.google.gson.annotations.SerializedName;

public class ModrinthDownloadMetadata {
    public final Reason reason;

    @SerializedName("game_version")
    public final String gameVersion;

    public final String loader;

    public ModrinthDownloadMetadata(Reason reason, String gameVersion, String loader) {
        this.reason = reason;
        this.gameVersion = gameVersion;
        this.loader = loader;
    }

    public static ModrinthDownloadMetadata from(Reason reason, String gameVersion,
        @Nullable LoaderVersion loaderVersion) {
        return new ModrinthDownloadMetadata(reason, gameVersion, getLoader(loaderVersion));
    }

    public static ModrinthDownloadMetadata from(Reason reason, ModrinthVersion version,
        @Nullable LoaderVersion loaderVersion) {
        String gameVersion = version.gameVersions == null || version.gameVersions.isEmpty()
            ? null
            : version.gameVersions.get(0);

        return new ModrinthDownloadMetadata(reason, gameVersion, getLoader(loaderVersion, version));
    }

    public static String getLoader(@Nullable LoaderVersion loaderVersion) {
        if (loaderVersion == null) {
            return "vanilla";
        }

        if (loaderVersion.isLegacyFabric()) {
            return "fabric";
        }

        return loaderVersion.getLoaderType().name().toLowerCase(Locale.ENGLISH);
    }

    private static String getLoader(@Nullable LoaderVersion loaderVersion, ModrinthVersion version) {
        if (loaderVersion != null) {
            return getLoader(loaderVersion);
        }

        if (version.loaders == null || version.loaders.isEmpty()) {
            return "vanilla";
        }

        return version.loaders.stream()
            .filter(loader -> !loader.equalsIgnoreCase("minecraft"))
            .findFirst()
            .orElse("vanilla");
    }

    public enum Reason {
        @SerializedName("standalone")
        STANDALONE,

        @SerializedName("dependency")
        DEPENDENCY,

        @SerializedName("modpack")
        MODPACK,

        @SerializedName("update")
        UPDATE
    }
}
