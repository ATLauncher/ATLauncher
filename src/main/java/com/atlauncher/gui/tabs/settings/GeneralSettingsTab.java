/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.GridBagConstraints;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover languageLabel;
    private JComboBox<String> language;
    private JButton translateButton;
    private JLabelWithHover themeLabel;
    private JComboBox<ComboItem> theme;
    private JLabelWithHover dateFormatLabel;
    private JComboBox<ComboItem> dateFormat;
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
    private JLabelWithHover enableFeralGamemodeLabel;
    private JCheckBox enableFeralGamemode;
    private JLabelWithHover enablePackTagsLabel;
    private JCheckBox enablePackTags;
    private JLabelWithHover disableAddModRestrictionsLabel;
    private JCheckBox disableAddModRestrictions;

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

        languageLabel = new JLabelWithHover(GetText.tr("Language") + ":", HELP_ICON,
                GetText.tr("This specifies the language used by the Launcher."));
        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel languagePanel = new JPanel();
        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.X_AXIS));

        language = new JComboBox<>(Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new));
        language.setSelectedItem(Language.selected);
        languagePanel.add(language);

        languagePanel.add(Box.createHorizontalStrut(5));

        translateButton = new JButton(GetText.tr("Help Translate"));
        translateButton.addActionListener(e -> OS.openWebBrowser(Constants.CROWDIN_URL));
        languagePanel.add(translateButton);

        add(languagePanel, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        themeLabel = new JLabelWithHover(GetText.tr("Theme") + ":", HELP_ICON,
                GetText.tr("This sets the theme that the launcher will use."));

        add(themeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<>();
        theme.addItem(new ComboItem("com.atlauncher.themes.Dark", "ATLauncher Dark (default)"));
        theme.addItem(new ComboItem("com.atlauncher.themes.Light", "ATLauncher Light"));
        theme.addItem(new ComboItem("com.atlauncher.themes.MonokaiPro", "Monokai Pro"));
        theme.addItem(new ComboItem("com.atlauncher.themes.DraculaContrast", "Dracula Contrast"));
        theme.addItem(new ComboItem("com.atlauncher.themes.HiberbeeDark", "Hiberbee Dark"));
        theme.addItem(new ComboItem("com.atlauncher.themes.Vuesion", "Vuesion"));
        theme.addItem(new ComboItem("com.atlauncher.themes.MaterialPalenightContrast", "Material Palenight Contrast"));
        theme.addItem(new ComboItem("com.atlauncher.themes.ArcOrange", "Arc Orange"));
        theme.addItem(new ComboItem("com.atlauncher.themes.CyanLight", "Cyan Light"));

        for (int i = 0; i < theme.getItemCount(); i++) {
            ComboItem item = theme.getItemAt(i);

            if (item.getValue().equalsIgnoreCase(App.settings.getTheme())) {
                theme.setSelectedIndex(i);
                break;
            }
        }

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        dateFormatLabel = new JLabelWithHover(GetText.tr("Date Format") + ":", HELP_ICON,
                GetText.tr("This controls the format that dates are displayed in the launcher."));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<>();

        for (String format : Constants.DATE_FORMATS) {
            dateFormat.addItem(new ComboItem(format, new SimpleDateFormat(format).format(new Date())));
        }

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
        gbc.insets = CHECKBOX_FIELD_INSETS;
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
        gbc.insets = CHECKBOX_FIELD_INSETS;
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
        gbc.insets = CHECKBOX_FIELD_INSETS;
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
        gbc.insets = CHECKBOX_FIELD_INSETS;
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
        gbc.insets = CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableDiscordIntegration = new JCheckBox();
        if (App.settings.enableDiscordIntegration()) {
            enableDiscordIntegration.setSelected(true);
        }
        add(enableDiscordIntegration, gbc);

        // Enable Feral Gamemode

        if (OS.isLinux()) {
            boolean gameModeExistsInPath = Utils.executableInPath("gamemoderun");

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            enableFeralGamemodeLabel = new JLabelWithHover(GetText.tr("Enable Feral Gamemode") + "?", HELP_ICON,
                    GetText.tr("This will enable Feral Gamemode for packs launched."));
            add(enableFeralGamemodeLabel, gbc);

            gbc.gridx++;
            gbc.insets = CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            enableFeralGamemode = new JCheckBox();
            if (App.settings.enableFeralGamemode()) {
                enableFeralGamemode.setSelected(true);
            }

            if (!gameModeExistsInPath) {
                enableFeralGamemodeLabel.setToolTipText(GetText.tr(
                        "This will enable Feral Gamemode for packs launched (disabled because gamemoderun not found in PATH, please install Feral Gamemode or add it to your PATH)."));
                enableFeralGamemodeLabel.setEnabled(false);

                enableFeralGamemode.setEnabled(false);
                enableFeralGamemode.setSelected(false);
            }
            add(enableFeralGamemode, gbc);
        }

        // Enable Pack Tags

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enablePackTagsLabel = new JLabelWithHover(GetText.tr("Enable Pack Tags?"), HELP_ICON,
                GetText.tr("Pack tags shows you if a pack is public, semi public or private"));
        add(enablePackTagsLabel, gbc);

        gbc.gridx++;
        gbc.insets = CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enablePackTags = new JCheckBox();
        enablePackTags.setSelected(App.settings.enabledPackTags());
        add(enablePackTags, gbc);

        // Disable Curse Minecraft version restrictions

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        disableAddModRestrictionsLabel = new JLabelWithHover(GetText.tr("Disable Add Mod Restrictions?"), HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This will allow you to disable the restrictions in place to prevent you from installing mods from Curse that are not for your current Minecraft version or loader.<br/><br/>By disabling these restrictions, you can install any mod, so be sure that it's compatable with the Minecraft version and loader (if any) that you're on."))
                        .build());
        add(disableAddModRestrictionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        disableAddModRestrictions = new JCheckBox();
        disableAddModRestrictions.setSelected(App.settings.disabledAddModRestrictions());
        add(disableAddModRestrictions, gbc);
    }

    public boolean needToReloadTheme() {
        return !((ComboItem) theme.getSelectedItem()).getValue().equalsIgnoreCase(App.settings.getTheme());
    }

    public boolean needToReloadPacksPanel() {
        return sortPacksAlphabetically.isSelected() != App.settings.sortPacksAlphabetically();
    }

    public boolean needToReloadLanguage() {
        return !((String) language.getSelectedItem()).equalsIgnoreCase(Language.selected);
    }

    public void save() {
        Language.setLanguage((String) language.getSelectedItem());
        App.settings.setTheme(((ComboItem) theme.getSelectedItem()).getValue());
        App.settings.setDateFormat(((ComboItem) dateFormat.getSelectedItem()).getValue());
        App.settings.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
        App.settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        App.settings.setEnableConsole(enableConsole.isSelected());
        App.settings.setEnableTrayIcon(enableTrayIcon.isSelected());
        App.settings.setEnableDiscordIntegration(enableDiscordIntegration.isSelected());

        if (OS.isLinux()) {
            App.settings.setEnableFeralGameMode(enableFeralGamemode.isSelected());
        } else {
            App.settings.setEnableFeralGameMode(false);
        }

        App.settings.setPackTags(enablePackTags.isSelected());
        App.settings.setAddModRestrictions(disableAddModRestrictions.isSelected());
    }

    @Override
    public String getTitle() {
        return GetText.tr("General");
    }
}
