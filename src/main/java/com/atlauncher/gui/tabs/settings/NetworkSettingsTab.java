/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.awt.*;
import java.awt.event.ItemEvent;

import javax.swing.*;

import com.atlauncher.viewmodel.base.settings.INetworkSettingsViewModel;
import com.atlauncher.viewmodel.base.settings.INetworkSettingsViewModel.ProxyType;
import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.viewmodel.impl.settings.NetworkSettingsViewModel;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;

import static com.atlauncher.constants.UIConstants.SPACING_SMALL;

@SuppressWarnings("serial")
public class NetworkSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private final JLabelWithHover concurrentConnectionsLabel;
    private final JSpinner concurrentConnections;

    private final JLabelWithHover connectionTimeoutLabel;
    private final JSpinner connectionTimeout;

    private final JLabelWithHover dontUseHttp2Label;
    private final JCheckBox dontUseHttp2;

    private final JLabelWithHover enableProxyLabel;
    private final JCheckBox enableProxy;

    private final JLabelWithHover proxyHostLabel;
    private JTextField proxyHost;

    private final JLabelWithHover proxyPortLabel;
    private JSpinner proxyPort;

    private final JLabelWithHover proxyTypeLabel;
    private JComboBox<ComboItem<ProxyType>> proxyType;

    public NetworkSettingsTab() {
        INetworkSettingsViewModel viewModel = new NetworkSettingsViewModel();
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
        SpinnerNumberModel concurrentConnectionsModel = new SpinnerNumberModel(viewModel.getConcurrentConnections(), null,
            null, 1);
        concurrentConnectionsModel.setMinimum(1);
        concurrentConnectionsModel.addChangeListener(changeEvent ->
            viewModel.setConcurrentConnections((Integer) concurrentConnectionsModel.getValue()));
        viewModel.addOnConcurrentConnectionsChanged(concurrentConnectionsModel::setValue);
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
        SpinnerNumberModel connectionTimeoutModel = new SpinnerNumberModel(viewModel.getConnectionTimeout(), null, null,
            1);
        connectionTimeoutModel.addChangeListener(changeEvent ->
            viewModel.setConnectionTimeout((Integer) connectionTimeoutModel.getValue()));
        viewModel.addOnConnectionTimeoutChanged(connectionTimeoutModel::setValue);
        connectionTimeoutModel.setMinimum(1);
        connectionTimeout = new JSpinner(connectionTimeoutModel);
        add(connectionTimeout, gbc);

        // Don't use HTTP2
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        dontUseHttp2Label = new JLabelWithHover(GetText.tr("Don't Use HTTP/2") + "?", HELP_ICON, GetText
            .tr("If HTTP/2 connections shouldn't be used. This should not be checked in a majority of cases."));
        add(dontUseHttp2Label, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dontUseHttp2 = new JCheckBox();
        dontUseHttp2.addItemListener(itemEvent ->
            viewModel.setDoNotUseHTTP2(itemEvent.getStateChange() == ItemEvent.SELECTED));
        viewModel.addOnDoNotUseHTTP2Changed(dontUseHttp2::setSelected);
        add(dontUseHttp2, gbc);

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
        enableProxy.addItemListener(itemEvent ->
            viewModel.setEnableProxy(itemEvent.getStateChange() == ItemEvent.SELECTED));
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
        proxyHost.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setProxyHost(proxyHost.getText()),
            viewModel::setProxyHostPending
        ));
        viewModel.addOnProxyHostChanged(proxyHostText ->
        {
            if (!proxyHost.getText().equals(proxyHostText)) {
                proxyHost.setText(proxyHostText);
            }
        });
        add(proxyHost, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(0, SPACING_SMALL, 0, 0);

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
        SpinnerNumberModel proxyPortModel = new SpinnerNumberModel(viewModel.getProxyPort(), null, null, 1);
        proxyPortModel.setMinimum(1);
        proxyPortModel.setMaximum(65535);
        proxyPortModel.addChangeListener(changeEvent ->
            viewModel.setProxyPort((Integer) proxyPortModel.getValue()));
        viewModel.addOnProxyPortChanged(proxyPortModel::setValue);
        proxyPort = new JSpinner(proxyPortModel);
        proxyPort.setEditor(new JSpinner.NumberEditor(proxyPort, "#"));
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
        proxyType.addItem(new ComboItem<>(ProxyType.HTTP, "HTTP"));
        proxyType.addItem(new ComboItem<>(ProxyType.SOCKS, "SOCKS"));
        proxyType.addItem(new ComboItem<>(ProxyType.DIRECT, "DIRECT"));
        proxyType.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    @SuppressWarnings("unchecked")
                    ComboItem<ProxyType> item =
                        (ComboItem<ProxyType>) itemEvent.getItem();
                    viewModel.setProxyType(item.getValue());
                }
            }
        );
        viewModel.addOnProxyTypeChanged(proxyType::setSelectedIndex);
        add(proxyType, gbc);

        viewModel.addOnEnableProxyChanged(enabled -> {
            enableProxy.setSelected(enabled);
            proxyHost.setEnabled(enabled);
            proxyPort.setEnabled(enabled);
            proxyType.setEnabled(enabled);
        });
    }

    @Override
    public String getTitle() {
        return GetText.tr("Network");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Network";
    }

    @Override
    public void onRelocalization() {
        this.concurrentConnectionsLabel.setText(GetText.tr("Concurrent Connections") + ":");
        this.concurrentConnectionsLabel.setToolTipText("<html>"
            + GetText.tr("This determines how many connections will be made when downloading files.") + "</html>");

        this.connectionTimeoutLabel.setText(GetText.tr("Connection Timeout") + ":");
        this.connectionTimeoutLabel.setToolTipText(
            "<html>" + GetText.tr("This determines how long connections will wait before timing out.") + "</html>");

        this.dontUseHttp2Label.setText(GetText.tr("Don't Use HTTP/2") + "?");
        this.dontUseHttp2Label.setToolTipText(GetText
            .tr("If HTTP/2 connections shouldn't be used. This should not be checked in a majority of cases."));

        this.enableProxyLabel.setText(GetText.tr("Don't Use HTTP/2") + "?");
        this.enableProxyLabel.setToolTipText(GetText
            .tr("If HTTP/2 connections shouldn't be used. This should not be checked in a majority of cases."));

        this.proxyHostLabel.setText(GetText.tr("Proxy Host") + ":");
        this.proxyHostLabel.setToolTipText(GetText.tr("This is the IP/hostname used to connect to the proxy."));

        this.proxyPortLabel.setText(GetText.tr("Proxy Port") + ":");
        this.proxyPortLabel.setToolTipText(GetText.tr("This is the port used to connect to the proxy."));

        this.proxyTypeLabel.setText(GetText.tr("Proxy Type") + ":");
        this.proxyTypeLabel.setToolTipText(
            GetText.tr("This is the type of connection the proxy uses. Either HTTP, SOCKS or DIRECT."));
    }
}
