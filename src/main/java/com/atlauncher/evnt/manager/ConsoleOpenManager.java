package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.ConsoleOpenListener;

import javax.swing.SwingUtilities;
import java.util.LinkedList;
import java.util.List;

public final class ConsoleOpenManager {
    private static final List<ConsoleOpenListener> listeners = new LinkedList<ConsoleOpenListener>();

    public static synchronized void addListener(ConsoleOpenListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(ConsoleOpenListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void post() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (ConsoleOpenListener listener : listeners) {
                    listener.onConsoleOpen();
                }
            }
        });
    }
}