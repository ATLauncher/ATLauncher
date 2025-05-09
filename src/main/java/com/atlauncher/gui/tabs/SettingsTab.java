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

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.settings.BackupsSettingsTab;
import com.atlauncher.gui.tabs.settings.CommandsSettingsTab;
import com.atlauncher.gui.tabs.settings.EnvironmentVariablesTab;
import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.gui.tabs.settings.JavaSettingsTab;
import com.atlauncher.gui.tabs.settings.LoggingSettingsTab;
import com.atlauncher.gui.tabs.settings.ModsSettingsTab;
import com.atlauncher.gui.tabs.settings.NetworkSettingsTab;
import com.atlauncher.network.Analytics;
import com.atlauncher.viewmodel.impl.settings.BackupsSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.CommandsSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.EnvironmentVariablesViewModel;
import com.atlauncher.viewmodel.impl.settings.GeneralSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.JavaSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.LoggingSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.ModsSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.NetworkSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.SettingsViewModel;

public class SettingsTab extends HierarchyPanel implements Tab {
    @Nullable
    private JTabbedPane tabbedPane;
    @Nullable
    private JButton saveButton;

    private SettingsViewModel viewModel;

    // We maintain the state at the top level for all tabs

    private BackupsSettingsViewModel backupSettingsViewModel;
    private CommandsSettingsViewModel commandsSettingsViewModel;
    private GeneralSettingsViewModel generalSettingsViewModel;
    private JavaSettingsViewModel javaSettingsViewModel;
    private EnvironmentVariablesViewModel environmentVariablesViewModel;
    private LoggingSettingsViewModel loggingSettingsViewModel;
    private ModsSettingsViewModel modsSettingsViewModel;
    private NetworkSettingsViewModel networkSettingsViewModel;

    @Nullable
    private GeneralSettingsTab generalSettingsTab;
    @Nullable
    private ModsSettingsTab modsSettingsTab;
    @Nullable
    private JavaSettingsTab javaSettingsTab;
    @Nullable
    private EnvironmentVariablesTab environmentVariablesTab;
    @Nullable
    private NetworkSettingsTab networkSettingsTab;
    @Nullable
    private LoggingSettingsTab loggingSettingsTab;
    @Nullable
    private BackupsSettingsTab backupsSettingsTab;
    @Nullable
    private CommandsSettingsTab commandSettingsTab;
    @Nullable
    private List<Tab> tabs;

    private int selectedTabIndex = 0;

    public SettingsTab() {
        setLayout(new BorderLayout());
    }

    @Override
    protected void createViewModel() {
        viewModel = new SettingsViewModel();

        backupSettingsViewModel = new BackupsSettingsViewModel();
        commandsSettingsViewModel = new CommandsSettingsViewModel();
        generalSettingsViewModel = new GeneralSettingsViewModel();
        javaSettingsViewModel = new JavaSettingsViewModel();
        environmentVariablesViewModel = new EnvironmentVariablesViewModel();
        loggingSettingsViewModel = new LoggingSettingsViewModel();
        modsSettingsViewModel = new ModsSettingsViewModel();
        networkSettingsViewModel = new NetworkSettingsViewModel();
    }

    @SuppressWarnings("null")
    @Override
    protected void onShow() {
        saveButton = new JButton(GetText.tr("Save"));
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        tabbedPane.setFont(App.THEME.getNormalFont().deriveFont(17.0F));

        generalSettingsTab = new GeneralSettingsTab(generalSettingsViewModel);
        modsSettingsTab = new ModsSettingsTab(modsSettingsViewModel);
        javaSettingsTab = new JavaSettingsTab(javaSettingsViewModel);
        networkSettingsTab = new NetworkSettingsTab(networkSettingsViewModel);
        loggingSettingsTab = new LoggingSettingsTab(loggingSettingsViewModel);
        backupsSettingsTab = new BackupsSettingsTab(backupSettingsViewModel);
        commandSettingsTab = new CommandsSettingsTab(commandsSettingsViewModel);
        environmentVariablesTab = new EnvironmentVariablesTab(environmentVariablesViewModel);
        tabs = Arrays.asList(
                new Tab[] { this.generalSettingsTab, this.modsSettingsTab, this.javaSettingsTab,
                        this.networkSettingsTab, this.loggingSettingsTab, this.backupsSettingsTab,
                        this.commandSettingsTab, this.environmentVariablesTab });

        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.setOpaque(true);
        tabbedPane.setSelectedIndex(selectedTabIndex);

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
        addDisposable(viewModel.getSaveEnabled().subscribe(saveButton::setEnabled));
        saveButton.addActionListener(arg0 -> viewModel.save());

        tabbedPane.addChangeListener(e -> {
            selectedTabIndex = tabbedPane.getSelectedIndex();
            Analytics.sendScreenView(
                    ((Tab) tabbedPane.getSelectedComponent()).getAnalyticsScreenViewName() + " Settings");
        });
    }

    @Override
    protected void onDestroy() {
        removeAll();
        tabbedPane = null;
        saveButton = null;

        generalSettingsTab = null;
        modsSettingsTab = null;
        javaSettingsTab = null;
        environmentVariablesTab = null;
        networkSettingsTab = null;
        loggingSettingsTab = null;
        backupsSettingsTab = null;
        commandSettingsTab = null;
        tabs = null;
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
}
