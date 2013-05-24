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

import javax.swing.JFrame;

import com.atlauncher.gui.InstancesPanel;
import com.atlauncher.gui.Utils;

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
    private Language[] languages;
    private Server[] servers;
    private InstancesPanel instancesPanel;

    public Settings(JFrame parent) {
        this.parent = parent;
        this.packs = new Packs();
        this.instances = new Instances();
        this.addons = new Addons();
        setupDefaultData();
    }

    private void setupDefaultData() {
        this.languages = new Language[] { new Language("English", "English"),
                new Language("Polish", "Polski") };
        this.servers = new Server[] {
                new Server("Europe", "eu.atlauncher.com"),
                new Server("US East", "useast.atlauncher.com"),
                new Server("US West", "uswest.atlauncher.com") };

        Version[] versions = { new Version(1, 1, 0), new Version(1, 1, 1),
                new Version(1, 1, 2) };
        Version mcVersion = new Version(1, 5, 1);
        Pack astockyPack = new Pack(
                1,
                "Astocky Pack",
                new Player("astocky"),
                versions,
                mcVersion,
                "Astocky Pack is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack herocraftReloaded = new Pack(
                2,
                "HeroCraft Reloaded",
                new Player("dwinget2008"),
                versions,
                mcVersion,
                "HeroCraft Reloaded is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack solitaryCraft = new Pack(
                3,
                "SolitaryCraft",
                new Player("haighyorkie"),
                versions,
                mcVersion,
                "SolitaryCraft is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack theAllmightyPack = new Pack(
                4,
                "The Allmighty Pack",
                new Player("RyanTheAllmighty"),
                versions,
                mcVersion,
                "The Allmighty Pack is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        this.packs.addPack(astockyPack);
        this.packs.addPack(herocraftReloaded);
        this.packs.addPack(solitaryCraft);
        this.packs.addPack(theAllmightyPack);
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

    public Language[] getLanguages() {
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
