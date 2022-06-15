/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.events.settings.SettingsEvent;
import com.atlauncher.events.settings.SettingsSavedEvent;
import com.atlauncher.events.theme.ThemeChangedEvent;
import com.atlauncher.gui.tabs.settings.BackupsSettingsTab;
import com.atlauncher.gui.tabs.settings.CommandsSettingsTab;
import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.gui.tabs.settings.JavaSettingsTab;
import com.atlauncher.gui.tabs.settings.LoggingSettingsTab;
import com.atlauncher.gui.tabs.settings.ModsSettingsTab;
import com.atlauncher.gui.tabs.settings.NetworkSettingsTab;
import com.formdev.flatlaf.FlatLaf;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class SettingsTab extends JPanel implements Tab {
    private final GeneralSettingsTab generalSettingsTab = new GeneralSettingsTab();
    private final ModsSettingsTab modsSettingsTab = new ModsSettingsTab();
    private final JavaSettingsTab javaSettingsTab = new JavaSettingsTab();
    private final NetworkSettingsTab networkSettingsTab = new NetworkSettingsTab();
    private final LoggingSettingsTab loggingSettingsTab = new LoggingSettingsTab();
    private final BackupsSettingsTab backupsSettingsTab = new BackupsSettingsTab();
    private final CommandsSettingsTab commandSettingsTab = new CommandsSettingsTab();
    private final List<Tab> tabs = Arrays.asList(
        new Tab[]{this.generalSettingsTab, this.modsSettingsTab, this.javaSettingsTab, this.networkSettingsTab,
            this.loggingSettingsTab, this.backupsSettingsTab, this.commandSettingsTab});
    private final JTabbedPane tabbedPane;
    private final JButton saveButton = new JButton(GetText.tr("Save"));

    public SettingsTab() {
        AppEventBus.register(this);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        tabbedPane.setFont(App.THEME.getNormalFont().deriveFont(17.0F));
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.setOpaque(true);

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
        saveButton.addActionListener(arg0 -> {
            if (javaSettingsTab.isValidJavaPath() && javaSettingsTab.isValidJavaParamaters()
                && networkSettingsTab.canConnectWithProxy()) {
                boolean reloadTheme = generalSettingsTab.needToReloadTheme();
                boolean themeChanged = generalSettingsTab.themeChanged();
                boolean reloadInstancesPanel = generalSettingsTab.needToReloadInstancesPanel();
                generalSettingsTab.save();
                modsSettingsTab.save();
                javaSettingsTab.save();
                networkSettingsTab.save();
                loggingSettingsTab.save();
                backupsSettingsTab.save();
                commandSettingsTab.save();
                App.settings.save();
                AppEventBus.post(SettingsSavedEvent.newInstance());
                if (reloadInstancesPanel) {
                    App.launcher.reloadInstancesPanel();
                }
                if (themeChanged) {
                    AppEventBus.postToDefault(ThemeChangedEvent.forCurrentTheme());
                }
                if (reloadTheme) {
                    App.loadTheme();
                    FlatLaf.updateUILater();
                    AppEventBus.post(ThemeChangedEvent.forCurrentTheme());//TODO: should this be a duplicate?
                }
                App.TOASTER.pop("Settings Saved");
            }
        });

        tabbedPane.addChangeListener(e ->{
            //TODO: Analytics.sendScreenView(((Tab) tabbedPane.getSelectedComponent()).getAnalyticsScreenViewName() + " Settings")
        });
    }

    @Override
    public String getTitle() {
        return GetText.tr("Settings");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        // since this is the default, this is the main view name
        return "General Settings";
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
        this.saveButton.setText(GetText.tr("Save"));
    }

}
