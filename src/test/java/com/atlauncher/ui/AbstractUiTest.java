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
package com.atlauncher.ui;

import java.awt.Frame;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.utils.FileUtils;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractUiTest extends AssertJSwingTestCaseTemplate {
    // get a working directory specifically for this run
    protected static final Path workingDir = Paths.get("testLauncher/ui-tests/" + UUID.randomUUID()).toAbsolutePath();

    protected FrameFixture frame;

    protected FrameFixture consoleFrame;

    @BeforeClass
    public static final void setUpOnce() {
        FileUtils.createDirectory(workingDir);
    }

    protected void preSetUp() {
    }

    @Before
    public final void setUp() {
        preSetUp();

        this.setUpRobot();

        // start the application
        ApplicationLauncher.application(App.class).withArgs("--skip-setup-dialog", "--skip-integration",
                "--skip-tray-integration", "--no-launcher-update", "--working-dir=" + workingDir.toString()).start();

        // get a reference to the main launcher frame
        frame = WindowFinder.findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return Constants.LAUNCHER_NAME.equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());

        // get a reference to the console frame
        consoleFrame = WindowFinder.findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return (Constants.LAUNCHER_NAME + " Console").equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());

        onSetUp();
    }

    protected void onSetUp() {
    }

    @After
    public final void tearDown() {
        try {
            onTearDown();
        } finally {
            cleanUp();
        }
    }

    @AfterClass
    public static final void tearDownOnce() {
        FileUtils.deleteDirectory(workingDir);
    }

    protected void onTearDown() {
    }
}
