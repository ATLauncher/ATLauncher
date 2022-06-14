package com.atlauncher.events.servers;

import com.atlauncher.data.Server;

import javax.annotation.Nonnull;

public final class ServerRemovedEvent extends ServerEvent{
    ServerRemovedEvent(@Nonnull final Server server) {
        super(server);
    }

    public static ServerRemovedEvent of(@Nonnull final Server server){
        return new ServerRemovedEvent(server);
    }
}