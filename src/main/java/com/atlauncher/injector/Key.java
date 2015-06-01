package com.atlauncher.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Key<T>{
    private static final Map<Type, Type> boxes = new HashMap<>();
    static{
        boxes.put(int.class, Integer.class);
        boxes.put(boolean.class, Boolean.class);
        boxes.put(short.class, Short.class);
        boxes.put(byte.class, Byte.class);
        boxes.put(void.class, Void.class);
    }

    public final Class<? super T> rawType;
    public final Type type;
    public final Annotation[] annotations;
    private final int hashcode;

    @SuppressWarnings("unchecked")
    public static <T> Key<T> get(Class<T> clazz, Annotation... annotations){
        if(boxes.containsKey(clazz)){
            clazz = (Class<T>) boxes.get(clazz);
        }
        return new Key<>(clazz, annotations);
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> get(Class<T> clazz){
        if(boxes.containsKey(clazz)){
            clazz = (Class<T>) boxes.get(clazz);
        }
        return new Key<>(clazz, clazz.getDeclaredAnnotations());
    }

    public static <T> Key<T> get(Type t){
        if(boxes.containsKey(t)){
            t = boxes.get(t);
        }
        return new Key<>(t, Types.getRawType(t).getAnnotations());
    }

    public static <T> Key<T> get(Type t, Annotation... annotations){
        if(boxes.containsKey(t)){
            t = boxes.get(t);
        }
        return new Key<>(t, annotations);
    }

    @SuppressWarnings("unchecked")
    public Key(Type type, Annotation[] annotations){
        this.type = type;
        this.annotations = annotations;
        this.rawType = (Class<? super T>) Types.getRawType(type);
        this.hashcode = this.type.hashCode() * 31;
    }

    @Override
    public String toString(){
        return "Key[rawType=" + this.rawType.getName() + ", annotations=" + Arrays.toString(this.annotations) + "]";
    }

    @Override
    public int hashCode(){
        return this.hashcode;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }

        if(!(obj instanceof Key<?>)){
            return false;
        }

        Key<?> other = (Key<?>) obj;
        return Types.equals(this.type, other.type)
            && Arrays.equals(this.annotations, other.annotations);
    }
}