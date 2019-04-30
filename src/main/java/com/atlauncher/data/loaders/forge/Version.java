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
package com.atlauncher.data.loaders.forge;

import com.atlauncher.annot.Json;

import java.util.List;
import java.util.Map;

@Json
public class Version {
    private String id;
    private String mainClass;

    private Map<String, List<String>> arguments;
    private List<Library> libraries;

    public String getId() {
        return this.id;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public Map<String, List<String>> getArguments() {
        return this.arguments;
    }

    public List<Library> getLibraries() {
        return this.libraries;
    }
}
