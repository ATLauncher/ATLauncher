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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.JTextComponent;

/**
 * A single use class that's job is to preserve the caret position of a text component while setting text.
 * <p>
 * This is useful for state based classes using view models reacting to view model state changes during user input.
 *
 * @since 27 / 04 / 2023
 */
public class PreservingCaretTextSetter {

    /**
     * Text component to work with.
     */
    @Nonnull
    private final JTextComponent component;

    /**
     * @param component TextComponent to set text of.
     */
    public PreservingCaretTextSetter(@Nonnull JTextComponent component) {
        this.component = component;
    }

    /**
     * Set the text of the component preserving caret position.
     *
     * @param newText New text to set
     */
    public void setText(@Nullable String newText) {
        // Preserve caret
        int oldCaret = component.getCaretPosition();

        // Preserve old text, do not calculate length
        String oldText = component.getText();

        // Set text as soon as possible
        component.setText(newText);

        // Ignore if new text is null
        // Ignore if oldCaret is further along then new text length
        if (newText != null && oldCaret < newText.length()) {
            component.setCaretPosition(oldCaret);
        }

        // Nothing to preserve
    }
}
