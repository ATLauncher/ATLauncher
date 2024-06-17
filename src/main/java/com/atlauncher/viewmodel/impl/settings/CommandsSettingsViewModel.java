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

import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.viewmodel.base.settings.ICommandsSettingsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 15
 */
public class CommandsSettingsViewModel implements ICommandsSettingsViewModel {
    private final BehaviorSubject<Boolean> enableCommands =
        BehaviorSubject.createDefault(App.settings.enableCommands);

    private final BehaviorSubject<String>
        preLaunchCommand = BehaviorSubject.create(),
        postExitCommand = BehaviorSubject.create(),
        wrapperCommand = BehaviorSubject.create();

    public CommandsSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    private String nullIfEmpty(String str) {
        if (str.isEmpty())
            return null;
        else
            return str;
    }

    @Override
    public void onSettingsSaved() {
        enableCommands.onNext(App.settings.enableCommands);
        preLaunchCommand.onNext(Optional.ofNullable(App.settings.preLaunchCommand).orElse(""));
        postExitCommand.onNext(Optional.ofNullable(App.settings.postExitCommand).orElse(""));
        wrapperCommand.onNext(Optional.ofNullable(App.settings.wrapperCommand).orElse(""));
    }

    @Override
    public Observable<Boolean> getEnableCommands() {
        return enableCommands.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableCommands(boolean b) {
        App.settings.enableCommands = b;
        SettingsManager.post();
    }

    @Override
    public void setPreLaunchCommandPending() {
        SettingsValidityManager.setValidity("preLaunchCommand", false);
    }

    @Override
    public Observable<String> getPreLaunchCommand() {
        return preLaunchCommand.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setPreLaunchCommand(String text) {
        App.settings.preLaunchCommand = nullIfEmpty(text);
        SettingsValidityManager.setValidity("preLaunchCommand", true);
        SettingsManager.post();
    }

    @Override
    public void setPostExitCommandPending() {
        SettingsValidityManager.setValidity("setPostExitCommand", false);
    }

    @Override
    public Observable<String> getPostExitCommand() {
        return postExitCommand.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setPostExitCommand(String text) {
        App.settings.postExitCommand = nullIfEmpty(text);
        SettingsValidityManager.setValidity("setPostExitCommand", true);
        SettingsManager.post();
    }

    @Override
    public void setWrapperCommandPending() {
        SettingsValidityManager.setValidity("wrapperCommand", false);
    }

    @Override
    public Observable<String> getWrapperCommand() {
        return wrapperCommand.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setWrapperCommand(String text) {
        App.settings.wrapperCommand = nullIfEmpty(text);
        SettingsValidityManager.setValidity("wrapperCommand", true);
        SettingsManager.post();
    }
}