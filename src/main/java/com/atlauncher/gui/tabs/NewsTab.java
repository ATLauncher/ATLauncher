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
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.utils.Resources;
import com.atlauncher.utils.Utils;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class extends {@link JPanel} and provides a Panel for displaying the latest news.
 */
public class NewsTab extends JPanel implements Tab {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4616284541226058793L;

    private final HTMLEditorKit NEWS_KIT = new HTMLEditorKit() {
        {
            this.setStyleSheet(Resources.makeStyleSheet("news"));
        }
    };

    private final ContextMenu NEWS_MENU = new ContextMenu();

    /**
     * Instantiates a new instance of this class which sets the layout and loads the content.
     */
    public NewsTab() {
        super(new BorderLayout());
        this.add(new JScrollPane(this.NEWS_PANE, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.reload();
    }

    /**
     * {@link JEditorPane} which contains all the news for this panel.
     */
    private final JEditorPane NEWS_PANE = new JEditorPane("text/html;charset=UTF-8", "") {
        {
            this.setEditable(false);
            this.setEditorKit(NEWS_KIT);
            this.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Utils.openBrowser(e.getURL());
                    }
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (NEWS_PANE.getSelectedText() != null) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            NEWS_MENU.show(NEWS_PANE, e.getX(), e.getY());
                        }
                    }
                }
            });
        }
    };

    /**
     * Reloads the panel with updated news.
     */
    public void reload() {
        this.NEWS_PANE.setText("");
        this.NEWS_PANE.setText(App.settings.getNewsHTML());
        this.NEWS_PANE.setCaretPosition(0);
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.news");
    }

    private final class ContextMenu extends JPopupMenu {
        private final JMenuItem COPY_ITEM = new JMenuItem(Language.INSTANCE.localize("common.copy"));

        public ContextMenu() {
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
