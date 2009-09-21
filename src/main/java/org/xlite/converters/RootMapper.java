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

        // check class namespaces of root element
        if (rootNodeName.getNamespaceURI().length() == 0
                && elementConverter instanceof AnnotatedClassConverter) {
            NsContext classNS = ((AnnotatedClassConverter) elementConverter).getClassNamespaces();
            this.rootNodeName = new QName(classNS.getNamespaceURI(rootNodeName.getPrefix()),
                    rootNodeName.getLocalPart(),
                    rootNodeName.getPrefix());
        } else {
            this.rootNodeName = rootNodeName;
        }
        this.mappingContext = mappingContext;
    }

    public Object getRootObject(XMLSimpleReader reader) {
        QName firstElement = reader.getRootName();
        if (firstElement.equals(rootNodeName)) {
            reader.moveDown();
            return elementConverter.fromElement(reader, mappingContext, "", null, rootClass, null);
        } else {
            throw new XliteException("Error: wrong XML element name. Was expecting root element <" + rootNodeName +
                    "> in input stream, but intead got <" + firstElement + ">.");
        }
    }

    public void toXML(Object object, XMLSimpleWriter writer) {
        writer.predefineNamespaces(mappingContext.getPredefinedNamespaces());
        elementConverter.toElement(object, rootNodeName, writer, mappingContext, "", null);
        writer.endDocument();
    }

}