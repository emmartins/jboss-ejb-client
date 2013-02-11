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
package org.jboss.ejb.client.http.apache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.ejb.client.http.HttpRequest;
import org.jboss.ejb.client.http.HttpResponse;

/**
 *
 * @author Eduardo Martins
 *
 */
public class ApacheHttpRequest implements HttpRequest {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final HttpPost request;
    private final DefaultHttpClient client;

    public ApacheHttpRequest(String url, String cookie, DefaultHttpClient client) {
        this.client = client;
        this.request = new HttpPost(url);
        this.request.setHeader("Content-Type", "application/octet-stream");
        if (cookie != null) {
            this.request.setHeader("Cookie", cookie);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return baos;
    }

    @Override
    public HttpResponse send() throws IOException {
        request.setEntity(new ByteArrayEntity(baos.toByteArray()));
        return new ApacheHttpResponse(client.execute(request));
    }

}
