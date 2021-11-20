/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.dialogs.instancesettings.CommandsInstanceSettingsTab;
import com.atlauncher.gui.dialogs.instancesettings.GeneralInstanceSettingsTab;
import com.atlauncher.gui.dialogs.instancesettings.JavaInstanceSettingsTab;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class InstanceSettingsDialog extends JDialog {
    private final Instance instance;

    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    private final JPanel bottomPanel = new JPanel();

    private final GeneralInstanceSettingsTab generalInstanceSettingsTab;
    private final JavaInstanceSettingsTab javaInstanceSettingsTab;
    private final CommandsInstanceSettingsTab commandsInstanceSettingsTab;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
    final ImageIcon ERROR_ICON = Utils.getIconImage(App.THEME.getIconPath("error"));
    final ImageIcon WARNING_ICON = Utils.getIconImage(App.THEME.getIconPath("warning"));

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);

    final GridBagConstraints gbc = new GridBagConstraints();

    public InstanceSettingsDialog(Instance instance) {
        super(App.launcher.getParent(), GetText.tr("{0} Settings", instance.launcher.name),
                ModalityType.DOCUMENT_MODAL);
        this.instance = instance;

        this.generalInstanceSettingsTab = new GeneralInstanceSettingsTab(instance);
        this.javaInstanceSettingsTab = new JavaInstanceSettingsTab(instance);
        this.commandsInstanceSettingsTab = new CommandsInstanceSettingsTab(instance);

        setupComponents();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void setupComponents() {
        setSize(800, 450);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Tabbed Pane

        tabbedPane.setFont(App.THEME.getNormalFont().deriveFont(17.0F));
        tabbedPane.addTab(GetText.tr("General"), generalInstanceSettingsTab);
        tabbedPane.addTab(GetText.tr("Java/Minecraft"), javaInstanceSettingsTab);
        tabbedPane.addTab(GetText.tr("Commands"), commandsInstanceSettingsTab);
        tabbedPane.setOpaque(true);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Panel

        bottomPanel.setLayout(new FlowLayout());

        JButton saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(arg0 -> {
            saveSettings();
            App.TOASTER.pop("Instance Settings Saved");
            close();
        });
        bottomPanel.add(saveButton);

        JButton cancelButton = new JButton(GetText.tr("Cancel"));
        cancelButton.addActionListener(arg0 -> {
            close();
        });
        bottomPanel.add(cancelButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    private void saveSettings() {
        generalInstanceSettingsTab.saveSettings();
        javaInstanceSettingsTab.saveSettings();
        commandsInstanceSettingsTab.saveSettings();

        this.instance.save();
    }

}
