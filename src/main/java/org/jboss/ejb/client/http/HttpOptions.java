package org.jboss.ejb.client.http;

import org.xnio.Option;

public final class HttpOptions {

    private HttpOptions() {
    }

    public static enum HTTP_CLIENT_TYPE {
        jdk, apache, apacheasync
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
     *  TODO use a provider/factory class instead
     */
    public static final Option<HTTP_CLIENT_TYPE> HTTP_CLIENT = Option.simple(HttpOptions.class, "HTTP_CLIENT", HTTP_CLIENT_TYPE.class);

    /**
     * FIXME temp way to have ejb id into the http ejb receiver
     */
    public static final Option<String> APP_NAME = Option.simple(HttpOptions.class, "APP_NAME", String.class);
    public static final Option<String> MODULE_NAME = Option.simple(HttpOptions.class, "MODULE_NAME", String.class);
    public static final Option<String> DISTINCT_NAME = Option.simple(HttpOptions.class, "DISTINCT_NAME", String.class);

}
