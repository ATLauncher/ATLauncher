package com.atlauncher.events.servers;

import com.atlauncher.data.Server;
import com.atlauncher.events.Event;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public abstract class ServerEvent implements Event{
    private final Server server;

    protected ServerEvent(@Nonnull final Server server){
        Preconditions.checkNotNull(server);
        this.server = server;
    }

    public final Server getServer(){
        return this.server;
    }
}