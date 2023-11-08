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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import com.atlauncher.data.Author;
import com.atlauncher.data.LauncherLibrary;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.managers.LogManager;
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
public class AboutTab extends HierarchyPanel implements Tab, RelocalizationListener {

    /**
     * Copies [textInfo] to the users clipboard
     */
    private JButton copyButton;

    private JLabel contributorLabel, acknowledgementsLabel, librariesLabel, licenseLabel;

    private IAboutTabViewModel viewModel;

    public AboutTab() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        RelocalizationManager.addListener(this);
    }

    @Override
    public void onRelocalization() {
        acknowledgementsLabel.setText(GetText.tr("Acknowledgements:"));
        copyButton.setText(GetText.tr("Copy"));
        contributorLabel.setText(GetText.tr("Contributors:"));
        librariesLabel.setText(GetText.tr("Libraries:"));
        licenseLabel.setText(GetText.tr("License:"));
    }

    @Override
    public String getTitle() {
        return GetText.tr("About");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "About";
    }

    @Override
    protected void createViewModel() {
        viewModel = new AboutTabViewModel();
    }

    @Override
    protected void onShow() {

        // Top info panel
        {
            // Add header
            {
                /**
                 * Info of the current instance of ATLauncher
                 */
                JLabel infoLabel = new JLabel();
                infoLabel.setText(Constants.LAUNCHER_NAME);
                infoLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                Box box = Box.createHorizontalBox();
                box.add(infoLabel);
                box.add(Box.createHorizontalGlue());
                add(box);

                add(new JSeparator());
            }

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.LINE_AXIS));
            info.setAlignmentY(Component.TOP_ALIGNMENT);
            info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

            // Add text info
            {
                /**
                 * Contained in [info], Displays to user various information on ATLauncher
                 */
                JTextPane textInfo = new JTextPane();
                textInfo.setText(viewModel.getInfo());
                textInfo.setEditable(false);
                info.add(textInfo);
            }

            // Add copy button
            {
                copyButton = new JButton();
                copyButton.addActionListener(e -> {
                    OS.copyToClipboard(viewModel.getCopyInfo());
                });
                info.add(copyButton);
            }

            // Add to layout
            add(info);
        }

        // Contributors panel
        {
            // Header
            {
                contributorLabel = new JLabel();
                contributorLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                contributorLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                Box box = Box.createHorizontalBox();
                box.add(contributorLabel);
                box.add(Box.createHorizontalGlue());
                add(box);
                add(new JSeparator());
            }
            // Content
            {
                // Create list
                JPanel authorsList = new JPanel();
                authorsList.setLayout(new GridLayout(1, 0));

                // Populate list
                for (Author author : viewModel.getAuthors()) {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                    BackgroundImageLabel icon = new BackgroundImageLabel(author.imageURL, 64, 64);
                    icon.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(icon);

                    JLabel pane = new JLabel();
                    pane.setText(author.name);
                    pane.setHorizontalAlignment(SwingConstants.CENTER);
                    pane.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(pane);
                    authorsList.add(panel);
                }

                // Create scroll panel
                JScrollPane contributorsScrollPane = new JScrollPane(authorsList);
                contributorsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                contributorsScrollPane.setPreferredSize(new Dimension(0, 128));
                SwingUtilities.invokeLater(() -> contributorsScrollPane.getHorizontalScrollBar().setValue(0));
                add(contributorsScrollPane);
            }
        }

        // Acknowledgements
        {
            // Label
            {
                acknowledgementsLabel = new JLabel();
                acknowledgementsLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                acknowledgementsLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                acknowledgementsLabel.setHorizontalAlignment(SwingConstants.LEADING);
                Box box = Box.createHorizontalBox();
                box.add(acknowledgementsLabel);
                box.add(Box.createHorizontalGlue());
                add(box);
                add(new JSeparator());
            }

            // Content
            {

                // Image sources
                {

                }

                // Libraries
                JPanel librariesPanel = new JPanel();
                {
                    librariesPanel.setLayout(new BoxLayout(librariesPanel, BoxLayout.PAGE_AXIS));

                    librariesLabel = new JLabel();
                    librariesLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                    librariesLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                    Box box = Box.createHorizontalBox();
                    box.add(librariesLabel);
                    box.add(Box.createHorizontalGlue());
                    librariesPanel.add(box);
                    librariesPanel.add(new JSeparator());

                    JPanel librariesListPanel = new JPanel();
                    librariesListPanel.setLayout(new BoxLayout(librariesListPanel, BoxLayout.PAGE_AXIS));
                    for (LauncherLibrary library : viewModel.getLibraries()) {
                        JButton button = new JButton();
                        button.setText(library.name);
                        button.addActionListener(event -> {
                            try {
                                Desktop.getDesktop().browse(library.link);
                            } catch (IOException e) {
                                LogManager.logStackTrace(e);
                            }
                        });
                        librariesListPanel.add(button);
                    }
                    librariesPanel.add(librariesListPanel);
                }
                // License
                JPanel licensePanel = new JPanel();
                {
                    licensePanel.setLayout(new BoxLayout(licensePanel, BoxLayout.PAGE_AXIS));
                    licenseLabel = new JLabel();
                    licenseLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                    licenseLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                    Box box = Box.createHorizontalBox();
                    box.add(licenseLabel);
                    box.add(Box.createHorizontalGlue());
                    licensePanel.add(box);
                    licensePanel.add(new JSeparator());
                    JTextPane license = new JTextPane();
                    license.setEditable(false);
                    license.setText("ATLauncher - https://github.com/ATLauncher/ATLauncher\n" +
                        "Copyright (C) 2013-2023 ATLauncher\n" +
                        "\n" +
                        "This program is free software: you can redistribute it and/or modify " +
                        "it under the terms of the GNU General Public License as published by " +
                        "the Free Software Foundation, either version 3 of the License, or " +
                        "(at your option) any later version.\n" +
                        "\n" +
                        "This program is distributed in the hope that it will be useful, " +
                        "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
                        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the " +
                        "GNU General Public License for more details.\n" +
                        "\n" +
                        "You should have received a copy of the GNU General Public License " +
                        "along with this program. If not, see <http://www.gnu.org/licenses/>.\n");
                    JScrollPane scrollPane = new JScrollPane(license);
                    licensePanel.add(scrollPane);
                }

                add(librariesPanel);
                add(licensePanel);
            }
        }

        // Stickers
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

            BackgroundImageLabel gpl = new BackgroundImageLabel("https://www.gnu.org/graphics/gplv3-88x31.png", 88, 31);
            gpl.setToolTipText("GPLv3");
            panel.add(gpl);

            BackgroundImageLabel nodecraft = new BackgroundImageLabel("https://nodecraft.com/assets/images/community/banner/ncsupportlogo.jpg", 32, 32);
            nodecraft.setToolTipText("Nodecraft");
            panel.add(nodecraft);

            add(panel);
        }
        onRelocalization();
    }

    @Override
    protected void onDestroy() {
        removeAll();

        copyButton = null;
        contributorLabel = null;
        acknowledgementsLabel = null;
        librariesLabel = null;
        licenseLabel = null;
    }
}
