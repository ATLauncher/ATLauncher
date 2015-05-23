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

import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.DeleteDirVisitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestCreateDirectory {
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

        // Testing making a single folder
        testPath = this.testStorage.resolve("SingleTest");
        Assert.assertTrue(Utils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));

        // Testing making a 2 folders folder
        testPath = this.testStorage.resolve("DoubleTest").resolve("Another");
        Assert.assertTrue(Utils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.getParent()));
        Assert.assertTrue(Files.isDirectory(testPath.getParent()));

        // Testing making a 3 folders folder
        testPath = this.testStorage.resolve("TripleTest").resolve("Another").resolve("Another");
        Assert.assertTrue(Utils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.getParent()));
        Assert.assertTrue(Files.isDirectory(testPath.getParent()));
        Assert.assertTrue(Files.exists(testPath.getParent().getParent()));
        Assert.assertTrue(Files.isDirectory(testPath.getParent().getParent()));

        // Testing making a single folder with a file of the same name already there
        testPath = this.testStorage.resolve("TestWithFile");

        try {
            Files.createFile(this.testStorage.resolve("TestWithFile"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Utils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
    }
}
