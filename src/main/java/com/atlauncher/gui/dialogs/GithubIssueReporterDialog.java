package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.gui.components.ToolsPanel;
import com.atlauncher.reporter.GithubIssueReporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public final class GithubIssueReporterDialog extends JDialog {
    private final JTextField TITLE_FIELD = new JTextField(16);
    private final JTextArea INFO_AREA = new JTextArea(16, 16);
    private final JButton CANCEL_BUTTON = new JButton("Cancel");
    private final JButton SUBMIT_BUTTON = new JButton("Submit");

    public GithubIssueReporterDialog(JFrame parent){
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
                            GithubIssueReporter.submit(TITLE_FIELD.getText() + " - " + Constants.VERSION, INFO_AREA.getText());
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
        this.add(new JScrollPane(this.INFO_AREA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.add(new ToolsPanel()
                .add(this.CANCEL_BUTTON)
                .add(this.SUBMIT_BUTTON), BorderLayout.SOUTH);
        this.pack();
    }
}