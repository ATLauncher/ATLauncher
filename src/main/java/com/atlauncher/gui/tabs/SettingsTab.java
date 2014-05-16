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

@SuppressWarnings("serial")
public class SettingsTab extends JPanel {

    private JTabbedPane tabbedPane;

    private GeneralSettingsTab generalSettingsTab = new GeneralSettingsTab();
    private JavaSettingsTab javaSettingsTab = new JavaSettingsTab();
    private LoggingSettingsTab loggingSettingsTab = new LoggingSettingsTab();

    private JPanel bottomPanel;
    private JButton saveButton = new JButton(App.settings.getLocalizedString("common.save"));

    public SettingsTab() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(App.THEME.getBaseColour());

        tabbedPane.setFont(App.THEME.getSettingsTabsFont());
        tabbedPane.addTab(App.settings.getLocalizedString("settings.generaltab"),
                generalSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("settings.javatab"), javaSettingsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("settings.loggingtab"),
                loggingSettingsTab);
        tabbedPane.setBackground(App.THEME.getTabBackgroundColour());
        tabbedPane.setOpaque(true);

        add(tabbedPane, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (javaSettingsTab.isValidJavaPath() && javaSettingsTab.isValidJavaParamaters()) {
                    boolean restartLauncher = generalSettingsTab.needToRestartLauncher();
                    boolean reloadPacksPanel = generalSettingsTab.needToReloadPacksPanel();
                    generalSettingsTab.save();
                    javaSettingsTab.save();
                    loggingSettingsTab.save();
                    App.settings.saveProperties();
                    App.settings.log("Settings Saved!");
                    if (restartLauncher) {
                        App.settings.restartLauncher();
                    }
                    if (reloadPacksPanel) {
                        App.settings.reloadPacksPanel();
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
}