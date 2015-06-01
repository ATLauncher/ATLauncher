package com.atlauncher.injector;

import com.atlauncher.injector.binding.Binding;

import java.util.Collection;

public interface Injector{
    public <T> T getInstance(Key<T> tKey);
    public <T> T getInstance(Class<T> tClass);
    public boolean hasBinding(Key<?> tKey);
    public boolean hasBinding(Class<?> tClass);
    public Collection<Binding<?>> getBindings();
}