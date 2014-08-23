package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.ConsoleCloseListener;

import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

public final class ConsoleCloseManager{
    private static final List<ConsoleCloseListener> listeners = new LinkedList<ConsoleCloseListener>();

    private ConsoleCloseManager(){
    }

    public static synchronized void addListener(ConsoleCloseListener listener){
        listeners.add(listener);
    }

    public static synchronized void removeListener(ConsoleCloseListener listener){
        listeners.remove(listener);
    }

    public static synchronized void post(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                for(ConsoleCloseListener listener : listeners){
                    listener.onConsoleClose();
                }
            }
        });
    }
}
