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
package com.atlauncher.data.curseforge;

import com.google.gson.annotations.SerializedName;

public enum CurseForgeSocialLinkType {
    @SerializedName("1")
    MASTODON(1),
    @SerializedName("2")
    DISCORD(2),
    @SerializedName("3")
    WEBSITE(3),
    @SerializedName("4")
    FACEBOOK(4),
    @SerializedName("5")
    TWITTER(5),
    @SerializedName("6")
    INSTAGRAM(6),
    @SerializedName("7")
    PATREON(7),
    @SerializedName("8")
    TWITCH(8),
    @SerializedName("9")
    REDDIT(9),
    @SerializedName("10")
    YOUTUBE(10),
    @SerializedName("11")
    TIKTOK(11),
    @SerializedName("12")
    PINTEREST(12),
    @SerializedName("13")
    GITHUB(13),
    @SerializedName("14")
    BLUESKY(14);

    private final int value;

    CurseForgeSocialLinkType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
