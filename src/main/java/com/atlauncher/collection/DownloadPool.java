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

public final class DownloadPool
extends LinkedList<Downloadable>{
    private final boolean wait;

    public DownloadPool(boolean wait) {this.wait = wait;}

    public DownloadPool(){
        this(true);
    }

    public void downloadAll(){
        ExecutorService executor = Utils.generateDownloadExecutor();
        for(Downloadable dl : this){
            executor.execute(new Downloader(dl));
        }
        executor.shutdown();
        if(this.wait){
            while(!executor.isTerminated()){}
        }
    }

    public void downloadAll(InstanceInstaller installer){
        ExecutorService executor = Utils.generateDownloadExecutor();
        for(Downloadable dl : this){
            executor.execute(new Installer(dl, installer));
        }
        executor.shutdown();
        if(this.wait){
            while(!executor.isTerminated()){}
        }
    }

    public int totalSize(){
        Future<Integer> sizeFuture = App.TASKPOOL.submit(new SizeCollector());

        try {
            return sizeFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LogManager.logStackTrace(e);
            return 0;
        }
    }

    public DownloadPool downsize(){
        Future<DownloadPool> poolFuture = App.TASKPOOL.submit(new Downsizer());

        try{
            return poolFuture.get();
        } catch(Exception e){
            LogManager.logStackTrace(e);
            return new DownloadPool(this.wait);
        }
    }

    public boolean any(){
        for(Downloadable dl : this){
            if(dl.needToDownload()){
                return true;
            }
        }

        return false;
    }

    private final class Downsizer
    implements Callable<DownloadPool>{
        @Override
        public DownloadPool call()
        throws Exception {
            DownloadPool pool = new DownloadPool(DownloadPool.this.wait);
            for(Downloadable dl : DownloadPool.this){
                if(dl.needToDownload()){
                    pool.add(dl);
                }
            }
            return pool;
        }
    }

    private final class SizeCollector
    implements Callable<Integer>{
        @Override
        public Integer call()
        throws Exception {
            int size = 0;
            for(Downloadable dl : DownloadPool.this){
                size += dl.getFilesize();
            }
            return size;
        }
    }

    private final class Installer
    implements Runnable{
        private final Downloadable dl;
        private final InstanceInstaller installer;

        private Installer(Downloadable dl, InstanceInstaller installer) {
            this.dl = dl;
            this.installer = installer;
        }

        @Override
        public void run() {
            try{
                if(this.dl.needToDownload()){
                    installer.fireTask(Language.INSTANCE.localize("common.downloading") + this.dl.to.getFileName());
                    this.dl.download();
                } else{
                    this.dl.copy();
                }
            } catch(Exception e){
                LogManager.logStackTrace(e);
            }
        }
    }

    private final class Downloader
    implements Runnable{
        private final Downloadable dl;

        private Downloader(Downloadable dl){
            this.dl = dl;
        }

        @Override
        public void run() {
            try{
                if(this.dl.needToDownload()){
                    this.dl.download();
                }
            } catch(Exception e){
                LogManager.logStackTrace("Error trying to download " + this.dl.to.getFileName(), e);
            }
        }
    }
}