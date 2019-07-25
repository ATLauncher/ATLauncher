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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class AddPackDialog extends JDialog {
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel packCodeLabel;
    private JTextField packCode;

    private JButton saveButton;

    public AddPackDialog() {
        super(null, GetText.tr("Add Pack"), ModalityType.APPLICATION_MODAL);
        setSize(350, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Add Pack Dialog");

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(GetText.tr("Add Pack")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        packCodeLabel = new JLabel(GetText.tr("Pack Code") + ": ");
        middle.add(packCodeLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        packCode = new JTextField(16);
        middle.add(packCode, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(GetText.tr("Add"));
        saveButton.addActionListener(e -> {
            if (App.settings.semiPublicPackExistsFromCode(packCode.getText())) {
                if (App.settings.addPack(packCode.getText())) {
                    DialogManager.okDialog().setParent(AddPackDialog.this).setTitle(GetText.tr("Pack Added"))
                            .setContent(GetText.tr("The pack has been added!")).setType(DialogManager.INFO).show();
                } else {
                    DialogManager.okDialog().setParent(AddPackDialog.this).setTitle(GetText.tr("Pack Already Added"))
                            .setContent(GetText.tr("The pack was already added!")).setType(DialogManager.ERROR).show();
                }
                setVisible(false);
                dispose();
            } else {
                DialogManager.okDialog().setParent(AddPackDialog.this).setTitle(GetText.tr("Error Adding Pack"))
                        .setContent(GetText.tr("A pack with that code doesn't exist!")).setType(DialogManager.ERROR)
                        .show();
            }
        });
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                setVisible(false);
                dispose();
            }
        });

        setVisible(true);
    }

}
