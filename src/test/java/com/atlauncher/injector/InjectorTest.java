package com.atlauncher.injector;

import org.junit.Test;

public final class InjectorTest {
    @Test
    public void testGetInstance()
    throws Exception {
        Injector injector = InjectorFactory.createInjector(new BasicModule());
        System.out.println(injector.getInstance(Injection.class).get());
    }

    private static final class BasicModule
    extends Module{
        @Override
        protected void configure() {
            this.bind(Injection.class).toInstance(new InjectionImpl());
        }
    }

    private static interface Injection{
        public String get();
    }

    private static final class InjectionImpl
    implements Injection{
        @Override
        public String get() {
            return "Hello World";
        }
    }
}