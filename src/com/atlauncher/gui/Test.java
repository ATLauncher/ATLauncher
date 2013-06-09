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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;

public class Test extends JPanel {

    /**
     * Create the panel.
     */
    public Test() {
        setLayout(new BorderLayout(0, 0));
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        scrollPane.setViewportView(panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        JPanel panel_2 = new JPanel();
        GridBagConstraints gbc_panel_2 = new GridBagConstraints();
        gbc_panel_2.insets = new Insets(0, 0, 5, 5);
        gbc_panel_2.fill = GridBagConstraints.BOTH;
        gbc_panel_2.gridx = 0;
        gbc_panel_2.gridy = 0;
        panel.add(panel_2, gbc_panel_2);
        
        JPanel panel_3 = new JPanel();
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.insets = new Insets(0, 0, 5, 0);
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 1;
        gbc_panel_3.gridy = 0;
        panel.add(panel_3, gbc_panel_3);
        
        JPanel panel_5 = new JPanel();
        GridBagConstraints gbc_panel_5 = new GridBagConstraints();
        gbc_panel_5.insets = new Insets(0, 0, 5, 5);
        gbc_panel_5.fill = GridBagConstraints.BOTH;
        gbc_panel_5.gridx = 0;
        gbc_panel_5.gridy = 1;
        panel.add(panel_5, gbc_panel_5);
        
        JPanel panel_6 = new JPanel();
        GridBagConstraints gbc_panel_6 = new GridBagConstraints();
        gbc_panel_6.insets = new Insets(0, 0, 5, 0);
        gbc_panel_6.fill = GridBagConstraints.BOTH;
        gbc_panel_6.gridx = 1;
        gbc_panel_6.gridy = 1;
        panel.add(panel_6, gbc_panel_6);
        
        JPanel panel_4 = new JPanel();
        GridBagConstraints gbc_panel_4 = new GridBagConstraints();
        gbc_panel_4.insets = new Insets(0, 0, 0, 5);
        gbc_panel_4.fill = GridBagConstraints.BOTH;
        gbc_panel_4.gridx = 0;
        gbc_panel_4.gridy = 2;
        panel.add(panel_4, gbc_panel_4);
        
        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 1;
        gbc_panel_1.gridy = 2;
        panel.setPreferredSize(new Dimension(1, 600));
        panel.add(panel_1, gbc_panel_1);

    }

}
