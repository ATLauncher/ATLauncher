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
package ui;

import java.awt.Dialog;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.atlauncher.constants.Constants;
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

import ui.mocks.MockHelper;

public class BasicLauncherUiTest extends AbstractUiTest {
    @Test
    public void testTheLauncherOpens() {
        this.frame.button("updateData").requireVisible();

        JTabbedPaneFixture mainTabsFixture = this.frame.tabbedPane("mainTabs");
        mainTabsFixture.requireVisible();
        mainTabsFixture.requireTitle("News", Index.atIndex(0));
        mainTabsFixture.requireSelectedTab(Index.atIndex(0));

        mainTabsFixture.selectTab("Accounts");
        Pause.pause(1, TimeUnit.SECONDS);

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

        MockHelper.mockJson(mockServer, "POST", "authserver.mojang.com", "/authenticate", "login-success.js");
        MockHelper.mockJson(mockServer, "GET", "sessionserver.mojang.com",
                "/session/minecraft/profile/e50e5b562ca3c41f35631867a7cb14c5", "profile.json");
        MockHelper.mockPng(mockServer, "GET", "textures.minecraft.net",
                "/texture/3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3",
                "3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3.png");

        usernameField.setText("test@example.com");
        passwordField.setText("password");

        // login
        loginButton.click();

        DialogFixture loginDialog = WindowFinder.findDialog("loginDialog").using(robot());
        loginDialog.requireVisible();

        // give it 5 seconds
        Pause.pause(5, TimeUnit.SECONDS);

        // account was added, fields cleared
        accountsTabAccountsComboBox.requireItemCount(2);
        usernameField.requireEmpty();
        passwordField.requireEmpty();

        // account selector now showing
        JComboBoxFixture accountSelector = this.frame.comboBox("accountSelector");
        accountSelector.requireVisible();
        mainTabsFixture.selectTab("Vanilla Packs");
        Pause.pause(1, TimeUnit.SECONDS);

        MockHelper.mockCdnJson(mockServer, "GET", "/containers/atl/packs/VanillaMinecraft/versions/1.16.4/Configs.json",
                "vanilla-1-16-4-configs.json");
        MockHelper.mockJson(mockServer, "GET", "launchermeta.mojang.com", "/mc/game/version_manifest.json",
                "version_manifest.json");
        MockHelper.mockJson(mockServer, "GET", "launchermeta.mojang.com",
                "/v1/packages/3c33166875193f50f446a0730960208fcbf9f96c/1.16.4.json", "1.16.4.json");
        MockHelper.mockJson(mockServer, "GET", "launchermeta.mojang.com",
                "/v1/packages/f8e11ca03b475dd655755b945334c7a0ac2c3b43/1.16.json", "1.16.json");
        MockHelper.mockPng(mockServer, "GET", "resources.download.minecraft.net",
                "/1b/1b5fa6ad7c204f60654d43fa25560ff36a6420dc", "1b5fa6ad7c204f60654d43fa25560ff36a6420dc");
        MockHelper.mockPng(mockServer, "GET", "resources.download.minecraft.net",
                "/a8/a81ca0f94145275865aca5b27df09b17836bc102", "a81ca0f94145275865aca5b27df09b17836bc102");
        MockHelper.mockJar(mockServer, "GET", "libraries.minecraft.net", "/com/atlauncher/test/1.0/test-1.0.jar",
                "test-1.0.jar");
        MockHelper.mockXml(mockServer, "GET", "launcher.mojang.com",
                "/v1/objects/9150e6e5de6d49a83113ed3be5719aed2a387523/client-1.12.xml", "client-1.12.xml");
        MockHelper.mockJar(mockServer, "GET", "launcher.mojang.com",
                "/v1/objects/4addb91039ae452c5612f288bfe6ce925dac92c5/client.jar", "client-1-16-4.jar");
        MockHelper.mockNoResponseSuccess(mockServer, "POST", Constants.API_HOST,
                "/v1/launcher/pack/VanillaMinecraft/installed/");

        JPanelFixture vanillaPacksPanel = this.frame.panel("vanillaPacksPanel");
        vanillaPacksPanel.requireVisible();

        JPanelFixture vanillaPackCard = vanillaPacksPanel.panel(new GenericTypeMatcher<PackCard>(PackCard.class, true) {
            @Override
            protected boolean isMatching(PackCard packCard) {
                return packCard.getPack().name.equalsIgnoreCase("Vanilla Minecraft") && packCard.isVisible();
            }
        });
        vanillaPackCard.requireVisible();

        JButtonFixture newInstanceButton = vanillaPackCard.button(JButtonMatcher.withText("New Instance"));
        newInstanceButton.requireVisible();
        newInstanceButton.click();

        DialogFixture instanceInstallerDialog = WindowFinder.findDialog("instanceInstallerDialog").using(robot());
        instanceInstallerDialog.requireVisible();

        JButtonFixture installButton = instanceInstallerDialog.button(JButtonMatcher.withText("Install"));
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
        Pause.pause(1, TimeUnit.SECONDS);

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
