/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover languageLabel;
    private JComboBox<String> language;
    private JLabelWithHover languageLabelRestart;
    private JPanel languageLabelPanel;

    private JLabelWithHover themeLabel;
    private JComboBox<String> theme;
    private JLabelWithHover themeLabelRestart;
    private JPanel themeLabelPanel;

    private JLabelWithHover dateFormatLabel;
    private JComboBox<String> dateFormat;

    private JLabelWithHover advancedBackupLabel;
    private JCheckBox advancedBackup;

    private JLabelWithHover sortPacksAlphabeticallyLabel;
    private JCheckBox sortPacksAlphabetically;

    private JLabelWithHover keepLauncherOpenLabel;
    private JCheckBox keepLauncherOpen;

    private JLabelWithHover enableConsoleLabel;
    private JCheckBox enableConsole;

    private JLabelWithHover enableTrayIconLabel;
    private JCheckBox enableTrayIcon;

    private final JButton TCDL_BUTTON = new JButton("Get the creator!") {
        {
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
        }
    };

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        languageLabelRestart = new JLabelWithHover(RESTART_ICON,
                App.settings.getLocalizedString("settings.requiresrestart"), RESTART_BORDER);

        languageLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.language")
                + ":", HELP_ICON, App.settings.getLocalizedString("settings.languagehelp"));

        languageLabelPanel = new JPanel();
        languageLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        languageLabelPanel.add(languageLabelRestart);
        languageLabelPanel.add(languageLabel);

        add(languageLabelPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<String>(Language.available());
        language.setSelectedItem(Language.current());
        add(language, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        themeLabelRestart = new JLabelWithHover(RESTART_ICON,
                App.settings.getLocalizedString("settings.requiresrestart"), RESTART_BORDER);

        themeLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.theme") + ":",
                HELP_ICON, App.settings.getLocalizedString("settings.themehelp"));

        themeLabelPanel = new JPanel();
        themeLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        themeLabelPanel.add(themeLabelRestart);
        themeLabelPanel.add(themeLabel);

        add(themeLabelPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<String>();
        for (String themee : App.settings.getThemesDir().list(Utils.getThemesFileFilter())) {
            theme.addItem(themee.replace(".json", ""));
        }
        theme.setSelectedItem(App.settings.getTheme());

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        dateFormatLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.dateformat") + ":", HELP_ICON,
                App.settings.getLocalizedString("settings.dateformathelp"));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<String>();
        dateFormat.addItem("dd/M/yyy");
        dateFormat.addItem("M/dd/yyy");
        dateFormat.addItem("yyy/M/dd");
        dateFormat.setSelectedItem(App.settings.getDateFormat());

        add(dateFormat, gbc);

        // Advanced Backup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        advancedBackupLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.advancedbackup") + "?", HELP_ICON,
                "<html>" + App.settings.getLocalizedString("settings.advancedbackuphelp", "<br/>")
                        + "</html>");
        add(advancedBackupLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        advancedBackup = new JCheckBox();
        if (App.settings.isAdvancedBackupsEnabled()) {
            advancedBackup.setSelected(true);
        }
        add(advancedBackup, gbc);

        // Sort Packs Alphabetically

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        sortPacksAlphabeticallyLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.sortpacksalphabetically") + "?",
                HELP_ICON, App.settings.getLocalizedString("settings.sortpacksalphabeticallyhelp"));
        add(sortPacksAlphabeticallyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        sortPacksAlphabetically = new JCheckBox();
        if (App.settings.sortPacksAlphabetically()) {
            sortPacksAlphabetically.setSelected(true);
        }
        add(sortPacksAlphabetically, gbc);

        // Keep Launcher Open

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        keepLauncherOpenLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.keeplauncheropen") + "?", HELP_ICON,
                App.settings.getLocalizedString("settings.keeplauncheropenhelp"));
        add(keepLauncherOpenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        keepLauncherOpen = new JCheckBox();
        if (App.settings.keepLauncherOpen()) {
            keepLauncherOpen.setSelected(true);
        }
        add(keepLauncherOpen, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableConsoleLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.console") + "?", HELP_ICON,
                App.settings.getLocalizedString("settings.consolehelp"));
        add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        if (App.settings.enableConsole()) {
            enableConsole.setSelected(true);
        }
        add(enableConsole, gbc);

        // Enable Tray Icon

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableTrayIconLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.traymenu") + "?", HELP_ICON, "<html>"
                        + App.settings.getLocalizedString("settings.traymenuhelp", "<br/>")
                        + "</html>");
        add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (App.settings.enableTrayIcon()) {
            enableTrayIcon.setSelected(true);
        }
        add(enableTrayIcon, gbc);
    }

    public boolean needToReloadTheme() {
        return !((String) theme.getSelectedItem()).equalsIgnoreCase(App.settings.getTheme());
    }

    public boolean needToReloadPacksPanel() {
        return sortPacksAlphabetically.isSelected() != App.settings.sortPacksAlphabetically();
    }

    public boolean needToRestartLauncher() {
        return !((String) language.getSelectedItem()).equalsIgnoreCase(Language.current());
    }

    public void save() {
        App.settings.setLanguage((String) language.getSelectedItem());
        App.settings.setTheme((String) theme.getSelectedItem());
        App.settings.setDateFormat((String) dateFormat.getSelectedItem());
        App.settings.setAdvancedBackups(advancedBackup.isSelected());
        App.settings.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
        App.settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        App.settings.setEnableConsole(enableConsole.isSelected());
        App.settings.setEnableTrayIcon(enableTrayIcon.isSelected());
    }
}
