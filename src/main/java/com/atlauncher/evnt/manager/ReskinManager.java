package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.ReskinListener;

import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

@Deprecated
public final class ReskinManager{
    private static final List<ReskinListener> listeners = new LinkedList<ReskinListener>();

    public static synchronized void addListener(ReskinListener listener){
        listeners.add(listener);
    }

    public static synchronized void post(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                for(ReskinListener listener : listeners){
                    listener.onReskin();
                }
            }
        });
    }
}