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

import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.ModPlatform;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 * <p>
 * Why are there functions here that are acronym?
 * The settings name would be too long otherwise.
 * </p>
 * <p>
 * EAMBD : enableAddedModsByDefault
 * <p>
 * DCMOC : dontCheckModsOnCurseForge
 * <p>
 * DCMOM : dontCheckModsOnModrinth
 * <p>
 * View model for {@link ModsSettingsTab}
 */
public interface IModsSettingsViewModel extends IAbstractSettingsViewModel {

    void setDefaultModPlatform(ModPlatform modPlatform);

    void addOnDefaultModPlatformChanged(Consumer<Integer> onChanged);

    void setAddModRestrictions(AddModRestriction modRestrictions);

    void addOnAddModRestrictionsChanged(Consumer<Integer> onChanged);

    void setEAMBD(Boolean b);

    void addOnEAMBDChanged(Consumer<Boolean> onChanged);

    void setDCMOC(Boolean b);

    void addOnDCMOCChanged(Consumer<Boolean> onChanged);

    void setDCMOM(Boolean b);

    void addOnDCMOMChanged(Consumer<Boolean> onChanged);

    void setDefaultExportFormat(InstanceExportFormat exportFormat);

    void addOnDefaultExportFormatChanged(Consumer<Integer> onChanged);
}