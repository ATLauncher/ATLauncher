/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import com.atlauncher.strings.Noun;
import com.atlauncher.strings.Sentence;
import com.atlauncher.strings.Verb;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.utils.Utils;

/**
 * Class for displaying packs in the Pack Tab.
 */
@SuppressWarnings("serial")
public class NilCard extends JPanel implements RelocalizationListener {
    private static final Image defaultImage = Utils.getIconImage("/assets/image/nil-card-image.png").getImage();

    private final JTextPane error = new JTextPane();

    public NilCard(CharSequence message) {
        super(new BorderLayout());
        RelocalizationManager.addListener(this);

        this.setBorder(new TitledBorder(null,
            Sentence.PRT_X_TO_Y.capitalize().insert(Noun.NOTHING).insert(Verb.SHOW).toString(),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            App.THEME.getBoldFont().deriveFont(15f)));

        this.error.setContentType("text/html");
        this.error.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.error.setEditable(false);
        this.error.setHighlighter(null);
        this.error.setText(message.toString());

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
        border.setTitle(Sentence.PRT_X_TO_Y.capitalize().insert(Noun.NOTHING).insert(Verb.SHOW).toString());
        border.setTitleFont(App.THEME.getBoldFont().deriveFont(15f));
    }
}
