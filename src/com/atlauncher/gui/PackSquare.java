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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.Highlighter;

public class PackSquare extends JPanel {

    private JPanel packActions;
    private JLabel packImage;
    private JButton newInstance;
    private JButton info;
    private JTextArea packDescription;
    private JSplitPane splitPane;
    private JPanel leftPanel, rightPanel;

    public PackSquare() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(45, 50, 55)));
        splitPane = new JSplitPane();
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        rightPanel.setLayout(new BorderLayout());
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setEnabled(false);

        packImage = new JLabel(Utils.getIconImage("/resources/test.png")); // 213x168 Maximum

        packActions = new JPanel();
        packActions.setLayout(new FlowLayout());
        newInstance = new JButton("New Instance");
        info = new JButton("Info");
        packActions.add(newInstance);
        packActions.add(info);
        packDescription = new JTextArea();
        packDescription.setBorder(BorderFactory.createEmptyBorder());
        packDescription.setEditable(false);
        packDescription.setHighlighter(null);
        packDescription.setLineWrap(true);
        packDescription.setWrapStyleWord(true);
        packDescription
                .setText("This pack is for the magic users out there and features some of the best magical mods and some of the up and coming ones. Thaumcraft and Ars Magica are part of this pack along with new ones such as Magical Crops and Elemental Tinkerer!");
        leftPanel.add(packImage, BorderLayout.CENTER);
        rightPanel.add(packDescription, BorderLayout.CENTER);
        rightPanel.add(packActions, BorderLayout.SOUTH);
        add(splitPane, BorderLayout.CENTER);
    }

}
