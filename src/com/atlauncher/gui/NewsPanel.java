/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class NewsPanel extends JPanel {

    private JEditorPane newsArea;

    public NewsPanel() {
        setLayout(new BorderLayout());
        loadContent();
    }

    private void loadContent() {
        newsArea = new JEditorPane("text/html", "");
        newsArea.setEditable(false);
        newsArea.setSelectionColor(Color.GRAY);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("A {color:#0088CC}");
        styleSheet.addRule("#newsHeader {font-weight:bold;font-size:14px;color:#339933;}");
        styleSheet.addRule("#newsBody {font-size:10px;padding-left:20px;}");
        newsArea.setEditorKit(kit);

        newsArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Utils.openBrowser(e.getURL());
                }
            }
        });
        add(new JScrollPane(newsArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        String news = "<html>";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(App.settings.getConfigsDir(), "news.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("article");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (i == nodeList.getLength() - 1) {
                        news += "<p id=\"newsHeader\">"
                                + element.getAttribute("posted")
                                + " - <a href=\""
                                + element.getAttribute("link")
                                + "\">"
                                + element.getAttribute("title")
                                + "</a> ("
                                + element.getAttribute("comments")
                                + " "
                                + (Integer.parseInt(element.getAttribute("comments")) == 1 ? "comment"
                                        : "comments") + ")</p>" + "<p id=\"newsBody\">"
                                + element.getTextContent() + "</p><br/>";
                    } else {
                        news += "<p id=\"newsHeader\">"
                                + element.getAttribute("posted")
                                + " - <a href=\""
                                + element.getAttribute("link")
                                + "\">"
                                + element.getAttribute("title")
                                + "</a> ("
                                + element.getAttribute("comments")
                                + " "
                                + (Integer.parseInt(element.getAttribute("comments")) == 1 ? "comment"
                                        : "comments") + ")</p>" + "<p id=\"newsBody\">"
                                + element.getTextContent() + "</p><br/><hr/>";
                    }
                }
            }
        } catch (SAXException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        newsArea.setText(news + "</html>");
        newsArea.setCaretPosition(0);
    }

    public void reload() {
        removeAll();
        loadContent();
        validate();
        repaint();
    }

}
