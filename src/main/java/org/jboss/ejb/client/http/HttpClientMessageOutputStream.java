/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb.client.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.remoting3.MessageOutputStream;

/**
 *
 * @author Eduardo Martins
 *
 */
public class HttpClientMessageOutputStream extends MessageOutputStream {

    private final OutputStream out;
    private final HttpClientChannel channel;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public HttpClientMessageOutputStream(final OutputStream outputStream, final HttpClientChannel channel) {
        this.out = outputStream;
        this.channel = channel;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        if(closed.compareAndSet(false, true)) {
            out.close();
            channel.outputStreamClosed();
        }
    }

    @Override
    public MessageOutputStream cancel() {
        try {
            out.close();
        } catch (IOException e) {
            // ignore
        }
        return this;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}
