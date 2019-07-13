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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

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

    private JLabel enableAnalyticsLabel;
    private JCheckBox enableAnalytics;

    private JButton saveButton;

    public SetupDialog() {
        // #. {0} is the name of the launcher (ATLauncher)
        super(null, GetText.tr("{0} Setup", Constants.LAUNCHER_NAME), ModalityType.APPLICATION_MODAL);
        this.requestFocus();
        this.setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();

        // #. {0} is the name of the launcher (ATLauncher)
        top.add(new JLabel(GetText.tr("Setting up {0}", Constants.LAUNCHER_NAME)));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel(GetText.tr("Language") + ": ");
        middle.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<>(Language.languages.keySet().toArray(new String[Language.languages.size()]));
        language.setSelectedItem(Language.selected);
        middle.add(language, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabel(GetText.tr("Enable Leaderboards") + "? ");
        middle.add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        enableLeaderboards.setSelected(true);
        middle.add(enableLeaderboards, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableAnalyticsLabel = new JLabelWithHover(GetText.tr("Enable Anonymous Analytics") + "? ",
                Utils.getIconImage("/assets/image/Help.png"),
                "<html>" + GetText.tr(
                        "The Launcher sends back anonymous analytics to Google Analytics%sin order to track what people do and don't use in the launcher.<br/>This helps determine what new features we implement in the future.<br/>All analytics are anonymous and contain no user/instance information in it at all.<br/>If you don't want to send anonymous analytics, you can disable this option.")
                        + "</html>");
        middle.add(enableAnalyticsLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableAnalytics = new JCheckBox();
        enableAnalytics.setSelected(true);
        middle.add(enableAnalytics, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(e -> {
            App.settings.setLanguage((String) language.getSelectedItem());
            App.settings.setEnableLeaderboards(enableLeaderboards.isSelected());
            App.settings.setEnableAnalytics(enableAnalytics.isSelected());
            App.settings.saveProperties();
            setVisible(false);
            dispose();
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
