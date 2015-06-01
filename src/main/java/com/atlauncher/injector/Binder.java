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
package com.atlauncher.injector;

import com.atlauncher.injector.binding.Binding;

import java.util.HashMap;
import java.util.Map;

public final class Binder {
    private final Map<Key<?>, Binding<?>> bindings = new HashMap<>();

    public BindingBuilder bind(Class<?> clazz) {
        return new BindingBuilder(this, clazz);
    }

    protected Map<Key<?>, Binding<?>> bindings() {
        return this.bindings;
    }

    protected void push(Linker linker) {
        linker.bindings.putAll(this.bindings);
    }
}