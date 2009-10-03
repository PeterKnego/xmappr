/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.*;

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

    public RootMapper(QName rootNodeName, Class rootClass, MappingContext mappingContext) {
        this.rootClass = rootClass;
        elementConverter = mappingContext.lookupElementConverter(rootClass);
        this.rootNodeName = rootNodeName;
        this.mappingContext = mappingContext;
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