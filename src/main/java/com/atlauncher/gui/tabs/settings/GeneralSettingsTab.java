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

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.CheckState;
import com.atlauncher.data.LauncherTheme;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.viewmodel.impl.settings.GeneralSettingsViewModel;

public class GeneralSettingsTab extends AbstractSettingsTab {
    private final GeneralSettingsViewModel viewModel;
    private JLabelWithHover customDownloadsPathChecker;

    public GeneralSettingsTab(GeneralSettingsViewModel generalSettingsViewModel) {
        this.viewModel = generalSettingsViewModel;
    }

    @Override
    protected void onShow() {
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

        JComboBox<String> language = new JComboBox<>(viewModel.getLanguages());
        language.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setSelectedLanguage((String) itemEvent.getItem());
        });
        addDisposable(viewModel.getSelectedLanguage().subscribe(language::setSelectedItem));
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
        JComboBox<ComboItem<String>> theme = new JComboBox<>();

        for (LauncherTheme launcherTheme : viewModel.getThemes()) {
            theme.addItem(new ComboItem<>(launcherTheme.id, launcherTheme.label));
        }
        addDisposable(viewModel.getSelectedTheme().subscribe(theme::setSelectedIndex));

        theme.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                viewModel.setSelectedTheme(((ComboItem<String>) itemEvent.getItem()).getValue());
            }
        });

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
        JComboBox<ComboItem<String>> dateFormat = new JComboBox<>();

        Date exampleDate = viewModel.getDate();

        for (String format : viewModel.getDateFormats()) {
            dateFormat.addItem(new ComboItem<>(format, new SimpleDateFormat(format).format(exampleDate)));
        }

        addDisposable(viewModel.getDateFormat().subscribe(dateFormat::setSelectedIndex));

        dateFormat.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setDateFormat(((ComboItem<String>) itemEvent.getItem()).getValue());
        });

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
        JComboBox<ComboItem<String>> instanceTitleFormat = new JComboBox<>();

        for (String format : viewModel.getInstanceTitleFormats()) {
            instanceTitleFormat.addItem(new ComboItem<>(format, String.format(format, GetText.tr("Instance Name"),
                    GetText.tr("Pack Name"), GetText.tr("Pack Version"), GetText.tr("Minecraft Version"))));
        }

        addDisposable(viewModel.getInstanceFormat().subscribe(instanceTitleFormat::setSelectedIndex));

        instanceTitleFormat.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setInstanceTitleFormat(((ComboItem<String>) itemEvent.getItem()).getValue());
        });

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
        JComboBox<ComboItem<Integer>> selectedTabOnStartup = new JComboBox<>();
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_NEWS_TAB, GetText.tr("News")));
        selectedTabOnStartup
                .addItem(new ComboItem<>(UIConstants.LAUNCHER_CREATE_PACK_TAB, GetText.tr("Create Pack")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_PACKS_TAB, GetText.tr("Packs")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_INSTANCES_TAB, GetText.tr("Instances")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_SERVERS_TAB, GetText.tr("Servers")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_ACCOUNTS_TAB, GetText.tr("Accounts")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_TOOLS_TAB, GetText.tr("Tools")));
        selectedTabOnStartup.addItem(new ComboItem<>(UIConstants.LAUNCHER_SETTINGS_TAB, GetText.tr("Settings")));

        addDisposable(viewModel.getSelectedTabOnStartup().subscribe(selectedTabOnStartup::setSelectedIndex));

        selectedTabOnStartup.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setSelectedTabOnStartup(((ComboItem<Integer>) itemEvent.getItem()).getValue());
        });

        add(selectedTabOnStartup, gbc);

        // Default instance sorting

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover defaultInstanceSortingLabel = new JLabelWithHover(GetText.tr("Default Instance Sort") + ":",
                HELP_ICON,
                GetText.tr("Default sorting of instances under the Instances tab."));

        add(defaultInstanceSortingLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JComboBox<InstanceSortingStrategies> defaultInstanceSorting = new JComboBox<>(
                InstanceSortingStrategies.values());
        addDisposable(viewModel.getInstanceSortingObservable().subscribe(defaultInstanceSorting::setSelectedIndex));
        defaultInstanceSorting.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setInstanceSorting((InstanceSortingStrategies) itemEvent.getItem());
        });

        add(defaultInstanceSorting, gbc);

        // Custom Downloads Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover customDownloadsPathLabel = new JLabelWithHover(GetText.tr("Downloads Folder") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to change the Downloads folder that the launcher looks in when downloading browser mods."))
                        .build());
        add(customDownloadsPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel customDownloadsPathPanel = new JPanel();
        customDownloadsPathPanel.setLayout(new BoxLayout(customDownloadsPathPanel, BoxLayout.X_AXIS));

        JTextField customDownloadsPath = new JTextField(16);
        customDownloadsPathChecker = new JLabelWithHover("", null, null);
        addDisposable(viewModel.getCustomsDownloadPath().subscribe(customDownloadsPath::setText));
        customDownloadsPath.addKeyListener(
                new DelayedSavingKeyListener(
                        500,
                        () -> viewModel.setCustomsDownloadPath(customDownloadsPath.getText()),
                        viewModel::setCustomsDownloadPathPending));
        addDisposable(viewModel.getCustomDownloadsPathChecker().subscribe(this::setCustomDownloadsPathCheckState));

        JButton customDownloadsPathResetButton = new JButton(GetText.tr("Reset"));

        customDownloadsPathResetButton.addActionListener(e -> {
            viewModel.resetCustomDownloadPath();
            resetCustomDownloadsPathCheckLabel();
        });

        JButton customDownloadsPathBrowseButton = new JButton(GetText.tr("Browse"));
        customDownloadsPathBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(customDownloadsPath.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedPath = chooser.getSelectedFile();
                customDownloadsPath.setText(selectedPath.getAbsolutePath());
                viewModel.setCustomsDownloadPath(selectedPath.getAbsolutePath());
                viewModel.setCustomsDownloadPathPending();
            }
        });

        customDownloadsPathPanel.add(customDownloadsPath);
        customDownloadsPathPanel.add(Box.createHorizontalStrut(5));
        customDownloadsPathPanel.add(customDownloadsPathChecker);
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
        JCheckBox keepLauncherOpen = new JCheckBox();
        keepLauncherOpen.addItemListener(e -> viewModel.setKeepLauncherOpen(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getKeepLauncherOpen().subscribe(keepLauncherOpen::setSelected));
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
        JCheckBox enableConsole = new JCheckBox();
        enableConsole.addItemListener(e -> viewModel.setEnableConsole(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getEnableConsole().subscribe(enableConsole::setSelected));
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
        JCheckBox enableTrayIcon = new JCheckBox();
        enableTrayIcon.addItemListener(e -> viewModel.setEnableTrayMenuOpen(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getEnableTrayMenu().subscribe(enableTrayIcon::setSelected));
        add(enableTrayIcon, gbc);

        // Enable Feral Gamemode

        if (viewModel.showFeralGameMode()) {
            boolean gameModeExistsInPath = viewModel.hasFeralGameMode();

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
            JCheckBox enableFeralGamemode = new JCheckBox();
            enableFeralGamemode
                    .addItemListener(e -> viewModel.setEnableFeralGameMode(e.getStateChange() == ItemEvent.SELECTED));
            addDisposable(viewModel.getEnableFeralGameMode().subscribe(enableFeralGamemode::setSelected));

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
        JCheckBox disableCustomFonts = new JCheckBox();
        disableCustomFonts.addItemListener(itemEvent -> {
            viewModel.setDisableCustomFonts(itemEvent.getStateChange() == ItemEvent.SELECTED);
        });
        addDisposable(viewModel.getDisableCustomFonts().subscribe(disableCustomFonts::setSelected));
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
        JCheckBox rememberWindowSizePosition = new JCheckBox();
        rememberWindowSizePosition
                .addItemListener(e -> viewModel.setRememberWindowStuff(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getRememberWindowSizePosition().subscribe(rememberWindowSizePosition::setSelected));
        add(rememberWindowSizePosition, gbc);

        // Use native file picker

        if (viewModel.getShowNativeFilePickerOption()) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover useNativeFilePickerLabel = new JLabelWithHover(GetText.tr("Use Native File Picker?"),
                    HELP_ICON,
                    new HTMLBuilder().center().split(100)
                            .text(GetText
                                    .tr("This will use your operating systems native file picker when selecting files."))
                            .build());
            add(useNativeFilePickerLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            JCheckBox useNativeFilePicker = new JCheckBox();
            useNativeFilePicker
                    .addItemListener(e -> viewModel.setUseNativeFilePicker(e.getStateChange() == ItemEvent.SELECTED));
            addDisposable(viewModel.getUseNativeFilePicker().subscribe(useNativeFilePicker::setSelected));
            add(useNativeFilePicker, gbc);
        }

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
        JCheckBox useRecycleBin = new JCheckBox();
        useRecycleBin.addItemListener(e -> viewModel.setUseRecycleBin(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getUseRecycleBin().subscribe(useRecycleBin::setSelected));
        add(useRecycleBin, gbc);

        if (viewModel.showArmSupport()) {
            // Enable ARM Support

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover enableArmSupportLabel = new JLabelWithHover(GetText.tr("Enable ARM Support?"), HELP_ICON,
                    new HTMLBuilder().center().split(100)
                            .text(GetText
                                    .tr("Support for ARM devices is still experimental. If you experience issues on an ARM based device, please turn this off."))
                            .build());
            add(enableArmSupportLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            JCheckBox enableArmSupport = new JCheckBox();
            useRecycleBin.addItemListener(e -> viewModel.setEnableArmSupport(e.getStateChange() == ItemEvent.SELECTED));
            addDisposable(viewModel.getEnableArmSupport().subscribe(useRecycleBin::setSelected));
            add(enableArmSupport, gbc);
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("General");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "General";
    }

    private void showCustomDownloadsPathWarning() {
        DialogManager.okDialog()
                .setTitle(GetText.tr("Help"))
                .setContent(
                        new HTMLBuilder()
                                .center()
                                .text(
                                        GetText.tr(
                                                "The Downloads Folder Path you set is incorrect.<br/><br/>Please verify it points to a folder and try again."))
                                .build())
                .setType(DialogManager.ERROR)
                .show();
    }

    private void setLabelState(String tooltip, String path) {
        try {
            customDownloadsPathChecker.setToolTipText(tooltip);
            ImageIcon icon = Utils.getIconImage(path);
            if (icon != null) {
                customDownloadsPathChecker.setIcon(icon);
                icon.setImageObserver(customDownloadsPathChecker);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void resetCustomDownloadsPathCheckLabel() {
        customDownloadsPathChecker.setText("");
        customDownloadsPathChecker.setIcon(null);
        customDownloadsPathChecker.setToolTipText(null);
    }

    private void setCustomDownloadsPathCheckState(CheckState state) {
        if (state == CheckState.NotChecking) {
            resetCustomDownloadsPathCheckLabel();
        } else if (state == CheckState.CheckPending) {
            setLabelState(GetText.tr("Downloads folder path change pending"),
                    "/assets/icon/warning.png");
        } else if (state == CheckState.Checking) {
            setLabelState(GetText.tr("Checking downloads folder path"),
                    "/assets/image/loading-bars-small.gif");
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetCustomDownloadsPathCheckLabel();
            } else {
                setLabelState(GetText.tr("Invalid!"), "/assets/icon/error.png");
                showCustomDownloadsPathWarning();
            }
        }
    }

    @Override
    protected void onDestroy() {
        removeAll();
        customDownloadsPathChecker = null;
    }

    @Override
    protected void createViewModel() {
    }
}
