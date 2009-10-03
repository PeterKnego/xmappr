/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */

package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Test {

    @Test
    public void test() throws UnsupportedEncodingException {

        String str = "abcdefghijklmnopqrst";
        for (int i = 1; i < str.length(); i++) {
            String data = str.substring(0, i);
            String enc = Base64.encode(data.getBytes("UTF-8"));
            String out = new String(Base64.decode(enc), "UTF-8");

            Assert.assertEquals(data, out);
        }

        String str2 = "abcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrstuvwz";
        String enc = Base64.encode(str2.getBytes("UTF-8"));
        String out2 = new String(Base64.decode(enc), "UTF-8");

        Assert.assertEquals(out2, str2);
    }
}
