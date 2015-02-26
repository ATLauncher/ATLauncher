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

package com.atlauncher.reporter;

import com.atlauncher.Gsons;

@SuppressWarnings("unused")
public final class GithubIssue {
    private final String title;
    private final String body;
    private final String[] labels;

    public GithubIssue(String title, String body) {
        this.title = title;
        this.body = body;
        this.labels = new String[]{"Bug(s)"};
    }

    @Override
    public String toString() {
        return Gsons.DEFAULT.toJson(this);
    }
}