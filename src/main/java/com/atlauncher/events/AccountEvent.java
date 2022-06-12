package com.atlauncher.events;

public abstract class AccountEvent extends Event{
    protected AccountEvent(){
        super();
    }

    @SwingEvent
    public static final class AccountChangedEvent extends AccountEvent{
        public AccountChangedEvent(){
            super();
        }
    }
}