package com.atlauncher.gui.tabs.instances;

import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.network.Analytics;

import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

public final class InstancesSearchField extends JTextField implements KeyListener{
    private final InstancesTab parent;

    public InstancesSearchField(final InstancesTab parent){
        super(16);
        this.parent = parent;

        this.setMaximumSize(new Dimension(190, 23));
        this.addKeyListener(this);
    }

    public Pattern getSearchPattern(){
        return Pattern.compile(Pattern.quote(this.getText()), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void keyTyped(KeyEvent e){}

    @Override
    public void keyPressed(KeyEvent e){}

    @Override
    public void keyReleased(KeyEvent e){
        if(e.getKeyChar() == KeyEvent.VK_ENTER){
            Analytics.sendEvent(this.getText(), "Search", "Instance");
            this.parent.fireSearchEvent(new InstancesSearchEvent(e.getSource(), this.getSearchPattern()));
        }
    }
}
