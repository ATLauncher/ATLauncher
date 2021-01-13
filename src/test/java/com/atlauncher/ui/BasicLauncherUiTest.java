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

import java.awt.Dialog;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.PackCard;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.data.Index;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.Test;

public class BasicLauncherUiTest extends AbstractUiTest {
    @Test
    public void testTheLauncherOpens() {
        this.frame.button("updateData").requireVisible();

        JTabbedPaneFixture mainTabsFixture = this.frame.tabbedPane("mainTabs");
        mainTabsFixture.requireVisible();
        mainTabsFixture.requireTitle("News", Index.atIndex(0));
        mainTabsFixture.requireSelectedTab(Index.atIndex(0));
    }

    @Test
    public void testThatUsersCanLogin() {
        JTabbedPaneFixture mainTabsFixture = this.frame.tabbedPane("mainTabs");
        mainTabsFixture.selectTab("Accounts");

        JComboBoxFixture accountsTabAccountsComboBox = this.frame.comboBox("accountsTabAccountsComboBox");
        accountsTabAccountsComboBox.requireVisible();
        accountsTabAccountsComboBox.requireItemCount(1);
        accountsTabAccountsComboBox.requireSelection(0);

        JButtonFixture loginButton = this.frame.button("leftButton");
        loginButton.requireVisible().requireEnabled();

        JTextComponentFixture usernameField = this.frame.textBox("usernameField");
        usernameField.requireVisible().requireEditable();

        JTextComponentFixture passwordField = this.frame.textBox("passwordField");
        passwordField.requireVisible().requireEditable();

        usernameField.setText(System.getenv("MOJANG_ACCOUNT_USERNAME"));
        passwordField.setText(System.getenv("MOJANG_ACCOUNT_PASSWORD"));

        // login
        loginButton.click();

        DialogFixture loginDialog = WindowFinder.findDialog("loginDialog").using(robot());
        loginDialog.requireVisible();

        // give it time
        Pause.pause(5, TimeUnit.SECONDS);

        // account was added, fields cleared
        accountsTabAccountsComboBox.requireItemCount(2);
        usernameField.requireEmpty();
        passwordField.requireEmpty();

        // account selector now showing
        JComboBoxFixture accountSelector = this.frame.comboBox("accountSelector");
        accountSelector.requireVisible();
    }

    @Test
    public void testThatInstancesCanInstall() {
        JTabbedPaneFixture mainTabsFixture = this.frame.tabbedPane("mainTabs");
        mainTabsFixture.selectTab("Vanilla Packs");

        JPanelFixture vanillaPacksPanel = this.frame.panel("vanillaPacksPanel");
        vanillaPacksPanel.requireVisible();

        // give it time
        Pause.pause(5, TimeUnit.SECONDS);

        JPanelFixture vanillaPackCard = vanillaPacksPanel.panel(new GenericTypeMatcher<PackCard>(PackCard.class) {
            @Override
            protected boolean isMatching(PackCard packCard) {
                return packCard.getPack().name.equalsIgnoreCase("Vanilla Minecraft") && packCard.isVisible();
            }
        });
        vanillaPackCard.requireVisible();

        JButtonFixture newInstanceButton = vanillaPackCard.button(JButtonMatcher.withText("New Instance"));
        newInstanceButton.requireVisible();
        newInstanceButton.click();

        DialogFixture loginDialog = WindowFinder.findDialog("instanceInstallerDialog").using(robot());
        loginDialog.requireVisible();

        JButtonFixture installButton = loginDialog.button(JButtonMatcher.withText("Install"));
        installButton.requireVisible();

        installButton.click();

        Pause.pause(new Condition("Waits for install to finish") {
            @Override
            public boolean test() {
                return Files.exists(workingDir.resolve("instances/VanillaMinecraft/instance.json"));
            }
        }, Timeout.timeout(5, TimeUnit.MINUTES));

        DialogFixture installSuccessDialog = WindowFinder.findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return dialog.getTitle().matches("Vanilla Minecraft [0-9\\.]+ Installed");
            }
        }).using(robot());
        installSuccessDialog.requireVisible();
        installSuccessDialog.button(JButtonMatcher.withText("Ok")).click();

        mainTabsFixture.selectTab("Instances");

        JPanelFixture instancesPanel = this.frame.panel("instancesPanel");
        instancesPanel.requireVisible();

        JPanelFixture vanillaInstanceCard = instancesPanel
                .panel(new GenericTypeMatcher<InstanceCard>(InstanceCard.class) {
                    @Override
                    protected boolean isMatching(InstanceCard instanceCard) {
                        return instanceCard.getInstance().launcher.name.equals("Vanilla Minecraft");
                    }
                });
        vanillaInstanceCard.requireVisible();

        vanillaInstanceCard.button(JButtonMatcher.withText("Play")).requireVisible().requireEnabled();
        vanillaInstanceCard.button(JButtonMatcher.withText("Reinstall")).requireVisible().requireEnabled();
    }
}
