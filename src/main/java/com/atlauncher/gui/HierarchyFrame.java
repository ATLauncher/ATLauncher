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

import java.awt.event.HierarchyListener;

import javax.swing.JFrame;

import com.atlauncher.gui.panels.HierarchyPanel;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * This panel uses {@link HierarchyListener} to react the visibility changes.
 * By implementing this panel instead of {@link JFrame} one can lower background
 * memory usage and increase application boot times by delegating resource intensive tasks to runtime.
 *
 * @since 2023 / 11 / 08
 */
public abstract class HierarchyFrame extends JFrame implements HierarchyView {
    protected final HierarchyController<HierarchyFrame> controller;

    public HierarchyFrame() {
        super();
        addNotify();
        controller = new HierarchyController<>(this);
        addHierarchyListener(controller);
    }

    @Override
    public void addDisposable(Disposable disposable) {
        controller.addDisposable(disposable);
    }

    @Override
    public void invokeLater(Runnable runnable) {
        controller.invokeLater(runnable);
    }
}
