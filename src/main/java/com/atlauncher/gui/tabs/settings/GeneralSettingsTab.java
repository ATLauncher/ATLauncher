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

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private JLabelWithHover languageLabel;
    private JComboBox<String> language;
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
        RelocalizationManager.addListener(this);
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        languageLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.language") + ":", HELP_ICON,
                Language.INSTANCE.localize("settings.languagehelp"));

        add(languageLabel, gbc);

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

        themeLabelRestart = new JLabelWithHover(ERROR_ICON, Language.INSTANCE.localize("settings" + "" +
                ".requiresrestart"), RESTART_BORDER);

        themeLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.theme") + ":", HELP_ICON,
                Language.INSTANCE.localize("settings.themehelp"));

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
            theme.addItem(themee.replace(".zip", ""));
        }
        theme.setSelectedItem(App.settings.getTheme());

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        dateFormatLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.dateformat") + ":", HELP_ICON,
                Language.INSTANCE.localize("settings.dateformathelp"));

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
        advancedBackupLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.advancedbackup") + "?",
                HELP_ICON, "<html>" + Language.INSTANCE.localizeWithReplace("settings.advancedbackuphelp",
                "<br/>") + "</html>");
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
        sortPacksAlphabeticallyLabel = new JLabelWithHover(Language.INSTANCE.localize("settings" + "" +
                ".sortpacksalphabetically") + "?", HELP_ICON, Language.INSTANCE.localize("settings" + "" +
                ".sortpacksalphabeticallyhelp"));
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
        keepLauncherOpenLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.keeplauncheropen") + "?",
                HELP_ICON, Language.INSTANCE.localize("settings.keeplauncheropenhelp"));
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
        enableConsoleLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.console") + "?", HELP_ICON,
                Language.INSTANCE.localize("settings.consolehelp"));
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
        enableTrayIconLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.traymenu") + "?", HELP_ICON,
                "<html>" + Language.INSTANCE.localizeWithReplace("settings.traymenuhelp", "<br/>") + "</html>");
        add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (App.settings.enableTrayIcon()) {
            enableTrayIcon.setSelected(true);
        }
        add(enableTrayIcon, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enablePackTagsLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.packtags"), HELP_ICON,
                Language.INSTANCE.localize("settings.packtagshelp"));
        add(enablePackTagsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enablePackTags = new JCheckBox();
        enablePackTags.setSelected(App.settings.enabledPackTags());
        add(enablePackTags, gbc);
    }

    public boolean needToReloadTheme() {
        return !((String) theme.getSelectedItem()).equalsIgnoreCase(App.settings.getTheme());
    }

    public boolean needToReloadPacksPanel() {
        return sortPacksAlphabetically.isSelected() != App.settings.sortPacksAlphabetically();
    }

    public boolean reloadLocalizationTable() {
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
        App.settings.setPackTags(enablePackTags.isSelected());
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("settings.generaltab");
    }

    @Override
    public void onRelocalization() {
        this.languageLabel.setText(Language.INSTANCE.localize("settings.language") + ":");
        this.languageLabel.setToolTipText(Language.INSTANCE.localize("settings.languagehelp"));

        this.themeLabelRestart.setToolTipText(Language.INSTANCE.localize("settings.requiresrestart"));

        this.themeLabel.setText(Language.INSTANCE.localize("settings.theme") + ":");
        this.themeLabel.setToolTipText(Language.INSTANCE.localize("settings.themehelp"));

        this.dateFormatLabel.setText(Language.INSTANCE.localize("settings.dateformat") + ":");
        this.dateFormatLabel.setToolTipText(Language.INSTANCE.localize("settings.dateformathelp"));

        this.advancedBackupLabel.setText(Language.INSTANCE.localize("settings.advancedbackup") + "?");
        this.advancedBackupLabel.setToolTipText("<html>" + Language.INSTANCE.localizeWithReplace("settings" + "" +
                ".advancedbackuphelp", "<br/>") + "</html>");

        this.sortPacksAlphabeticallyLabel.setText(Language.INSTANCE.localize("settings.sortpacksalphabetically") + "?");
        this.sortPacksAlphabeticallyLabel.setToolTipText(Language.INSTANCE.localize("settings" + "" +
                ".sortpacksalphabeticallyhelp"));

        this.keepLauncherOpenLabel.setText(Language.INSTANCE.localize("settings.keeplauncheropen") + "?");
        this.keepLauncherOpenLabel.setToolTipText(Language.INSTANCE.localize("settings.keeplauncheropenhelp"));

        this.enableConsoleLabel.setText(Language.INSTANCE.localize("settings.console") + "?");
        this.enableConsoleLabel.setToolTipText(Language.INSTANCE.localize("settings.consolehelp"));

        this.enableTrayIconLabel.setText(Language.INSTANCE.localize("settings.traymenu") + "?");
        this.enableTrayIconLabel.setToolTipText("<html>" + Language.INSTANCE.localizeWithReplace("settings" + "" +
                ".traymenuhelp", "<br/>") + "</html>");
    }
}
