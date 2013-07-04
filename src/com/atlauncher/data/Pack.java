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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class Pack {

    private int id;
    private String name;
    private boolean createServer;
    private String[] versions;
    private String[] testers;
    private String description;
    private String supportURL;
    private String websiteURL;

    private String xml; // The XML
    private String xmlVersion; // The version the XML is for

    public Pack(int id, String name, boolean createServer, String[] versions, String[] testers,
            String description, String supportURL, String websiteURL) {
        this.id = id;
        this.name = name;
        this.createServer = createServer;
        this.versions = versions;
        this.testers = testers;
        this.description = description;
        this.supportURL = supportURL;
        this.websiteURL = websiteURL;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ImageIcon getImage() {
        File imageFile = new File(LauncherFrame.settings.getImagesDir(), getSafeName()
                .toLowerCase() + ".png");
        if (!imageFile.exists()) {
            imageFile = new File(LauncherFrame.settings.getImagesDir(), "defaultimage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    /**
     * Gets a file safe and URL safe name which simply means replacing all non alpha numerical
     * characters with nothing
     * 
     * @return File safe and URL safe name of the pack
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String[] getVersions() {
        return this.versions;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSupportURL() {
        return this.supportURL;
    }

    public String getWebsiteURL() {
        return this.websiteURL;
    }

    public int getVersionCount() {
        return this.versions.length;
    }

    public String getVersion(int index) {
        return this.versions[index];
    }

    public String getXML(String version) {
        if (this.xml == null || !this.xmlVersion.equalsIgnoreCase(version) || isTester()) {
            String path = "packs/" + getSafeName() + "/versions/" + version + "/Configs.xml";
            String versionURL = LauncherFrame.settings.getFileURL(path); // The XML with path on
                                                                         // server
            this.xml = Utils.urlToString(versionURL);
            this.xmlVersion = version;
        }
        return this.xml;
    }

    public String getMinecraftVersion(String version) {
        String xml = getXML(version);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("minecraft");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
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

    public ArrayList<Mod> getMods(String versionToInstall) {
        ArrayList<Mod> mods = new ArrayList<Mod>(); // ArrayList to hold the mods
        String xml = getXML(versionToInstall);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("mod");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String version = element.getAttribute("version");
                    String url = element.getAttribute("url");
                    String file = element.getAttribute("file");
                    String website = element.getAttribute("website");
                    String donation = element.getAttribute("donation");
                    String md5 = element.getAttribute("md5");
                    Type type = Type.valueOf(element.getAttribute("type").toLowerCase());
                    ExtractTo extractTo = null;
                    String decompFile = null;
                    DecompType decompType = null;
                    if (type == Type.extract) {
                        extractTo = ExtractTo.valueOf(element.getAttribute("extractto")
                                .toLowerCase());
                    } else if (type == Type.decomp) {
                        decompFile = element.getAttribute("decompFile");
                        decompType = DecompType.valueOf(element.getAttribute("decomptype")
                                .toLowerCase());
                    }
                    boolean server = false;
                    String serverURL = null;
                    String serverFile = null;
                    Type serverType = null;
                    if (element.getAttribute("server").equalsIgnoreCase("seperate")) {
                        server = true;
                        serverURL = element.getAttribute("serverurl");
                        serverFile = element.getAttribute("serverfile");
                        serverType = Type.valueOf(element.getAttribute("servertype").toLowerCase());
                    }
                    boolean optional = false;
                    if (element.getAttribute("optional").equalsIgnoreCase("yes")) {
                        optional = true;
                    }
                    boolean directDownload = false;
                    if (element.getAttribute("directdownload").equalsIgnoreCase("yes")) {
                        directDownload = true;
                    }
                    String linked = element.getAttribute("linked");
                    String description = element.getAttribute("description");
                    mods.add(new Mod(name, version, url, file, website, donation, md5, type,
                            extractTo, decompFile, decompType, server, serverURL, serverFile,
                            serverType, optional, directDownload, linked, description));
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mods;
    }

    public boolean hasVersions() {
        if (this.versions.length == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasTesters() {
        if (this.testers.length == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isTester() {
        Account account = LauncherFrame.settings.getAccount();
        if (account == null) {
            return false;
        }
        for (String tester : testers) {
            if (tester.equalsIgnoreCase(account.getMinecraftUsername())) {
                return true;
            }
        }
        return false;
    }

    public boolean canInstall() {
        if (hasVersions() || isTester()) {
            return true;
        }
        return false;
    }

    public boolean canCreateServer() {
        return this.createServer;
    }

    public boolean isNewInstallMethod(String version) {
        if (LauncherFrame.settings.getMinecraftInstallMethod(getMinecraftVersion(version))
                .equalsIgnoreCase("new")) {
            return true;
        } else {
            return false;
        }
    }

    public String getLatestVersion() {
        return this.versions[0];
    }
}
