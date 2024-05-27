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
package com.atlauncher.viewmodel.impl.settings;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.data.LauncherTheme;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.viewmodel.base.settings.IGeneralSettingsViewModel;
import com.formdev.flatlaf.FlatLaf;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 15
 */
public class GeneralSettingsViewModel implements IGeneralSettingsViewModel {
    private final BehaviorSubject<Integer> _addOnSelectedLanguageChanged = BehaviorSubject.create(),
        _addOnSelectedThemeChanged = BehaviorSubject.create(),
        _addOnDateFormatChanged = BehaviorSubject.create(),
        _addOnInstanceFormatChanged = BehaviorSubject.create(),
        _addOnSelectedTabOnStartupChanged = BehaviorSubject.create(),
        _addInstanceSortingChanged = BehaviorSubject.create();

    private final BehaviorSubject<String> _addOnCustomsDownloadPathChanged =
        BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _addOnKeepLauncherOpenChanged = BehaviorSubject.create(),
        _addOnEnableConsoleChanged = BehaviorSubject.create(),
        _addOnEnableTrayMenuChanged = BehaviorSubject.create(),
        _addOnEnableDiscordIntegrationChanged = BehaviorSubject.create(),
        _addOnEnableFeralGameModeChanged = BehaviorSubject.create(),
        _addOnDisableCustomFontsChanged = BehaviorSubject.create(),
        _addOnRememberWindowStuffChanged = BehaviorSubject.create(),
        _addOnUseNativeFilePickerChanged = BehaviorSubject.create(),
        _addOnUseRecycleBinChanged = BehaviorSubject.create(),
        enableArmSupport = BehaviorSubject.create(),
        scanModsOnLaunch = BehaviorSubject.create();

    private List<LauncherTheme> themes = null;

    public GeneralSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        pushSelectedLanguage();
        pushSelectedTheme();
        pushDateFormat();
        pushInstanceFormat();
        _addOnSelectedTabOnStartupChanged.onNext(App.settings.selectedTabOnStartup);
        pushInstanceSorting();

        _addOnCustomsDownloadPathChanged.onNext(Optional.ofNullable(App.settings.customDownloadsPath)
            .orElse(FileSystem.getUserDownloadsPath(false).toString()));

        _addOnKeepLauncherOpenChanged.onNext(App.settings.keepLauncherOpen);
        _addOnEnableConsoleChanged.onNext(App.settings.enableConsole);
        _addOnEnableTrayMenuChanged.onNext(App.settings.enableTrayMenu);
        _addOnEnableDiscordIntegrationChanged.onNext(App.settings.enableDiscordIntegration);
        _addOnEnableFeralGameModeChanged.onNext(App.settings.enableFeralGamemode);
        _addOnDisableCustomFontsChanged.onNext(App.settings.disableCustomFonts);
        _addOnRememberWindowStuffChanged.onNext(App.settings.rememberWindowSizePosition);
        _addOnUseNativeFilePickerChanged.onNext(App.settings.useNativeFilePicker);
        _addOnUseRecycleBinChanged.onNext(App.settings.useRecycleBin);
    }

    @Override
    public String[] getLanguages() {
        return Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new);
    }

    @Override
    public void setSelectedLanguage(String language) {
        Language.setLanguage(language);
        App.settings.language = language;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> SelectedLanguageChanged() {
        return _addOnSelectedLanguageChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushSelectedLanguage() {
        for (int index = 0; index < getLanguages().length; index++) {
            if (getLanguages()[index].equals(App.settings.language))
                _addOnSelectedLanguageChanged.onNext(index);
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
        Analytics.trackEvent(AnalyticsEvent.forThemeChange(App.THEME.getName()));
        App.settings.theme = theme;
        SettingsManager.post();
        App.loadTheme(App.settings.theme);
        FlatLaf.updateUILater();
        ThemeManager.post();
    }

    @Override
    public Observable<Integer> SelectedThemeChanged() {
        return _addOnSelectedThemeChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushSelectedTheme() {
        for (int index = 0; index < getThemes().size(); index++) {
            if (getThemes().get(index).id.equals(App.settings.theme))
                _addOnSelectedThemeChanged.onNext(index);
        }
    }

    @Override
    public Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        return cal.getTime();
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
    public Observable<Integer> DateFormatChanged() {
        return _addOnDateFormatChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushDateFormat() {
        for (int index = 0; index < getDateFormats().length; index++) {
            if (getDateFormats()[index].equals(App.settings.dateFormat))
                _addOnDateFormatChanged.onNext(index);
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
    }

    @Override
    public Observable<Integer> InstanceFormatChanged() {
        return _addOnInstanceFormatChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushInstanceFormat() {
        for (int index = 0; index < getInstanceTitleFormats().length; index++) {
            if (getInstanceTitleFormats()[index].equals(App.settings.instanceTitleFormat))
                _addOnInstanceFormatChanged.onNext(index);
        }
    }

    @Override
    public void setSelectedTabOnStartup(int tab) {
        App.settings.selectedTabOnStartup = tab;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> SelectedTabOnStartupChanged() {
        return _addOnSelectedTabOnStartupChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public InstanceSortingStrategies[] getInstanceSorting() {
        return InstanceSortingStrategies.values();
    }

    @Override
    public void setInstanceSorting(InstanceSortingStrategies sorting) {
        App.settings.defaultInstanceSorting = sorting;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> addInstanceSortingChanged() {
        return _addInstanceSortingChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushInstanceSorting() {
        for (int index = 0; index < getInstanceSorting().length; index++) {
            if (getInstanceSorting()[index] == App.settings.defaultInstanceSorting)
                _addInstanceSortingChanged.onNext(index);
        }
    }

    @Override
    public void resetCustomDownloadPath() {
        App.settings.customDownloadsPath = null;
        SettingsManager.post();
    }

    @Override
    public void setCustomsDownloadPath(String value) {
        App.settings.customDownloadsPath = value;
        SettingsValidityManager.setValidity("customDownloadsPath", true);
        SettingsManager.post();
    }

    @Override
    public void setCustomsDownloadPathPending() {
        SettingsValidityManager.setValidity("customDownloadsPath", false);
    }

    @Override
    public Observable<String> CustomsDownloadPathChanged() {
        return _addOnCustomsDownloadPathChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setKeepLauncherOpen(boolean b) {
        App.settings.keepLauncherOpen = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> KeepLauncherOpenChanged() {
        return _addOnKeepLauncherOpenChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableConsole(boolean b) {
        App.settings.enableConsole = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> EnableConsoleChanged() {
        return _addOnEnableConsoleChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableTrayMenuOpen(boolean b) {
        App.settings.enableTrayMenu = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> EnableTrayMenuChanged() {
        return _addOnEnableTrayMenuChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableDiscordIntegration(boolean b) {
        App.settings.enableDiscordIntegration = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> EnableDiscordIntegrationChanged() {
        return _addOnEnableDiscordIntegrationChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public boolean showFeralGameMode() {
        return OS.isLinux();
    }

    @Override
    public boolean hasFeralGameMode() {
        return Utils.executableInPath("gamemoderun");
    }

    @Override
    public void setEnableFeralGameMode(boolean b) {
        App.settings.enableFeralGamemode = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> EnableFeralGameModeChanged() {
        return _addOnEnableFeralGameModeChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setDisableCustomFonts(boolean b) {
        App.settings.disableCustomFonts = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> DisableCustomFontsChanged() {
        return _addOnDisableCustomFontsChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setRememberWindowStuff(boolean remember) {
        App.settings.rememberWindowSizePosition = remember;
        if (!remember) {
            App.settings.consoleSize = new Dimension(650, 400);
            App.settings.consolePosition = new Point(0, 0);

            App.settings.launcherSize = new Dimension(1200, 700);
            App.settings.launcherPosition = null;
        }
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> RememberWindowStuffChanged() {
        return _addOnRememberWindowStuffChanged.observeOn(SwingSchedulers.edt());
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
    public Observable<Boolean> UseNativeFilePickerChanged() {
        return _addOnUseNativeFilePickerChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setUseRecycleBin(boolean b) {
        App.settings.useRecycleBin = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> UseRecycleBinChanged() {
        return _addOnUseRecycleBinChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public boolean showArmSupport() {
        return ConfigManager.getConfigItem("useLwjglReplacement", false);
    }

    @Override
    public Observable<Boolean> getEnableArmSupport() {
        return enableArmSupport.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableArmSupport(boolean b) {
        App.settings.enableArmSupport = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getScanModsOnLaunch() {
        return scanModsOnLaunch.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setScanModsOnLaunch(boolean b) {
        App.settings.scanModsOnLaunch = b;
        SettingsManager.post();
    }
}