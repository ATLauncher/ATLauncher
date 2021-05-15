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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.javafinder.JavaInfo;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class InstanceSettingsDialog extends JDialog {
    private Instance instance;

    private final JPanel topPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
    final ImageIcon ERROR_ICON = Utils.getIconImage(App.THEME.getIconPath("error"));
    final ImageIcon WARNING_ICON = Utils.getIconImage(App.THEME.getIconPath("warning"));

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);

    final GridBagConstraints gbc = new GridBagConstraints();

    public InstanceSettingsDialog(Instance instance) {
        super(App.launcher.getParent(), GetText.tr("{0} Settings", instance.launcher.name),
                ModalityType.DOCUMENT_MODAL);
        this.instance = instance;

        setupComponents();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void setupComponents() {
        int systemRam = OS.getSystemRam();
        setSize(750, 400);
        setMinimumSize(new Dimension(750, 400));
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        topPanel.setLayout(new GridBagLayout());
        // Initial Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON,
                "<html>" + Utils.splitMultilinedString(GetText.tr(
                        "You are running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."),
                        80, "<br/>") + "</html>",
                RESTART_BORDER);

        JLabelWithHover initialMemoryLabel = new JLabelWithHover(GetText.tr("Initial Memory/Ram") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(GetText.tr(
                        "Initial memory/ram is the starting amount of memory/ram to use when starting Minecraft. This should be left at the default of 512 MB unless you know what your doing."),
                        80, "<br/>") + "</html>");

        JPanel initialMemoryPanel = new JPanel();
        initialMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!OS.is64Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        topPanel.add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel initialMemoryModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.initialMemory, App.settings.initialMemory), null, null, 128);
        initialMemoryModel.setMinimum(128);
        initialMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        final JSpinner initialMemory = new JSpinner(initialMemoryModel);
        ((JSpinner.DefaultEditor) initialMemory.getEditor()).getTextField().setColumns(5);
        topPanel.add(initialMemory, gbc);

        // Maximum Memory Settings
        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover maximumMemoryLabel = new JLabelWithHover(GetText.tr("Maximum Memory/Ram") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(
                        GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft."), 80,
                        "<br/>") + "</html>");
        topPanel.add(maximumMemoryLabel, gbc);

        JPanel maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        maximumMemoryPanel.add(maximumMemoryLabel);

        topPanel.add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel maximumMemoryModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.maximumMemory, App.settings.maximumMemory), null, null, 512);
        maximumMemoryModel.setMinimum(512);
        maximumMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        final JSpinner maximumMemory = new JSpinner(maximumMemoryModel);
        ((JSpinner.DefaultEditor) maximumMemory.getEditor()).getTextField().setColumns(5);
        topPanel.add(maximumMemory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover permGenLabel = new JLabelWithHover(GetText.tr("PermGen Size") + ":", HELP_ICON,
                GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));
        topPanel.add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel permGenModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.permGen, App.settings.metaspace), null, null, 32);
        permGenModel.setMinimum(32);
        permGenModel.setMaximum((systemRam == 0 ? null : systemRam));
        final JSpinner permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        topPanel.add(permGen, gbc);

        // Java Path
        final JTextField javaPath = new JTextField(32);
        if (App.settings.useJavaProvidedByMinecraft && instance.javaVersion != null) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                    new HTMLBuilder().center().text(GetText.tr(
                            "This version of Minecraft provides a specific version of Java to be used with it, so you cannot set a custom Java path.<br/><br/>In order to manually set a path, you must disable this option (highly not recommended) in the Java settings of the launcher."))
                            .build());
            topPanel.add(javaPathLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            final JLabel javaPathDummy = new JLabel("Uses Java provided by Minecraft");
            javaPathDummy.setEnabled(false);
            topPanel.add(javaPathDummy, gbc);
        } else {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabelWithHover javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                    "<html>" + GetText.tr(
                            "This setting allows you to specify where your Java Path is.<br/><br/>This should be left as default, but if you know what your doing just set<br/>this to the path where the bin folder is for the version of Java you want to use<br/><br/>If you mess up, click the Reset button to go back to the default")
                            + "</html>");
            topPanel.add(javaPathLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;

            JPanel javaPathPanel = new JPanel();
            javaPathPanel.setLayout(new BoxLayout(javaPathPanel, BoxLayout.Y_AXIS));

            JPanel javaPathPanelTop = new JPanel();
            javaPathPanelTop.setLayout(new BoxLayout(javaPathPanelTop, BoxLayout.X_AXIS));

            JPanel javaPathPanelBottom = new JPanel();
            javaPathPanelBottom.setLayout(new BoxLayout(javaPathPanelBottom, BoxLayout.X_AXIS));

            javaPath.setText(getIfNotNull(this.instance.launcher.javaPath, App.settings.javaPath));
            JButton javaPathResetButton = new JButton(GetText.tr("Reset"));
            javaPathResetButton.addActionListener(e -> javaPath.setText(OS.getDefaultJavaPath()));
            JButton javaBrowseButton = new JButton(GetText.tr("Browse"));
            javaBrowseButton.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(javaPath.getText()));
                chooser.setDialogTitle(GetText.tr("Select path to Java install"));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    javaPath.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });

            JComboBox<JavaInfo> installedJavas = new JComboBox<>();
            installedJavas.setPreferredSize(new Dimension(516, 24));
            if (Java.getInstalledJavas().size() != 0) {
                Java.getInstalledJavas().forEach(installedJavas::addItem);

                installedJavas.setSelectedItem(Java.getInstalledJavas().stream()
                        .filter(javaInfo -> javaInfo.rootPath.equalsIgnoreCase(App.settings.javaPath)).findFirst()
                        .orElse(null));

                installedJavas.addActionListener(
                        e -> javaPath.setText(((JavaInfo) installedJavas.getSelectedItem()).rootPath));
            }

            if (installedJavas.getItemCount() != 0) {
                javaPathPanelTop.add(installedJavas);
            }

            javaPathPanelBottom.add(javaPath);
            javaPathPanelBottom.add(Box.createHorizontalStrut(5));
            javaPathPanelBottom.add(javaPathResetButton);
            javaPathPanelBottom.add(Box.createHorizontalStrut(5));
            javaPathPanelBottom.add(javaBrowseButton);

            javaPathPanel.add(javaPathPanelTop);
            javaPathPanel.add(Box.createVerticalStrut(5));
            javaPathPanel.add(javaPathPanelBottom);

            topPanel.add(javaPathPanel, gbc);
        }

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        JLabelWithHover javaParametersLabel = new JLabelWithHover(GetText.tr("Java Parameters") + ":", HELP_ICON,
                GetText.tr("Extra Java command line paramaters can be added here."));
        topPanel.add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        JPanel javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new BoxLayout(javaParametersPanel, BoxLayout.X_AXIS));

        final JTextArea javaParameters = new JTextArea(6, 40);
        javaParameters.setText(getIfNotNull(this.instance.launcher.javaArguments, App.settings.javaParameters));
        javaParameters.setLineWrap(true);
        javaParameters.setWrapStyleWord(true);
        JButton javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e -> javaParameters.setText(App.settings.javaParameters));

        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(Box.createHorizontalStrut(5));

        Box paramsResetBox = Box.createVerticalBox();
        paramsResetBox.add(javaParametersResetButton);
        paramsResetBox.add(Box.createVerticalGlue());

        javaParametersPanel.add(paramsResetBox);

        topPanel.add(javaParametersPanel, gbc);

        // Account

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover accountLabel = new JLabelWithHover(GetText.tr("Account Override") + ":", HELP_ICON, GetText.tr(
                "Which account to use when launching this instance. Use Launcher Default will use whichever account is selected in the launcher."));

        topPanel.add(accountLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JComboBox<ComboItem<String>> account = new JComboBox<>();
        account.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        AccountManager.getAccounts().stream()
                .forEach(a -> account.addItem(new ComboItem<>(a.username, a.minecraftUsername)));

        for (int i = 0; i < account.getItemCount(); i++) {
            ComboItem<String> item = account.getItemAt(i);

            if (item.getValue() != null && item.getValue().equalsIgnoreCase(instance.launcher.account)) {
                account.setSelectedIndex(i);
                break;
            }
        }

        topPanel.add(account, gbc);

        bottomPanel.setLayout(new FlowLayout());
        JButton saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(arg0 -> {
            saveSettings((Integer) initialMemory.getValue(), (Integer) maximumMemory.getValue(),
                    (Integer) permGen.getValue(), javaPath.getText(), javaParameters.getText(),
                    ((ComboItem<String>) account.getSelectedItem()).getValue());
            App.TOASTER.pop("Instance Settings Saved");
            close();
        });
        bottomPanel.add(saveButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    private Integer getIfNotNull(Integer value, Integer defaultValue) {
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    private String getIfNotNull(String value, String defaultValue) {
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    private void saveSettings(Integer initialMemory, Integer maximumMemory, Integer permGen, String javaPath,
            String javaParameters, String account) {
        this.instance.launcher.initialMemory = (initialMemory == App.settings.initialMemory ? null : initialMemory);
        this.instance.launcher.maximumMemory = (maximumMemory == App.settings.maximumMemory ? null : maximumMemory);
        this.instance.launcher.permGen = (permGen == App.settings.metaspace ? null : permGen);

        if (!App.settings.useJavaProvidedByMinecraft || instance.javaVersion == null) {
            this.instance.launcher.javaPath = (javaPath.equals(App.settings.javaPath) ? null : javaPath);
        }

        this.instance.launcher.javaArguments = (javaParameters.equals(App.settings.javaParameters) ? null
                : javaParameters);
        this.instance.launcher.account = account;
        this.instance.launcher.account = account;
        this.instance.save();
    }

}
