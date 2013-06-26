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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.atlauncher.data.Pack;

public class PacksPanel extends JPanel {

    private JPanel panel;
    private JScrollPane scrollPane;

    public PacksPanel() {
        setLayout(new BorderLayout());
        loadContent();
    }

    public void loadContent() {

        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        int count = 0;
        for (Pack pack : LauncherFrame.settings.getPacks()) {
            if (pack.hasVersions() || pack.isTester()) {
                panel.add(new PackDisplay(pack), gbc);
                gbc.gridy++;
                count++;
            }
        }
        if (count == 0) {
            panel.add(new NothingToDisplay("There are no packs to display\n\n"
                    + "Please check back another time"), gbc);
        }
    }

    public void reload() {
        removeAll();
        loadContent();
        validate();
        repaint();
    }

}