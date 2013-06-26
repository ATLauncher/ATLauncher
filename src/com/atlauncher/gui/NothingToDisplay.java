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
import java.awt.Font;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * Class for displaying packs in the Pack Tab
 * 
 * @author Ryan
 * 
 */
public class NothingToDisplay extends JPanel {

    private JPanel leftPanel; // Left panel with image
    private JPanel rightPanel; // Right panel with error message
    private JSplitPane splitPane; // The split pane
    private JLabel errorImage; // The image to display
    private JTextArea errorMessage; // Error message to show

    public NothingToDisplay(final String message) {
        setLayout(new BorderLayout());

        // Add titles border with name, Mac needs smaller font
        if (Utils.isMac()) {
            setBorder(new TitledBorder(null, "Nothing To Show", TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));
        } else {
            setBorder(new TitledBorder(null, "Nothing To Show", TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 15)));
        }

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setEnabled(false);

        errorImage = new JLabel(Utils.getIconImage(new File(LauncherFrame.settings.getImagesDir(),
                "defaultimage.png")));

        errorMessage = new JTextArea();
        errorMessage.setBorder(BorderFactory.createEmptyBorder());
        errorMessage.setEditable(false);
        errorMessage.setHighlighter(null);
        errorMessage.setLineWrap(true);
        errorMessage.setWrapStyleWord(true);
        errorMessage.setText(message);

        leftPanel.add(errorImage, BorderLayout.CENTER);
        rightPanel.add(errorMessage, BorderLayout.CENTER);

        add(splitPane, BorderLayout.CENTER);
    }

}
