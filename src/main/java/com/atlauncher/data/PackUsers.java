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
package com.atlauncher.data;

import com.atlauncher.annot.Json;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;

import java.util.List;

@Json
public class PackUsers {
    private int pack;
    private List<String> testers;
    private List<String> allowedPlayers;

    public void addUsers() {
        Pack pack = null;
        try {
            pack = PackManager.getPackByID(this.pack);
        } catch (InvalidPack e) {
            LogManager.logStackTrace(e);
            return;
        }
        if (this.testers != null) {
            pack.addTesters(testers);
        }
        if (this.allowedPlayers != null) {
            pack.addAllowedPlayers(allowedPlayers);
        }
    }
}