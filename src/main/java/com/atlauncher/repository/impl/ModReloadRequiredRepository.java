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
package com.atlauncher.repository.impl;

import com.atlauncher.repository.base.IModReloadRequiredRepository;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class ModReloadRequiredRepository implements IModReloadRequiredRepository {

    private static ModReloadRequiredRepository repo = null;

    private final BehaviorSubject<Boolean> modReloadRequired =
        BehaviorSubject.createDefault(false);

    private ModReloadRequiredRepository() {
    }

    public static IModReloadRequiredRepository get() {
        if (repo == null) repo = new ModReloadRequiredRepository();

        return repo;
    }

    @Override
    public Subject<Boolean> getModReloadRequiredObservable() {
        return modReloadRequired;
    }

    @Override
    public Boolean getModReloadRequired() {
        return modReloadRequired.getValue();
    }

    @Override
    public void setModReloadRequired(boolean required) {
        modReloadRequired.onNext(required);
    }
}
