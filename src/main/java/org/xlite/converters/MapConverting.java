package org.xlite.converters;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 7:10:38 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MapConverting {

     public Map initializeMap(Class targetType);

    public void addItem(Map<QName, Object> collection, QName key, Object object);

    public Iterator getIterator(Map collection);
}
