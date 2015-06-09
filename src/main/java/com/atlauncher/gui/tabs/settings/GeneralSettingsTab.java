/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.FileUtils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover languageLabel;
    private JComboBox<Language> language;
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
    private JLabelWithHover enablePackTagsLabel;
    private JCheckBox enablePackTags;

    public GeneralSettingsTab() {
        EventHandler.EVENT_BUS.subscribe(this);
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        languageLabel = new JLabelWithHover(LanguageManager.localize("settings.language") + ":", HELP_ICON,
                LanguageManager.localize("settings.languagehelp"));

        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<>(Data.LANGUAGES.toArray(new Language[Data.LANGUAGES.size()]));
        language.setSelectedItem(LanguageManager.getLanguage());
        add(language, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        themeLabelRestart = new JLabelWithHover(ERROR_ICON, LanguageManager.localize("settings" + "" +
                ".requiresrestart"), RESTART_BORDER);

        themeLabel = new JLabelWithHover(LanguageManager.localize("settings.theme") + ":", HELP_ICON, LanguageManager.localize("settings.themehelp"));

        themeLabelPanel = new JPanel();
        themeLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        themeLabelPanel.add(themeLabelRestart);
        themeLabelPanel.add(themeLabel);

        add(themeLabelPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<>();
        for (String themee : FileSystem.THEMES.toFile().list(FileUtils.getThemesFileFilter())) {
            theme.addItem(themee.replace(".zip", ""));
        }
        theme.setSelectedItem(SettingsManager.getTheme());

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        dateFormatLabel = new JLabelWithHover(LanguageManager.localize("settings.dateformat") + ":", HELP_ICON,
                LanguageManager.localize("settings.dateformathelp"));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<String>();
        dateFormat.addItem("dd/M/yyy");
        dateFormat.addItem("M/dd/yyy");
        dateFormat.addItem("yyy/M/dd");
        dateFormat.setSelectedItem(SettingsManager.getDateFormat());

        add(dateFormat, gbc);

        // Advanced Backup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        advancedBackupLabel = new JLabelWithHover(LanguageManager.localize("settings.advancedbackup") + "?",
                HELP_ICON, "<html>" + LanguageManager.localizeWithReplace("settings.advancedbackuphelp", "<br/>") +
                "</html>");
        add(advancedBackupLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        advancedBackup = new JCheckBox();
        if (SettingsManager.isAdvancedBackupsEnabled()) {
            advancedBackup.setSelected(true);
        }
        add(advancedBackup, gbc);

        // Sort Packs Alphabetically

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        sortPacksAlphabeticallyLabel = new JLabelWithHover(LanguageManager.localize("settings" + "" +
                ".sortpacksalphabetically") + "?", HELP_ICON, LanguageManager.localize("settings" + "" +
                ".sortpacksalphabeticallyhelp"));
        add(sortPacksAlphabeticallyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        sortPacksAlphabetically = new JCheckBox();
        if (SettingsManager.sortPacksAlphabetically()) {
            sortPacksAlphabetically.setSelected(true);
        }
        add(sortPacksAlphabetically, gbc);

        // Keep Launcher Open

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        keepLauncherOpenLabel = new JLabelWithHover(LanguageManager.localize("settings.keeplauncheropen") + "?",
                HELP_ICON, LanguageManager.localize("settings.keeplauncheropenhelp"));
        add(keepLauncherOpenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        keepLauncherOpen = new JCheckBox();
        if (SettingsManager.keepLauncherOpen()) {
            keepLauncherOpen.setSelected(true);
        }
        add(keepLauncherOpen, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableConsoleLabel = new JLabelWithHover(LanguageManager.localize("settings.console") + "?", HELP_ICON,
                LanguageManager.localize("settings.consolehelp"));
        add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        if (SettingsManager.enableConsole()) {
            enableConsole.setSelected(true);
        }
        add(enableConsole, gbc);

        // Enable Tray Icon

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableTrayIconLabel = new JLabelWithHover(LanguageManager.localize("settings.traymenu") + "?", HELP_ICON,
                "<html>" + LanguageManager.localizeWithReplace("settings.traymenuhelp", "<br/>") + "</html>");
        add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (SettingsManager.enableTrayIcon()) {
            enableTrayIcon.setSelected(true);
        }
        add(enableTrayIcon, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enablePackTagsLabel = new JLabelWithHover(LanguageManager.localize("settings.packtags"), HELP_ICON,
                LanguageManager.localize("settings.packtagshelp"));
        add(enablePackTagsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enablePackTags = new JCheckBox();
        enablePackTags.setSelected(SettingsManager.enabledPackTags());
        add(enablePackTags, gbc);
    }

    public boolean needToReloadTheme() {
        return !((String) theme.getSelectedItem()).equalsIgnoreCase(SettingsManager.getTheme());
    }

    public boolean needToReloadPacksPanel() {
        return sortPacksAlphabetically.isSelected() != SettingsManager.sortPacksAlphabetically();
    }

    public boolean reloadLocalizationTable() {
        return language.getSelectedItem() != LanguageManager.getLanguage();
    }

    public void save() {
        SettingsManager.setLanguage(((Language) language.getSelectedItem()).getCode());
        SettingsManager.setTheme((String) theme.getSelectedItem());
        SettingsManager.setDateFormat((String) dateFormat.getSelectedItem());
        SettingsManager.setAdvancedBackups(advancedBackup.isSelected());
        SettingsManager.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
        SettingsManager.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        SettingsManager.setEnableConsole(enableConsole.isSelected());
        SettingsManager.setEnableTrayIcon(enableTrayIcon.isSelected());
        SettingsManager.setPackTags(enablePackTags.isSelected());
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("settings.generaltab");
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.languageLabel.setText(LanguageManager.localize("settings.language") + ":");
        this.languageLabel.setToolTipText(LanguageManager.localize("settings.languagehelp"));

        this.themeLabelRestart.setToolTipText(LanguageManager.localize("settings.requiresrestart"));

        this.themeLabel.setText(LanguageManager.localize("settings.theme") + ":");
        this.themeLabel.setToolTipText(LanguageManager.localize("settings.themehelp"));

        this.dateFormatLabel.setText(LanguageManager.localize("settings.dateformat") + ":");
        this.dateFormatLabel.setToolTipText(LanguageManager.localize("settings.dateformathelp"));

        this.advancedBackupLabel.setText(LanguageManager.localize("settings.advancedbackup") + "?");
        this.advancedBackupLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" + "" +
                ".advancedbackuphelp", "<br/>") + "</html>");

        this.sortPacksAlphabeticallyLabel.setText(LanguageManager.localize("settings.sortpacksalphabetically") + "?");
        this.sortPacksAlphabeticallyLabel.setToolTipText(LanguageManager.localize("settings" + "" +
                ".sortpacksalphabeticallyhelp"));

        this.keepLauncherOpenLabel.setText(LanguageManager.localize("settings.keeplauncheropen") + "?");
        this.keepLauncherOpenLabel.setToolTipText(LanguageManager.localize("settings.keeplauncheropenhelp"));

        this.enableConsoleLabel.setText(LanguageManager.localize("settings.console") + "?");
        this.enableConsoleLabel.setToolTipText(LanguageManager.localize("settings.consolehelp"));

        this.enableTrayIconLabel.setText(LanguageManager.localize("settings.traymenu") + "?");
        this.enableTrayIconLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" + "" +
                ".traymenuhelp", "<br/>") + "</html>");
    }
}
