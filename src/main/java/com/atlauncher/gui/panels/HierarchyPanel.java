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
package com.atlauncher.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * 24 / 06 / 2022
 * <p>
 * This panel uses {@link HierarchyListener} to react the visibility changes.
 * By implementing this panel instead of {@link JPanel} one can lower background
 * memory usage.
 */
public abstract class HierarchyPanel extends JPanel implements HierarchyListener {
    public HierarchyPanel(LayoutManager layout) {
        super(layout);
        addNotify();
        addHierarchyListener(this);
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            if (isShowing()) {
                onShow();
            } else {
                onHide();
                System.gc(); // Run GC to clear out any now stale data
            }
        }
    }

    /**
     * Render the UI
     */
    protected abstract void onShow();

    /**
     * Destroy the UI
     */
    protected abstract void onHide();
}
