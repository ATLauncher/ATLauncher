package com.atlauncher.injector.binding;

import com.atlauncher.injector.Key;
import com.atlauncher.injector.Linker;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public final class ReflectiveInjectionBinding<T>
implements Binding<T>{
    @SuppressWarnings("unchecked")
    public static <T> Binding<T> create(Class<T> tClass){
        List<Key> keys = new LinkedList<>();
        Constructor<T> tConstructor = null;
        for(Constructor<T> constructor : (Constructor<T>[]) tClass.getDeclaredConstructors()){
            if(!constructor.isAnnotationPresent(Inject.class)){
                continue;
            }

            if(tConstructor != null){
                throw new IllegalStateException(tClass.getName() + " has too many @Inject constructors");
            }

            tConstructor = constructor;
        }

        if(tConstructor == null){
            try{
                tConstructor = tClass.getDeclaredConstructor();
            } catch(NoSuchMethodException e){
                // Fallthrough
            }
        }

        Key key = null;
        int paramCount = 0;
        if(tConstructor != null){
            key = Key.get(tClass);
            tConstructor.setAccessible(true);
            Type[] types = tConstructor.getGenericParameterTypes();
            paramCount = types.length;
            if(paramCount != 0){
                Annotation[][] annotations = tConstructor.getParameterAnnotations();
                for(int i = 0; i < types.length; i++){
                    keys.add(Key.get(types[i], annotations[i]));
                }
            }
        }

        return new ReflectiveInjectionBinding<>(key, tConstructor, keys.toArray(new Key[keys.size()]), paramCount);
    }

    public final  Key key;

    private final Constructor<T> constructor;
    private final Key<?>[] keys;
    private final Binding<?>[] paramBindings;

    private ReflectiveInjectionBinding(Key key, Constructor<T> constructor, Key<?>[] keys, int paramCount) {
        this.key = key;
        this.constructor = constructor;
        this.paramBindings = new Binding<?>[paramCount];
        this.keys = keys;
    }

    @Override
    public void link(Linker linker) {
        for(int i = 0; i < this.keys.length; i++){
            this.paramBindings[i] = linker.requestBinding(this.keys[i]);
        }
    }

    @Override
    public T get() {
        if(this.constructor == null){
            throw new NullPointerException("constructor");
        }

        Object[] args = new Object[this.paramBindings.length];
        for(int i = 0; i < this.paramBindings.length; i++){
            args[i] = this.paramBindings[i].get();
        }

        T instance;
        try{
            this.constructor.setAccessible(true);
            instance = this.constructor.newInstance(args);
        } catch(InvocationTargetException | InstantiationException e){
            throw new RuntimeException(e);
        } catch(IllegalAccessException e){
            throw new AssertionError(e);
        }

        return instance;
    }

    @Override
    public String toString(){
        return "ReflectiveBinding[key=" + this.key + "]";
    }
}