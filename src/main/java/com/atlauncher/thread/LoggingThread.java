package com.atlauncher.thread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import com.atlauncher.App;
import com.atlauncher.evnt.LogEvent;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.writer.LogEventWriter;

public final class LoggingThread extends Thread {
    private final LogEventWriter writer;
    private final BlockingQueue<LogEvent> queue;

    public LoggingThread(BlockingQueue<LogEvent> queue) {
        this.queue = queue;
        this.setName("ATL-Logging-Thread");
        try {
            this.writer = new LogEventWriter(new FileWriter(new File(App.settings.getBaseDir(), "ATLauncher-Log-1.txt")));
            this.writer.write("Generated on " + Timestamper.now() + "\n");
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create LogEventWriter");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                LogEvent next = this.queue.take();
                if (next != null) {
                    next.post(this.writer);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

}