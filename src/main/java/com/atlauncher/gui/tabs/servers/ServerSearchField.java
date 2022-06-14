package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

public final class ServerSearchField extends JTextField implements KeyListener {
    public ServerSearchField(){
        super(16);
        this.addKeyListener(this);
        this.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        this.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        this.putClientProperty("JTextField.showClearButton", true);
        this.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            this.setText("");
            this.fireSearchEvent();
        });
    }

    public Pattern getSearchPattern(){
        return Pattern.compile(Pattern.quote(this.getText()), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void keyTyped(KeyEvent e){
    }

    @Override
    public void keyPressed(KeyEvent e){
    }

    @Override
    public void keyReleased(KeyEvent e){
        if(e.getKeyChar() == KeyEvent.VK_ENTER)
            this.fireSearchEvent();
    }

    private void fireSearchEvent(){
        AppEventBus.post(ServerSearchEvent.forSearchField(this));
    }

    @Subscribe
    public void onLocalizationChanged(final LocalizationChangedEvent event){
        this.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
    }
}