/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.evnt.RelocalizationEvent;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.tabs.AccountsTab;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.SettingsTab;
import com.atlauncher.gui.tabs.ToolsTab;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class LauncherFrame extends JFrame implements RelocalizationListener{
    private JTabbedPane tabbedPane;
    private NewsTab newsTab;
    private PacksTab packsTab;
    private InstancesTab instancesTab;
    private AccountsTab accountsTab;
    private ToolsTab toolsTab;
    private SettingsTab settingsTab;

    private LauncherBottomBar bottomBar;

    public LauncherFrame(boolean show) {
        App.settings.log("Launcher opening");
        App.settings.log("Made By Bob*");
        App.settings.log("*(Not Actually)");
        App.settings.setParentFrame(this);
        setSize(new Dimension(1000, 575));
        setTitle("ATLauncher " + Constants.VERSION);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setLayout(new BorderLayout());

        App.settings.log("Setting up Bottom Bar");
        setupBottomBar(); // Setup the Bottom Bar
        App.settings.log("Finished Setting up Bottom Bar");

        App.settings.log("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        App.settings.log("Finished Setting up Tabs");

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        if (show) {
            App.settings.log("Showing Launcher");
            setVisible(true);
        }

        App.TASKPOOL.execute(new Runnable() {
            public void run() {
                App.settings.checkMojangStatus(); // Check Minecraft status
                bottomBar.updateStatus(App.settings.getMojangStatus());
            }
        });
    }

    /**
     * Setup the individual tabs used in the Launcher sidebar
     */
    private void setupTabs() {
        tabbedPane = new JTabbedPane((App.THEME.tabsOnRight() ? JTabbedPane.RIGHT
                : JTabbedPane.LEFT));
        tabbedPane.setBackground(App.THEME.getBaseColor());

        newsTab = new NewsTab();
        App.settings.setNewsPanel(newsTab);
        packsTab = new PacksTab();
        App.settings.setPacksPanel(packsTab);
        instancesTab = new InstancesTab();
        App.settings.setInstancesPanel(instancesTab);
        accountsTab = new AccountsTab();
        toolsTab = new ToolsTab();
        settingsTab = new SettingsTab();

        tabbedPane.setFont(App.THEME.getTabFont().deriveFont(34.0F));
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.news"), newsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.packs"), packsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.instances"), instancesTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.account"), accountsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.tools"), toolsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.settings"), settingsTab);
        tabbedPane.setBackground(App.THEME.getTabBackgroundColor());
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
    public void onRelocalization(RelocalizationEvent event) {
        this.tabbedPane.removeAll();
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.news"), newsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.packs"), packsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.instances"), instancesTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.account"), accountsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.tools"), toolsTab);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.settings"), settingsTab);
    }
}
