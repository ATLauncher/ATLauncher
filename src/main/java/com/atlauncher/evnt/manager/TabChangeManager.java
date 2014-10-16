package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.TabChangeListener;

import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

public final class TabChangeManager {
    private static final List<TabChangeListener> listeners = new LinkedList<TabChangeListener>();

    public static synchronized void addListener(TabChangeListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(TabChangeListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void post() {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                for(TabChangeListener listener : listeners){
                    listener.on();
                }
            }
        });
    }
}
