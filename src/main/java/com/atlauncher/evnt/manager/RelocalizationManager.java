package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.ConsoleOpenEvent;
import com.atlauncher.evnt.RelocalizationEvent;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.listener.RelocalizationListener;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public final class RelocalizationManager {
    private static final List<RelocalizationListener> listeners = new LinkedList<RelocalizationListener>();

    public static synchronized void addListener(RelocalizationListener listener){
        listeners.add(listener);
    }

    public static synchronized void removeListener(RelocalizationListener listener){
        listeners.remove(listener);
    }

    public static synchronized void post(final RelocalizationEvent event){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (RelocalizationListener listener : listeners) {
                    listener.onRelocalization(event);
                }
            }
        });
    }
}
