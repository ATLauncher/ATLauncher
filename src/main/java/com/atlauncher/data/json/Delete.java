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
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;
import com.atlauncher.data.Instance;

import java.nio.file.Path;

@Json
public class Delete {
    private String base;
    private String target;

    public String getBase() {
        return this.base;
    }

    public String getTarget() {
        return this.target;
    }

    public boolean isValid() {
        return !this.base.equalsIgnoreCase("root") || !(this.target.startsWith("world") || this.target.startsWith
                ("DIM") || this.target.startsWith("saves") || this.target.startsWith("instance.json") || this.target
                .contains("./") || this.target.contains(".\\") || this.target.contains("~/") || this.target.contains
                ("~\\"));
    }

    public Path getFile(Instance instance) {
        return instance.getRootDirectory().resolve(this.target.replace("%s%", "/"));
    }
}
