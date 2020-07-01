/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemTray;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.LauncherBottomBar;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.tabs.AccountsTab;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.gui.tabs.SettingsTab;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.gui.tabs.ToolsTab;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public final class LauncherFrame extends JFrame implements RelocalizationListener {
    private JTabbedPane tabbedPane;
    private NewsTab newsTab;
    private PacksTab vanillaPacksTab;
    private PacksTab featuredPacksTab;
    private PacksTab packsTab;
    private InstancesTab instancesTab;
    private ServersTab serversTab;
    private AccountsTab accountsTab;
    private ToolsTab toolsTab;
    private SettingsTab settingsTab;

    private List<Tab> tabs;

    private LauncherBottomBar bottomBar;

    public LauncherFrame(boolean show) {
        LogManager.info("Launcher opening");
        LogManager.info("Made By Bob*");
        LogManager.info("*(Not Actually)");

        App.settings.setParentFrame(this);
        setSize(new Dimension(1200, 700));
        setTitle(Constants.LAUNCHER_NAME + " " + Constants.VERSION);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        this.setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));

        LogManager.info("Setting up Bottom Bar");
        setupBottomBar(); // Setup the Bottom Bar
        LogManager.info("Finished Setting up Bottom Bar");

        LogManager.info("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        LogManager.info("Finished Setting up Tabs");

        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);

        if (show) {
            LogManager.info("Showing Launcher");
            setVisible(true);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    try {
                        if (SystemTray.isSupported()) {
                            SystemTray.getSystemTray().remove(App.trayIcon);
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        RelocalizationManager.addListener(this);

        if (App.packToInstall != null) {
            Pack pack = App.settings.getPackBySafeName(App.packToInstall);

            if (pack != null && pack.isSemiPublic() && !App.settings.canViewSemiPublicPackByCode(pack.getCode())) {
                LogManager.error("Error automatically installing " + pack.getName() + " as you don't have the "
                        + "pack added to the launcher!");
            } else {
                if (App.settings.getAccount() == null || pack == null) {
                    LogManager
                            .error("Error automatically installing " + (pack == null ? "pack" : pack.getName()) + "!");
                } else {
                    new InstanceInstallerDialog(pack);
                }
            }
        } else if (App.packShareCodeToInstall != null) {
            String[] parts = App.packShareCodeToInstall.split("\\|\\|\\|");

            if (parts.length != 4) {
                LogManager.error("Error automatically installing pack from share code!");
            } else {
                Pack pack = App.settings.getPackBySafeName(parts[0]);

                if (pack != null && pack.isSemiPublic() && !App.settings.canViewSemiPublicPackByCode(pack.getCode())) {
                    LogManager.error("Error automatically installing " + pack.getName() + " as you don't have the "
                            + "pack added to the launcher!");
                } else {
                    if (pack == null) {
                        LogManager.error("Error automatically installing pack from share code!");
                    } else {
                        PackVersion version = pack.getVersionByName(parts[1]);

                        if (version == null) {
                            LogManager.error("Error automatically installing " + pack.getName() + " from share code!");
                        } else {
                            new InstanceInstallerDialog(pack, version, parts[2], Boolean.parseBoolean(parts[3]));
                        }
                    }
                }

            }
        }
    }

    /**
     * Setup the individual tabs used in the Launcher sidebar
     */
    private void setupTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);

        tabbedPane.addChangeListener(e -> {
            String title = ((Tab) tabbedPane.getSelectedComponent()).getTitle();
            Analytics.sendScreenView(title);
        });

        newsTab = new NewsTab();
        App.settings.setNewsPanel(newsTab);

        vanillaPacksTab = new PacksTab(false, true);
        App.settings.setVanillaPacksPanel(vanillaPacksTab);

        featuredPacksTab = new PacksTab(true, false);
        App.settings.setFeaturedPacksPanel(featuredPacksTab);

        packsTab = new PacksTab(false, false);
        App.settings.setPacksPanel(packsTab);

        instancesTab = new InstancesTab();
        App.settings.setInstancesPanel(instancesTab);

        serversTab = new ServersTab();
        App.settings.setServersPanel(serversTab);

        accountsTab = new AccountsTab();
        toolsTab = new ToolsTab();
        settingsTab = new SettingsTab();

        this.tabs = Arrays.asList(new Tab[] { newsTab, vanillaPacksTab, featuredPacksTab, packsTab, instancesTab,
                serversTab, accountsTab, toolsTab, settingsTab });

        tabbedPane.setFont(App.THEME.getTabFont());
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.setOpaque(true);
    }

    /**
     * Setup the bottom bar of the Launcher
     */
    private void setupBottomBar() {
        bottomBar = new LauncherBottomBar();
        App.settings.setBottomBar(bottomBar);
    }

    @Override
    public void onRelocalization() {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
    }
}
