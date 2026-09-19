/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2026 ATLauncher
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
package com.atlauncher.gui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
public class ProgressDialogTest {
    private static final int TIMEOUT_SECONDS = 10;

    private final List<Thread> threads = new ArrayList<>();
    private JFrame owner;
    private ObservedProgressDialog dialog;

    @BeforeEach
    void createDialog() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required for modal dialog tests");
        onEdt(() -> {
            owner = new JFrame("Progress dialog regression test");
            owner.setSize(320, 160);
            owner.setVisible(true);
            dialog = new ObservedProgressDialog(owner);
            return null;
        });
    }

    @AfterEach
    void cleanUp() throws Exception {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        onEdt(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
            if (owner != null) {
                owner.dispose();
            }
            return null;
        });
        for (Thread thread : threads) {
            thread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            assertFalse(thread.isAlive(), "Test thread did not terminate");
        }
    }

    @Test
    void immediateCompletionBeforeShowingDoesNotLeaveAModalDialogOnEdt() throws Exception {
        immediateCompletionBeforeShowing(true);
    }

    @Test
    void immediateCompletionBeforeShowingDoesNotLeaveAModalDialogFromBackgroundThread() throws Exception {
        immediateCompletionBeforeShowing(false);
    }

    private void immediateCompletionBeforeShowing(boolean startOnEdt) throws Exception {
        CountDownLatch showRequested = new CountDownLatch(1);
        CountDownLatch closeReturned = new CountDownLatch(1);
        // Force completion between starting the worker and actually showing the native dialog.
        dialog.beforeShowing = () -> {
            showRequested.countDown();
            await(closeReturned);
        };
        FutureTask<Void> worker = addWorker(() -> {
            await(showRequested);
            dialog.setReturnValue("complete");
            dialog.close();
            closeReturned.countDown();
            return null;
        });

        FutureTask<String> start = startDialog(startOnEdt);

        assertEquals("complete", start.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        worker.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertClosedOnEdt();
    }

    @Test
    void backgroundStartWaitsForWorkerAndRetainsItsResult() throws Exception {
        CountDownLatch completeWorker = new CountDownLatch(1);
        FutureTask<Void> worker = addWorker(() -> {
            await(completeWorker);
            dialog.setReturnValue("complete");
            dialog.close();
            return null;
        });
        FutureTask<String> start = startDialog(false);
        await(dialog.opened);

        onEdt(() -> {
            assertTrue(dialog.isShowing());
            assertFalse(start.isDone(), "start() must block while its modal dialog is open");
            return null;
        });
        completeWorker.countDown();

        assertEquals("complete", start.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        worker.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertClosedOnEdt();
    }

    @Test
    void cancellingDialogInterruptsWorkerAndUnblocksStart() throws Exception {
        CountDownLatch workerStarted = new CountDownLatch(1);
        CountDownLatch waitForCancellation = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        FutureTask<Void> worker = addWorker(() -> {
            workerStarted.countDown();
            try {
                waitForCancellation.await();
            } catch (InterruptedException e) {
                interrupted.set(true);
                Thread.currentThread().interrupt();
            } finally {
                dialog.close();
            }
            return null;
        });
        FutureTask<String> start = startDialog(true);
        await(workerStarted);
        await(dialog.opened);

        onEdt(() -> {
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            return null;
        });

        start.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        worker.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(dialog.wasClosed);
        assertTrue(interrupted.get(), "Closing the dialog must interrupt its worker");
        assertClosedOnEdt();
    }

    private FutureTask<Void> addWorker(Callable<Void> action) {
        FutureTask<Void> worker = new FutureTask<>(action);
        Thread thread = newThread(worker);
        dialog.addThread(thread);
        return worker;
    }

    private FutureTask<String> startDialog(boolean onEdt) {
        FutureTask<String> start = new FutureTask<>(() -> {
            dialog.start();
            return dialog.getReturnValue();
        });
        if (onEdt) {
            SwingUtilities.invokeLater(start);
        } else {
            newThread(start).start();
        }
        return start;
    }

    private Thread newThread(Runnable action) {
        Thread thread = new Thread(action, "progress-dialog-test");
        thread.setDaemon(true);
        threads.add(thread);
        return thread;
    }

    private void assertClosedOnEdt() throws Exception {
        onEdt(() -> {
            assertFalse(dialog.isVisible());
            assertFalse(dialog.isDisplayable());
            assertTrue(dialog.disposed);
            assertFalse(dialog.visibilityChangedOffEdt, "Visibility must only change on the EDT");
            assertFalse(dialog.disposedOffEdt, "Disposal must only happen on the EDT");
            return null;
        });
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeLater(task);
        return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Timed out waiting for dialog lifecycle");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for dialog lifecycle", e);
        }
    }

    private static class ObservedProgressDialog extends ProgressDialog<String> {
        private final CountDownLatch opened = new CountDownLatch(1);
        private Runnable beforeShowing;
        private volatile boolean visibilityChangedOffEdt;
        private volatile boolean disposedOffEdt;
        private volatile boolean disposed;

        ObservedProgressDialog(Window owner) {
            super("Progress dialog regression test", false, owner);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent event) {
                    opened.countDown();
                }
            });
        }

        @Override
        public void setVisible(boolean visible) {
            if (!SwingUtilities.isEventDispatchThread()) {
                visibilityChangedOffEdt = true;
            }
            if (visible && beforeShowing != null) {
                beforeShowing.run();
            }
            super.setVisible(visible);
        }

        @Override
        public void dispose() {
            if (!SwingUtilities.isEventDispatchThread()) {
                disposedOffEdt = true;
            }
            disposed = true;
            super.dispose();
        }
    }
}
