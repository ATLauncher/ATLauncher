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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Contributor;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.managers.LogManager;
import com.atlauncher.themes.ATLauncherLaf;
import com.atlauncher.utils.OS;
import com.atlauncher.viewmodel.base.IAboutTabViewModel;
import com.atlauncher.viewmodel.impl.AboutTabViewModel;

/**
 * The about tab displays to the user some basic information in regard to the current state of ATLauncher, and some
 * other basic diagnostic information to let users more easily report errors.
 */
public class AboutTab extends HierarchyPanel implements Tab {
    private JLabel contributorsLabel;
    private JScrollPane contributorsScrollPane;
    private JPanel authorsList;

    private IAboutTabViewModel viewModel;

    public AboutTab() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
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
            // Add header, info of the current instance of ATLauncher
            {
                JLabel infoLabel = new JLabel();
                infoLabel.setText(Constants.LAUNCHER_NAME);
                infoLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                infoLabel.setBorder(BorderFactory.createEmptyBorder(0, UIConstants.SPACING_LARGE, 0, 0));
                Box box = Box.createHorizontalBox();
                box.add(infoLabel);
                box.add(Box.createHorizontalGlue());
                add(box);

                add(new JSeparator() {
                    {
                        setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
                    }
                });
            }

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.LINE_AXIS));
            info.setAlignmentY(Component.TOP_ALIGNMENT);
            info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

            // Add text info, contained in [info], Displays to user various information on ATLauncher
            {
                JTextPane textInfo = new JTextPane();
                textInfo.setText(viewModel.getInfo());
                textInfo.setEditable(false);
                textInfo.setFocusable(false);
                info.add(textInfo);
            }

            // Add to layout
            add(info);
        }

        add(Box.createVerticalStrut(5));

        // Contributors panel
        {
            // Header
            {
                contributorsLabel = new JLabel(GetText.tr("Contributors"));
                contributorsLabel.setFont(ATLauncherLaf.getInstance().getTitleFont());
                contributorsLabel.setBorder(
                    BorderFactory.createEmptyBorder(UIConstants.SPACING_XLARGE, UIConstants.SPACING_LARGE, 0, 0));

                Box box = Box.createHorizontalBox();
                box.add(contributorsLabel);
                box.add(Box.createHorizontalGlue());
                add(box);
                add(new JSeparator() {
                    {
                        setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
                    }
                });
                add(Box.createVerticalStrut(5));
            }
            // Content
            {
                // Create list
                authorsList = new JPanel();
                authorsList.setLayout(new GridLayout(1, 0));

                // Populate list
                addDisposable(viewModel.getContributors().subscribe(this::renderAuthors));

                // Create scroll panel
                contributorsScrollPane = new JScrollPane(authorsList);
                contributorsScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
                contributorsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                contributorsScrollPane.setPreferredSize(new Dimension(0, 100));
                contributorsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                add(contributorsScrollPane);
            }
        }

        add(Box.createVerticalStrut(5));

        JTabbedPane tabbedPane = new JTabbedPane();

        // License Panel
        {
            JPanel licensePanel = new JPanel();
            licensePanel.setLayout(new BoxLayout(licensePanel, BoxLayout.Y_AXIS));

            JEditorPane license = new JEditorPane("text/html", "");
            license.setEditable(false);
            license.setFocusable(false);
            license.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OS.openWebBrowser(e.getURL());
                }
            });

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(App.class.getResourceAsStream("/LICENSE"),
                    StandardCharsets.UTF_8))) {
                license.setText(
                    new HTMLBuilder()
                        .text(reader.lines().collect(Collectors.joining("<br/>"))
                            .replace("%YEAR%", new SimpleDateFormat("yyyy").format(new Date())))
                        .build());
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
            JScrollPane scrollPane = new JScrollPane(license);
            scrollPane.setPreferredSize(new Dimension(0, 220));
            SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
            licensePanel.add(scrollPane);

            tabbedPane.addTab("License", licensePanel);
        }

        {
            // Third Party Libraries Panel
            {
                JPanel thirdPartyLibrariesPanel = new JPanel();
                thirdPartyLibrariesPanel.setLayout(new BoxLayout(thirdPartyLibrariesPanel, BoxLayout.Y_AXIS));

                JEditorPane thirdPartyLibraries = new JEditorPane("text/html", "");
                thirdPartyLibraries.setEditable(false);
                thirdPartyLibraries.setFocusable(false);
                thirdPartyLibraries.addHyperlinkListener(e -> {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        OS.openWebBrowser(e.getURL());
                    }
                });
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(App.class.getResourceAsStream("/THIRDPARTYLIBRARIES"),
                        StandardCharsets.UTF_8))) {
                    thirdPartyLibraries.setText(
                        new HTMLBuilder()
                            .text(reader.lines().collect(Collectors.joining("<br/>"))
                                .replace("%YEAR%", new SimpleDateFormat("yyyy").format(new Date())))
                            .build());
                } catch (Exception e) {
                    LogManager.logStackTrace(e);
                }
                JScrollPane scrollPane = new JScrollPane(thirdPartyLibraries);
                scrollPane.setPreferredSize(new Dimension(0, 220));
                SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
                thirdPartyLibrariesPanel.add(scrollPane);

                tabbedPane.addTab("Third Party Libraries", thirdPartyLibrariesPanel);
            }

            add(tabbedPane);
        }
    }

    @Override
    protected void onDestroy() {
        removeAll();

        contributorsLabel = null;
        authorsList = null;
        contributorsScrollPane = null;
    }

    /**
     * Accepts contributors to render onto the screen.
     *
     * @param contributors contributors to render
     */
    private void renderAuthors(List<Contributor> contributors) {
        for (Contributor contributor : contributors) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(
                BorderFactory.createEmptyBorder(0, UIConstants.SPACING_XLARGE, 0, UIConstants.SPACING_XLARGE));
            // panel.setSize(new Dimension(120, 0));

            BackgroundImageLabel icon = new BackgroundImageLabel(contributor.avatarUrl, 64, 64);
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        OS.openWebBrowser(contributor.url);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
            panel.add(icon);

            JEditorPane contributorName = new JEditorPane("text/html",
                "<html><p style=\"padding: 0\" align=\"center\"><a href=\"" + contributor.url + "\">"
                    + contributor.name
                    + "</a></p></html>");
            contributorName.setEditable(false);
            contributorName.setFocusable(false);
            contributorName.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OS.openWebBrowser(e.getURL());
                }
            });
            panel.add(contributorName);
            authorsList.add(panel);
        }

        SwingUtilities.invokeLater(() -> {
            if (contributorsScrollPane != null) {
                contributorsScrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }
}
