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
import java.util.ArrayList;
import java.util.List;

public class TestDelete {
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

        // Testing deleting a single file
        testPath = this.testStorage.resolve("Test.txt");

        try {
            Files.createFile(testPath);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isRegularFile(testPath));
        Assert.assertTrue(Utils.delete(testPath));
        Assert.assertFalse(Files.exists(testPath));

        // Testing deleting a a directory
        testPath = this.testStorage.resolve("Test");
        Path file = testPath.resolve("Test.txt");

        try {
            Files.createDirectory(testPath);

            Files.createFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(file));
        Assert.assertTrue(Files.isRegularFile(file));
        Assert.assertTrue(Utils.delete(testPath));
        Assert.assertFalse(Files.exists(testPath));

        // Testing deleting a directory with a filter
        testPath = this.testStorage.resolve("Test");
        List<String> deleteThese = new ArrayList<>();
        deleteThese.add("Test1.txt");

        try {
            Files.createDirectory(testPath);

            Files.createFile(testPath.resolve("Test1.txt"));
            Files.createFile(testPath.resolve("Test2.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.resolve("Test1.txt")));
        Assert.assertTrue(Files.isRegularFile(testPath.resolve("Test1.txt")));
        Assert.assertTrue(Files.exists(testPath.resolve("Test2.txt")));
        Assert.assertTrue(Files.isRegularFile(testPath.resolve("Test2.txt")));
        Assert.assertTrue(Utils.deleteSpecifiedFiles(testPath, deleteThese));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertFalse(Files.exists(testPath.resolve("Test1.txt")));
        Assert.assertTrue(Files.exists(testPath.resolve("Test2.txt")));
    }
}
