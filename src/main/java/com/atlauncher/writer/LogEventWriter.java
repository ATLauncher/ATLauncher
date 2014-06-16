package com.atlauncher.writer;

import com.atlauncher.evnt.LogEvent;
import com.atlauncher.exceptions.ChunkyException;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public final class LogEventWriter implements Closeable, Flushable{
    private final Writer writer;

    public LogEventWriter(Writer writer){
        if(writer == null){
            throw new ChunkyException("Writer == null");
        }

        this.writer = writer;
    }

    public void write(LogEvent event)
    throws IOException{
        if(event == null){
            throw new ChunkyException("Event == null");
        } else{
            this.writer.write(event.toString());
        }
    }

    public void write(String comment)
    throws IOException{
        if(comment == null){
            throw new ChunkyException("Comment == null");
        } else{
            this.writer.write((!comment.startsWith("#") ? "#" + comment : comment));
        }
    }

    @Override
    public void flush()
    throws IOException {
        this.writer.flush();
    }

    @Override
    public void close()
    throws IOException {
        this.writer.close();
    }
}