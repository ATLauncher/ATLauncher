/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.atlauncher.data.Settings;

@SuppressWarnings("serial")
public class LauncherFrame extends JFrame {

    // Size of initial window
    private final Dimension WINDOW_SIZE = new Dimension(800, 500);
    private final BorderLayout LAYOUT_MANAGER = new BorderLayout();
    private final Color BASE_COLOR = new Color(40, 45, 50);

    public static LauncherConsole console;
    
    private JTabbedPane tabbedPane;
    private NewsPanel newsPanel;
    private PacksPanel packsPanel;
    private AddonsPanel addonsPanel;
    private InstancesPanel instancesPanel;
    private AccountPanel accountPanel;
    private SettingsPanel settingsPanel;

    private BottomBar bottomBar;

    private Settings settings;

    public LauncherFrame() {
        setSize(WINDOW_SIZE);
        setTitle("ATLauncher");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);
        
        setupConsole(); // Setup the console

        console.log("Setting up Look & Feel");
        setupLookAndFeel(); // Setup the look and feel for the Launcher
        console.log("Finished Setting up Look & Feel");

        setupData(); // Setup all the data needed

        console.log("Setting up Look & Feel");
        setupBottomBar(); // Setup the Bottom Bar
        console.log("Finished Setting up Bottom Bar");

        console.log("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        console.log("Finished Setting up Tabs");

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });
        console.log("Showing Launcher");
        setVisible(true);
    }
    
    private void setupConsole() {
        console = new LauncherConsole();
    }

    private void setupData() {
        this.settings = new Settings(this);
    }

    private void setupTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBackground(BASE_COLOR);

        newsPanel = new NewsPanel(settings);
        packsPanel = new PacksPanel(settings);
        addonsPanel = new AddonsPanel(settings);
        instancesPanel = new InstancesPanel(settings);
        settings.setInstancesPanel(instancesPanel);
        accountPanel = new AccountPanel(settings);
        settingsPanel = new SettingsPanel(settings);

        tabbedPane.addTab(null, Utils.getIconImage("/resources/NewsTab.png"),
                newsPanel, "News");
        tabbedPane.addTab(null, Utils.getIconImage("/resources/PacksTab.png"),
                packsPanel, "Packs");
        tabbedPane.addTab(null, Utils.getIconImage("/resources/AddonsTab.png"),
                addonsPanel, "Addons");
        tabbedPane.addTab(null,
                Utils.getIconImage("/resources/InstancesTab.png"),
                instancesPanel, "Instances");
        tabbedPane.addTab(null,
                Utils.getIconImage("/resources/AccountTab.png"), accountPanel,
                "Account");
        tabbedPane.addTab(null,
                Utils.getIconImage("/resources/SettingsTab.png"),
                settingsPanel, "Settings");
        tabbedPane.setBackground(BASE_COLOR.brighter());
        tabbedPane.setOpaque(true);
    }

    private void setupBottomBar() {
        bottomBar = new BottomBar();
    }

    private void setupLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // if (OSUtils.isMac()) {
        // UIManager.getLookAndFeelDefaults().put("defaultFont", new
        // Font("SansSerif", Font.PLAIN, 11));
        // }

        UIManager.put("control", BASE_COLOR);
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", Color.BLACK);
        UIManager.put("nimbusFocus", BASE_COLOR);
        UIManager.put("nimbusBorder", BASE_COLOR);
        UIManager.put("nimbusLightBackground", BASE_COLOR);
        UIManager.put("info", BASE_COLOR);
        UIManager.put("nimbusSelectionBackground", new Color(100, 100, 200));
        UIManager.put("Table.focusCellHighlightBorder",
                BorderFactory.createEmptyBorder(2, 5, 2, 5));
    }

}