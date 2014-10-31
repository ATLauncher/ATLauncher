/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.gui.dialogs.AddEditServerForCheckerDialog;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ServersForCheckerTab extends JPanel implements ActionListener {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 3385411077046354453L;

    private final JPopupMenu CONTEXT_MENU = new JPopupMenu();
    private final JMenuItem EDIT_BUTTON = new JMenuItem(Language.INSTANCE.localize("common.edit"));
    private final JMenuItem DELETE_BUTTON = new JMenuItem(Language.INSTANCE.localize("common.delete"));

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
        serverList.setSelectionModel(new DefaultListSelectionModel() {
            private static final long serialVersionUID = -88997910673981243L;

            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                }
                super.setSelectionInterval(index0, index1);
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
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
                if (e.getButton() == MouseEvent.BUTTON3) {
                    CONTEXT_MENU.show(serverList, e.getX(), e.getY());
                }
            }
        });

        add(serverList, BorderLayout.CENTER);
    }

    public void editSelectedElement() {
        if (serverList.getSelectedIndex() != -1) {
            new AddEditServerForCheckerDialog(((MinecraftServer) serverList.getSelectedValue()));
            reloadServers();
        }
    }

    public void deleteSelectedElement() {
        if (serverList.getSelectedIndex() != -1) {
            MinecraftServer selectedValue = ((MinecraftServer) serverList.getSelectedValue());
            App.settings.removeCheckingServer(selectedValue);
            listModel.removeElement(selectedValue);
            reloadServers();
        }
    }

    public void reloadServers() {
        listModel.removeAllElements();
        for (MinecraftServer server : App.settings.getCheckingServers()) {
            listModel.addElement(server);
        }
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        this.serverList.addListSelectionListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == EDIT_BUTTON) {
            editSelectedElement();
        } else if (e.getSource() == DELETE_BUTTON) {
            deleteSelectedElement();
        }
    }
}
