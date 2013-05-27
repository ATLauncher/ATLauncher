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
package com.atlauncher.workers;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.atlauncher.data.Addon;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Player;
import com.atlauncher.data.Version;
import com.atlauncher.gui.LauncherFrame;

public class AddonLoader extends SwingWorker<Void, Addon> {

    @Override
    protected Void doInBackground() throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder
                    .parse("http://newfiles.atlauncher.com/launcher/addons.xml");
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("addon");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    String name = element.getAttribute("name");
                    Player owner = new Player(element.getAttribute("owner"));
                    Version[] versions;
                    if (element.getAttribute("versions").isEmpty()) {
                        // Pack has no versions so log it and continue to next
                        // pack
                        LauncherFrame.console.log("Addon " + name
                                + " has no versions!");
                        continue;
                    } else {
                        String[] tempVersions = element
                                .getAttribute("versions").split(",");
                        versions = new Version[tempVersions.length];
                        for (int v = 0; v < tempVersions.length; v++) {
                            String[] parsed = tempVersions[v].split("\\.");
                            versions[v] = new Version(
                                    Integer.parseInt(parsed[0]),
                                    Integer.parseInt(parsed[1]),
                                    Integer.parseInt(parsed[2]));
                        }
                    }
                    String description = element.getAttribute("description");
                    Pack forPack;
                    Pack pack = LauncherFrame.settings.getPacks().getPackByID(
                            id);
                    if (pack != null) {
                        forPack = pack;
                    } else {
                        LauncherFrame.console.log("Addon " + name
                                + " is not available for any packs!");
                        continue;
                    }
                    Addon addon = new Addon(id, name, owner, versions,
                            description, forPack);
                    publish(addon);
                    Thread.sleep(50); // Needed for publish to work properly
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
