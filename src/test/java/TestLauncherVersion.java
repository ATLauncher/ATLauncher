/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.data.Constants;
import com.atlauncher.data.LauncherVersion;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestLauncherVersion {

    @Test
    public void test() {
        // Test same version - no update
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants.VERSION
                .getBuild())));

        // Test older Reserved - launcher had a big update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved() + 1, Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants.VERSION
                .getBuild())));

        // Test older Major - launcher had major update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor() + 1, Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants
                .VERSION.getBuild())));

        // Test older Minor - launcher had minor update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor() + 1, Constants.VERSION.getRevision(), Constants
                .VERSION.getBuild())));

        // Test older Revision - launcher had a bug fix
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision() + 1, Constants
                .VERSION.getBuild())));

        // Test older Build - launcher had a beta update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants.VERSION
                .getBuild() + 1)));

        // Test user has a beta build but the real build comes out
        LauncherVersion testBuild = new LauncherVersion(Constants.VERSION.getReserved(), Constants.VERSION.getMajor()
                , Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants.VERSION.getBuild() + 6);
        LauncherVersion actualBuild = new LauncherVersion(Constants.VERSION.getReserved(), Constants.VERSION.getMajor
                (), Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), 0);
        assertTrue(testBuild.needsUpdate(actualBuild));

        // Test newer Reserved - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved() - 1, Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants.VERSION
                .getBuild())));

        // Test newer Major - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor() - 1, Constants.VERSION.getMinor(), Constants.VERSION.getRevision(), Constants
                .VERSION.getBuild())));

        // Test newer Minor - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor() - 1, Constants.VERSION.getRevision(), Constants
                .VERSION.getBuild())));

        // Test newer Revision - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION.getReserved(), Constants
                .VERSION.getMajor(), Constants.VERSION.getMinor(), Constants.VERSION.getRevision() - 1, Constants
                .VERSION.getBuild())));
    }

}
