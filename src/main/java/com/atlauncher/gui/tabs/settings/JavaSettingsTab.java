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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.javafinder.JavaInfo;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab implements RelocalizationListener, SettingsListener {
    private final JLabelWithHover initialMemoryLabel;
    private final JSpinner initialMemory;
    private final JLabelWithHover initialMemoryLabelWarning;

    private final JLabelWithHover maximumMemoryLabel;
    private final JSpinner maximumMemory;

    private final JLabelWithHover permGenLabel;
    private final JSpinner permGen;

    private final JLabelWithHover windowSizeLabel;
    private final JSpinner widthField;
    private final JSpinner heightField;
    private final JComboBox<String> commonScreenSizes;
    private final JLabelWithHover javaPathLabel;
    private JTextField javaPath;
    private final JComboBox<JavaInfo> installedJavas;
    private final JButton javaPathResetButton;
    private final JButton javaBrowseButton;
    private final JLabelWithHover javaParametersLabel;
    private final JTextArea javaParameters;
    private final JButton javaParametersResetButton;
    private final JLabelWithHover startMinecraftMaximisedLabel;
    private final JCheckBox startMinecraftMaximised;
    private final JLabelWithHover ignoreJavaOnInstanceLaunchLabel;
    private final JCheckBox ignoreJavaOnInstanceLaunch;

    public JavaSettingsTab() {
        int systemRam = OS.getSystemRam();

        RelocalizationManager.addListener(this);
        SettingsManager.addListener(this);

        // Initial Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON, new HTMLBuilder().center().split(100).text(GetText
                .tr("You're running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."))
                .build(), RESTART_BORDER);

        initialMemoryLabel = new JLabelWithHover(GetText.tr("Initial Memory/Ram") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "Initial memory/ram is the starting amount of memory/ram to use when starting Minecraft. This should be left at the default of 512 MB unless you know what your doing."))
                        .build());

        JPanel initialMemoryPanel = new JPanel();
        initialMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!OS.is64Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel initialMemoryModel = new SpinnerNumberModel(App.settings.initialMemory, null, null, 128);
        initialMemoryModel.setMinimum(128);
        initialMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        initialMemory = new JSpinner(initialMemoryModel);
        ((JSpinner.DefaultEditor) initialMemory.getEditor()).getTextField().setColumns(5);
        add(initialMemory, gbc);

        // Maximum Memory Settings
        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        maximumMemoryLabel = new JLabelWithHover(GetText.tr("Maximum Memory/Ram") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100)
                        .text(GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft."))
                        .build());
        add(maximumMemoryLabel, gbc);

        JPanel maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!OS.is64Bit()) {
            maximumMemoryPanel.add(new JLabelWithHover(WARNING_ICON, new HTMLBuilder().center().split(100).text(GetText
                    .tr("You're running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."))
                    .build(), RESTART_BORDER));
        }
        maximumMemoryPanel.add(maximumMemoryLabel);

        add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel maximumMemoryModel = new SpinnerNumberModel(App.settings.maximumMemory, null, null, 512);
        maximumMemoryModel.setMinimum(512);
        maximumMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        maximumMemory = new JSpinner(maximumMemoryModel);
        ((JSpinner.DefaultEditor) maximumMemory.getEditor()).getTextField().setColumns(5);
        add(maximumMemory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        permGenLabel = new JLabelWithHover(GetText.tr("PermGen Size") + ":", HELP_ICON,
                GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));
        add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel permGenModel = new SpinnerNumberModel(App.settings.metaspace, null, null, 32);
        permGenModel.setMinimum(32);
        permGenModel.setMaximum((systemRam == 0 ? null : systemRam));
        permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
        windowSizeLabel = new JLabelWithHover(GetText.tr("Window Size") + ":", HELP_ICON,
                GetText.tr("The size that the Minecraft window should open as, Width x Height, in pixels."));
        add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new BoxLayout(windowSizePanel, BoxLayout.X_AXIS));

        SpinnerNumberModel widthModel = new SpinnerNumberModel(App.settings.windowWidth, 1, OS.getMaximumWindowWidth(),
                1);
        widthField = new JSpinner(widthModel);
        widthField.setEditor(new JSpinner.NumberEditor(widthField, "#"));

        SpinnerNumberModel heightModel = new SpinnerNumberModel(App.settings.windowHeight, 1,
                OS.getMaximumWindowHeight(), 1);
        heightField = new JSpinner(heightModel);
        heightField.setEditor(new JSpinner.NumberEditor(heightField, "#"));

        commonScreenSizes = new JComboBox<>();
        commonScreenSizes.addItem("Select An Option");

        for (String screenSize : Constants.SCREEN_RESOLUTIONS) {
            String[] size = screenSize.split("x");
            if (OS.getMaximumWindowWidth() >= Integer.parseInt(size[0])
                    && OS.getMaximumWindowHeight() >= Integer.parseInt(size[1])) {
                commonScreenSizes.addItem(screenSize);
            }
        }
        commonScreenSizes.addActionListener(e -> {
            String selected = (String) commonScreenSizes.getSelectedItem();
            if (selected.contains("x")) {
                String[] parts = selected.split("x");
                widthField.setValue(Integer.parseInt(parts[0]));
                heightField.setValue(Integer.parseInt(parts[1]));
            }
        });
        commonScreenSizes.setPreferredSize(new Dimension(commonScreenSizes.getPreferredSize().width + 10,
                commonScreenSizes.getPreferredSize().height));

        windowSizePanel.add(widthField);
        windowSizePanel.add(Box.createHorizontalStrut(5));
        windowSizePanel.add(new JLabel("x"));
        windowSizePanel.add(Box.createHorizontalStrut(5));
        windowSizePanel.add(heightField);
        windowSizePanel.add(Box.createHorizontalStrut(5));
        windowSizePanel.add(commonScreenSizes);

        add(windowSizePanel, gbc);

        // Java Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to specify where your Java Path is. This should be left as default, but if you know what your doing just set this to the path where the bin folder is for the version of Java you want to use If you mess up, click the Reset button to go back to the default"))
                        .build());
        add(javaPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel javaPathPanel = new JPanel();
        javaPathPanel.setLayout(new BoxLayout(javaPathPanel, BoxLayout.Y_AXIS));

        JPanel javaPathPanelTop = new JPanel();
        javaPathPanelTop.setLayout(new BoxLayout(javaPathPanelTop, BoxLayout.X_AXIS));

        JPanel javaPathPanelBottom = new JPanel();
        javaPathPanelBottom.setLayout(new BoxLayout(javaPathPanelBottom, BoxLayout.X_AXIS));

        installedJavas = new JComboBox<>();
        installedJavas.setPreferredSize(new Dimension(516, 24));
        if (Java.getInstalledJavas().size() != 0) {
            Java.getInstalledJavas().forEach(installedJavas::addItem);

            installedJavas.setSelectedItem(Java.getInstalledJavas().stream()
                    .filter(javaInfo -> javaInfo.rootPath.equalsIgnoreCase(App.settings.javaPath)).findFirst()
                    .orElse(null));

            installedJavas
                    .addActionListener(e -> javaPath.setText(((JavaInfo) installedJavas.getSelectedItem()).rootPath));
        }

        if (installedJavas.getItemCount() != 0) {
            javaPathPanelTop.add(installedJavas);
        }

        javaPath = new JTextField(32);
        javaPath.setText(App.settings.javaPath);
        javaPathResetButton = new JButton(GetText.tr("Reset"));
        javaPathResetButton.addActionListener(e -> javaPath.setText(OS.getDefaultJavaPath()));
        javaBrowseButton = new JButton(GetText.tr("Browse"));
        javaBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(javaPath.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                javaPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        javaPathPanelBottom.add(javaPath);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));
        javaPathPanelBottom.add(javaPathResetButton);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));
        javaPathPanelBottom.add(javaBrowseButton);

        javaPathPanel.add(javaPathPanelTop);
        javaPathPanel.add(Box.createVerticalStrut(5));
        javaPathPanel.add(javaPathPanelBottom);

        add(javaPathPanel, gbc);

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        javaParametersLabel = new JLabelWithHover(GetText.tr("Java Parameters") + ":", HELP_ICON,
                GetText.tr("Extra Java command line paramaters can be added here."));
        add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        JPanel javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new BoxLayout(javaParametersPanel, BoxLayout.X_AXIS));
        javaParametersPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        javaParameters = new JTextArea(6, 40);
        javaParameters.setText(App.settings.javaParameters);
        javaParameters.setLineWrap(true);
        javaParameters.setWrapStyleWord(true);

        javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e -> javaParameters.setText(Constants.DEFAULT_JAVA_PARAMETERS));

        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(Box.createHorizontalStrut(5));

        Box paramsResetBox = Box.createVerticalBox();
        paramsResetBox.add(javaParametersResetButton);
        paramsResetBox.add(Box.createVerticalGlue());
        javaParametersPanel.add(paramsResetBox);

        add(javaParametersPanel, gbc);

        // Start Minecraft Maximised

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        startMinecraftMaximisedLabel = new JLabelWithHover(GetText.tr("Start Minecraft Maximised") + "?", HELP_ICON,
                GetText.tr(
                        "Enabling this will start Minecraft maximised so that it takes up the full size of your screen."));
        add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        startMinecraftMaximised = new JCheckBox();
        if (App.settings.maximiseMinecraft) {
            startMinecraftMaximised.setSelected(true);
        }
        add(startMinecraftMaximised, gbc);

        // Ignore Java checks On Launch

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        ignoreJavaOnInstanceLaunchLabel = new JLabelWithHover(GetText.tr("Ignore Java Checks On Launch") + "?",
                HELP_ICON, GetText.tr(
                        "This enables ignoring errors when launching a pack that you don't have a compatable Java version for."));
        add(ignoreJavaOnInstanceLaunchLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        ignoreJavaOnInstanceLaunch = new JCheckBox();
        if (App.settings.ignoreJavaOnInstanceLaunch) {
            ignoreJavaOnInstanceLaunch.setSelected(true);
        }
        add(ignoreJavaOnInstanceLaunch, gbc);
    }

    public boolean isValidJavaPath() {
        File jPath = new File(javaPath.getText(), "bin");
        if (!jPath.exists()) {
            DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent(new HTMLBuilder().center().text(GetText.tr(
                    "The Java Path you set is incorrect.<br/><br/>Please verify it points to the folder where the bin folder is and try again."))
                    .build()).setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public boolean isValidJavaParamaters() {
        if (javaParameters.getText().contains("-Xms") || javaParameters.getText().contains("-Xmx")
                || javaParameters.getText().contains("-XX:PermSize")
                || javaParameters.getText().contains("-XX:MetaspaceSize")) {
            DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent(new HTMLBuilder().center().text(GetText.tr(
                    "The entered Java Parameters were incorrect.<br/><br/>Please remove any references to Xmx, Xms or XX:PermSize."))
                    .build()).setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public void save() {
        App.settings.initialMemory = (Integer) initialMemory.getValue();
        App.settings.maximumMemory = (Integer) maximumMemory.getValue();
        App.settings.metaspace = (Integer) permGen.getValue();
        App.settings.windowWidth = (Integer) widthField.getValue();
        App.settings.windowHeight = (Integer) heightField.getValue();
        App.settings.javaPath = javaPath.getText();
        App.settings.javaParameters = javaParameters.getText();
        App.settings.maximiseMinecraft = startMinecraftMaximised.isSelected();
        App.settings.ignoreJavaOnInstanceLaunch = ignoreJavaOnInstanceLaunch.isSelected();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Java/Minecraft");
    }

    @Override
    public void onRelocalization() {
        this.initialMemoryLabelWarning.setToolTipText(new HTMLBuilder().center().split(100).text(GetText.tr(
                "You're running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."))
                .build());

        this.initialMemoryLabel.setText(GetText.tr("Initial Memory/Ram") + ":");
        this.initialMemoryLabel.setToolTipText(new HTMLBuilder().center().split(100).text(GetText.tr(
                "Initial memory/ram is the starting amount of memory/ram to use when starting Minecraft. This should be left at the default of 512 MB unless you know what your doing."))
                .build());

        this.maximumMemoryLabel.setText(GetText.tr("Maximum Memory/Ram") + ":");
        this.maximumMemoryLabel.setToolTipText(new HTMLBuilder().center().split(100)
                .text(GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft.")).build());

        this.permGenLabel.setText(GetText.tr("PermGen Size") + ":");
        this.permGenLabel
                .setToolTipText(GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));

        this.windowSizeLabel.setText(GetText.tr("Window Size") + ":");
        this.windowSizeLabel.setToolTipText(
                GetText.tr("The size that the Minecraft window should open as, Width x Height, in pixels."));

        this.javaPathLabel.setText(GetText.tr("Java Path") + ":");
        this.javaPathLabel.setToolTipText(new HTMLBuilder().center().split(100).text(GetText.tr(
                "This setting allows you to specify where your Java Path is. This should be left as default, but if you know what your doing just set this to the path where the bin folder is for the version of Java you want to use If you mess up, click the Reset button to go back to the default"))
                .build());

        this.javaPathResetButton.setText(GetText.tr("Reset"));

        this.javaBrowseButton.setText(GetText.tr("Browse"));

        this.javaParametersLabel.setText(GetText.tr("Java Parameters") + ":");
        this.javaParametersLabel.setToolTipText(GetText.tr("Extra Java command line paramaters can be added here."));

        this.javaParametersResetButton.setText(GetText.tr("Reset"));

        this.startMinecraftMaximisedLabel.setText(GetText.tr("Start Minecraft Maximised") + "?");
        this.startMinecraftMaximisedLabel.setToolTipText(GetText
                .tr("Enabling this will start Minecraft maximised so that it takes up the full size of your screen."));

        this.ignoreJavaOnInstanceLaunchLabel.setText(GetText.tr("Ignore Java checks On Launch") + "?");
        this.ignoreJavaOnInstanceLaunchLabel.setToolTipText(GetText.tr(
                "This enables ignoring errors when launching a pack that you don't have a compatable Java version for."));
    }

    @Override
    public void onSettingsSaved() {
        javaPath.setText(App.settings.javaPath);
    }
}
