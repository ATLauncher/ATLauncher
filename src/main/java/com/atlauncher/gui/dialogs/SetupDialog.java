/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SetupDialog extends JDialog {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -2931970914611329658L;
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel languageLabel;
    private JComboBox<String> language;

    private JLabel enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JButton saveButton;

    public SetupDialog() {
        super(null, "ATLauncher Setup", ModalityType.APPLICATION_MODAL);
        this.requestFocus();
        this.setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
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
        language = new JComboBox<String>(Language.available());
        language.setSelectedItem(Language.current());
        middle.add(language, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabel("Enable Leaderboards? ");
        middle.add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        enableLeaderboards.setSelected(true);
        middle.add(enableLeaderboards, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.setLanguage((String) language.getSelectedItem());
                App.settings.setEnableLeaderboards(enableLeaderboards.isSelected());
                App.settings.saveProperties();
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