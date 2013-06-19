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

import com.atlauncher.data.Settings;

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

    public static Settings settings;

    public LauncherFrame(Settings settings) {
        LauncherFrame.settings = settings;
        LauncherFrame.settings.setParentFrame(this);
        if(settings.enableConsole()){
            LauncherFrame.settings.getConsole().setVisible(true);
        }
        setSize(new Dimension(800, 500));
        setTitle("ATLauncher");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);

        LauncherFrame.settings.getConsole().log("Setting up Look & Feel");
        setupLookAndFeel(); // Setup the look and feel for the Launcher
        LauncherFrame.settings.getConsole().log("Finished Setting up Look & Feel");

        LauncherFrame.settings.getConsole().log("Setting up Look & Feel");
        setupBottomBar(); // Setup the Bottom Bar
        LauncherFrame.settings.getConsole().log("Finished Setting up Bottom Bar");

        LauncherFrame.settings.getConsole().log("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        LauncherFrame.settings.getConsole().log("Finished Setting up Tabs");

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });
        LauncherFrame.settings.getConsole().log("Showing Launcher");
        setVisible(true);

        LauncherFrame.settings.getConsole().addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                LauncherFrame.settings.getConsole().log("Hidding console");
                LauncherFrame.settings.getConsole().setVisible(false);
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
        packsPanel = new PacksPanel();
        addonsPanel = new AddonsPanel();
        instancesPanel = new InstancesPanel();
        settings.setInstancesPanel(instancesPanel);
        accountPanel = new AccountPanel();
        settingsPanel = new SettingsPanel();

        tabbedPane.addTab(null, Utils.getIconImage("/resources/NewsTab.png"), newsPanel);
        tabbedPane.addTab(null, Utils.getIconImage("/resources/PacksTab.png"), packsPanel);
        tabbedPane.addTab(null, Utils.getIconImage("/resources/AddonsTab.png"), addonsPanel);
        tabbedPane.addTab(null, Utils.getIconImage("/resources/InstancesTab.png"), instancesPanel);
        tabbedPane.addTab(null, Utils.getIconImage("/resources/AccountTab.png"), accountPanel);
        tabbedPane.addTab(null, Utils.getIconImage("/resources/SettingsTab.png"), settingsPanel);
        tabbedPane.setBackground(BASE_COLOR.brighter());
        tabbedPane.setOpaque(true);
    }

    /**
     * Setup the bottom bar of the Launcher
     */
    private void setupBottomBar() {
        bottomBar = new BottomBar();
        settings.setBottomBar(bottomBar);
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
            e.printStackTrace();
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