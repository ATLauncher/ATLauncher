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
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Pack;
import com.atlauncher.data.version.PackVersion;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.LauncherBottomBar;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.tabs.AccountsTab;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.SettingsTab;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.gui.tabs.ToolsTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.utils.Utils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public final class LauncherFrame extends JFrame implements RelocalizationListener {
    private JTabbedPane tabbedPane;
    private NewsTab newsTab;
    private PacksTab packsTab;
    private InstancesTab instancesTab;
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
        setSize(new Dimension(1000, 615));
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
        }

        RelocalizationManager.addListener(this);

        App.TASKPOOL.execute(new Runnable() {
            public void run() {
                App.settings.checkMojangStatus(); // Check Minecraft status
                bottomBar.updateStatus(App.settings.getMojangStatus());
            }
        });

        if (App.packToInstall != null) {
            Pack pack = PackManager.getPackBySafeName(App.packToInstall);

            if (pack != null && pack.isSemiPublic() && !PackManager.canViewSemiPublicPackByCode(pack.getCode())) {
                LogManager.error("Error automatically installing " + pack.getName() + " as you don't have the " +
                        "pack added to the launcher!");
            } else {
                if (App.settings.isInOfflineMode() || AccountManager.getActiveAccount() == null || pack == null) {
                    LogManager.error("Error automatically installing " + (pack == null ? "pack" : pack.getName()) +
                            "!");
                } else {
                    new InstanceInstallerDialog(pack);
                }
            }
        } else if (App.packShareCodeToInstall != null) {
            String[] parts = App.packShareCodeToInstall.split("\\|\\|\\|");

            if (parts.length != 4) {
                LogManager.error("Error automatically installing pack from share code!");
            } else {
                Pack pack = PackManager.getPackBySafeName(parts[0]);

                if (pack != null && pack.isSemiPublic() && !PackManager.canViewSemiPublicPackByCode(pack.getCode())) {
                    LogManager.error("Error automatically installing " + pack.getName() + " as you don't have the " +
                            "pack added to the launcher!");
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

    public void updateTitle(String str) {
        setTitle(Constants.LAUNCHER_NAME + " " + Constants.VERSION + " - " + str);
    }

    /**
     * Setup the individual tabs used in the Launcher sidebar
     */
    private void setupTabs() {
        tabbedPane = new JTabbedPane((App.THEME.tabsOnRight() ? JTabbedPane.RIGHT : JTabbedPane.LEFT));
        tabbedPane.setBackground(App.THEME.getBaseColor());

        newsTab = new NewsTab();
        App.settings.setNewsPanel(newsTab);
        packsTab = new PacksTab();
        instancesTab = new InstancesTab();
        accountsTab = new AccountsTab();
        toolsTab = new ToolsTab();
        settingsTab = new SettingsTab();

        this.tabs = Arrays.asList(new Tab[]{newsTab, packsTab, instancesTab, accountsTab, toolsTab, settingsTab});

        tabbedPane.setFont(App.THEME.getTabFont().deriveFont(34.0F));
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String tabName = ((Tab) tabbedPane.getSelectedComponent()).getTitle();
                if (tabbedPane.getSelectedIndex() == 1) {
                    updateTitle("Packs - " + PackManager.getInstallableCount());
                } else {
                    updateTitle(tabName);
                }

                EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.TabChangeEvent.class));
            }
        });
        tabbedPane.setBackground(App.THEME.getTabBackgroundColor());
        tabbedPane.setOpaque(true);
    }

    /**
     * Setup the bottom bar of the Launcher
     */
    private void setupBottomBar() {
        bottomBar = new LauncherBottomBar();
    }

    @Override
    public void onRelocalization() {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
    }
}