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
package com.atlauncher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Assert;
import org.junit.Test;

public final class OptionsTest {
    @Test
    public void test() {
        OptionParser parser = new OptionParser();
        parser.accepts("launch").withRequiredArg().ofType(String.class);
        parser.accepts("updated").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug-level").withRequiredArg().ofType(Integer.class);
        parser.accepts("use-gzip").withRequiredArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration").withRequiredArg().ofType(Boolean.class);
        parser.accepts("force-offline-mode").withRequiredArg().ofType(Boolean.class);

        OptionSet options = parser.parse("--force-offline-mode=true", "--launch=ResonantRise",
                "--skip-tray-integration=true", "--debug-level=3");

        Assert.assertTrue(options.has("force-offline-mode"));
        Assert.assertEquals(true, options.valueOf("force-offline-mode"));
        Assert.assertEquals("ResonantRise", options.valueOf("launch"));
        Assert.assertEquals(3, options.valueOf("debug-level"));
    }
}