package com.atlauncher.events;

public abstract class ConsoleEvent extends Event{
    protected ConsoleEvent(){
        super();
    }

    @SwingEvent
    public static final class ConsoleOpenedEvent extends ConsoleEvent{
        public ConsoleOpenedEvent(){
            super();
        }
    }

    @SwingEvent
    public static final class ConsoleClosedEvent extends ConsoleEvent{
        public ConsoleClosedEvent(){
            super();
        }
    }
}