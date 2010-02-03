/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

import org.xmappr.MappingContext;
import org.xmappr.XMLSimpleReader;
import org.xmappr.XMLSimpleWriter;

import javax.xml.namespace.QName;

/**
 * Classes implementing ElementConverter interface are used for serializing/deserializing xml
 * nodes to Java objects. They are responsible for reading/writing the xml stream on their own.
 * They must always read/write the full xml node including all subnodes.
 */
public interface ElementConverter extends Converter {

    /**
     * Method responsible for reading a complete xml node from XMLSimpleReader and returning deserialized Object
     * that corresponds to this node. When XMLSimpleReader instance is passed to this method it is already
     * positioned on the xml node that is to be converted. Method can inspect all node's attributes, value and subnodes.
     * When method returns, the reader MUST be positioned on the end of the same node as it was when entering method.
     *
     * @param reader         XMLSimpleReader to read XML data from.
     * @param mappingContext
     * @param defaultValue   Default value to use if xml element is empty.
     * @param format         Format string passed in from @XMLelement(format="..."). Used to set conversion options.
     * @param targetType
     * @param targetObject
     * @return               Deserialized object.
     */
    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject);

    /**
     * Takes an Object and serializes it to a XML element and writes it out to XmlSimpleWriter.
     * Method must write out the whole XML element including closing tag.
     *
     * @param object         Object to be serialized. If it is null then no element must be written.
     * @param nodeName       The name of the XML element.
     * @param writer         XmlSimpleWriter to write data to.
     * @param mappingContext
     * @param defaultValue   If serialized object equals default value (for simple elements), then an empty element
     * @param format         Format string passed in from @XMLelement(format="..."). Used to set conversion options.
     */
    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer,
                          MappingContext mappingContext, String defaultValue, String format);

}
