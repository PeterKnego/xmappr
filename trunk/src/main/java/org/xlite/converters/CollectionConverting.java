/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author peter
 */
public interface CollectionConverting {

    public Collection initializeCollection(Class targetType);

    public void addItem(Collection collection, Object object);
    
}
