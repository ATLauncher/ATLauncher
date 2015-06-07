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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HashingTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path testStorage;

    @Before
    public void setUp() throws Exception {
        testStorage = temporaryFolder.newFolder("ATLauncherTests").toPath();
    }

    @Test
    public void testObjectHashing() {
        Path testFile = this.testStorage.resolve("TestMD5.txt");

        byte[] bytes = {'T', 'e', 's', 't'};

        try {
            Files.write(testFile, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Hashing.HashCode code = Hashing.md5(testFile);
        Hashing.HashCode code1 = Hashing.md5(testFile);

        Assert.assertEquals(code, code1);
        Assert.assertTrue(code.equals(code1));
    }

    @Test
    public void testComparison() {
        Hashing.HashCode code = new Hashing.HashCode("0cbc6611f5540bd0809a388dc95a615b");
        Hashing.HashCode code1 = Hashing.HashCode.fromString("0cbc6611f5540bd0809a388dc95a615b");

        Assert.assertEquals(code, code1);
        Assert.assertEquals(code.intern(), code1);
        Assert.assertTrue(code.equals(code1));
    }

    @Test
    public void testMD5() {
        Path testFile = this.testStorage.resolve("TestMD5.txt");

        byte[] bytes = {'T', 'e', 's', 't'};

        try {
            Files.write(testFile, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals("0cbc6611f5540bd0809a388dc95a615b", Hashing.md5("Test").toString());

        Assert.assertTrue(Files.exists(testFile));
        Assert.assertTrue(Files.isRegularFile(testFile));

        Assert.assertEquals("0cbc6611f5540bd0809a388dc95a615b", Hashing.md5(testFile).toString());

        byte[] bytes2 = {'T', 'e', 's', 't', '2'};

        try {
            Files.delete(testFile);

            Assert.assertFalse(Files.exists(testFile));

            Files.write(testFile, bytes2, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals("c454552d52d55d3ef56408742887362b", Hashing.md5("Test2").toString());

        Assert.assertTrue(Files.exists(testFile));
        Assert.assertTrue(Files.isRegularFile(testFile));

        Assert.assertEquals("c454552d52d55d3ef56408742887362b", Hashing.md5(testFile).toString());
    }

    @Test
    public void testSHA1() {
        Path testFile = this.testStorage.resolve("TestSHA1.txt");

        byte[] bytes = {'T', 'e', 's', 't'};

        try {
            Files.write(testFile, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals("640ab2bae07bedc4c163f679a746f7ab7fb5d1fa", Hashing.sha1("Test").toString());

        Assert.assertTrue(Files.exists(testFile));
        Assert.assertTrue(Files.isRegularFile(testFile));

        Assert.assertEquals("640ab2bae07bedc4c163f679a746f7ab7fb5d1fa", Hashing.sha1(testFile).toString());

        byte[] bytes2 = {'T', 'e', 's', 't', '2'};

        try {
            Files.delete(testFile);

            Assert.assertFalse(Files.exists(testFile));

            Files.write(testFile, bytes2, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals("2b84f621c0fd4ba8bd514c5c43ab9a897c8c014e", Hashing.sha1("Test2").toString());

        Assert.assertTrue(Files.exists(testFile));
        Assert.assertTrue(Files.isRegularFile(testFile));

        Assert.assertEquals("2b84f621c0fd4ba8bd514c5c43ab9a897c8c014e", Hashing.sha1(testFile).toString());
    }
}
