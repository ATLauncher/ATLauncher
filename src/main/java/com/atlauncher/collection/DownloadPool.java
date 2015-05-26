/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.collection;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class DownloadPool extends LinkedList<Downloadable> {
    private final boolean wait;

    public DownloadPool(boolean wait) {
        this.wait = wait;
    }

    public DownloadPool() {
        this(true);
    }

    public void downloadAll() {
        ExecutorService executor = Utils.generateDownloadExecutor();
        for (Downloadable dl : this) {
            executor.execute(new Downloader(dl));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public void downloadAll(InstanceInstaller installer) {
        ExecutorService executor = Utils.generateDownloadExecutor();
        for (Downloadable dl : this) {
            executor.execute(new Installer(dl, installer));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public int totalSize() {
        Future<Integer> sizeFuture = App.TASKPOOL.submit(new SizeCollector());

        try {
            return sizeFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LogManager.logStackTrace(e);
            return 0;
        }
    }

    public DownloadPool downsize() {
        DownloadPool pool = new DownloadPool(wait);
        for (Downloadable dl : this) {
            if (dl.needToDownload()) {
                pool.add(dl);
            }
        }
        return pool;
    }

    public boolean any() {
        for (Downloadable dl : this) {
            if (dl.needToDownload()) {
                return true;
            }
        }

        return false;
    }

    private final class SizeCollector implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            int size = 0;
            for (Downloadable dl : DownloadPool.this) {
                size += dl.getFilesize();
            }
            return size;
        }
    }

    private final class Installer implements Runnable {
        private final Downloadable dl;
        private final InstanceInstaller installer;

        private Installer(Downloadable dl, InstanceInstaller installer) {
            this.dl = dl;
            this.installer = installer;
        }

        @Override
        public void run() {
            try {
                if (this.dl.needToDownload()) {
                    installer.fireTask(Language.INSTANCE.localize("common.downloading") + this.dl.to.getFileName());
                    this.dl.download();
                } else {
                    this.dl.copy();
                }
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    private final class Downloader implements Runnable {
        private final Downloadable dl;

        private Downloader(Downloadable dl) {
            this.dl = dl;
        }

        @Override
        public void run() {
            try {
                if (this.dl.needToDownload()) {
                    this.dl.download();
                }
            } catch (Exception e) {
                LogManager.logStackTrace("Error trying to download " + this.dl.to.getFileName(), e);
            }
        }
    }
}