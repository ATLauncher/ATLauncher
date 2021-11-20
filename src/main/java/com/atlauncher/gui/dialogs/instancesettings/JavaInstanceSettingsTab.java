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
package com.atlauncher.gui.dialogs.instancesettings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.javafinder.JavaInfo;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class JavaInstanceSettingsTab extends JPanel {
    private final Instance instance;

    private JSpinner initialMemory;
    private JSpinner maximumMemory;
    private JSpinner permGen;
    private JTextField javaPath;
    private JTextArea javaParameters;
    private JComboBox<ComboItem<Boolean>> useJavaProvidedByMinecraft;
    private JComboBox<ComboItem<Boolean>> disableLegacyLaunching;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
    final ImageIcon ERROR_ICON = Utils.getIconImage(App.THEME.getIconPath("error"));
    final ImageIcon WARNING_ICON = Utils.getIconImage(App.THEME.getIconPath("warning"));

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);

    final GridBagConstraints gbc = new GridBagConstraints();

    public JavaInstanceSettingsTab(Instance instance) {
        this.instance = instance;

        setupComponents();
    }

    private void setupComponents() {
        int systemRam = OS.getSystemRam();
        setLayout(new GridBagLayout());

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
        if (!Java.is64Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel initialMemoryModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.initialMemory, App.settings.initialMemory), null, null, 128);
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
            }
        });
        add(initialMemory, gbc);

        // Maximum Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover maximumMemoryLabel = new JLabelWithHover(GetText.tr("Maximum Memory/Ram") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(
                        GetText.tr("The maximum amount of memory/ram to allocate when starting Minecraft."), 80,
                        "<br/>") + "</html>");
        add(maximumMemoryLabel, gbc);

        JPanel maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        maximumMemoryPanel.add(maximumMemoryLabel);

        add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        SpinnerNumberModel maximumMemoryModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.maximumMemory, App.settings.maximumMemory), null, null, 512);
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
            }
        });
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
        SpinnerNumberModel permGenModel = new SpinnerNumberModel(
                getIfNotNull(this.instance.launcher.permGen, App.settings.metaspace), null, null, 32);
        permGenModel.setMinimum(32);
        permGenModel.setMaximum((systemRam == 0 ? null : systemRam));
        permGen = new JSpinner(permGenModel);
        ((JSpinner.DefaultEditor) permGen.getEditor()).getTextField().setColumns(3);
        add(permGen, gbc);

        // Java Path
        javaPath = new JTextField(32);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover javaMinecraftProvidedLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This version of Minecraft provides a specific version of Java to be used with it, so you cannot set a custom Java path.<br/><br/>In order to manually set a path, you must disable this option (highly not recommended) in the Java settings of the launcher."))
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
        JLabelWithHover javaPathLabel = new JLabelWithHover(GetText.tr("Java Path") + ":", HELP_ICON, "<html>" + GetText
                .tr("This setting allows you to specify where your Java Path is.<br/><br/>This should be left as default, but if you know what you're doing, just set<br/>this to the path where the bin folder is for the version of Java you want to use.<br/><br/>If you mess up, click the Reset button to go back to the default")
                + "</html>");
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
            }
        });

        JComboBox<ComboItem<JavaInfo>> installedJavasComboBox = new JComboBox<>();
        installedJavasComboBox.setPreferredSize(new Dimension(516, 24));
        installedJavasComboBox.addItem(new ComboItem<JavaInfo>(null, GetText.tr("Use Launcher Default")));
        List<JavaInfo> installedJavas = Java.getInstalledJavas();
        int selectedIndex = 0;

        for (JavaInfo javaInfo : installedJavas) {
            installedJavasComboBox.addItem(new ComboItem<JavaInfo>(javaInfo, javaInfo.toString()));

            if (javaInfo.rootPath
                    .equalsIgnoreCase(getIfNotNull(this.instance.launcher.javaPath, App.settings.javaPath))) {
                selectedIndex = installedJavasComboBox.getItemCount() - 2;
            }
        }

        if (installedJavasComboBox.getItemCount() != 1) {
            installedJavasComboBox.setSelectedIndex(selectedIndex);
            installedJavasComboBox.addActionListener(e -> {
                JavaInfo selectedItem = ((ComboItem<JavaInfo>) installedJavasComboBox.getSelectedItem()).getValue();

                if (selectedItem == null) {
                    javaPath.setText("");
                } else {
                    javaPath.setText(selectedItem.rootPath);
                }
            });

            javaPathPanelTop.add(installedJavasComboBox);
        }

        javaPathPanelBottom.add(javaPath);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));
        javaPathPanelBottom.add(javaPathResetButton);
        javaPathPanelBottom.add(Box.createHorizontalStrut(5));
        javaPathPanelBottom.add(javaBrowseButton);

        javaPathPanel.add(javaPathPanelTop);
        javaPathPanel.add(Box.createVerticalStrut(5));
        javaPathPanel.add(javaPathPanelBottom);

        add(javaPathPanel, gbc);

        boolean isUsingMinecraftProvidedJava = Optional.ofNullable(instance.launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft);
        javaMinecraftProvidedLabel.setVisible(isUsingMinecraftProvidedJava);
        javaPathDummy.setVisible(isUsingMinecraftProvidedJava);

        javaPathLabel.setVisible(!isUsingMinecraftProvidedJava);
        javaPathPanel.setVisible(!isUsingMinecraftProvidedJava);

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

        javaParameters = new JTextArea(6, 40);
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

        add(javaParametersPanel, gbc);

        // Use Java Provided By Minecraft
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useJavaProvidedByMinecraftLabel = new JLabelWithHover(
                GetText.tr("Use Java Provided By Minecraft") + "?", HELP_ICON,
                new HTMLBuilder().center().text(GetText.tr(
                        "This allows you to enable/disable using the version of Java provided by the version of Minecraft you're running.<br/><br/>It's highly recommended to not disable this, unless you know what you're doing."))
                        .build());
        add(useJavaProvidedByMinecraftLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        useJavaProvidedByMinecraft = new JComboBox<>();
        useJavaProvidedByMinecraft.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        useJavaProvidedByMinecraft.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        useJavaProvidedByMinecraft.addItem(new ComboItem<>(false, GetText.tr("No")));

        if (instance.launcher.useJavaProvidedByMinecraft == null) {
            useJavaProvidedByMinecraft.setSelectedIndex(0);
        } else if (instance.launcher.useJavaProvidedByMinecraft) {
            useJavaProvidedByMinecraft.setSelectedIndex(1);
        } else {
            useJavaProvidedByMinecraft.setSelectedIndex(2);
        }

        useJavaProvidedByMinecraft.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(() -> {
                        if (useJavaProvidedByMinecraft.getSelectedIndex() == 2) {
                            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Warning"))
                                    .setType(DialogManager.WARNING)
                                    .setContent(GetText.tr(
                                            "Unchecking this is not recommended and may cause Minecraft to no longer run. Are you sure you want to do this?"))
                                    .show();

                            if (ret != 0) {
                                useJavaProvidedByMinecraft.setSelectedIndex(0);
                            } else {
                                javaMinecraftProvidedLabel.setVisible(false);
                                javaPathDummy.setVisible(false);

                                javaPathLabel.setVisible(true);
                                javaPathPanel.setVisible(true);
                            }
                        } else if (useJavaProvidedByMinecraft.getSelectedIndex() == 1) {
                            javaMinecraftProvidedLabel.setVisible(true);
                            javaPathDummy.setVisible(true);

                            javaPathLabel.setVisible(false);
                            javaPathPanel.setVisible(false);
                        } else {
                            javaMinecraftProvidedLabel.setVisible(App.settings.useJavaProvidedByMinecraft);
                            javaPathDummy.setVisible(App.settings.useJavaProvidedByMinecraft);

                            javaPathLabel.setVisible(!App.settings.useJavaProvidedByMinecraft);
                            javaPathPanel.setVisible(!App.settings.useJavaProvidedByMinecraft);
                        }
                    });
                }
            }
        });

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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        disableLegacyLaunching = new JComboBox<>();
        disableLegacyLaunching.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        disableLegacyLaunching.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        disableLegacyLaunching.addItem(new ComboItem<>(false, GetText.tr("No")));

        if (instance.launcher.disableLegacyLaunching == null) {
            disableLegacyLaunching.setSelectedIndex(0);
        } else if (instance.launcher.disableLegacyLaunching) {
            disableLegacyLaunching.setSelectedIndex(1);
        } else {
            disableLegacyLaunching.setSelectedIndex(2);
        }

        add(disableLegacyLaunching, gbc);
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

    public void saveSettings() {
        Integer initialMemory = (Integer) this.initialMemory.getValue();
        Integer maximumMemory = (Integer) this.maximumMemory.getValue();
        Integer permGen = (Integer) this.permGen.getValue();
        String javaPath = this.javaPath.getText();
        String javaParameters = this.javaParameters.getText();
        Boolean useJavaProvidedByMinecraftVal = ((ComboItem<Boolean>) useJavaProvidedByMinecraft.getSelectedItem())
                .getValue();
        Boolean disableLegacyLaunchingVal = ((ComboItem<Boolean>) disableLegacyLaunching.getSelectedItem()).getValue();

        this.instance.launcher.initialMemory = (initialMemory == App.settings.initialMemory ? null : initialMemory);
        this.instance.launcher.maximumMemory = (maximumMemory == App.settings.maximumMemory ? null : maximumMemory);
        this.instance.launcher.permGen = (permGen == App.settings.metaspace ? null : permGen);

        boolean instanceWillUseMinecraftProvidedJava = Optional.ofNullable(useJavaProvidedByMinecraftVal)
                .orElse(App.settings.useJavaProvidedByMinecraft);

        if (!instanceWillUseMinecraftProvidedJava || instance.javaVersion == null) {
            this.instance.launcher.javaPath = (javaPath.equals(App.settings.javaPath) ? null : javaPath);
        }

        this.instance.launcher.javaArguments = (javaParameters.equals(App.settings.javaParameters) ? null
                : javaParameters);

        this.instance.launcher.useJavaProvidedByMinecraft = useJavaProvidedByMinecraftVal;
        this.instance.launcher.disableLegacyLaunching = disableLegacyLaunchingVal;
    }

}
