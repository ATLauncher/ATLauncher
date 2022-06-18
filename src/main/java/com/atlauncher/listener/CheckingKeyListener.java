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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 16 / 06 / 2022
 * <p>
 * Ensures that the typed data is correct
 * <p>
 * Note, use [check] to also save the text
 */
public class CheckingKeyListener extends Thread implements KeyListener {
    private static final Logger LOG = LogManager.getLogger();
    private final Function<Void, Boolean> check;
    private long lastType = 0;
    private boolean changed = false;
    private final Consumer<Void> invalid;
    private final Consumer<Boolean> isLoading;
    private final long checkDelay;

    /**
     * Constructor, sets delay to 1 second
     *
     * @param check   check if the typed text is valid, return result
     * @param invalid invoked when the check fails
     */
    public CheckingKeyListener(
        Function<Void, Boolean> check,
        Consumer<Void> invalid
    ) {
        this(
            1000,
            check,
            invalid,
            ignored -> {
            }
        );
    }

    /**
     * Constructor, sets delay to 1 second
     *
     * @param check   check if the typed text is valid, return result
     * @param invalid invoked when the check fails
     */
    public CheckingKeyListener(
        Function<Void, Boolean> check,
        Consumer<Void> invalid,
        Consumer<Boolean> isLoading
    ) {
        this(
            1000,
            check,
            invalid,
            isLoading
        );
    }

    /**
     * Constructor
     *
     * @param checkDelay time before checker checks
     * @param check      check if the typed text is valid, return result
     * @param invalid    invoked when the check fails
     */
    public CheckingKeyListener(
        long checkDelay,
        Function<Void, Boolean> check,
        Consumer<Void> invalid,
        Consumer<Boolean> isLoading
    ) {
        this.checkDelay = checkDelay;
        this.check = check;
        this.invalid = invalid;
        this.isLoading = isLoading;
        start();
    }

    @Override
    public void run() {
        LOG.debug("Running check thread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("Failed to delay check thread", e);
            } finally {
                if (changed && lastType + checkDelay < System.currentTimeMillis()) {
                    isLoading.accept(true);
                    boolean valid = check.apply(null);
                    isLoading.accept(false);
                    changed = false;

                    if (!valid) {
                        LOG.debug("Check thread reporting check fail");
                        SwingUtilities.invokeLater(() -> {
                            invalid.accept(null);

                        });
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
        lastType = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        changed = true;
    }
}
