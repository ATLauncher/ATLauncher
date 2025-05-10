/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2025 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.atlauncher.viewmodel.impl.settings;

import java.util.HashMap;
import java.util.Map;

import com.atlauncher.App;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * ViewModel for Environment Variables Tab
 */
public class EnvironmentVariablesViewModel {
    private final BehaviorSubject<Map<String, String>> environmentVariablesSubject = BehaviorSubject
            .createDefault(new HashMap<>());

    public EnvironmentVariablesViewModel() {
        // Load from settings
        Map<String, String> env = App.settings.environmentVariables;
        if (env != null) {
            environmentVariablesSubject.onNext(new HashMap<>(env));
        }
    }

    public Observable<Map<String, String>> getEnvironmentVariablesObservable() {
        return environmentVariablesSubject.hide();
    }

    /**
     * Adds a new environment variable. Returns false if the key already exists.
     */
    public boolean addVariable(String name, String value) {
        Map<String, String> current = new HashMap<>(environmentVariablesSubject.getValue());
        if (current.containsKey(name)) {
            return false;
        }
        current.put(name, value);
        environmentVariablesSubject.onNext(current);
        App.settings.environmentVariables = current;
        return true;
    }

    /**
     * Updates an environment variable. Returns false if newName is a duplicate (except for rename to same key).
     */
    public boolean updateVariable(String oldName, String newName, String value) {
        Map<String, String> current = new HashMap<>(environmentVariablesSubject.getValue());
        if (!oldName.equals(newName) && current.containsKey(newName)) {
            return false;
        }
        current.remove(oldName);
        current.put(newName, value);
        environmentVariablesSubject.onNext(current);
        App.settings.environmentVariables = current;
        return true;
    }

    public void removeVariable(String name) {
        Map<String, String> current = new HashMap<>(environmentVariablesSubject.getValue());
        current.remove(name);
        environmentVariablesSubject.onNext(current);
        App.settings.environmentVariables = current;
    }

    public void clearAll() {
        environmentVariablesSubject.onNext(new HashMap<>());
        App.settings.environmentVariables = new HashMap<>();
    }
}
