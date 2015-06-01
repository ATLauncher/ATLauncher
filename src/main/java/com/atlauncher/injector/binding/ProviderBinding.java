package com.atlauncher.injector.binding;

import com.atlauncher.injector.Linker;

import javax.inject.Provider;

public final class ProviderBinding<T>
implements Binding<T>{
    private final Provider<T> provider;

    public ProviderBinding(Provider<T> provider){
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