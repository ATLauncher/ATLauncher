/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.utils.Utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * Class for displaying packs in the Pack Tab
 *
 * @author Ryan
 */
public class NothingToDisplay
        extends JPanel{
    private static final Image dfImg = Utils.getIconImage(
            new File(App.settings.getImagesDir(), "defaultimage.png")
    ).getImage();

    private final JTextArea error = new JTextArea();
    private final JSplitPane splitter = new JSplitPane();

    public NothingToDisplay(String message){
        super(new BorderLayout());
        if(Utils.isMac()){
            this.setBorder(new TitledBorder(null,
                    App.settings.getLocalizedString("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
                    "SansSerif", Font.BOLD, 14)));
        } else{
            this.setBorder(new TitledBorder(null,
                    App.settings.getLocalizedString("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
                    "SansSerif", Font.BOLD, 15)));
        }

        this.error.setBorder(BorderFactory.createEmptyBorder());
        this.error.setEditable(false);
        this.error.setHighlighter(null);
        this.error.setLineWrap(true);
        this.error.setWrapStyleWord(true);
        this.error.setText(message);

        this.splitter.setEnabled(false);
        this.splitter.setLeftComponent(new ImagePanel(dfImg));
        this.splitter.setRightComponent(this.error);

        this.add(this.splitter, BorderLayout.CENTER);
    }
}
