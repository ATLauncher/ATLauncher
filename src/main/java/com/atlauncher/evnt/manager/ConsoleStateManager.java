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

import javax.annotation.Nonnull;

import com.atlauncher.data.ConsoleState;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public final class ConsoleStateManager {
    private static final BehaviorSubject<ConsoleState> state = BehaviorSubject.createDefault(ConsoleState.CLOSED);

    private ConsoleStateManager() {
    }

    public static void setState(@Nonnull ConsoleState newState) {
        state.onNext(newState);
    }

    public static @Nonnull ConsoleState getState() {
        return state.getValue();
    }

    public static @Nonnull Observable<ConsoleState> getObservable(){
        return state;
    }
}
