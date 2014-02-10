/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.atlauncher.App;
import com.atlauncher.data.News;
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
        for (News newsItem : App.settings.getNews()) {
            news += newsItem;
            if (App.settings.getNews().get(App.settings.getNews().size() - 1) != newsItem) {
                news += "<hr/>";
            }
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
