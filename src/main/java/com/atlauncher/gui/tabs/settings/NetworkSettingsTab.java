/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.GridBagConstraints;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class NetworkSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
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
        RelocalizationManager.addListener(this);
        // Concurrent Connection Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        concurrentConnectionsLabel = new JLabelWithHover(GetText.tr("Concurrent Connections") + ":", HELP_ICON, "<html>"
                + GetText.tr("This determines how many connections will be made when downloading files.") + "</html>");
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
        enableProxyLabel = new JLabelWithHover(GetText.tr("Enable Proxy") + "?", HELP_ICON,
                GetText.tr("If you use a proxy to connect to the internet you can enable it here."));
        add(enableProxyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableProxy = new JCheckBox();
        if (App.settings.getEnableProxy()) {
            enableProxy.setSelected(true);
        }
        enableProxy.addActionListener(e -> {
            if (!enableProxy.isSelected()) {
                proxyHost.setEnabled(false);
                proxyPort.setEnabled(false);
                proxyType.setEnabled(false);
            } else {
                proxyHost.setEnabled(true);
                proxyPort.setEnabled(true);
                proxyType.setEnabled(true);
            }
        });
        add(enableProxy, gbc);

        // Proxy Host Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyHostLabel = new JLabelWithHover(GetText.tr("Proxy Host") + ":", HELP_ICON,
                GetText.tr("This is the IP/hostname used to connect to the proxy."));
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
        proxyPortLabel = new JLabelWithHover(GetText.tr("Proxy Port") + ":", HELP_ICON,
                GetText.tr("This is the port used to connect to the proxy."));
        add(proxyPortLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyPort = new JTextField(4);
        proxyPort.setText((App.settings.getProxyPort() == 0 ? "" : App.settings.getProxyPort()) + "");
        if (!enableProxy.isSelected()) {
            proxyPort.setEnabled(false);
        }
        add(proxyPort, gbc);

        // Proxy Type Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyTypeLabel = new JLabelWithHover(GetText.tr("Proxy Type") + ":", HELP_ICON,
                GetText.tr("This is the type of connection the proxy uses. Either HTTP, SOCKS or DIRECT."));
        add(proxyTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyType = new JComboBox<>();
        proxyType.addItem("HTTP");
        proxyType.addItem("SOCKS");
        proxyType.addItem("DIRECT");
        proxyType.setSelectedItem(App.settings.getProxyType());
        if (!enableProxy.isSelected()) {
            proxyType.setEnabled(false);
        }
        add(proxyType, gbc);
    }

    public boolean isValidConcurrentConnections() {
        if (Integer.parseInt(concurrentConnections.getText().replaceAll("[^0-9]", "")) < 1) {
            DialogManager.okDialog().setTitle(GetText.tr("Help"))
                    .setContent(GetText
                            .tr("The concurrent connections you specified is invalid. Please check it and try again."))
                    .setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public boolean isValidProxyPort() {
        if (!enableProxy.isSelected()) {
            return true;
        }
        if (proxyPort.getText().isEmpty() || Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")) > 65535) {
            DialogManager.okDialog().setTitle(GetText.tr("Help"))
                    .setContent(GetText.tr("The port you specified is invalid. Please check it and try again."))
                    .setType(DialogManager.ERROR).show();
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
        final ProgressDialog dialog = new ProgressDialog(GetText.tr("Checking Proxy"), 0,
                GetText.tr("Checking the proxy entered."), "Cancelled Proxy Test!");
        dialog.addThread(new Thread(() -> {
            dialog.setReturnValue(Utils.testProxy(new Proxy(theType, new InetSocketAddress(proxyHost.getText(),
                    Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", ""))))));
            dialog.close();
        }));
        dialog.start();

        if (dialog.getReturnValue() == null) {
            return false;
        }

        if (!(Boolean) dialog.getReturnValue()) {
            DialogManager.okDialog().setTitle(GetText.tr("Help"))
                    .setContent(GetText.tr("Cannot connect to proxy. Please check the settings and try again."))
                    .setType(DialogManager.ERROR).show();
            return false;
        }

        return true;
    }

    public void save() {
        App.settings
                .setConcurrentConnections(Integer.parseInt(concurrentConnections.getText().replaceAll("[^0-9]", "")));
        App.settings.setEnableProxy(enableProxy.isSelected());
        if (enableProxy.isSelected()) {
            App.settings.setProxyHost(proxyHost.getText());
            App.settings.setProxyPort(Integer.parseInt(proxyPort.getText().replaceAll("[^0-9]", "")));
            App.settings.setProxyType(((String) proxyType.getSelectedItem()));
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Network");
    }

    @Override
    public void onRelocalization() {
        this.concurrentConnectionsLabel.setText(GetText.tr("Concurrent Connections") + ":");
        this.concurrentConnectionsLabel.setToolTipText("<html>"
                + GetText.tr("This determines how many connections will be made when downloading files.") + "</html>");

        this.enableProxyLabel.setText(GetText.tr("Enable Proxy") + "?");
        this.enableProxyLabel
                .setToolTipText(GetText.tr("If you use a proxy to connect to the internet you can enable it here."));

        this.proxyHostLabel.setText(GetText.tr("Proxy Host") + ":");
        this.proxyHostLabel.setToolTipText(GetText.tr("This is the IP/hostname used to connect to the proxy."));

        this.proxyPortLabel.setText(GetText.tr("Proxy Port") + ":");
        this.proxyPortLabel.setToolTipText(GetText.tr("This is the port used to connect to the proxy."));

        this.proxyTypeLabel.setText(GetText.tr("Proxy Type") + ":");
        this.proxyTypeLabel.setToolTipText(
                GetText.tr("This is the type of connection the proxy uses. Either HTTP, SOCKS or DIRECT."));
    }
}
