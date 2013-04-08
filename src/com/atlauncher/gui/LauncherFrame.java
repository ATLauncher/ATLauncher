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
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.atlauncher.workers.UpdateDownloader;
import com.atlauncher.workers.UpdateDownloaderResults;

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

    public LauncherFrame() {
        setSize(WINDOW_SIZE);
        setTitle("ATLauncher");
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setLayout(LAYOUT_MANAGER);

        setLookAndFeel(); // Set the look and feel for the Launcher

        setupTabs(); // Setup the JTabbedPane

        setupSidebar(); // Setup the Sidebar

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });
        checkUpdates();
    }

    private void checkUpdates() {
        UpdateDownloader updater = new UpdateDownloader(){
            @Override
            protected void process(List<UpdateDownloaderResults> chunks) {
                UpdateDownloaderResults got = chunks.get(chunks.size()-1);
                if(got == UpdateDownloaderResults.checking){
                    bottomBar.setText("Checking For Updates!");
                    bottomBar.setIndeterminate(true);
                }else{
                    bottomBar.setIndeterminate(false);
                }

                if(got == UpdateDownloaderResults.downloading){
                    bottomBar.setText("Updates Downloading!");
                    bottomBar.setProgress(50);
                }else if(got == UpdateDownloaderResults.complete){
                    bottomBar.setText("Updates Completed!");
                    bottomBar.setProgress(100);
                }else if(got == UpdateDownloaderResults.serverNotReachable){
                    bottomBar.setText("Server Not Reachable!");
                    bottomBar.setProgress(100);
                }
            }
        };
        updater.execute();
    }

    private void setupTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBackground(BASE_COLOR);

        newsPanel = new NewsPanel(this);
        packsPanel = new PacksPanel();
        instancesPanel = new InstancesPanel();
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

    private void setupSidebar() {
        bottomBar = new BottomBar();
        
    }

    private void setLookAndFeel() {
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
        UIManager.put("nimbusSelectionBackground", BASE_COLOR);
    }

}