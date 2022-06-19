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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;

/**
 * 19 / 06 / 2022
 * <p>
 * This is a key listener that delays the saving of text to a view model.
 * This ensures that the user can type in text and not have their cursor
 * reset to the end.
 */
public class DelayedSavingKeyListener extends Thread implements KeyListener {
    private final long saveDelay;
    private boolean changed = false;
    private long lastSave = 0;
    private final Runnable onSave;
    private final Runnable onChangePending;

    /**
     * Public constructor for DelayedSavingKeyListener
     *
     * @param saveDelay       how much time till the onSave should be invoked
     * @param onSave          called to save the text
     * @param onChangePending called when a change is pending
     */
    public DelayedSavingKeyListener(
        long saveDelay,
        Runnable onSave,
        Runnable onChangePending
    ) {
        this.onChangePending = onChangePending;
        assert saveDelay > 100;
        this.saveDelay = saveDelay;
        this.onSave = onSave;
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(saveDelay);
            } catch (InterruptedException ignored) {
            } finally {
                if (changed) {
                    onChangePending.run();
                    if ((lastSave + saveDelay) < System.currentTimeMillis()) {
                        changed = false;
                        onSave.run();
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        lastSave = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (!keyEvent.isActionKey()) changed = true;
    }
}
