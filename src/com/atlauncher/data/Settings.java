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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.InstancesPanel;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.Utils;

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

    // Directories and Files for the Launcher
    private File baseDir = new File(System.getProperty("user.dir"));
    private File backupsDir = new File(baseDir, "Backups");
    private File configsDir = new File(baseDir, "Configs");
    private File imagesDir = new File(configsDir, "Images");
    private File skinsDir = new File(imagesDir, "Skins");
    private File jarsDir = new File(configsDir, "Jars");
    private File downloadsDir = new File(baseDir, "Downloads");
    private File instancesDir = new File(baseDir, "Instances");
    private File tempDir = new File(baseDir, "Temp");
    private File instancesFile = new File(getConfigsDir(), "instances.xml");
    private File propertiesFile = new File(baseDir, "ATLauncher.conf"); // File for properties

    // Launcher Settings
    private JFrame parent; // Parent JFrame of the actual Launcher
    private Properties properties = new Properties(); // Properties to store everything in
    private LauncherConsole console = new LauncherConsole(); // The Launcher's Console
    private ArrayList<Language> languages = new ArrayList<Language>(); // Languages for the Launcher
    private ArrayList<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
    private InstancesPanel instancesPanel; // The instances panel
    private boolean firstTimeRun = false; // If this is the first time the Launcher has been run
    private Server bestConnectedServer; // The best connected server for Auto selection
    private boolean offlineMode = false; // If offline mode is enabled

    public Settings() {
        checkFolders(); // Checks the setup of the folders and makes sure they're there
    }

    public void loadEverything() {
        setupServers(); // Setup the servers available to use in the Launcher
        testServers(); // Test servers for best connected one
        loadServerProperty(); // Get users Server preference
        if (!isInOfflineMode()) {
            checkForUpdatedFiles(); // Checks for updated files on the server
        }
        loadLanguages(); // Load the Languages available in the Launcher
        loadPacks(); // Load the Packs available in the Launcher
        loadAddons(); // Load the Addons available in the Launcher
        loadInstances(); // Load the users installed Instances
        loadProperties(); // Load the users Properties
    }

    /**
     * This checks the servers hashes.xml file and downloads and new/updated files that differ from
     * what the user has
     */
    private void checkForUpdatedFiles() {
        String hashes = new Downloader(getFileURL("launcher/hashes.xml")).run();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(hashes)));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("hash");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String type = element.getAttribute("type");
                    String md5 = element.getAttribute("md5");
                    File file = null;
                    if (type.equalsIgnoreCase("Root")) {
                        file = new File(configsDir, name);
                    } else if (type.equalsIgnoreCase("Images")) {
                        file = new File(imagesDir, name);
                        name = "images/" + name;
                    }
                    boolean download = false; // If we have to download the file or not
                    if (!file.exists()) {
                        download = true; // File doesn't exist so download it
                    } else {
                        if (!Utils.getMD5(file).equalsIgnoreCase(md5)) {
                            download = true; // MD5 hashes don't match so download it
                        }
                    }

                    if (download) {
                        new Downloader(getFileURL("launcher/" + name), file.getAbsolutePath())
                                .runNoReturn();
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the directory to make sure all the necessary folders are there
     */
    private void checkFolders() {
        File[] files = { backupsDir, configsDir, imagesDir, skinsDir, jarsDir, downloadsDir,
                instancesDir, tempDir };
        for (File file : files) {
            if (!file.exists()) {
                console.log("Folder " + file.getAbsolutePath() + " doesn't exist. Creating now");
                file.mkdir();
            }
        }
        if (!instancesFile.exists()) {
            try {
                FileWriter fw = new FileWriter(instancesFile);
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><instances></instances>");
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the base directory
     * 
     * @return File object for the base directory
     */
    public File getBaseDir() {
        return this.baseDir;
    }

    /**
     * Returns the backups directory
     * 
     * @return File object for the backups directory
     */
    public File getBackupsDir() {
        return this.backupsDir;
    }

    /**
     * Returns the configs directory
     * 
     * @return File object for the configs directory
     */
    public File getConfigsDir() {
        return this.configsDir;
    }

    /**
     * Returns the images directory
     * 
     * @return File object for the images directory
     */
    public File getImagesDir() {
        return this.imagesDir;
    }

    /**
     * Returns the skins directory
     * 
     * @return File object for the skins directory
     */
    public File getSkinsDir() {
        return this.skinsDir;
    }

    /**
     * Returns the jars directory
     * 
     * @return File object for the jars directory
     */
    public File getJarsDir() {
        return this.jarsDir;
    }

    /**
     * Returns the downloads directory
     * 
     * @return File object for the downloads directory
     */
    public File getDownloadsDir() {
        return this.downloadsDir;
    }

    /**
     * Returns the instances directory
     * 
     * @return File object for the instances directory
     */
    public File getInstancesDir() {
        return this.instancesDir;
    }

    /**
     * Returns the temp directory
     * 
     * @return File object for the temp directory
     */
    public File getTempDir() {
        return this.tempDir;
    }

    /**
     * Returns the instances.xml file
     * 
     * @return File object for the instances.xml file
     */
    public File getInstancesFile() {
        return instancesFile;
    }

    /**
     * Sets the main parent JFrame reference for the Launcher
     * 
     * @param parent
     *            The Launcher main JFrame
     */
    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Load the users Server preference from file
     */
    public void loadServerProperty() {
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            String serv = properties.getProperty("server", "Auto");
            if (isServerByName(serv)) {
                this.server = getServerByName(serv);
            } else {
                console.log("Server " + serv + " is invalid");
                this.server = getServerByName("Auto"); // Server not found, use default of Auto
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the properties from file
     */
    public void loadProperties() {
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            this.firstTimeRun = Boolean
                    .parseBoolean(properties.getProperty("firsttimerun", "true"));

            String lang = properties.getProperty("language", "English");
            if (isLanguageByName(lang)) {
                this.language = getLanguageByName(lang);
            } else {
                console.log("Language " + lang + " is invalid");
                this.language = getLanguageByName("English"); // Language not found, use default
            }

            this.ram = Integer.parseInt(properties.getProperty("ram", "512"));
            if (this.ram > Utils.getMaximumRam()) {
                console.log("Cannot allocate " + this.ram + "MB of Ram");
                this.ram = 512; // User tried to allocate too much ram, set it back to 0.5GB
            }

            this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
            if (this.windowWidth > Utils.getMaximumWindowWidth()) {
                console.log("Cannot set screen width to " + this.windowWidth);
                this.windowWidth = 854; // User tried to make screen size wider than they have
            }

            this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));
            if (this.windowHeight > Utils.getMaximumWindowHeight()) {
                console.log("Cannot set screen height to " + this.windowHeight);
                this.windowHeight = 480; // User tried to make screen size wider than they have
            }

            this.javaParamaters = properties.getProperty("javaparameters", "");

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                    "true"));

            this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty(
                    "enableleaderboards", "false"));

            this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the properties to file
     */
    public void saveProperties() {
        try {
            properties.setProperty("firsttimerun", "false");
            properties.setProperty("language", this.language.getName());
            properties.setProperty("server", this.server.getName());
            properties.setProperty("ram", this.ram + "");
            properties.setProperty("windowwidth", this.windowWidth + "");
            properties.setProperty("windowheight", this.windowHeight + "");
            properties.setProperty("javaparameters", this.javaParamaters);
            properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
            properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                    : "false");
            properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            this.properties.store(new FileOutputStream(propertiesFile), "ATLauncher Settings");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The servers available to use in the Launcher
     * 
     * These MUST be hardcoded in order for the Launcher to make the initial connections to download
     * files
     */
    private void setupServers() {
        servers.add(new Server("Auto", ""));
        servers.add(new Server("Europe", "eu.atlcdn.net"));
        servers.add(new Server("US East", "useast.atlcdn.net"));
        servers.add(new Server("US West", "uswest.atlcdn.net"));
    }

    /**
     * Tests the servers for availability and best connection
     */
    private void testServers() {
        double[] responseTimes = new double[servers.size()];
        int count = 0;
        int up = 0;
        for (Server server : servers) {
            if (server.isAuto())
                continue; // Don't scan the Auto server
            double startTime = System.currentTimeMillis();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(server.getTestURL())
                        .openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(3000);
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    responseTimes[count] = 1000000.0;
                    console.log("Server " + server.getName() + " isn't available!");
                    server.disableServer();
                } else {
                    double endTime = System.currentTimeMillis();
                    responseTimes[count] = endTime - startTime;
                    console.log("Server " + server.getName() + " is available! ("
                            + responseTimes[count] + ")");
                    up++;
                }
            } catch (SocketTimeoutException e) {
                responseTimes[count] = 1000000.0;
                console.log("Server " + server.getName() + " isn't available!");
                server.disableServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
        int best = 0;
        double bestTime = 10000000.0;
        for (int i = 0; i < responseTimes.length; i++) {
            if (responseTimes[i] < bestTime) {
                best = i;
                bestTime = responseTimes[i];
            }
        }
        if (up != 0) {
            console.log("The best connected server is " + servers.get(best).getName());
            this.bestConnectedServer = servers.get(best);
        } else {
            JOptionPane.showMessageDialog(null,
                    "<html><center>There was an issue connecting to ATLauncher "
                            + "Servers<br/><br/>Offline mode is now enabled.<br/><br/>"
                            + "To install packs again, please try connecting later"
                            + "</center></html>", "Error Connecting To ATLauncher Servers",
                    JOptionPane.ERROR_MESSAGE);
            this.offlineMode = true; // Set offline mode to be true
        }
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadLanguages() {
        Language language;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "languages.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("language");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String localizedName = element.getAttribute("localizedname");
                    String file = element.getAttribute("file");
                    String author = element.getAttribute("author");
                    language = new Language(name, localizedName, file, author);
                    languages.add(language);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    private void loadPacks() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "packs.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("pack");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    String name = element.getAttribute("name");
                    String[] versions;
                    if (element.getAttribute("versions").isEmpty()) {
                        // Pack has no versions so log it and continue to next pack
                        getConsole().log("Pack " + name + " has no versions!");
                        continue;
                    } else {
                        versions = element.getAttribute("versions").split(",");
                    }
                    String[] minecraftversions;
                    if (element.getAttribute("minecraftversions").isEmpty()) {
                        // Pack has no versions so log it and continue to next pack
                        getConsole().log("Pack " + name + " has no minecraftversions!");
                        continue;
                    } else {
                        minecraftversions = element.getAttribute("minecraftversions").split(",");
                    }
                    String description = element.getAttribute("description");
                    Pack pack = new Pack(id, name, versions, minecraftversions, description);
                    packs.add(pack);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the Addons for use in the Launcher
     */
    private void loadAddons() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "addons.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("addon");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    String name = element.getAttribute("name");
                    String[] versions;
                    if (element.getAttribute("versions").isEmpty()) {
                        // Pack has no versions so log it and continue to next
                        // pack
                        getConsole().log("Addon " + name + " has no versions!");
                        continue;
                    } else {
                        versions = element.getAttribute("versions").split(",");
                    }
                    String description = element.getAttribute("description");
                    Pack forPack;
                    Pack pack = getPackByID(id);
                    if (pack != null) {
                        forPack = pack;
                    } else {
                        getConsole().log("Addon " + name + " is not available for any packs!");
                        continue;
                    }
                    Addon addon = new Addon(id, name, versions, description, forPack);
                    addons.add(addon);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPack e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the user installed Instances
     */
    private void loadInstances() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(instancesFile);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("instance");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String pack = element.getAttribute("pack");
                    String version = element.getAttribute("version");
                    String minecraftVersion = element.getAttribute("minecraftversion");
                    String jarOrder = element.getAttribute("jarorder");
                    Instance instance = new Instance(name, pack, version, minecraftVersion,
                            jarOrder);
                    instances.add(instance);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the MD5 hash for a minecraft.jar or minecraft_server.jar
     */
    public String getMinecraftHash(String root, String version, String type) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "minecraft.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName(root);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getAttribute("version").equalsIgnoreCase(version)) {
                        if (type.equalsIgnoreCase("client")) {
                            return element.getAttribute("client");
                        } else {
                            return element.getAttribute("server");
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds out if this is the first time the Launcher has been run
     * 
     * @return true if the Launcher hasn't been run and setup yet, false for otherwise
     */
    public boolean isFirstTimeRun() {
        return this.firstTimeRun;
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
     * Adds an instance to the Launcher and saves it to the instances.xml file
     */
    public void addInstance(String name, String pack, String version, String minecraftVersion,
            String jarOrder) {
        try {
            // Open the file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(getInstancesFile());
            Node instances = document.getFirstChild();

            // Create the instances element
            Element instance = document.createElement("instance");
            instance.setAttribute("name", name);
            instance.setAttribute("pack", pack);
            instance.setAttribute("version", version);
            instance.setAttribute("minecraft", minecraftVersion);
            instance.setAttribute("jarorder", jarOrder);
            instances.appendChild(instance);

            // Save it all back to original file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(getInstancesFile());
            transformer.transform(source, result);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        this.instances.add(new Instance(name, pack, version, minecraftVersion, jarOrder)); // Add It
    }

    /**
     * Removes an instance from the Launcher
     */
    public void removeInstance(Instance instance) {
        boolean found = false;
        try {
            // Open the file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(getInstancesFile());
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("instance");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    if (name.equalsIgnoreCase(instance.getName())) {
                        found = true; // Found it
                        node.getParentNode().removeChild(node);
                    }
                }
            }

            if (found) {
                // Save it all back to original file if found
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(getInstancesFile());
                transformer.transform(source, result);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        if (found) {
            Utils.delete(instance.getRootDirectory());
            this.instances.remove(instance); // Remove the instance
            reloadInstancesPanel(); // Reload the instances panel
        }
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
     * Determines if offline mode is enabled or not
     * 
     * @return true if offline mode is enabled, false otherwise
     */
    public boolean isInOfflineMode() {
        return this.offlineMode;
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
     * Sets the panel used for Instances
     * 
     * @param instancesPanel
     *            Instances Panel
     */
    public void setInstancesPanel(InstancesPanel instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    /**
     * Reloads the panel used for Instances
     */
    public void reloadInstancesPanel() {
        this.instancesPanel.reload(); // Reload the instances panel
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

    /**
     * Finds a Pack from the given name
     * 
     * @param name
     *            name of the Pack to find
     * @return Pack if the pack is found from the name
     * @throws InvalidPack
     *             If name is not found
     */
    public Pack getPackByName(String name) throws InvalidPack {
        for (Pack pack : packs) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with name " + name);
    }

    /**
     * Finds a Language from the given name
     * 
     * @param name
     *            Name of the Language to find
     * @return Language if the language is found from the name
     */
    private Language getLanguageByName(String name) {
        for (Language language : languages) {
            if (language.getName().equalsIgnoreCase(name)) {
                return language;
            }
        }
        return null;
    }

    /**
     * Finds a Language from the given name
     * 
     * @param name
     *            Name of the Language to find
     * @return Language if the language is found from the name
     */
    private Server getServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Finds if a language is available
     * 
     * @param name
     *            The name of the Language
     * @return true if found, false if not
     */
    public boolean isLanguageByName(String name) {
        for (Language language : languages) {
            if (language.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if a server is available
     * 
     * @param name
     *            The name of the Server
     * @return true if found, false if not
     */
    public boolean isServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the URL for a file on the user selected server
     * 
     * @param filename
     *            Filename including directories on the server
     * @return URL of the file
     */
    public String getFileURL(String filename) {
        return this.server.getFileURL(filename, bestConnectedServer);
    }

    /**
     * Gets the Launcher's current Console instance
     * 
     * @return The Launcher's Console instance
     */
    public LauncherConsole getConsole() {
        return this.console;
    }

    /**
     * Returns the best connected server
     * 
     * @return The server that the user was best connected to
     */
    public Server getBestConnectedServer() {
        System.out.println("hi");
        return this.bestConnectedServer;
    }

    /**
     * Gets the users current active Language
     * 
     * @return The users set language
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Sets the users current active Language
     * 
     * @param language
     *            The language to set to
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Gets the users current active Server
     * 
     * @return The users set server
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Sets the users current active Server
     * 
     * @param server
     *            The server to set to
     */
    public void setServer(Server server) {
        this.server = server;
    }

    public int getMemory() {
        return this.ram;
    }

    public void setMemory(int memory) {
        this.ram = memory;
    }

    public int getWindowWidth() {
        return this.windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return this.windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public String getJavaParameters() {
        return this.javaParamaters;
    }

    public void setJavaParameters(String javaParamaters) {
        this.javaParamaters = javaParamaters;
    }

    /**
     * If the user has selected to show the console always or not
     * 
     * @return true if yes, false if not
     */
    public boolean enableConsole() {
        return this.enableConsole;
    }

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public boolean enableLeaderboards() {
        return this.enableLeaderboards;
    }

    public void setEnableLeaderboards(boolean enableLeaderboards) {
        this.enableLeaderboards = enableLeaderboards;
    }

    public boolean enableLogs() {
        return this.enableLogs;
    }

    public void setEnableLogs(boolean enableLogs) {
        this.enableLogs = enableLogs;
    }

}
