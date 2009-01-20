package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;

import javax.xml.namespace.QName;

/**
 * Classes implementing ElementConverter interface are used for serializing/deserializing xml
 * nodes to Java objects. They are responsible for reading/writing the xml stream on their own.
 * They must always read/write the full xml node including all subnodes.
 */
public interface ElementConverter {

    /**
     * Indicates whether an implementation of NodeConverter can convert xml node to given Class.
     * If it can then it returns an instance of NodeConverter. Otherwise it returns null.
     *
     * @param type
     * @return
     */
    public boolean canConvert(Class type);

    /**
     * Method responsible for reading a complete xml node from XMLSimpleReader and returning deserialized Object
     * that corresponds to this node. When XMLSimpleReader instance is passed to this method it is already
     * positioned on the xml node that is to be converted. Method can inspect all node's attributes, value and subnodes.
     * When method returns, the reader MUST be positioned on the same node as it was when entering method.
     *
     * @param reader
     * @param mappingContext
     * @param defaultValue
     * @param defaultValue
     * @return
     */
    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue);

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer,
                          MappingContext mappingContext, String defaultValue);

}
