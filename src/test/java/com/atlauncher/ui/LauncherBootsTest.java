/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.ui;

import org.assertj.swing.data.Index;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.junit.Test;

public class LauncherBootsTest extends AbstractUiTest {
    @Test
    public void testTheLauncherOpens() {
        this.frame.button("updateData").requireVisible();

        JTabbedPaneFixture mainTabsFixture = this.frame.tabbedPane("mainTabs");
        mainTabsFixture.requireVisible();
        mainTabsFixture.requireTitle("News", Index.atIndex(0));
        mainTabsFixture.requireSelectedTab(Index.atIndex(0));
    }
}
