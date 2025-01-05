/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.gui.card;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.DropDownButton;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;

public class ServerCard extends CollapsiblePanel implements RelocalizationListener {
    private final Server server;
    private final ImagePanel image;

    private final JPopupMenu launchPopMenu = new JPopupMenu();
    private final JMenuItem launchAndCloseButton = new JMenuItem(GetText.tr("Launch & Close"));
    private final JMenuItem launchWithGui = new JMenuItem(GetText.tr("Launch With GUI"));
    private final JMenuItem launchWithGuiAndClose = new JMenuItem(GetText.tr("Launch With GUI & Close"));
    private final DropDownButton launchButton = new DropDownButton(GetText.tr("Launch"), launchPopMenu, true,
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    server.launch("nogui", false);
                }
            });

    private final JPopupMenu morePopupMenu = new JPopupMenu();
    private final JMenuItem backupButton = new JMenuItem(GetText.tr("Backup"));
    private final JMenuItem deleteButton = new JMenuItem(GetText.tr("Delete"));
    private final JMenuItem openButton = new JMenuItem(GetText.tr("Open Folder"));
    // seaprator
    private final JMenuItem discordLinkMenuItem = new JMenuItem(GetText.tr("Discord"));
    private final JMenuItem supportLinkMenuItem = new JMenuItem(GetText.tr("Support"));
    private final JMenuItem websiteLinkMenuItem = new JMenuItem(GetText.tr("Website"));
    private final JMenuItem wikiLinkMenuItem = new JMenuItem(GetText.tr("Wiki"));
    private final JMenuItem sourceLinkMenuItem = new JMenuItem(GetText.tr("Source"));
    private final DropDownButton moreButton = new DropDownButton("More", morePopupMenu);

    private final JTextArea descArea = new JTextArea();

    public ServerCard(Server server) {
        super(server);
        this.server = server;
        this.image = new ImagePanel(() -> server.getImage().getImage());

        descArea.setText(server.getPackDescription());
        descArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setForeground(getBackground().brighter().brighter().brighter().brighter());

        descArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    server.startChangeDescription();
                    descArea.setText(server.getPackDescription());
                }
            }
        });

        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 8, 6));
        buttonGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        buttonGrid.add(this.launchButton);
        launchPopMenu.add(this.launchAndCloseButton);
        launchPopMenu.add(this.launchWithGui);
        launchPopMenu.add(this.launchWithGuiAndClose);

        morePopupMenu.add(this.openButton);
        morePopupMenu.add(this.backupButton);
        morePopupMenu.add(this.deleteButton);
        buttonGrid.add(moreButton);

        // unfortunately OSX doesn't allow us to pass arguments with open and Terminal
        if (OS.isMac()) {
            this.launchButton.setVisible(false);
            this.launchAndCloseButton.setVisible(false);
        }

        JScrollPane desc = new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        desc.setPreferredSize(new Dimension(getPreferredSize().width, 50));

        add(image);
        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));

        upper.add(mainTitile);
        upper.add(desc);

        JSplitPane subSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, buttonGrid);
        JSplitPane mainSpitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, image, subSplitter);
        mainSpitter.setEnabled(false);
        subSplitter.setEnabled(false);
        add(mainSpitter);

        setupButtonPopupMenus();

        RelocalizationManager.addListener(this);

        this.addActionListeners();
        this.addMouseListeners();
    }

    private void addActionListeners() {
        // this.launchButton.addActionListener(e -> );
        this.launchAndCloseButton.addActionListener(e -> server.launch("nogui", true));
        this.launchWithGui.addActionListener(e -> server.launch(false));
        this.launchWithGuiAndClose.addActionListener(e -> server.launch(true));
        this.backupButton.addActionListener(e -> server.backup());
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog(false).setTitle(GetText.tr("Delete Server"))
                    .setContent(GetText.tr("Are you sure you want to delete this server?")).setType(DialogManager.ERROR)
                    .show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.trackEvent(AnalyticsEvent.forServerEvent("server_delete", server));
                final ProgressDialog<Object> dialog = new ProgressDialog<>(GetText.tr("Deleting Server"), 0,
                        GetText.tr("Deleting Server. Please wait..."), null, App.launcher.getParent());
                dialog.addThread(new Thread(() -> {
                    ServerManager.removeServer(server);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Deleted Server Successfully"));
                }));
                dialog.start();
            }
        });
        this.openButton.addActionListener(e -> OS.openFileExplorer(server.getRoot()));
    }

    private void addMouseListeners() {
        this.image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (OS.isMac()) {
                        server.launch(false);
                    } else {
                        server.launch("nogui", false);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu rightClickMenu = new JPopupMenu();

                    JMenuItem changeDescriptionItem = new JMenuItem(GetText.tr("Change Description"));
                    rightClickMenu.add(changeDescriptionItem);

                    JMenuItem changeImageItem = new JMenuItem(GetText.tr("Change Image"));
                    rightClickMenu.add(changeImageItem);

                    rightClickMenu.show(image, e.getX(), e.getY());

                    changeDescriptionItem.addActionListener(e14 -> {
                        server.startChangeDescription();
                        descArea.setText(server.getPackDescription());
                    });

                    changeImageItem.addActionListener(e13 -> {
                        server.startChangeImage();
                        image.setImage(server.getImage().getImage());
                    });
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void setupButtonPopupMenus() {
        if (server.showGetHelpButton()) {
            morePopupMenu.addSeparator();
            if (server.getDiscordInviteUrl() != null) {
                discordLinkMenuItem.addActionListener(e -> OS.openWebBrowser(server.getDiscordInviteUrl()));
                morePopupMenu.add(discordLinkMenuItem);
            }

            if (server.getSupportUrl() != null) {
                supportLinkMenuItem.addActionListener(e -> OS.openWebBrowser(server.getSupportUrl()));
                morePopupMenu.add(supportLinkMenuItem);
            }

            if (server.getWebsiteUrl() != null) {
                websiteLinkMenuItem.addActionListener(e -> OS.openWebBrowser(server.getWebsiteUrl()));
                morePopupMenu.add(websiteLinkMenuItem);
            }

            if (server.getWikiUrl() != null) {
                wikiLinkMenuItem.addActionListener(e -> OS.openWebBrowser(server.getWikiUrl()));
                morePopupMenu.add(wikiLinkMenuItem);
            }

            if (server.getSourceUrl() != null) {
                sourceLinkMenuItem.addActionListener(e -> OS.openWebBrowser(server.getSourceUrl()));
                morePopupMenu.add(sourceLinkMenuItem);
            }
        }
    }

    @Override
    public void onRelocalization() {
        this.launchButton.setText(GetText.tr("Launch"));
        this.launchAndCloseButton.setText(GetText.tr("Launch & Close"));
        this.launchWithGui.setText(GetText.tr("Launch With GUI"));
        this.launchWithGuiAndClose.setText(GetText.tr("Launch With GUI & Close"));
        this.backupButton.setText(GetText.tr("Backup"));
        this.deleteButton.setText(GetText.tr("Delete"));
        this.openButton.setText(GetText.tr("Open Folder"));
    }
}
