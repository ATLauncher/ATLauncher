package com.atlauncher.events.console;

public final class ConsoleOpenedEvent extends ConsoleEvent{
    ConsoleOpenedEvent(){
        super();
    }

    public static ConsoleOpenedEvent newInstance(){
        return new ConsoleOpenedEvent();
    }
}