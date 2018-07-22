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
package com.atlauncher.data.mojang;

import java.util.List;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;

@Json
public class ArgumentRule {
    private List<Object> game;
    private List<Object> jvm;

    public List<Object> getGame() {
        return this.game;
    }

    public List<Object> getJVM() {
        return this.jvm;
    }

    public String asString() {
        String arguments = "";

        for (Object arg : this.game) {
            if (arg.getClass() == String.class) {
                arguments += " " + arg.toString();
            } else {
                LogManager.error(arg.getClass().toString());
            }
        }

        return arguments;
    }
}
