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
package com.atlauncher.data;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.atlauncher.gui.InstancesPanel;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;
import com.atlauncher.workers.AddonLoader;
import com.atlauncher.workers.LanguageLoader;
import com.atlauncher.workers.PackLoader;

public class Settings {

    // User Settings
    private Language language;
    private Server server;
    private int ram;
    private int windowWidth;
    private int windowHeight;
    private String javaParamaters;
    private boolean enableConsole;
    private boolean enableLeaderboards;
    private boolean enableLogs;

    // Pack and Instances
    private Packs packs;
    private Instances instances;
    private Addons addons;

    // Launcher Settings
    private JFrame parent;
    private ArrayList<Language> languages = new ArrayList<Language>();
    private Server[] servers;
    private InstancesPanel instancesPanel;

    public Settings(JFrame parent) {
        this.parent = parent;
        this.packs = new Packs();
        this.instances = new Instances();
        this.addons = new Addons();
        setupDefaultData();
        loadLanguages();
        loadPacks();
        loadAddons();
    }

    private void loadLanguages() {
        LanguageLoader languageLoader = new LanguageLoader() {
            @Override
            protected void process(List<Language> chunks) {
                Language got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Language " + got.getName()
                        + " (" + got.getLocalizedName() + ")");
                languages.add(got);
            }
        };
        languageLoader.execute();
    }

    private void loadPacks() {
        PackLoader packLoader = new PackLoader() {
            @Override
            protected void process(List<Pack> chunks) {
                Pack got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Pack " + got.getName());
                packs.add(got);
            }
        };
        packLoader.execute();
    }

    private void loadAddons() {
        AddonLoader addonLoader = new AddonLoader() {
            @Override
            protected void process(List<Addon> chunks) {
                Addon got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Addon " + got.getName());
                addons.add(got);
            }
        };
        addonLoader.execute();
    }

    private void setupDefaultData() {
        this.servers = new Server[] {
                new Server("Europe", "eu.atlauncher.com"),
                new Server("US East", "useast.atlauncher.com"),
                new Server("US West", "uswest.atlauncher.com") };
    }

    public Packs getPacks() {
        return this.packs;
    }

    public Instances getInstances() {
        return this.instances;
    }

    public Addons getAddons() {
        return this.addons;
    }

    public ArrayList<Language> getLanguages() {
        return this.languages;
    }

    public Server[] getServers() {
        return this.servers;
    }

    public String[] getMemoryOptions() {
        int options = Utils.getMaximumRam() / 512;
        int ramLeft = 0;
        int count = 0;
        String[] ramOptions = new String[options];
        while ((ramLeft + 512) <= Utils.getMaximumRam()) {
            ramLeft = ramLeft + 512;
            ramOptions[count] = ramLeft + " MB";
            count++;
        }
        return ramOptions;
    }

    public Window getParent() {
        return this.parent;
    }

    public void setInstancesPanel(InstancesPanel instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    public void reloadTable() {
        this.instancesPanel.reloadTable();
    }
}
