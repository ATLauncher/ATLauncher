package com.atlauncher.injector.binding;

import com.atlauncher.injector.Linker;

public final class InstanceBinding<T>
implements Binding<T>{
    private final T instance;

    public InstanceBinding(T instance){
        this.instance = instance;
    }

    @Override
    public void link(Linker linker) {

    }

    @Override
    public T get() {
        return this.instance;
    }
}