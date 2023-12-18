/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.github.philippefichet.sonarlint4netbeans.remote.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteConsumerProgressInputStream extends InputStream {
    private final Consumer<Long> responseReadByteConsumer;
    private long lengthRead = 0;
    private final InputStream inputStream;

    public SonarLintRemoteConsumerProgressInputStream(Consumer<Long> responseReadByteConsumer, InputStream inputStream) {
        this.responseReadByteConsumer = responseReadByteConsumer;
        this.inputStream = inputStream;
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream.transferTo(out);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        lengthRead = 0;
        inputStream.reset();
        responseReadByteConsumer.accept(lengthRead);
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        int readNBytes = inputStream.readNBytes(b, off, len);
        lengthRead += readNBytes;
        responseReadByteConsumer.accept(lengthRead);
        return readNBytes;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] readNBytes = inputStream.readNBytes(len);
        lengthRead += readNBytes.length;
        responseReadByteConsumer.accept(lengthRead);
        return readNBytes;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] readAllBytes = inputStream.readAllBytes();
        lengthRead += readAllBytes.length;
        responseReadByteConsumer.accept(lengthRead);
        return readAllBytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inputStream.read(b, off, len);
        lengthRead += read;
        responseReadByteConsumer.accept(lengthRead);
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inputStream.read(b);
        lengthRead += read;
        responseReadByteConsumer.accept(lengthRead);
        return read;
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        lengthRead += read;
        responseReadByteConsumer.accept(lengthRead);
        return read;
    }
}
