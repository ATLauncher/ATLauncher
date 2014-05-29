/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.Server;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class NetworkSettingsTab extends AbstractSettingsTab {

    private JLabel downloadServerLabel;
    private JComboBox<Server> server;

    private JLabel connectionTimeoutLabel;
    private JTextField connectionTimeout;

    private JLabel concurrentConnectionsLabel;
    private JTextField concurrentConnections;

    private JLabel enableProxyLabel;
    private JCheckBox enableProxy;

    private JLabel proxyHostLabel;
    private JTextField proxyHost;

    private JLabel proxyPortLabel;
    private JTextField proxyPort;

    private JLabel proxyTypeLabel;
    private JComboBox<String> proxyType;

    public NetworkSettingsTab() {
        // Download Server
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        downloadServerLabel = new JLabel(App.settings.getLocalizedString("settings.downloadserver")
                + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        downloadServerLabel.setIcon(helpIcon);
        downloadServerLabel.setToolTipText(App.settings
                .getLocalizedString("settings.downloadserverhelp"));
        add(downloadServerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        server = new JComboBox<Server>();
        for (Server serverr : App.settings.getServers()) {
            if (serverr.isUserSelectable()) {
                server.addItem(serverr);
            }
        }
        server.setSelectedItem(App.settings.getOriginalServer());
        add(server, gbc);

        // Connection Timeout Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        connectionTimeoutLabel = new JLabel(
                App.settings.getLocalizedString("settings.connectiontimeout") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        connectionTimeoutLabel.setIcon(helpIcon);
        connectionTimeoutLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.connectiontimeouthelp", "<br/><br/>")
                + "</center></html>");
        add(connectionTimeoutLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        connectionTimeout = new JTextField(4);
        connectionTimeout.setText(App.settings.getConnectionTimeout() / 1000 + "");
        add(connectionTimeout, gbc);

        // Concurrent Connection Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        concurrentConnectionsLabel = new JLabel(
                App.settings.getLocalizedString("settings.concurrentconnections") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        concurrentConnectionsLabel.setIcon(helpIcon);
        concurrentConnectionsLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.concurrentconnectionshelp",
                        "<br/><br/>") + "</center></html>");
        add(concurrentConnectionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        concurrentConnections = new JTextField(4);
        concurrentConnections.setText(App.settings.getConcurrentConnections() + "");
        add(concurrentConnections, gbc);

        // Enable Proxy

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableProxyLabel = new JLabel(App.settings.getLocalizedString("settings.enableproxy") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableProxyLabel.setIcon(helpIcon);
        enableProxyLabel
                .setToolTipText(App.settings.getLocalizedString("settings.enableproxyhelp"));
        add(enableProxyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableProxy = new JCheckBox();
        if (App.settings.getEnableProxy()) {
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
        proxyHostLabel = new JLabel(App.settings.getLocalizedString("settings.proxyhost") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        proxyHostLabel.setIcon(helpIcon);
        proxyHostLabel.setToolTipText(App.settings.getLocalizedString("settings.proxyhosthelp"));
        add(proxyHostLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyHost = new JTextField(20);
        proxyHost.setText(App.settings.getProxyHost());
        if (!enableProxy.isSelected()) {
            proxyHost.setEnabled(false);
        }
        add(proxyHost, gbc);

        // Proxy Port Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyPortLabel = new JLabel(App.settings.getLocalizedString("settings.proxyport") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        proxyPortLabel.setIcon(helpIcon);
        proxyPortLabel.setToolTipText(App.settings.getLocalizedString("settings.proxyporthelp"));
        add(proxyPortLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyPort = new JTextField(4);
        proxyPort.setText((App.settings.getProxyPort() == 0 ? "" : App.settings.getProxyPort())
                + "");
        if (!enableProxy.isSelected()) {
            proxyPort.setEnabled(false);
        }
        add(proxyPort, gbc);

        // Proxy Type Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyTypeLabel = new JLabel(App.settings.getLocalizedString("settings.proxytype") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        proxyTypeLabel.setIcon(helpIcon);
        proxyTypeLabel.setToolTipText(App.settings.getLocalizedString("settings.proxytypehelp"));
        add(proxyTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyType = new JComboBox<String>();
        proxyType.addItem("HTTP");
        proxyType.addItem("SOCKS");
        proxyType.addItem("DIRECT");
        proxyType.setSelectedItem(App.settings.getProxyType());
        if (!enableProxy.isSelected()) {
            proxyType.setEnabled(false);
        }
        add(proxyType, gbc);
    }

    public boolean isValidConnectionTimeout() {
        if (Integer.parseInt(connectionTimeout.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(connectionTimeout.getText().replaceAll("[^0-9]", "")) > 30) {
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("settings.connectiontimeoutinvalid"),
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isValidConcurrentConnections() {
        if (Integer.parseInt(concurrentConnections.getText().replaceAll("[^0-9]", "")) < 1) {
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("settings.concurrentconnectionsinvalid"),
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isValidProxyPort() {
        if (!enableProxy.isSelected()) {
            return true;
        }
        if (proxyPort.getText().isEmpty()
                || Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) > 65535) {
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("settings.proxyportinvalid"),
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
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
        final ProgressDialog dialog = new ProgressDialog(
                App.settings.getLocalizedString("settings.checkingproxytitle"), 0,
                App.settings.getLocalizedString("settings.checkingproxy"), "Cancelled Proxy Test!");
        dialog.addThread(new Thread() {
            @Override
            public void run() {
                dialog.setReturnValue(Utils.testProxy(
                        new Proxy(theType, new InetSocketAddress(proxyHost.getText(), Integer
                                .parseInt(proxyPort.getText().replaceAll("[^0-9]", "")))), Integer
                                .parseInt(connectionTimeout.getText().replaceAll("[^0-9]", ""))));
                dialog.close();
            }
        });
        dialog.start();

        if (dialog.getReturnValue() == null) {
            return false;
        }

        if (!(Boolean) dialog.getReturnValue()) {
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("settings.proxycannotconnect"),
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }

        return true;
    }

    public void save() {
        App.settings.setServer((Server) server.getSelectedItem());
        App.settings.setConnectionTimeout(Integer.parseInt(connectionTimeout.getText().replaceAll(
                "[^0-9]", "")));
        App.settings.setConcurrentConnections(Integer.parseInt(concurrentConnections.getText()
                .replaceAll("[^0-9]", "")));
        App.settings.setEnableProxy(enableProxy.isSelected());
        if (enableProxy.isSelected()) {
            App.settings.setProxyHost(proxyHost.getText());
            App.settings.setProxyPort(Integer
                    .parseInt(proxyPort.getText().replaceAll("[^0-9]", "")));
            App.settings.setProxyType(((String) proxyType.getSelectedItem()));
        }
    }
}
