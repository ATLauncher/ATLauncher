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

import com.atlauncher.injector.binding.InstanceBinding;
import com.atlauncher.injector.binding.ProviderBinding;
import com.atlauncher.injector.binding.ReflectiveInjectionBinding;
import com.atlauncher.injector.binding.SingletonBinding;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

public final class BindingBuilder {
    private final Binder binder;
    private final Class<?> clazz;
    private Annotation annotation;

    public BindingBuilder(Binder binder, Class<?> clazz) {
        this.binder = binder;
        this.clazz = clazz;
    }

    public BindingBuilder annotatedWith(Annotation annotation) {
        this.annotation = annotation;
        return this;
    }

    public void asSingleton() {
        if (this.annotation != null) {
            this.binder.bindings().put(Key.get(this.clazz, this.annotation), new SingletonBinding<>
                    (ReflectiveInjectionBinding.create(this.clazz)));
        } else {
            this.binder.bindings().put(Key.get(this.clazz), new SingletonBinding<>(ReflectiveInjectionBinding.create
                    (this.clazz)));
        }
    }

    public <T> void toInstance(T instance) {
        if (this.annotation != null) {
            this.binder.bindings().put(Key.get(this.clazz, this.annotation), new InstanceBinding<>(instance));
        } else {
            this.binder.bindings().put(Key.get(this.clazz), new InstanceBinding<>(instance));
        }
    }

    public <T> void toProvider(Provider<T> provider) {
        if (this.annotation != null) {
            this.binder.bindings().put(Key.get(this.clazz, this.annotation), new ProviderBinding<>(provider));
        } else {
            this.binder.bindings().put(Key.get(this.clazz), new ProviderBinding<>(provider));
        }
    }

    public <T> void to(Class<T> t) {
        if (this.annotation != null) {
            this.binder.bindings().put(Key.get(this.clazz, this.annotation), ReflectiveInjectionBinding.create(t));
        } else {
            this.binder.bindings().put(Key.get(this.clazz), ReflectiveInjectionBinding.create(t));
        }
    }
}