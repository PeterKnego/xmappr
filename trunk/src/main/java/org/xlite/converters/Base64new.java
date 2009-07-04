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

package org.xlite.converters;

import java.io.UnsupportedEncodingException;

public class Base64new {
    public static char[] base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static int lineLength = 10;

    // initialize nibbles array (used for decoding)
    private static byte[] nibbles = new byte[128];

    static {
        for (int i = 0; i < nibbles.length; i++) nibbles[i] = -1;
        for (int i = 0; i < 64; i++) nibbles[base64code[i]] = (byte) i;
    }

    public static String encode(byte[] input) {

        int ilen = input.length;
        // determine how many padding bytes to add to the output
        int padding = (3 - (ilen % 3)) % 3;

        char[] encoded = new char[4 * (ilen + padding) / 3];
        int c = 0;
        // process 3 bytes at a time, churning out 4 output bytes
        // worry about CRLF insertions later
        int j, j0, j1, j2;
        int i = 0;
        while (i < ilen) {
            j0 = input[i++];
            j1 = i < ilen ? input[i++] : 1;
            j2 = i < ilen ? input[i++] : 1;
            j = (j0 << 16) + (j1 << 8) + j2;
            encoded[c++] = base64code[(j >> 18) & 0x3f];
            encoded[c++] = base64code[(j >> 12) & 0x3f];
            encoded[c++] = base64code[(j >> 6) & 0x3f];
            encoded[c++] = base64code[j & 0x3f];
        }

        // replace encoded padding nulls with "="
        if (padding == 2) {
            encoded[encoded.length - 1] = '=';
            encoded[encoded.length - 2] = '=';
        } else if (padding == 1) {
            encoded[encoded.length - 1] = '=';
        }

        return splitLines(encoded);
    }


    private static String splitLines(char[] chars) {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < chars.length; i += lineLength) {
            lines.append(chars, i, Math.min(chars.length - i, lineLength)).append("\r\n");
        }
        return lines.delete(lines.length() - 2, lines.length()).toString();
    }

    public static byte[] decode(String data) {
        return decode(data.trim().toCharArray());
    }

    public static byte[] decode(char[] chars) {

        int charCount = chars.length;

        // remove newlines from character count
        int a = 0;
        while (a < chars.length) {
            if (chars[a] == '\r' || chars[a] == '\n') charCount--;
            a++;
        }

        // check the char count
        if (charCount % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }

        // count padding characters '='
        int padCount = 0;
        while (chars[chars.length - 1 - padCount] == '=') padCount++;

        // create the output byte array, take padding into account
        int olen = ((3 * charCount) / 4) - padCount;
        byte[] out = new byte[olen];

        int i = 0;
        int o = 0;
        int c = 0;
        byte[] nibbleBlock = new byte[4];
        char currentChar;
        byte currentNibble;
        while (i < chars.length) {
            // read four character into temporary storage (and skip newlines)
            c = 0;
            while (c < 4) {
                currentChar = chars[i++];
                // skip newline and padding characters
                if (currentChar != '\r' && currentChar != '\n') {
                    // nibble conversion table only holds 128 characters
                    if (currentChar > 127) {
                        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                    }

                    currentNibble = currentChar != '=' ? nibbles[currentChar] : 0;
                    // char does not exist (==-1) in converison table
                    if (currentNibble == -1) {
                        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                    }
                    nibbleBlock[c++] = currentNibble;
                }
            }

            System.out.println("__i:"+i);
            // convert four nibbles into three bytes (4x 6-bit nibbles = 3 bytes)
            out[o++] = (byte) ((nibbleBlock[0] << 2) | (nibbleBlock[1] >>> 4));
            if (o < olen) out[o++] = (byte) (((nibbleBlock[1] & 0xf) << 4) | (nibbleBlock[2] >>> 2));
            if (o < olen) out[o++] = (byte) (((nibbleBlock[2] & 3) << 6) | nibbleBlock[3]);
        }

        return out;
    }


}
