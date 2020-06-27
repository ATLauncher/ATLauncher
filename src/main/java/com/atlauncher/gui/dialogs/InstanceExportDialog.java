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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class InstanceExportDialog extends JDialog {
    private InstanceV2 instance;

    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();

    final ImageIcon HELP_ICON = Utils.getIconImage("/assets/image/Help.png");

    final GridBagConstraints gbc = new GridBagConstraints();
    final Insets LABEL_INSETS = new Insets(5, 0, 5, 10);
    final Insets FIELD_INSETS = new Insets(5, 0, 5, 0);
    final Insets LABEL_INSETS_SMALL = new Insets(0, 0, 0, 10);
    final Insets FIELD_INSETS_SMALL = new Insets(0, 0, 0, 0);

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
        setSize(500, 200);
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
        gbc.insets = LABEL_INSETS_SMALL;
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
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField author = new JTextField(30);
        author.setText(App.settings.getAccount().getMinecraftUsername());
        topPanel.add(author, gbc);

        // Export File
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover saveToLabel = new JLabelWithHover(GetText.tr("Save To") + ":", HELP_ICON,
                GetText.tr("Select the folder you wish to export the instance to"));
        topPanel.add(saveToLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel saveToPanel = new JPanel();
        saveToPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

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
        saveToPanel.add(browseButton);
        topPanel.add(saveToPanel, gbc);

        // bottom panel
        bottomPanel.setLayout(new FlowLayout());

        JButton exportButton = new JButton(GetText.tr("Export"));
        exportButton.addActionListener(arg0 -> {
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Exporting Instance"), 0,
                    GetText.tr("Exporting Instance. Please wait..."), null);

            dialog.addThread(new Thread(() -> {
                if (instance.exportAsCurseZip(name.getText(), author.getText(), saveTo.getText())) {
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
