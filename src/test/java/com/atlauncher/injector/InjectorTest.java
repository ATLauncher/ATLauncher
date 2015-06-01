package com.atlauncher.injector;

import com.atlauncher.evnt.EventHandler;
import org.junit.Test;

public final class InjectorTest {
    @Test
    public void testGetInstance()
    throws Exception {
        Injector injector = InjectorFactory.createInjector(new BasicModule());
    }

    private static final class BasicModule
    extends Module{
        @Override
        protected void configure() {
            this.bind(EventHandler.AccountsChangeEvent.class).asSingleton();
        }
    }
}