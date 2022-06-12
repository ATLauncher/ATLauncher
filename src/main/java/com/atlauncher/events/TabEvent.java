package com.atlauncher.events;

public abstract class TabEvent extends Event{
    protected TabEvent(){
        super();
    }

    @SwingEvent
    public static final class TabChangedEvent extends TabEvent{
        public TabChangedEvent(){
            super();
        }
    }
}