/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.atlauncher.App;
import com.atlauncher.data.Addon;

public class AddonsPanel extends JPanel {

    private JPanel panel;
    private JScrollPane scrollPane;

    public AddonsPanel() {
        setLayout(new BorderLayout());
        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        if (App.settings.getAddons().size() == 0) {
            panel.add(new NothingToDisplay(App.settings.getLocalizedString("addon.nodisplay", "\n\n")), gbc);
        } else {
            for (Addon addon : App.settings.getAddons()) {
                panel.add(new AddonDisplay(addon), gbc);
                gbc.gridy++;
            }
        }
    }

}
