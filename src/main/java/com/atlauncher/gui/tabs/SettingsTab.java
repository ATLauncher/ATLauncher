/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.RelocalizationEvent;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.tabs.settings.*;

@SuppressWarnings("serial")
public class SettingsTab extends JPanel implements Tab{
    private JTabbedPane tabbedPane;

    private GeneralSettingsTab generalSettingsTab = new GeneralSettingsTab();
    private JavaSettingsTab javaSettingsTab = new JavaSettingsTab();
    private NetworkSettingsTab networkSettingsTab = new NetworkSettingsTab();
    private LoggingSettingsTab loggingSettingsTab = new LoggingSettingsTab();
    private ToolsSettingsTab toolsSettingsTab = new ToolsSettingsTab();

    private JPanel bottomPanel;
    private JButton saveButton = new JButton(App.settings.getLocalizedString("common.save"));

    public SettingsTab() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(App.THEME.getBaseColor());

        tabbedPane.setFont(App.THEME.getDefaultFont().deriveFont(17.0F));
        tabbedPane.addTab(App.settings.getLocalizedString("settings.generaltab"),
                generalSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("settings.javatab"), javaSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("settings.networktab"),
                networkSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("settings.loggingtab"),
                loggingSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.tools"), toolsSettingsTab);
        tabbedPane.setBackground(App.THEME.getTabBackgroundColor());
        tabbedPane.setOpaque(true);

        add(tabbedPane, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (javaSettingsTab.isValidJavaPath() && javaSettingsTab.isValidJavaParamaters()
                        && networkSettingsTab.isValidConcurrentConnections()
                        && networkSettingsTab.isValidProxyPort()
                        && networkSettingsTab.canConnectWithProxy()
                        && toolsSettingsTab.isValidServerCheckerWait()) {
                    boolean reloadLocalizationTable = generalSettingsTab.reloadLocalizationTable()
                            || generalSettingsTab.needToReloadTheme();
                    boolean reloadPacksPanel = generalSettingsTab.needToReloadPacksPanel();
                    boolean restartServerChecker = toolsSettingsTab.needToRestartServerChecker();
                    generalSettingsTab.save();
                    javaSettingsTab.save();
                    networkSettingsTab.save();
                    loggingSettingsTab.save();
                    toolsSettingsTab.save();
                    App.settings.saveProperties();
                    App.settings.log("Settings Saved!");
                    if (reloadLocalizationTable) {
                        RelocalizationManager.post(new RelocalizationEvent());
                    }
                    if (reloadPacksPanel) {
                        App.settings.reloadPacksPanel();
                    }
                    if (restartServerChecker) {
                        App.settings.startCheckingServers();
                    }
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("settings.saved"),
                            App.settings.getLocalizedString("settings.saved"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            options, options[0]);
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.settings");
    }
}