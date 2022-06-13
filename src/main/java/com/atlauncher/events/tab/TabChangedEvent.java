package com.atlauncher.events.tab;

public final class TabChangedEvent extends TabEvent{
    TabChangedEvent(){
        super();
    }

    public static TabChangedEvent newInstance(){
        return new TabChangedEvent();
    }
}