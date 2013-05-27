/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class InstancesPanel extends JPanel {

    private InstancesTable instancesTable;
    private JSplitPane splitPane;
    private JPanel packActions;
    private JButton playButton;
    private JButton backupButton;
    private JButton deleteButton;

    public InstancesPanel() {
        setLayout(new BorderLayout());

        instancesTable = new InstancesTable();
        instancesTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        playButton.setEnabled(true);
                        backupButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    }
                });

        packActions = new JPanel(new FlowLayout());

        playButton = new JButton("Play");
        playButton.setEnabled(false);
        packActions.add(playButton);

        backupButton = new JButton("Backups");
        backupButton.setEnabled(false);
        packActions.add(backupButton);

        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int todo = JOptionPane.showConfirmDialog(InstancesPanel.this,
                        "Are you sure you want to delete the instance \""
                                + instancesTable.getSelectedInstance()
                                        .getName() + "\"?", "Are you sure?",
                        JOptionPane.YES_NO_OPTION);
                if (todo == JOptionPane.YES_OPTION) {
                    int selected = instancesTable.getSelectedRow();
                    LauncherFrame.settings.getInstances().remove(
                            instancesTable.getSelectedInstance());
                    reloadTable();
                    if (selected == instancesTable.getModel().getRowCount()) {
                        selected--;
                    }
                    instancesTable.getSelectionModel().setSelectionInterval(
                            selected, selected);
                }
            }
        });

        packActions.add(deleteButton);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(
                instancesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), packActions);
        splitPane.setEnabled(false);
        splitPane.setDividerLocation(375);
        add(splitPane, BorderLayout.CENTER);
    }

    public void reloadTable() {
        instancesTable.reload();
        if (instancesTable.getModel().getRowCount() == 0) {
            playButton.setEnabled(false);
            backupButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            if (instancesTable.getSelectedRow() == -1) {
                instancesTable.getSelectionModel().setSelectionInterval(0, 0);
            }
            playButton.setEnabled(true);
            backupButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }
}
