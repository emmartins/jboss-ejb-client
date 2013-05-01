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
package org.jboss.ejb.client.http.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jboss.ejb.client.DefaultCallbackHandler;
import org.jboss.ejb.client.http.BasicHttpAuthentication;
import org.jboss.ejb.client.http.DigestHttpAuthentication;
import org.jboss.ejb.client.http.HttpAuthentication;
import org.jboss.ejb.client.http.HttpRemotingConnectionEJBReceiver;
import org.jboss.ejb.client.http.HttpRequest;
import org.jboss.ejb.client.http.HttpResponse;

/**
 *
 * @author Eduardo Martins
 *
 */
public class JDKHttpRequest implements HttpRequest {

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String WWW_AUTHENTICATE_HEADER_NAME = "WWW-Authenticate";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/octet-stream";
    private static final String COOKIE_HEADER_NAME = "Cookie";

    private final HttpRemotingConnectionEJBReceiver receiver;
    private HttpURLConnection connection;
    private final ByteArrayOutputStream cache;

    public JDKHttpRequest(HttpRemotingConnectionEJBReceiver receiver) throws IOException {
        if (receiver == null) {
            throw new NullPointerException("null receiver");
        }
        this.receiver = receiver;
        connection = (HttpURLConnection) new URL(receiver.getURL()).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
        final String cookie = receiver.getCookie();
        if (cookie != null) {
            // session established
            connection.setRequestProperty(COOKIE_HEADER_NAME, cookie);
        }
        if (receiver.getCallbackHandler() instanceof DefaultCallbackHandler) {
            // no authentication means available
            cache = null;
        } else {
            if (receiver.isPreemptiveBasicAuthentication()) {
                // send authorization right away and save traffic of handling server auth required response (at the expense of sending always the authorization)
                connection.setRequestProperty(AUTHORIZATION_HEADER_NAME, new BasicHttpAuthentication().getAuthorization(receiver.getCallbackHandler()));
                cache = null;
            } else {
                // no preemptive authorization, wait for server response to decide which auth to do, need to cache request content data in a byte array stream for possible request replay
                cache = new ByteArrayOutputStream();
            }
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return cache != null ? cache : connection.getOutputStream();
    }

    @Override
    public HttpResponse send() throws IOException {
        final byte[] bytes;
        if (cache != null) {
            bytes = cache.toByteArray();
            final OutputStream out = connection.getOutputStream();
            out.write(bytes);
            out.close();
        } else {
            bytes = null;
        }
        if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED && bytes != null) {
            // handle authentication
            final List<String> wwwAuthenticateHeaderStrings = connection.getHeaderFields().get(WWW_AUTHENTICATE_HEADER_NAME);
            if (wwwAuthenticateHeaderStrings == null) {
                throw new IOException("received 401 error response but no www authenticate headers");
            }
            HttpAuthentication authentication = null;
            for (String wwwAuthenticateHeaderString : wwwAuthenticateHeaderStrings) {
                WWWAuthenticateHeader header = WWWAuthenticateHeader.parse(wwwAuthenticateHeaderString);
                if (header.getType() == WWWAuthenticateHeader.Type.basic) {
                    authentication = new BasicHttpAuthentication();
                } else if (header.getType() == WWWAuthenticateHeader.Type.digest) {
                    authentication = new DigestHttpAuthentication(connection.getURL().getFile(), connection.getRequestMethod(),
                            header.getParams().get("realm"), header.getParams().get("nonce"), header.getParams().get("opaque"),
                            header.getParams().get("qop"), UUID.randomUUID().toString());
                    // no need to process others, digest is at top priority
                    break;
                }
            }
            if (authentication == null) {
                throw new IOException("no supported authentication methods found in server response");
            }
            final String authorization = authentication.getAuthorization(receiver.getCallbackHandler());
            // resend request
            connection = (HttpURLConnection) connection.getURL().openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
            connection.setRequestProperty(AUTHORIZATION_HEADER_NAME, authorization);
            final OutputStream out = connection.getOutputStream();
            out.write(bytes);
            out.close();
        }
        return new JDKHttpResponse(connection);
    }

}
