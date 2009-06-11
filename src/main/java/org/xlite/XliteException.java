package org.xlite;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 10:15:47 PM
 */
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
