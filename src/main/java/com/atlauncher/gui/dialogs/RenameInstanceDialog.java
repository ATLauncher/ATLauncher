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
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

public class RenameInstanceDialog extends JDialog {
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel instanceNameLabel;
    private JTextField instanceName;

    private JButton saveButton;

    public RenameInstanceDialog(final Instance instance) {
        super(null, Language.INSTANCE.localize("instance.renaminginstance"), ModalityType.APPLICATION_MODAL);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(Language.INSTANCE.localize("instance.renaminginstance")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel(Language.INSTANCE.localize("instance.name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceName = new JTextField(16);
        instanceName.setText(instance.getName());
        middle.add(instanceName, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(Language.INSTANCE.localize("common.save"));
        saveButton.addActionListener(e -> {
            if (App.settings.isInstance(instanceName.getText())) {
                DialogManager.okDialog().setParent(RenameInstanceDialog.this)
                        .setTitle(Language.INSTANCE.localize("common" + ".error")).setContent(Language.INSTANCE
                                .localizeWithReplace("instance.alreadyinstance", instanceName.getText()))
                        .setType(DialogManager.ERROR).show();
            } else if (instanceName.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(
                                HTMLUtils
                                        .centerParagraph(
                                                Language.INSTANCE.localize("common.error") + "<br/><br/>"
                                                        + Language.INSTANCE.localizeWithReplace("instance.invalidname",
                                                                instanceName.getText())))
                        .setType(DialogManager.ERROR).show();
            } else {
                if (instance.rename(instanceName.getText())) {
                    App.settings.saveInstances();
                    App.settings.reloadInstancesPanel();
                } else {
                    LogManager.error("Unknown Error Occured While Renaming Instance!");
                    DialogManager.okDialog().setParent(RenameInstanceDialog.this)
                            .setTitle(Language.INSTANCE.localize("common" + ".error"))
                            .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                                    .localizeWithReplace("instance.errorrenaming", instance.getName() + "<br/><br/>")))
                            .setType(DialogManager.ERROR).show();
                }
                close();
            }
        });
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
