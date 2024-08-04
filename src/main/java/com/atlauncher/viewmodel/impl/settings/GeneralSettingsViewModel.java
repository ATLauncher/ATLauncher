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
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.formdev.flatlaf.FlatLaf;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 15
 * <p>
 * View model for {@link GeneralSettingsTab}
 */
public class GeneralSettingsViewModel implements SettingsListener {
    private final BehaviorSubject<Integer>
        _addOnSelectedLanguage = BehaviorSubject.create(),
        _selectedTheme = BehaviorSubject.create(),
        _dateFormat = BehaviorSubject.create(),
        _addOnInstanceFormat = BehaviorSubject.create(),
        _addOnSelectedTabOnStartup = BehaviorSubject.create(),
        _addInstanceSorting = BehaviorSubject.create();

    private final BehaviorSubject<String>
        _addOnCustomsDownloadPath = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _keepLauncherOpen = BehaviorSubject.create(),
        _enableConsole = BehaviorSubject.create(),
        _enableTrayMenu = BehaviorSubject.create(),
        _enableDiscordIntegration = BehaviorSubject.create(),
        _enableFeralGameMode = BehaviorSubject.create(),
        _disableCustomFonts = BehaviorSubject.create(),
        _rememberWindowSizePosition = BehaviorSubject.create(),
        _useNativeFilePicker = BehaviorSubject.create(),
        _useRecycleBin = BehaviorSubject.create(),
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
        _addOnSelectedTabOnStartup.onNext(App.settings.selectedTabOnStartup);
        pushInstanceSorting();

        _addOnCustomsDownloadPath.onNext(Optional.ofNullable(App.settings.customDownloadsPath)
            .orElse(FileSystem.getUserDownloadsPath(false).toString()));

        _keepLauncherOpen.onNext(App.settings.keepLauncherOpen);
        _enableConsole.onNext(App.settings.enableConsole);
        _enableTrayMenu.onNext(App.settings.enableTrayMenu);
        _enableDiscordIntegration.onNext(App.settings.enableDiscordIntegration);
        _enableFeralGameMode.onNext(App.settings.enableFeralGamemode);
        _disableCustomFonts.onNext(App.settings.disableCustomFonts);
        _rememberWindowSizePosition.onNext(App.settings.rememberWindowSizePosition);
        _useNativeFilePicker.onNext(App.settings.useNativeFilePicker);
        _useRecycleBin.onNext(App.settings.useRecycleBin);
    }

    /**
     * Get the languages to have as options.
     * <p>
     * TODO Upon implementation of translations, ensure the returned value is
     *  cached in the view model to avoid extra processing.
     *
     * @return languages
     */
    public String[] getLanguages() {
        return Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new);
    }

    /**
     * @return Selected language as per settings
     */
    public Observable<Integer> getSelectedLanguage() {
        return _addOnSelectedLanguage.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the launcher language.
     *
     * @param language language
     */
    public void setSelectedLanguage(String language) {
        Language.setLanguage(language);
        App.settings.language = language;
        SettingsManager.post();
    }

    private void pushSelectedLanguage() {
        for (int index = 0; index < getLanguages().length; index++) {
            if (getLanguages()[index].equals(App.settings.language))
                _addOnSelectedLanguage.onNext(index);
        }
    }

    /**
     * Get the themes.
     * <p>
     * This is a cached result, subsequent calls are faster.
     *
     * @return Launcher themes
     */
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

    /**
     * Listen to the theme being changed
     */
    public Observable<Integer> getSelectedTheme() {
        return _selectedTheme.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the launcher theme
     *
     * @param theme Theme id as provided in {@link LauncherTheme}
     */
    public void setSelectedTheme(String theme) {
        Analytics.trackEvent(AnalyticsEvent.forThemeChange(App.THEME.getName()));
        App.settings.theme = theme;
        SettingsManager.post();
        App.loadTheme(App.settings.theme);
        FlatLaf.updateUILater();
        ThemeManager.post();
    }

    private void pushSelectedTheme() {
        for (int index = 0; index < getThemes().size(); index++) {
            if (getThemes().get(index).id.equals(App.settings.theme))
                _selectedTheme.onNext(index);
        }
    }

    /**
     * Get today's date
     */
    public Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        return cal.getTime();
    }

    /**
     * Get the date formats
     *
     * @return date formats
     */
    public String[] getDateFormats() {
        return Constants.DATE_FORMATS;
    }

    /**
     * Listen to date format being changed
     */
    public Observable<Integer> getDateFormat() {
        return _dateFormat.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the selected date format
     *
     * @param format date format
     */
    public void setDateFormat(String format) {
        App.settings.dateFormat = format;
        SettingsManager.post();
    }

    private void pushDateFormat() {
        for (int index = 0; index < getDateFormats().length; index++) {
            if (getDateFormats()[index].equals(App.settings.dateFormat))
                _dateFormat.onNext(index);
        }
    }

    /**
     * Get instance title formats
     *
     * @return instance title formats
     */
    public String[] getInstanceTitleFormats() {
        return Constants.INSTANCE_TITLE_FORMATS;
    }

    /**
     * Set the instance title format
     *
     * @param format instance title format
     */
    public void setInstanceTitleFormat(String format) {
        App.settings.instanceTitleFormat = format;
        SettingsManager.post();
    }

    /**
     * Listen to the instance title format being changed
     */
    public Observable<Integer> getInstanceFormat() {
        return _addOnInstanceFormat.observeOn(SwingSchedulers.edt());
    }

    private void pushInstanceFormat() {
        for (int index = 0; index < getInstanceTitleFormats().length; index++) {
            if (getInstanceTitleFormats()[index].equals(App.settings.instanceTitleFormat))
                _addOnInstanceFormat.onNext(index);
        }
    }

    /**
     * Listen to selected tab on startup being changed
     */
    public Observable<Integer> getSelectedTabOnStartup() {
        return _addOnSelectedTabOnStartup.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set selected tab on startup
     *
     * @param tab selected tab index, starting from 0
     */
    public void setSelectedTabOnStartup(int tab) {
        App.settings.selectedTabOnStartup = tab;
        SettingsManager.post();
    }

    /**
     * Get instance sorting strategies
     *
     * @return instance sorting strategies
     */
    public InstanceSortingStrategies[] getInstanceSorting() {
        return InstanceSortingStrategies.values();
    }

    /**
     * Set the selected instance sorting strategies
     *
     * @param sorting sorting strategy
     */
    public void setInstanceSorting(InstanceSortingStrategies sorting) {
        App.settings.defaultInstanceSorting = sorting;
        SettingsManager.post();
    }

    /**
     * Listen to sorting strategy being changed
     */
    public Observable<Integer> getInstanceSortingObservable() {
        return _addInstanceSorting.observeOn(SwingSchedulers.edt());
    }

    private void pushInstanceSorting() {
        for (int index = 0; index < getInstanceSorting().length; index++) {
            if (getInstanceSorting()[index] == App.settings.defaultInstanceSorting)
                _addInstanceSorting.onNext(index);
        }
    }

    /**
     * Reset the custom download path
     */
    public void resetCustomDownloadPath() {
        App.settings.customDownloadsPath = null;
        SettingsManager.post();
    }

    /**
     * Inform the settings that the custom download path is pending write
     */
    public void setCustomsDownloadPathPending() {
        SettingsValidityManager.setValidity("customDownloadsPath", false);
    }

    /**
     * Listen to the custom download path being changed
     */
    public Observable<String> getCustomsDownloadPath() {
        return _addOnCustomsDownloadPath.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the custom download path
     *
     * @param value download path
     */
    public void setCustomsDownloadPath(String value) {
        App.settings.customDownloadsPath = value;
        SettingsValidityManager.setValidity("customDownloadsPath", true);
        SettingsManager.post();
    }

    /**
     * Listen to keep launcher open changed
     */
    public Observable<Boolean> getKeepLauncherOpen() {
        return _keepLauncherOpen.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the launcher to stay open with a minecraft launch
     *
     * @param b keep launcher open or not
     */
    public void setKeepLauncherOpen(boolean b) {
        App.settings.keepLauncherOpen = b;
        SettingsManager.post();
    }

    /**
     * Listen to console being enabled or not
     */
    public Observable<Boolean> getEnableConsole() {
        return _enableConsole.observeOn(SwingSchedulers.edt());
    }

    /**
     * Enable the console or not
     *
     * @param b console enabled?
     */
    public void setEnableConsole(boolean b) {
        App.settings.enableConsole = b;
        SettingsManager.post();
    }

    /**
     * Set tray menu enabled or not
     *
     * @param b enabled?
     */
    public void setEnableTrayMenuOpen(boolean b) {
        App.settings.enableTrayMenu = b;
        SettingsManager.post();
    }

    /**
     * Listen to tray menu being enabled or not
     */
    public Observable<Boolean> getEnableTrayMenu() {
        return _enableTrayMenu.observeOn(SwingSchedulers.edt());
    }

    public Observable<Boolean> getEnableDiscordIntegration() {
        return _enableDiscordIntegration.observeOn(SwingSchedulers.edt());
    }

    public void setEnableDiscordIntegration(boolean b) {
        App.settings.enableDiscordIntegration = b;
        SettingsManager.post();
    }

    /**
     * Whether to show the option for FeralGameMode
     *
     * @return if to show
     */
    public boolean showFeralGameMode() {
        return OS.isLinux();
    }

    public boolean hasFeralGameMode() {
        return Utils.executableInPath("gamemoderun");
    }

    public Observable<Boolean> getEnableFeralGameMode() {
        return _enableFeralGameMode.observeOn(SwingSchedulers.edt());
    }

    public void setEnableFeralGameMode(boolean b) {
        App.settings.enableFeralGamemode = b;
        SettingsManager.post();
    }


    public Observable<Boolean> getDisableCustomFonts() {
        return _disableCustomFonts.observeOn(SwingSchedulers.edt());
    }

    public void setDisableCustomFonts(boolean b) {
        App.settings.disableCustomFonts = b;
        SettingsManager.post();
    }


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

    public Observable<Boolean> getRememberWindowSizePosition() {
        return _rememberWindowSizePosition.observeOn(SwingSchedulers.edt());
    }

    public boolean getShowNativeFilePickerOption() {
        return !OS.isUsingFlatpak();
    }

    public Observable<Boolean> getUseNativeFilePicker() {
        return _useNativeFilePicker.observeOn(SwingSchedulers.edt());
    }


    public void setUseNativeFilePicker(boolean b) {
        App.settings.useNativeFilePicker = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getUseRecycleBin() {
        return _useRecycleBin.observeOn(SwingSchedulers.edt());
    }

    public void setUseRecycleBin(boolean b) {
        App.settings.useRecycleBin = b;
        SettingsManager.post();
    }


    public boolean showArmSupport() {
        return ConfigManager.getConfigItem("useLwjglReplacement", false);
    }

    public Observable<Boolean> getEnableArmSupport() {
        return enableArmSupport.observeOn(SwingSchedulers.edt());
    }

    public void setEnableArmSupport(boolean b) {
        App.settings.enableArmSupport = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getScanModsOnLaunch() {
        return scanModsOnLaunch.observeOn(SwingSchedulers.edt());
    }

    public void setScanModsOnLaunch(boolean b) {
        App.settings.scanModsOnLaunch = b;
        SettingsManager.post();
    }
}