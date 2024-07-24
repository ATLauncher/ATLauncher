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

import com.atlauncher.App;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.repository.base.IModReloadRequiredRepository;
import com.atlauncher.repository.impl.ModReloadRequiredRepository;
import com.atlauncher.viewmodel.base.settings.ISettingsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 19
 */
public class SettingsViewModel implements ISettingsViewModel {
    private final IModReloadRequiredRepository modReloadRequiredRepository = ModReloadRequiredRepository.get();

    @Override
    public void save() {
        App.settings.save();
        SettingsManager.post();
        App.TOASTER.pop("Settings Saved");
        if (modReloadRequiredRepository.getModReloadRequired()){
            App.launcher.checkForExternalPackUpdates();
        }
    }

    @Override
    public Observable<Boolean> getSaveEnabled() {
        return SettingsValidityManager.isValid.observeOn(SwingSchedulers.edt());
    }
}