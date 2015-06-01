package com.atlauncher.injector;

import com.atlauncher.injector.binding.Binding;

import java.util.Arrays;
import java.util.Collection;

public final class InjectorFactory{
    public static Injector createInjector(Module... modules){
        return new InjectorImpl(Arrays.asList(modules));
    }

    private static final class InjectorImpl
    implements Injector{
        private final Linker linker = new Linker();

        private InjectorImpl(Iterable<Module> modules){
            for(Module mod : modules){
                mod.configure();
                mod.putBindings(this.linker);
            }
        }

        @Override
        public <T> T getInstance(Key<T> tKey) {
            return this.linker.requestBinding(tKey).get();
        }

        @Override
        public <T> T getInstance(Class<T> tClass) {
            return this.linker.requestBinding(Key.get(tClass)).get();
        }

        @Override
        public boolean hasBinding(Key<?> tKey) {
            return this.linker.bindings.containsKey(tKey);
        }

        @Override
        public boolean hasBinding(Class<?> tClass) {
            return this.linker.bindings.containsKey(Key.get(tClass));
        }

        @Override
        public Collection<Binding<?>> getBindings() {
            return this.linker.bindings.values();
        }
    }
}