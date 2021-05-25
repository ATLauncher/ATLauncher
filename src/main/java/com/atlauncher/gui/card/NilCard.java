/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

/**
 * Class for displaying packs in the Pack Tab.
 */
@SuppressWarnings("serial")
public class NilCard extends JPanel implements RelocalizationListener {
    private static final Image defaultImage = Utils.getIconImage("/assets/image/nil-card-image.png")
            .getImage();

    private final JTextArea error = new JTextArea();

    public NilCard(String message) {
        super(new BorderLayout());
        RelocalizationManager.addListener(this);

        this.setBorder(new TitledBorder(null, GetText.tr("Nothing To Show"), TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, App.THEME.getBoldFont().deriveFont(15f)));

        this.error.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.error.setEditable(false);
        this.error.setHighlighter(null);
        this.error.setLineWrap(true);
        this.error.setWrapStyleWord(true);
        this.error.setText(message);

        JSplitPane splitter = new JSplitPane();
        splitter.setEnabled(false);
        splitter.setLeftComponent(new ImagePanel(defaultImage));
        splitter.setRightComponent(this.error);
        splitter.setBorder(BorderFactory.createEmptyBorder());

        this.add(splitter, BorderLayout.CENTER);
    }

    public void setMessage(String message) {
        error.setText(message);
    }

    @Override
    public void onRelocalization() {
        TitledBorder border = (TitledBorder) this.getBorder();
        border.setTitle(GetText.tr("Nothing To Show"));
    }
}
