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
package com.atlauncher.data;

import org.junit.jupiter.api.Test;

import com.atlauncher.utils.Hashing;

import static org.junit.jupiter.api.Assertions.*;

public class LauncherVersionTest {

    private static final LauncherVersion testVersion = new LauncherVersion(1, 0, 0, 0, "Release",
        Hashing.EMPTY_HASH_CODE);

    private static final LauncherVersion semanticVersion = new LauncherVersion(null, 0, 0, 0, "Release",
        Hashing.EMPTY_HASH_CODE);

    @Test
    public void test() {
        LauncherVersion[] versions = {testVersion, semanticVersion};
        for (LauncherVersion version : versions) {

            // Test same version - no update
            assertFalse(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor(), version.getRevision(), version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test older Reserved - launcher had a big update
            if (version.getReserved() != null) {
                assertTrue(
                    version.needsUpdate(
                        new LauncherVersion(
                            version.getReserved() + 1,
                            version.getMajor(),
                            version.getMinor(),
                            version.getRevision(),
                            version.getStream(),
                            Hashing.EMPTY_HASH_CODE
                        )
                    )
                );
            }

            // Test old to new semver
            if (version.getReserved() != null)
                assertTrue(
                    version.needsUpdate(
                        new LauncherVersion(
                            null,
                            version.getMajor(),
                            version.getMinor(),
                            version.getRevision(),
                            version.getStream(),
                            Hashing.EMPTY_HASH_CODE
                        )
                    )
                );

            // Test older Major - launcher had major update
            assertTrue(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor() + 1,
                version.getMinor(), version.getRevision(), version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test older Minor - launcher had minor update
            assertTrue(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor() + 1, version.getRevision(), version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test older Revision - launcher had a bug fix
            assertTrue(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor(), version.getRevision() + 1, version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test user has a beta stream but the real stream comes out
            LauncherVersion betaBuild = new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor(), version.getRevision(), "Beta", Hashing.EMPTY_HASH_CODE);
            LauncherVersion releaseBuild = new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor(), version.getRevision(), "Release", Hashing.EMPTY_HASH_CODE);
            assertTrue(betaBuild.needsUpdate(releaseBuild));

            // Test user has a release stream but a beta build comes out
            assertFalse(releaseBuild.needsUpdate(betaBuild));

            if (version.getReserved() != null) {
                // Test newer Reserved - launcher dev version
                assertFalse(version.needsUpdate(new LauncherVersion(version.getReserved() - 1, version.getMajor(),
                    version.getMinor(), version.getRevision(), version.getStream(),
                    Hashing.EMPTY_HASH_CODE)));
            } else {
                // Test newer semver to current old versioning
                assertFalse(version.needsUpdate(new LauncherVersion(1, version.getMajor(),
                    version.getMinor(), version.getRevision(), version.getStream(),
                    Hashing.EMPTY_HASH_CODE)));
            }


            // Test newer Major - launcher dev version
            assertFalse(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor() - 1,
                version.getMinor(), version.getRevision(), version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test newer Minor - launcher dev version
            assertFalse(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor() - 1, version.getRevision(), version.getStream(),
                Hashing.EMPTY_HASH_CODE)));

            // Test newer Revision - launcher dev version
            assertFalse(version.needsUpdate(new LauncherVersion(version.getReserved(), version.getMajor(),
                version.getMinor(), version.getRevision() - 1, version.getStream(),
                Hashing.EMPTY_HASH_CODE)));
        }
    }

}
