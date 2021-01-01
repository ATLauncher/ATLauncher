/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.data.minecraft;

import java.util.ArrayList;
import java.util.List;

import com.atlauncher.annot.Json;

@Json
public class Arguments {
    public List<ArgumentRule> game;
    public List<ArgumentRule> jvm;

    public Arguments(List<ArgumentRule> game, List<ArgumentRule> jvm) {
        this.game = game;
        this.jvm = jvm;

        if (this.game == null) {
            this.game = new ArrayList<>();
        }

        if (this.jvm == null) {
            this.jvm = new ArrayList<>();
        }
    }

    public Arguments(List<ArgumentRule> game) {
        this(game, null);
    }

    public Arguments() {
        this(null, null);
    }

    public String asString() {
        StringBuilder arguments = new StringBuilder();

        for (ArgumentRule rule : this.jvm) {
            if (rule.applies()) {
                arguments.append(" ").append(rule.getValueAsString());
            }
        }

        for (ArgumentRule rule : this.game) {
            if (rule.applies()) {
                arguments.append(" ").append(rule.getValueAsString());
            }
        }

        return arguments.toString();
    }

    public List<String> asStringList() {
        List<String> arguments = new ArrayList<>();

        if (this.jvm != null) {
            for (ArgumentRule rule : this.jvm) {
                if (rule.applies()) {
                    arguments.addAll(rule.getValueAsList());
                }
            }
        }

        if (this.game != null) {
            for (ArgumentRule rule : this.game) {
                if (rule.applies()) {
                    arguments.addAll(rule.getValueAsList());
                }
            }
        }

        return arguments;
    }

    public List<String> jvmAsStringList() {
        List<String> arguments = new ArrayList<>();

        if (this.jvm != null) {
            for (ArgumentRule rule : this.jvm) {
                if (rule.applies()) {
                    arguments.addAll(rule.getValueAsList());
                }
            }
        }

        return arguments;
    }

    public List<String> gameAsStringList() {
        List<String> arguments = new ArrayList<>();

        if (this.game != null) {
            for (ArgumentRule rule : this.game) {
                if (rule.applies()) {
                    arguments.addAll(rule.getValueAsList());
                }
            }
        }

        return arguments;
    }
}
