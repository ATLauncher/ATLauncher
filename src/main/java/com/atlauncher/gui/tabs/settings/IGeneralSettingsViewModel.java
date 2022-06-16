package com.atlauncher.gui.tabs.settings;

import com.atlauncher.utils.sort.InstanceSortingStrategies;

import java.util.List;
import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public interface IGeneralSettingsViewModel extends IAbstractSettingsViewModel {
    String[] getLanguages();

    void setSelectedLanguage(String language);

    void addOnSelectedLanguageChanged(Consumer<Integer> changed);

    List<LauncherTheme> getThemes();

    void setSelectedTheme(String theme);

    void addOnSelectedThemeChanged(Consumer<Integer> onThemeChanged);

    String[] getDateFormats();

    void setDateFormat(String format);

    void addOnDateFormatChanged(Consumer<Integer> onDateFormatChanged);

    String[] getInstanceTitleFormats();

    void setInstanceTitleFormat(String format);

    void addOnInstanceFormatChanged(Consumer<Integer> instance);

    void setSelectedTabOnStartup(int tab);

    void addOnSelectedTabOnStartupChanged(Consumer<Integer> onChanged);

    InstanceSortingStrategies[] getInstanceSorting();

    void setInstanceSorting(InstanceSortingStrategies sorting);

    void addInstanceSortingChanged(Consumer<Integer> onChanged);

    void setCustomsDownloadPath(String value);

    void addOnCustomsDownloadPathChanged(Consumer<String> onChanged);

    void setKeepLauncherOpen(boolean b);

    void addOnKeepLauncherOpenChanged(Consumer<Boolean> onChanged);

    void setEnableConsole(boolean b);

    void addOnEnableConsoleChanged(Consumer<Boolean> onChanged);

    void setEnableTrayMenuOpen(boolean b);

    void addOnEnableTrayMenuChanged(Consumer<Boolean> onChanged);

    void setEnableDiscordIntegration(boolean b);

    void addOnEnableDiscordIntegrationChanged(Consumer<Boolean> onChanged);

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

    class LauncherTheme {
        String id;
        String label;

        public LauncherTheme(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
}