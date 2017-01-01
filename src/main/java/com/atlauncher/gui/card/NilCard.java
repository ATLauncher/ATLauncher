/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
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
public class NilCard extends JPanel implements RelocalizationListener {
    private static final Image defaultImage = Utils.getIconImage(new File(App.settings.getImagesDir(), "defaultimage" +
            ".png")).getImage();

    private final JTextArea error = new JTextArea();
    private final JSplitPane splitter = new JSplitPane();

    public NilCard(String message) {
        super(new BorderLayout());
        RelocalizationManager.addListener(this);

        if (Utils.isMac()) {
            this.setBorder(new TitledBorder(null, Language.INSTANCE.localize("common.nothingtoshow"), TitledBorder
                    .DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));
        } else {
            this.setBorder(new TitledBorder(null, Language.INSTANCE.localize("common.nothingtoshow"), TitledBorder
                    .DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 15)));
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
    
    public void setMessage(String message) {
        error.setText(message);
    }

    @Override
    public void onRelocalization() {
        TitledBorder border = (TitledBorder) this.getBorder();
        border.setTitle(Language.INSTANCE.localize("common.nothingtoshow"));
    }
}
