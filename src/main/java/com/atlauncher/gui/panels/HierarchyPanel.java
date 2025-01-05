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

import java.awt.LayoutManager;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 24 / 06 / 2022
 * <p>
 * This panel uses {@link HierarchyListener} to react the visibility changes.
 * By implementing this panel instead of {@link JPanel} one can lower background
 * memory usage and increase application boot times by delegating resource intensive tasks to runtime.
 * <p>
 * If child class subscribes to Observables, use addDisposable to manage subscriptions.
 */
public abstract class HierarchyPanel extends JPanel implements HierarchyListener {
    private final CompositeDisposable disposablePool = new CompositeDisposable();
    /**
     * Used to keep track of the view model lifecycle.
     */
    private boolean isViewModelCreated = false;

    /**
     * Used to keep track of view state.
     * <p>
     * We do not use isShowing because that returns true even if the view is not fully created yet.
     */
    private boolean isViewCreated = false;

    public HierarchyPanel(LayoutManager layout) {
        super(layout);
        addNotify();
        addHierarchyListener(this);

        // If the child is a child of RelocalizationListener
        // We can handle relocalization for them
        if (this instanceof RelocalizationListener) {
            RelocalizationManager.addListener(this::tryReLocalization);
        }
    }

    /**
     * Will only re-localize the view if it is showing.
     */
    private void tryReLocalization() {
        if (isShowing()) {
            ((RelocalizationListener) this).onRelocalization();
        }
    }

    /**
     * Add a disposable to be automatically cleared upon view destruction.
     */
    public void addDisposable(Disposable disposable) {
        disposablePool.add(disposable);
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        final String className = getClass().getSimpleName();

        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            if (isShowing()) {
                if (!isViewModelCreated) {
                    LogManager.debug("Creating view-model for: " + className);
                    PerformanceManager.start(className + ":ViewModel:Create");
                    createViewModel();
                    isViewModelCreated = true;
                    PerformanceManager.end(className + ":ViewModel:Create");
                }
                LogManager.debug("Showing UI for: " + className);
                PerformanceManager.start(className + ":View:Create");
                onShow();
                // Mark view as created for invokeLater to work properly
                isViewCreated = true;
                PerformanceManager.end(className + ":View:Create");
            } else {
                // A little trick here. We can guess the UI has not been created yet if the view model hasn't.
                // Thus, no need to destroy.
                if (!isViewModelCreated) return;
                LogManager.debug("Destroying UI for: " + className);
                PerformanceManager.start(className + ":View:Destroy");
                // Stop processes
                disposablePool.clear();
                // Destroy layer so the UI can hurry on
                onDestroy();
                isViewCreated = false;
                System.gc(); // Run GC to clear out any now stale data
                PerformanceManager.end(className + ":View:Destroy");
            }
        }
    }

    /**
     * Create the view model.
     * <p>
     * This is invoked before the first invocation of [onShow].
     * <p>
     * This is important as view model instantiation is process intensive,
     * thus should occur on the fly just before the view is created for the first time.
     */
    protected abstract void createViewModel();

    /**
     * Populate the UI, so the user can view it.
     * <p>
     * This is invoked when this panel is visible.
     */
    protected abstract void onShow();

    /**
     * Destroy the UI.
     * <p>
     * This is invoked when this panel is no longer visible.
     */
    protected abstract void onDestroy();

    /**
     * Override to SwingUtilities.invokeLater, providing view state awareness.
     *
     * @param runnable Action to run the event cycle while the view is still present.
     */
    protected void invokeLater(Runnable runnable) {
        SwingUtilities.invokeLater(() -> {
            if (isViewCreated) {
                runnable.run();
            }
        });
        ;
    }
}
