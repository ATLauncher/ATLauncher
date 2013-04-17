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

import com.atlauncher.data.Instance;
import com.atlauncher.data.Instances;
import com.atlauncher.data.Packs;
import com.atlauncher.data.Version;

@SuppressWarnings("serial")
public class LauncherFrame extends JFrame {

    private final Dimension WINDOW_SIZE = new Dimension(800, 500);
    private final BorderLayout LAYOUT_MANAGER = new BorderLayout();
    private final Color BASE_COLOR = new Color(40, 45, 50);

    private JTabbedPane tabbedPane;
    private NewsPanel newsPanel;
    private PacksPanel packsPanel;
    private InstancesPanel instancesPanel;
    private SettingsPanel settingsPanel;

    private BottomBar bottomBar;
    
    private Instances instances;
    private Packs packs;

    public LauncherFrame() {
        setSize(WINDOW_SIZE);
        setTitle("ATLauncher");
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);

        setupLookAndFeel(); // Setup the look and feel for the Launcher
        
        setupData(); // Setup all the data needed

        setupBottomBar(); // Setup the Bottom Bar

        setupTabs(); // Setup the JTabbedPane

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });
    }
    
    private void setupData() {
        this.instances = new Instances();
        this.packs = new Packs();
        this.instances.addInstance(new Instance("Test 1", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 2", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 3", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 4", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 5", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 6", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 7", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 8", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 9", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 10", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 11", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 12", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 13", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 14", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 15", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 16", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 17", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 18", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 19", "SolitaryCraft", new Version(1, 2, 3)));
        this.instances.addInstance(new Instance("Test 20", "SolitaryCraft", new Version(1, 2, 3)));
    }

    private void setupTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBackground(BASE_COLOR);

        newsPanel = new NewsPanel();
        packsPanel = new PacksPanel(this, instances);
        instancesPanel = new InstancesPanel(this, instances);
        instances.addPanel(instancesPanel);
        settingsPanel = new SettingsPanel();

        tabbedPane.addTab(null, Utils.getIconImage("/resources/NewsTab.png"),
                newsPanel, "News");
        tabbedPane.addTab(null, Utils.getIconImage("/resources/PacksTab.png"),
                packsPanel, "Packs");
        tabbedPane.addTab(null,
                Utils.getIconImage("/resources/InstancesTab.png"),
                instancesPanel, "Instances");
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