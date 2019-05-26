/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pack {
    private int id;
    private int position;
    private String name;
    private PackType type;
    private String code;
    private List<PackVersion> versions;
    private List<PackVersion> devVersions;
    private boolean createServer;
    private boolean leaderboards;
    private boolean logging;
    private boolean featured;
    private String description;
    private String discordInviteURL;
    private String supportURL;
    private String websiteURL;
    private List<String> testers = new ArrayList<String>();
    private List<String> allowedPlayers = new ArrayList<String>();
    private String xml; // The xml for a version of the pack
    private String xmlVersion; // The version the XML above is for
    private String json; // The JSON for a version of the pack
    private String jsonVersion; // The version the JSON above is for

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets a file safe and URL safe name which simply means replacing all non alpha
     * numerical characters with nothing
     *
     * @return File safe and URL safe name of the pack
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public int getPosition() {
        return this.position;
    }

    public ImageIcon getImage() {
        File imageFile = new File(App.settings.getImagesDir(), getSafeName().toLowerCase() + ".png");
        if (!imageFile.exists()) {
            imageFile = new File(App.settings.getImagesDir(), "defaultimage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    public boolean isPublic() {
        return this.type == PackType.PUBLIC;
    }

    public boolean isSemiPublic() {
        return this.type == PackType.SEMIPUBLIC;
    }

    public boolean isPrivate() {
        return this.type == PackType.PRIVATE;
    }

    public String getCode() {
        if (!isSemiPublic()) {
            return "";
        }
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDiscordInviteURL() {
        return this.discordInviteURL;
    }

    public String getSupportURL() {
        return this.supportURL;
    }

    public String getWebsiteURL() {
        return this.websiteURL;
    }

    public boolean canCreateServer() {
        return this.createServer;
    }

    public boolean isLoggingEnabled() {
        return this.logging;
    }

    public boolean isLeaderboardsEnabled() {
        return this.leaderboards;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public void addTesters(List<String> users) {
        this.testers.addAll(users);
    }

    public void addAllowedPlayers(List<String> users) {
        this.allowedPlayers.addAll(users);
    }

    public List<PackVersion> getVersions() {
        return this.versions;
    }

    public List<PackVersion> getDevVersions() {
        return this.devVersions;
    }

    public void processVersions() {
        for (PackVersion pv : this.versions) {
            pv.setMinecraftVesion();
        }
        for (PackVersion dpv : this.devVersions) {
            dpv.setMinecraftVesion();
        }
    }

    public boolean isTester() {
        Account account = App.settings.getAccount();
        if (account == null) {
            return false;
        }
        for (String tester : this.testers) {
            if (tester.equalsIgnoreCase(account.getMinecraftUsername())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVersions() {
        return this.versions.size() != 0;
    }

    public boolean hasDevVersions() {
        return this.devVersions.size() != 0;
    }

    public boolean canInstall() {
        if (this.type == PackType.PRIVATE) {
            if (isTester() || (hasVersions() && isAllowedPlayer())) {
                return true;
            }
        } else if (this.type == PackType.SEMIPUBLIC) {
            if (isTester() || (hasVersions() && App.settings.canViewSemiPublicPackByCode(this.code))) {
                return true;
            }
        } else {
            if (isTester() || hasVersions()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowedPlayer() {
        if (this.type != PackType.PRIVATE) {
            return true;
        }
        Account account = App.settings.getAccount();
        if (account == null) {
            return false;
        }
        for (String player : this.allowedPlayers) {
            if (player.equalsIgnoreCase(account.getMinecraftUsername())) {
                return true;
            }
        }
        return false;
    }

    public int getVersionCount() {
        return this.versions.size();
    }

    public int getDevVersionCount() {
        return this.devVersions.size();
    }

    public PackVersion getDevVersionByName(String name) {
        if (this.devVersions.size() == 0) {
            return null;
        }

        for (PackVersion devVersion : this.devVersions) {
            if (devVersion.versionMatches(name)) {
                return devVersion;
            }
        }

        return null;
    }

    public PackVersion getVersionByName(String name) {
        if (this.versions.size() == 0) {
            return null;
        }

        for (PackVersion version : this.versions) {
            if (version.versionMatches(name)) {
                return version;
            }
        }

        return null;
    }

    public PackVersion getLatestVersion() {
        if (this.versions.size() == 0) {
            return null;
        }
        return this.versions.get(0);
    }

    public PackVersion getLatestDevVersion() {
        if (this.devVersions.size() == 0) {
            return null;
        }
        return this.devVersions.get(0);
    }

    public boolean isLatestVersionNoUpdate() {
        if (this.versions.size() == 0) {
            return false;
        }
        if (!getLatestVersion().canUpdate()) {
            return true;
        }
        return !getLatestVersion().isRecommended();
    }

    public String getXML(String version) {
        return getXML(version, true);
    }

    public String getXML(String version, boolean redownload) {
        if (this.xml == null || !this.xmlVersion.equalsIgnoreCase(version) || (isTester() && redownload)) {
            String path = "packs/" + getSafeName() + "/versions/" + version + "/Configs.xml";
            Downloadable download = new Downloadable(path, true);
            int tries = 1;
            do {
                this.xml = download.getContents();
                tries++;
            } while (xml == null && tries < 5);
            this.xmlVersion = version;
        }
        return this.xml;
    }

    public String getJSON(String version) {
        return getJSON(version, true);
    }

    public String getJSON(String version, boolean redownload) {
        if (this.json == null || !this.jsonVersion.equalsIgnoreCase(version) || (isTester() && redownload)) {
            String path = "packs/" + getSafeName() + "/versions/" + version + "/Configs.json";
            Downloadable download = new Downloadable(path, true);
            int tries = 1;
            do {
                this.json = download.getContents();
                tries++;
            } while (json == null && tries < 5);
            this.jsonVersion = version;
        }
        return this.json;
    }

    public String getWarningMessage(String version, String name) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("warning");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    if (element.getAttribute("name").equals(name)) {
                        return nodeList1.item(0).getNodeValue();
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getInstallMessage(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("install");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getUpdateMessage(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("update");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public int getMemory(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("memory");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return Integer.parseInt(nodeList1.item(0).getNodeValue());
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return 0;
    }

    public String getMainClass(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("mainclass");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getMainClassDepends(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("mainclass");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("depends")) {
                        return element.getAttribute("depends");
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getMainClassDependsGroup(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("mainclass");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("dependsgroup")) {
                        return element.getAttribute("dependsgroup");
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getExtraArguments(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("extraarguments");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getExtraArgumentsDepends(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("extraarguments");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("depends")) {
                        return element.getAttribute("depends");
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public String getExtraArgumentsDependsGroup(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("extraarguments");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("dependsgroup")) {
                        return element.getAttribute("dependsgroup");
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public int getPermGen(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("permgen");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return Integer.parseInt(nodeList1.item(0).getNodeValue());
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return 0;
    }

    public String getCaseAllFiles(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("caseallfiles");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return nodeList1.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public boolean hasConfigs(String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("noconfigs");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList nodeList1 = element.getChildNodes();
                    return !Boolean.parseBoolean(nodeList1.item(0).getNodeValue());
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return true;
    }

    public String getColour(String version, String name) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("colour");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getAttribute("name").equalsIgnoreCase(name)) {
                        return element.getAttribute("code").replace("#", "");
                    }
                }
            }
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", xml);
            LogManager.logStackTrace("Exception when reading a versions XML. See error details at " + result, e);
        }
        return null;
    }

    public ArrayList<Mod> getMods(String versionToInstall, boolean isServer) {
        ArrayList<Mod> mods = new ArrayList<Mod>(); // ArrayList to hold the mods
        String xml = getXML(versionToInstall, false);
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
                    Color colour = null;
                    if (element.hasAttribute("colour")) {
                        String tempColour = getColour(versionToInstall, element.getAttribute("colour"));
                        if (tempColour != null && tempColour.length() == 6) {
                            int r, g, b;
                            try {
                                r = Integer.parseInt(tempColour.substring(0, 2), 16);
                                g = Integer.parseInt(tempColour.substring(2, 4), 16);
                                b = Integer.parseInt(tempColour.substring(4, 6), 16);
                                colour = new Color(r, g, b);
                            } catch (NumberFormatException e) {
                                colour = null;
                            }
                        }
                    }
                    String warning = null;
                    if (element.hasAttribute("warning")) {
                        warning = element.getAttribute("warning");
                    }
                    String md5 = element.getAttribute("md5");
                    Type type = Type.valueOf(element.getAttribute("type").toLowerCase());
                    ExtractTo extractTo = null;
                    String extractFolder = null;
                    String decompFile = null;
                    DecompType decompType = null;
                    if (type == Type.extract) {
                        extractTo = ExtractTo.valueOf(element.getAttribute("extractto").toLowerCase());
                        if (element.hasAttribute("extractfolder")) {
                            extractFolder = element.getAttribute("extractfolder");
                        } else {
                            extractFolder = "/";
                        }
                    } else if (type == Type.decomp) {
                        decompFile = element.getAttribute("decompfile");
                        decompType = DecompType.valueOf(element.getAttribute("decomptype").toLowerCase());
                    }
                    boolean filePattern = false;
                    if (element.getAttribute("filepattern").equalsIgnoreCase("yes")) {
                        filePattern = true;
                    }
                    String filePreference = null;
                    if (element.hasAttribute("filepreference")) {
                        filePreference = element.getAttribute("filepreference");
                    }
                    String fileCheck = null;
                    if (element.hasAttribute("filecheck")) {
                        fileCheck = element.getAttribute("filecheck");
                    }
                    boolean client = true;
                    if (element.getAttribute("client").equalsIgnoreCase("no")) {
                        client = false;
                        if (!isServer) {
                            continue; // Don't add this mod as its specified as server only
                        }
                    }
                    boolean server = true;
                    String serverURL = null;
                    String serverFile = null;
                    Type serverType = null;
                    Download serverDownload = null;
                    String serverMD5 = null;
                    if (element.getAttribute("server").equalsIgnoreCase("seperate")) {
                        server = false;
                        serverURL = element.getAttribute("serverurl");
                        serverFile = element.getAttribute("serverfile");
                        serverType = Type.valueOf(element.getAttribute("servertype").toLowerCase());
                        serverDownload = Download.valueOf(element.getAttribute("serverdownload").toLowerCase());
                        serverMD5 = element.getAttribute("servermd5");
                    } else if (element.getAttribute("server").equalsIgnoreCase("no")) {
                        server = false;
                        if (isServer) {
                            continue;
                        }
                    }
                    boolean optional = false;
                    if (element.getAttribute("optional").equalsIgnoreCase("yes")) {
                        optional = true;
                    }
                    boolean serverOptional = optional;
                    if (element.getAttribute("serveroptional").equalsIgnoreCase("yes")) {
                        serverOptional = true;
                    } else if (element.getAttribute("serveroptional").equalsIgnoreCase("no")) {
                        serverOptional = false;
                    }
                    boolean selected = false;
                    if (element.hasAttribute("selected")) {
                        if (element.getAttribute("selected").equalsIgnoreCase("yes")) {
                            selected = true;
                        }
                    }
                    Download download = Download.valueOf(element.getAttribute("download").toLowerCase());
                    boolean hidden = false;
                    if (element.getAttribute("hidden").equalsIgnoreCase("yes")) {
                        hidden = true;
                    }
                    boolean library = false;
                    if (element.getAttribute("library").equalsIgnoreCase("yes")) {
                        library = true;
                    }
                    String group = element.getAttribute("group");
                    String category = element.getAttribute("category");
                    String linked = element.getAttribute("linked");
                    String[] depends;
                    if (element.hasAttribute("depends")) {
                        String dependTemp = element.getAttribute("depends");
                        if (dependTemp.contains(",")) {
                            depends = dependTemp.split(",");
                        } else {
                            depends = new String[] { dependTemp };
                        }
                    } else {
                        depends = null;
                    }
                    String filePrefix = element.getAttribute("fileprefix");
                    boolean recommended = true;
                    if (element.getAttribute("recommended").equalsIgnoreCase("no")) {
                        recommended = false;
                    }

                    String description = element.getAttribute("description");
                    mods.add(new Mod(name, version, url, file, website, donation, colour, warning, md5, type, extractTo,
                            extractFolder, decompFile, decompType, filePattern, filePreference, fileCheck, client,
                            server, serverURL, serverFile, serverDownload, serverMD5, serverType, optional,
                            serverOptional, selected, download, hidden, library, group, category, linked, depends,
                            filePrefix, recommended, description));
                }
            }
        } catch (SAXException e) {
            LogManager.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            LogManager.logStackTrace(e);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return mods;
    }

    public boolean hasDeleteArguments(boolean getFiles, String version) {
        String xml = getXML(version, false);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("delete");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList nodeListInside = node.getChildNodes();
                for (int j = 0; j < nodeListInside.getLength(); j++) {
                    Node nodeInside = nodeListInside.item(j);
                    if (nodeInside.getNodeType() == Node.ELEMENT_NODE) {
                        if (nodeInside.getNodeName().equalsIgnoreCase((getFiles ? "file" : "folder"))) {
                            return true;
                        }
                    }
                }
            }
        } catch (SAXException e) {
            LogManager.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            LogManager.logStackTrace(e);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return false;
    }

    public List<File> getDeletes(boolean getFiles, String version, Instance instance) {
        String xml = getXML(version, false);
        List<File> files = new ArrayList<File>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("delete");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList nodeListInside = node.getChildNodes();
                for (int j = 0; j < nodeListInside.getLength(); j++) {
                    Node nodeInside = nodeListInside.item(j);
                    if (nodeInside.getNodeType() == Node.ELEMENT_NODE) {
                        if (nodeInside.getNodeName().equalsIgnoreCase((getFiles ? "file" : "folder"))) {
                            Element element = (Element) nodeInside;
                            File file = new File(instance.getRootDirectory(),
                                    element.getAttribute("target").replace("%s%", File.separator));
                            if (element.getAttribute("base").equalsIgnoreCase("root")) {
                                if (element.getAttribute("target").startsWith("world")
                                        || element.getAttribute("target").startsWith("DIM")
                                        || element.getAttribute("target").startsWith("saves")
                                        || element.getAttribute("target").startsWith("instance.json")
                                        || element.getAttribute("target").contains("./")
                                        || element.getAttribute("target").contains(".\\")
                                        || element.getAttribute("target").contains("~/")
                                        || element.getAttribute("target").contains("~\\") || !file.getCanonicalPath()
                                                .contains(instance.getRootDirectory().getCanonicalPath())) {
                                    LogManager.error("Cannot delete the file/folder " + file.getAbsolutePath() + " as"
                                            + " it's protected.");
                                } else {
                                    files.add(file);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SAXException e) {
            LogManager.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            LogManager.logStackTrace(e);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return files;
    }

    public String addInstall(String version) {
        Map<String, Object> request = new HashMap<String, Object>();

        request.put("username", App.settings.getAccount().getMinecraftUsername());
        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/installed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }

    public String addServerInstall(String version) {
        Map<String, Object> request = new HashMap<String, Object>();

        request.put("username", App.settings.getAccount().getMinecraftUsername());
        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/serverinstalled/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }

    public String addUpdate(String version) {
        Map<String, Object> request = new HashMap<String, Object>();

        request.put("username", App.settings.getAccount().getMinecraftUsername());
        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + getSafeName() + "/updated/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Install Not Added!";
    }
}
