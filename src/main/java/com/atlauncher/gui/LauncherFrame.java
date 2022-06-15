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
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.events.OnSide;
import com.atlauncher.events.Side;
import com.atlauncher.events.ScreenViewEvent;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.events.tab.TabChangedEvent;
import com.atlauncher.gui.components.LauncherBottomBar;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.tabs.AccountsTab;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksBrowserTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.gui.tabs.SettingsTab;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.gui.tabs.ToolsTab;
import com.atlauncher.gui.tabs.VanillaPacksTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.Utils;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
@Singleton
public final class LauncherFrame extends JFrame {
    private static final Logger LOG = LogManager.getLogger(LauncherFrame.class);

    private final LauncherBottomBar bottomBar;
    private JTabbedPane tabbedPane;

    private List<Tab> tabs;

    @Inject
    private LauncherFrame(final LauncherBottomBar bottomBar){
        LOG.info("Launcher opening");
        LOG.info("Made By Bob*");
        LOG.info("*(Not Actually)");

        App.launcher.setParentFrame(this);
        setTitle(Constants.LAUNCHER_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/icon.png"));

        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        try {
            if (App.settings.rememberWindowSizePosition && App.settings.launcherSize != null) {
                setSize(App.settings.launcherSize);
            }

            if (App.settings.rememberWindowSizePosition && App.settings.launcherPosition != null) {
                setLocation(App.settings.launcherPosition);
            }
        } catch (Exception e) {
            LOG.error("Error setting custom remembered window size settings", e);
        }

        LOG.info("Setting up Bottom Bar");
        this.bottomBar = bottomBar;
        LOG.info("Finished Setting up Bottom Bar");

        LOG.info("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        LOG.info("Finished Setting up Tabs");

        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);

        if (App.packToInstall != null) {
            Pack pack = PackManager.getPackBySafeName(App.packToInstall);

            if (pack != null && pack.isSemiPublic() && !PackManager.canViewSemiPublicPackByCode(pack.getCode())) {
                LOG.error("Error automatically installing " + pack.getName() + " as you don't have the "
                    + "pack added to the launcher!");
            } else {
                if (AccountManager.getSelectedAccount() == null || pack == null) {
                    LOG
                        .error("Error automatically installing " + (pack == null ? "pack" : pack.getName()) + "!");
                } else {
                    new InstanceInstallerDialog(pack);
                }
            }
        } else if (App.packShareCodeToInstall != null) {
            String[] parts = App.packShareCodeToInstall.split("\\|\\|\\|");

            if (parts.length != 4) {
                LOG.error("Error automatically installing pack from share code!");
            } else {
                Pack pack = PackManager.getPackBySafeName(parts[0]);

                if (pack != null && pack.isSemiPublic() && !PackManager.canViewSemiPublicPackByCode(pack.getCode())) {
                    LOG.error("Error automatically installing " + pack.getName() + " as you don't have the "
                        + "pack added to the launcher!");
                } else {
                    if (pack == null) {
                        LOG.error("Error automatically installing pack from share code!");
                    } else {
                        PackVersion version = pack.getVersionByName(parts[1]);

                        if (version == null) {
                            LOG.error("Error automatically installing " + pack.getName() + " from share code!");
                        } else {
                            new InstanceInstallerDialog(pack, version, parts[2], Boolean.parseBoolean(parts[3]));
                        }
                    }
                }

            }
        }

        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();

                if (App.settings.rememberWindowSizePosition) {
                    App.settings.launcherSize = c.getSize();
                    App.settings.save();
                }
            }

            public void componentMoved(ComponentEvent evt) {
                Component c = (Component) evt.getSource();

                if (App.settings.rememberWindowSizePosition) {
                    App.settings.launcherPosition = c.getLocation();
                    App.settings.save();
                }
            }
        });

        AppEventBus.register(this);
    }

    public void openLauncher(final boolean show){
        if (show) {
            LOG.info("Showing Launcher");
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
    }

    /**
     * Setup the individual tabs used in the Launcher sidebar
     */
    private void setupTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setName("mainTabs");

        PerformanceManager.start("newsTab");
        NewsTab newsTab = new NewsTab();
        App.launcher.setNewsPanel(newsTab);
        PerformanceManager.end("newsTab");

        PerformanceManager.start("vanillaPacksTab");
        VanillaPacksTab vanillaPacksTab = new VanillaPacksTab();
        PerformanceManager.end("vanillaPacksTab");

        PerformanceManager.start("packsBrowserTab");
        PacksBrowserTab packsBrowserTab = new PacksBrowserTab();
        App.launcher.setPacksBrowserPanel(packsBrowserTab);
        PerformanceManager.end("packsBrowserTab");

        PerformanceManager.start("instancesTab");
        InstancesTab instancesTab = new InstancesTab();
        App.launcher.setInstancesPanel(instancesTab);
        PerformanceManager.end("instancesTab");

        PerformanceManager.start("serversTab");
        ServersTab serversTab = new ServersTab();
        App.launcher.setServersPanel(serversTab);
        PerformanceManager.end("serversTab");

        PerformanceManager.start("accountsTab");
        AccountsTab accountsTab = new AccountsTab();
        PerformanceManager.end("accountsTab");

        PerformanceManager.start("toolsTab");
        ToolsTab toolsTab = new ToolsTab();
        PerformanceManager.end("toolsTab");

        PerformanceManager.start("settingsTab");
        SettingsTab settingsTab = new SettingsTab();
        PerformanceManager.end("settingsTab");

        this.tabs = Arrays.asList(new Tab[]{newsTab, vanillaPacksTab, packsBrowserTab, instancesTab,
            serversTab, accountsTab, toolsTab, settingsTab});

        tabbedPane.setFont(App.THEME.getTabFont());
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.setOpaque(true);
        tabbedPane.setSelectedIndex(App.settings.selectedTabOnStartup);

        final Tab tab = ((Tab)tabbedPane.getSelectedComponent());
        tabbedPane.addChangeListener(e -> {
            AppEventBus.postToDefault(TabChangedEvent.newInstance());
        });
        AppEventBus.postToDefault(ScreenViewEvent.forScreen(tab.getAnalyticsScreenViewName()));
    }

    @Subscribe
    @OnSide(Side.UI)
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }

        tabbedPane.setFont(App.THEME.getTabFont());
    }

    public static void open(){

    }
}
