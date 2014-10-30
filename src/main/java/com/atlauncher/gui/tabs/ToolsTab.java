/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import com.atlauncher.data.Language;
import com.atlauncher.gui.components.BlankToolPanel;
import com.atlauncher.gui.components.NetworkCheckerToolPanel;
import com.atlauncher.gui.components.ServerCheckerToolPanel;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

@SuppressWarnings("serial")
public class ToolsTab extends JPanel implements Tab {
    private JPanel mainPanel;

    public ToolsTab() {
        setLayout(new BorderLayout());

        mainPanel = new JPanel();

        mainPanel.setLayout(new GridLayout(3, 2));

        mainPanel.add(new NetworkCheckerToolPanel());
        mainPanel.add(new ServerCheckerToolPanel());
        mainPanel.add(new BlankToolPanel());
        mainPanel.add(new BlankToolPanel());
        mainPanel.add(new BlankToolPanel());
        mainPanel.add(new BlankToolPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.tools");
    }
}