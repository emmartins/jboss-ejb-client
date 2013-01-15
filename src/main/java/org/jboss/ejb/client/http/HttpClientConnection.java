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
import java.security.Principal;
import java.util.Collection;

import org.jboss.remoting3.Attachments;
import org.jboss.remoting3.Channel;
import org.jboss.remoting3.CloseHandler;
import org.jboss.remoting3.Connection;
import org.jboss.remoting3.Endpoint;
import org.jboss.remoting3.security.UserInfo;
import org.xnio.IoFuture;
import org.xnio.OptionMap;

/**
 *
 * @author Eduardo Martins
 *
 */
public class HttpClientConnection implements Connection {

    final String url;

    public HttpClientConnection(String url) {
        this.url = url;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

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
    public org.jboss.remoting3.HandleableCloseable.Key addCloseHandler(CloseHandler<? super Connection> handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attachments getAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Principal> getPrincipals() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserInfo getUserInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IoFuture<Channel> openChannel(String serviceType, OptionMap optionMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteEndpointName() {
        return url;
    }

    @Override
    public Endpoint getEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }

}
