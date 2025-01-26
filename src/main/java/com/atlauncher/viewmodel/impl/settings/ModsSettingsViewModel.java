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
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.tabs.settings.ModsSettingsTab;
import com.atlauncher.repository.base.IModReloadRequiredRepository;
import com.atlauncher.repository.impl.ModReloadRequiredRepository;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 17
 * <p>
 * View model for {@link ModsSettingsTab}
 */
public class ModsSettingsViewModel implements SettingsListener {
    private final IModReloadRequiredRepository modReloadRequiredRepository =
        ModReloadRequiredRepository.get();

    private final BehaviorSubject<Integer>
        _defaultModPlatform = BehaviorSubject.create(),
        _addModRestriction = BehaviorSubject.create(),
        _defaultExportFormat = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _enableAddedModsByDefault = BehaviorSubject.create(),
        _showFabricModsWhenSinytraInstalled = BehaviorSubject.create(),
        _doNotCheckModsOnCurseForge = BehaviorSubject.create(),
        _doNotCheckModsOnModrinth = BehaviorSubject.create(),
        allowCurseForgeAlphaBetaFiles = BehaviorSubject.create();

    public ModsSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _defaultModPlatform.onNext(App.settings.defaultModPlatform.ordinal());
        _addModRestriction.onNext(App.settings.addModRestriction.ordinal());
        _defaultExportFormat.onNext(App.settings.defaultExportFormat.ordinal());

        _enableAddedModsByDefault.onNext(App.settings.enableAddedModsByDefault);
        _showFabricModsWhenSinytraInstalled.onNext(App.settings.showFabricModsWhenSinytraInstalled);
        _doNotCheckModsOnCurseForge.onNext(App.settings.dontCheckModsOnCurseForge);
        _doNotCheckModsOnModrinth.onNext(App.settings.dontCheckModsOnModrinth);
        allowCurseForgeAlphaBetaFiles.onNext(App.settings.allowCurseForgeAlphaBetaFiles);
    }

    public Observable<Integer> getDefaultModPlatform() {
        return _defaultModPlatform.observeOn(SwingSchedulers.edt());
    }

    public void setDefaultModPlatform(ModPlatform modPlatform) {
        App.settings.defaultModPlatform = modPlatform;
        SettingsManager.post();
    }

    public void setAddModRestrictions(AddModRestriction modRestrictions) {
        App.settings.addModRestriction = modRestrictions;
        SettingsManager.post();
    }

    public Observable<Integer> getAddModRestriction() {
        return _addModRestriction.observeOn(SwingSchedulers.edt());
    }

    public Observable<Boolean> getEnableAddedModsByDefault() {
        return _enableAddedModsByDefault.observeOn(SwingSchedulers.edt());
    }

    public void setEnableAddedModsByDefault(Boolean b) {
        App.settings.enableAddedModsByDefault = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getShowFabricModsWhenSinytraInstalled() {
        return _showFabricModsWhenSinytraInstalled.observeOn(SwingSchedulers.edt());
    }

    public void setShowFabricModsWhenSinytraInstalled(Boolean b) {
        App.settings.showFabricModsWhenSinytraInstalled = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getDoNotCheckModsOnCurseForge() {
        return _doNotCheckModsOnCurseForge.observeOn(SwingSchedulers.edt());
    }

    public void setDoNotCheckModsOnCurseForge(Boolean b) {
        App.settings.dontCheckModsOnCurseForge = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getDoNotCheckModsOnModrinth() {
        return _doNotCheckModsOnModrinth.observeOn(SwingSchedulers.edt());
    }

    public void setDoNotCheckModsOnModrinth(Boolean b) {
        App.settings.dontCheckModsOnModrinth = b;
        SettingsManager.post();
    }

    public Observable<Integer> getDefaultExportFormat() {
        return _defaultExportFormat.observeOn(SwingSchedulers.edt());
    }

    public void setDefaultExportFormat(InstanceExportFormat exportFormat) {
        App.settings.defaultExportFormat = exportFormat;
        SettingsManager.post();
    }

    public Observable<Boolean> getAllowCurseForgeAlphaBetaFiles() {
        return allowCurseForgeAlphaBetaFiles.observeOn(SwingSchedulers.edt());
    }

    public void setAllowCurseForgeAlphaBetaFiles(boolean b) {
        if (App.settings.allowCurseForgeAlphaBetaFiles != b) {
            modReloadRequiredRepository.setModReloadRequired(true);
        }

        App.settings.allowCurseForgeAlphaBetaFiles = b;
        SettingsManager.post();
    }
}