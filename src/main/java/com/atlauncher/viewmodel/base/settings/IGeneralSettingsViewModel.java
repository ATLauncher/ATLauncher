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

import com.atlauncher.gui.tabs.settings.GeneralSettingsTab;
import com.atlauncher.utils.sort.InstanceSortingStrategies;

import java.util.List;
import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 * <p>
 * View model for {@link GeneralSettingsTab}
 */
public interface IGeneralSettingsViewModel extends IAbstractSettingsViewModel {
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

    void addOnSelectedLanguageChanged(Consumer<Integer> changed);

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
     *
     * @param onThemeChanged invoked when changed
     */
    void addOnSelectedThemeChanged(Consumer<Integer> onThemeChanged);

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
     *
     * @param onDateFormatChanged invoked on changed
     */
    void addOnDateFormatChanged(Consumer<Integer> onDateFormatChanged);

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
     *
     * @param instance invoked on changed
     */
    void addOnInstanceFormatChanged(Consumer<Integer> instance);

    /**
     * Set selected tab on startup
     *
     * @param tab selected tab index, starting from 0
     */
    void setSelectedTabOnStartup(int tab);

    /**
     * Listen to selected tab on startup being changed
     *
     * @param onChanged invoked on change
     */
    void addOnSelectedTabOnStartupChanged(Consumer<Integer> onChanged);

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
     *
     * @param onChanged invoked on change
     */
    void addInstanceSortingChanged(Consumer<Integer> onChanged);

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
     *
     * @param onChanged invoked on change
     */
    void addOnCustomsDownloadPathChanged(Consumer<String> onChanged);

    /**
     * Set the launcher to stay open with a minecraft launch
     *
     * @param b keep launcher open or not
     */
    void setKeepLauncherOpen(boolean b);

    /**
     * Listen to keep launcher open changed
     *
     * @param onChanged invoked on change
     */
    void addOnKeepLauncherOpenChanged(Consumer<Boolean> onChanged);

    /**
     * Enable the console or not
     *
     * @param b console enabled?
     */
    void setEnableConsole(boolean b);

    /**
     * Listen to console being enabled or not
     *
     * @param onChanged invoked on change
     */
    void addOnEnableConsoleChanged(Consumer<Boolean> onChanged);

    /**
     * Set tray menu enabled or not
     *
     * @param b enabled?
     */
    void setEnableTrayMenuOpen(boolean b);

    /**
     * Listen to tray menu being enabled or not
     *
     * @param onChanged invoked on changed
     */
    void addOnEnableTrayMenuChanged(Consumer<Boolean> onChanged);

    void setEnableDiscordIntegration(boolean b);

    void addOnEnableDiscordIntegrationChanged(Consumer<Boolean> onChanged);

    /**
     * Whether to show the option for FeralGameMode
     *
     * @return if to show
     */
    boolean showFeralGameMode();

    void setEnableFeralGameMode(boolean b);

    void addOnEnableFeralGameModeChanged(Consumer<Boolean> onChanged);

    void setDisableCustomFonts(boolean b);

    void addOnDisableCustomFontsChanged(Consumer<Boolean> onChanged);

    void setRememberWindowStuff(boolean b);

    void addOnRememberWindowStuffChanged(Consumer<Boolean> onChanged);

    boolean getShowNativeFilePickerOption();

    void setUseNativeFilePicker(boolean b);

    void addOnUseNativeFilePickerChanged(Consumer<Boolean> onChanged);

    void setUseRecycleBin(boolean b);

    void addOnUseRecycleBinChanged(Consumer<Boolean> onChanged);

    /**
     * Data object for UI representation.
     */
    public class LauncherTheme {
        /**
         * Identification of the theme
         */
        public final String id;

        /**
         * UI Label for the theme
         */
        public final String label;

        public LauncherTheme(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
}