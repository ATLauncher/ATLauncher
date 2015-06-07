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
package com.atlauncher.injector.binding;

import com.atlauncher.injector.Linker;

import javax.inject.Provider;

public final class ProviderBinding<T> implements Binding<T> {
    private final Provider<T> provider;

    public ProviderBinding(Provider<T> provider) {
        this.provider = provider;
    }

    @Override
    public void link(Linker linker) {

    }

    @Override
    public T get() {
        return this.provider.get();
    }
}