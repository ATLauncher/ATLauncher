package com.atlauncher.injector;

import javax.inject.Named;
import java.io.Serializable;
import java.lang.annotation.Annotation;

public final class Names{
    public static Named named(String value){
        return new NamedImpl(value);
    }

    private static final class NamedImpl
    implements Named,
               Serializable{
        private final String name;

        private NamedImpl(String name){
            this.name = name;
        }

        @Override
        public String value() {
            return this.name;
        }

        @Override
        public int hashCode(){
            return (127 * "value".hashCode()) ^ this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Named
                && ((Named) obj).value().equals(this.value());

        }

        @Override
        public String toString(){
            return "@" + Named.class.getName() + "(value=" + this.name + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}