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
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.atlauncher.workers.NewsDownloader;

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
        styleSheet.addRule("A {color:#00C6EE}");
        styleSheet
                .addRule("#loading {text-align:center;font-weight:bold;font-size:16px;color:#339933;}");
        styleSheet
                .addRule("#newsHeader {font-weight:bold;font-size:16px;color:#339933;}");
        styleSheet.addRule("#newsBody {font-size:10px;padding-left:20px;}");
        newsArea.setEditorKit(kit);
        newsArea.setText("<html><p id=\"loading\">Loading News</p></html>");

        newsArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Utils.openBrowser(e.getURL());
                }
            }
        });
        add(new JScrollPane(newsArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        loadNews();
    }

    private void loadNews() {
        NewsDownloader newsDownloader = new NewsDownloader() {
            String news = "";

            @Override
            protected void process(List<String> chunks) {
                String got = chunks.get(chunks.size() - 1);
                if (news.isEmpty()) {
                    news = "<html>" + got;
                } else {
                    news = news + got;
                }
            }

            @Override
            protected void done() {
                newsArea.setText(news + "</html>");
                newsArea.setCaretPosition(0);
            }
        };
        newsDownloader.execute();
    }

}
