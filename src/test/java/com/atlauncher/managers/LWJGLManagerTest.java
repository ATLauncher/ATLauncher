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
package com.atlauncher.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.data.LWJGLLibrary;
import com.atlauncher.data.LWJGLMajorVersion;
import com.atlauncher.data.LWJGLVersion;
import com.atlauncher.data.LWJGLVersions;
import com.atlauncher.data.Settings;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.utils.OS;

public class LWJGLManagerTest {
    private final LWJGLVersions LWJGL_VERSIONS = new LWJGLVersions();

    private LWJGLMajorVersion lwjgl2MajorVersion;
    private LWJGLVersion lwjgl2Version;

    private LWJGLMajorVersion lwjgl3MajorVersion;
    private LWJGLVersion lwjgl3Version;

    private Library lwjgl2Library;
    private Library lwjgl3Library;
    private Library lwjgl3NativesLibrary;
    private Library lwjgl3Pre119NativesLibrary;
    private Library lwjgl3OSXArmLibrary;
    private Library nonLwjglLibrary;

    @BeforeEach
    public void initialize() {
        lwjgl2Version = new LWJGLVersion();
        lwjgl2Version.version = "2";
        lwjgl2Version.libraries = new HashMap<>();

        lwjgl2MajorVersion = new LWJGLMajorVersion();
        lwjgl2MajorVersion.version = 2;
        lwjgl2MajorVersion.versions = Arrays.asList(lwjgl2Version);

        LWJGLLibrary linuxArm32Library = new LWJGLLibrary();
        linuxArm32Library.name = "org.lwjgl:lwjgl:3.3.1:natives-linux-arm32";
        linuxArm32Library.path = "org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-linux-arm32.jar";
        linuxArm32Library.url = "https://example.com/maven/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-linux-arm32.jar";
        linuxArm32Library.size = 82374;
        linuxArm32Library.sha1 = "41a3c1dd15d6b964eb8196dde69720a3e3e5e969";

        LWJGLLibrary starLwjglLibrary = new LWJGLLibrary();
        starLwjglLibrary.name = "org.lwjgl:lwjgl:3.3.1";
        starLwjglLibrary.path = "org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1.jar";
        starLwjglLibrary.url = "https://example.com/maven/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1.jar";
        starLwjglLibrary.size = 724243;
        starLwjglLibrary.sha1 = "ae58664f88e18a9bb2c77b063833ca7aaec484cb";

        Map<String, LWJGLLibrary> lwjglVersionLibrary = new HashMap<>();
        lwjglVersionLibrary.put("linux-arm32", linuxArm32Library);
        lwjglVersionLibrary.put("*", starLwjglLibrary);

        lwjgl3Version = new LWJGLVersion();
        lwjgl3Version.version = "3.3.1";
        lwjgl3Version.libraries = new HashMap<>();
        lwjgl3Version.libraries.put("lwjgl", lwjglVersionLibrary);

        lwjgl3MajorVersion = new LWJGLMajorVersion();
        lwjgl3MajorVersion.version = 3;
        lwjgl3MajorVersion.versions = Arrays.asList(lwjgl3Version);

        LWJGL_VERSIONS.legacyLwjglVersions = Arrays.asList("1.12.2");
        LWJGL_VERSIONS.versions = Arrays.asList(lwjgl2MajorVersion, lwjgl3MajorVersion);

        lwjgl2Library = new Library();
        lwjgl2Library.name = "org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209";

        lwjgl3Library = new Library();
        lwjgl3Library.name = "org.lwjgl:lwjgl:3.3.1";
        lwjgl3Library.downloads = new Downloads();
        lwjgl3Library.downloads.artifact = new Download();
        lwjgl3Library.downloads.artifact.sha1 = "ae58664f88e18a9bb2c77b063833ca7aaec484cb";

        lwjgl3NativesLibrary = new Library();
        lwjgl3NativesLibrary.name = "org.lwjgl:lwjgl:3.3.1:natives-linux";
        lwjgl3NativesLibrary.downloads = new Downloads();
        lwjgl3NativesLibrary.downloads.artifact = new Download();
        lwjgl3NativesLibrary.downloads.artifact.sha1 = "41a3c1dd15d6b964eb8196dde69720a3e3e5e969";

        lwjgl3Pre119NativesLibrary = new Library();
        lwjgl3Pre119NativesLibrary.name = "org.lwjgl:lwjgl:3.3.1";
        lwjgl3Pre119NativesLibrary.natives = new HashMap<>();
        lwjgl3Pre119NativesLibrary.natives.put("linux", "natives-linux");
        lwjgl3Pre119NativesLibrary.downloads = new Downloads();
        lwjgl3Pre119NativesLibrary.downloads.artifact = new Download();
        lwjgl3Pre119NativesLibrary.downloads.artifact.sha1 = "ae58664f88e18a9bb2c77b063833ca7aaec484cb";
        Download linuxNativesDownload = new Download();
        linuxNativesDownload.path = "org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-linux.jar";
        linuxNativesDownload.sha1 = "05359f3aa50d36352815fc662ea73e1c00d22170";
        lwjgl3Pre119NativesLibrary.downloads.classifiers = new HashMap<>();
        lwjgl3Pre119NativesLibrary.downloads.classifiers.put("natives-linux", linuxNativesDownload);

        lwjgl3OSXArmLibrary = new Library();
        lwjgl3OSXArmLibrary.name = "org.lwjgl:lwjgl:3.3.1:natives-macos-arm64";

        nonLwjglLibrary = new Library();
        nonLwjglLibrary.name = "com.github.oshi:oshi-core:5.8.5";

        Data.LWJGL_VERSIONS = LWJGL_VERSIONS;

        App.settings = new Settings();
        App.settings.enableArmSupport = true;

        Data.CONFIG = new HashMap<>();
        Data.CONFIG.put("useLwjglReplacement", true);
    }

    @Test
    public void testUsesLegacyLWJGLAsTrue() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";

        assertTrue(LWJGLManager.usesLegacyLWJGL(minecraftVersion));
    }

    @Test
    public void testUsesLegacyLWJGLAsFalse() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";

        assertFalse(LWJGLManager.usesLegacyLWJGL(minecraftVersion));
    }

    @Test
    public void testShouldUseLegacyLWJGLAsTrue() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(true);
            assertTrue(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testShouldUseLegacyLWJGLAsFalseWhenSettingDisabled() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";

        App.settings.enableArmSupport = false;

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(true);
            assertFalse(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testShouldUseLegacyLWJGLAsFalseWhenConfigToggledOff() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";

        Data.CONFIG.put("useLwjglReplacement", false);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(true);
            assertFalse(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testShouldReplaceLWJGL3AsTrueWhenNoMacOSArmLibrary() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isMacArm).thenReturn(false);
            assertTrue(LWJGLManager.shouldReplaceLWJGL3(minecraftVersion));
        }
    }

    @Test
    public void testShouldReplaceLWJGL3AsTrueWhenNotMacOSArm() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary, lwjgl3OSXArmLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isMacArm).thenReturn(false);
            assertTrue(LWJGLManager.shouldReplaceLWJGL3(minecraftVersion));
        }
    }

    @Test
    public void testShouldReplaceLWJGL3AsFalseWhenMacOSArm() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary, lwjgl3OSXArmLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isMacArm).thenReturn(true);
            assertFalse(LWJGLManager.shouldReplaceLWJGL3(minecraftVersion));
        }
    }

    @Test
    public void testShouldReplaceLWJGL3AsFalseWhenLegacyLWJGL() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";
        minecraftVersion.libraries = Arrays.asList(lwjgl2Library);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isMacArm).thenReturn(false);
            assertFalse(LWJGLManager.shouldReplaceLWJGL3(minecraftVersion));
        }
    }

    @Test
    public void testShouldReplaceLWJGL3AsFalseWhenNotArm() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary, lwjgl3OSXArmLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(false);
            utilities.when(OS::isMacArm).thenReturn(false);
            assertFalse(LWJGLManager.shouldReplaceLWJGL3(minecraftVersion));
        }
    }

    @Test
    public void testShouldUseLegacyLWJGLAsTrueWhenLegacyLWJGL() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";
        minecraftVersion.libraries = Arrays.asList(lwjgl2Library);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(true);
            assertTrue(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testShouldUseLegacyLWJGLAsFalseWhenLegacyLWJGLButNotLinux() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.12.2";
        minecraftVersion.libraries = Arrays.asList(lwjgl2Library);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(false);
            assertFalse(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testShouldUseLegacyLWJGLAsFalseWhenNotLegacyLWJGL() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary, lwjgl3OSXArmLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::isLinux).thenReturn(true);
            assertFalse(LWJGLManager.shouldUseLegacyLWJGL(minecraftVersion));
        }
    }

    @Test
    public void testGetLegacyLWJGLLibraryReturnsCorrectlyForLinux64BitArm() {
        LWJGLLibrary arm32Library = new LWJGLLibrary();
        LWJGLLibrary arm64Library = new LWJGLLibrary();
        Map<String, LWJGLLibrary> lwjglLibrary = new HashMap<>();
        lwjglLibrary.put("linux-arm32", arm32Library);
        lwjglLibrary.put("linux-arm64", arm64Library);
        lwjgl2Version.libraries.put("lwjgl", lwjglLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(true);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(arm64Library, LWJGLManager.getLegacyLWJGLLibrary());
        }
    }

    @Test
    public void testGetLegacyLWJGLLibraryReturnsCorrectlyForLinux32BitArm() {
        LWJGLLibrary arm32Library = new LWJGLLibrary();
        LWJGLLibrary arm64Library = new LWJGLLibrary();
        Map<String, LWJGLLibrary> lwjglLibrary = new HashMap<>();
        lwjglLibrary.put("linux-arm32", arm32Library);
        lwjglLibrary.put("linux-arm64", arm64Library);
        lwjgl2Version.libraries.put("lwjgl", lwjglLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(arm32Library, LWJGLManager.getLegacyLWJGLLibrary());
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsOriginalLibraryWhenLibraryIsntLWJGL() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(nonLwjglLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(nonLwjglLibrary,
                    LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, nonLwjglLibrary));
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsOriginalLibraryWhenLibraryIsntLWJGL3() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl2Library);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(lwjgl2Library, LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, lwjgl2Library));
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsOriginalLibraryWhenLibrarySha1IsTheSame() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3Library);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(lwjgl3Library, LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, lwjgl3Library));
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsOriginalLibraryWhenNativesLibrarySha1IsTheSame() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary);

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();
            assertEquals(lwjgl3NativesLibrary,
                    LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, lwjgl3NativesLibrary));
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsNewLibraryWhenShaDoesntMatch() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3Library);

        lwjgl3Library.downloads.artifact.sha1 = "fff8664f88e18a9bb2c77b063833ca7aaec484cb";

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();

            Library mappedLibrary = LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, lwjgl3Library);
            assertNotEquals(lwjgl3Library, mappedLibrary);
            assertEquals(lwjgl3Library.name, mappedLibrary.name);
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsNewNativesLibraryWhenShaDoesntMatch() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.19.1";
        minecraftVersion.libraries = Arrays.asList(lwjgl3NativesLibrary);

        lwjgl3NativesLibrary.downloads.artifact.sha1 = "fff8664f88e18a9bb2c77b063833ca7aaec484cb";

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();

            Library mappedLibrary = LWJGLManager.getReplacementLWJGL3Library(minecraftVersion, lwjgl3NativesLibrary);
            assertNotEquals(lwjgl3NativesLibrary, mappedLibrary);
            assertNotEquals(lwjgl3NativesLibrary.name, mappedLibrary.name);
            assertEquals("org.lwjgl:lwjgl:3.3.1:natives-linux-arm32", mappedLibrary.name);
        }
    }

    @Test
    public void testGetReplacementLWJGL3LibraryReturnsNewPre119NativesLibraryWhenShaDoesntMatch() {
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        minecraftVersion.id = "1.18.2";
        minecraftVersion.libraries = Arrays.asList(lwjgl3Pre119NativesLibrary);

        lwjgl3Pre119NativesLibrary.downloads.artifact.sha1 = "fff8664f88e18a9bb2c77b063833ca7aaec484cb";

        try (MockedStatic<OS> utilities = mockStatic(OS.class)) {
            utilities.when(OS::isWindows).thenReturn(false);
            utilities.when(OS::isMac).thenReturn(false);
            utilities.when(OS::isLinux).thenReturn(true);
            utilities.when(OS::isArm).thenReturn(true);
            utilities.when(OS::is64Bit).thenReturn(false);
            utilities.when(OS::getNativesArch).thenReturn("32");
            utilities.when(OS::getLWJGLClassifier).thenCallRealMethod();

            Library mappedLibrary = LWJGLManager.getReplacementLWJGL3Library(minecraftVersion,
                    lwjgl3Pre119NativesLibrary);
            assertNotEquals(lwjgl3Pre119NativesLibrary, mappedLibrary);
            assertEquals(lwjgl3Pre119NativesLibrary.name, mappedLibrary.name);
            assertNotEquals(lwjgl3Pre119NativesLibrary.downloads.classifiers.get("natives-linux").sha1,
                    mappedLibrary.downloads.classifiers.get("natives-linux").sha1);
            assertEquals("org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1-natives-linux-arm32.jar",
                    mappedLibrary.getNativeDownloadForOS().path);
        }
    }
}
