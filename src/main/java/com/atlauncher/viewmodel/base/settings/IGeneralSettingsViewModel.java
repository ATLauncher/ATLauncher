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
package com.atlauncher.viewmodel.base.settings;

import java.util.Date;
import java.util.List;

import com.atlauncher.data.LauncherTheme;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.utils.sort.InstanceSortingStrategies;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 15
 * <p>
 * View model for {@link GeneralSettingsTab}
 */
public interface IGeneralSettingsViewModel extends SettingsListener {
    /**
     * Get the languages to have as options.
     * <p>
     * TODO Upon implementation of translations, ensure the returned value is
     *  cached in the view model to avoid extra processing.
     *
     * @return languages
     */
    String[] getLanguages();

    /**
     * Set the launcher language.
     *
     * @param language language
     */
    void setSelectedLanguage(String language);

    Observable<Integer> getSelectedLanguage();

    /**
     * Get the themes.
     * <p>
     * This is a cached result, subsequent calls are faster.
     *
     * @return Launcher themes
     */
    List<LauncherTheme> getThemes();

    /**
     * Set the launcher theme
     *
     * @param theme Theme id as provided in {@link LauncherTheme}
     */
    void setSelectedTheme(String theme);

    /**
     * Listen to the theme being changed
     */
    Observable<Integer> getSelectedTheme();

    /**
     * Get today's date
     */
    Date getDate();

    /**
     * Get the date formats
     *
     * @return date formats
     */
    String[] getDateFormats();

    /**
     * Set the selected date format
     *
     * @param format date format
     */
    void setDateFormat(String format);

    /**
     * Listen to date format being changed
     */
    Observable<Integer> getDateFormat();

    /**
     * Get instance title formats
     *
     * @return instance title formats
     */
    String[] getInstanceTitleFormats();

    /**
     * Set the instance title format
     *
     * @param format instance title format
     */
    void setInstanceTitleFormat(String format);

    /**
     * Listen to the instance title format being changed
     */
    Observable<Integer> getInstanceFormat();

    /**
     * Set selected tab on startup
     *
     * @param tab selected tab index, starting from 0
     */
    void setSelectedTabOnStartup(int tab);


    /**
     * Listen to selected tab on startup being changed
     */
    Observable<Integer> getSelectedTabOnStartup();

    /**
     * Get instance sorting strategies
     *
     * @return instance sorting strategies
     */
    InstanceSortingStrategies[] getInstanceSorting();

    /**
     * Set the selected instance sorting strategies
     *
     * @param sorting sorting strategy
     */
    void setInstanceSorting(InstanceSortingStrategies sorting);

    /**
     * Listen to sorting strategy being changed
     */
    Observable<Integer> getInstanceSortingObservable();

    /**
     * Reset the custom download path
     */
    void resetCustomDownloadPath();

    /**
     * Set the custom download path
     *
     * @param value download path
     */
    void setCustomsDownloadPath(String value);

    /**
     * Inform the settings that the custom download path is pending write
     */
    void setCustomsDownloadPathPending();

    /**
     * Listen to the custom download path being changed
     */
    Observable<String> getCustomsDownloadPath();

    /**
     * Set the launcher to stay open with a minecraft launch
     *
     * @param b keep launcher open or not
     */
    void setKeepLauncherOpen(boolean b);

    /**
     * Listen to keep launcher open changed
     */
    Observable<Boolean> getKeepLauncherOpen();

    /**
     * Enable the console or not
     *
     * @param b console enabled?
     */
    void setEnableConsole(boolean b);

    /**
     * Listen to console being enabled or not
     */
    Observable<Boolean> getEnableConsole();

    /**
     * Set tray menu enabled or not
     *
     * @param b enabled?
     */
    void setEnableTrayMenuOpen(boolean b);

    /**
     * Listen to tray menu being enabled or not
     */
    Observable<Boolean> getEnableTrayMenu();

    void setEnableDiscordIntegration(boolean b);

    Observable<Boolean> getEnableDiscordIntegration();

    /**
     * Whether to show the option for FeralGameMode
     *
     * @return if to show
     */
    boolean showFeralGameMode();

    boolean hasFeralGameMode();

    void setEnableFeralGameMode(boolean b);

    Observable<Boolean> getEnableFeralGameMode();

    void setDisableCustomFonts(boolean b);

    Observable<Boolean> getDisableCustomFonts();

    void setRememberWindowStuff(boolean b);

    Observable<Boolean> getRememberWindowStuff();

    boolean getShowNativeFilePickerOption();

    void setUseNativeFilePicker(boolean b);

    Observable<Boolean> getUseNativeFilePicker();

    void setUseRecycleBin(boolean b);

    Observable<Boolean> getUseRecycleBin();

    boolean showArmSupport();

    Observable<Boolean> getEnableArmSupport();

    void setEnableArmSupport(boolean b);

    Observable<Boolean> getScanModsOnLaunch();

    void setScanModsOnLaunch(boolean b);
}