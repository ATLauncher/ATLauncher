/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import com.atlauncher.App;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.gui.dialogs.AddEditServerForCheckerDialog;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ServersForCheckerTab extends JPanel implements ActionListener {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 3385411077046354453L;

    private final JPopupMenu CONTEXT_MENU = new JPopupMenu();
    private final JMenuItem EDIT_BUTTON = new JMenuItem("Edit");
    private final JMenuItem DELETE_BUTTON = new JMenuItem("Delete");

    private DefaultListModel<MinecraftServer> listModel;
    private JList serverList;

    public ServersForCheckerTab() {
        setLayout(new BorderLayout());
        EDIT_BUTTON.addActionListener(this);
        DELETE_BUTTON.addActionListener(this);
        CONTEXT_MENU.add(EDIT_BUTTON);
        CONTEXT_MENU.add(DELETE_BUTTON);

        listModel = new DefaultListModel<MinecraftServer>();
        for (MinecraftServer server : App.settings.getCheckingServers()) {
            listModel.addElement(server);
        }
        serverList = new JList(listModel);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    deleteSelectedElement();
                }
            }

        });
        serverList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                serverList.setSelectedIndex(serverList.locationToIndex(e.getPoint()));
                if (e.getButton() == MouseEvent.BUTTON3) {
                    CONTEXT_MENU.show(serverList, e.getX(), e.getY());
                }
            }
        });

        add(serverList, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == EDIT_BUTTON) {
            if (serverList.getSelectedIndex() != -1) {
                new AddEditServerForCheckerDialog(((MinecraftServer) serverList.getSelectedValue()));
                reloadServers();
            }
        } else if (e.getSource() == DELETE_BUTTON) {
            deleteSelectedElement();
        }
    }

    private void deleteSelectedElement() {
        if (serverList.getSelectedIndex() != -1) {
            MinecraftServer selectedValue = ((MinecraftServer) serverList.getSelectedValue());
            App.settings.removeCheckingServer(selectedValue);
            listModel.removeElement(selectedValue);
        }
    }

    public void reloadServers() {
        listModel.removeAllElements();
        for (MinecraftServer server : App.settings.getCheckingServers()) {
            listModel.addElement(server);
        }
    }
}
