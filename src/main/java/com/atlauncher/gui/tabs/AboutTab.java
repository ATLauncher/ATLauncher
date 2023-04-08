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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.constants.Constants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.themes.ATLauncherLaf;
import com.atlauncher.utils.OS;
import com.atlauncher.viewmodel.base.IAboutTabViewModel;
import com.atlauncher.viewmodel.impl.AboutTabViewModel;

/**
 * 14 / 04 / 2022
 * <p>
 * The about tab displays to the user some basic information in regard to
 * the current state of ATLauncher, and some other basic diagnostic information
 * to let users more easily report errors.
 */
public class AboutTab extends JPanel implements Tab, RelocalizationListener {
    /**
     * Info of the current instance of ATLauncher
     */
    private final JLabel infoTitle;
    private final JPanel info;

    /**
     * Contained in [info], Displays to user various information on ATLauncher
     */
    private final JTextPane textInfo;

    /**
     * Copies [textInfo] to the users clipboard
     */
    private final JButton copyButton;

    private final JLabel authorsLabel;
    private final JScrollPane authors;

    private IAboutTabViewModel viewModel;

    public AboutTab() {
        viewModel = new AboutTabViewModel();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top info panel
        {
            info = new JPanel();
            info.setLayout(new BorderLayout());

            // Add header
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

                infoTitle = new JLabel();
                infoTitle.setText(Constants.LAUNCHER_NAME);
                infoTitle.setFont(ATLauncherLaf.getInstance().getTitleFont());
                infoTitle.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                infoPanel.add(infoTitle);

                infoPanel.add(new JSeparator());

                info.add(infoPanel, BorderLayout.PAGE_START);
            }

            // Add text info
            {
                textInfo = new JTextPane();
                textInfo.setText(viewModel.getInfo());
                textInfo.setEditable(false);
                info.add(textInfo, BorderLayout.LINE_START);
            }

            // Add copy button
            {
                copyButton = new JButton();
                copyButton.setText(GetText.tr("Copy"));
                copyButton.addActionListener(e -> {
                    OS.copyToClipboard(viewModel.getCopyInfo());
                });
                info.add(copyButton, BorderLayout.LINE_END);
            }

            // Add to layout
            {
                this.add(info, BorderLayout.NORTH);
            }
        }

        // Contributors panel
        {
            // Create list
            JPanel authorsList = new JPanel();
            authorsList.setLayout(new GridLayout(0,4));

            // Populate list
            for (String author : viewModel.getAuthors()) {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

                BackgroundImageLabel icon = new BackgroundImageLabel("https://avatars.githubusercontent.com/" + author, 64, 64);
                icon.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(icon);

                JLabel pane = new JLabel();
                pane.setText(author);
                pane.setHorizontalAlignment(SwingConstants.CENTER);
                pane.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(pane);
                authorsList.add(panel);
            }

            // Create scroll panel
            authors = new JScrollPane(authorsList);
            authors.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            SwingUtilities.invokeLater(() -> authors.getVerticalScrollBar().setValue(0));

            // Add to layout
            {
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

                JPanel authorsLabelPanel = new JPanel();
                authorsLabelPanel.setLayout(new BoxLayout(authorsLabelPanel, BoxLayout.Y_AXIS));

                authorsLabel = new JLabel();
                authorsLabel.setText("Authors:");
                authorsLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                authorsLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                authorsLabelPanel.add(authorsLabel);
                authorsLabelPanel.add(new JSeparator());

                panel.add(authorsLabelPanel, BorderLayout.NORTH);
                panel.add(authors, BorderLayout.CENTER);
                this.add(panel, BorderLayout.CENTER);
            }
        }

        RelocalizationManager.addListener(this);
    }

    @Override
    public void onRelocalization() {
        // TODO Request Ryan explain this to me
    }

    @Override
    public String getTitle() {
        return GetText.tr("About");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "About";
    }
}
