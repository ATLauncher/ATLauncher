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
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * This is a key listener that delays the saving of text to a view model.
 * This ensures that the user can type in text and not have their cursor
 * reset to the end.
 */
public class DelayedSavingKeyListener extends StatefulTextKeyAdapter implements Runnable {
    private final long saveDelay;
    private final Runnable onSave;
    private final Runnable onChangePending;
    private Thread thread;
    private boolean changed = false;
    private long lastSave = 0;

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
        super();
        this.onChangePending = onChangePending;
        assert saveDelay > 100;
        this.saveDelay = saveDelay;
        this.onSave = onSave;
        setConsumer(this::onStateConsumer);

        // Start check thread
        startThread();
    }

    private void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    public void onStateConsumer(KeyEvent keyEvent) {
        if (!keyEvent.isActionKey()) {
            changed = true;

            if (!thread.isAlive()) {
                startThread();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            // Attempt to sleep
            try {
                TimeUnit.MILLISECONDS.sleep(saveDelay);
            } catch (InterruptedException ignored) {
                // ignored
            }

            // Check if the contents have changed
            if (changed) {
                // Notify that a change is in progress
                onChangePending.run();

                // Check if enough time has passed since the last change
                if ((lastSave + saveDelay) < System.currentTimeMillis()) {
                    // Time has passed, we can assume things are done
                    changed = false;
                    onSave.run(); // save the contents
                }
            } else if (lastSave + (saveDelay * 10) < System.currentTimeMillis()) {
                // it has been 10 times the saveDelay since last change, we can cease this thread to save resources.
                break;
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
}