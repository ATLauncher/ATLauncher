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

import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.InstancesPanel;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.workers.AddonLoader;
import com.atlauncher.workers.LanguageLoader;
import com.atlauncher.workers.PackLoader;

/**
 * Settings class for storing all data for the Launcher and the settings of the user
 * 
 * @author Ryan
 */
public class Settings {

    // Users Settings
    private Language language; // Language for the Launcher
    private Server server; // Server to use for the Launcher
    private int ram; // RAM to use when launching Minecraft
    private int windowWidth; // Width of the Minecraft window
    private int windowHeight; // Height of the Minecraft window
    private String javaParamaters; // Extra Java paramaters when launching Minecraft
    private boolean enableConsole; // If to show the console by default
    private boolean enableLeaderboards; // If to enable the leaderboards
    private boolean enableLogs; // If to enable logs

    // Packs, Addons and Instances
    private ArrayList<Pack> packs = new ArrayList<Pack>(); // Packs in the Launcher
    private ArrayList<Instance> instances = new ArrayList<Instance>(); // Users Installed Instances
    private ArrayList<Addon> addons = new ArrayList<Addon>(); // Addons in the Launcher

    // Launcher Settings
    private JFrame parent; // The Main JFrame of the Launcher
    private ArrayList<Language> languages = new ArrayList<Language>(); // Languages for the Launcher
    private ArrayList<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
    private InstancesPanel instancesPanel; // The instances panel

    public Settings(JFrame parent) {
        this.parent = parent; // Set the Launcher JFrame
        setupServers(); // Setup the servers available to use in the Launcher
        loadLanguages(); // Load the Languages available in the Launcher
        loadPacks(); // Load the Packs available in the Launcher
        loadAddons(); // Load the Addons available in the Launcher
    }

    /**
     * The servers available to use in the Launcher
     * 
     * These MUST be hardcoded in order for the Launcher to make the initial connections to download
     * files
     */
    private void setupServers() {
        servers.add(new Server("Europe", "eu.atlauncher.com"));
        servers.add(new Server("US East", "useast.atlauncher.com"));
        servers.add(new Server("US West", "uswest.atlauncher.com"));
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadLanguages() {
        // Setup the worker process
        LanguageLoader languageLoader = new LanguageLoader() {
            protected void process(List<Language> chunks) {
                Language got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Language " + got.getName() + " ("
                        + got.getLocalizedName() + ")");
                languages.add(got);
            }
        };
        languageLoader.execute(); // Run the worker process
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    private void loadPacks() {
        // Setup the worker process
        PackLoader packLoader = new PackLoader() {
            protected void process(List<Pack> chunks) {
                Pack got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Pack " + got.getName());
                packs.add(got);
            }
        };
        packLoader.execute(); // Run the worker process
    }

    /**
     * Loads the Addons for use in the Launcher
     */
    private void loadAddons() {
        // Setup the worker process
        AddonLoader addonLoader = new AddonLoader() {
            protected void process(List<Addon> chunks) {
                Addon got = chunks.get(chunks.size() - 1);
                LauncherFrame.console.log("Loaded Addon " + got.getName());
                addons.add(got);
            }
        };
        addonLoader.execute(); // Run the worker process
    }

    /**
     * Get the Packs available in the Launcher
     * 
     * @return The Packs available in the Launcher
     */
    public ArrayList<Pack> getPacks() {
        return this.packs;
    }

    /**
     * Get the Instances available in the Launcher
     * 
     * @return The Instances available in the Launcher
     */
    public ArrayList<Instance> getInstances() {
        return this.instances;
    }

    /**
     * Get the Addons available in the Launcher
     * 
     * @return The Addons available in the Launcher
     */
    public ArrayList<Addon> getAddons() {
        return this.addons;
    }

    /**
     * Get the Languages available in the Launcher
     * 
     * @return The Languages available in the Launcher
     */
    public ArrayList<Language> getLanguages() {
        return this.languages;
    }

    /**
     * Get the Servers available in the Launcher
     * 
     * @return The Servers available in the Launcher
     */
    public ArrayList<Server> getServers() {
        return this.servers;
    }

    /**
     * Returns the JFrame reference of the main Launcher
     * 
     * @return Main JFrame of the Launcher
     */
    public Window getParent() {
        return this.parent;
    }

    /**
     * Sets the Panel used for Instances
     * 
     * @param instancesPanel
     *            Instances Panel
     */
    public void setInstancesPanel(InstancesPanel instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    /**
     * Reloads the Instances Panel table
     */
    public void reloadTable() {
        this.instancesPanel.reloadTable();
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     * 
     * @param name
     *            The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public boolean isInstance(String name) {
        for (Instance instance : instances) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given ID number
     * 
     * @param id
     *            ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack
     *             If ID is not found
     */
    public Pack getPackByID(int id) throws InvalidPack {
        for (Pack pack : packs) {
            if (pack.getID() == id) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with ID " + id);
    }
}
