/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

public class AddPackDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel packCodeLabel;
    private JTextField packCode;

    private JButton saveButton;

    public AddPackDialog() {
        super(null, App.settings.getLocalizedString("pack.addpack"), ModalityType.APPLICATION_MODAL);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(App.settings.getLocalizedString("pack.addpack")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        packCodeLabel = new JLabel(App.settings.getLocalizedString("pack.packcode") + ": ");
        middle.add(packCodeLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        packCode = new JTextField(16);
        middle.add(packCode, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(App.settings.getLocalizedString("common.save"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.semiPublicPackExistsFromCode(packCode.getText())) {
                    if (App.settings.addPack(packCode.getText())) {
                        JOptionPane.showMessageDialog(AddPackDialog.this,
                                App.settings.getLocalizedString("pack.packaddedmessage"),
                                App.settings.getLocalizedString("pack.packadded"),
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AddPackDialog.this,
                                App.settings.getLocalizedString("pack.packalreadyaddedmessage"),
                                App.settings.getLocalizedString("pack.packalreadyadded"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    setVisible(false);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(AddPackDialog.this,
                            App.settings.getLocalizedString("pack.packdoesntexist"),
                            App.settings.getLocalizedString("pack.packaddederror"),
                            JOptionPane.ERROR_MESSAGE);
                }
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
