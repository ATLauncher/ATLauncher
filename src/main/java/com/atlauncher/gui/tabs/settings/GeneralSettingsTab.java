/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.sort.InstanceSortingStrategies;

@SuppressWarnings("serial")
public class GeneralSettingsTab extends AbstractSettingsTab {
    private final JComboBox<String> language;
    private final JComboBox<ComboItem<String>> theme;
    private final JComboBox<ComboItem<String>> dateFormat;
    private final JComboBox<ComboItem<String>> instanceTitleFormat;
    private final JComboBox<ComboItem<Integer>> selectedTabOnStartup;
    private final JComboBox<InstanceSortingStrategies> defaultInstanceSorting;

    private final JLabelWithHover customDownloadsPathLabel;
    private JTextField customDownloadsPath;
    private final JButton customDownloadsPathResetButton;
    private final JButton customDownloadsPathBrowseButton;

    private final JCheckBox keepLauncherOpen;
    private final JCheckBox enableConsole;
    private final JCheckBox enableTrayIcon;
    private final JCheckBox enableDiscordIntegration;
    private JCheckBox enableFeralGamemode;
    private final JCheckBox disableCustomFonts;
    private final JCheckBox rememberWindowSizePosition;
    private final JCheckBox useNativeFilePicker;
    private final JCheckBox useRecycleBin;

    public GeneralSettingsTab() {
        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

        JLabelWithHover languageLabel = new JLabelWithHover(GetText.tr("Language") + ":", HELP_ICON,
                GetText.tr("This specifies the language used by the Launcher."));
        add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel languagePanel = new JPanel();
        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.X_AXIS));

        language = new JComboBox<>(Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new));
        language.setSelectedItem(Language.selected);
        languagePanel.add(language);

        languagePanel.add(Box.createHorizontalStrut(5));

        JButton translateButton = new JButton(GetText.tr("Help Translate"));
        translateButton.addActionListener(e -> OS.openWebBrowser(Constants.CROWDIN_URL));
        languagePanel.add(translateButton);

        add(languagePanel, gbc);

        // Theme

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover themeLabel = new JLabelWithHover(GetText.tr("Theme") + ":", HELP_ICON,
                GetText.tr("This sets the theme that the launcher will use."));

        add(themeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        theme = new JComboBox<>();
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Dark", "ATLauncher Dark (default)"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Light", "ATLauncher Light"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.MonokaiPro", "Monokai Pro"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.DraculaContrast", "Dracula Contrast"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.HiberbeeDark", "Hiberbee Dark"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.Vuesion", "Vuesion"));
        theme.addItem(
                new ComboItem<>("com.atlauncher.themes.MaterialPalenightContrast", "Material Palenight Contrast"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.ArcOrange", "Arc Orange"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.CyanLight", "Cyan Light"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.HighTechDarkness", "High Tech Darkness"));
        theme.addItem(new ComboItem<>("com.atlauncher.themes.OneDark", "One Dark"));

        for (int i = 0; i < theme.getItemCount(); i++) {
            ComboItem<String> item = theme.getItemAt(i);

            if (item.getValue().equalsIgnoreCase(App.settings.theme)) {
                theme.setSelectedIndex(i);
                break;
            }
        }

        add(theme, gbc);

        // Date Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover dateFormatLabel = new JLabelWithHover(GetText.tr("Date Format") + ":", HELP_ICON,
                GetText.tr("This controls the format that dates are displayed in the launcher."));

        add(dateFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dateFormat = new JComboBox<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        Date exampleDate = cal.getTime();

        for (String format : Constants.DATE_FORMATS) {
            dateFormat.addItem(new ComboItem<>(format, new SimpleDateFormat(format).format(exampleDate)));
        }

        dateFormat.setSelectedItem(App.settings.dateFormat);

        add(dateFormat, gbc);

        // Instance Title Format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover instanceTitleFormatLabel = new JLabelWithHover(GetText.tr("Instance Title Format") + ":",
                HELP_ICON, GetText.tr("This controls the format that instances titles are shown as."));

        add(instanceTitleFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceTitleFormat = new JComboBox<>();

        for (String format : Constants.INSTANCE_TITLE_FORMATS) {
            instanceTitleFormat.addItem(new ComboItem<>(format, String.format(format, GetText.tr("Instance Name"),
                    GetText.tr("Pack Name"), GetText.tr("Pack Version"), GetText.tr("Minecraft Version"))));
        }

        instanceTitleFormat.setSelectedItem(App.settings.instanceTitleFormat);

        add(instanceTitleFormat, gbc);

        // Selected tab on startup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover selectedTabOnStartupLabel = new JLabelWithHover(GetText.tr("Default Tab") + ":", HELP_ICON,
                GetText.tr("Which tab to have selected by default when opening the launcher."));

        add(selectedTabOnStartupLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        selectedTabOnStartup = new JComboBox<>();
        selectedTabOnStartup.addItem(new ComboItem<>(0, GetText.tr("News")));
        selectedTabOnStartup.addItem(new ComboItem<>(1, GetText.tr("Vanilla Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(2, GetText.tr("Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(3, GetText.tr("Instances")));
        selectedTabOnStartup.addItem(new ComboItem<>(4, GetText.tr("Servers")));
        selectedTabOnStartup.addItem(new ComboItem<>(5, GetText.tr("Accounts")));
        selectedTabOnStartup.addItem(new ComboItem<>(6, GetText.tr("Tools")));
        selectedTabOnStartup.addItem(new ComboItem<>(7, GetText.tr("Settings")));
        selectedTabOnStartup.setSelectedItem(App.settings.selectedTabOnStartup);

        for (int i = 0; i < selectedTabOnStartup.getItemCount(); i++) {
            ComboItem<Integer> item = selectedTabOnStartup.getItemAt(i);

            if (item.getValue() == App.settings.selectedTabOnStartup) {
                selectedTabOnStartup.setSelectedIndex(i);
                break;
            }
        }

        add(selectedTabOnStartup, gbc);

        // Default instance sorting

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover defaultInstanceSortingLabel = new JLabelWithHover(GetText.tr("Default Instance Sort") + ":", HELP_ICON,
                GetText.tr("Default sorting of instances under the Instances tab."));

        add(defaultInstanceSortingLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        defaultInstanceSorting = new JComboBox<>(InstanceSortingStrategies.values());
        defaultInstanceSorting.setSelectedItem(App.settings.defaultInstanceSorting);

        add(defaultInstanceSorting, gbc);

        // Custom Downloads Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        customDownloadsPathLabel = new JLabelWithHover(GetText.tr("Downloads Folder") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to change the Downloads folder that the launcher looks in when downloading browser mods."))
                        .build());
        add(customDownloadsPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel customDownloadsPathPanel = new JPanel();
        customDownloadsPathPanel.setLayout(new BoxLayout(customDownloadsPathPanel, BoxLayout.X_AXIS));

        customDownloadsPath = new JTextField(16);
        customDownloadsPath.setText(
                Optional.ofNullable(App.settings.customDownloadsPath)
                        .orElse(FileSystem.getUserDownloadsPath(false).toString()));
        customDownloadsPathResetButton = new JButton(GetText.tr("Reset"));
        customDownloadsPathResetButton.addActionListener(
                e -> customDownloadsPath.setText(FileSystem.getUserDownloadsPath(false).toString()));
        customDownloadsPathBrowseButton = new JButton(GetText.tr("Browse"));
        customDownloadsPathBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(customDownloadsPath.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedPath = chooser.getSelectedFile();
                customDownloadsPath.setText(selectedPath.getAbsolutePath());
            }
        });

        customDownloadsPathPanel.add(customDownloadsPath);
        customDownloadsPathPanel.add(Box.createHorizontalStrut(5));
        customDownloadsPathPanel.add(customDownloadsPathResetButton);
        customDownloadsPathPanel.add(Box.createHorizontalStrut(5));
        customDownloadsPathPanel.add(customDownloadsPathBrowseButton);

        add(customDownloadsPathPanel, gbc);

        // Keep Launcher Open

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover keepLauncherOpenLabel = new JLabelWithHover(GetText.tr("Keep Launcher Open") + "?", HELP_ICON,
                GetText.tr("This determines if ATLauncher should stay open or exit after Minecraft has exited"));
        add(keepLauncherOpenLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        keepLauncherOpen = new JCheckBox();
        if (App.settings.keepLauncherOpen) {
            keepLauncherOpen.setSelected(true);
        }
        add(keepLauncherOpen, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableConsoleLabel = new JLabelWithHover(GetText.tr("Enable Console") + "?", HELP_ICON,
                GetText.tr("If you want the console to be visible when opening the Launcher."));
        add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        if (App.settings.enableConsole) {
            enableConsole.setSelected(true);
        }
        add(enableConsole, gbc);

        // Enable Tray Icon

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableTrayIconLabel = new JLabelWithHover(GetText.tr("Enable Tray Menu") + "?", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "The Tray Menu is a little icon that shows in your system taskbar which allows you to perform different functions to do various things with the launcher such as hiding or showing the console, killing Minecraft or closing ATLauncher."))
                        .build());
        add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (App.settings.enableTrayMenu) {
            enableTrayIcon.setSelected(true);
        }
        add(enableTrayIcon, gbc);

        // Enable Discord Integration

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableDiscordIntegrationLabel = new JLabelWithHover(
                GetText.tr("Enable Discord Integration") + "?", HELP_ICON,
                GetText.tr("This will enable showing which pack you're playing in Discord."));
        add(enableDiscordIntegrationLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableDiscordIntegration = new JCheckBox();
        if (App.settings.enableDiscordIntegration) {
            enableDiscordIntegration.setSelected(true);
        }
        add(enableDiscordIntegration, gbc);

        // Enable Feral Gamemode

        if (OS.isLinux()) {
            boolean gameModeExistsInPath = Utils.executableInPath("gamemoderun");

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover enableFeralGamemodeLabel = new JLabelWithHover(GetText.tr("Enable Feral Gamemode") + "?",
                    HELP_ICON, GetText.tr("This will enable Feral Gamemode for packs launched."));
            add(enableFeralGamemodeLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            enableFeralGamemode = new JCheckBox();
            if (App.settings.enableFeralGamemode) {
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

        // Disable custom fonts

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover disableCustomFontsLabel = new JLabelWithHover(GetText.tr("Disable Custom Fonts?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This will disable custom fonts used by themes. If your system has issues with font display not looking right, you can disable this to switch to a default compatible font."))
                        .build());
        add(disableCustomFontsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        disableCustomFonts = new JCheckBox();
        disableCustomFonts.setSelected(App.settings.disableCustomFonts);
        add(disableCustomFonts, gbc);

        // Remember gui sizes and positions

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover rememberWindowSizePositionLabel = new JLabelWithHover(
                GetText.tr("Remember Window Size & Positions?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This will remember the windows positions and size so they keep the same size and position when you restart the launcher."))
                        .build());
        add(rememberWindowSizePositionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberWindowSizePosition = new JCheckBox();
        rememberWindowSizePosition.setSelected(App.settings.rememberWindowSizePosition);
        add(rememberWindowSizePosition, gbc);

        // Use native file picker

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useNativeFilePickerLabel = new JLabelWithHover(GetText.tr("Use Native File Picker?"), HELP_ICON,
                new HTMLBuilder().center().split(100)
                        .text(GetText
                                .tr("This will use your operating systems native file picker when selecting files."))
                        .build());
        add(useNativeFilePickerLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useNativeFilePicker = new JCheckBox();
        useNativeFilePicker.setSelected(App.settings.useNativeFilePicker);
        add(useNativeFilePicker, gbc);

        // Use recycle bin

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useRecycleBinLabel = new JLabelWithHover(GetText.tr("Use Recycle Bin/Trash?"), HELP_ICON,
                new HTMLBuilder().center().split(100)
                        .text(GetText
                                .tr("This will use your operating systems recycle bin/trash where possible when deleting files/instances/servers instead of just deleting them entirely, allowing you to recover files if you make a mistake or want to get them back."))
                        .build());
        add(useRecycleBinLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useRecycleBin = new JCheckBox();
        useRecycleBin.setSelected(App.settings.useRecycleBin);
        add(useRecycleBin, gbc);
    }

    @SuppressWarnings("unchecked")
    public boolean needToReloadTheme() {
        return !((ComboItem<String>) theme.getSelectedItem()).getValue().equalsIgnoreCase(App.settings.theme)
                || App.settings.disableCustomFonts != disableCustomFonts.isSelected()
                || !((String) language.getSelectedItem()).equalsIgnoreCase(App.settings.language);
    }

    @SuppressWarnings("unchecked")
    public boolean themeChanged() {
        return !((ComboItem<String>) theme.getSelectedItem()).getValue().equalsIgnoreCase(App.settings.theme);
    }

    public boolean needToReloadInstancesPanel() {
        return !(((ComboItem<String>) instanceTitleFormat.getSelectedItem()).getValue())
                .equals(App.settings.instanceTitleFormat);
    }

    public boolean needToReloadLanguage() {
        return !((String) language.getSelectedItem()).equalsIgnoreCase(Language.selected);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        Language.setLanguage((String) language.getSelectedItem());
        App.settings.language = (String) language.getSelectedItem();
        App.settings.theme = ((ComboItem<String>) theme.getSelectedItem()).getValue();
        App.settings.dateFormat = ((ComboItem<String>) dateFormat.getSelectedItem()).getValue();
        App.settings.instanceTitleFormat = ((ComboItem<String>) instanceTitleFormat.getSelectedItem()).getValue();
        App.settings.selectedTabOnStartup = ((ComboItem<Integer>) selectedTabOnStartup.getSelectedItem()).getValue();
        App.settings.defaultInstanceSorting = (InstanceSortingStrategies) defaultInstanceSorting.getSelectedItem();
        App.settings.keepLauncherOpen = keepLauncherOpen.isSelected();
        App.settings.enableConsole = enableConsole.isSelected();
        App.settings.enableTrayMenu = enableTrayIcon.isSelected();
        App.settings.enableDiscordIntegration = enableDiscordIntegration.isSelected();

        if (customDownloadsPath.getText().equalsIgnoreCase(FileSystem.getUserDownloadsPath(false).toString())) {
            App.settings.customDownloadsPath = null;
        } else {
            App.settings.customDownloadsPath = customDownloadsPath.getText();
        }

        if (OS.isLinux()) {
            App.settings.enableFeralGamemode = enableFeralGamemode.isSelected();
        } else {
            App.settings.enableFeralGamemode = false;
        }

        App.settings.disableCustomFonts = disableCustomFonts.isSelected();
        App.settings.rememberWindowSizePosition = rememberWindowSizePosition.isSelected();

        if (!rememberWindowSizePosition.isSelected()) {
            App.settings.consoleSize = new Dimension(650, 400);
            App.settings.consolePosition = new Point(0, 0);

            App.settings.launcherSize = new Dimension(1200, 700);
            App.settings.launcherPosition = null;
        }

        App.settings.useNativeFilePicker = useNativeFilePicker.isSelected();
        App.settings.useRecycleBin = useRecycleBin.isSelected();
    }

    @Override
    public String getTitle() {
        return GetText.tr("General");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "General";
    }
}
