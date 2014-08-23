package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.SettingsListener;

import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

public final class SettingsManager{
    private static final List<SettingsListener> listeners = new LinkedList<SettingsListener>();

    public static synchronized void addListener(SettingsListener listener){
        listeners.add(listener);
    }

    public static synchronized void removeListener(SettingsListener listener){
        listeners.remove(listener);
    }

    public static synchronized void post(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                for(SettingsListener listener : listeners){
                    listener.onSettingsSaved();
                }
            }
        });
    }
}
