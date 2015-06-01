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

public abstract class Module {
    private final Binder binder = new Binder();

    protected abstract void configure();

    protected Binder binder() {
        return this.binder;
    }

    protected <T> BindingBuilder bind(Class<T> tClass) {
        return this.binder().bind(tClass);
    }

    protected <T> BindingBuilder bind(Key<T> tKey) {
        return this.binder().bind(tKey.rawType);
    }

    protected final void putBindings(Linker linker) {
        this.binder.push(linker);
    }
}