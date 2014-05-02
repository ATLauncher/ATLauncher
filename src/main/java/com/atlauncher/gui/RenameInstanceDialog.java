/**
 * Copyright 2013-2014 by ATLauncher and Contributors
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
import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.utils.Utils;

public class RenameInstanceDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel instanceNameLabel;
    private JTextField instanceName;

    private JButton saveButton;

    public RenameInstanceDialog(final Instance instance) {
        super(null, App.settings.getLocalizedString("instance.renaminginstance"),
                ModalityType.APPLICATION_MODAL);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(App.settings.getLocalizedString("instance.renaminginstance")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel(App.settings.getLocalizedString("instance.name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceName = new JTextField(16);
        instanceName.setText(instance.getName());
        middle.add(instanceName, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(App.settings.getLocalizedString("common.save"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInstance(instanceName.getText())) {
                    JOptionPane.showMessageDialog(RenameInstanceDialog.this,
                            App.settings.getLocalizedString("instance.alreadyinstance",
                                    instanceName.getText()), App.settings
                                    .getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
                } else if (instanceName.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    JOptionPane.showMessageDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("common.error")
                                    + "<br/><br/>"
                                    + App.settings.getLocalizedString("instance.invalidname",
                                            instanceName.getText()) + "</center></html>",
                            App.settings.getLocalizedString("common.error"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    if (instance.rename(instanceName.getText())) {
                        App.settings.saveInstances();
                        App.settings.reloadInstancesPanel();
                    } else {
                        App.settings.log("Unknown Error Occured While Renaming Instance!",
                                LogMessageType.error, false);
                        JOptionPane.showMessageDialog(
                                RenameInstanceDialog.this,
                                "<html><center>"
                                        + App.settings.getLocalizedString("instance.errorrenaming",
                                                instance.getName() + "<br/><br/>")
                                        + "</center></html>", App.settings
                                        .getLocalizedString("common.error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    close();
                }
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
