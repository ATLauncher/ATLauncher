/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.atlauncher.App;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class InstanceExportDialog extends JDialog {
    private InstanceV2 instance;
    private List<String> overrides = new ArrayList<>();

    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();

    final ImageIcon HELP_ICON = Utils.getIconImage("/assets/image/Help.png");

    final GridBagConstraints gbc = new GridBagConstraints();
    final Insets LABEL_INSETS = new Insets(5, 0, 5, 10);

    public InstanceExportDialog(InstanceV2 instance) {
        super(App.settings.getParent(), GetText.tr("Export {0}", instance.launcher.name),
                ModalityType.APPLICATION_MODAL);
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
        setSize(550, 400);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        topPanel.setLayout(new GridBagLayout());

        // Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover nameLabel = new JLabelWithHover(GetText.tr("Name") + ":", HELP_ICON,
                GetText.tr("The name of the instance"));
        topPanel.add(nameLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField name = new JTextField(30);
        name.setText(instance.launcher.name);
        topPanel.add(name, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover authorLabel = new JLabelWithHover(GetText.tr("Author") + ":", HELP_ICON,
                GetText.tr("Your name"));
        topPanel.add(authorLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField author = new JTextField(30);
        author.setText(App.settings.getAccount().getMinecraftUsername());
        topPanel.add(author, gbc);

        // Export File
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
        JLabelWithHover saveToLabel = new JLabelWithHover(GetText.tr("Save To") + ":", HELP_ICON,
                GetText.tr("Select the folder you wish to export the instance to"));
        topPanel.add(saveToLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel saveToPanel = new JPanel();
        saveToPanel.setLayout(new BoxLayout(saveToPanel, BoxLayout.X_AXIS));

        final JTextField saveTo = new JTextField(25);
        saveTo.setText(instance.getRoot().toAbsolutePath().toString());

        JButton browseButton = new JButton(GetText.tr("Browse"));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(saveTo.getText()));
            chooser.setDialogTitle(GetText.tr("Select path to save to"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                saveTo.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        saveToPanel.add(saveTo);
        saveToPanel.add(Box.createHorizontalStrut(5));
        saveToPanel.add(browseButton);

        topPanel.add(saveToPanel, gbc);

        // Overrides
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover overridesLabel = new JLabelWithHover(GetText.tr("Folders To Export") + ":", HELP_ICON,
                GetText.tr("Select the folders you wish to include for this export"));
        topPanel.add(overridesLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel overridesPanel = new JPanel();
        overridesPanel.setLayout(new BoxLayout(overridesPanel, BoxLayout.Y_AXIS));
        overridesPanel.setBorder(BorderFactory.createEmptyBorder(0, -3, 0, 0));

        File[] files = instance.getRoot().toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().equalsIgnoreCase(".fabric")
                        && !pathname.getName().equalsIgnoreCase(".jumploader")
                        && !pathname.getName().equalsIgnoreCase(".mixin.out")
                        && !pathname.getName().equalsIgnoreCase("disabledmods")
                        && !pathname.getName().equalsIgnoreCase("jarmods")
                        && !pathname.getName().equalsIgnoreCase("instance.json");
            }
        });

        for (File filename : files) {
            JCheckBox checkBox = new JCheckBox(filename.getName());

            checkBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (checkBox.isSelected()) {
                        overrides.add(checkBox.getText());
                    } else {
                        overrides.remove(checkBox.getText());
                    }
                }
            });

            if (filename.getName().equalsIgnoreCase("config") || filename.getName().equalsIgnoreCase("mods")
                    || filename.getName().equalsIgnoreCase("oresources")
                    || filename.getName().equalsIgnoreCase("resourcepacks")
                    || filename.getName().equalsIgnoreCase("resources")
                    || filename.getName().equalsIgnoreCase("scripts")) {
                checkBox.setSelected(true);
            }

            overridesPanel.add(checkBox);
        }

        JScrollPane overridesScrollPanel = new JScrollPane(overridesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };
        overridesScrollPanel.setPreferredSize(new Dimension(350, 200));

        topPanel.add(overridesScrollPanel, gbc);

        // bottom panel
        bottomPanel.setLayout(new FlowLayout());

        JButton exportButton = new JButton(GetText.tr("Export"));
        exportButton.addActionListener(arg0 -> {
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Exporting Instance"), 0,
                    GetText.tr("Exporting Instance. Please wait..."), null);

            dialog.addThread(new Thread(() -> {
                if (instance.exportAsCurseZip(name.getText(), author.getText(), saveTo.getText(), overrides)) {
                    App.TOASTER.pop(GetText.tr("Exported Instance Successfully"));
                } else {
                    App.TOASTER.popError(GetText.tr("Failed to export instance. Check the console for details"));
                }
                dialog.close();
                close();
            }));

            dialog.start();
        });
        bottomPanel.add(exportButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
