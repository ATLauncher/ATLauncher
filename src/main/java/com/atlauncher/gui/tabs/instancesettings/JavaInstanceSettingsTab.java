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
package com.atlauncher.gui.tabs.instancesettings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.javafinder.JavaInfo;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;

public class JavaInstanceSettingsTab extends JPanel {
    private final Instance instance;

    private JSpinner maximumMemory;
    private JSpinner permGen;
    private JTextField javaPath;
    private JTextArea javaParameters;
    private JComboBox<ComboItem<String>> javaRuntimeOverride;
    private JComboBox<ComboItem<Boolean>> useJavaProvidedByMinecraft;
    private JComboBox<ComboItem<Boolean>> disableLegacyLaunching;
    private JComboBox<ComboItem<Boolean>> useSystemGlfw;
    private JComboBox<ComboItem<Boolean>> useSystemOpenAl;

    private boolean permgenWarningShown = false;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
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
        permGen.addChangeListener(e -> {
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
        });
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
                    javaPath.setText(selectedPath.getParent());
                } else {
                    javaPath.setText(selectedPath.getAbsolutePath());
                }
            }
        });

        JComboBox<ComboItem<JavaInfo>> installedJavasComboBox = new JComboBox<>();
        installedJavasComboBox.setPreferredSize(new Dimension(516, 24));
        installedJavasComboBox.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        List<JavaInfo> installedJavas = Java.getInstalledJavas();
        int selectedIndex = 0;

        for (JavaInfo javaInfo : installedJavas) {
            installedJavasComboBox.addItem(new ComboItem<>(javaInfo, javaInfo.toString()));

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
        javaMinecraftProvidedLabel.setVisible(instance.javaVersion != null && isUsingMinecraftProvidedJava);
        javaPathDummy.setVisible(instance.javaVersion != null && isUsingMinecraftProvidedJava);

        javaPathLabel.setVisible(instance.javaVersion == null || !isUsingMinecraftProvidedJava);
        javaPathPanel.setVisible(instance.javaVersion == null || !isUsingMinecraftProvidedJava);

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
        JScrollPane javaParametersScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        javaParametersScrollPane.setBorder(new FlatScrollPaneBorder());
        javaParametersScrollPane.setMaximumSize(new Dimension(1000, 200));

        javaParameters = new JTextArea(6, 40);
        ((AbstractDocument) javaParameters.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                    fb.insertString(offset, string.replaceAll("[\n\r]", ""), attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                    fb.replace(offset, length, text.replaceAll("[\n\r]", ""), attrs);
                }
            });
        javaParameters.setText(getIfNotNull(this.instance.launcher.javaArguments, App.settings.javaParameters));
        javaParameters.setLineWrap(true);
        javaParameters.setWrapStyleWord(true);
        JButton javaParametersResetButton = new JButton(GetText.tr("Reset"));
        javaParametersResetButton.addActionListener(e -> javaParameters.setText(App.settings.javaParameters));

        javaParametersScrollPane.setViewportView(javaParameters);
        javaParametersPanel.add(javaParametersScrollPane);
        javaParametersPanel.add(Box.createHorizontalStrut(5));

        Box paramsResetBox = Box.createVerticalBox();
        paramsResetBox.add(javaParametersResetButton);
        paramsResetBox.add(Box.createVerticalGlue());

        javaParametersPanel.add(paramsResetBox);

        add(javaParametersPanel, gbc);

        // Runtime Override

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover javaRuntimeOverrideLabel = new JLabelWithHover(GetText.tr("Runtime Override") + ":",
            HELP_ICON,
            new HTMLBuilder().center().text(GetText.tr(
                    "This allows you to override which runtime is used to launch this instance.<br/><br/>Runtimes are provided by Mojang and used to launch the game and generally correspond to a particular Java version.<br/><br/>Changing this is usually not required or recommended."))
                .build());
        add(javaRuntimeOverrideLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        javaRuntimeOverride = new JComboBox<>();
        if (instance.javaVersion != null) {
            javaRuntimeOverride.addItem(new ComboItem<>(null, GetText.tr("Use Default (Recommended)")));
        } else {
            javaRuntimeOverride.addItem(new ComboItem<>(null, GetText.tr("Use System Java")));
        }

        int selectedIndexRuntime = 0;
        Map<String, List<JavaRuntime>> runtimes = Data.JAVA_RUNTIMES.getForSystem();
        for (String runtime : runtimes.keySet()) {
            List<JavaRuntime> runtimeObject = runtimes.get(runtime);

            if (runtimeObject != null && !runtimeObject.isEmpty()) {
                javaRuntimeOverride.addItem(
                    new ComboItem<>(runtime,
                        String.format("%s (Java %s)", runtime, runtimeObject.get(0).version.name)));

                if (this.instance.launcher.javaRuntimeOverride != null
                    && this.instance.launcher.javaRuntimeOverride.equals(runtime)) {
                    selectedIndexRuntime = javaRuntimeOverride.getItemCount() - 1;
                }
            }
        }

        javaRuntimeOverride.setSelectedIndex(selectedIndexRuntime);
        javaRuntimeOverrideLabel.setVisible(isUsingMinecraftProvidedJava);
        javaRuntimeOverride.setVisible(isUsingMinecraftProvidedJava);

        add(javaRuntimeOverride, gbc);

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

        useJavaProvidedByMinecraft.addItemListener(e -> {
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
                            javaRuntimeOverrideLabel.setVisible(false);
                            javaRuntimeOverride.setVisible(false);

                            javaPathLabel.setVisible(true);
                            javaPathPanel.setVisible(true);
                        }
                    } else if (useJavaProvidedByMinecraft.getSelectedIndex() == 1) {
                        javaMinecraftProvidedLabel.setVisible(true);
                        javaPathDummy.setVisible(true);
                        javaRuntimeOverrideLabel.setVisible(true);
                        javaRuntimeOverride.setVisible(true);

                        javaPathLabel.setVisible(false);
                        javaPathPanel.setVisible(false);
                    } else {
                        javaMinecraftProvidedLabel.setVisible(App.settings.useJavaProvidedByMinecraft);
                        javaPathDummy.setVisible(App.settings.useJavaProvidedByMinecraft);
                        javaRuntimeOverrideLabel.setVisible(App.settings.useJavaProvidedByMinecraft);
                        javaRuntimeOverride.setVisible(App.settings.useJavaProvidedByMinecraft);

                        javaPathLabel.setVisible(!App.settings.useJavaProvidedByMinecraft);
                        javaPathPanel.setVisible(!App.settings.useJavaProvidedByMinecraft);
                    }
                });
            }
        });

        useJavaProvidedByMinecraftLabel.setVisible(instance.javaVersion != null);
        useJavaProvidedByMinecraft.setVisible(instance.javaVersion != null);

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

        // Use System GLFW
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useSystemGlfwLabel = new JLabelWithHover(GetText.tr("Use System GLFW") + "?", HELP_ICON,
            new HTMLBuilder().center().text(GetText.tr("Use the systems install for GLFW native library."))
                .build());
        add(useSystemGlfwLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        useSystemGlfw = new JComboBox<>();
        useSystemGlfw.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        useSystemGlfw.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        useSystemGlfw.addItem(new ComboItem<>(false, GetText.tr("No")));

        if (instance.launcher.useSystemGlfw == null) {
            useSystemGlfw.setSelectedIndex(0);
        } else if (instance.launcher.useSystemGlfw) {
            useSystemGlfw.setSelectedIndex(1);
        } else {
            useSystemGlfw.setSelectedIndex(2);
        }

        add(useSystemGlfw, gbc);

        // Use System OpenAL
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover useSystemOpenAlLabel = new JLabelWithHover(GetText.tr("Use System OpenAL") + "?", HELP_ICON,
            new HTMLBuilder().center().text(GetText.tr("Use the systems install for OpenAL native library."))
                .build());
        add(useSystemOpenAlLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        useSystemOpenAl = new JComboBox<>();
        useSystemOpenAl.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        useSystemOpenAl.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        useSystemOpenAl.addItem(new ComboItem<>(false, GetText.tr("No")));

        if (instance.launcher.useSystemOpenAl == null) {
            useSystemOpenAl.setSelectedIndex(0);
        } else if (instance.launcher.useSystemOpenAl) {
            useSystemOpenAl.setSelectedIndex(1);
        } else {
            useSystemOpenAl.setSelectedIndex(2);
        }

        add(useSystemOpenAl, gbc);
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
        if (javaParameters.getText().contains("-Xmx")
            || javaParameters.getText().contains("-XX:PermSize")
            || javaParameters.getText().contains("-XX:MetaspaceSize")) {
            DialogManager.okDialog().setTitle(GetText.tr("Help")).setContent(new HTMLBuilder().center().text(GetText.tr(
                    "The entered Java Parameters were incorrect.<br/><br/>Please remove any references to Xmx or XX:PermSize."))
                .build()).setType(DialogManager.ERROR).show();
            return false;
        }
        return true;
    }

    public void saveSettings() {
        Integer maximumMemory = (Integer) this.maximumMemory.getValue();
        Integer permGen = (Integer) this.permGen.getValue();
        String javaPath = this.javaPath.getText();
        String javaParameters = this.javaParameters.getText();
        String javaRuntimeOverrideVal = ((ComboItem<String>) javaRuntimeOverride.getSelectedItem())
            .getValue();
        Boolean useJavaProvidedByMinecraftVal = ((ComboItem<Boolean>) useJavaProvidedByMinecraft.getSelectedItem())
            .getValue();
        Boolean disableLegacyLaunchingVal = ((ComboItem<Boolean>) disableLegacyLaunching.getSelectedItem()).getValue();
        Boolean useSystemGlfwVal = ((ComboItem<Boolean>) useSystemGlfw.getSelectedItem()).getValue();
        Boolean useSystemOpenAlVal = ((ComboItem<Boolean>) useSystemOpenAl.getSelectedItem()).getValue();

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
        this.instance.launcher.javaRuntimeOverride = javaRuntimeOverrideVal;
        this.instance.launcher.useSystemGlfw = useSystemGlfwVal;
        this.instance.launcher.useSystemOpenAl = useSystemOpenAlVal;
    }
}
