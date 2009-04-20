package org.xlite.converters;

import org.xlite.XMLSimpleReader;
import org.xlite.MappingContext;
import org.xlite.XliteException;
import org.xlite.XMLSimpleWriter;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 7:05:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapConverter implements ElementConverter, MapConverting {

    public boolean canConvert(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue) {
//        ElementConverter converter = mappingContext.lookupElementConverter(targetType);
//        return converter.fromElement(reader, mappingContext);
        throw new XliteException("MapConverter.fromElement() method should not be called directly. " +
                "Rather for every object in a map, a .fromElement() method should be called on it's assigned converter");
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue) {
        throw new XliteException("MapConverter.toElement() method should not be called directly. " +
                "Rather for every object in a map, a .toElement() method should be called on it's assigned converter");
    }

    public Map initializeMap(Class targetType) {
        // is target class a Collection?
        if (!Map.class.isAssignableFrom(targetType)) {
            throw new XliteException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Map!");
        }
        Class<? extends Map> concreteType = getConcreteMapType(targetType);
        try {
            return concreteType.newInstance();
        } catch (InstantiationException e) {
            throw new XliteException("Could not instantiate map " + targetType.getName() + ". ", e);
        } catch (IllegalAccessException e) {
            throw new XliteException("Could not instantiate map " + targetType.getName() + ". ", e);
        }
    }

    public void addItem(Map<QName, Object> map, QName key, Object object) {
        map.put(key, object);
    }

    public Iterator getIterator(Map map) {
        return map.entrySet().iterator();
    }


    private Class<? extends Map> getConcreteMapType(Class<? extends Map> targetType) {
        if (targetType == Map.class) {
            return HashMap.class;
        }
        //todo add more Interface-Class mappings
        return targetType;
    }

}

