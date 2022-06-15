package com.atlauncher.events.instance;

import com.atlauncher.data.Instance;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public final class InstanceAddedEvent extends InstanceEvent{
    InstanceAddedEvent(@NotNull final Instance server) {
        super(server);
    }

    public static InstanceAddedEvent of(@Nonnull final Instance instance){
        return new InstanceAddedEvent(instance);
    }
}