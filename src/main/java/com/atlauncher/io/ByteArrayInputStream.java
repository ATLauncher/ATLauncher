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
import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {
    protected byte[] buf;
    protected int pos;
    protected int mark = 0;
    protected int count;

    public ByteArrayInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public ByteArrayInputStream(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    public int read() {
        return this.pos < this.count ? this.buf[this.pos++] & 255 : -1;
    }

    public int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off >= 0 && len >= 0 && len <= b.length - off) {
            if (this.pos >= this.count) {
                return -1;
            } else {
                int avail = this.count - this.pos;
                if (len > avail) {
                    len = avail;
                }

                if (len <= 0) {
                    return 0;
                } else {
                    System.arraycopy(this.buf, this.pos, b, off, len);
                    this.pos += len;
                    return len;
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public long skip(long n) {
        long k = (long) (this.count - this.pos);
        if (n < k) {
            k = n < 0L ? 0L : n;
        }

        this.pos = (int) ((long) this.pos + k);
        return k;
    }

    public int available() {
        return this.count - this.pos;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        this.mark = this.pos;
    }

    public void reset() {
        this.pos = this.mark;
    }

    public void close() throws IOException {
    }
}
