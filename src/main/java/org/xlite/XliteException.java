/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

public class XliteException extends RuntimeException {

    private Throwable cause;

    public XliteException(Throwable cause) {
        this("", cause);
    }

    public XliteException(String message) {
        this(message, null);
    }

    public XliteException(String message, Throwable cause) {
        super(message + (cause == null ? "" : " : " + cause.getMessage()));
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
