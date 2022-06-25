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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.*;

import com.atlauncher.constants.Constants.ScreenResolution;
import com.atlauncher.viewmodel.base.settings.IJavaSettingsViewModel;
import com.atlauncher.viewmodel.base.settings.IJavaSettingsViewModel.MaxRamWarning;
import com.atlauncher.data.CheckState;
import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.impl.settings.JavaSettingsViewModel;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
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
    private final JComboBox<ComboItem<ScreenResolution>> commonScreenSizes;
    private final JLabelWithHover javaPathLabel;
    private final JTextField javaPath;
    private final JLabelWithHover javaPathChecker;
    private final JComboBox<String> installedJavasComboBox;
    private final JButton javaPathResetButton;
    private final JButton javaBrowseButton;
    private final JLabelWithHover javaParametersLabel;
    private final JTextArea javaParameters;
    private final JButton javaParametersResetButton;
    private final JLabelWithHover javaParamChecker;
    private final JLabelWithHover startMinecraftMaximisedLabel;
    private final JCheckBox startMinecraftMaximised;
    private final JLabelWithHover ignoreJavaOnInstanceLaunchLabel;
    private final JCheckBox ignoreJavaOnInstanceLaunch;
    private final JLabelWithHover useJavaProvidedByMinecraftLabel;
    private final JCheckBox useJavaProvidedByMinecraft;
    private final JLabelWithHover disableLegacyLaunchingLabel;
    private final JCheckBox disableLegacyLaunching;
    private final JLabelWithHover useSystemGlfwLabel;
    private final JCheckBox useSystemGlfw;
    private final JLabelWithHover useSystemOpenAlLabel;
    private final JCheckBox useSystemOpenAl;


    public JavaSettingsTab() {
        IJavaSettingsViewModel viewModel = new JavaSettingsViewModel();
        RelocalizationManager.addListener(this);

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
        if (viewModel.isJava32Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel initialMemoryModel = new SpinnerNumberModel(App.settings.initialMemory, null, null, 128);
        initialMemoryModel.setMinimum(128);
        initialMemoryModel.setMaximum(viewModel.getSystemRam());
        initialMemory = new JSpinner(initialMemoryModel);
        ((JSpinner.DefaultEditor) initialMemory.getEditor()).getTextField().setColumns(5);
        initialMemory.addChangeListener(e -> {
            JSpinner s = (JSpinner) e.getSource();

            boolean result = viewModel.setInitialRam((Integer) s.getValue());

            if (result) {
                viewModel.setInitialMemoryWarningShown();
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                    .setType(DialogManager.WARNING)
                    .setContent(GetText.tr(
                        "Setting initial memory above 512MB is not recommended and can cause issues. Are you sure you want to do this?"))
                    .show();

                if (ret != 0) {
                    viewModel.setInitialRam(512);
                }
            }
        });
        viewModel.addOnInitialRamChanged(initialMemory::setValue);
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
        if (viewModel.isJava32Bit()) {
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
        maximumMemoryModel.setMaximum(viewModel.getSystemRam());
        maximumMemory = new JSpinner(maximumMemoryModel);
        ((JSpinner.DefaultEditor) maximumMemory.getEditor()).getTextField().setColumns(5);
        maximumMemory.addChangeListener(e -> {
            JSpinner s = (JSpinner) e.getSource();
            MaxRamWarning warning = viewModel.setMaxRam((Integer) s.getValue());
            if (warning == null) return;

            switch (warning) {
                case ABOVE_8GB:
                    viewModel.setMaximumMemoryEightGBWarningShown();

                    DialogManager.okDialog().setTitle(GetText.tr("Warning"))
                        .setType(DialogManager.WARNING)
                        .setContent(GetText.tr(
                            "Setting maximum memory above 8GB is not recommended for most modpacks and can cause issues."))
                        .show();
                    break;
                case ABOVE_HALF:
                    viewModel.setMaximumMemoryHalfWarningShown();
                    DialogManager.okDialog().setTitle(GetText.tr("Warning"))
                        .setType(DialogManager.WARNING)
                        .setContent(GetText.tr(
                            "Setting maximum memory to more than half of your systems total memory is not recommended and can cause issues in some cases. Are you sure you want to do this?"))
                        .show();
                    break;
            }
        });
        viewModel.addOnMaxRamChanged(maximumMemory::setValue);
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
        permGenModel.setMaximum(viewModel.getSystemRam());
        permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        permGen.addChangeListener(e -> {
            JSpinner s = (JSpinner) e.getSource();
            boolean result = viewModel.setPermGen((Integer) s.getValue());
            if (result) {
                viewModel.setPermgenWarningShown();
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                    .setType(DialogManager.WARNING)
                    .setContent(GetText.tr(
                        "Setting PermGen size above {0}MB is not recommended and can cause issues. Are you sure you want to do this?",
                        viewModel.getPermGenMaxRecommendSize()))
                    .show();

                if (ret != 0) {
                    viewModel.setPermGen(viewModel.getPermGenMaxRecommendSize());
                }
            }
        });
        viewModel.addOnPermGenChanged(permGen::setValue);
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
        widthField.addChangeListener(e ->
            viewModel.setWidth((Integer) widthModel.getValue()));
        viewModel.addOnWidthChanged(widthModel::setValue);

        SpinnerNumberModel heightModel = new SpinnerNumberModel(App.settings.windowHeight, 1,
            OS.getMaximumWindowHeight(), 1);
        heightField = new JSpinner(heightModel);
        heightField.setEditor(new JSpinner.NumberEditor(heightField, "#"));
        heightField.addChangeListener(e ->
            viewModel.setHeight((Integer) heightField.getValue()));
        viewModel.addOnHeightChanged(heightField::setValue);

        commonScreenSizes = new JComboBox<>();
        commonScreenSizes.addItem(new ComboItem<>(null, "Select An Option"));

        for (ScreenResolution resolution : viewModel.getScreenResolutions()) {
            commonScreenSizes.addItem(new ComboItem<>(resolution, resolution.toString()));
        }
        commonScreenSizes.addActionListener(e -> {
            Object selectedItem = commonScreenSizes.getSelectedItem();
            @SuppressWarnings("unchecked")
            ComboItem<ScreenResolution> selected =
                (ComboItem<ScreenResolution>) selectedItem;
            ScreenResolution screenResolution = selected.getValue();
            if (screenResolution != null)
                viewModel.setScreenResolution(screenResolution);
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
                    "This setting allows you to specify where your Java Path is. This should be left as default, but if you know what you're doing, just set this to the path where the bin folder is for the version of Java you want to use. If you mess up, click the Reset button to go back to the default"))
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

        installedJavasComboBox = new JComboBox<>();
        installedJavasComboBox.setPreferredSize(new Dimension(516, 24));
        int selectedIndex = 0;

        for (String javaInfo : viewModel.getJavaPaths()) {
            installedJavasComboBox.addItem(javaInfo);

            if (javaInfo.equalsIgnoreCase(viewModel.getJavaPath())) {
                selectedIndex = installedJavasComboBox.getItemCount() - 1;
            }
        }

        if (installedJavasComboBox.getItemCount() != 0) {
            installedJavasComboBox.setSelectedIndex(selectedIndex);
            installedJavasComboBox.addActionListener(e -> {
                    String path = ((String) installedJavasComboBox.getSelectedItem());
                    viewModel.setJavaPath(path);
                }
            );
            javaPathPanelTop.add(installedJavasComboBox);
        }

        javaPath = new JTextField(32);
        javaPathChecker = new JLabelWithHover("", null, null);
        javaPath.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setJavaPath(javaPath.getText()),
            viewModel::setJavaPathPending
        ));

        viewModel.addOnJavaPathChanged(path -> {
            if (!javaPath.getText().equals(path))
                javaPath.setText(path);
        });
        viewModel.addOnJavaPathCheckerListener(this::setJavaPathCheckState);

        javaPathPanelBottom.add(javaPath);

        javaPathPanelBottom.add(Box.createHorizontalStrut(5));

        javaPathPanelBottom.add(javaPathChecker, gbc);

        javaPathPanelBottom.add(Box.createHorizontalStrut(5));

        javaPathResetButton = new JButton(GetText.tr("Reset"));
        javaPathResetButton.addActionListener(e -> {
            viewModel.resetJavaPath();
            resetJavaPathCheckLabel();
        });

        javaPathPanelBottom.add(javaPathResetButton);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));

        javaBrowseButton = new JButton(GetText.tr("Browse"));
        javaBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(viewModel.getJavaPath()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedPath = chooser.getSelectedFile();
                File jPath = new File(selectedPath, "bin");
                File javaExe = new File(selectedPath, "java.exe");
                File javaExecutable = new File(selectedPath, "java");

                // user selected the bin dir
                if (!jPath.exists() && (javaExe.exists() || javaExecutable.exists())) {
                    javaPath.setText(selectedPath.getParent());
                } else {
                    javaPath.setText(selectedPath.getAbsolutePath());
                }
            }
        });

        javaPathPanelBottom.add(javaBrowseButton);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));

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
        javaParamChecker = new JLabelWithHover("", null, null);
        javaParameters.setLineWrap(true);
        javaParameters.setWrapStyleWord(true);
        javaParameters.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setJavaParams(javaParameters.getText()),
            viewModel::setJavaParamsPending
        ));
        viewModel.addOnJavaParamsChanged(params -> {
            if (!javaParameters.getText().equals(params))
                javaParameters.setText(params);
        });
        viewModel.addOnJavaParamsCheckerListener(this::setJavaParamCheckState);

        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(Box.createHorizontalStrut(5));

        Box paramsResetBox = Box.createVerticalBox();
        javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e ->
            viewModel.resetJavaParams()
        );
        paramsResetBox.add(javaParametersResetButton);
        paramsResetBox.add(Box.createVerticalGlue());

        paramsResetBox.add(javaParamChecker, gbc);
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
        startMinecraftMaximised.addItemListener(itemEvent ->
            viewModel.setStartMinecraftMax(itemEvent.getStateChange() == ItemEvent.SELECTED)
        );
        viewModel.addOnStartMinecraftMaxChanged(startMinecraftMaximised::setSelected);

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
        ignoreJavaOnInstanceLaunch.addItemListener(itemEvent ->
            viewModel.setIgnoreJavaChecks(itemEvent.getStateChange() == ItemEvent.SELECTED)
        );
        viewModel.addOnIgnoreJavaChecksChanged(ignoreJavaOnInstanceLaunch::setSelected);
        add(ignoreJavaOnInstanceLaunch, gbc);

        // Use Java Provided By Minecraft

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        useJavaProvidedByMinecraftLabel = new JLabelWithHover(GetText.tr("Use Java Provided By Minecraft") + "?",
            HELP_ICON,
            new HTMLBuilder().center().text(GetText.tr(
                    "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing.{0}",
                    (OS.isArm() && !OS.isMacArm()) ? GetText.tr(
                        "<br/><br/>This setting cannot be changed if using an ARM based computer as it's not compatable and will not be used.")
                        : ""))
                .build());
        add(useJavaProvidedByMinecraftLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useJavaProvidedByMinecraft = new JCheckBox();
        useJavaProvidedByMinecraft.setEnabled(viewModel.getUseJavaFromMinecraftEnabled());
        useJavaProvidedByMinecraft.addItemListener(e ->
            viewModel.setJavaFromMinecraft(e.getStateChange() == ItemEvent.SELECTED));
        viewModel.addOnJavaFromMinecraftChanged(enabled -> {
            useJavaProvidedByMinecraft.setEnabled(enabled);
            if (!enabled) {
                SwingUtilities.invokeLater(() -> {
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                        .setType(DialogManager.WARNING)
                        .setContent(GetText.tr(
                            "Unchecking this is not recommended and may cause Minecraft to no longer run. Are you sure you want to do this?"))
                        .show();

                    if (ret != 0) {
                        viewModel.setJavaFromMinecraft(true);
                    }
                });
            }
        });
        add(useJavaProvidedByMinecraft, gbc);

        // Disable Legacy Launching

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        disableLegacyLaunchingLabel = new JLabelWithHover(GetText.tr("Disable Legacy Launching") + "?", HELP_ICON,
            new HTMLBuilder().center().text(GetText.tr(
                    "This allows you to disable legacy launching for Minecraft < 1.6.<br/><br/>It's highly recommended to not disable this, unless you're having issues launching older Minecraft versions."))
                .build());
        add(disableLegacyLaunchingLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        disableLegacyLaunching = new JCheckBox();
        disableLegacyLaunching.addItemListener(itemEvent ->
            viewModel.setDisableLegacyLaunching(itemEvent.getStateChange() == ItemEvent.SELECTED));
        viewModel.addOnDisableLegacyLaunchingChanged(disableLegacyLaunching::setSelected);
        add(disableLegacyLaunching, gbc);

        // Use System GLFW

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        useSystemGlfwLabel = new JLabelWithHover(GetText.tr("Use System GLFW") + "?", HELP_ICON, new HTMLBuilder()
            .center().text(GetText.tr("Use the systems install for GLFW native library.")).build());
        add(useSystemGlfwLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useSystemGlfw = new JCheckBox();
        useSystemGlfw.addItemListener(itemEvent ->
            viewModel.setSystemGLFW(itemEvent.getStateChange() == ItemEvent.SELECTED));
        viewModel.addOnSystemGLFWChanged(useSystemGlfw::setSelected);
        add(useSystemGlfw, gbc);

        // Use System OpenAL

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        useSystemOpenAlLabel = new JLabelWithHover(GetText.tr("Use System OpenAL") + "?", HELP_ICON, new HTMLBuilder()
            .center().text(GetText.tr("Use the systems install for OpenAL native library.")).build());
        add(useSystemOpenAlLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useSystemOpenAl = new JCheckBox();
        useSystemOpenAl.addItemListener(itemEvent ->
            viewModel.setSystemOpenAL(itemEvent.getStateChange() == ItemEvent.SELECTED));
        viewModel.addOnSystemOpenALChanged(useSystemOpenAl::setSelected);
        add(useSystemOpenAl, gbc);
    }

    private void invalidJavaPath() {
        DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent(new HTMLBuilder().center().text(GetText.tr(
                "The Java Path you set is incorrect.<br/><br/>Please verify it points to the folder where the bin folder is and try again."))
            .build()).setType(DialogManager.ERROR).show();
    }

    private void invalidJavaParameters() {
        DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent(new HTMLBuilder().center().text(GetText.tr(
                "The entered Java Parameters were incorrect.<br/><br/>Please remove any references to Xmx, Xms or XX:PermSize."))
            .build()).setType(DialogManager.ERROR).show();
    }

    private void setLabelState(JLabelWithHover label, String tooltip, String path) {
        try {
            label.setToolTipText(tooltip);
            ImageIcon icon = Utils.getIconImage(path);
            label.setIcon(icon);
            icon.setImageObserver(label);
        } catch (NullPointerException ignored) {
        }
    }

    private void resetJavaPathCheckLabel() {
        setLabelState(javaPathChecker, "Visualize java path checker", "/assets/icon/question.png");
    }

    private void setJavaPathCheckState(CheckState state) {
        if (state instanceof CheckState.NotChecking) {
            resetJavaPathCheckLabel();
        } else if (state instanceof CheckState.CheckPending) {
            setLabelState(javaPathChecker, "Java path change pending", "/assets/icon/warning.png");
        } else if (state instanceof CheckState.Checking) {
            setLabelState(javaPathChecker, "Checking java path", "/assets/image/loading-bars-small.gif");

            javaPath.setEnabled(false);
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetJavaPathCheckLabel();
            } else {
                setLabelState(javaPathChecker, "Invalid!", "/assets/icon/error.png");
                invalidJavaPath();
            }
            javaPath.setEnabled(true);
        }
    }

    private void resetJavaParamCheckLabel() {
        setLabelState(javaParamChecker, "Visualize java param checker", "/assets/icon/question.png");

    }

    private void setJavaParamCheckState(CheckState state) {
        if (state instanceof CheckState.NotChecking) {
            resetJavaParamCheckLabel();
        } else if (state instanceof CheckState.CheckPending) {
            setLabelState(javaParamChecker, "Java params change pending", "/assets/icon/warning.png");
        } else if (state instanceof CheckState.Checking) {
            setLabelState(javaParamChecker, "Checking java params", "/assets/image/loading-bars-small.gif");

            javaParameters.setEnabled(false);
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetJavaParamCheckLabel();
            } else {
                setLabelState(javaParamChecker, "Invalid!", "/assets/icon/error.png");
                invalidJavaParameters();
            }
            javaParameters.setEnabled(true);
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Java/Minecraft");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Java/Minecraft";
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

        this.ignoreJavaOnInstanceLaunchLabel.setText(GetText.tr("Ignore Java Checks On Launch") + "?");
        this.ignoreJavaOnInstanceLaunchLabel.setToolTipText(GetText.tr(
            "This enables ignoring errors when launching a pack that you don't have a compatable Java version for."));

        this.useJavaProvidedByMinecraftLabel.setText(GetText.tr("Use Java Provided By Minecraft") + "?");
        this.useJavaProvidedByMinecraftLabel.setToolTipText(new HTMLBuilder().center().text(GetText.tr(
                "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing."))
            .build());

        this.disableLegacyLaunchingLabel.setText(GetText.tr("Disable Legacy Launching") + "?");
        this.disableLegacyLaunchingLabel.setToolTipText(new HTMLBuilder().center().text(GetText.tr(
                "This allows you to disable legacy launching for Minecraft < 1.6.<br/><br/>It's highly recommended to not disable this, unless you're having issues launching older Minecraft versions."))
            .build());
    }
}
