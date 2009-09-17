/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.RootMapper;

import javax.xml.namespace.QName;

public interface Configuration {

    RootMapper getRootElementMapper(QName name);

    RootMapper getRootElementMapper(Class sourceClass);

    public void initialize();

    boolean isPrettyPrint();

    void setPrettyPrint(boolean prettyPrint);

    void addNamespace(String namespace);

    void addMapping(Class rootClass);

}
