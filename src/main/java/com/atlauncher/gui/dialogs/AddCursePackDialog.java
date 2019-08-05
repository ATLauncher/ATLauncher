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
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.gui.handlers.CursePackTransferHandler;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CursePackUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class AddCursePackDialog extends JDialog {
    private JPanel middle;
    private JPanel bottom;

    private JLabel urlLabel;
    private JTextField url;

    private JButton addButton;

    public AddCursePackDialog() {
        super(App.settings.getParent(), GetText.tr("Add Curse Pack"), ModalityType.APPLICATION_MODAL);
        setSize(450, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Add Curse Pack Dialog");

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new BorderLayout());

        JEditorPane infoMessage = new JEditorPane("text/html",
                new HTMLBuilder().center().text(GetText.tr("Paste in a link to a modpack on CurseForge")).build());
        infoMessage.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        infoMessage.setEditable(false);
        middle.add(infoMessage, BorderLayout.NORTH);

        JPanel urlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        urlLabel = new JLabel(GetText.tr("Curse Url") + ": ");
        urlPanel.add(urlLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        url = new JTextField(25);

        try {
            String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);

            if (clipboard.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
                url.setText(clipboard);
            }
        } catch (HeadlessException | UnsupportedFlavorException | IOException ignored) {
        }

        urlPanel.add(url, gbc);
        middle.add(urlPanel, BorderLayout.CENTER);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        addButton = new JButton(GetText.tr("Add"));
        addButton.addActionListener(e -> {
            setVisible(false);

            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Adding Curse Pack"), 0,
                    GetText.tr("Adding Curse Pack"));

            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(CursePackUtils.loadFromUrl(url.getText()));
                dialog.close();
            }));

            dialog.start();

            if (!((boolean) dialog.getReturnValue())) {
                DialogManager.okDialog().setTitle(GetText.tr("Failed To Add Pack"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occured when trying to add Curse pack.<br/><br/>Check the console for more information."))
                                .build())
                        .setType(DialogManager.ERROR).show();
            }
            dispose();
        });
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

}
