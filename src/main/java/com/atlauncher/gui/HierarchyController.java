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
package com.atlauncher.gui;

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.SwingUtilities;

import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Dictates behavior for any given {@link HierarchyView}s.
 *
 * @see HierarchyView
 * @since 2023 / 11 / 08
 */
public class HierarchyController<T extends Component & HierarchyView> implements HierarchyListener {
    private final CompositeDisposable disposablePool = new CompositeDisposable();

    /**
     * View to control.
     */
    private final T view;

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

    /**
     * @param view Given view to control.
     */
    public HierarchyController(T view) {
        this.view = view;

        // If the child is a child of RelocalizationListener
        // We can handle relocalization for them
        if (this instanceof RelocalizationListener) {
            RelocalizationManager.addListener(this::tryReLocalization);
        }
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        final String className = getClass().getSimpleName();

        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            if (view.isShowing()) {
                if (!isViewModelCreated) {
                    LogManager.debug("Creating view-model for: " + className);
                    PerformanceManager.start(className + ":ViewModel:Create");
                    view.createViewModel();
                    isViewModelCreated = true;
                    PerformanceManager.end(className + ":ViewModel:Create");
                }
                LogManager.debug("Showing UI for: " + className);
                PerformanceManager.start(className + ":View:Create");
                view.onShow();
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
                view.onDestroy();
                isViewCreated = false;
                System.gc(); // Run GC to clear out any now stale data
                PerformanceManager.end(className + ":View:Destroy");
            }
        }
    }

    /**
     * @see HierarchyView
     */
    public void addDisposable(Disposable disposable){
        disposablePool.add(disposable);
    }


    /**
     * @see HierarchyView
     */
    public void invokeLater(Runnable runnable) {
        SwingUtilities.invokeLater(() -> {
            if (isViewCreated) {
                runnable.run();
            }
        });
    }

    /**
     * Will only re-localize the view if it is showing.
     */
    private void tryReLocalization() {
        if (view.isShowing()) {
            ((RelocalizationListener) view).onRelocalization();
        }
    }
}
