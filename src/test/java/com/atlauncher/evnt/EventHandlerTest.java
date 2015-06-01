package com.atlauncher.evnt;

import com.atlauncher.MemorySpy;
import com.atlauncher.annot.Subscribe;
import org.junit.Test;

public class EventHandlerTest {
    @Test
    public void testGet()
    throws Exception {
        MemorySpy memorySpy = new MemorySpy();
        EventHandler.EVENT_BUS.subscribe(new Handler());
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleCloseEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleOpenEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleCloseEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleOpenEvent.class));
        System.out.println(memorySpy.used() + "b");
    }

    private final class Handler{
        @Subscribe
        private void onConsoleOpen(EventHandler.ConsoleOpenEvent e){
            System.out.println("Console Opened");
        }

        @Subscribe
        private void onConsoleClosed(EventHandler.ConsoleCloseEvent e){
            System.out.println("Console Closed");
        }
    }
}