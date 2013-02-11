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
