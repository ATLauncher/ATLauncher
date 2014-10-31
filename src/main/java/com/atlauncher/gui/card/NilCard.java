/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;

/**
 * Class for displaying packs in the Pack Tab
 *
 * @author Ryan
 */
public class NilCard extends JPanel {
    private static final Image defaultImage = Utils.getIconImage(new File(App.settings.getImagesDir(),
            "defaultimage.png")).getImage();

    private final JTextArea error = new JTextArea();
    private final JSplitPane splitter = new JSplitPane();

    public NilCard(String message) {
        super(new BorderLayout());

        if (Utils.isMac()) {
            this.setBorder(new TitledBorder(null, Language.INSTANCE.localize("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif",
                    Font.BOLD, 14)));
        } else {
            this.setBorder(new TitledBorder(null, Language.INSTANCE.localize("common.nothingtoshow"),
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif",
                    Font.BOLD, 15)));
        }

        this.error.setBorder(BorderFactory.createEmptyBorder());
        this.error.setEditable(false);
        this.error.setHighlighter(null);
        this.error.setLineWrap(true);
        this.error.setWrapStyleWord(true);
        this.error.setText(message);

        this.splitter.setEnabled(false);
        this.splitter.setLeftComponent(new ImagePanel(defaultImage));
        this.splitter.setRightComponent(this.error);

        this.add(this.splitter, BorderLayout.CENTER);
    }
}
