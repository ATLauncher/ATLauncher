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
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mini2Dx.gettext.GetText;

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
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.javafinder.JavaInfo;

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
    private final JComboBox<ComboItem<JavaInfo>> installedJavasComboBox;
    private final JButton javaPathResetButton;
    private final JButton javaBrowseButton;
    private final JLabelWithHover javaParametersLabel;
    private final JTextArea javaParameters;
    private final JButton javaParametersResetButton;
    private final JLabelWithHover startMinecraftMaximisedLabel;
    private final JCheckBox startMinecraftMaximised;
    private final JLabelWithHover ignoreJavaOnInstanceLaunchLabel;
    private final JCheckBox ignoreJavaOnInstanceLaunch;
    private final JLabelWithHover useJavaProvidedByMinecraftLabel;
    private final JCheckBox useJavaProvidedByMinecraft;
    private final JTextField baseJavaInstallFolder;
    private final JLabelWithHover disableLegacyLaunchingLabel;
    private final JCheckBox disableLegacyLaunching;
    private final JLabelWithHover useSystemGlfwLabel;
    private final JCheckBox useSystemGlfw;
    private final JLabelWithHover useSystemOpenAlLabel;
    private final JCheckBox useSystemOpenAl;

    private boolean initialMemoryWarningShown = false;
    private boolean maximumMemoryHalfWarningShown = false;
    private boolean maximumMemoryEightGBWarningShown = false;
    private boolean permgenWarningShown = false;

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
        if (!Java.is64Bit()) {
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
        initialMemory.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                // if initial memory is larger than maximum memory, make maximum memory match
                if ((Integer) s.getValue() > (Integer) maximumMemory.getValue()) {
                    maximumMemory.setValue((Integer) s.getValue());
                }

                if ((Integer) s.getValue() > 512 && !initialMemoryWarningShown) {
                    initialMemoryWarningShown = true;
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                            .setType(DialogManager.WARNING)
                            .setContent(GetText.tr(
                                    "Setting initial memory above 512MB is not recommended and can cause issues. Are you sure you want to do this?"))
                            .show();

                    if (ret != 0) {
                        initialMemory.setValue(512);
                    }
                }
            }
        });
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
        if (!Java.is64Bit()) {
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
        maximumMemory.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                // if initial memory is larger than maximum memory, make initial memory match
                if ((Integer) initialMemory.getValue() > (Integer) s.getValue()) {
                    initialMemory.setValue(s.getValue());
                }

                if ((Integer) s.getValue() > 8192 && !maximumMemoryEightGBWarningShown) {
                    maximumMemoryEightGBWarningShown = true;
                    int ret = DialogManager.okDialog().setTitle(GetText.tr("Warning"))
                            .setType(DialogManager.WARNING)
                            .setContent(GetText.tr(
                                    "Setting maximum memory above 8GB is not recommended for most modpacks and can cause issues."))
                            .addOption(GetText.tr("More Explanation"))
                            .show();

                    if (ret == 1) {
                        OS.openWebBrowser("https://atl.pw/allocatetoomuchram");
                    }
                } else if ((OS.getMaximumRam() != 0 && OS.getMaximumRam() < 16384)
                        && (Integer) s.getValue() > (OS.getMaximumRam() / 2)
                        && !maximumMemoryHalfWarningShown) {
                    maximumMemoryHalfWarningShown = true;
                    DialogManager.okDialog().setTitle(GetText.tr("Warning"))
                            .setType(DialogManager.WARNING)
                            .setContent(GetText.tr(
                                    "Setting maximum memory to more than half of your systems total memory is not recommended and can cause issues in some cases. Are you sure you want to do this?"))
                            .show();
                }
            }
        });
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
        permGen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                int permGenMaxRecommendedSize = (OS.is64Bit() ? 256 : 128);

                if ((Integer) s.getValue() > permGenMaxRecommendedSize && !permgenWarningShown) {
                    permgenWarningShown = true;
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                            .setType(DialogManager.WARNING)
                            .setContent(GetText.tr(
                                    "Setting PermGen size above {0}MB is not recommended and can cause issues. Are you sure you want to do this?",
                                    permGenMaxRecommendedSize))
                            .show();

                    if (ret != 0) {
                        permGen.setValue(permGenMaxRecommendedSize);
                    }
                }
            }
        });
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
        JLabelWithHover javaMinecraftProvidedLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This version of Minecraft provides a specific version of Java to be used with it, so you cannot set a custom Java path.<br/><br/>In order to manually set a path, you must disable this option (highly not recommended)."))
                        .build());
        add(javaMinecraftProvidedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JLabel javaPathDummy = new JLabel("Uses Java provided by Minecraft");
        javaPathDummy.setEnabled(false);
        add(javaPathDummy, gbc);

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
        List<JavaInfo> installedJavas = Java.getInstalledJavas();

        installedJavasComboBox.addItem(new ComboItem<JavaInfo>(null, GetText.tr("Select Java Path To Autofill")));

        for (JavaInfo javaInfo : installedJavas) {
            installedJavasComboBox.addItem(new ComboItem<JavaInfo>(javaInfo, javaInfo.toString()));
        }

        if (installedJavasComboBox.getItemCount() != 1) {
            installedJavasComboBox.addActionListener(e -> {
                JavaInfo selectedItem = ((ComboItem<JavaInfo>) installedJavasComboBox.getSelectedItem())
                        .getValue();

                if (selectedItem != null) {
                    javaPath.setText(selectedItem.rootPath);
                }
                installedJavasComboBox.setSelectedIndex(0);
            });
            javaPathPanelTop.add(installedJavasComboBox);
        }

        javaPath = new JTextField(32);
        javaPath.setText(App.settings.javaPath);
        javaPathResetButton = new JButton(GetText.tr("Reset"));
        javaPathResetButton.addActionListener(e -> {
            javaPath.setText(OS.getDefaultJavaPath());
            installedJavasComboBox.setSelectedIndex(0);
        });
        javaBrowseButton = new JButton(GetText.tr("Browse"));
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
                    javaPath.setText(selectedPath.getParent().toString());
                } else {
                    javaPath.setText(selectedPath.getAbsolutePath());
                }
                installedJavasComboBox.setSelectedIndex(0);
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

        boolean isUsingMinecraftProvidedJava = App.settings.useJavaProvidedByMinecraft;
        javaMinecraftProvidedLabel.setVisible(isUsingMinecraftProvidedJava);
        javaPathDummy.setVisible(isUsingMinecraftProvidedJava);
        javaPathLabel.setVisible(!isUsingMinecraftProvidedJava);
        javaPathPanel.setVisible(!isUsingMinecraftProvidedJava);

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

        baseJavaInstallFolder = new JTextField(32);
        baseJavaInstallFolder.setText(App.settings.baseJavaInstallFolder);
        JButton baseJavaInstallFolderResetButton = new JButton(GetText.tr("Reset"));
        baseJavaInstallFolderResetButton.addActionListener(e -> {
            baseJavaInstallFolder.setText("");
        });
        JButton baseJavaInstallFolderBrowseButton = new JButton(GetText.tr("Browse"));
        baseJavaInstallFolderBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(baseJavaInstallFolder.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                baseJavaInstallFolder.setText(chooser.getSelectedFile().getAbsolutePath());
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
                        "This enables ignoring errors when launching a pack that you don't have a compatible Java version for."));
        add(ignoreJavaOnInstanceLaunchLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        ignoreJavaOnInstanceLaunch = new JCheckBox();
        if (App.settings.ignoreJavaOnInstanceLaunch) {
            ignoreJavaOnInstanceLaunch.setSelected(true);
        }
        add(ignoreJavaOnInstanceLaunch, gbc);

        // Use Java Provided By Minecraft

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        useJavaProvidedByMinecraftLabel = new JLabelWithHover(GetText.tr("Use Java Provided By Minecraft") + "?",
                HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing."))
                        .build());
        add(useJavaProvidedByMinecraftLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        useJavaProvidedByMinecraft = new JCheckBox();
        useJavaProvidedByMinecraft.setSelected(App.settings.useJavaProvidedByMinecraft);
        useJavaProvidedByMinecraft.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (!useJavaProvidedByMinecraft.isSelected()) {
                        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                                .setType(DialogManager.WARNING)
                                .setContent(GetText.tr(
                                        "Unchecking this is not recommended and may cause Minecraft to no longer run. Are you sure you want to do this?"))
                                .show();

                        if (ret != 0) {
                            useJavaProvidedByMinecraft.setSelected(true);
                        }
                    }

                    javaMinecraftProvidedLabel.setVisible(useJavaProvidedByMinecraft.isSelected());
                    javaPathDummy.setVisible(useJavaProvidedByMinecraft.isSelected());

                    javaPathLabel.setVisible(!useJavaProvidedByMinecraft.isSelected());
                    javaPathPanel.setVisible(!useJavaProvidedByMinecraft.isSelected());
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
        disableLegacyLaunching.setSelected(App.settings.disableLegacyLaunching);
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
        useSystemGlfw.setSelected(App.settings.useSystemGlfw);
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
        useSystemOpenAl.setSelected(App.settings.useSystemOpenAl);
        add(useSystemOpenAl, gbc);
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
        App.settings.usingCustomJavaPath = !javaPath.getText().equalsIgnoreCase(OS.getDefaultJavaPath());
        App.settings.javaParameters = javaParameters.getText();
        App.settings.maximiseMinecraft = startMinecraftMaximised.isSelected();
        App.settings.ignoreJavaOnInstanceLaunch = ignoreJavaOnInstanceLaunch.isSelected();
        App.settings.useJavaProvidedByMinecraft = useJavaProvidedByMinecraft.isSelected();
        App.settings.baseJavaInstallFolder = baseJavaInstallFolder.getText().isEmpty() ? null
                : baseJavaInstallFolder.getText();
        App.settings.disableLegacyLaunching = disableLegacyLaunching.isSelected();
        App.settings.useSystemGlfw = useSystemGlfw.isSelected();
        App.settings.useSystemOpenAl = useSystemOpenAl.isSelected();
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
                "This enables ignoring errors when launching a pack that you don't have a compatible Java version for."));

        this.useJavaProvidedByMinecraftLabel.setText(GetText.tr("Use Java Provided By Minecraft") + "?");
        this.useJavaProvidedByMinecraftLabel.setToolTipText(new HTMLBuilder().center().text(GetText.tr(
                "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing."))
                .build());

        this.disableLegacyLaunchingLabel.setText(GetText.tr("Disable Legacy Launching") + "?");
        this.disableLegacyLaunchingLabel.setToolTipText(new HTMLBuilder().center().text(GetText.tr(
                "This allows you to disable legacy launching for Minecraft < 1.6.<br/><br/>It's highly recommended to not disable this, unless you're having issues launching older Minecraft versions."))
                .build());
    }

    @Override
    public void onSettingsSaved() {
        javaPath.setText(App.settings.javaPath);
    }
}
