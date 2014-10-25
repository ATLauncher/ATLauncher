/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.TabChangeManager;
import com.atlauncher.gui.components.LauncherBottomBar;
import com.atlauncher.gui.layer.BlurLayer;
import com.atlauncher.gui.tabs.AccountsTab;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.SettingsTab;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.gui.tabs.ToolsTab;
import com.atlauncher.utils.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public final class LauncherFrame
extends JFrame
implements RelocalizationListener{
    private JTabbedPane tabbedPane;
    private NewsTab newsTab;
    private PacksTab packsTab;
    private InstancesTab instancesTab;
    private AccountsTab accountsTab;
    private ToolsTab toolsTab;
    private SettingsTab settingsTab;

    public final BlurLayer blur = new BlurLayer();

    private List<Tab> tabs;

    private LauncherBottomBar bottomBar;

    public LauncherFrame(boolean show) {
        LogManager.info("Launcher opening");
        LogManager.info("Made By Bob*");
        LogManager.info("*(Not Actually)");

        App.settings.setParentFrame(this);
        setSize(new Dimension(1000, 575));
        setTitle("ATLauncher " + Constants.VERSION);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));

        LogManager.info("Setting up Bottom Bar");
        setupBottomBar(); // Setup the Bottom Bar
        LogManager.info("Finished Setting up Bottom Bar");

        LogManager.info("Setting up Tabs");
        setupTabs(); // Setup the JTabbedPane
        LogManager.info("Finished Setting up Tabs");

        JPanel content = new JPanel(new BorderLayout());
        this.add(new JLayer<JPanel>(content, this.blur));

        content.add(tabbedPane, BorderLayout.CENTER);
        content.add(bottomBar, BorderLayout.SOUTH);

        if (show) {
            LogManager.info("Showing Launcher");
            setVisible(true);
        }

        RelocalizationManager.addListener(this);

        App.TASKPOOL.execute(new Runnable(){
            public void run(){
                App.settings.checkMojangStatus(); // Check Minecraft status
                bottomBar.updateStatus(App.settings.getMojangStatus());
            }
        });
    }

    public void updateTitle(String str){
        setTitle("ATLauncher " + Constants.VERSION + " - " + str);
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
        App.settings.setPacksPanel(packsTab);
        instancesTab = new InstancesTab();
        App.settings.setInstancesPanel(instancesTab);
        accountsTab = new AccountsTab();
        toolsTab = new ToolsTab();
        settingsTab = new SettingsTab();

        this.tabs = Arrays.asList(new Tab[]{newsTab, packsTab, instancesTab, accountsTab, toolsTab, settingsTab});

        tabbedPane.setFont(App.THEME.getTabFont().deriveFont(34.0F));
        for (Tab tab : this.tabs) {
            this.tabbedPane.addTab(tab.getTitle(), (JPanel) tab);
        }
        tabbedPane.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                String tabName = ((Tab) tabbedPane.getSelectedComponent()).getTitle();
                if(tabbedPane.getSelectedIndex() == 1){
                    updateTitle("Packs - " + App.settings.getPackInstallableCount());
                } else{
                    updateTitle(tabName);
                }

                TabChangeManager.post();
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
        App.settings.setBottomBar(bottomBar);
    }

    @Override
    public void onRelocalization() {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            this.tabbedPane.setTitleAt(i, this.tabs.get(i).getTitle());
        }
    }
}