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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class which represents HTTP WWW-Authenticate header, includes parsing from string value.
 *
 * @author Eduardo Martins
 *
 */
public class WWWAuthenticateHeader {

    public enum Type {
        basic, digest
    }

    private final Type type;
    private final Map<String, String> params;

    public WWWAuthenticateHeader(Type type, Map<String, String> params) {
        this.type = type;
        this.params = params;
    }

    public Type getType() {
        return type;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public static WWWAuthenticateHeader parse(String header) throws IllegalArgumentException {

        if (header == null) {
            throw new IllegalArgumentException();
        }

        header = header.trim();

        // get type
        int firstSpace = header.indexOf(' ');
        if (firstSpace < 1) {
            throw new IllegalArgumentException();
        }
        String typeString = header.substring(0, firstSpace).toLowerCase();
        final Type type;
        try {
            type = Type.valueOf(typeString);
        } catch (Throwable t) {
            throw new IllegalArgumentException("No valid auth type found. supported types: " + Arrays.asList(Type.values()));
        }

        // get params
        final Map<String, String> params = new HashMap<String, String>();
        char[] chars = header.substring(firstSpace + 1).trim().toCharArray();
        boolean inKey = true;
        boolean inValue = false;
        boolean quotedValue = false;
        int start = 0;
        String key = null;
        String value = null;
        for (int i = 0; i < chars.length; i++) {
            if (inKey) {
                // key ends on '=' or ' '
                if (chars[i] != '=' && chars[i] != ' ') {
                    continue;
                }
                key = new String(chars, start, i - start).toLowerCase();
                inKey = false;
            } else if (inValue) {
                if (quotedValue) {
                    // quoted value ends on "
                    if (chars[i] != '\"') {
                        continue;
                    }
                } else {
                    // unquoted value ends on ' ' or ','
                    if (chars[i] != ' ' && chars[i] != ',') {
                        continue;
                    }
                }
                value = new String(chars, start, i - start);
                params.put(key, value);
                key = null;
                inValue = false;
            } else {
                // in between ignore ' ' or ',' or '='
                if (chars[i] == ' ' || chars[i] == ',' || chars[i] == '=') {
                    continue;
                }
                if (key == null) {
                    // key parsing start
                    inKey = true;
                    start = i;
                } else {
                    // value parsing start
                    inValue = true;
                    if (chars[i] == '\"') {
                        quotedValue = true;
                        start = i + 1;
                    } else {
                        quotedValue = false;
                        start = i;
                    }
                }
            }
        }

        return new WWWAuthenticateHeader(type, params);
    }
}
