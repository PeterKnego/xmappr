/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;
import org.xlite.XliteException;

import javax.xml.namespace.QName;

/**
 * RootMapper sits at the top of the Mapper hierarchy
 *
 * @author peter
 */
public class RootMapper {

    private QName rootNodeName;
    private MappingContext mappingContext;
    private ElementConverter elementConverter;
    private Class rootClass;

    public RootMapper(QName rootNodeName, Class rootClass, ElementConverter elementConverter, MappingContext mappingContext) {
        this.rootClass = rootClass;
        this.elementConverter = elementConverter;
        this.rootNodeName = rootNodeName;
        this.mappingContext = mappingContext;
    }

    public QName getRootNodeName() {
        return rootNodeName;
    }

    public Class getRootClass() {
        return rootClass;
    }

    public Object getRootObject(XMLSimpleReader reader) {
        QName firstElement = reader.getRootName();
        if (firstElement.equals(rootNodeName)) {
            reader.moveDown();
            return elementConverter.fromElement(reader, mappingContext, "", null, rootClass, null);
        } else {
            throw new XliteException("Error: wrong XML element name. Was expecting root element <" + rootNodeName +
                    "> in input stream, but instead got <" + firstElement + ">.");
        }
    }

    public void toXML(Object object, XMLSimpleWriter writer) {
        writer.predefineNamespaces(mappingContext.getPredefinedNamespaces());
        elementConverter.toElement(object, rootNodeName, writer, mappingContext, "", null);
        writer.endDocument();
    }

}