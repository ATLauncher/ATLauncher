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

import javax.inject.Named;
import java.io.Serializable;
import java.lang.annotation.Annotation;

public final class Names {
    public static Named named(String value) {
        return new NamedImpl(value);
    }

    private static final class NamedImpl implements Named, Serializable {
        private final String name;

        private NamedImpl(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return (127 * "value".hashCode()) ^ this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Named && ((Named) obj).value().equals(this.value());

        }

        @Override
        public String toString() {
            return "@" + Named.class.getName() + "(value=" + this.name + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}