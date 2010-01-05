/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

import java.util.Collection;

/**
 * @author peter
 */
public interface CollectionConverting {

    public Collection initializeCollection(Class targetType);

    public void addItem(Collection collection, Object object);

}
