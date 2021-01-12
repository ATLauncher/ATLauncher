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
package com.atlauncher.gui.tabs.settings;

import java.awt.GridBagConstraints;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class NetworkSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private final JLabelWithHover concurrentConnectionsLabel;
    private final JSpinner concurrentConnections;

    private final JLabelWithHover connectionTimeoutLabel;
    private final JSpinner connectionTimeout;

    private final JLabelWithHover enableProxyLabel;
    private final JCheckBox enableProxy;

    private final JLabelWithHover proxyHostLabel;
    private JTextField proxyHost;

    private final JLabelWithHover proxyPortLabel;
    private JSpinner proxyPort;

    private final JLabelWithHover proxyTypeLabel;
    private JComboBox<String> proxyType;

    public NetworkSettingsTab() {
        RelocalizationManager.addListener(this);

        // Concurrent Connection Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        concurrentConnectionsLabel = new JLabelWithHover(GetText.tr("Concurrent Connections") + ":", HELP_ICON, "<html>"
                + GetText.tr("This determines how many connections will be made when downloading files.") + "</html>");
        add(concurrentConnectionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel concurrentConnectionsModel = new SpinnerNumberModel(App.settings.concurrentConnections, null,
                null, 1);
        concurrentConnectionsModel.setMinimum(1);
        concurrentConnections = new JSpinner(concurrentConnectionsModel);
        add(concurrentConnections, gbc);

        // Connection Timeout Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        connectionTimeoutLabel = new JLabelWithHover(GetText.tr("Connection Timeout") + ":", HELP_ICON,
                "<html>" + GetText.tr("This determines how long connections will wait before timing out.") + "</html>");
        add(connectionTimeoutLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel connectionTimeoutModel = new SpinnerNumberModel(App.settings.connectionTimeout, null, null,
                1);
        connectionTimeoutModel.setMinimum(1);
        connectionTimeout = new JSpinner(connectionTimeoutModel);
        add(connectionTimeout, gbc);

        // Enable Proxy
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableProxyLabel = new JLabelWithHover(GetText.tr("Enable Proxy") + "?", HELP_ICON,
                GetText.tr("If you use a proxy to connect to the internet you can enable it here."));
        add(enableProxyLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableProxy = new JCheckBox();
        if (App.settings.enableProxy) {
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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyHostLabel = new JLabelWithHover(GetText.tr("Proxy Host") + ":", HELP_ICON,
                GetText.tr("This is the IP/hostname used to connect to the proxy."));
        add(proxyHostLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyHost = new JTextField(20);
        proxyHost.setText(App.settings.proxyHost);
        if (!enableProxy.isSelected()) {
            proxyHost.setEnabled(false);
        }
        add(proxyHost, gbc);

        // Proxy Port Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyPortLabel = new JLabelWithHover(GetText.tr("Proxy Port") + ":", HELP_ICON,
                GetText.tr("This is the port used to connect to the proxy."));
        add(proxyPortLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel proxyPortModel = new SpinnerNumberModel(App.settings.proxyPort, null, null, 1);
        proxyPortModel.setMinimum(1);
        proxyPortModel.setMaximum(65535);
        proxyPort = new JSpinner(proxyPortModel);
        proxyPort.setEditor(new JSpinner.NumberEditor(proxyPort, "#"));
        if (!enableProxy.isSelected()) {
            proxyPort.setEnabled(false);
        }
        add(proxyPort, gbc);

        // Proxy Type Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        proxyTypeLabel = new JLabelWithHover(GetText.tr("Proxy Type") + ":", HELP_ICON,
                GetText.tr("This is the type of connection the proxy uses. Either HTTP, SOCKS or DIRECT."));
        add(proxyTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        proxyType = new JComboBox<>();
        proxyType.addItem("HTTP");
        proxyType.addItem("SOCKS");
        proxyType.addItem("DIRECT");
        proxyType.setSelectedItem(App.settings.proxyType);
        if (!enableProxy.isSelected()) {
            proxyType.setEnabled(false);
        }
        add(proxyType, gbc);
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
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Checking Proxy"), 0,
                GetText.tr("Checking the proxy entered."), "Cancelled Proxy Test!");
        dialog.addThread(new Thread(() -> {
            dialog.setReturnValue(Utils.testProxy(
                    new Proxy(theType, new InetSocketAddress(proxyHost.getText(), (Integer) proxyPort.getValue()))));
            dialog.close();
        }));
        dialog.start();

        if (dialog.getReturnValue() == null) {
            return false;
        }

        if (!dialog.getReturnValue()) {
            DialogManager.okDialog().setTitle(GetText.tr("Help"))
                    .setContent(GetText.tr("Cannot connect to proxy. Please check the settings and try again."))
                    .setType(DialogManager.ERROR).show();
            return false;
        }

        return true;
    }

    public void save() {
        boolean timeoutChanged = App.settings.connectionTimeout != (Integer) connectionTimeout.getValue();

        App.settings.concurrentConnections = (Integer) concurrentConnections.getValue();
        App.settings.connectionTimeout = (Integer) connectionTimeout.getValue();
        App.settings.enableProxy = enableProxy.isSelected();
        if (enableProxy.isSelected()) {
            App.settings.proxyHost = proxyHost.getText();
            App.settings.proxyPort = (Integer) proxyPort.getValue();
            App.settings.proxyType = ((String) proxyType.getSelectedItem());
        }

        if (timeoutChanged) {
            Network.setConnectionTimeouts();
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

        this.connectionTimeoutLabel.setText(GetText.tr("Connection Timeout") + ":");
        this.connectionTimeoutLabel.setToolTipText(
                "<html>" + GetText.tr("This determines how long connections will wait before timing out.") + "</html>");

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
