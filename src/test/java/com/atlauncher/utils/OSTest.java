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

import java.util.ArrayList;
import java.util.List;

import com.atlauncher.utils.javafinder.JavaInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OSTest {
    private List<JavaInfo> installedJavas;

    @BeforeEach
    public void initialize() {
        installedJavas = new ArrayList<>();
    }

    @Test
    public void testThatJava864BitIsPreferredOverJava832Bit() {
        // Test preferencing Java 8 64 bit over Java 8 32 bit
        installedJavas.add(new JavaInfo("C:/Java/8.111/64bit/bin/java.exe", "C:/Java/8/64bit", "8.111", 8, 111, true));
        installedJavas.add(new JavaInfo("C:/Java/8.111/32bit/bin/java.exe", "C:/Java/8/32bit", "8.111", 8, 111, false));
        assertEquals("C:/Java/8/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatJavaVersionsWithSameMajorPreferHigherMinor() {
        // Test preferencing the highest minor version when all the same major versions
        installedJavas
                .add(new JavaInfo("C:/Java/8.108/64bit/bin/java.exe", "C:/Java/8.108/64bit", "8.108", 8, 108, true));
        installedJavas
                .add(new JavaInfo("C:/Java/8.105/64bit/bin/java.exe", "C:/Java/8.105/64bit", "8.105", 8, 105, true));
        installedJavas
                .add(new JavaInfo("C:/Java/8.111/64bit/bin/java.exe", "C:/Java/8.111/64bit", "8.111", 8, 111, true));
        installedJavas
                .add(new JavaInfo("C:/Java/8.102/64bit/bin/java.exe", "C:/Java/8.102/64bit", "8.102", 8, 102, true));
        installedJavas
                .add(new JavaInfo("C:/Java/8.100/64bit/bin/java.exe", "C:/Java/8.100/64bit", "8.100", 8, 100, true));
        assertEquals("C:/Java/8.111/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatDownloadedRuntimeJavaIsPreferredOverAllOthers() {
        // Test preferencing the downloaded runtime Java is preferred over all others
        installedJavas.add(
                new JavaInfo("C:/Java/8.111/32bit/bin/java.exe", "C:/Java/8.111/64bit", "8.111", 8, 111, true, false));
        installedJavas.add(new JavaInfo("C:/ATLauncher/runtimes/8.15/64bit/bin/java.exe",
                "C:/ATLauncher/runtimes/8.15/64bit", "8.15", 8, 15, true, true));
        assertEquals("C:/ATLauncher/runtimes/8.15/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatJava764BitIsPreferredOverJava832Bit() {
        // Test preferencing Java 7 64 bit over Java 8 32 bit
        installedJavas.add(new JavaInfo("C:/Java/7.111/64bit/bin/java.exe", "C:/Java/7/64bit", "7.111", 7, 111, true));
        installedJavas.add(new JavaInfo("C:/Java/8.111/32bit/bin/java.exe", "C:/Java/8/32bit", "8.111", 8, 111, false));
        assertEquals("C:/Java/7/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatJava964BitIsPreferredOverJava732Bit() {
        // Test preferencing Java 9 64 bit over Java 7 64 bit
        installedJavas.add(new JavaInfo("C:/Java/9.111/64bit/bin/java.exe", "C:/Java/9/64bit", "9.111", 9, 111, true));
        installedJavas.add(new JavaInfo("C:/Java/7.111/64bit/bin/java.exe", "C:/Java/7/64bit", "7.111", 7, 111, true));
        assertEquals("C:/Java/9/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatJava864BitIsPreferredOverJava964Bit() {
        // Test preferencing Java 8 64 bit over Java 9 64 bit
        installedJavas.add(new JavaInfo("C:/Java/8.111/64bit/bin/java.exe", "C:/Java/8/64bit", "8.111", 8, 111, true));
        installedJavas.add(new JavaInfo("C:/Java/9.111/64bit/bin/java.exe", "C:/Java/9/64bit", "9.111", 9, 111, true));
        assertEquals("C:/Java/8/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatWhenOnlyASingleJavaIsInstalledItIsReturned() {
        // Test preferencing Java 7 32 bit when it's the only option
        installedJavas.add(new JavaInfo("C:/Java/7.111/64bit/bin/java.exe", "C:/Java/7/64bit", "7.111", 7, 111, true));
        assertEquals("C:/Java/7/64bit", OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatWhenNoJavaIsDetectedOnTheSystemItReturnsNull() {
        // Test null being returned when no installed java paths detected
        assertEquals(null, OS.getPreferredJavaPath(installedJavas));
    }

    @Test
    public void testThatWhenAnInvalidJavaVersionIsFoundItReturnsNull() {
        // Test an invalid version found returning null
        installedJavas
                .add(new JavaInfo("C:/Java/8.111/64bit/bin/java.exe", "C:/Java/8/64bit", "Unknown", null, null, false));
        installedJavas.add(new JavaInfo("C:/Java/8.111/64bit/bin/java.exe", "C:/Java/8/64bit", "Unknown", false));
        assertEquals(null, OS.getPreferredJavaPath(installedJavas));
    }

}
