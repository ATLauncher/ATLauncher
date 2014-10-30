/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.dialogs;

import com.atlauncher.data.Language;
import com.atlauncher.gui.tabs.ServersForCheckerTab;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerListForCheckerDialog extends JDialog implements ActionListener, ListSelectionListener {
    /**
     * Auto generate serial.
     */
    private static final long serialVersionUID = -1462218261978353036L;

    private final JTabbedPane TABBED_PANE = new JTabbedPane(JTabbedPane.TOP);
    private final JButton ADD_BUTTON = new JButton(Language.INSTANCE.localize("common.add"));
    private final JButton CLOSE_BUTTON = new JButton(Language.INSTANCE.localize("common.close"));
    private final JButton DELETE_BUTTON = new JButton(Language.INSTANCE.localize("common.delete"));
    private final JButton EDIT_BUTTON = new JButton(Language.INSTANCE.localize("common.edit"));

    private final ServersForCheckerTab SERVERS_TAB = new ServersForCheckerTab();

    private final JPanel BOTTOM_PANEL = new JPanel();

    public ServerListForCheckerDialog() {
        super(null, Language.INSTANCE.localize("tools.serverchecker"), ModalityType.APPLICATION_MODAL);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        TABBED_PANE.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        TABBED_PANE.addTab(Language.INSTANCE.localize("tools.serverchecker.servers"), SERVERS_TAB);

        SERVERS_TAB.addListSelectionListener(this);

        ADD_BUTTON.addActionListener(this);

        EDIT_BUTTON.addActionListener(this);
        EDIT_BUTTON.setEnabled(false);

        DELETE_BUTTON.addActionListener(this);
        DELETE_BUTTON.setEnabled(false);

        CLOSE_BUTTON.addActionListener(this);

        BOTTOM_PANEL.setLayout(new FlowLayout());

        BOTTOM_PANEL.add(ADD_BUTTON);
        BOTTOM_PANEL.add(EDIT_BUTTON);
        BOTTOM_PANEL.add(DELETE_BUTTON);
        BOTTOM_PANEL.add(CLOSE_BUTTON);

        add(TABBED_PANE, BorderLayout.CENTER);
        add(BOTTOM_PANEL, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ADD_BUTTON) {
            new AddEditServerForCheckerDialog(null);
            SERVERS_TAB.reloadServers();
        } else if (e.getSource() == EDIT_BUTTON) {
            SERVERS_TAB.editSelectedElement();
        } else if (e.getSource() == DELETE_BUTTON) {
            SERVERS_TAB.deleteSelectedElement();
        } else if (e.getSource() == CLOSE_BUTTON) {
            close();
        }
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            JList list = (JList) e.getSource();
            EDIT_BUTTON.setEnabled(list.getSelectedIndex() != -1);
            DELETE_BUTTON.setEnabled(list.getSelectedIndex() != -1);
        }
    }
}
