package com.atlauncher.gui.tabs;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public final class TabTracker{
    private final List<Tab> tabs = new LinkedList<Tab>();
    private final JTabbedPane tabbedPane;

    public TabTracker(JTabbedPane pane){
        this.tabbedPane = pane;
    }

    public TabTracker add(Tab tab){
        this.tabs.add(tab);
        return this;
    }

    public TabTracker finish(){
        for(Tab tab : this.tabs){
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        return this;
    }

    public void relocalize(){
        for(int i = 0; i < this.tabbedPane.getTabCount(); i++){
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
    }
}