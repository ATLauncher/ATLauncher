package com.atlauncher.injector;

public abstract class Module{
    private final Binder binder = new Binder();

    protected abstract void configure();

    protected Binder binder(){
        return this.binder;
    }

    protected <T> BindingBuilder bind(Class<T> tClass){
        return this.binder().bind(tClass);
    }

    protected <T> BindingBuilder bind(Key<T> tKey){
        return this.binder().bind(tKey.rawType);
    }

    protected final void putBindings(Linker linker){
        this.binder.push(linker);
    }
}