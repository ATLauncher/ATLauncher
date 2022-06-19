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

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.evnt.manager.SettingsValidityManager;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.formdev.flatlaf.FlatLaf;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public class GeneralSettingsViewModel implements IGeneralSettingsViewModel {
    private List<LauncherTheme> themes = null;

    Consumer<Integer> _addOnSelectedLanguageChanged;
    Consumer<Integer> _addOnSelectedThemeChanged;
    Consumer<Integer> _addOnDateFormatChanged;
    Consumer<Integer> _addOnInstanceFormatChanged;
    Consumer<Integer> _addOnSelectedTabOnStartupChanged;
    Consumer<Integer> _addInstanceSortingChanged;
    Consumer<String> _addOnCustomsDownloadPathChanged;
    Consumer<Boolean> _addOnKeepLauncherOpenChanged;
    Consumer<Boolean> _addOnEnableConsoleChanged;
    Consumer<Boolean> _addOnEnableTrayMenuChanged;
    Consumer<Boolean> _addOnEnableDiscordIntegrationChanged;
    Consumer<Boolean> _addOnEnableFeralGameModeChanged = b -> {
    };
    Consumer<Boolean> _addOnDisableCustomFontsChanged;
    Consumer<Boolean> _addOnRememberWindowStuffChanged;
    Consumer<Boolean> _addOnUseNativeFilePickerChanged = b -> {
    };
    Consumer<Boolean> _addOnUseRecycleBinChanged;

    public GeneralSettingsViewModel() {
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        pushSelectedLanguage();
        pushSelectedTheme();
        pushDateFormat();
        pushInstanceFormat();
        _addOnSelectedTabOnStartupChanged.accept(App.settings.selectedTabOnStartup);
        pushInstanceSorting();
        _addOnCustomsDownloadPathChanged.accept(App.settings.customDownloadsPath);
        _addOnKeepLauncherOpenChanged.accept(App.settings.keepLauncherOpen);
        _addOnEnableConsoleChanged.accept(App.settings.enableConsole);
        _addOnEnableTrayMenuChanged.accept(App.settings.enableTrayMenu);
        _addOnEnableDiscordIntegrationChanged.accept(App.settings.enableDiscordIntegration);
        _addOnEnableFeralGameModeChanged.accept(App.settings.enableFeralGamemode);
        _addOnDisableCustomFontsChanged.accept(App.settings.disableCustomFonts);
        _addOnRememberWindowStuffChanged.accept(App.settings.rememberWindowSizePosition);
        _addOnUseNativeFilePickerChanged.accept(App.settings.useNativeFilePicker);
        _addOnUseRecycleBinChanged.accept(App.settings.useRecycleBin);
    }

    @Override
    public String[] getLanguages() {
        return Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new);
    }

    @Override
    public void setSelectedLanguage(String language) {
        App.settings.language = language;
        SettingsManager.post();
    }

    @Override
    public void addOnSelectedLanguageChanged(Consumer<Integer> changed) {
        _addOnSelectedLanguageChanged = changed;
        pushSelectedLanguage();
    }

    private void pushSelectedLanguage() {
        for (int index = 0; index < getLanguages().length; index++) {
            if (getLanguages()[index].equals(App.settings.language))
                _addOnSelectedLanguageChanged.accept(index);
        }
    }

    @Override
    public List<LauncherTheme> getThemes() {
        if (themes == null)
            themes = Arrays.asList(
                new LauncherTheme("com.atlauncher.themes.Dark", "ATLauncher Dark (default)"),
                new LauncherTheme("com.atlauncher.themes.Light", "ATLauncher Light"),
                new LauncherTheme("com.atlauncher.themes.MonokaiPro", "Monokai Pro"),
                new LauncherTheme("com.atlauncher.themes.DraculaContrast", "Dracula Contrast"),
                new LauncherTheme("com.atlauncher.themes.HiberbeeDark", "Hiberbee Dark"),
                new LauncherTheme("com.atlauncher.themes.Vuesion", "Vuesion"),
                new LauncherTheme("com.atlauncher.themes.MaterialPalenightContrast", "Material Palenight Contrast"),
                new LauncherTheme("com.atlauncher.themes.ArcOrange", "Arc Orange"),
                new LauncherTheme("com.atlauncher.themes.CyanLight", "Cyan Light"),
                new LauncherTheme("com.atlauncher.themes.HighTechDarkness", "High Tech Darkness"),
                new LauncherTheme("com.atlauncher.themes.OneDark", "One Dark")
            );
        return themes;
    }

    @Override
    public void setSelectedTheme(String theme) {
        Analytics.sendEvent(App.THEME.getName(), "ChangeTheme", "Launcher");
        App.settings.theme = theme;
        SettingsManager.post();
        App.loadTheme(App.settings.theme);
        FlatLaf.updateUILater();
        ThemeManager.post();
    }

    @Override
    public void addOnSelectedThemeChanged(Consumer<Integer> themeChanged) {
        _addOnSelectedThemeChanged = themeChanged;
        pushSelectedTheme();
    }

    private void pushSelectedTheme() {
        for (int index = 0; index < getLanguages().length; index++) {
            if (getThemes().get(index).id.equals(App.settings.theme))
                _addOnSelectedThemeChanged.accept(index);
        }
    }

    @Override
    public String[] getDateFormats() {
        return Constants.DATE_FORMATS;
    }

    @Override
    public void setDateFormat(String format) {
        App.settings.dateFormat = format;
        SettingsManager.post();
    }

    @Override
    public void addOnDateFormatChanged(Consumer<Integer> onDateFormatChanged) {
        _addOnDateFormatChanged = onDateFormatChanged;
        pushDateFormat();
    }

    private void pushDateFormat() {
        for (int index = 0; index < getDateFormats().length; index++) {
            if (getDateFormats()[index].equals(App.settings.dateFormat))
                _addOnDateFormatChanged.accept(index);
        }
    }

    @Override
    public String[] getInstanceTitleFormats() {
        return Constants.INSTANCE_TITLE_FORMATS;
    }

    @Override
    public void setInstanceTitleFormat(String format) {
        App.settings.instanceTitleFormat = format;
        SettingsManager.post();
        App.launcher.reloadInstancesPanel();
    }

    @Override
    public void addOnInstanceFormatChanged(Consumer<Integer> instance) {
        _addOnInstanceFormatChanged = instance;
        pushInstanceFormat();
    }

    private void pushInstanceFormat() {
        for (int index = 0; index < getInstanceTitleFormats().length; index++) {
            if (getInstanceTitleFormats()[index].equals(App.settings.instanceTitleFormat))
                _addOnInstanceFormatChanged.accept(index);
        }
    }

    @Override
    public void setSelectedTabOnStartup(int tab) {
        App.settings.selectedTabOnStartup = tab;
        SettingsManager.post();
    }

    @Override
    public void addOnSelectedTabOnStartupChanged(Consumer<Integer> onChanged) {
        _addOnSelectedTabOnStartupChanged = onChanged;
        _addOnSelectedTabOnStartupChanged.accept(App.settings.selectedTabOnStartup);
    }

    @Override
    public InstanceSortingStrategies[] getInstanceSorting() {
        return InstanceSortingStrategies.values();
    }

    @Override
    public void setInstanceSorting(InstanceSortingStrategies sorting) {
        App.settings.defaultInstanceSorting = sorting;
        SettingsManager.post();
        App.launcher.reloadInstancesPanel();
    }

    @Override
    public void addInstanceSortingChanged(Consumer<Integer> onChanged) {
        _addInstanceSortingChanged = onChanged;
        pushInstanceSorting();
    }

    private void pushInstanceSorting() {
        for (int index = 0; index < getInstanceSorting().length; index++) {
            if (getInstanceSorting()[index] == App.settings.defaultInstanceSorting)
                _addInstanceSortingChanged.accept(index);
        }
    }

    @Override
    public void resetCustomDownloadPath() {
        App.settings.customDownloadsPath = FileSystem.getUserDownloadsPath(false).toString();
        SettingsManager.post();
    }

    @Override
    public void setCustomsDownloadPath(String value) {
        App.settings.customDownloadsPath = value;
        SettingsValidityManager.post("customDownloadsPath", true);
        SettingsManager.post();
    }

    @Override
    public void setCustomsDownloadPathPending() {
        SettingsValidityManager.post("customDownloadsPath", false);
    }

    @Override
    public void addOnCustomsDownloadPathChanged(Consumer<String> onChanged) {
        onChanged.accept(
            Optional.ofNullable(App.settings.customDownloadsPath)
                .orElse(FileSystem.getUserDownloadsPath(false).toString())
        );
        _addOnCustomsDownloadPathChanged = onChanged;
    }

    @Override
    public void setKeepLauncherOpen(boolean b) {
        App.settings.keepLauncherOpen = b;
        SettingsManager.post();
    }

    @Override
    public void addOnKeepLauncherOpenChanged(Consumer<Boolean> onChanged) {
        _addOnKeepLauncherOpenChanged = onChanged;
        onChanged.accept(App.settings.keepLauncherOpen);
    }

    @Override
    public void setEnableConsole(boolean b) {
        App.settings.enableConsole = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableConsoleChanged(Consumer<Boolean> onChanged) {
        _addOnEnableConsoleChanged = onChanged;
        onChanged.accept(App.settings.enableConsole);
    }

    @Override
    public void setEnableTrayMenuOpen(boolean b) {
        App.settings.enableTrayMenu = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableTrayMenuChanged(Consumer<Boolean> onChanged) {
        _addOnEnableTrayMenuChanged = onChanged;
        onChanged.accept(App.settings.enableTrayMenu);
    }

    @Override
    public void setEnableDiscordIntegration(boolean b) {
        App.settings.enableDiscordIntegration = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableDiscordIntegrationChanged(Consumer<Boolean> onChanged) {
        _addOnEnableDiscordIntegrationChanged = onChanged;
        onChanged.accept(App.settings.enableDiscordIntegration);
    }

    @Override
    public boolean showFeralGameMode() {
        // TODO Check for gamemoderun command
        return OS.isLinux();
    }

    @Override
    public void setEnableFeralGameMode(boolean b) {
        App.settings.enableFeralGamemode = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableFeralGameModeChanged(Consumer<Boolean> onChanged) {
        _addOnEnableFeralGameModeChanged = onChanged;
        onChanged.accept(App.settings.enableFeralGamemode);
    }

    @Override
    public void setDisableCustomFonts(boolean b) {
        App.settings.disableCustomFonts = b;
        SettingsManager.post();
    }

    @Override
    public void addOnDisableCustomFontsChanged(Consumer<Boolean> onChanged) {
        _addOnDisableCustomFontsChanged = onChanged;
        onChanged.accept(App.settings.disableCustomFonts);
    }

    @Override
    public void setRememberWindowStuff(boolean b) {
        App.settings.rememberWindowSizePosition = b;
        SettingsManager.post();
    }

    @Override
    public void addOnRememberWindowStuffChanged(Consumer<Boolean> onChanged) {
        _addOnRememberWindowStuffChanged = onChanged;
        onChanged.accept(App.settings.rememberWindowSizePosition);
    }

    @Override
    public boolean getShowNativeFilePickerOption() {
        return !OS.isUsingFlatpak();
    }

    @Override
    public void setUseNativeFilePicker(boolean b) {
        App.settings.useNativeFilePicker = b;
        SettingsManager.post();
    }

    @Override
    public void addOnUseNativeFilePickerChanged(Consumer<Boolean> onChanged) {
        _addOnUseNativeFilePickerChanged = onChanged;
        onChanged.accept(App.settings.useNativeFilePicker);
    }

    @Override
    public void setUseRecycleBin(boolean b) {
        App.settings.useRecycleBin = b;
        SettingsManager.post();
    }

    @Override
    public void addOnUseRecycleBinChanged(Consumer<Boolean> onChanged) {
        _addOnUseRecycleBinChanged = onChanged;
        onChanged.accept(App.settings.useRecycleBin);
    }


}
