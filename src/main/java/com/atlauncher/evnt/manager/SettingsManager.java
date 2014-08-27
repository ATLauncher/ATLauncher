package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.SettingsListener;

import javax.swing.SwingUtilities;
import java.util.LinkedList;
import java.util.List;

public final class SettingsManager {
    private static final List<SettingsListener> listeners = new LinkedList<SettingsListener>();

    public static synchronized void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void post() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (SettingsListener listener : listeners) {
                    listener.onSettingsSaved();
                }
            }
        });
    }
}
