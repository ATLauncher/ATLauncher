/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
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

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

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
            setBorder(new TitledBorder(null,
                    App.settings.getLocalizedString("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
                            "SansSerif", Font.BOLD, 14)));
        } else {
            setBorder(new TitledBorder(null,
                    App.settings.getLocalizedString("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
                            "SansSerif", Font.BOLD, 15)));
        }

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setEnabled(false);

        errorImage = new JLabel(Utils.getIconImage(new File(App.settings.getImagesDir(),
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
