/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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
import javax.swing.JPanel;

public class SetupDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;
    private JButton saveButton;

    public SetupDialog() {
        super(null, "ATLauncher Setup", ModalityType.APPLICATION_MODAL);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel("Setting up ATLauncher"));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // gbc.gridx = 0;
        // gbc.gridy = 0;
        // gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        // instanceNameLabel = new JLabel("Instance Name: ");
        // middle.add(instanceNameLabel, gbc);
        //
        // gbc.gridx++;
        // gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        // instanceNameField = new JTextField(17);
        // instanceNameField.setText(pack.getName());
        // middle.add(instanceNameField, gbc);
        //
        // gbc.gridx = 0;
        // gbc.gridy = 1;
        // gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        // versionLabel = new JLabel("Version To Install: ");
        // middle.add(versionLabel, gbc);
        //
        // gbc.gridx++;
        // gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        // versionsDropDown = new JComboBox<Version>();
        // for (int i = 0; i < pack.getVersionCount(); i++) {
        // versionsDropDown.addItem(pack.getVersion(i));
        // }
        // versionsDropDown.setPreferredSize(new Dimension(200, 25));
        // middle.add(versionsDropDown, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

}
