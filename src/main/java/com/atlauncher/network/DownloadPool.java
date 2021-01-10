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
package com.atlauncher.network;

import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.FileUtils;

@SuppressWarnings("serial")
public final class DownloadPool extends LinkedList<Download> {
    private final boolean wait;

    public DownloadPool(boolean wait) {
        this.wait = wait;
    }

    public DownloadPool() {
        this(true);
    }

    public void downloadAll() {
        for (Download dl : this) {
            if (!Files.isDirectory(dl.to.getParent())) {
                FileUtils.createDirectory(dl.to.getParent());
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(App.settings.concurrentConnections);
        synchronized (this) {
            for (Download dl : this) {
                executor.execute(new Downloader(dl));
            }
        }
        executor.shutdown();
        if (this.wait) {
            while (!executor.isTerminated()) {
            }
        }
    }

    public long totalSize() {
        long size = 0;
        synchronized (this) {
            for (Download dl : this) {
                if (dl.needToDownload()) {
                    size += dl.size;
                }
            }
        }
        return size;
    }

    public DownloadPool downsize() {
        final DownloadPool pool = new DownloadPool(this.wait);
        final List<Download> downloads = this.stream().distinct().collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(App.settings.concurrentConnections);
        for (final Download dl : downloads) {
            executor.submit(() -> {
                if (dl.needToDownload()) {
                    synchronized (pool) {
                        pool.add(dl);
                    }
                } else {
                    dl.copy();
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        return pool;
    }

    public boolean any() {
        synchronized (this) {
            for (Download dl : this) {
                if (dl.needToDownload()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static final class Downloader implements Runnable {
        private final Download dl;

        private Downloader(Download dl) {
            this.dl = dl;
        }

        @Override
        public void run() {
            try {
                if (this.dl.needToDownload()) {
                    this.dl.downloadFile();
                } else {
                    this.dl.copy();
                }
            } catch (Exception e) {
                LogManager.logStackTrace("Error trying to download " + this.dl.to.getFileName(), e);
            }
        }
    }
}
