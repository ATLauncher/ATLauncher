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

import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.DeleteDirVisitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestMoveFile {
    private final Path testStorage = Utils.getCoreGracefully().resolve("Tests");

    @Before
    public void setUp() throws Exception {
        this.testStorage.toFile().mkdir();
        Assert.assertTrue(Files.exists(this.testStorage));
        Assert.assertTrue(Files.isDirectory(this.testStorage));
    }

    @After
    public void tearDown() throws Exception {
        try {
            Files.walkFileTree(this.testStorage, new DeleteDirVisitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        Path testPath;
        Path movedTo;

        // Testing moving a file to a different name
        testPath = this.testStorage.resolve("SingleTest1.txt");
        movedTo = this.testStorage.resolve("SingleTestMoved.txt");

        try {
            Files.createFile(testPath);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));

        FileUtils.moveFile(testPath, movedTo, true);

        Assert.assertTrue(!Files.exists(testPath));
        Assert.assertTrue(Files.exists(movedTo));
        Assert.assertTrue(Files.isRegularFile(movedTo));

        // Testing moving a file to a folder that exist's
        testPath = this.testStorage.resolve("SingleTest2.txt");
        movedTo = this.testStorage.resolve("SingleTest2");

        try {
            Files.createFile(testPath);
            Files.createDirectory(movedTo);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));

        FileUtils.moveFile(testPath, movedTo);

        Assert.assertTrue(!Files.exists(testPath));
        Assert.assertTrue(Files.exists(movedTo));
        Assert.assertTrue(Files.isDirectory(movedTo));
        Assert.assertTrue(Files.exists(movedTo.resolve("SingleTest2.txt")));
        Assert.assertTrue(Files.isRegularFile(movedTo.resolve("SingleTest2.txt")));

        // Testing moving a file to a folder that doesn't exist
        testPath = this.testStorage.resolve("SingleTest3.txt");
        movedTo = this.testStorage.resolve("SingleTest3");

        try {
            Files.createFile(testPath);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));

        FileUtils.moveFile(testPath, movedTo);

        Assert.assertTrue(!Files.exists(testPath));
        Assert.assertTrue(Files.exists(movedTo));
        Assert.assertTrue(Files.isDirectory(movedTo));
        Assert.assertTrue(Files.exists(movedTo.resolve("SingleTest3.txt")));
        Assert.assertTrue(Files.isRegularFile(movedTo.resolve("SingleTest3.txt")));

        // Testing moving a file to the uppercase version of itself
        testPath = this.testStorage.resolve("uppercase-test.txt");
        movedTo = this.testStorage.resolve("UPPERCASE-TEST.txt");

        try {
            Files.createFile(testPath);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));

        FileUtils.moveFile(testPath, movedTo, true);

        Assert.assertTrue(!Files.exists(this.testStorage.resolve("uppercase-test.txt.bak")));

        try {
            Assert.assertEquals("UPPERCASE-TEST.txt", testPath.toRealPath().getFileName().toString());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.isRegularFile(movedTo));
    }
}
