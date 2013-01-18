/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xnio.ducts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import org.xnio.Option;
import org.xnio.XnioExecutor;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSinkChannel;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class EmptyStreamSourceDuct implements StreamSourceDuct {
    private final XnioWorker worker;
    private final XnioExecutor readThread;
    private ReadReadyHandler readReadyHandler;
    private boolean shutdown;
    private boolean resumed;

    public EmptyStreamSourceDuct(final XnioWorker worker, final XnioExecutor readThread) {
        this.worker = worker;
        this.readThread = readThread;
    }

    public void setReadReadyHandler(final ReadReadyHandler handler) {
        readReadyHandler = handler;
    }

    public long transferTo(final long position, final long count, final FileChannel target) throws IOException {
        return 0;
    }

    public long transferTo(final long count, final ByteBuffer throughBuffer, final StreamSinkChannel target) throws IOException {
        resumed = false;
        return -1L;
    }

    public int read(final ByteBuffer dst) throws IOException {
        resumed = false;
        return -1;
    }

    public long read(final ByteBuffer[] dsts, final int offs, final int len) throws IOException {
        resumed = false;
        return -1L;
    }

    public boolean isReadShutdown() {
        return shutdown;
    }

    public void resumeReads() {
        resumed = true;
        readThread.execute(new Runnable() {
            public void run() {
                final ReadReadyHandler handler = readReadyHandler;
                if (handler != null) {
                    handler.readReady();
                }
            }
        });
    }

    public void suspendReads() {
        resumed = false;
    }

    public void wakeupReads() {
        resumeReads();
    }

    public boolean isReadResumed() {
        return resumed;
    }

    public void awaitReadable() throws IOException {
        // always ready
    }

    public void awaitReadable(final long time, final TimeUnit timeUnit) throws IOException {
        // always ready
    }

    public void terminateReads() throws IOException {
        if (! shutdown) {
            shutdown = true;
            readReadyHandler.terminated();
        }
    }

    public XnioExecutor getReadThread() {
        return readThread;
    }

    public XnioWorker getWorker() {
        return worker;
    }

    public boolean supportsOption(final Option<?> option) {
        return false;
    }

    public <T> T getOption(final Option<T> option) throws IOException {
        return null;
    }

    public <T> T setOption(final Option<T> option, final T value) throws IllegalArgumentException, IOException {
        return null;
    }
}