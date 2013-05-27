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

import com.atlauncher.data.Language;

public class LanguageLoader extends SwingWorker<Void, Language> {

    @Override
    protected Void doInBackground() throws Exception {
        Language language;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder
                    .parse("http://newfiles.atlauncher.com/launcher/languages.xml");
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("language");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    language = new Language(element.getAttribute("name"),
                            element.getAttribute("localizedname"),
                            element.getAttribute("file"),
                            element.getAttribute("author"));
                    publish(language);
                    Thread.sleep(50); // Needed for publish to work properly
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
