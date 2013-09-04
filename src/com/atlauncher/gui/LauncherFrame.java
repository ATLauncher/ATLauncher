/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.atlauncher.App;

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
        App.settings.getConsole().log("Launcher opening");
        App.settings.getConsole().log("Made By Bob*");
        App.settings.getConsole().log("*(Not Actually)");
        App.settings.setParentFrame(this);
        if (App.settings.getLanguage().getName().equalsIgnoreCase("German")) {
            setSize(new Dimension(875, 500));
        } else {
            setSize(new Dimension(800, 500));
        }
        setTitle("ATLauncher %VERSION%");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);

        App.settings.getConsole().log("Setting up Look & Feel");
        setupLookAndFeel(); // Setup the look and feel for the Launcher
        App.settings.getConsole().log("Finished Setting up Look & Feel");

        App.settings.getConsole().log("Setting up Look & Feel");
        setupBottomBar(); // Setup the Bottom Bar
        App.settings.getConsole().log("Finished Setting up Bottom Bar");

        App.settings.getConsole().log("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        App.settings.getConsole().log("Finished Setting up Tabs");

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

        if (show) {
            App.settings.getConsole().log("Showing Launcher");
            setVisible(true);
        }

        App.settings.getConsole().addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                App.settings.getConsole().log("Hiding console");
                App.settings.getConsole().setVisible(false);
                bottomBar.hideConsole();
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

    /**
     * Setup the Java Look and Feel to make things look pretty
     */
    private void setupLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            App.settings.getConsole().logStackTrace(e);
        }

        // For some reason Mac OS makes text bigger then it should be
        if (Utils.isMac()) {
            UIManager.getLookAndFeelDefaults().put("defaultFont",
                    new Font("SansSerif", Font.PLAIN, 11));
        }

        UIManager.put("control", BASE_COLOR);
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", Color.BLACK);
        UIManager.put("nimbusFocus", BASE_COLOR);
        UIManager.put("nimbusBorder", BASE_COLOR);
        UIManager.put("nimbusLightBackground", BASE_COLOR);
        UIManager.put("info", BASE_COLOR);
        UIManager.put("nimbusSelectionBackground", new Color(100, 100, 200));
        UIManager
                .put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(2, 5, 2, 5));
    }

}