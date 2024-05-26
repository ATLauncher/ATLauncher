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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.utils.Utils;

/**
 * Class for displaying packs in the Pack Tab.
 */
@SuppressWarnings("serial")
public class NilCard extends JPanel implements RelocalizationListener {
    private static final Image defaultImage = Utils.getIconImage("/assets/image/default-image.png").getImage();

    private final JPanel column = new JPanel();
    private final JPanel row = new JPanel();
    private final JTextPane errorMessage = new JTextPane();

    public NilCard(@Nonnull String message) {
        this(message, null);
    }

    public NilCard(@Nonnull String message, @Nullable Action[] actions) {
        super(new BorderLayout());
        RelocalizationManager.addListener(this);

        this.setBorder(new TitledBorder(null, GetText.tr("Nothing To Show"), TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, App.THEME.getBoldFont().deriveFont(15f)));

        column.setLayout(new BoxLayout(column, BoxLayout.PAGE_AXIS));

        this.errorMessage.setContentType("text/html");
        this.errorMessage.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.errorMessage.setEditable(false);
        this.errorMessage.setHighlighter(null);
        this.errorMessage.setText(message);
        column.add(errorMessage);

        row.setLayout(new FlowLayout());
        setActions(actions, false);
        column.add(row);

        JSplitPane splitter = new JSplitPane();
        splitter.setEnabled(false);
        splitter.setLeftComponent(new ImagePanel(() -> defaultImage));
        splitter.setRightComponent(this.column);
        splitter.setBorder(BorderFactory.createEmptyBorder());

        this.add(splitter, BorderLayout.CENTER);
    }

    public void setActions(@Nullable Action[] actions) {
        setActions(actions, true);
    }

    private void setActions(@Nullable Action[] actions, boolean revalidate) {
        if (actions != null)
            for (Action action : actions) {
                JButton button = new JButton(action.name);
                button.addActionListener(action.onClicked);
                row.add(button);
            }

        if (revalidate) {
            revalidate();
            repaint();
        }
    }

    public void setMessage(String message) {
        errorMessage.setText(message);
    }

    @Override
    public void onRelocalization() {
        TitledBorder border = (TitledBorder) this.getBorder();
        border.setTitle(GetText.tr("Nothing To Show"));
        border.setTitleFont(App.THEME.getBoldFont().deriveFont(15f));
    }

    public static class Action {
        public final String name;
        public final ActionListener onClicked;

        public Action(String name, ActionListener onClicked) {
            this.name = name;
            this.onClicked = onClicked;
        }

        public static Action createCreatePackAction() {
            return new NilCard.Action(
                    GetText.tr("Create Pack"),
                    e -> App.navigate(UIConstants.LAUNCHER_CREATE_PACK_TAB));
        }

        public static Action createDownloadPackAction() {
            return new NilCard.Action(
                    GetText.tr("Download Pack"),
                    e -> App.navigate(UIConstants.LAUNCHER_PACKS_TAB));
        }
    }
}
