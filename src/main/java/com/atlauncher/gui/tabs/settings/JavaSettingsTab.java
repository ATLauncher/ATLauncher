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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
import javax.swing.SwingUtilities;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.CheckState;
import com.atlauncher.data.ScreenResolution;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.listener.StatefulTextKeyAdapter;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.impl.settings.JavaSettingsViewModel;

public class JavaSettingsTab extends AbstractSettingsTab {
    private final JavaSettingsViewModel viewModel;

    private JTextField javaPath;
    private JLabelWithHover javaPathChecker;
    private JLabelWithHover javaParamChecker;
    private JTextArea javaParameters;

    public JavaSettingsTab(JavaSettingsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    protected void onShow() {
        Integer systemRam = viewModel.getSystemRam();
        Integer maximumSystemRamForSpinnerModels = systemRam == null || systemRam == 0 ? null : systemRam;

        if (viewModel.useInitialMemoryOption()) {
            // Initial Memory Settings
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

            JLabelWithHover initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON,
                    new HTMLBuilder().center().split(100).text(GetText
                            .tr("You're running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."))
                            .build(),
                    RESTART_BORDER);

            JLabelWithHover initialMemoryLabel = new JLabelWithHover(GetText.tr("Initial Memory/Ram") + ":", HELP_ICON,
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
            initialMemoryModel.setMaximum(maximumSystemRamForSpinnerModels);
            JSpinner initialMemory = new JSpinner(initialMemoryModel);
            ((JSpinner.DefaultEditor) initialMemory.getEditor()).getTextField().setColumns(5);
            initialMemory.addChangeListener(e -> {
                boolean result = viewModel.setInitialRam((Integer) initialMemory.getValue());

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
            add(initialMemory, gbc);
            addDisposable(viewModel.getInitialRam().subscribe(initialMemory::setValue));
        }

        // Maximum Memory Settings
        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover maximumMemoryLabel = new JLabelWithHover(GetText.tr("Maximum Memory/Ram") + ":", HELP_ICON,
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
        maximumMemoryModel.setMaximum(maximumSystemRamForSpinnerModels);
        JSpinner maximumMemory = new JSpinner(maximumMemoryModel);
        ((JSpinner.DefaultEditor) maximumMemory.getEditor()).getTextField().setColumns(5);
        maximumMemory.addChangeListener(e -> {
            viewModel.setMaxRam((Integer) maximumMemory.getValue());
        });
        addDisposable(viewModel.getMaxRam().subscribe(maximumMemory::setValue));
        add(maximumMemory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover permGenLabel = new JLabelWithHover(GetText.tr("PermGen Size") + ":", HELP_ICON,
                GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));
        add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel permGenModel = new SpinnerNumberModel(App.settings.metaspace, null, null, 32);
        permGenModel.setMinimum(32);
        permGenModel.setMaximum(maximumSystemRamForSpinnerModels);
        JSpinner permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        permGen.addChangeListener(e -> {
            boolean result = viewModel.setPermGen((Integer) permGen.getValue());

            if (result) {
                viewModel.setPermgenWarningShown();
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                        .setType(DialogManager.WARNING)
                        .setContent(GetText.tr(
                                "Setting PermGen size above {0}MB is not recommended and can cause issues. Are you sure you want to do this?",
                                viewModel.getPermGenMaxRecommendSize()))
                        .show();

                if (ret != 0) {
                    permGen.setValue(viewModel.getPermGenMaxRecommendSize());
                }
            }
        });
        addDisposable(viewModel.getMetaspace().subscribe(permGen::setValue));
        add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
        JLabelWithHover windowSizeLabel = new JLabelWithHover(GetText.tr("Window Size") + ":", HELP_ICON,
                GetText.tr("The size that the Minecraft window should open as, Width x Height, in pixels."));
        add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new BoxLayout(windowSizePanel, BoxLayout.X_AXIS));

        SpinnerNumberModel widthModel = new SpinnerNumberModel(App.settings.windowWidth, 1, OS.getMaximumWindowWidth(),
                1);
        JSpinner widthField = new JSpinner(widthModel);
        widthField.setEditor(new JSpinner.NumberEditor(widthField, "#"));
        widthField.addChangeListener(e -> viewModel.setWidth((Integer) widthModel.getValue()));
        addDisposable(viewModel.getWidth().subscribe(widthModel::setValue));

        SpinnerNumberModel heightModel = new SpinnerNumberModel(App.settings.windowHeight, 1,
                OS.getMaximumWindowHeight(), 1);
        JSpinner heightField = new JSpinner(heightModel);
        heightField.setEditor(new JSpinner.NumberEditor(heightField, "#"));
        heightField.addChangeListener(e -> viewModel.setHeight((Integer) heightField.getValue()));
        addDisposable(viewModel.getHeight().subscribe(heightField::setValue));

        JComboBox<ComboItem<ScreenResolution>> commonScreenSizes = new JComboBox<>();
        commonScreenSizes.addItem(new ComboItem<>(null, "Select An Option"));

        for (ScreenResolution resolution : viewModel.getScreenResolutions()) {
            commonScreenSizes.addItem(new ComboItem<>(resolution, resolution.toString()));
        }
        commonScreenSizes.addActionListener(e -> {
            Object selectedItem = commonScreenSizes.getSelectedItem();
            if (selectedItem == null)
                return;

            @SuppressWarnings("unchecked")
            ComboItem<ScreenResolution> selected = (ComboItem<ScreenResolution>) selectedItem;

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
        JLabelWithHover javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to specify where your Java Path is. Where possible the launcher will use a version of Java provided by Minecraft to launch the instance, but in cases where one isn't available, this path will be used."))
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

        JComboBox<ComboItem<String>> installedJavasComboBox = new JComboBox<>();
        installedJavasComboBox.setPreferredSize(new Dimension(516, 24));

        installedJavasComboBox.addItem(new ComboItem<String>(null, GetText.tr("Select Java Path To Autofill")));

        for (String javaInfo : viewModel.getJavaPaths()) {
            installedJavasComboBox.addItem(new ComboItem<>(javaInfo, javaInfo));
        }

        if (installedJavasComboBox.getItemCount() != 1) {
            installedJavasComboBox.addActionListener(e -> {
                ComboItem<String> path = ((ComboItem<String>) installedJavasComboBox.getSelectedItem());
                String value = path.getValue();
                if (value != null)
                    viewModel.setJavaPath(value);
            });
            javaPathPanelTop.add(installedJavasComboBox);
        }

        javaPath = new JTextField(32);
        javaPathChecker = new JLabelWithHover("", null, null);
        javaPath.addKeyListener(new DelayedSavingKeyListener(
                500,
                () -> viewModel.setJavaPath(javaPath.getText()),
                viewModel::setJavaPathPending));

        addDisposable(viewModel.getJavaPathObservable().subscribe(path -> {
            if (!javaPath.getText().equals(path))
                javaPath.setText(path);
        }));
        javaPath.setText(App.settings.javaPath);
        addDisposable(viewModel.getJavaPathChecker().subscribe(this::setJavaPathCheckState));

        JButton javaPathResetButton = new JButton(GetText.tr("Reset"));
        javaPathResetButton.addActionListener(e -> {
            viewModel.resetJavaPath();
            resetJavaPathCheckLabel();
        });
        JButton javaBrowseButton = new JButton(GetText.tr("Browse"));
        javaBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(javaPath.getText()));
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
                    viewModel.setJavaPath(selectedPath.getParent());
                } else {
                    viewModel.setJavaPath(selectedPath.getAbsolutePath());
                }
                viewModel.setJavaPathPending();
            }
        });

        javaPathPanelBottom.add(javaPath);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));
        javaPathPanelBottom.add(javaPathChecker, gbc);
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
        JLabelWithHover javaParametersLabel = new JLabelWithHover(GetText.tr("Java Parameters") + ":", HELP_ICON,
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
                viewModel::setJavaParamsPending));
        addDisposable(viewModel.getJavaParams().subscribe(params -> {
            if (!javaParameters.getText().equals(params))
                javaParameters.setText(params);
        }));
        addDisposable(viewModel.getJavaParamsChecker().subscribe(this::setJavaParamCheckState));

        JButton javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e -> viewModel.resetJavaParams());

        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(Box.createHorizontalStrut(5));

        Box paramsResetBox = Box.createVerticalBox();
        paramsResetBox.add(javaParametersResetButton);
        paramsResetBox.add(Box.createVerticalGlue());
        paramsResetBox.add(javaParamChecker, gbc);
        paramsResetBox.add(Box.createVerticalGlue());
        javaParametersPanel.add(paramsResetBox);

        add(javaParametersPanel, gbc);

        // Base Java Install Folder

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover baseJavaInstallFolderLabel = new JLabelWithHover(GetText.tr("Java Install Location") + ":",
                HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to specify a common location that you install all your Java installs to. This helps find your installed Java installs easier if you install them all within 1 folder."))
                        .build());
        add(baseJavaInstallFolderLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel baseJavaInstallFolderPanel = new JPanel();
        baseJavaInstallFolderPanel.setLayout(new BoxLayout(baseJavaInstallFolderPanel, BoxLayout.X_AXIS));

        JTextField baseJavaInstallFolder = new JTextField(32);
        baseJavaInstallFolder.setText(App.settings.baseJavaInstallFolder);
        baseJavaInstallFolder.addKeyListener(new StatefulTextKeyAdapter(
                (e) -> viewModel.setBaseInstallFolder(baseJavaInstallFolder.getText())));
        addDisposable(viewModel.getBaseInstallFolder().subscribe(folder -> {
            if (!baseJavaInstallFolder.getText().equals(folder))
                baseJavaInstallFolder.setText(folder);
        }));

        JButton baseJavaInstallFolderResetButton = new JButton(GetText.tr("Reset"));
        baseJavaInstallFolderResetButton.addActionListener(e -> viewModel.resetBaseInstallFolder());

        JButton baseJavaInstallFolderBrowseButton = new JButton(GetText.tr("Browse"));
        baseJavaInstallFolderBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(baseJavaInstallFolder.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                viewModel.setBaseInstallFolder(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        baseJavaInstallFolderPanel.add(baseJavaInstallFolder);
        baseJavaInstallFolderPanel.add(Box.createHorizontalStrut(5));
        baseJavaInstallFolderPanel.add(baseJavaInstallFolderResetButton);
        baseJavaInstallFolderPanel.add(Box.createHorizontalStrut(5));
        baseJavaInstallFolderPanel.add(baseJavaInstallFolderBrowseButton);

        add(baseJavaInstallFolderPanel, gbc);

        // Start Minecraft Maximised

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover startMinecraftMaximisedLabel = new JLabelWithHover(
                GetText.tr("Start Minecraft Maximised") + "?", HELP_ICON,
                GetText.tr(
                        "Enabling this will start Minecraft maximised so that it takes up the full size of your screen."));
        add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox startMinecraftMaximised = new JCheckBox();
        startMinecraftMaximised.addItemListener(
                itemEvent -> viewModel.setStartMinecraftMax(itemEvent.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getMaximizeMinecraft().subscribe(startMinecraftMaximised::setSelected));
        add(startMinecraftMaximised, gbc);

        // Ignore Java checks On Launch

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover ignoreJavaOnInstanceLaunchLabel = new JLabelWithHover(
                GetText.tr("Ignore Java Checks On Launch") + "?",
                HELP_ICON, GetText.tr(
                        "This enables ignoring errors when launching a pack that you don't have a compatible Java version for."));
        add(ignoreJavaOnInstanceLaunchLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox ignoreJavaOnInstanceLaunch = new JCheckBox();
        ignoreJavaOnInstanceLaunch.addItemListener(
                itemEvent -> viewModel.setIgnoreJavaChecks(itemEvent.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getIgnoreJavaOnInstanceLaunch().subscribe(ignoreJavaOnInstanceLaunch::setSelected));
        add(ignoreJavaOnInstanceLaunch, gbc);

        // Use Java Provided By Minecraft

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useJavaProvidedByMinecraftLabel = new JLabelWithHover(
                GetText.tr("Use Java Provided By Minecraft") + "?",
                HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing."))
                        .build());
        add(useJavaProvidedByMinecraftLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox useJavaProvidedByMinecraft = new JCheckBox();
        useJavaProvidedByMinecraft.setSelected(viewModel.getUseJavaFromMinecraftEnabled());
        useJavaProvidedByMinecraft.addItemListener(e -> {
            boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
            viewModel.setJavaFromMinecraft(enabled);

            if (!enabled) {
                SwingUtilities.invokeLater(() -> {
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                            .setType(DialogManager.WARNING)
                            .setContent(GetText.tr(
                                    "Unchecking this is not recommended and may cause Minecraft to no longer run. Are you sure you want to do this?"))
                            .show();

                    if (ret != 0) {
                        useJavaProvidedByMinecraft.setSelected(true);
                    }
                });
            }
        });
        addDisposable(viewModel.getUseJavaProvidedByMinecraft().subscribe(useJavaProvidedByMinecraft::setEnabled));
        add(useJavaProvidedByMinecraft, gbc);

        // Disable Legacy Launching

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover disableLegacyLaunchingLabel = new JLabelWithHover(GetText.tr("Disable Legacy Launching") + "?",
                HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This allows you to disable legacy launching for Minecraft < 1.6.<br/><br/>It's highly recommended to not disable this, unless you're having issues launching older Minecraft versions."))
                        .build());
        add(disableLegacyLaunchingLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox disableLegacyLaunching = new JCheckBox();
        disableLegacyLaunching.addItemListener(
                itemEvent -> viewModel.setDisableLegacyLaunching(itemEvent.getStateChange() == ItemEvent.SELECTED));
        addDisposable(
                viewModel.getDisableLegacyLaunching().subscribe(disableLegacyLaunching::setSelected));
        add(disableLegacyLaunching, gbc);

        // Use System GLFW

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useSystemGlfwLabel = new JLabelWithHover(GetText.tr("Use System GLFW") + "?", HELP_ICON,
                new HTMLBuilder()
                        .center().text(GetText.tr("Use the systems install for GLFW native library.")).build());
        add(useSystemGlfwLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox useSystemGlfw = new JCheckBox();
        useSystemGlfw.addItemListener(
                itemEvent -> viewModel.setSystemGLFW(itemEvent.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getSystemGLFW().subscribe(useSystemGlfw::setSelected));
        add(useSystemGlfw, gbc);

        // Use System OpenAL

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useSystemOpenAlLabel = new JLabelWithHover(GetText.tr("Use System OpenAL") + "?", HELP_ICON,
                new HTMLBuilder()
                        .center().text(GetText.tr("Use the systems install for OpenAL native library.")).build());
        add(useSystemOpenAlLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JCheckBox useSystemOpenAl = new JCheckBox();
        useSystemOpenAl.addItemListener(
                itemEvent -> viewModel.setSystemOpenAL(itemEvent.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getSystemOpenAL().subscribe(useSystemOpenAl::setSelected));
        add(useSystemOpenAl, gbc);
    }

    private void showJavaPathWarning() {
        DialogManager.okDialog()
                .setTitle(GetText.tr("Help"))
                .setContent(
                        new HTMLBuilder()
                                .center()
                                .text(
                                        GetText.tr(
                                                "The Java Path you set is incorrect.<br/><br/>Please verify it points to the folder where the bin folder is and try again."))
                                .build())
                .setType(DialogManager.ERROR)
                .show();
    }

    private void showJavaParamWarning() {
        DialogManager.okDialog()
                .setTitle(GetText.tr("Help"))
                .setContent(
                        new HTMLBuilder()
                                .center()
                                .text(
                                        GetText.tr(
                                                "The entered Java Parameters were incorrect.<br/><br/>Please remove any references to Xmx, Xms or XX:PermSize."))
                                .build())
                .setType(DialogManager.ERROR)
                .show();
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
    protected void createViewModel() {
    }

    private void setLabelState(JLabelWithHover label, String tooltip, String path) {
        try {
            label.setToolTipText(tooltip);
            ImageIcon icon = Utils.getIconImage(path);
            if (icon != null) {
                label.setIcon(icon);
                icon.setImageObserver(label);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void resetJavaPathCheckLabel() {
        setLabelState(javaPathChecker, "Visualize java path checker", "/assets/icon/question.png");
    }

    private void setJavaPathCheckState(CheckState state) {
        if (state == CheckState.NotChecking) {
            resetJavaPathCheckLabel();
        } else if (state == CheckState.CheckPending) {
            setLabelState(javaPathChecker, "Java path change pending", "/assets/icon/warning.png");
        } else if (state == CheckState.Checking) {
            setLabelState(javaPathChecker, "Checking java path", "/assets/image/loading-bars-small.gif");

            javaPath.setEnabled(false);
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetJavaPathCheckLabel();
            } else {
                setLabelState(javaPathChecker, "Invalid!", "/assets/icon/error.png");
                showJavaPathWarning();
            }
            javaPath.setEnabled(true);
        }
    }

    private void resetJavaParamCheckLabel() {
        setLabelState(javaParamChecker, "Visualize java param checker", "/assets/icon/question.png");

    }

    private void setJavaParamCheckState(CheckState state) {
        if (state == CheckState.NotChecking) {
            resetJavaParamCheckLabel();
        } else if (state == CheckState.CheckPending) {
            setLabelState(javaParamChecker, "Java params change pending", "/assets/icon/warning.png");
        } else if (state == CheckState.Checking) {
            setLabelState(javaParamChecker, "Checking java params", "/assets/image/loading-bars-small.gif");

            javaParameters.setEnabled(false);
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetJavaParamCheckLabel();
            } else {
                setLabelState(javaParamChecker, "Invalid!", "/assets/icon/error.png");
                showJavaParamWarning();
            }
            javaParameters.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        removeAll();
        javaPath = null;
        javaPathChecker = null;
        javaParamChecker = null;
        javaParameters = null;
    }
}
