/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JavaTest {
    @Test
    public void testThatJavaVersionNumberIsReturnedCorrectly() {
        assertEquals(7, Java.parseJavaVersionNumber("1.7.0_64"));
        assertEquals(8, Java.parseJavaVersionNumber("1.8.0_212"));
        assertEquals(9, Java.parseJavaVersionNumber("9.0.4"));
        assertEquals(10, Java.parseJavaVersionNumber("10.0.2"));
        assertEquals(11, Java.parseJavaVersionNumber("11.0.3"));
        assertEquals(12, Java.parseJavaVersionNumber("12.0.1"));
        assertEquals(13, Java.parseJavaVersionNumber("13-ea"));
    }

    @Test
    public void testThatJavaBuildNumberIsReturnedCorrectly() {
        assertEquals(64, Java.parseJavaBuildVersion("1.7.0_64"));
        assertEquals(212, Java.parseJavaBuildVersion("1.8.0_212"));
        assertEquals(4, Java.parseJavaBuildVersion("9.0.4"));
        assertEquals(2, Java.parseJavaBuildVersion("10.0.2"));
        assertEquals(3, Java.parseJavaBuildVersion("11.0.3"));
        assertEquals(1, Java.parseJavaBuildVersion("12.0.1"));
        assertEquals(0, Java.parseJavaBuildVersion("13-ea"));
    }

}
