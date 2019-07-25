/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class RenameInstanceDialog extends JDialog {
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel instanceNameLabel;
    private JTextField instanceName;

    private JButton saveButton;

    private Instance instance;
    private InstanceV2 instanceV2;

    public RenameInstanceDialog(Instance instance) {
        super(null, GetText.tr("Renaming Instance"), ModalityType.APPLICATION_MODAL);

        this.instance = instance;

        Analytics.sendScreenView("Rename Instance Dialog");

        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        setupComponents();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    public RenameInstanceDialog(InstanceV2 instanceV2) {
        super(null, GetText.tr("Renaming Instance"), ModalityType.APPLICATION_MODAL);

        this.instanceV2 = instanceV2;

        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        setupComponents();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void setupComponents() {
        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(GetText.tr("Renaming Instance")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel(GetText.tr("Instance Name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceName = new JTextField(16);
        instanceName.setText(this.instanceV2 != null ? this.instanceV2.launcher.name : this.instance.getName());
        middle.add(instanceName, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(e -> {
            if (App.settings.isInstance(instanceName.getText())) {
                DialogManager.okDialog().setParent(RenameInstanceDialog.this).setTitle(GetText.tr("Error"))
                        .setContent(
                                GetText.tr("There is already an instance called {0}.<br/><br/>Rename it and try again.",
                                        instanceName.getText()))
                        .setType(DialogManager.ERROR).show();
            } else if (instanceName.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(
                                new HTMLBuilder().center()
                                        .text(GetText.tr("Error") + "<br/><br/>" + GetText.tr(
                                                "The name {0} is invalid. It must contain at least 1 letter or number.",
                                                instanceName.getText()))
                                        .build())
                        .setType(DialogManager.ERROR).show();
            } else {
                if (this.instanceV2 != null && instanceV2.rename(instanceName.getText())) {
                    App.settings.reloadInstancesPanel();
                } else if (this.instance != null && instance.rename(instanceName.getText())) {
                    App.settings.saveInstances();
                    App.settings.reloadInstancesPanel();
                } else {
                    LogManager.error("Unknown Error Occurred While Renaming Instance!");
                    DialogManager.okDialog().setParent(RenameInstanceDialog.this).setTitle(GetText.tr("Error"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "An error occurred renaming the instance.<br/><br/>Please check the console and try again."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                }
                close();
            }
        });
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
