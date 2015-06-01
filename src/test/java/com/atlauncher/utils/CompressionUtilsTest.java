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

import com.atlauncher.OrderedRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RunWith(OrderedRunner.class)
public class CompressionUtilsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path testStorage;

    @Before
    public void setup() throws IOException {
        testStorage = temporaryFolder.newFolder("ATLauncherTests").toPath();
    }

    @Test
    public void testZip() {
        Path testFolder = this.testStorage.resolve("TestZip");
        Path testFile = testFolder.resolve("Test.txt");
        Path outputZip = testFolder.resolve("Test.zip");

        try {
            Files.createDirectory(testFolder);
            Files.createFile(testFile);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            CompressionUtils.zip(outputZip, testFolder);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(outputZip));
        Assert.assertTrue(Files.isRegularFile(outputZip));

        Assert.assertTrue(ZipUtil.containsEntry(outputZip.toFile(), "Test.txt"));
    }

    @Test
    public void testUnzip() {
        Path testFolder = this.testStorage.resolve("TestUnzip");
        Path testFolderOut = this.testStorage.resolve("TestUnzipOut");
        Path testFile = testFolder.resolve("Test.txt");
        Path testFileOut = testFolderOut.resolve("Test.txt");
        Path outputZip = testFolder.resolve("Test.zip");

        byte[] bytes = {'T', 'e', 's', 't'};

        try {
            Files.createDirectory(testFolder);
            Files.write(testFile, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        ZipUtil.pack(testFolder.toFile(), outputZip.toFile());

        Assert.assertTrue(ZipUtil.containsEntry(outputZip.toFile(), "Test.txt"));

        Assert.assertTrue(Files.exists(outputZip));
        Assert.assertTrue(Files.isRegularFile(outputZip));

        try {
            CompressionUtils.unzip(outputZip, testFolderOut);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertTrue(Files.exists(testFolderOut));
        Assert.assertTrue(Files.isDirectory(testFolderOut));

        Assert.assertTrue(Files.exists(testFileOut));
        Assert.assertTrue(Files.isRegularFile(testFileOut));

        try {
            Assert.assertEquals("Test", new String(Files.readAllBytes(testFileOut)));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}