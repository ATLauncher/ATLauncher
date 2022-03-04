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
package ui;

import java.awt.Dialog;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.atlauncher.constants.Constants;
import com.atlauncher.gui.card.InstanceCard;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.data.Index;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import ui.mocks.MockHelper;

public class BasicLauncherUiTest extends AbstractUiTest {
    @Test
    public void testTheLauncherOpens() {
        this.frame.button("checkForUpdates").requireVisible();

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

        JButtonFixture loginWithMicrosoftButton = this.frame.button("loginWithMicrosoftButton");
        loginWithMicrosoftButton.requireVisible().requireEnabled();

        MockHelper.mockJson(mockServer, "POST", "login.live.com", "/oauth20_token.srf", "microsoft-oauth-token.json");
        MockHelper.mockJson(mockServer, "POST", "user.auth.xboxlive.com", "/user/authenticate",
                "user-authenticate.json");
        MockHelper.mockJson(mockServer, "POST", "xsts.auth.xboxlive.com", "/xsts/authorize", "xsts-authorize.json");
        MockHelper.mockJson(mockServer, "POST", "api.minecraftservices.com", "/launcher/login", "launcher-login.json");
        MockHelper.mockJson(mockServer, "GET", "api.minecraftservices.com", "/entitlements/license*",
                "entitlements-license.json");
        MockHelper.mockJson(mockServer, "GET", "api.minecraftservices.com", "/minecraft/profile",
                "minecraft-profile.json");
        MockHelper.mockPng(mockServer, "GET", "textures.minecraft.net",
                "/texture/3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3",
                "3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3.png");

        // login
        loginWithMicrosoftButton.click();

        DialogFixture loginDialog = WindowFinder.findDialog("LoginWithMicrosoftDialog").using(robot());
        loginDialog.requireVisible();

        // fake a response token from a Microsoft login
        try {
            new OkHttpClient()
                    .newCall(new Request.Builder()
                            .url("http://127.0.0.1:28562?code=M.R3_BAY.63fa6b74-be49-487c-9b3a-ccb94ab70908").build())
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // wait 3 seconds for calls to happen and account to add
        Pause.pause(3, TimeUnit.SECONDS);

        // account was added, fields cleared
        accountsTabAccountsComboBox.requireItemCount(2);

        // account selector now showing
        JComboBoxFixture accountSelector = this.frame.comboBox("accountSelector");
        accountSelector.requireVisible();
        mainTabsFixture.selectTab("Vanilla Packs");
        Pause.pause(1, TimeUnit.SECONDS);

        MockHelper.mockCdnJson(mockServer, "GET", "/containers/atl/packs/VanillaMinecraft/versions/1.16.4/Configs.json",
                "vanilla-1-16-4-configs.json");
        MockHelper.mockJson(mockServer, "GET", "launchermeta.mojang.com",
                "/v1/packages/8c72b5155010a100c70a558c6a7bef3e923c8525/1.16.4.json", "1.16.4.json");
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

        JButtonFixture createInstanceButton = vanillaPacksPanel.button(JButtonMatcher.withText("Create Instance"));
        createInstanceButton.requireVisible();
        createInstanceButton.click();

        Pause.pause(new Condition("Waits for install to finish") {
            @Override
            public boolean test() {
                return Files.exists(workingDir.resolve("instances/Minecraft1164/instance.json"));
            }
        }, Timeout.timeout(5, TimeUnit.MINUTES));

        DialogFixture installSuccessDialog = WindowFinder.findDialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return dialog.getTitle().matches("Minecraft [0-9\\.]+ Installed");
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
                        return instanceCard.getInstance().launcher.name.equals("Minecraft 1.16.4");
                    }
                });
        vanillaInstanceCard.requireVisible();

        vanillaInstanceCard.button(JButtonMatcher.withText("Play")).requireVisible().requireEnabled();
        vanillaInstanceCard.button(JButtonMatcher.withText("Edit Instance")).requireVisible().requireEnabled();
    }
}
