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

import com.atlauncher.constants.Constants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import org.jetbrains.annotations.NotNull;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;

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
    private final JPanel info;

    /**
     * Contained in [info], Displays to user various information on ATLauncher
     */
    private final JTextPane textInfo;

    /**
     * Copies [textInfo] to the users clipboard
     */
    private final JButton copyButton;

    private final JScrollPane authors;

    /**
     * Optimization method to ensure about tab opens with information as fast
     * as possible.
     *
     * @return information on this launcher
     */
    private static @NotNull String getInformation() {
        if (_information == null)
            _information = Constants.LAUNCHER_NAME + "\n" +
                "Version:\t" + Constants.VERSION.toString() + "\n" +
                "OS:\t" + System.getProperty("os.name") + "\n" +
                "Java:\t" +
                String.format("Java %d (%s)",
                    Java.getLauncherJavaVersionNumber(),
                    Java.getLauncherJavaVersion()
                );

        return _information;
    }

    public AboutTab() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        // Top info panel
        {
            info = new JPanel();
            info.setLayout(new BorderLayout());

            // Add text info
            {
                textInfo = new JTextPane();
                textInfo.setText(getInformation());
                textInfo.setEditable(false);
                info.add(textInfo, BorderLayout.WEST);
            }

            // Add copy button
            {
                copyButton = new JButton();
                copyButton.setText(GetText.tr("Copy"));
                copyButton.addActionListener(e -> {
                    OS.copyToClipboard(getInformation());
                });
                info.add(copyButton, BorderLayout.EAST);
            }

            // Add to layout
            {
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                this.add(info, constraints);
            }
        }

        // Contributors panel
        // TODO Add label "Authors" or something
        {
            // Create list
            JPanel authorsList = new JPanel();
            authorsList.setLayout(new FlowLayout());

            // Populate list
            for (String author : AUTHORS_ARRAY) {
                JTextPane pane = new JTextPane();
                pane.setText(author);
                authorsList.add(pane);
            }

            // Create scroll panel
            authors = new JScrollPane(authorsList);
            authors.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            Dimension size = authorsList.getMinimumSize();
            size.height = 40; // TODO Dynamic height
            authors.setMinimumSize(size);

            // Add to layout
            {
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                this.add(authors, constraints);
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


    private static String _information = null;

    /**
     * Produced via "git shortlog -s -n --all --no-merges" then some edits
     * <p>
     * Use the following pattern to retrieve icons
     * "https://avatars.githubusercontent.com/USERNAME"
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    private static final String[] AUTHORS_ARRAY = {
        "Ryan Dowling",
        "RyanTheAllmighty",
        "atlauncher-bot",
        "Asyncronous",
        "PORTB",
        "ATLauncher Bot",
        "doomsdayrs",
        "JakeJMattson",
        "Jamie (Lexteam)",
        "Ryan",
        "Jamie Mansfield",
        "flaw600",
        "s0cks",
        "Leah",
        "Alan Jenkins",
        "dgelessus",
        "Kihira",
        "Harald Krämer",
        "James Ross",
        "iarspider",
        "xz-dev",
        "Mysticpasta1",
        "Torsten Walluhn",
        "modmuss50",
        "Andrew Thurman",
        "Cassandra Caina",
        "Jamie (Lexware)",
        "Jowsey",
        "Shegorath123",
        "notfood",
        "Dallas Epperson",
        "Emma Waffle",
        "Hossam Mohsen",
        "Jamie",
        "Laceh",
        "Sasha Sorokin",
        "TecCheck",
        "Trejkaz",
        "mac",
    };

}
