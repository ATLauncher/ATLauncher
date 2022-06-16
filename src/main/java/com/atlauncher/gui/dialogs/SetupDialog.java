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
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Language;
import com.atlauncher.events.SetupDialogCompleteEvent;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

public class SetupDialog extends JDialog {
    private static final long serialVersionUID = -2931970914611329658L;

    private final JLabel setupLabel;

    private final JLabel languageLabel;
    private final JComboBox<String> language;

    private final JLabel enableAnalyticsLabel;
    private final JCheckBox enableAnalytics;

    private final JButton saveButton;

    public SetupDialog() {
        // #. {0} is the name of the launcher (ATLauncher)
        super(null, GetText.tr("{0} Setup", Constants.LAUNCHER_NAME), ModalityType.DOCUMENT_MODAL);
        this.requestFocus();
        this.setSize(500, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        JPanel top = new JPanel();

        // #. {0} is the name of the launcher (ATLauncher)
        setupLabel = new JLabel(GetText.tr("Setting up {0}", Constants.LAUNCHER_NAME));
        top.add(setupLabel);

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel(GetText.tr("Language") + ": ");
        middle.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<>(Language.locales.stream().map(Locale::getDisplayName).toArray(String[]::new));
        language.setSelectedItem(Language.selected);
        language.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Language.setLanguage((String) language.getSelectedItem());
            }
        });
        middle.add(language, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableAnalyticsLabel = new JLabelWithHover(GetText.tr("Enable Anonymous Analytics") + "? ",
                Utils.getIconImage(App.THEME.getIconPath("question")),
                "<html>" + Utils.splitMultilinedString(GetText.tr(
                        "The Launcher sends back anonymous analytics to Google Analytics in order to track what people do and don't use in the launcher. This helps determine what new features we implement in the future. All analytics are anonymous and contain no user/instance information in it at all. If you don't want to send anonymous analytics, you can disable this option."),
                        80, "<br/>") + "</html>");
        middle.add(enableAnalyticsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FLOW_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel enableAnalyticsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UIConstants.SPACING_LARGE, 0));
        enableAnalytics = new JCheckBox();
        enableAnalytics.setSelected(!App.disableAnalytics);
        enableAnalyticsPanel.add(enableAnalytics);

        JButton privacyPolicyButton = new JButton(GetText.tr("Open Privacy Policy"));
        privacyPolicyButton.addActionListener(e -> {
            OS.openWebBrowser("https://atlauncher.com/privacy-policy");
        });
        enableAnalyticsPanel.add(privacyPolicyButton);

        middle.add(enableAnalyticsPanel, gbc);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(e -> {
            Language.setLanguage((String) language.getSelectedItem());
            App.settings.language = (String) language.getSelectedItem();
            App.settings.enableAnalytics = enableAnalytics.isSelected();
            App.settings.firstTimeRun = false;
            App.settings.save();

            if (enableAnalytics.isSelected()) {
                Analytics.startSession();
                AppEventBus.postToDefault(SetupDialogCompleteEvent.of());
            }

            setVisible(false);
            dispose();
        });
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });

        setVisible(true);

        AppEventBus.register(this);
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        setupLabel.setText(GetText.tr("Setting up {0}", Constants.LAUNCHER_NAME));
        languageLabel.setText(GetText.tr("Language") + ": ");
        enableAnalyticsLabel.setText(GetText.tr("Enable Anonymous Analytics") + "? ");
        enableAnalyticsLabel.setToolTipText("<html>" + Utils.splitMultilinedString(GetText.tr(
                "The Launcher sends back anonymous analytics to Google Analytics in order to track what people do and don't use in the launcher. This helps determine what new features we implement in the future. All analytics are anonymous and contain no user/instance information in it at all. If you don't want to send anonymous analytics, you can disable this option."),
                80, "<br/>") + "</html>");
        saveButton.setText(GetText.tr("Save"));
    }
}
