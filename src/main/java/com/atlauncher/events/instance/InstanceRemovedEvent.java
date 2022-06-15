package com.atlauncher.events.instance;

import com.atlauncher.data.Instance;

import javax.annotation.Nonnull;

public final class InstanceRemovedEvent extends InstanceEvent{
    InstanceRemovedEvent(@Nonnull final Instance instance){
        super(instance);
    }

    public static InstanceRemovedEvent of(@Nonnull final Instance instance){
        return new InstanceRemovedEvent(instance);
    }
}