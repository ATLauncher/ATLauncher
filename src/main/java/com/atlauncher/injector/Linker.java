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
import com.atlauncher.injector.binding.ReflectiveInjectionBinding;

import java.util.HashMap;
import java.util.Map;

public final class Linker {
    protected final Map<Key<?>, Binding<?>> bindings = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Binding<T> requestBinding(Key<T> tKey) {
        Binding<T> tBinding = (Binding<T>) this.bindings.get(tKey);
        if (tBinding != null) {
            tBinding.link(this);
            return tBinding;
        }

        if (tKey.annotations != null && tKey.annotations.length > 0) {
            throw new CreationException("Cannot create binding for " + tKey.rawType.getName());
        } else {
            tBinding = (Binding<T>) ReflectiveInjectionBinding.create(tKey.rawType);
        }

        tBinding.link(this);
        return tBinding;
    }
}