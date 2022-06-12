package com.atlauncher;

import com.atlauncher.events.SwingEvent;
import com.google.common.eventbus.EventBus;

import javax.annotation.Nonnull;
import javax.swing.*;

//TODO: refactor so subscribers can individually choose to run on the swing thread
public final class AppEventBus extends EventBus{
    @Override
    public void post(@Nonnull final Object event){
        if(!isSwingEvent(event)){
            super.post(event);
            return;
        }
        SwingUtilities.invokeLater(() -> super.post(event));
    }

    private static boolean isSwingEvent(final Object event){
        return event.getClass().isAnnotationPresent(SwingEvent.class);
    }
}