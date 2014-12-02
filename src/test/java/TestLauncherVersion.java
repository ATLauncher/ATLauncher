/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 * 
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.atlauncher.data.Constants;
import com.atlauncher.data.LauncherVersion;

public class TestLauncherVersion {

    @Test
    public void test() {
        // Test same version - no update
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test older Reserved - launcher had a big update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved() + 1, Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test older Major - launcher had major update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor() + 1, Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test older Minor - launcher had minor update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor() + 1,
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test older Revision - launcher had a bug fix
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision() + 1, Constants.VERSION.getBuild())));

        // Test older Build - launcher had a beta update
        assertTrue(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild() + 1)));

        // Test user has a beta build but the real build comes out
        LauncherVersion testBuild = new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild() + 6);
        LauncherVersion actualBuild = new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), 0);
        assertTrue(testBuild.needsUpdate(actualBuild));

        // Test newer Reserved - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved() - 1, Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test newer Major - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor() - 1, Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test newer Minor - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor() - 1,
                Constants.VERSION.getRevision(), Constants.VERSION.getBuild())));

        // Test newer Revision - launcher dev version
        assertFalse(Constants.VERSION.needsUpdate(new LauncherVersion(Constants.VERSION
                .getReserved(), Constants.VERSION.getMajor(), Constants.VERSION.getMinor(),
                Constants.VERSION.getRevision() - 1, Constants.VERSION.getBuild())));
    }

}
