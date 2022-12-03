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
package com.atlauncher.gui.tabs.instances;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.network.Analytics;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public final class InstancesSearchField extends JTextField implements KeyListener {
    private final InstancesTab parent;
    private final IInstancesTabViewModel viewModel;

    public InstancesSearchField(final InstancesTab parent,final IInstancesTabViewModel viewModel) {
        super(16);
        this.parent = parent;
        this.viewModel = viewModel;

        this.setMaximumSize(new Dimension(190, 23));
        this.addKeyListener(this);
        this.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        this.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        this.putClientProperty("JTextField.showClearButton", true);
        this.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            setText("");
            viewModel.setSearch(null);
        });
    }

    public Pattern getSearchPattern() {
        return Pattern.compile(Pattern.quote(this.getText()), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            Analytics.sendEvent(this.getText(), "Search", "Instance");
            this.viewModel.setSearch(this.getSearchPattern());
        }
    }
}
