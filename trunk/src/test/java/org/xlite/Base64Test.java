/*
 * This is "Open Source" software and released under the following license:
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.xlite;

import org.xlite.converters.Base64;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.UnsupportedEncodingException;

public class Base64Test {

    @Test
    public void test() throws UnsupportedEncodingException {

//        String str = "abcdefghijklmnopqrst";
//        for (int i = 1; i < str.length(); i++) {
//            String data = str.substring(0, i);
//            String enc = Base64.encode(data.getBytes("UTF-8"));
//            String out = new String(Base64.decode(enc), "UTF-8");
//
//            Assert.assertEquals(data, out);
//        }

//        String str2 = "abcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrsabcdefghijklmnopqrstuvwz";
        String str2 = "aaaaaaaaa aaaaaaaaa aaaaaaaaa bbbbb";
        String enc = Base64.encode(str2.getBytes("UTF-8"));
        String out2 = new String(Base64.decode(enc), "UTF-8");

        Assert.assertEquals(out2, str2);
    }
}
