package com.atlauncher.injector.binding;

import com.atlauncher.injector.Linker;

public final class SingletonBinding<T>
implements Binding<T>{
    private static final Object UNINITIALIZED = new Object();

    private final Binding<T> delegate;
    private volatile Object instance = UNINITIALIZED;

    public SingletonBinding(Binding<T> delegate){
        this.delegate = delegate;
    }

    @Override
    public void link(Linker linker) {
        this.delegate.link(linker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if(instance == UNINITIALIZED){
            synchronized(this){
                if(instance == UNINITIALIZED){
                    instance = this.delegate.get();
                }
            }
        }

        return (T) this.instance;
    }

    @Override
    public String toString(){
        return "SingletonBinding[delegate=" + this.delegate + "]";
    }
}