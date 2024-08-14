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
package com.atlauncher.gui.components;

import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper of PreservingCaretTextSetter, allowing locking of setting.
 *
 * @since 2024 / 08 / 03
 */
public class LockingPreservingCaretTextSetter extends PreservingCaretTextSetter {
    private boolean isLocked = false;

    /**
     * @param component TextComponent to set text of.
     */
    public LockingPreservingCaretTextSetter(@NotNull JTextComponent component) {
        super(component);
    }

    @Override
    public void setText(@Nullable String newText) {
        if (!isLocked) // If locked, drop the input from view model.
            super.setText(newText);
    }

    /**
     * Set if the text setter is locked or not.
     * @param isLocked
     */
    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
}
