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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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
public class DigestHttpAuthentication implements HttpAuthentication {

    private static final String USER_PROMPT = "User:";
    private static final String PASSWORD_PROMPT = "Password:";

    private final AtomicInteger nc = new AtomicInteger(0);

    private final String uri;
    private final String method;
    private final String cnonce;
    private final String realm;
    private String nonce;
    private final String opaque;
    private final boolean authQop;

    public DigestHttpAuthentication(String uri, String method, String realm, String nonce, String opaque, String qop,
            String cnonce) {
        this.uri = uri;
        this.method = method;
        this.cnonce = cnonce;
        this.realm = realm;
        this.nonce = nonce;
        this.opaque = opaque;
        if (qop != null) {
            String[] qopParts = qop.split(",");
            boolean found = false;
            for (String qopPart : qopParts) {
                if (qopPart.equals("auth")) {
                    found = true;
                    break;
                }
            }
            authQop = found;
        } else {
            authQop = false;
        }
    }

    public void resetNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAuthorization(CallbackHandler callbackHandler) throws IOException {

        final NameCallback nameCallback = new NameCallback(USER_PROMPT);
        final PasswordCallback passwordCallback = new PasswordCallback(PASSWORD_PROMPT, false);
        try {
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
        } catch (UnsupportedCallbackException e) {
            throw new IOException(e);
        }
        // get user name
        final String userName = nameCallback.getName();
        if (userName == null) {
            throw new IOException("null authentication username");
        }
        // get password
        final char[] password = passwordCallback.getPassword();
        if (password == null) {
            throw new IOException("null authentication password");
        }
        return getAuthorization(userName, password);
    }

    public String getAuthorization(String userName, char[] password) throws IOException {

        int nccount = nc.incrementAndGet();
        String ncstring = null;
        if (nccount != -1) {
            ncstring = Integer.toHexString(nccount).toLowerCase();
            int len = ncstring.length();
            if (len < 8)
                ncstring = zeroPad[len] + ncstring;
        }

        String response;
        try {
            response = computeDigest(userName, password, realm, method, uri, nonce, cnonce, ncstring, authQop);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

        final StringBuilder value = new StringBuilder("Digest username=\"").append(userName);
        value.append("\", realm=\"").append(realm);
        value.append("\", nonce=\"").append(nonce);
        if (authQop) {
            value.append("\", nc=").append(ncstring);
        } else {
            value.append("\"");
        }
        value.append(", uri=\"").append(uri);
        value.append("\", response=\"").append(response);
        if (opaque != null) {
            value.append("\", opaque=\"").append(opaque);
        }
        if (cnonce != null) {
            value.append("\", cnonce=\"").append(cnonce);
        }
        if (authQop) {
            value.append("\", qop=\"auth");
        }
        value.append("\"");
        return value.toString();
    }

    private String computeDigest(String userName, char[] password, String realm, String connMethod, String requestURI,
            String nonceString, String cnonce, String ncValue, boolean qop) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {

        final MessageDigest md = MessageDigest.getInstance("MD5");

        final String a1 = new StringBuilder(userName).append(':').append(realm).append(':').toString();
        final String hashA1 = encode(a1, password, md);

        final String a2 = new StringBuilder(connMethod).append(':').append(requestURI).toString();
        final String hashA2 = encode(a2, null, md);

        final String combo;
        if (qop) {
            // RRC2617 qop=auth
            combo = new StringBuilder(hashA1).append(':').append(nonceString).append(':').append(ncValue).append(':')
                    .append(cnonce).append(":auth:").append(hashA2).toString();
        } else {
            // RFC2069 fallback
            combo = new StringBuilder(hashA1).append(':').append(nonceString).append(':').append(hashA2).toString();
        }

        return encode(combo, null, md);

    }

    private static final char[] charArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final String[] zeroPad = {
            // 0 1 2 3 4 5 6 7
            "00000000", "0000000", "000000", "00000", "0000", "000", "00", "0" };

    private String encode(String src, char[] passwd, MessageDigest md) throws UnsupportedEncodingException {
        md.update(src.getBytes("ISO-8859-1"));
        if (passwd != null) {
            byte[] passwdBytes = new byte[passwd.length];
            for (int i = 0; i < passwd.length; i++)
                passwdBytes[i] = (byte) passwd[i];
            md.update(passwdBytes);
            Arrays.fill(passwdBytes, (byte) 0x00);
        }
        byte[] digest = md.digest();
        StringBuilder res = new StringBuilder(digest.length * 2);
        for (int i = 0; i < digest.length; i++) {
            int hashchar = ((digest[i] >>> 4) & 0xf);
            res.append(charArray[hashchar]);
            hashchar = (digest[i] & 0xf);
            res.append(charArray[hashchar]);
        }
        return res.toString();
    }

}
