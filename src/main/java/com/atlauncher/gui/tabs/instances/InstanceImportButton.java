package com.atlauncher.gui.tabs.instances;

import com.atlauncher.AppEventBus;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.gui.dialogs.ImportInstanceDialog;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;

public final class InstanceImportButton extends JButton{
    public InstanceImportButton(){
        super(GetText.tr("Import"));
        this.setMaximumSize(new Dimension(190, 23));
        this.addActionListener((e) -> new ImportInstanceDialog());
        AppEventBus.registerToUIOnly(this);
    }

    @Subscribe
    public void onLocalizationChanged(final LocalizationChangedEvent event){
        this.setText(GetText.tr("Import"));
    }
}