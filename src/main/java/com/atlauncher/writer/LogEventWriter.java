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
package com.atlauncher.writer;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import com.atlauncher.evnt.LogEvent;
import com.atlauncher.exceptions.ChunkyException;

public final class LogEventWriter implements Closeable, Flushable {
    private final Writer writer;

    public LogEventWriter(Writer writer) {
        if (writer == null) {
            throw new ChunkyException("Writer == null");
        }

        this.writer = writer;
    }

    public void write(LogEvent event) throws IOException {
        if (event == null) {
            throw new ChunkyException("Event == null");
        } else {
            this.writer.write(event.toString());
        }
    }

    public void write(String comment) throws IOException {
        if (comment == null) {
            throw new ChunkyException("Comment == null");
        } else {
            this.writer.write((!comment.startsWith("#") ? "#" + comment : comment));
        }
    }

    @Override
    public void flush() throws IOException {
        this.writer.flush();
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}