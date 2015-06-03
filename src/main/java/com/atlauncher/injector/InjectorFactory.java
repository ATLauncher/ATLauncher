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

import java.util.Arrays;
import java.util.Collection;

public final class InjectorFactory {
    public static Injector createInjector(Module... modules) {
        return new InjectorImpl(Arrays.asList(modules));
    }

    private static final class InjectorImpl implements Injector {
        private final Linker linker = new Linker();

        private InjectorImpl(Iterable<Module> modules) {
            for (Module mod : modules) {
                mod.configure();
                mod.putBindings(this.linker);
            }
        }

        @Override
        public <T> T getInstance(Key<T> tKey) {
            return this.linker.requestBinding(tKey).get();
        }

        @Override
        public <T> T getInstance(Class<T> tClass) {
            return this.linker.requestBinding(Key.get(tClass)).get();
        }

        @Override
        public boolean hasBinding(Key<?> tKey) {
            return this.linker.bindings.containsKey(tKey);
        }

        @Override
        public boolean hasBinding(Class<?> tClass) {
            return this.linker.bindings.containsKey(Key.get(tClass));
        }

        @Override
        public Collection<Binding<?>> getBindings() {
            return this.linker.bindings.values();
        }
    }
}