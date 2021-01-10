/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

@SuppressWarnings("serial")
public class ServerCard extends CollapsiblePanel implements RelocalizationListener {
    private final Server server;
    private final ImagePanel image;
    private final JButton launchButton = new JButton(GetText.tr("Launch"));
    private final JButton launchAndCloseButton = new JButton(GetText.tr("Launch & Close"));
    private final JButton launchWithGui = new JButton(GetText.tr("Launch With GUI"));
    private final JButton launchWithGuiAndClose = new JButton(GetText.tr("Launch With GUI & Close"));
    private final JButton backupButton = new JButton(GetText.tr("Backup"));
    private final JButton deleteButton = new JButton(GetText.tr("Delete"));
    private final JButton openButton = new JButton(GetText.tr("Open Folder"));

    public ServerCard(Server server) {
        super(server);
        this.server = server;
        this.image = new ImagePanel(server.getImage().getImage());
        JSplitPane splitter = new JSplitPane();
        splitter.setLeftComponent(this.image);
        JPanel rightPanel = new JPanel();
        splitter.setRightComponent(rightPanel);
        splitter.setEnabled(false);

        JTextArea descArea = new JTextArea();
        descArea.setText(server.getPackDescription());
        descArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        as.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        top.add(this.launchButton);
        top.add(this.launchAndCloseButton);
        top.add(this.launchWithGui);
        top.add(this.launchWithGuiAndClose);
        bottom.add(this.backupButton);
        bottom.add(this.deleteButton);
        bottom.add(this.openButton);

        // unfortunately OSX doesn't allow us to pass arguments with open and Terminal
        if (OS.isMac()) {
            this.launchButton.setVisible(false);
            this.launchAndCloseButton.setVisible(false);
        }

        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, 180));
        rightPanel.add(new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(splitter, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        this.addActionListeners();
        this.addMouseListeners();
    }

    private void addActionListeners() {
        this.launchButton.addActionListener(e -> server.launch("nogui", false));
        this.launchAndCloseButton.addActionListener(e -> server.launch("nogui", true));
        this.launchWithGui.addActionListener(e -> server.launch(false));
        this.launchWithGuiAndClose.addActionListener(e -> server.launch(true));
        this.backupButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Backing Up {0}", server.name))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This will backup your entire server folder so you can restore in case of an incident.<br/><br/>Do you want to backup this server?"))
                            .build())
                    .setType(DialogManager.INFO).show();

            if (ret == DialogManager.YES_OPTION) {
                final JDialog dialog = new JDialog(App.launcher.getParent(), GetText.tr("Backing Up {0}", server.name),
                        ModalityType.DOCUMENT_MODAL);
                dialog.setSize(300, 100);
                dialog.setLocationRelativeTo(App.launcher.getParent());
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                JLabel doing = new JLabel(GetText.tr("Backing Up {0}", server.name));
                doing.setHorizontalAlignment(JLabel.CENTER);
                doing.setVerticalAlignment(JLabel.TOP);
                topPanel.add(doing);

                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BorderLayout());
                JProgressBar progressBar = new JProgressBar();
                bottomPanel.add(progressBar, BorderLayout.NORTH);
                progressBar.setIndeterminate(true);

                dialog.add(topPanel, BorderLayout.CENTER);
                dialog.add(bottomPanel, BorderLayout.SOUTH);

                Analytics.sendEvent(server.pack + " - " + server.version, "Backup", "Server");

                final Thread backupThread = new Thread(() -> {
                    Timestamp timestamp = new Timestamp(new Date().getTime());
                    String time = timestamp.toString().replaceAll("[^0-9]", "_");
                    String filename = "Server-" + server.getSafeName() + "-" + time.substring(0, time.lastIndexOf("_"))
                            + ".zip";
                    ZipUtil.pack(server.getRoot().toFile(), FileSystem.BACKUPS.resolve(filename).toFile());
                    dialog.dispose();
                    App.TOASTER.pop(GetText.tr("Backup is complete"));
                });
                backupThread.start();
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        backupThread.interrupt();
                        dialog.dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete Server"))
                    .setContent(GetText.tr("Are you sure you want to delete this server?")).setType(DialogManager.ERROR)
                    .show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.sendEvent(server.pack + " - " + server.version, "Delete", "Server");
                final ProgressDialog dialog = new ProgressDialog(GetText.tr("Deleting Server"), 0,
                        GetText.tr("Deleting Server. Please wait..."), null);
                dialog.addThread(new Thread(() -> {
                    ServerManager.removeServer(server);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Deleted Server Successfully"));
                }));
                dialog.start();
                App.launcher.reloadServersPanel();
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

                    JMenuItem changeImageItem = new JMenuItem(GetText.tr("Change Image"));
                    rightClickMenu.add(changeImageItem);

                    rightClickMenu.show(image, e.getX(), e.getY());

                    changeImageItem.addActionListener(e13 -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);
                        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                        int ret = chooser.showOpenDialog(App.launcher.getParent());
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            File img = chooser.getSelectedFile();
                            if (img.getAbsolutePath().endsWith(".png")) {
                                Analytics.sendEvent(server.pack + " - " + server.version, "ChangeImage", "Server");
                                try {
                                    Utils.safeCopy(img, server.getRoot().resolve("server.png").toFile());
                                    image.setImage(server.getImage().getImage());
                                    server.save();
                                } catch (IOException ex) {
                                    LogManager.logStackTrace("Failed to set server image", ex);
                                }
                            }
                        }
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
