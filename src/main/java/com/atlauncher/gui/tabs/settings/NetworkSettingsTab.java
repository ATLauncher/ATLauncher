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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

@SuppressWarnings("serial")
public class NetworkSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover downloadServerLabel;
    private JComboBox<Server> server;

    private JLabelWithHover concurrentConnectionsLabel;
    private JTextField concurrentConnections;

    private JLabelWithHover enableProxyLabel;
    private JCheckBox enableProxy;

    private JLabelWithHover proxyHostLabel;
    private JTextField proxyHost;

    private JLabelWithHover proxyPortLabel;
    private JTextField proxyPort;

    private JLabelWithHover proxyTypeLabel;
    private JComboBox<String> proxyType;

    public NetworkSettingsTab() {
        EventHandler.EVENT_BUS.subscribe(this);
        // Download Server
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        downloadServerLabel = new JLabelWithHover(LanguageManager.localize("settings.downloadserver") + ":",
                HELP_ICON, LanguageManager.localize("settings.downloadserverhelp"));
        add(downloadServerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        server = new JComboBox<>();
        for (Server serverr : ServerManager.getServers()) {
            if (serverr.isUserSelectable()) {
                server.addItem(serverr);
            }
        }
        server.setSelectedItem(SettingsManager.getActiveServer());
        add(server, gbc);

        // Concurrent Connection Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        concurrentConnectionsLabel = new JLabelWithHover(LanguageManager.localize("settings" + "" +
                ".concurrentconnections") + ":", HELP_ICON, "<html>" + LanguageManager.localizeWithReplace
                ("settings" + "" +
                ".concurrentconnectionshelp", "<br/><br/>") + "</html>");
        add(concurrentConnectionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        concurrentConnections = new JTextField(4);
        concurrentConnections.setText(SettingsManager.getConcurrentConnections() + "");
        add(concurrentConnections, gbc);

        // Enable Proxy

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableProxyLabel = new JLabelWithHover(LanguageManager.localize("settings.enableproxy") + "?", HELP_ICON,
                LanguageManager.localize("settings.enableproxyhelp"));
        add(enableProxyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableProxy = new JCheckBox();
        if (SettingsManager.getEnableProxy()) {
            enableProxy.setSelected(true);
        }
        enableProxy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enableProxy.isSelected()) {
                    proxyHost.setEnabled(false);
                    proxyPort.setEnabled(false);
                    proxyType.setEnabled(false);
                } else {
                    proxyHost.setEnabled(true);
                    proxyPort.setEnabled(true);
                    proxyType.setEnabled(true);
                }
            }
        });
        add(enableProxy, gbc);

        // Proxy Host Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyHostLabel = new JLabelWithHover(LanguageManager.localize("settings.proxyhost") + ":", HELP_ICON,
                LanguageManager.localize("settings.proxyhosthelp"));
        add(proxyHostLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyHost = new JTextField(20);
        proxyHost.setText(SettingsManager.getProxyHost());
        if (!enableProxy.isSelected()) {
            proxyHost.setEnabled(false);
        }
        add(proxyHost, gbc);

        // Proxy Port Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyPortLabel = new JLabelWithHover(LanguageManager.localize("settings.proxyport") + ":", HELP_ICON,
                LanguageManager.localize("settings.proxyporthelp"));
        add(proxyPortLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyPort = new JTextField(4);
        proxyPort.setText((SettingsManager.getProxyPort() == 0 ? "" : SettingsManager.getProxyPort()) + "");
        if (!enableProxy.isSelected()) {
            proxyPort.setEnabled(false);
        }
        add(proxyPort, gbc);

        // Proxy Type Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyTypeLabel = new JLabelWithHover(LanguageManager.localize("settings.proxytype") + ":", HELP_ICON,
                LanguageManager.localize("settings.proxytypehelp"));
        add(proxyTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyType = new JComboBox<>();
        proxyType.addItem("HTTP");
        proxyType.addItem("SOCKS");
        proxyType.addItem("DIRECT");
        proxyType.setSelectedItem(SettingsManager.getProxyType());
        if (!enableProxy.isSelected()) {
            proxyType.setEnabled(false);
        }
        add(proxyType, gbc);
    }

    public boolean isValidConcurrentConnections() {
        if (Integer.parseInt(concurrentConnections.getText().replaceAll("[^0-9]", "")) < 1) {
            JOptionPane.showMessageDialog(App.settings.getParent(), LanguageManager.localize("settings" + "" +
                            ".concurrentconnectionsinvalid"), LanguageManager.localize("settings.help"),
                    JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isValidProxyPort() {
        if (!enableProxy.isSelected()) {
            return true;
        }
        if (proxyPort.getText().isEmpty() || Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) < 1 ||
                Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) > 65535) {
            JOptionPane.showMessageDialog(App.settings.getParent(), LanguageManager.localize("settings" + "" +
                    ".proxyportinvalid"), LanguageManager.localize("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean canConnectWithProxy() {
        if (!enableProxy.isSelected()) {
            return true;
        }

        Type type = null;

        if (proxyType.getSelectedItem().equals("HTTP")) {
            type = Proxy.Type.HTTP;
        } else if (proxyType.getSelectedItem().equals("SOCKS")) {
            type = Proxy.Type.SOCKS;
        } else if (proxyType.getSelectedItem().equals("DIRECT")) {
            type = Proxy.Type.DIRECT;
        }

        if (type == null) {
            return false;
        }

        final Type theType = type;
        final ProgressDialog dialog = new ProgressDialog(LanguageManager.localize("settings" + "" +
                ".checkingproxytitle"), 0, LanguageManager.localize("settings.checkingproxy"), "Cancelled Proxy " +
                "Test!");
        dialog.addThread(new Thread() {
            @Override
            public void run() {
                dialog.setReturnValue(Utils.testProxy(new Proxy(theType, new InetSocketAddress(proxyHost.getText(),
                        Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", ""))))));
                dialog.close();
            }
        });
        dialog.start();

        if (dialog.getReturnValue() == null) {
            return false;
        }

        if (!(Boolean) dialog.getReturnValue()) {
            JOptionPane.showMessageDialog(App.settings.getParent(), LanguageManager.localize("settings" + "" +
                    ".proxycannotconnect"), LanguageManager.localize("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }

        return true;
    }

    public void save() {
        SettingsManager.setServer((Server) server.getSelectedItem());
        SettingsManager.setConcurrentConnections(Integer.parseInt(concurrentConnections.getText().replaceAll("[^0-9]",
                "")));
        SettingsManager.setEnableProxy(enableProxy.isSelected());
        if (enableProxy.isSelected()) {
            SettingsManager.setProxyHost(proxyHost.getText());
            SettingsManager.setProxyPort(Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")));
            SettingsManager.setProxyType(((String) proxyType.getSelectedItem()));
            SettingsManager.configureProxy();
        }
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("settings.networktab");
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.downloadServerLabel.setText(LanguageManager.localize("settings.downloadserver") + ":");
        this.downloadServerLabel.setToolTipText(LanguageManager.localize("settings.downloadserverhelp"));

        this.concurrentConnectionsLabel.setText(LanguageManager.localize("settings" + "" +
                ".concurrentconnections") + ":");
        this.concurrentConnectionsLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" +
                "" +
                ".concurrentconnectionshelp", "<br/><br/>") + "</html>");

        this.enableProxyLabel.setText(LanguageManager.localize("settings.enableproxy") + "?");
        this.enableProxyLabel.setToolTipText(LanguageManager.localize("settings.enableproxyhelp"));

        this.proxyHostLabel.setText(LanguageManager.localize("settings.proxyhost") + ":");
        this.proxyHostLabel.setToolTipText(LanguageManager.localize("settings.proxyhosthelp"));

        this.proxyPortLabel.setText(LanguageManager.localize("settings.proxyport") + ":");
        this.proxyPortLabel.setToolTipText(LanguageManager.localize("settings.proxyporthelp"));

        this.proxyTypeLabel.setText(LanguageManager.localize("settings.proxytype") + ":");
        this.proxyTypeLabel.setToolTipText(LanguageManager.localize("settings.proxytypehelp"));
    }
}
