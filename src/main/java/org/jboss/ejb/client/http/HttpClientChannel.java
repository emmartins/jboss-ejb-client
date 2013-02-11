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

import java.io.DataOutputStream;
import java.io.IOException;

import org.jboss.remoting3.Attachments;
import org.jboss.remoting3.Channel;
import org.jboss.remoting3.CloseHandler;
import org.jboss.remoting3.Connection;
import org.jboss.remoting3.MessageOutputStream;
import org.xnio.Option;

/**
 *
 * @author Eduardo Martins
 *
 */
public class HttpClientChannel implements Channel {

    private final HttpRemotingConnectionEJBReceiver receiver;
    private HttpRequest request;
    private Receiver handler;

    public HttpClientChannel(HttpRemotingConnectionEJBReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Attachments getAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void awaitClosed() throws InterruptedException {
        // TODO Auto-generated method stub

    }

    @Override
    public void awaitClosedUninterruptibly() {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeAsync() {
        // TODO Auto-generated method stub

    }

    @Override
    public org.jboss.remoting3.HandleableCloseable.Key addCloseHandler(CloseHandler<? super Channel> handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection getConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageOutputStream writeMessage() throws IOException {
        request = receiver.getClient().getRequest(receiver.getURL(), receiver.getCookie());
        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        output.writeByte(receiver.getClientProtocolVersion());
        output.writeUTF(receiver.getClientMarshallingStrategy());
        return new HttpClientMessageOutputStream(output,this);
    }

    @Override
    public void writeShutdown() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveMessage(Receiver handler) {
        this.handler = handler;
    }

    @Override
    public boolean supportsOption(Option<?> option) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T getOption(Option<T> option) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    public void outputStreamClosed() throws IOException {
        // send request
        HttpResponse response = request.send();
        // update cookie
        final String cookie = response.getCookie();
        if (cookie != null) {
            receiver.setCookie(cookie);
        }
        // handle response
        handler.handleMessage(this, new HttpClientMessageInputStream(response.getInputStream()));
    }

}
