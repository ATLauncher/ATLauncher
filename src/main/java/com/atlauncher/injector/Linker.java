package com.atlauncher.injector;

import com.atlauncher.injector.binding.Binding;
import com.atlauncher.injector.binding.ReflectiveInjectionBinding;

import java.util.HashMap;
import java.util.Map;

public final class Linker{
    protected final Map<Key<?>, Binding<?>> bindings = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Binding<T> requestBinding(Key<T> tKey){
        Binding<T> tBinding = (Binding<T>) this.bindings.get(tKey);
        if(tBinding != null){
            tBinding.link(this);
            return tBinding;
        }

        if(tKey.annotations != null && tKey.annotations.length > 0){
            throw new CreationException("Cannot create binding for " + tKey.rawType.getName());
        } else{
            tBinding = (Binding<T>) ReflectiveInjectionBinding.create(tKey.rawType);
        }

        tBinding.link(this);
        return tBinding;
    }
}