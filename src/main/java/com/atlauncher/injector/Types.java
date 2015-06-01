package com.atlauncher.injector;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public final class Types{
    public static boolean equals(Type type1, Type type2){
        if(type1 == type2){
            return true;
        } else if(type1 instanceof Class<?>){
            return type1.equals(type2);
        } else if(type1 instanceof ParameterizedType){
            if(!(type2 instanceof ParameterizedType)){
                return false;
            }

            ParameterizedType p1 = (ParameterizedType) type1;
            ParameterizedType p2 = (ParameterizedType) type2;
            return p1.getOwnerType().equals(p2.getOwnerType())
                && p1.getRawType().equals(p2.getRawType())
                && Arrays.equals(p1.getActualTypeArguments(), p2.getActualTypeArguments());
        } else if(type1 instanceof GenericArrayType){
            if(!(type2 instanceof GenericArrayType)){
                return false;
            }

            GenericArrayType g1 = (GenericArrayType) type1;
            GenericArrayType g2 = (GenericArrayType) type2;
            return Types.equals(g1.getGenericComponentType(), g2.getGenericComponentType());
        } else if(type1 instanceof WildcardType){
            if(!(type2 instanceof WildcardType)){
                return false;
            }

            WildcardType w1 = (WildcardType) type1;
            WildcardType w2 = (WildcardType) type2;
            return Arrays.equals(w1.getUpperBounds(), w2.getUpperBounds())
                && Arrays.equals(w1.getLowerBounds(), w2.getLowerBounds());
        } else if(type1 instanceof TypeVariable){
            if(!(type2 instanceof TypeVariable)){
                return false;
            }

            TypeVariable<?> v1 = (TypeVariable<?>) type1;
            TypeVariable<?> v2 = (TypeVariable<?>) type2;
            return v1.getGenericDeclaration().equals(v2.getGenericDeclaration())
                && v1.getName().equals(v2.getName());
        } else{
            return false;
        }
    }

    public static Class<?> getRawType(Type type){
        if(type instanceof Class<?>){
            return (Class<?>) type;
        } else if(type instanceof ParameterizedType){
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if(!(rawType instanceof Class<?>)){
                throw new IllegalStateException("Expected a Class, but " + type + " is of type " + type.getClass().getName());
            }
            return (Class<?>) rawType;
        } else if(type instanceof GenericArrayType){
            Type cType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(cType), 0).getClass();
        } else if(type instanceof TypeVariable){
            return Object.class;
        } else{
            throw new IllegalArgumentException("Expected a Class, ParamterizedType, GenericArrayType, or TypeVariable but got " + type.getClass().getName());
        }
    }
}