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
package com.atlauncher.utils;

import com.atlauncher.utils.walker.DeleteDirVisitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtilsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path testStorage;

    @Before
    public void setUp() throws Exception {
        testStorage = temporaryFolder.newFolder("ATLauncherTests").toPath();
    }

    @Test
    public void testMoveFile() {
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

    @Test
    public void testCreateDirectory() {
        Path testPath;

        // Testing making a single folder
        testPath = this.testStorage.resolve("SingleTest");
        Assert.assertTrue(FileUtils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));

        // Testing making a 2 folders folder
        testPath = this.testStorage.resolve("DoubleTest").resolve("Another");
        Assert.assertTrue(FileUtils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.getParent()));
        Assert.assertTrue(Files.isDirectory(testPath.getParent()));

        // Testing making a 3 folders folder
        testPath = this.testStorage.resolve("TripleTest").resolve("Another").resolve("Another");
        Assert.assertTrue(FileUtils.createDirectory(testPath));
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

        Assert.assertTrue(FileUtils.createDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
    }

    @Test
    public void testDelete() {
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
        Assert.assertTrue(FileUtils.delete(testPath));
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
        Assert.assertTrue(FileUtils.delete(testPath));
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
        Assert.assertTrue(FileUtils.deleteSpecifiedFiles(testPath, deleteThese));
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertFalse(Files.exists(testPath.resolve("Test1.txt")));
        Assert.assertTrue(Files.exists(testPath.resolve("Test2.txt")));
    }
}
