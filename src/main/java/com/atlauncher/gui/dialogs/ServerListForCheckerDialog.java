/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.gui.tabs.ServersForCheckerTab;
import com.atlauncher.utils.Utils;

public class ServerListForCheckerDialog extends JDialog {

    /**
     * Auto generate serial.
     */
    private static final long serialVersionUID = -1462218261978353036L;

    private final JTabbedPane TABBED_PANE = new JTabbedPane(JTabbedPane.TOP);
    private final JButton ADD_BUTTON = new JButton(App.settings.getLocalizedString("common.add"));
    private final JButton CLOSE_BUTTON = new JButton(
            App.settings.getLocalizedString("common.close"));

    private final ServersForCheckerTab SERVERS_TAB = new ServersForCheckerTab();

    private final JPanel BOTTOM_PANEL = new JPanel();

    public ServerListForCheckerDialog() {
        super(null, App.settings.getLocalizedString("tools.serverchecker"),
                ModalityType.APPLICATION_MODAL);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        TABBED_PANE.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        TABBED_PANE.addTab(App.settings.getLocalizedString("tools.serverchecker.servers"),
                SERVERS_TAB);

        ADD_BUTTON.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddEditServerForCheckerDialog(null);
                SERVERS_TAB.reloadServers();
            }
        });

        CLOSE_BUTTON.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        BOTTOM_PANEL.setLayout(new FlowLayout());

        BOTTOM_PANEL.add(ADD_BUTTON);
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

    public void close() {
        setVisible(false);
        dispose();
    }

}
