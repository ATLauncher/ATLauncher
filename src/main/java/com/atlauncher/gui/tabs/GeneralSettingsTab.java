/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {

    private JLabel languageLabel;
    private JComboBox<Language> language;

    private JLabel themeLabel;
    private JComboBox<String> theme;

    private JLabel advancedBackupLabel;
    private JCheckBox advancedBackup;

    private JLabel sortPacksAlphabeticallyLabel;
    private JCheckBox sortPacksAlphabetically;

    private JLabel keepLauncherOpenLabel;
    private JCheckBox keepLauncherOpen;

    private JLabel enableConsoleLabel;
    private JCheckBox enableConsole;

    private JLabel enableTrayIconLabel;
    private JCheckBox enableTrayIcon;

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel(App.settings.getLocalizedString("settings.language") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        languageLabel.setIcon(helpIcon);
        languageLabel.setToolTipText(App.settings.getLocalizedString("settings.languagehelp"));
        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<Language>();
        for (Language languagee : App.settings.getLanguages()) {
            language.addItem(languagee);
        }
        language.setSelectedItem(App.settings.getLanguage());
        add(language, gbc);

        // Theme
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        themeLabel = new JLabel(App.settings.getLocalizedString("settings.theme") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        themeLabel.setIcon(helpIcon);
        themeLabel.setToolTipText(App.settings.getLocalizedString("settings.themehelp"));
        add(themeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<String>();
        for (String themee : App.settings.getThemesDir().list(Utils.getThemesFileFilter())) {
            theme.addItem(themee.replace(".json", ""));
        }
        theme.setSelectedItem(App.settings.getTheme());
        add(theme, gbc);

        // Advanced Backup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        advancedBackupLabel = new JLabel(App.settings.getLocalizedString("settings.advancedbackup")
                + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        advancedBackupLabel.setIcon(helpIcon);
        advancedBackupLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.advancedbackuphelp", "<br/>")
                + "</center></html>");
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
        sortPacksAlphabeticallyLabel = new JLabel(
                App.settings.getLocalizedString("settings.sortpacksalphabetically") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        sortPacksAlphabeticallyLabel.setIcon(helpIcon);
        sortPacksAlphabeticallyLabel.setToolTipText(App.settings
                .getLocalizedString("settings.sortpacksalphabeticallyhelp"));
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
        keepLauncherOpenLabel = new JLabel(
                App.settings.getLocalizedString("settings.keeplauncheropen") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        keepLauncherOpenLabel.setIcon(helpIcon);
        keepLauncherOpenLabel.setToolTipText(App.settings
                .getLocalizedString("settings.keeplauncheropenhelp"));
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
        enableConsoleLabel = new JLabel(App.settings.getLocalizedString("settings.console") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableConsoleLabel.setIcon(helpIcon);
        enableConsoleLabel.setToolTipText(App.settings.getLocalizedString("settings.consolehelp"));
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
        enableTrayIconLabel = new JLabel(App.settings.getLocalizedString("settings.traymenu") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableTrayIconLabel.setIcon(helpIcon);
        enableTrayIconLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.traymenuhelp", "<br/>")
                + "</center></html>");
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
        return language.getSelectedItem() != App.settings.getLanguage();
    }

    public void save() {
        App.settings.setLanguage((Language) language.getSelectedItem());
        App.settings.setTheme((String) theme.getSelectedItem());
        App.settings.setAdvancedBackups(advancedBackup.isSelected());
        App.settings.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
        App.settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        App.settings.setEnableConsole(enableConsole.isSelected());
        App.settings.setEnableTrayIcon(enableTrayIcon.isSelected());
    }
}
