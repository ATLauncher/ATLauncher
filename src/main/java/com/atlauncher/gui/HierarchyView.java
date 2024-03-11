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

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @since 2023 / 11 / 08
 */
public interface HierarchyView {

    /**
     * Create the view model.
     * <p>
     * This is invoked before the first invocation of [onShow].
     * <p>
     * This is important as view model instantiation is process intensive,
     * thus should occur on the fly just before the view is created for the first time.
     */
    void createViewModel();

    /**
     * Populate the UI, so the user can view it.
     * <p>
     * This is invoked when this panel is visible.
     */
    void onShow();

    /**
     * Destroy the UI.
     * <p>
     * This is invoked when this panel is no longer visible.
     */
    void onDestroy();

    /**
     * Add a disposable to be automatically cleared upon view destruction.
     */
    void addDisposable(Disposable disposable);

    /**
     * Override to SwingUtilities.invokeLater, providing view state awareness.
     *
     * @param runnable Action to run the event cycle while the view is still present.
     */
    void invokeLater(Runnable runnable);
}
