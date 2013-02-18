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

import org.xnio.Option;

/**
 *
 * @author Eduardo Martins
 *
 */
public final class HttpOptions {

    private HttpOptions() {
    }

    /**
     *
     */
    public static final Option<Boolean> HTTPS = Option.simple(HttpOptions.class, "HTTPS", Boolean.class);

    /**
     *
     */
    public static final Option<String> SERVLET_NAME = Option.simple(HttpOptions.class, "SERVLET_NAME", String.class);
    public static final String DEFAULT_SERVLET_NAME = "ejb3-remote";

    /**
     *
     */
    public static final Option<String> HTTP_CLIENT_FACTORY_CLASS_NAME = Option.simple(HttpOptions.class, "HTTP_CLIENT_FACTORY_CLASS_NAME", String.class);

    /**
     * FIXME temp way to have ejb id into the http ejb receiver
     */
    public static final Option<String> APP_NAME = Option.simple(HttpOptions.class, "APP_NAME", String.class);
    public static final Option<String> MODULE_NAME = Option.simple(HttpOptions.class, "MODULE_NAME", String.class);
    public static final Option<String> DISTINCT_NAME = Option.simple(HttpOptions.class, "DISTINCT_NAME", String.class);

}
