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
package com.atlauncher.gui.tabs.settings;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.atlauncher.gui.tabs.Tab;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.network.Analytics;

@SuppressWarnings("serial")
public class SettingsTab extends JPanel implements Tab, RelocalizationListener {
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
        ISettingsViewModel viewModel = new SettingsViewModel();
        RelocalizationManager.addListener(this);
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
        saveButton.addActionListener(arg0 -> viewModel.save());
        viewModel.addOnSaveEnabledChanged(saveValidity -> {
            saveButton.setEnabled(saveValidity);
            if (!saveValidity)
                saveButton.setToolTipText(GetText.tr("Review settings"));
            else saveButton.setToolTipText(null);
        });

        tabbedPane.addChangeListener(e -> Analytics
            .sendScreenView(((Tab) tabbedPane.getSelectedComponent()).getAnalyticsScreenViewName() + " Settings"));
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

    @Override
    public void onRelocalization() {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
        this.saveButton.setText(GetText.tr("Save"));
    }

}
