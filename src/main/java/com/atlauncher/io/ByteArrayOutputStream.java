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
package com.atlauncher.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ByteArrayOutputStream extends OutputStream {
    protected byte[] buf;
    protected int count;

    public ByteArrayOutputStream() {
        this(32);
    }

    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        } else {
            this.buf = new byte[size];
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - this.buf.length > 0) {
            this.grow(minCapacity);
        }

    }

    private void grow(int minCapacity) {
        int oldCapacity = this.buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }

        if (newCapacity < 0) {
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }

            newCapacity = 2147483647;
        }

        this.buf = Arrays.copyOf(this.buf, newCapacity);
    }

    @Override
    public void write(int b) {
        this.ensureCapacity(this.count + 1);
        this.buf[this.count] = (byte) b;
        ++this.count;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off >= 0 && off <= b.length && len >= 0 && off + len - b.length <= 0) {
            this.ensureCapacity(this.count + len);
            System.arraycopy(b, off, this.buf, this.count, len);
            this.count += len;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.buf, this.count);
    }

    public int size() {
        return this.count;
    }

    @Override
    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    public String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(this.buf, 0, this.count, charsetName);
    }

    @Override
    public void close() throws IOException {
    }
}
