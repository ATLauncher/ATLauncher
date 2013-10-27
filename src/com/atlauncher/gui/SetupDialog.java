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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.atlauncher.data.Language;
import com.atlauncher.data.Settings;
import com.atlauncher.utils.Utils;

public class SetupDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel languageLabel;
    private JComboBox<Language> language;

    private JLabel enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JButton saveButton;
    private Settings settings;

    public SetupDialog(Settings set) {
        super(null, "ATLauncher Setup", ModalityType.APPLICATION_MODAL);
        this.requestFocus();
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
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabel("Enable Leaderboards? ");
        middle.add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        middle.add(enableLeaderboards, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settings.setLanguage((Language) language.getSelectedItem());
                settings.setEnableLeaderboards(enableLeaderboards.isSelected());
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
