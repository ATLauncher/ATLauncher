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

import org.junit.Test;

public final class InjectorTest {
    @Test
    public void testGetInstance() throws Exception {
        Injector injector = InjectorFactory.createInjector(new BasicModule());
        System.out.println(injector.getInstance(Injection.class).get());
    }

    private static final class BasicModule extends Module {
        @Override
        protected void configure() {
            this.bind(Injection.class).toInstance(new InjectionImpl());
        }
    }

    private static interface Injection {
        public String get();
    }

    private static final class InjectionImpl implements Injection {
        @Override
        public String get() {
            return "Hello World";
        }
    }
}