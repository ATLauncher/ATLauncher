/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.javafinder.JavaInfo;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab implements RelocalizationListener, SettingsListener {
    private JLabelWithHover initialMemoryLabel;
    private JSpinner initialMemory;
    private JLabelWithHover initialMemoryLabelWarning;

    private JPanel initialMemoryPanel;
    private JLabelWithHover maximumMemoryLabel;
    private JSpinner maximumMemory;
    private JLabelWithHover maximumMemoryLabelWarning;
    private JPanel maximumMemoryPanel;

    private JLabelWithHover permGenLabel;
    private JSpinner permGen;

    private JPanel windowSizePanel;
    private JLabelWithHover windowSizeLabel;
    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> commonScreenSizes;
    private JPanel javaPathPanel;
    private JLabelWithHover javaPathLabel;
    private JTextField javaPath;
    private JComboBox<JavaInfo> installedJavas;
    private JButton javaPathResetButton;
    private JButton javaBrowseButton;
    private JPanel javaParametersPanel;
    private JLabelWithHover javaParametersLabel;
    private JTextField javaParameters;
    private JButton javaParametersResetButton;
    private JLabelWithHover startMinecraftMaximisedLabel;
    private JCheckBox startMinecraftMaximised;
    private JLabelWithHover saveCustomModsLabel;
    private JCheckBox saveCustomMods;
    private JLabelWithHover ignoreJavaOnInstanceLaunchLabel;
    private JCheckBox ignoreJavaOnInstanceLaunch;

    public JavaSettingsTab() {
        int systemRam = OS.getSystemRam();

        RelocalizationManager.addListener(this);
        SettingsManager.addListener(this);

        // Initial Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON, "<html>" + Utils.splitMultilinedString(GetText.tr(
                "You are running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."),
                80, "<br/>") + "</html>", RESTART_BORDER);

        initialMemoryLabel = new JLabelWithHover(GetText.tr("Initial Memory/Ram") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(GetText.tr(
                        "Initial memory/ram is the starting amount of memory/ram to use when starting Minecraft. This should be left at the default of 512 MB unless you know what your doing."),
                        80, "<br/>") + "</html>");

        initialMemoryPanel = new JPanel();
        initialMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!OS.is64Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel initialMemoryModel = new SpinnerNumberModel(App.settings.getInitialMemory(), null, null,
                128);
        initialMemoryModel.setMinimum(128);
        initialMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        initialMemory = new JSpinner(initialMemoryModel);
        ((JSpinner.DefaultEditor) initialMemory.getEditor()).getTextField().setColumns(5);
        add(initialMemory, gbc);

        // Maximum Memory Settings
        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        maximumMemoryLabel = new JLabelWithHover(GetText.tr("Maximum Memory/Ram") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(
                        GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft."), 80,
                        "<br/>") + "</html>");
        add(maximumMemoryLabel, gbc);

        maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!OS.is64Bit()) {
            maximumMemoryPanel.add(new JLabelWithHover(WARNING_ICON, "<html>" + Utils.splitMultilinedString(GetText.tr(
                    "You are running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."),
                    80, "<br/>") + "</html>", RESTART_BORDER));
        }
        maximumMemoryPanel.add(maximumMemoryLabel);

        add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel maximumMemoryModel = new SpinnerNumberModel(App.settings.getMaximumMemory(), null, null,
                512);
        maximumMemoryModel.setMinimum(512);
        maximumMemoryModel.setMaximum((systemRam == 0 ? null : systemRam));
        maximumMemory = new JSpinner(maximumMemoryModel);
        ((JSpinner.DefaultEditor) maximumMemory.getEditor()).getTextField().setColumns(5);
        add(maximumMemory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        permGenLabel = new JLabelWithHover(GetText.tr("PermGen Size") + ":", HELP_ICON,
                GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));
        add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel permGenModel = new SpinnerNumberModel(App.settings.getPermGen(), null, null, 32);
        permGenModel.setMinimum(32);
        permGenModel.setMaximum((systemRam == 0 ? null : systemRam));
        permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        windowSizeLabel = new JLabelWithHover(GetText.tr("Window Size") + ":", HELP_ICON,
                GetText.tr("The size that the Minecraft window should open as, Width x Height, in pixels."));
        add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        widthField = new JTextField(4);
        widthField.setText(App.settings.getWindowWidth() + "");
        heightField = new JTextField(4);
        heightField.setText(App.settings.getWindowHeight() + "");
        commonScreenSizes = new JComboBox<>();
        commonScreenSizes.addItem("Select An Option");
        commonScreenSizes.addItem("854x480");
        if (OS.getMaximumWindowWidth() >= 1280 && OS.getMaximumWindowHeight() >= 720) {
            commonScreenSizes.addItem("1280x720");
        }
        if (OS.getMaximumWindowWidth() >= 1600 && OS.getMaximumWindowHeight() >= 900) {
            commonScreenSizes.addItem("1600x900");
        }
        if (OS.getMaximumWindowWidth() >= 1920 && OS.getMaximumWindowHeight() >= 1080) {
            commonScreenSizes.addItem("1920x1080");
        }
        commonScreenSizes.addActionListener(e -> {
            String selected = (String) commonScreenSizes.getSelectedItem();
            if (selected.contains("x")) {
                String[] parts = selected.split("x");
                widthField.setText(parts[0]);
                heightField.setText(parts[1]);
            }
        });
        commonScreenSizes.setPreferredSize(new Dimension(commonScreenSizes.getPreferredSize().width + 10,
                commonScreenSizes.getPreferredSize().height));
        windowSizePanel.add(widthField);
        windowSizePanel.add(new JLabel("x"));
        windowSizePanel.add(heightField);
        windowSizePanel.add(commonScreenSizes);
        add(windowSizePanel, gbc);

        // Java Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON, "<html>" + GetText.tr(
                "This setting allows you to specify where your Java Path is.<br/><br/>This should be left as default, but if you know what your doing just set<br/><br/>this to the path where the bin folder is for the version of Java you want to use<br/><br/>If you mess up, click the Reset button to go back to the default")
                + "</html>");
        add(javaPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        javaPathPanel = new JPanel(new BorderLayout());

        JPanel javaPathPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel javaPathPanelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        installedJavas = new JComboBox<>();
        addInstalledJavas();
        if (installedJavas.getItemCount() != 0) {
            javaPathPanelTop.add(installedJavas);
        }

        javaPath = new JTextField(32);
        javaPath.setText(App.settings.getJavaPath());
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
        javaPathPanelBottom.add(javaPathResetButton);
        javaPathPanelBottom.add(javaBrowseButton);
        javaPathPanel.add(javaPathPanelTop, BorderLayout.NORTH);
        javaPathPanel.add(javaPathPanelBottom, BorderLayout.CENTER);
        add(javaPathPanel, gbc);

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersLabel = new JLabelWithHover(GetText.tr("Java Parameters") + ":", HELP_ICON,
                GetText.tr("Extra Java command line paramaters can be added here."));
        add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        javaParameters = new JTextField(40);
        javaParameters.setText(App.settings.getJavaParameters());
        javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e -> javaParameters.setText(
                "-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M"));
        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(javaParametersResetButton);
        add(javaParametersPanel, gbc);

        // Start Minecraft Maximised

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        startMinecraftMaximisedLabel = new JLabelWithHover(GetText.tr("Start Minecraft Maximised") + "?", HELP_ICON,
                GetText.tr(
                        "Enabling this will start Minecraft maximised so that it takes up the full size of your screen."));
        add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        startMinecraftMaximised = new JCheckBox();
        if (App.settings.startMinecraftMaximised()) {
            startMinecraftMaximised.setSelected(true);
        }
        add(startMinecraftMaximised, gbc);

        // Save Custom Mods

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        saveCustomModsLabel = new JLabelWithHover(GetText.tr("Save Custom Mods") + "?", HELP_ICON, GetText
                .tr("This enables the saving of custom mods added to an instance when it's updated or reinstalled."));
        add(saveCustomModsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        saveCustomMods = new JCheckBox();
        if (App.settings.saveCustomMods()) {
            saveCustomMods.setSelected(true);
        }
        add(saveCustomMods, gbc);

        // Save Custom Mods

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        ignoreJavaOnInstanceLaunchLabel = new JLabelWithHover(GetText.tr("Ignore Java checks On Launch") + "?",
                HELP_ICON, GetText.tr(
                        "This enables ignoring errors when launching a pack that you don't have a compatable Java version for."));
        add(ignoreJavaOnInstanceLaunchLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        ignoreJavaOnInstanceLaunch = new JCheckBox();
        if (App.settings.ignoreJavaOnInstanceLaunch()) {
            ignoreJavaOnInstanceLaunch.setSelected(true);
        }
        add(ignoreJavaOnInstanceLaunch, gbc);
    }

    public boolean isValidJavaPath() {
        File jPath = new File(javaPath.getText(), "bin");
        if (!jPath.exists()) {
            DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent("<html>" + GetText.tr(
                    "The Java Path you set is incorrect.<br/><br/>Please verify it points to the folder where the bin folder is and try again.")
                    + "</html>").setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public boolean isValidJavaParamaters() {
        if (javaParameters.getText().contains("-Xms") || javaParameters.getText().contains("-Xmx")
                || javaParameters.getText().contains("-XX:PermSize")
                || javaParameters.getText().contains("-XX:MetaspaceSize")) {
            DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent("<html>" + GetText.tr(
                    "The entered Java Parameters were incorrect.<br/>Please remove any references to Xmx, Xms or XX:PermSize.")
                    + "</html>").setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public void save() {
        App.settings.setInitialMemory((Integer) initialMemory.getValue());
        App.settings.setMaximumMemory((Integer) maximumMemory.getValue());
        App.settings.setPermGen((Integer) permGen.getValue());
        App.settings.setWindowWidth(Integer.parseInt(widthField.getText().replaceAll("[^0-9]", "")));
        App.settings.setWindowHeight(Integer.parseInt(heightField.getText().replaceAll("[^0-9]", "")));
        App.settings.setJavaPath(javaPath.getText());
        App.settings.setJavaParameters(javaParameters.getText());
        App.settings.setStartMinecraftMaximised(startMinecraftMaximised.isSelected());
        App.settings.setSaveCustomMods(saveCustomMods.isSelected());
        App.settings.setIgnoreJavaOnInstanceLaunch(ignoreJavaOnInstanceLaunch.isSelected());
    }

    @Override
    public String getTitle() {
        return GetText.tr("Java/Minecraft");
    }

    @Override
    public void onRelocalization() {
        this.initialMemoryLabelWarning.setToolTipText("<html>" + Utils.splitMultilinedString(GetText.tr(
                "You are running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."),
                80, "<br/>") + "</html>");

        this.initialMemoryLabel.setText(GetText.tr("Initial Memory/Ram") + ":");
        this.initialMemoryLabel.setToolTipText("<html>" + Utils.splitMultilinedString(GetText.tr(
                "Initial memory/ram is the starting amount of memory/ram to use when starting Minecraft. This should be left at the default of 512 MB unless you know what your doing."),
                80, "<br/>") + "</html>");

        this.maximumMemoryLabelWarning.setToolTipText("<html>" + Utils.splitMultilinedString(GetText.tr(
                "You are running a 32 bit Java and therefore cannot use more than 1GB of Ram. Please see http://atl.pw/32bit for help."),
                80, "<br/>") + "</html>");

        this.maximumMemoryLabel.setText(GetText.tr("Maximum Memory/Ram") + ":");
        this.maximumMemoryLabel.setToolTipText("<html>" + Utils.splitMultilinedString(
                GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft."), 80, "<br/>")
                + "</html>");

        this.permGenLabel.setText(GetText.tr("PermGen Size") + ":");
        this.permGenLabel
                .setToolTipText(GetText.tr("The PermGen Size for java to use when launching Minecraft in MB."));

        this.windowSizeLabel.setText(GetText.tr("Window Size") + ":");
        this.windowSizeLabel.setToolTipText(
                GetText.tr("The size that the Minecraft window should open as, Width x Height, in pixels."));

        this.javaPathLabel.setText(GetText.tr("Java Path") + ":");
        this.javaPathLabel.setToolTipText("<html>" + GetText.tr(
                "This setting allows you to specify where your Java Path is.<br/><br/>This should be left as default, but if you know what your doing just set<br/><br/>this to the path where the bin folder is for the version of Java you want to use<br/><br/>If you mess up, click the Reset button to go back to the default")
                + "</html>");

        this.javaPathResetButton.setText(GetText.tr("Reset"));

        this.javaBrowseButton.setText(GetText.tr("Browse"));

        this.javaParametersLabel.setText(GetText.tr("Java Parameters") + ":");
        this.javaParametersLabel.setToolTipText(GetText.tr("Extra Java command line paramaters can be added here."));

        this.javaParametersResetButton.setText(GetText.tr("Reset"));

        this.startMinecraftMaximisedLabel.setText(GetText.tr("Start Minecraft Maximised") + "?");
        this.startMinecraftMaximisedLabel.setToolTipText(GetText
                .tr("Enabling this will start Minecraft maximised so that it takes up the full size of your screen."));

        this.saveCustomModsLabel.setText(GetText.tr("Save Custom Mods") + "?");
        this.saveCustomModsLabel.setToolTipText(GetText
                .tr("This enables the saving of custom mods added to an instance when it's updated or reinstalled."));

        this.ignoreJavaOnInstanceLaunchLabel.setText(GetText.tr("Ignore Java checks On Launch") + "?");
        this.ignoreJavaOnInstanceLaunchLabel.setToolTipText(GetText.tr(
                "This enables ignoring errors when launching a pack that you don't have a compatable Java version for."));
    }

    public void addInstalledJavas() {
        installedJavas.removeAll();

        List<JavaInfo> systemJavas = Java.getInstalledJavas();

        if (systemJavas.size() != 0) {
            systemJavas.stream().forEach(installedJavas::addItem);

            installedJavas.setSelectedItem(systemJavas.stream()
                    .filter(javaInfo -> javaInfo.rootPath.equalsIgnoreCase(App.settings.getJavaPath())).findFirst()
                    .orElse(null));

            installedJavas
                    .addActionListener(e -> javaPath.setText(((JavaInfo) installedJavas.getSelectedItem()).rootPath));
        }
    }

    @Override
    public void onSettingsSaved() {
        javaPath.setText(App.settings.getJavaPath());
    }
}
