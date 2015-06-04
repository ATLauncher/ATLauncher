/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.gui.components.ToolsPanel;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.reporter.GithubIssueReporter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class GithubIssueReporterDialog extends JDialog {
    private final JTextField TITLE_FIELD = new JTextField(16);
    private final JTextArea INFO_AREA = new JTextArea(16, 16);
    private final JButton CANCEL_BUTTON = new JButton(LanguageManager.localize("common.cancel"));
    private final JButton SUBMIT_BUTTON = new JButton(LanguageManager.localize("common.submit"));

    public GithubIssueReporterDialog(JFrame parent) {
        super(parent, "Submit a bug", ModalityType.APPLICATION_MODAL);

        this.CANCEL_BUTTON.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        this.SUBMIT_BUTTON.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                App.TASKPOOL.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GithubIssueReporter.submit(TITLE_FIELD.getText() + " - " + Constants.VERSION, INFO_AREA
                                    .getText());
                        } catch (Exception e1) {
                            e1.printStackTrace(System.err);
                        }
                        dispose();
                    }
                });
            }
        });

        this.INFO_AREA.setLineWrap(true);

        this.setLocationRelativeTo(parent);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.add(this.TITLE_FIELD, BorderLayout.NORTH);
        this.add(new JScrollPane(this.INFO_AREA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.add(new ToolsPanel().add(this.CANCEL_BUTTON).add(this.SUBMIT_BUTTON), BorderLayout.SOUTH);
        this.pack();
    }
}