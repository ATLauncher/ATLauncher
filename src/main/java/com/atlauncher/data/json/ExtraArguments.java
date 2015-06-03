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
import com.atlauncher.utils.validator.DependencyValidator;
import com.atlauncher.utils.validator.GroupValidator;

@Json
public class ExtraArguments {
    private String arguments;
    private String depends;
    private String dependsGroup;

    public String getArguments() {
        return this.arguments;
    }

    public String getDepends() {
        return this.depends;
    }

    public String getDependsGroup() {
        return this.dependsGroup;
    }

    public GroupValidator groupValidator() {
        return new GroupValidator(this.dependsGroup);
    }

    public DependencyValidator dependencyValidator() {
        return new DependencyValidator(this.depends);
    }

    public boolean hasDepends() {
        return this.depends != null;
    }

    public boolean hasDependsGroup() {
        return this.dependsGroup != null;
    }
}