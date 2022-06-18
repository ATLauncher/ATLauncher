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
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.evnt.manager.SettingsManager;

import java.util.function.Consumer;

/**
 * 17 / 06 / 2022
 */
public class ModsSettingsViewModel implements IModsSettingsViewModel {
    public ModsSettingsViewModel() {
        SettingsManager.addListener(this);
    }

    private Consumer<Integer>
        _addOnDefaultModPlatformChanged,
        _addOnAddModRestrictionsChanged,
        _addOnDefaultExportFormatChanged;

    private Consumer<Boolean>
        _addOnEAMBDChanged,
        _addOnDCMOCChanged,
        _addOnDCMOMChanged;

    @Override
    public void onSettingsSaved() {
        _addOnDefaultModPlatformChanged.accept(App.settings.defaultModPlatform.ordinal());
        _addOnAddModRestrictionsChanged.accept(App.settings.addModRestriction.ordinal());
        _addOnDefaultExportFormatChanged.accept(App.settings.defaultExportFormat.ordinal());

        _addOnEAMBDChanged.accept(App.settings.enableAddedModsByDefault);
        _addOnDCMOCChanged.accept(App.settings.dontCheckModsOnCurseForge);
        _addOnDCMOMChanged.accept(App.settings.dontCheckModsOnModrinth);
    }

    @Override
    public void setDefaultModPlatform(ModPlatform modPlatform) {
        App.settings.defaultModPlatform = modPlatform;
        SettingsManager.post();
    }

    @Override
    public void addOnDefaultModPlatformChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.defaultModPlatform.ordinal());
        _addOnDefaultModPlatformChanged = onChanged;
    }

    @Override
    public void setAddModRestrictions(AddModRestriction modRestrictions) {
        App.settings.addModRestriction = modRestrictions;
        SettingsManager.post();
    }

    @Override
    public void addOnAddModRestrictionsChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.addModRestriction.ordinal());
        _addOnAddModRestrictionsChanged = onChanged;
    }

    @Override
    public void setEAMBD(Boolean b) {
        App.settings.enableAddedModsByDefault = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEAMBDChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableAddedModsByDefault);
        _addOnEAMBDChanged = onChanged;
    }

    @Override
    public void setDCMOC(Boolean b) {
        App.settings.dontCheckModsOnCurseForge = b;
        SettingsManager.post();
    }

    @Override
    public void addOnDCMOCChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.dontCheckModsOnCurseForge);
        _addOnDCMOCChanged = onChanged;
    }

    @Override
    public void setDCMOM(Boolean b) {
        App.settings.dontCheckModsOnModrinth = b;
        SettingsManager.post();
    }

    @Override
    public void addOnDCMOMChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.dontCheckModsOnModrinth);
        _addOnDCMOMChanged = onChanged;
    }

    @Override
    public void setDefaultExportFormat(InstanceExportFormat exportFormat) {
        App.settings.defaultExportFormat = exportFormat;
        SettingsManager.post();
    }

    @Override
    public void addOnDefaultExportFormatChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.defaultExportFormat.ordinal());
        _addOnDefaultExportFormatChanged = onChanged;
    }
}
