/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.gui.tabs.settings.JavaSettingsTab;
import com.atlauncher.gui.tabs.settings.LoggingSettingsTab;
import com.atlauncher.gui.tabs.settings.NetworkSettingsTab;
import com.atlauncher.gui.tabs.settings.ToolsSettingsTab;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class SettingsTab extends JPanel implements Tab, RelocalizationListener {

    private final GeneralSettingsTab generalSettingsTab = new GeneralSettingsTab();
    private final JavaSettingsTab javaSettingsTab = new JavaSettingsTab();
    private final NetworkSettingsTab networkSettingsTab = new NetworkSettingsTab();
    private final LoggingSettingsTab loggingSettingsTab = new LoggingSettingsTab();
    private final ToolsSettingsTab toolsSettingsTab = new ToolsSettingsTab();
    private final List<Tab> tabs = Arrays.asList(new Tab[]{this.generalSettingsTab, this.javaSettingsTab, this
            .networkSettingsTab, this.loggingSettingsTab, this.toolsSettingsTab});
    private JTabbedPane tabbedPane;
    private JPanel bottomPanel;
    private JButton saveButton = new JButton(Language.INSTANCE.localize("common.save"));

    public SettingsTab() {
        RelocalizationManager.addListener(this);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(App.THEME.getBaseColor());

        tabbedPane.setFont(App.THEME.getDefaultFont().deriveFont(17.0F));
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.setBackground(App.THEME.getTabBackgroundColor());
        tabbedPane.setOpaque(true);

        add(tabbedPane, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (javaSettingsTab.isValidJavaPath() && javaSettingsTab.isValidJavaParamaters() &&
                        networkSettingsTab.isValidConcurrentConnections() && networkSettingsTab.isValidProxyPort() &&
                        networkSettingsTab.canConnectWithProxy() && toolsSettingsTab.isValidServerCheckerWait()) {
                    boolean reloadTheme = generalSettingsTab.needToReloadTheme();
                    boolean reloadLocalizationTable = generalSettingsTab.reloadLocalizationTable();
                    boolean reloadPacksPanel = generalSettingsTab.needToReloadPacksPanel();
                    boolean restartServerChecker = toolsSettingsTab.needToRestartServerChecker();
                    generalSettingsTab.save();
                    javaSettingsTab.save();
                    networkSettingsTab.save();
                    loggingSettingsTab.save();
                    toolsSettingsTab.save();
                    App.settings.saveProperties();
                    SettingsManager.post();
                    if (reloadLocalizationTable) {
                        RelocalizationManager.post();
                    }
                    if (reloadPacksPanel) {
                        App.settings.reloadPacksPanel();
                    }
                    if (restartServerChecker) {
                        App.settings.startCheckingServers();
                    }
                    if (reloadTheme) {
                        App.settings.restartLauncher();
                    }
                    App.TOASTER.pop("Settings Saved");
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.settings");
    }

    @Override
    public void onRelocalization() {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
        this.saveButton.setText(Language.INSTANCE.localize("common.save"));
    }

}