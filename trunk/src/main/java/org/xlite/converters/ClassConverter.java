/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.*;

import javax.xml.namespace.QName;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

//todo Write javadoc

/**
 * This is a default ElementConverter that tries to convert any Class based on injected mappers.
 * User: peter
 * Date: Feb 28, 2008
 * Time: 10:19:19 PM
 */
public class ClassConverter implements ElementConverter {


    private Class<?> targetClass;

    private TextMapper textMapper;

    private ElementMapper elementCatcher;

    private AttributeMapper attributeCatcher;

    private Map<QName, ElementMapper> elementMappersByName = new LinkedHashMap<QName, ElementMapper>();

    private Map<QName, AttributeMapper> attributeMappers = new LinkedHashMap<QName, AttributeMapper>();

    private NsContext classNamespaces;

    public ClassConverter(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public NsContext getClassNamespaces() {
        return classNamespaces;
    }

    public void setClassNamespaces(NsContext classNamespaces) {
        this.classNamespaces = classNamespaces;
    }

    public void setElementCatcher(ElementMapper elementCatcher) {
        this.elementCatcher = elementCatcher;
    }

    public void setAttributeCatcher(AttributeMapper attributeCatcher) {
        this.attributeCatcher = attributeCatcher;
    }

    public void setTextMapper(TextMapper textMapper) {
        this.textMapper = textMapper;
    }

    public void addElementMapper(QName qName, ElementMapper elementMapper) {
        elementMappersByName.put(qName, elementMapper);
//        elementMappers.add(elementMapper);
    }

    public void addAttributeMapper(QName attributeQName, AttributeMapper attributeMapper) {
        attributeMappers.put(attributeQName, attributeMapper);
    }

    public boolean canConvert(Class type) {
        return targetClass.equals(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject) {

        Object currentObject;

        // checks if targetObject if provided and checks if it is of correct type (gets rid of Collections or Maps)
        if (targetObject != null && targetClass.isAssignableFrom(targetObject.getClass())) {
           currentObject = targetObject;
        } else {
            // instantiate object that maps to the current XML element
            try {
                currentObject = targetClass.newInstance();
            } catch (Exception e) {
                throw new XliteException("Could not instantiate class " + targetClass.getName(), e);
            }
        }

        // XML element attributes
        Iterator<Map.Entry<QName, String>> attributeSet = reader.getAttributeIterator();
        while (attributeSet.hasNext()) {
            Map.Entry<QName, String> entry = attributeSet.next();
            QName attrQName = entry.getKey();
            String attrValue = entry.getValue();

            // find the attribute mapper
            AttributeMapper attrMapper = attributeMappers.get(attrQName);

//            if (attrValue.length() != 0) {
            // if mapper for this attribute is defined, use it to setValue field to attribute value
            if (attrMapper != null) {
                attrMapper.setValue(attrQName, currentObject, attrValue);
            } else if (attributeCatcher != null) { // if there is a Mapper defined thet catches any attribute name
                attributeCatcher.setValue(attrQName, currentObject, attrValue);
            }
//            }
//            System.out.println("ATTR: " + attrQName);
        }

        String text = reader.getText();
        if (text.length() != 0 && textMapper != null)
            textMapper.setValue(currentObject, text);

        // XML subelements
        QName qname;
        while (reader.moveDown()) {
            qname = reader.getName();
//          String  name = qname.getPrefix().length() == 0 ? qname.getLocalPart() : (qname.getPrefix() + ":" + qname.getLocalPart());

            // find ElementMapper for converting XML element with given name
            ElementMapper subMapper = elementMappersByName.get(qname);
            if (subMapper != null) {  // converter is found
//                System.out.println("START:" + name + " thisConverter:" + this.toString() +
//                        " subConverter:" + subMapper.valueConverter);
                subMapper.readElement(qname, currentObject, reader);

            } else if (elementCatcher != null) {
                elementCatcher.readElement(qname, currentObject, reader);

            } else { // unknown subMapper
                reader.saveSubTree(currentObject);
            }
//            String nm = "null";
//            nm = (reader.reader.getEventType() == 1 || reader.reader.getEventType() == 2) ? reader.reader.getName().getLocalPart() : "";
//            System.out.println("BEFORE moveUp: "+reader.reader.getEventType()+" "+nm);
            reader.moveUp();

            if (textMapper != null && textMapper.isIntermixed()) {
                text = reader.getText();
                if (text.length() != 0) textMapper.setValue(currentObject, text);
            }

        }

        return currentObject;
    }

    public void toElement(Object object, QName elementName, XMLSimpleWriter writer,
                          MappingContext mappingContext, String defaultValue, String format) {

        if (object == null) {
            return;
        }

        // write a start tag
        writer.startElement(elementName);

        // write directly mapped attributes
        for (QName attrName : attributeMappers.keySet()) {
            AttributeMapper mapper = attributeMappers.get(attrName);
            String value = mapper.getValue(attrName, object);
            if (value != null) {
                writer.addAttribute(attrName, value);
            }
        }
        // handle attributeCatcher (wildcard mapped attributes)
        if (attributeCatcher != null) {
            // is target a Map?
            if (attributeCatcher.isTargetMap()) {
                Map attrs = (Map) attributeCatcher.getTarget(object);
                QName aName;
                for (Object key : attrs.keySet()) {
                    // check type of attribute name
                    if (QName.class == key.getClass()) {
                        aName = (QName) key;
                    } else if (String.class == key.getClass()) {
                        aName = new QName((String) key);
                    } else {
                        continue;
                    }
                    // check that this attribute name is not already handled by direct mappers
                    if (!attributeMappers.containsKey(aName)) {
                        String value = attributeCatcher.getValue(key, object);
                        if (value != null) {
                            writer.addAttribute(aName, value);
                        }
                    }
                }
            }
        }


        // write element's value
        if (textMapper != null && !textMapper.isIntermixed()) {
            writer.addText(textMapper.getValue(object));
        }

        // write subelements
        Map<ElementMapper, Integer> alreadyProcessed = new IdentityHashMap<ElementMapper, Integer>();
        for (QName subName : elementMappersByName.keySet()) {
            ElementMapper elementMapper = elementMappersByName.get(subName);
            if (!alreadyProcessed.containsKey(elementMapper)) {
                elementMapper.writeElement(object, subName, writer, textMapper);
                alreadyProcessed.put(elementMapper, 0);
            }
        }

        // write  unknown (stored) subelements
        writer.restoreSubTrees(object);

        // write end tag
        writer.endElement();
    }

//    public void printContents(String prefix) {
//        prefix += " ";
//        for (Map.Entry<QName, ValueMapper> attrEntry : attributeMappers.entrySet()) {
//            System.out.println(prefix + "attribute:" + attrEntry.getKey()
//                    + " field:" + attrEntry.getValue().targetField.getName() + "(" + attrEntry.getValue().targetField.getType() + ")");
//        }
//
//        for (Map.Entry<QName, ElementMapper> elementEntry : elementMappersByName.entrySet()) {
//            System.out.println(prefix + "element:" + elementEntry.getKey());
//        }
//
//    }

}


