/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class LauncherFrame extends JFrame {

    // Size of initial window
    private final BorderLayout LAYOUT_MANAGER = new BorderLayout();
    private final Color BASE_COLOR = new Color(40, 45, 50);

    private JTabbedPane tabbedPane;
    private NewsPanel newsPanel;
    private PacksPanel packsPanel;
    private AddonsPanel addonsPanel;
    private InstancesPanel instancesPanel;
    private AccountPanel accountPanel;
    private SettingsPanel settingsPanel;

    private BottomBar bottomBar;

    public LauncherFrame(boolean show) {
        App.settings.log("Launcher opening");
        App.settings.log("Made By Bob*");
        App.settings.log("*(Not Actually)");
        App.settings.setParentFrame(this);
        setSize(new Dimension(1000, 600));
        setTitle("ATLauncher %VERSION%");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);

        App.settings.log("Setting up Look & Feel");
        setupBottomBar(); // Setup the Bottom Bar
        App.settings.log("Finished Setting up Bottom Bar");

        App.settings.log("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        App.settings.log("Finished Setting up Tabs");

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

        if (show) {
            App.settings.log("Showing Launcher");
            setVisible(true);
        }

        App.settings.addConsoleListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                App.settings.log("Hiding console");
                App.settings.setConsoleVisible(false);
                bottomBar.hideConsole();
            }
        });

        App.TASKPOOL.execute(new Runnable(){
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
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBackground(BASE_COLOR);

        newsPanel = new NewsPanel();
        App.settings.setNewsPanel(newsPanel);
        packsPanel = new PacksPanel();
        App.settings.setPacksPanel(packsPanel);
        addonsPanel = new AddonsPanel();
        instancesPanel = new InstancesPanel();
        App.settings.setInstancesPanel(instancesPanel);
        accountPanel = new AccountPanel();
        settingsPanel = new SettingsPanel();

        tabbedPane.setFont(Utils.makeFont("Oswald-Regular").deriveFont((float) 34));
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.news"), newsPanel);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.packs"), packsPanel);
        // tabbedPane.addTab(App.settings.getLocalizedString("tabs.addons"), addonsPanel);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.instances"), instancesPanel);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.account"), accountPanel);
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.settings"), settingsPanel);
        tabbedPane.setBackground(BASE_COLOR.brighter());
        tabbedPane.setOpaque(true);
    }

    /**
     * Setup the bottom bar of the Launcher
     */
    private void setupBottomBar() {
        bottomBar = new BottomBar();
        App.settings.setBottomBar(bottomBar);
    }
}
