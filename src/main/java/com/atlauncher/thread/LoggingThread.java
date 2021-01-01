/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.thread;

import java.util.concurrent.BlockingQueue;

import com.atlauncher.evnt.LogEvent;

import org.apache.logging.log4j.Logger;

public final class LoggingThread extends Thread {
    private final BlockingQueue<LogEvent> queue;
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(LoggingThread.class);

    public LoggingThread(BlockingQueue<LogEvent> queue) {
        this.queue = queue;
        this.setName("ATL-Logging-Thread");
    }

    @Override
    public void run() {
        while (true) {
            LogEvent next;
            try {
                next = this.queue.take();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
            if (next != null) {
                next.post(logger);
            }
        }
    }

}
