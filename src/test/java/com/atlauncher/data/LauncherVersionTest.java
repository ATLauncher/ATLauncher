/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LauncherVersionTest {

    private static final LauncherVersion testVersion = new LauncherVersion(1, 0, 0, 0, "Release");

    @Test
    public void test() {
        // Test same version - no update
        assertFalse(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision(), testVersion.getStream())));

        // Test older Reserved - launcher had a big update
        assertTrue(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved() + 1, testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision(), testVersion.getStream())));

        // Test older Major - launcher had major update
        assertTrue(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor() + 1,
                testVersion.getMinor(), testVersion.getRevision(), testVersion.getStream())));

        // Test older Minor - launcher had minor update
        assertTrue(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor() + 1, testVersion.getRevision(), testVersion.getStream())));

        // Test older Revision - launcher had a bug fix
        assertTrue(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision() + 1, testVersion.getStream())));

        // Test user has a beta stream but the real stream comes out
        LauncherVersion betaBuild = new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision(), "Beta");
        LauncherVersion releaseBuild = new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision(), "Release");
        assertTrue(betaBuild.needsUpdate(releaseBuild));

        // Test user has a release stream but a beta build comes out
        assertFalse(releaseBuild.needsUpdate(betaBuild));

        // Test newer Reserved - launcher dev version
        assertFalse(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved() - 1, testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision(), testVersion.getStream())));

        // Test newer Major - launcher dev version
        assertFalse(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor() - 1,
                testVersion.getMinor(), testVersion.getRevision(), testVersion.getStream())));

        // Test newer Minor - launcher dev version
        assertFalse(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor() - 1, testVersion.getRevision(), testVersion.getStream())));

        // Test newer Revision - launcher dev version
        assertFalse(testVersion.needsUpdate(new LauncherVersion(testVersion.getReserved(), testVersion.getMajor(),
                testVersion.getMinor(), testVersion.getRevision() - 1, testVersion.getStream())));
    }

}
