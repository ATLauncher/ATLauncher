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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.managers.CheckingServersManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.MCQuery;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

import de.zh32.pingtest.QueryVersion;

public class AddEditServerForCheckerDialog extends JDialog implements ActionListener {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 3385411077046354453L;

    private JTextField serverName;

    private JTextField serverHost;

    private JTextField serverPort;

    private JButton addEditButton;
    private JButton closeButton;

    private MinecraftServer serverEditing = null;

    public AddEditServerForCheckerDialog(MinecraftServer minecraftServer) {
        super(null, (minecraftServer == null ? GetText.tr("Add Server") : GetText.tr("Edit Server")),
                ModalityType.DOCUMENT_MODAL);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Add/Edit Server Dialog");

        setupComponents();

        if (minecraftServer != null) {
            this.serverEditing = minecraftServer;
            addEditButton.setText(GetText.tr("Edit"));
            serverName.setText(minecraftServer.getName());
            serverHost.setText(minecraftServer.getHost());
            serverPort.setText(minecraftServer.getPort() + "");
        }

        setVisible(true);
    }

    private void setupComponents() {
        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Server Name

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel serverNameLabel = new JLabel(GetText.tr("Name") + ": ");
        middle.add(serverNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverName = new JTextField(16);
        middle.add(serverName, gbc);

        // Server Host/IP

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel serverHostLabel = new JLabel(GetText.tr("Host/IP") + ": ");
        middle.add(serverHostLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverHost = new JTextField(16);
        middle.add(serverHost, gbc);

        // Server Port

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel serverPortLabel = new JLabel(GetText.tr("Port") + ": ");
        middle.add(serverPortLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverPort = new JTextField(16);
        serverPort.setText("25565");
        middle.add(serverPort, gbc);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addEditButton = new JButton(GetText.tr("Add"));
        addEditButton.addActionListener(this);
        bottom.add(addEditButton);

        closeButton = new JButton(GetText.tr("Close"));
        closeButton.addActionListener(this);
        bottom.add(closeButton);

        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public boolean isValidPort() {
        return !(serverPort.getText().isEmpty() || Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", "")) > 65535);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addEditButton) {
            if (serverName.getText().isEmpty() || serverHost.getText().isEmpty() || serverPort.getText().isEmpty()) {
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(GetText.tr("Not all fields were filled in. Please go back and fill them in!"))
                        .setType(DialogManager.ERROR).show();
            } else if (!isValidPort()) {
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(GetText.tr("The port you specified is invalid. Please check it and try again."))
                        .setType(DialogManager.ERROR).show();
            } else {
                String name = serverName.getText();
                final String host = serverHost.getText();
                final int port = Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", ""));

                final ProgressDialog<QueryVersion> dialog = new ProgressDialog<>(GetText.tr("Checking Server"), 0,
                        GetText.tr("Checking Server"), "Cancelled Server Check!");
                dialog.addThread(new Thread(() -> {
                    dialog.setReturnValue(MCQuery.getMinecraftServerQueryVersion(host, port));
                    dialog.close();
                }));
                dialog.start();

                QueryVersion qv = dialog.getReturnValue();

                if (qv == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(GetText.tr(
                            "Couldn't connect to server. All servers must be online and successfully checked once before adding."))
                            .setType(DialogManager.ERROR).show();
                } else {
                    App.TOASTER.pop(
                            (this.serverEditing == null ? GetText.tr("Server Added") : GetText.tr("Server Edited")));
                    if (this.serverEditing == null) {
                        CheckingServersManager.addCheckingServer(new MinecraftServer(name, host, port, qv));
                    } else {
                        this.serverEditing.setName(name);
                        this.serverEditing.setHost(host);
                        this.serverEditing.setPort(port);
                        this.serverEditing.setQueryVersion(qv);
                        CheckingServersManager.saveCheckingServers();
                    }
                    close();
                }
            }
        } else if (e.getSource() == closeButton) {
            close();
        }
    }

    public void close() {
        setVisible(false);
        dispose();
    }

}
