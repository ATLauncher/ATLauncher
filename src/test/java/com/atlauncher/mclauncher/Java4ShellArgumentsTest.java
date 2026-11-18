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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class Java4ShellArgumentsTest {
    private static final String UNLOCK = "-XX:+UnlockDiagnosticVMOptions";
    private static final String DISABLE = "-XX:-UseBASE64Intrinsics";

    @Test
    void addsMissingArgumentsWithoutChangingOtherOptions() {
        List<String> arguments = new ArrayList<>(Arrays.asList("java", "-Xmx4G", "-cp", "libraries.jar"));

        Java4ShellArguments.normalize(arguments);

        assertEquals(Arrays.asList("java", "-Xmx4G", "-cp", "libraries.jar", UNLOCK, DISABLE), arguments);
    }

    @Test
    void handlesEitherOrBothExistingArgumentsWithoutDuplicates() {
        for (List<String> existing : Arrays.asList(Arrays.asList(UNLOCK), Arrays.asList(DISABLE),
            Arrays.asList(UNLOCK, DISABLE), Arrays.asList(DISABLE, UNLOCK))) {
            List<String> arguments = new ArrayList<>();
            arguments.add("java");
            arguments.addAll(existing);
            arguments.add("-Xmx4G");

            Java4ShellArguments.normalize(arguments);

            assertEquals(Arrays.asList("java", UNLOCK, DISABLE, "-Xmx4G"), arguments);
        }
    }

    @Test
    void normalizesOverlappingUserAndProfileArgumentsAndConflictingFlags() {
        List<String> arguments = new ArrayList<>(Arrays.asList("java", UNLOCK, "-Xmx4G", DISABLE,
            "-XX:-UnlockDiagnosticVMOptions", "-XX:+UseBASE64Intrinsics", UNLOCK, DISABLE, "-Dexample=true"));

        Java4ShellArguments.normalize(arguments);
        Java4ShellArguments.normalize(arguments);

        assertEquals(Arrays.asList("java", UNLOCK, DISABLE, "-Xmx4G", "-Dexample=true"), arguments);
    }

    @Test
    void appliesArgumentsForJava17AndNewer() {
        for (int version : new int[] { 17, 18, 21, 25, 27 }) {
            List<String> arguments = new ArrayList<>(Arrays.asList("selected-java", "-Xmx4G"));

            Java4ShellArguments.apply(arguments, version, false);

            assertEquals(Arrays.asList("selected-java", "-Xmx4G", UNLOCK, DISABLE), arguments);
        }
    }

    @Test
    void optOutPreservesManualArguments() {
        List<String> original = Arrays.asList("java", DISABLE, UNLOCK, "-XX:+UseBASE64Intrinsics");
        List<String> arguments = new ArrayList<>(original);

        Java4ShellArguments.apply(arguments, 21, true);

        assertEquals(original, arguments);
    }

    @Test
    void olderOrUnknownJavaDoesNotReceiveArguments() {
        for (int version : new int[] { -1, 7, 8, 11, 16 }) {
            List<String> original = Arrays.asList("selected-java", "-Xmx4G");
            List<String> arguments = new ArrayList<>(original);

            Java4ShellArguments.apply(arguments, version, false);

            assertEquals(original, arguments);
        }
    }
}
