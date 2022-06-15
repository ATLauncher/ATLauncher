package com.atlauncher.events.instance;

import com.atlauncher.data.Instance;
import com.atlauncher.events.Event;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public abstract class InstanceEvent implements Event {
    private final Instance server;

    protected InstanceEvent(@Nonnull final Instance server){
        Preconditions.checkNotNull(server);
        this.server = server;
    }

    public final Instance getInstance(){
        return this.server;
    }
}