/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

public class UtilsTest {

    @Test
    public void testThatConvertingMavenIdentifierToPathWorksCorrectly() {
        Pair<String, String> pair = Utils
                .convertMavenIdentifierToNameAndVersion("org.lwjgl.lwjgl:lwjgl:2.9.1-nightly-20130708-debug3");
        assertEquals("org.lwjgl.lwjgl:lwjgl", pair.left());
        assertEquals("2.9.1-nightly-20130708-debug3", pair.right());

        pair = Utils
                .convertMavenIdentifierToNameAndVersion("org.lwjgl.lwjgl:lwjgl:2.9.1");
        assertEquals("org.lwjgl.lwjgl:lwjgl", pair.left());
        assertEquals("2.9.1", pair.right());

        pair = Utils
                .convertMavenIdentifierToNameAndVersion("net.neoforged.fancymodloader:earlydisplay:4.0.6@jar");
        assertEquals("net.neoforged.fancymodloader:earlydisplay", pair.left());
        assertEquals("4.0.6", pair.right());

        pair = Utils
                .convertMavenIdentifierToNameAndVersion("org.lwjgl:lwjgl-freetype:3.3.3:natives-linux");
        assertEquals("org.lwjgl:lwjgl-freetype:natives-linux", pair.left());
        assertEquals("3.3.3", pair.right());
    }

    @Test
    public void testThatConvertingMavenIdentifierOnInvalidIdentifierWorksCorrectly() {
        Pair<String, String> pair = Utils
                .convertMavenIdentifierToNameAndVersion("launchwrapper-1.12.jar");
        assertEquals("launchwrapper-1.12.jar", pair.left());
        assertEquals("0", pair.right());

        pair = Utils
                .convertMavenIdentifierToNameAndVersion("com.mojang:1.3.5");
        assertEquals("com.mojang:1.3.5", pair.left());
        assertEquals("0", pair.right());
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionsAreEqual() {
        assertEquals(0, Utils.compareVersions("1.7.0", "1.7.0"));
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionIsOlder() {
        assertEquals(-1, Utils.compareVersions("1.7.0", "2.0.0"));
        assertEquals(-1, Utils.compareVersions("1.7.0", "1.8.0"));
        assertEquals(-1, Utils.compareVersions("1.7.0", "1.7.1"));
        assertEquals(-1, Utils.compareVersions("1.7.0", "1.8"));
        assertEquals(-1, Utils.compareVersions("1", "1.7.0"));
        assertEquals(-1, Utils.compareVersions("2.1.2", "3"));
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionIsNewer() {
        assertEquals(1, Utils.compareVersions("1.8.0", "1.7.0"));
        assertEquals(1, Utils.compareVersions("1.8.1", "1.8.0"));
        assertEquals(1, Utils.compareVersions("2.8.0", "1.8.0"));
        assertEquals(1, Utils.compareVersions("1.8.1", "1.8"));
        assertEquals(1, Utils.compareVersions("2", "1.7.0"));
        assertEquals(1, Utils.compareVersions("2.1.2", "2"));
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionIsEqualButHasMoreParts() {
        assertEquals(0, Utils.compareVersions("2.9.1-nightly-20130708-debug3", "2.9.1-nightly-20130708-debug3"));
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionIsOlderButHasMoreParts() {
        assertEquals(-1, Utils.compareVersions("2.9.0-nightly-20130708-debug3", "2.9.1-nightly-20130708-debug3"));
        assertEquals(-1, Utils.compareVersions("2.9.0-nightly-20130708-debug3", "2.10.0-nightly-20130708-debug3"));
        assertEquals(-1, Utils.compareVersions("2.9.0-nightly-20130708-debug3", "3.9.0-nightly-20130708-debug3"));
    }

    @Test
    public void testThatComparingVersionsWorksCorrectlyWhenVersionIsNewerButHasMoreParts() {
        assertEquals(1, Utils.compareVersions("2.9.2-nightly-20130708-debug3", "2.9.1-nightly-20130708-debug3"));
        assertEquals(1, Utils.compareVersions("2.9.2-nightly-20130708-debug3", "2.8.2-nightly-20130708-debug3"));
        assertEquals(1, Utils.compareVersions("2.9.2-nightly-20130708-debug3", "1.9.2-nightly-20130708-debug3"));
    }

    @Test
    public void testThatComparingVersionsReturnsEqualWhenAnExceptionIsThrown() {
        assertEquals(0, Utils.compareVersions("1.7.0", "asdasdasds"));
        assertEquals(0, Utils.compareVersions("asdasdasds", "1.7.0"));
    }

    @Test
    public void testGetSafeIntegerFromString() {
        // Test null input
        assertEquals(null, Utils.getSafeIntegerFromString(null));

        // Test valid integer string
        assertEquals(Integer.valueOf(123), Utils.getSafeIntegerFromString("123"));
        assertEquals(Integer.valueOf(-456), Utils.getSafeIntegerFromString("-456"));
        assertEquals(Integer.valueOf(0), Utils.getSafeIntegerFromString("0"));

        // Test invalid integer string
        assertEquals(null, Utils.getSafeIntegerFromString("abc"));
        assertEquals(null, Utils.getSafeIntegerFromString("12.34"));
        assertEquals(null, Utils.getSafeIntegerFromString("124hh"));
        assertEquals(null, Utils.getSafeIntegerFromString("124hh"));
    }
}
