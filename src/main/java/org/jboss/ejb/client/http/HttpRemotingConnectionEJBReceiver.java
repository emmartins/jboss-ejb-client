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

import org.jboss.ejb.client.EJBReceiverContext;
import org.jboss.ejb.client.NodeAffinity;
import org.jboss.ejb.client.StatefulEJBLocator;
import org.jboss.ejb.client.http.jdk.JDKHttpClientFactory;
import org.jboss.ejb.client.remoting.ChannelAssociation;
import org.jboss.ejb.client.remoting.RemotingConnectionEJBReceiver;
import org.xnio.OptionMap;

/**
 *
 * @author Eduardo Martins
 *
 */
public class HttpRemotingConnectionEJBReceiver extends RemotingConnectionEJBReceiver {

    private final HttpClientFactory clientFactory;

    private String cookie;

    public HttpRemotingConnectionEJBReceiver(final String url, final OptionMap connectionCreationOptions) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(new HttpClientConnection(url), null, connectionCreationOptions);
        // parse and register module (TODO fetch from server, or perhaps http receiver should be considered as a special case that handles all beans)
        final String appName = connectionCreationOptions.get(HttpOptions.APP_NAME);
        final String moduleName = connectionCreationOptions.get(HttpOptions.MODULE_NAME);
        final String distinctName = connectionCreationOptions.get(HttpOptions.DISTINCT_NAME);
        registerModule(appName, moduleName, distinctName);
        // parse and setup http client
        final String httpClientFactoryClassName = connectionCreationOptions.get(HttpOptions.HTTP_CLIENT_FACTORY_CLASS_NAME);
        if (httpClientFactoryClassName == null) {
            clientFactory = new JDKHttpClientFactory();
        } else {
            Class<?> httpClientFactoryClass = Class.forName(httpClientFactoryClassName);
            clientFactory = (HttpClientFactory) httpClientFactoryClass.newInstance();
        }
    }

    @Override
    public void associate(EJBReceiverContext context) {
        // do nothing
    }

    @Override
    protected ChannelAssociation requireChannelAssociation(EJBReceiverContext ejbReceiverContext) {
       return new ChannelAssociation(this, ejbReceiverContext, new HttpClientChannel(this), this.clientProtocolVersion, this.marshallerFactory, null);
    }

    @Override
    protected <T> StatefulEJBLocator<T> openSession(EJBReceiverContext receiverContext, Class<T> viewType, String appName,
            String moduleName, String distinctName, String beanName) throws IllegalArgumentException {
        StatefulEJBLocator<T> locator = super.openSession(receiverContext, viewType, appName, moduleName, distinctName, beanName);
        // overwrite affinity, since server will send different node name
        return new StatefulEJBLocator<T>(viewType, appName, moduleName, beanName, distinctName, locator.getSessionId(), new NodeAffinity(getNodeName()), getNodeName());
    }

    HttpClient getClient() {
        return clientFactory.getClient();
    }

    String getURL() {
        return getNodeName();
    }

    String getClientMarshallingStrategy() {
        return super.clientMarshallingStrategy;
    }

    byte getClientProtocolVersion() {
        return super.clientProtocolVersion;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

}
