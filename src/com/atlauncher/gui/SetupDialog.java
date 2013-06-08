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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.data.Language;
import com.atlauncher.data.Settings;

public class SetupDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel languageLabel, installLocationLabel;
    private JComboBox<Language> language;
    private JButton installLocation;
    private JTextField installLocationField;
    private JFileChooser location;

    private JButton saveButton;
    private Settings settings;

    public SetupDialog(Settings set) {
        super(null, "ATLauncher Setup", ModalityType.APPLICATION_MODAL);
        this.settings = set;
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel("Language: ");
        middle.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<Language>();
        for (Language languagee : settings.getLanguages()) {
            language.addItem(languagee);
        }
        language.setSelectedItem(settings.getLanguage());
        middle.add(language, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        installLocationLabel = new JLabel("Install Location: ");
        middle.add(installLocationLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        installLocation = new JButton("...");
        installLocationField = new JTextField(16);
        installLocationField.setEditable(false);
        installLocationField.setText(settings.getInstallLocation().getAbsolutePath());
        location = new JFileChooser();
        location.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        location.setMultiSelectionEnabled(false);
        location.setCurrentDirectory(settings.getInstallLocation());
        installLocation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int done = location.showSaveDialog(SetupDialog.this);
                if (done == JFileChooser.APPROVE_OPTION) {
                    installLocationField.setText(location.getSelectedFile().getAbsolutePath());
                }
            }
        });
        middle.add(installLocationField, gbc);
        gbc.gridx++;
        middle.add(installLocation, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settings.setLanguage((Language) language.getSelectedItem());
                settings.setInstallLocation(location.getSelectedFile().getAbsolutePath());
                settings.saveProperties();
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
