/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

public class XmlStreamSettings {

    // default settings
    public XmlStreamSettings() {
        this.encoding = "UTF-8";
        this.version = "1.0";
    }

    public String encoding;
    public String version;
}
