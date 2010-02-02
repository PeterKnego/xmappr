/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import java.nio.charset.Charset;

public class XmlStreamSettings {

    // default settings
    public XmlStreamSettings() {
        this.encoding = "UTF-8";
        this.version = "1.0";
    }

    public String encoding;
    public String version;
    public boolean writeHeader;

}
