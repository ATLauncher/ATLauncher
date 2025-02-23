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
package com.atlauncher.evnt.manager;

import com.atlauncher.evnt.listener.RelocalizationListener;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public final class RelocalizationManager {
    private static final PublishSubject<Object> emission = PublishSubject.create();

    // TODO: this leaks listeners as time goes on. Listeners aren't disposed of from non HierarchyPanel components
    public static synchronized Disposable addListener(RelocalizationListener listener) {
        return emission.observeOn(SwingSchedulers.edt()).subscribe((e) -> listener.onRelocalization());
    }

    public static synchronized void post() {
        emission.onNext(0);
    }
}
