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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.atlauncher.data.Settings;
import com.atlauncher.workers.ServerTester;

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

    public static Settings settings;

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

        console.log("Checking Servers");
        checkServers(); // Check the servers
        console.log("Finished Checking Servers");

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

    /**
     * Check the server list and see which are/aren't available
     */
    private void checkServers() {
        new ServerTester().execute();
    }

    /**
     * Setup the console and display it
     */
    private void setupConsole() {
        console = new LauncherConsole();
    }

    /**
     * Setup the settings/data for the Launcher including Packs, Addons,
     * Languages and other things
     */
    private void setupData() {
        LauncherFrame.settings = new Settings(this);
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
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedComponent() instanceof PacksPanel) {
                    // Reloads the data in the PacksPanel
                    ((PacksPanel) tabbedPane.getSelectedComponent()).reloadTable();
                }else if (tabbedPane.getSelectedComponent() instanceof SettingsPanel) {
                    // Reloads the data in the SettingsPanel
                    ((SettingsPanel) tabbedPane.getSelectedComponent()).reloadData();
                }
            }
        });
        tabbedPane.setBackground(BASE_COLOR.brighter());
        tabbedPane.setOpaque(true);
    }

    /**
     * Setup the bottom bar of the Launcher
     */
    private void setupBottomBar() {
        bottomBar = new BottomBar();
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
        UIManager.put("Table.focusCellHighlightBorder",
                BorderFactory.createEmptyBorder(2, 5, 2, 5));
    }

}