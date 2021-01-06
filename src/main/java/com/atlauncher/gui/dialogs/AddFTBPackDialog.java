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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ImportPackUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class AddFTBPackDialog extends JDialog {

    private final JTextField packId;

    private final JButton addButton;

    public AddFTBPackDialog() {
        super(App.launcher.getParent(), GetText.tr("Add FTB Pack"), ModalityType.APPLICATION_MODAL);
        setSize(450, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Add FTB Pack Dialog");

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new BorderLayout());

        JEditorPane infoMessage = new JEditorPane("text/html", new HTMLBuilder().center()
                .text(GetText.tr("Put in the ID of the pack and version to install.")).build());
        infoMessage.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        infoMessage.setEditable(false);
        middle.add(infoMessage, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // pack id

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel packIdLabel = new JLabel(GetText.tr("Pack ID") + ": ");
        mainPanel.add(packIdLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        packId = new JTextField(25);
        packId.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeAddButtonStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeAddButtonStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeAddButtonStatus();
            }
        });
        mainPanel.add(packId, gbc);

        middle.add(mainPanel, BorderLayout.CENTER);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        addButton = new JButton(GetText.tr("Add"));
        addButton.addActionListener(e -> {
            setVisible(false);

            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Adding FTB Pack"), 0,
                    GetText.tr("Adding FTB Pack"));

            dialog.addThread(new Thread(() -> {
                Analytics.sendEvent(packId.getText(), "AddFromId", "FTBPack");
                dialog.setReturnValue(ImportPackUtils.loadModpacksChPack(packId.getText()));
                dialog.close();
            }));

            dialog.start();

            if (!((boolean) dialog.getReturnValue())) {
                setVisible(true);
                DialogManager.okDialog().setTitle(GetText.tr("Failed To Add Pack"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occured when trying to add FTB pack.<br/><br/>Check the console for more information."))
                                .build())
                        .setType(DialogManager.ERROR).show();
            } else {
                dispose();
            }
        });
        addButton.setEnabled(false);
        bottom.add(addButton);

        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                setVisible(false);
                dispose();
            }
        });

        setVisible(true);
    }

    private void changeAddButtonStatus() {
        addButton.setEnabled(!packId.getText().isEmpty());
    }
}
