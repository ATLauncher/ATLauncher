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
package ui;

import java.awt.Frame;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

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
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.tls.KeyStoreFactory;

import ui.mocks.MockHelper;

public class AbstractUiTest extends AssertJSwingTestCaseTemplate {
    // get a working directory specifically for this run
    protected static final Path workingDir = Paths.get("testLauncher/ui-tests/" + UUID.randomUUID()).toAbsolutePath();
    protected ClientAndServer mockServer;

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

        HttpsURLConnection.setDefaultSSLSocketFactory(
                new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());

        mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());

        if (System.getenv("CI") == null || !System.getenv("CI").equalsIgnoreCase("true")) {
            mockServer.openUI();
        }

        this.setUpRobot();

        this.setupHttpMocks();

        // start the application
        ApplicationLauncher.application(App.class)
                .withArgs("--skip-setup-dialog", "--disable-analytics", "--disable-error-reporting",
                        "--skip-tray-integration", "--no-launcher-update", "--proxy-type=SOCKS",
                        "--proxy-host=127.0.0.1", "--proxy-port=" + mockServer.getPort(),
                        "--working-dir=" + workingDir.toString())
                .start();

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

    private void setupHttpMocks() {
        // files.json
        MockHelper.mockFilesJson(mockServer);

        // files to download
        MockHelper.mockFileResponse(mockServer, "newnews.json");
        MockHelper.mockFileResponse(mockServer, "runtimes.json");
        MockHelper.mockFileResponse(mockServer, "users.json");
        MockHelper.mockFileResponse(mockServer, "minecraft.json");
        MockHelper.mockFileResponse(mockServer, "packsnew.json");
        MockHelper.mockFileResponse(mockServer, "version.json");
    }

    protected void onSetUp() {
    }

    @After
    public final void tearDown() {
        try {
            onTearDown();
            mockServer.stop(true);
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
