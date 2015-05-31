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
import com.atlauncher.anno.ExecutionOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(OrderedRunner.class)
public class CompressionUtilsTest {
    private Path desktop;

    @Before
    public void setup() {
        this.desktop = Paths.get(System.getProperty("user.home"), "Desktop");
    }

    @Test
    @ExecutionOrder(2)
    public void testUnzip() throws Exception {
        Path zip = this.desktop.resolve("Test.zip");
        Path output = this.desktop.resolve("Test2");
        FileUtils.createDirectory(output);
        CompressionUtils.unzip(zip, output);
    }

    @Test
    @ExecutionOrder(1)
    public void testZip() throws Exception {
        Path dir = this.desktop.resolve("Test");
        Path output = this.desktop.resolve("Test.zip");
        CompressionUtils.zip(output, dir);
    }
}