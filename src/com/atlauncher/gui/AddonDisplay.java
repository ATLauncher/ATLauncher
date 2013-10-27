/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.atlauncher.App;
import com.atlauncher.data.Addon;
import com.atlauncher.utils.Utils;

/**
 * Class for displaying addons in the Addons Tab
 * 
 * @author Ryan
 * 
 */
public class AddonDisplay extends JPanel {

    private JPanel leftPanel; // Left panel with image
    private JPanel rightPanel; // Right panel with description and actions
    private JSplitPane splitPane; // The split pane
    private JLabel addonImage; // The image for the addon
    private JTextArea addonDescription; // Description of the addon
    private JPanel addonActions; // All the actions that can be performed on the pack
    private JButton install; // Install button

    public AddonDisplay(final Addon addon) {
        setLayout(new BorderLayout());

        // Add titles border with name, Mac needs smaller font
        if (Utils.isMac()) {
            setBorder(new TitledBorder(null, addon.getName(), TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));
        } else {
            setBorder(new TitledBorder(null, addon.getName(), TitledBorder.DEFAULT_JUSTIFICATION,
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

        addonImage = new JLabel(Utils.getIconImage("/resources/DefaultImage.png"));

        addonDescription = new JTextArea();
        addonDescription.setBorder(BorderFactory.createEmptyBorder());
        addonDescription.setEditable(false);
        addonDescription.setHighlighter(null);
        addonDescription.setLineWrap(true);
        addonDescription.setWrapStyleWord(true);
        addonDescription.setText(addon.getDescription());

        addonActions = new JPanel();
        addonActions.setLayout(new FlowLayout());
        install = new JButton(App.settings.getLocalizedString("common.install"));
        addonActions.add(install);

        leftPanel.add(addonImage, BorderLayout.CENTER);
        rightPanel.add(addonDescription, BorderLayout.CENTER);
        rightPanel.add(addonActions, BorderLayout.SOUTH);

        add(splitPane, BorderLayout.CENTER);
    }

}
