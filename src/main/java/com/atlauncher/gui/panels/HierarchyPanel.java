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
