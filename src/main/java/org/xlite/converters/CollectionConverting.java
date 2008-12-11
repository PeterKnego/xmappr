package org.xlite.converters;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author peter
 */
public interface CollectionConverting {
    public Collection initializeCollection(Class targetType);
    public void addItem(Collection collection, Object object);
    public Iterator getIterator(Collection collection);
}
