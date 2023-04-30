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
package com.atlauncher.listener;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * 29 / 04 / 2023
 * <p>
 * A KeyAdapter for views that work with state based view models.
 */
public class StatefulTextKeyAdapter extends KeyAdapter {

    /**
     * Consumer to feed events
     */
    @Nonnull
    private final Consumer<KeyEvent> consumer;

    /**
     * @param consumer Consumer to receive events with
     */
    public StatefulTextKeyAdapter(@Nonnull Consumer<KeyEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void keyReleased(KeyEvent e) {

        // Ignore if the event is null
        // Ignore if it is an action key (arrows)
        // Ignore if it is a shift key (for shift selection)
        // Ignore if it is a control key (for ctrl commands)
        // Ignore if it is an alt key
        // Ignore if the modifiers has control down (for ctrl commands)
        if (e != null &&
            !e.isActionKey() &&
            e.getKeyCode() != KeyEvent.VK_SHIFT &&
            e.getKeyCode() != KeyEvent.VK_CONTROL &&
            e.getKeyCode() != KeyEvent.VK_ALT &&
            e.getModifiersEx() != KeyEvent.CTRL_DOWN_MASK
        ) {
            consumer.accept(e);
        } else super.keyReleased(e);
    }
}
