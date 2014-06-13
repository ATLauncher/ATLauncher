/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Toolkit;
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
import com.atlauncher.utils.Utils;

/**
 * This class extends {@link JPanel} and provides a Panel for displaying the latest news.
 */
public class NewsTab extends JPanel {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4616284541226058793L;

    private final HTMLEditorKit NEWS_KIT = new HTMLEditorKit(){
        {
            this.setStyleSheet(Utils.createStyleSheet("news"));
        }
    };

    private final ContextMenu NEWS_MENU = new ContextMenu();

    /**
     * {@link JEditorPane} which contains all the news for this panel.
     */
    private final JEditorPane NEWS_PANE = new JEditorPane("text/html", ""){
        {
            this.setEditable(false);
            this.setEditorKit(NEWS_KIT);
            this.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                        Utils.openBrowser(e.getURL());
                    }
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if(NEWS_PANE.getSelectedText() != null){
                        if(e.getButton() == MouseEvent.BUTTON3){
                            NEWS_MENU.show(NEWS_PANE, e.getX(), e.getY());
                        }
                    }
                }
            });
        }
    };


    /**
     * Instantiates a new instance of this class which sets the layout and loads the content.
     */
    public NewsTab() {
<<<<<<< HEAD
        setLayout(new BorderLayout());
        loadContent();
    }

    /**
     * Loads the news into the content of this panel.
     */
    private void loadContent() {
        newsArea = new JEditorPane("text/html", null);
        newsArea.setEditable(false);
        newsArea.setSelectionColor(App.THEME.getSelectionColor());
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
     * Sets up the language and correct localisation for the users selected language.
     */
    public void setupLanguage() {
        copy.setText(App.settings.getLocalizedString("common.copy"));
=======
        super(new BorderLayout());
        this.add(new JScrollPane(this.NEWS_PANE, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.reload();
>>>>>>> branch 'master' of https://github.com/ATLauncher/ATLauncher.git
    }

    /**
     * Reloads the panel with updated news.
     */
    public void reload() {
        this.NEWS_PANE.setText("");
        this.NEWS_PANE.setText(App.settings.getNewsHTML());
        this.NEWS_PANE.setCaretPosition(0);
    }

    private final class ContextMenu extends JPopupMenu {
        private final JMenuItem COPY_ITEM = new JMenuItem("Copy");

        public ContextMenu(){
            super();
            this.COPY_ITEM.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringSelection text = new StringSelection(NEWS_PANE.getSelectedText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, null);
                }
            });
        }
    }
}
