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
package com.atlauncher.gui.tabs.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;

/**
 * Represents a single tool in Tools that the user can use.
 */
public abstract class AbstractToolPanel extends JPanel implements RelocalizationListener {

    /**
     * Label that describes tool function.
     */
    private final JLabel INFO_LABEL;

    /**
     * Button that the user presses to launch the tool.
     */
    protected final JButton LAUNCH_BUTTON = new JButton(getButtonText());

    /**
     * Convenience reference to the view model.
     */
    protected final IToolsViewModel viewModel;

    /**
     * @param viewModel View model to store for later. Can be null if you do not use it.
     */
    public AbstractToolPanel(IToolsViewModel viewModel) {
        this.viewModel = viewModel;

        setLayout(new BorderLayout());

        JPanel MIDDLE_PANEL = new JPanel();

        INFO_LABEL = new JLabel(getLabel());
        MIDDLE_PANEL.add(INFO_LABEL);

        add(MIDDLE_PANEL, BorderLayout.CENTER);

        JPanel BOTTOM_PANEL = new JPanel(new FlowLayout());

        LAUNCH_BUTTON.setFont(App.THEME.getNormalFont().deriveFont(16f));
        LAUNCH_BUTTON.addActionListener((event) -> onLaunch());
        BOTTOM_PANEL.add(LAUNCH_BUTTON);

        add(BOTTOM_PANEL, BorderLayout.SOUTH);

        setTitle();
        RelocalizationManager.addListener(this);
    }

    /**
     * Called when user launches this tool.
     */
    protected abstract void onLaunch();

    /**
     * @return Title of this view.
     */
    protected abstract @Nonnull String getTitle();

    /**
     * @return Description of what this tool does.
     */
    protected abstract @Nonnull String getLabel();

    /**
     * Set the title of the view, this also sets a border for the view.
     */
    private void setTitle() {
        setBorder(
            BorderFactory.createTitledBorder(null,
                getTitle(),
                TitledBorder.LEADING,
                TitledBorder.DEFAULT_POSITION,
                App.THEME.getBoldFont()
            )
        );
    }

    /**
     * @return Text for the launch button.
     */
    private static String getButtonText() {
        return GetText.tr("Launch");
    }

    @Override
    public void onRelocalization() {
        setTitle();
        LAUNCH_BUTTON.setText(getButtonText());
        INFO_LABEL.setText(getLabel());
    }
}
