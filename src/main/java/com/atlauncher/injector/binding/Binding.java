package com.atlauncher.injector.binding;

import com.atlauncher.injector.Linker;

public interface Binding<T>{
    public void link(Linker linker);
    public T get();
}