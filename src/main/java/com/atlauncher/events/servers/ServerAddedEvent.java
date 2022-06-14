package com.atlauncher.events.servers;

import com.atlauncher.data.Server;

import javax.annotation.Nonnull;

public final class ServerAddedEvent extends ServerEvent{
    ServerAddedEvent(@Nonnull final Server server) {
        super(server);
    }

    public static ServerAddedEvent of(@Nonnull final Server server){
        return new ServerAddedEvent(server);
    }
}