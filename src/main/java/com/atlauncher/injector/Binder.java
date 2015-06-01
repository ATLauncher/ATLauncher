package com.atlauncher.injector;

import com.atlauncher.injector.binding.Binding;

import java.util.HashMap;
import java.util.Map;

public final class Binder{
    private final Map<Key<?>, Binding<?>> bindings = new HashMap<>();

    public BindingBuilder bind(Class<?> clazz){
        return new BindingBuilder(this, clazz);
    }

    protected Map<Key<?>, Binding<?>> bindings(){
        return this.bindings;
    }

    protected void push(Linker linker){
        linker.bindings.putAll(this.bindings);
    }
}