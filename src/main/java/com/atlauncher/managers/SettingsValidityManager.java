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
package com.atlauncher.managers;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2024 / 05 / 23
 */
public class SettingsValidityManager {
    /**
     * Backing state for validities.
     */
    private static BehaviorSubject<Map<String, Boolean>> validities =
        BehaviorSubject.createDefault(new HashMap<>());

    public static Observable<Boolean> isValid =
        validities.map(SettingsValidityManager::isValidAtAll);

    /**
     * @return are all settings valid?
     */
    private static boolean isValidAtAll(Map<String, Boolean> validities) {
        if (!validities.isEmpty())
            for (boolean validity : validities.values()) {
                if (!validity)
                    return false;
            }
        return true;
    }

    /**
     * Set the validity for a given setting.
     *
     * @param key     unique id for the setting.
     * @param isValid if said setting is valid or not.
     */
    public static synchronized void setValidity(String key, Boolean isValid) {
        Map<String, Boolean> map = new HashMap<>(validities.getValue());
        map.put(key, isValid);
        validities.onNext(map);
    }
}
