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

package org.jboss.ejb.client.test.client.http;

import java.net.URL;

import org.jboss.ejb.client.http.DigestHttpAuthentication;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests digest authentication computation.
 *
 * @author Eduardo Martins
 */
public class DigestAuthenticationTestCase {

    @Test
    public void testRFC2617DigestExample() throws Exception {
        URL url = new URL("http://www.nowhere.org/dir/index.html");
        String computedAuthentication = new DigestHttpAuthentication(url.getFile(), "GET", "testrealm@host.com",
                "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", "auth", "0a4f113b")
                .getAuthorization("Mufasa", "Circle Of Life".toCharArray());
        String expectedAuthenticaton = "Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", nc=00000001, uri=\"/dir/index.html\", response=\"6629fae49393a05397450978507c4ef1\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\", cnonce=\"0a4f113b\", qop=\"auth\"";
        Assert.assertEquals(computedAuthentication, expectedAuthenticaton);
    }

}
