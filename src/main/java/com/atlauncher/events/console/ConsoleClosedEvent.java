package com.atlauncher.events.console;

public final class ConsoleClosedEvent extends ConsoleEvent{
    ConsoleClosedEvent(){
        super();
    }

    public static ConsoleClosedEvent newInstance(){
        return new ConsoleClosedEvent();
    }
}