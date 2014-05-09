/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import com.atlauncher.App;
import com.atlauncher.data.Settings;
import com.atlauncher.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * This class extends {@link JPanel} and provides a Panel for displaying the latest news.
 */
public class NewsPanel extends JPanel {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4616284541226058793L;

    /**
     * {@link JEditorPane} which contains all the news for this panel.
     */
    private JEditorPane newsArea;

    /**
     * The context menu when selected text is right clicked.
     */
    private JPopupMenu contextMenu;

    /**
     * The copy button used in the context menu.
     */
    private JMenuItem copy;

    /**
     * Instantiates a new instance of this class which sets the layout and loads the content.
     */
    public NewsPanel() {
        setLayout(new BorderLayout());
        loadContent();
    }

    /**
     * Loads the news into the content of this panel.
     */
    private void loadContent() {
        newsArea = new JEditorPane("text/html", null);
        newsArea.setEditable(false);
        newsArea.setSelectionColor(Settings.selectionColour);
        setupContextMenu(); // Setup the right click context menu

        HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(Utils.createStyleSheet("news")); // Get the news StyleSheet
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

        newsArea.setText(App.settings.getNewsHTML()); // Load the HTML for the news
        newsArea.setCaretPosition(0); // Reset position back to the top
    }

    /**
     * Sets up the right click Context Menu.
     */
    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringSelection text = new StringSelection(newsArea.getSelectedText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        contextMenu.add(copy);

        newsArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (newsArea.getSelectedText() != null) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        contextMenu.show(newsArea, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    /**
     * Sets up the language and correct localization for the users selected langauge.
     */
    public void setupLanguage() {
        copy.setText(App.settings.getLocalizedString("common.copy"));
    }

    /**
     * Reloads the panel with updated news.
     */
    public void reload() {
        removeAll();
        loadContent();
        validate();
        repaint();
    }

}
