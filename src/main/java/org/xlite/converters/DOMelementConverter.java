package org.xlite.converters;

import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;
import org.xlite.MappingContext;
import org.xlite.DOMelement;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 16, 2009
 * Time: 9:54:04 PM
 */
public class DOMelementConverter implements ElementConverter {

    public boolean canConvert(Class type) {
        return DOMelement.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue) {
        DOMelement element = new DOMelement();
        //todo process attributres and text here
        while (reader.moveDown()) {
            //todo process subelements
            reader.moveUp();
        }
        return element;
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue) {

    }
}
