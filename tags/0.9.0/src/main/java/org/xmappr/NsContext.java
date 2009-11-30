/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author peter
 */
public class NsContext implements Iterable<Map.Entry<String, String>> {

    private Map<String, String> prefixToNS = new HashMap<String, String>();

    public void addNamespace(String namespace) {

        int index = namespace.indexOf('=');
        String prefix, nsURI;
        if (index > 0) {  // with prefix
            prefix = namespace.substring(0, index);
            nsURI = namespace.substring(index + 1, namespace.length());

        } else if (index == 0) { // empty prefix (no prefix defined - e.g ":namespaceURL")
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            nsURI = namespace.substring(1, namespace.length());

        } else { // no prefix given
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            nsURI = namespace;
        }
//        System.out.println("namespace nsURi=" + nsURI + " prefix=" + prefix);
        prefixToNS.put(prefix, nsURI);
    }

    public void addNamespace(String prefix, String namespaceURI) {
        if (prefix == null || prefix.length() == 0) {
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
        }
        prefixToNS.put(prefix, namespaceURI);
    }


    public Iterator<Map.Entry<String, String>> iterator() {
        return prefixToNS.entrySet().iterator();
    }

    public String getNamespaceURI(String prefix) {
        return prefixToNS.get(prefix);
    }

}
