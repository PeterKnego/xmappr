package org.xlite;

import org.xlite.converters.AnnotatedClassConverter;

/**
 * Utility methods for copying arrays. Needed because they are not present in Java 1.5
 * User: peter
 * Date: Feb 23, 2008
 * Time: 5:57:10 PM
 */
public class ArrayUtil {

    public static byte[] arrayCopy(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    public static byte[][] arrayCopy(byte[][] original, int newLength) {
        byte[][] copy = new byte[newLength][];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    public static AnnotatedClassConverter[] arrayCopy(AnnotatedClassConverter[] original, int newLength) {
        AnnotatedClassConverter[] copy = new AnnotatedClassConverter[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }
}
