package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.events.console.ConsoleClosedEvent;
import com.atlauncher.events.console.ConsoleOpenedEvent;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;

public final class ToggleConsoleButton extends JButton{
    public ToggleConsoleButton(){
        super(App.console.isVisible() ? GetText.tr("Hide Console") : GetText.tr("Show Console"));
        AppEventBus.registerToUIOnly(this);
        this.addActionListener(e ->{
            App.console.setVisible(!App.console.isVisible());
        });
    }

    @Subscribe
    public void onLocalizationChanged(final LocalizationChangedEvent event){
        this.setText(App.console.isVisible() ? GetText.tr("Hide Console") : GetText.tr("Show Console"));
    }

    @Subscribe
    public void onConsoleOpened(final ConsoleOpenedEvent event){
        this.setText(GetText.tr("Hide Console"));
    }

    @Subscribe
    public void onConsoleClosed(final ConsoleClosedEvent event){
        this.setText(GetText.tr("Show Console"));
    }
}