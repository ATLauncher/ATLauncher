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
package com.atlauncher.gui.tabs.settings;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover languageLabel;
    private JComboBox<String> language;
    private JLabelWithHover languageLabelRestart;
    private JPanel languagePanel;
    private JLabelWithHover themeLabel;
    private JComboBox<String> theme;
    private JLabelWithHover themeLabelRestart;
    private JPanel themeLabelPanel;
    private JLabelWithHover dateFormatLabel;
    private JComboBox<String> dateFormat;
    private JLabelWithHover sortPacksAlphabeticallyLabel;
    private JCheckBox sortPacksAlphabetically;
    private JLabelWithHover keepLauncherOpenLabel;
    private JCheckBox keepLauncherOpen;
    private JLabelWithHover enableConsoleLabel;
    private JCheckBox enableConsole;
    private JLabelWithHover enableTrayIconLabel;
    private JCheckBox enableTrayIcon;
    private JLabelWithHover enableDiscordIntegrationLabel;
    private JCheckBox enableDiscordIntegration;
    private JLabelWithHover enablePackTagsLabel;
    private JCheckBox enablePackTags;

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        languageLabel = new JLabelWithHover(GetText.tr("Language") + ":", HELP_ICON,
                GetText.tr("This specifies the Language used by the Launcher."));
        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<>(Language.locales.stream().map(l -> l.getDisplayName()).toArray(String[]::new));
        language.setSelectedItem(Language.selected);
        add(language, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        themeLabelRestart = new JLabelWithHover(ERROR_ICON,
                GetText.tr("Changing this setting will automatically restart the launcher."), RESTART_BORDER);

        themeLabel = new JLabelWithHover(GetText.tr("Theme") + ":", HELP_ICON,
                GetText.tr("This sets the theme that the launcher will use."));

        themeLabelPanel = new JPanel();
        themeLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        themeLabelPanel.add(themeLabelRestart);
        themeLabelPanel.add(themeLabel);

        add(themeLabelPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<>();
        for (String themee : FileSystem.THEMES.toFile().list(Utils.getThemesFileFilter())) {
            theme.addItem(themee.replace(".zip", ""));
        }
        theme.setSelectedItem(App.settings.getTheme());

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        dateFormatLabel = new JLabelWithHover(GetText.tr("Date Format") + ":", HELP_ICON, GetText.tr(
                "This controls the format that dates are displayed in the launcher with the value dd meaning the day, M being the month and yyy being the year."));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<>();
        dateFormat.addItem("dd/M/yyy");
        dateFormat.addItem("M/dd/yyy");
        dateFormat.addItem("yyy/M/dd");
        dateFormat.setSelectedItem(App.settings.getDateFormat());

        add(dateFormat, gbc);

        // Sort Packs Alphabetically

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        sortPacksAlphabeticallyLabel = new JLabelWithHover(GetText.tr("Sort Packs Alphabetically") + "?", HELP_ICON,
                GetText.tr("If you want to sort the packs in the packs panel alphabetically by default or not."));
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
        keepLauncherOpenLabel = new JLabelWithHover(GetText.tr("Keep Launcher Open") + "?", HELP_ICON,
                GetText.tr("This determines if ATLauncher should stay open or exit after Minecraft has exited"));
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
        enableConsoleLabel = new JLabelWithHover(GetText.tr("Enable Console") + "?", HELP_ICON,
                GetText.tr("If you want the console to be visible when opening the Launcher."));
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
        enableTrayIconLabel = new JLabelWithHover(GetText.tr("Enable Tray Menu") + "?", HELP_ICON, "<html>" + GetText
                .tr("The Tray Menu is a little icon that shows in your system taskbar which<br/>allows you to perform different functions to do various things with the launcher<br/>such as hiding or showing the console, killing Minecraft or closing ATLauncher.")
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

        // Enable Discord Integration

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableDiscordIntegrationLabel = new JLabelWithHover(GetText.tr("Enable Discord Integration") + "?", HELP_ICON,
                GetText.tr("This will enable showing which pack you're playing in Discord."));
        add(enableDiscordIntegrationLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableDiscordIntegration = new JCheckBox();
        if (App.settings.enableDiscordIntegration()) {
            enableDiscordIntegration.setSelected(true);
        }
        add(enableDiscordIntegration, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enablePackTagsLabel = new JLabelWithHover(GetText.tr("Enable Pack Tags"), HELP_ICON,
                GetText.tr("Pack tags shows you if a pack is public, semi public or private"));
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

    public boolean needToReloadLanguage() {
        return !((String) language.getSelectedItem()).equalsIgnoreCase(Language.selected);
    }

    public void save() {
        Language.setLanguage((String) language.getSelectedItem());
        App.settings.setTheme((String) theme.getSelectedItem());
        App.settings.setDateFormat((String) dateFormat.getSelectedItem());
        App.settings.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
        App.settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        App.settings.setEnableConsole(enableConsole.isSelected());
        App.settings.setEnableTrayIcon(enableTrayIcon.isSelected());
        App.settings.setEnableDiscordIntegration(enableDiscordIntegration.isSelected());
        App.settings.setPackTags(enablePackTags.isSelected());
    }

    @Override
    public String getTitle() {
        return GetText.tr("General");
    }
}
