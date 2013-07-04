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

@SuppressWarnings("serial")
public class NewsPanel extends JPanel {

    private JEditorPane newsArea;

    public NewsPanel() {
        setLayout(new BorderLayout());
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
                        news += "<p id=\"newsHeader\">" + element.getAttribute("posted")
                                + " - <a href=\"" + element.getAttribute("link") + "\">"
                                + element.getAttribute("title") + "</a></p>"
                                + "<p id=\"newsBody\">" + element.getTextContent() + "</p><br/>";
                    } else {
                        news += "<p id=\"newsHeader\">" + element.getAttribute("posted")
                                + " - <a href=\"" + element.getAttribute("link") + "\">"
                                + element.getAttribute("title") + "</a></p>"
                                + "<p id=\"newsBody\">" + element.getTextContent()
                                + "</p><br/><hr/>";
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
        newsArea.setText(news + "</html>");
        newsArea.setCaretPosition(0);
    }

}
