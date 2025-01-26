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
package com.atlauncher.data;

/**
 * Represents an contributor in AboutTab.
 */
public class Contributor {
    /**
     * Name of the contributor.
     */
    public final String name;

    /**
     * Url to the GitHub profile of the contributor.
     */
    public final String url;

    /**
     * URL to the avatar of the contributor.
     */
    public final String avatarUrl;

    /**
     * @param name     of the author
     * @param imageURL of the authors profile picture
     */
    public Contributor(String name, String url, String avatarUrl) {
        this.name = name;
        this.url = url;
        this.avatarUrl = avatarUrl;
    }
}