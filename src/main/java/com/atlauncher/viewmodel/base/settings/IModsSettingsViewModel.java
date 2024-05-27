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

import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.ModsSettingsTab;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 15
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
 * ACFABF : allowCurseForgeAlphaBetaFiles
 * <p>
 * View model for {@link ModsSettingsTab}
 */
public interface IModsSettingsViewModel extends SettingsListener {

    void setDefaultModPlatform(ModPlatform modPlatform);

    Observable<Integer> DefaultModPlatformChanged();

    void setAddModRestrictions(AddModRestriction modRestrictions);

    Observable<Integer> AddModRestrictionsChanged();

    void setEAMBD(Boolean b);

    Observable<Boolean> EAMBDChanged();

    void setDCMOC(Boolean b);

    Observable<Boolean> DCMOCChanged();

    void setDCMOM(Boolean b);

    Observable<Boolean> DCMOMChanged();

    void setDefaultExportFormat(InstanceExportFormat exportFormat);

    Observable<Integer> DefaultExportFormatChanged();

    void setACFABF(boolean b);

    Observable<Boolean> getACFABF();
}