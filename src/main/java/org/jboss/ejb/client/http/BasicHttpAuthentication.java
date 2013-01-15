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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 * @author Eduardo Martins
 *
 */
public class BasicHttpAuthentication implements HttpAuthentication {

    private static final String USER_PROMPT = "User:";
    private static final String PASSWORD_PROMPT = "Password:";
    private static final String BASIC_AUTHENTICATION_PREFIX = "Basic ";

    @Override
    public String getAuthorization(CallbackHandler callbackHandler) throws IOException {
        final NameCallback nameCallback = new NameCallback(USER_PROMPT);
        final PasswordCallback passwordCallback = new PasswordCallback(PASSWORD_PROMPT, false);
        try {
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
        } catch (UnsupportedCallbackException e) {
            throw new IOException(e);
        }
        return getAuthorization(nameCallback.getName(), passwordCallback.getPassword());
    }

    @Override
    public String getAuthorization(String userName, char[] password) throws IOException {
        // transform username into bytes
        if (userName == null) {
            throw new IOException("null authentication username");
        }
        byte[] userNameBytes = new StringBuilder(userName).append(':').toString().getBytes("ISO-8859-1");
        // transform password into bytes
        if (password == null) {
            throw new IOException("null authentication password");
        }
        byte[] passwordBytes = new byte[password.length];
        for (int i = 0; i < password.length; i++)
            passwordBytes[i] = (byte) password[i];
        // compute authentication
        byte[] bytes = new byte[userNameBytes.length + passwordBytes.length];
        System.arraycopy(userNameBytes, 0, bytes, 0, userNameBytes.length);
        System.arraycopy(passwordBytes, 0, bytes, userNameBytes.length, passwordBytes.length);
        return new StringBuilder(BASIC_AUTHENTICATION_PREFIX).append(javax.xml.bind.DatatypeConverter.printBase64Binary(bytes))
                .toString();
    }
}
