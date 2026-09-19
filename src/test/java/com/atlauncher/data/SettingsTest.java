/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2026 ATLauncher
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
package com.atlauncher.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.atlauncher.Gsons;
import com.atlauncher.utils.OS;

public class SettingsTest {
    private MockedStatic<OS> os;

    @BeforeEach
    public void setUp() {
        boolean isWindows = OS.isWindows();
        os = mockStatic(OS.class);
        os.when(OS::isWindows).thenReturn(isWindows);
        os.when(OS::getDefaultJavaPath).thenReturn(System.getProperty("java.home"));
        os.when(OS::getScreenVirtualBounds).thenReturn(new Rectangle(0, 0, 1920, 1080));
        os.when(OS::getMaximumRam).thenReturn(16384);
        os.when(OS::getMaximumWindowWidth).thenReturn(1920);
        os.when(OS::getMaximumWindowHeight).thenReturn(1080);
    }

    @AfterEach
    public void tearDown() {
        os.close();
    }

    @Test
    public void testSavedStrictRestrictionTakesPrecedenceOverLegacyFlag() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":true,\"addModRestriction\":\"STRICT\"}");

        assertEquals(AddModRestriction.STRICT, settings.addModRestriction);
    }

    @Test
    public void testSavedLaxRestrictionTakesPrecedenceOverLegacyFlag() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":true,\"addModRestriction\":\"LAX\"}");

        assertEquals(AddModRestriction.LAX, settings.addModRestriction);
    }

    @Test
    public void testSavedUnrestrictedChoiceIsPreserved() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":false,\"addModRestriction\":\"NONE\"}");

        assertEquals(AddModRestriction.NONE, settings.addModRestriction);
    }

    @Test
    public void testLegacyDisabledRestrictionsAreMigrated() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":true}");

        assertEquals(AddModRestriction.NONE, settings.addModRestriction);
        assertEquals(AddModRestriction.NONE, loadSettings(Gsons.DEFAULT.toJson(settings)).addModRestriction);
    }

    @Test
    public void testLegacyEnabledRestrictionsAreMigrated() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":false}");

        assertEquals(AddModRestriction.STRICT, settings.addModRestriction);
    }

    @Test
    public void testDefaultRestrictionIsStrict() {
        assertEquals(AddModRestriction.STRICT, loadSettings("{}").addModRestriction);
    }

    @Test
    public void testRestrictionChangesSurviveRestartAfterLegacyMigration() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":true}");

        for (AddModRestriction restriction : AddModRestriction.values()) {
            settings.addModRestriction = restriction;
            settings = loadSettings(Gsons.DEFAULT.toJson(settings));

            assertEquals(restriction, settings.addModRestriction);
        }
    }

    @Test
    public void testLegacyFlagIsNoLongerSaved() {
        Settings settings = loadSettings("{\"disableAddModRestrictions\":true}");

        assertFalse(Gsons.DEFAULT.toJsonTree(settings).getAsJsonObject().has("disableAddModRestrictions"));
    }

    @Test
    public void testRestrictionIsMigratedBeforeOtherValidationSavesSettings() {
        TestSettings settings = readSettings("{\"disableAddModRestrictions\":true}");

        settings.validate();

        assertEquals(Collections.singletonList(AddModRestriction.NONE), settings.savedRestrictions);
    }

    @Test
    public void testLegacyPropertiesRestrictionsAreMigrated() {
        for (boolean disabled : new boolean[] { false, true }) {
            Settings settings = readSettings("{}");
            Properties properties = new Properties();
            properties.setProperty("disableaddmodrestrictions", Boolean.toString(disabled));

            settings.convert(properties);

            assertEquals(disabled ? AddModRestriction.NONE : AddModRestriction.STRICT, settings.addModRestriction);
        }
    }

    private TestSettings readSettings(String json) {
        return Gsons.DEFAULT.fromJson(json, TestSettings.class);
    }

    private Settings loadSettings(String json) {
        Settings settings = readSettings(json);
        settings.validate();
        return settings;
    }

    private static class TestSettings extends Settings {
        private transient List<AddModRestriction> savedRestrictions = new ArrayList<>();

        @Override
        public void save() {
            // Capture migration saves without writing the user's settings or instrumenting JDK classes with a spy.
            savedRestrictions.add(addModRestriction);
        }
    }
}
