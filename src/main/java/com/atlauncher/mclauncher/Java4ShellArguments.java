/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2026 ATLauncher
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
package com.atlauncher.mclauncher;

import java.util.List;

public final class Java4ShellArguments {
    private static final String UNLOCK_DIAGNOSTIC_OPTIONS = "-XX:+UnlockDiagnosticVMOptions";
    private static final String DISABLE_BASE64_INTRINSICS = "-XX:-UseBASE64Intrinsics";

    private Java4ShellArguments() {}

    public static void apply(List<String> arguments, int javaMajorVersion, boolean disabled) {
        if (disabled || javaMajorVersion < 17) {
            return;
        }

        normalize(arguments);
    }

    static void normalize(List<String> arguments) {
        int insertionIndex = -1;
        for (int i = 0; i < arguments.size();) {
            String argument = arguments.get(i);
            if (argument.equals(UNLOCK_DIAGNOSTIC_OPTIONS) || argument.equals("-XX:-UnlockDiagnosticVMOptions")
                    || argument.equals(DISABLE_BASE64_INTRINSICS) || argument.equals("-XX:+UseBASE64Intrinsics")) {
                if (insertionIndex == -1) {
                    insertionIndex = i;
                }
                arguments.remove(i);
            } else {
                i++;
            }
        }

        // Keep a single ordered pair, even when user and loader arguments overlap or
        // conflict. The caller has not added the Java entry point yet.
        if (insertionIndex == -1) {
            insertionIndex = arguments.size();
        }
        arguments.add(insertionIndex, UNLOCK_DIAGNOSTIC_OPTIONS);
        arguments.add(insertionIndex + 1, DISABLE_BASE64_INTRINSICS);
    }
}
