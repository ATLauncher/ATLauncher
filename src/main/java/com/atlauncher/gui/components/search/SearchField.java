package com.atlauncher.gui.components.search;

import com.atlauncher.AppEventBus;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

public abstract class SearchField<SearchEvent> extends JTextField implements KeyListener{
    public static final int DEFAULT_COLUMNS = 16;

    protected SearchField(final int columns){
        super(columns);
        this.addKeyListener(this);
        this.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        this.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        this.putClientProperty("JTextField.showClearButton", true);
        this.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            this.setText("");
            this.fireSearchEvent();
        });
    }

    protected SearchField(){
        this(DEFAULT_COLUMNS);
    }

    public final Pattern getSearchPattern(){
        return Pattern.compile(Pattern.quote(this.getText()), Pattern.CASE_INSENSITIVE);
    }

    protected abstract SearchEvent createSearchEvent();

    protected final void fireSearchEvent(){
        AppEventBus.post(this.createSearchEvent());
    }

    @Override
    public final void keyTyped(KeyEvent e){
    }

    @Override
    public final void keyPressed(KeyEvent e){
    }

    @Override
    public final void keyReleased(KeyEvent e){
        if(e.getKeyChar() == KeyEvent.VK_ENTER)
            this.fireSearchEvent();
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationChangedEvent event){
        this.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
    }
}